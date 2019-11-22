package com.huatu.ztk.smc.flume;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import static com.huatu.ztk.smc.flume.CustomDelimCursor.DelimMode;

/**
 * This "tail"s a filename. Like a unix tail utility, it will wait for more
 * information to come to the file and periodically dump data as it is written.
 * It assumes that each line is a separate event.
 * <p/>
 * This is for legacy log files where the file system is the only mechanism
 * flume has to get events. It assumes that there is one entry per line (per
 * \n). If a file currently does not end with \n, it will remain buffered
 * waiting for more data until either a different file with the same name has
 * appeared, or the tail source is closed.
 * <p/>
 * It also has logic to deal with file rotations -- if a file is renamed and
 * then a new file is created, it will shift over to the new file. The current
 * file is read until the file pointer reaches the end of the file. It will wait
 * there until periodic checks notice that the file has become longer. If the
 * file "shrinks" we assume that the file has been replaced with a new log file.
 * <p/>
 * TODO (jon) This is not perfect.
 * <p/>
 * This reads bytes and does not assume any particular character encoding other
 * than that entry are separated by new lines ('\n').
 * <p/>
 * There is a possibility for inconsistent conditions when logs are rotated.
 * <p/>
 * 1) If rotation periods are faster than periodic checks, a file may be missed.
 * (this mimics gnu-tail semantics here)
 * <p/>
 * 2) Truncations of files will reset the file pointer. This is because the Java
 * file api does not a mechanism to get the inode of a particular file, so there
 * is no way to differentiate between a new file or a truncated file!
 * <p/>
 * 3) If a file is being read, is moved, and replaced with another file of
 * exactly the same size in a particular window and the last mod time of the two
 * are identical (this is often at the second granularity in FS's), the data in
 * the new file may be lost. If the original file has been completely read and
 * then replaced with a file of the same length this problem will not occur.
 * (See TestTailSource.readRotatePrexistingFailure vs
 * TestTailSource.readRotatePrexistingSameSizeWithNewModetime)
 * <p/>
 * Ideally this would use the inode number of file handle number but didn't find
 * java api to get these, or Java 7's WatchService file watcher API.
 */
public class TailSource extends EventSource.Base {
    private static final Logger LOG = LoggerFactory.getLogger(TailSource.class);
    public static final String A_TAILSRCFILE = "tailSrcFile";

    private static long tailPollPeriod = 10;
    private static int thdCount = 0;
    private volatile boolean done = false;

    private final long sleepTime; // millis
    private final List<Cursor> cursors = new ArrayList<Cursor>();
    private final List<Cursor> newCursors = new ArrayList<Cursor>();
    private final List<Cursor> rmCursors = new ArrayList<Cursor>();

    // We "queue" only allowing a single Event.
    private final SynchronousQueue<Event> sync = new SynchronousQueue<Event>();
    private TailThread thd = null;

    /**
     * Constructor for backwards compatibility.
     */
    public TailSource(File f, long offset, long sleepTime) {
        this(f, offset, sleepTime, false);
    }

    /**
     * Specify the file, the starting offset (something >=0) and wait time between
     * checks in millis. If startFromEnd is set, begin reading the file at the
     * end, not the beginning.
     */
    public TailSource(File f, long offset, long sleepTime, boolean startFromEnd) {
        Preconditions.checkArgument(offset >= 0 || startFromEnd,
                "offset needs to be >=0 or startFromEnd needs to be true");
        Preconditions.checkNotNull(f);
        Preconditions.checkArgument(sleepTime > 0);
        this.sleepTime = sleepTime;

        // add initial cursor.
        long fileLen = f.length();
        long readOffset = startFromEnd ? fileLen : offset;
        long modTime = f.lastModified();
        Cursor c = new Cursor(sync, f, readOffset, fileLen, modTime);
        addCursor(c);

    }

    /**
     * Custom delimiter version *
     */
    public TailSource(File f, long offset, long waitTime, boolean startFromEnd,
                      String regex, DelimMode dm) {
        Preconditions.checkArgument(f != null, "Null File is an illegal argument");
        Preconditions.checkArgument(waitTime > 0,
                "waitTime <=0 is an illegal argument");
        Preconditions.checkArgument(regex != null,
                "Null regex is an illegal argument");
        Preconditions.checkArgument(dm != null,
                "Null Delimiter mode is an illegal argument");
        this.sleepTime = waitTime;

        // add initial cursor.
        long fileLen = f.length();
        long readOffset = startFromEnd ? fileLen : offset;
        long modTime = f.lastModified();

        Cursor c = new CustomDelimCursor(sync, f, readOffset, fileLen, modTime,
                regex, dm);
        addCursor(c);
    }

    /**
     * This creates an empty tail source. It expects something else to add cursors
     * to it
     */
    public TailSource(long waitTime) {
        this.sleepTime = waitTime;
    }

    /**
     * This is the main driver thread that runs through the file cursor list
     * checking for updates and sleeping if there are none.
     */
    private class TailThread extends Thread {

        TailThread() {
            super("TailThread-" + thdCount++);
        }

        @Override
        public void run() {
            try {
                // initialize based on initial settings.
                for (Cursor c : cursors) {
                    c.initCursorPos();
                }

                while (!done) {
                    synchronized (newCursors) {
                        cursors.addAll(newCursors);
                        newCursors.clear();
                    }

                    synchronized (rmCursors) {
                        cursors.removeAll(rmCursors);
                        for (Cursor c : rmCursors) {
                            c.flush();
                        }
                        rmCursors.clear();
                    }

                    boolean madeProgress = false;
                    for (Cursor c : cursors) {
                        LOG.debug("Progress loop: " + c.file);
                        if (c.tailBody()) {
                            madeProgress = true;
                        }
                    }

                    if (!madeProgress) {
                        Clock.sleep(sleepTime);
                    }
                }
                LOG.debug("Tail got done flag");
            } catch (InterruptedException e) {
                LOG.error("Tail thread nterrupted: " + e.getMessage(), e);
            } finally {
                LOG.info("TailThread has exited");
            }
        }
    }

    /**
     * Add another file Cursor to tail concurrently.
     */
    synchronized void addCursor(Cursor cursor) {
        Preconditions.checkArgument(cursor != null);

        if (thd == null) {
            cursors.add(cursor);
            LOG.debug("Unstarted Tail has added cursor: " + cursor.file.getName());

        } else {
            synchronized (newCursors) {
                newCursors.add(cursor);
            }
            LOG.debug("Tail added new cursor to new cursor list: "
                    + cursor.file.getName());
        }

    }

    /**
     * Remove an existing cursor to tail.
     */
    synchronized public void removeCursor(Cursor cursor) {
        Preconditions.checkArgument(cursor != null);
        if (thd == null) {
            cursors.remove(cursor);
        } else {

            synchronized (rmCursors) {
                rmCursors.add(cursor);
            }
        }

    }

    @Override
    public void close() throws IOException, InterruptedException {
        synchronized (this) {
            done = true;
            if (thd == null) {
                LOG.warn("TailSource double closed");
                return;
            }
            while (thd.isAlive()) {
                thd.join(100L);
                thd.interrupt();
            }
            thd = null;
        }
    }

    /**
     * This function will block when the end of all the files it is trying to tail
     * is reached.
     */
    @Override
    public Event next() throws IOException, InterruptedException {
        try {
            while (!done) {
                // This blocks on the synchronized queue until a new event arrives.
                Event e = sync.poll(100, TimeUnit.MILLISECONDS);
                if (e == null)
                    continue; // nothing there, retry.
                updateEventProcessingStats(e);
                return e;
            }
            return null; // closed
        } catch (InterruptedException e1) {
            LOG.warn("next unexpectedly interrupted :" + e1.getMessage(), e1);
            throw e1;
        }
    }

    @Override
    public synchronized void open() throws IOException {
        if (thd != null) {
            throw new IllegalStateException("Attempted to open tail source twice!");
        }
        thd = new TailThread();
        thd.start();
    }

    /**
     * This takes a context and extracts the delimiter regex and dilimiter mode.
     * If no mode is specified it defaults to EXCLUDE mode. If no regex is
     * specified, null is returned.
     */
    public static Pair<String, DelimMode> extractDelimContext(Context ctx) {
        String delimRegex = ctx.getValue("delim");
        if (delimRegex == null) {
            // don't have a regex, return null;
            return null;
        }

        // figure out mode, and delimiters
        String delimModeStr = ctx.getValue("delimMode");
        DelimMode delimMode = DelimMode.EXCLUDE; // default to exclude mode
        if (delimModeStr != null) {
            if ("exclude".equals(delimModeStr)) {
                delimMode = DelimMode.EXCLUDE;
            } else if ("prev".equals(delimModeStr)) {
                delimMode = DelimMode.INCLUDE_PREV;
            } else if ("next".equals(delimModeStr)) {
                delimMode = DelimMode.INCLUDE_NEXT;
            }
        }
        return new Pair<String, DelimMode>(delimRegex, delimMode);

    }

    public static SourceFactory.SourceBuilder builder() {
        return new SourceFactory.SourceBuilder() {

            @Override
            public EventSource build(Context ctx, String... argv) {
                if (argv.length != 1 && argv.length != 2) {
                    throw new IllegalArgumentException(
                            "usage: tail(filename, [startFromEnd] {, delim=\"regex\", delimMode=\"exclude|prev|next\"}) ");
                }
                boolean startFromEnd = false;
                if (argv.length == 2) {
                    startFromEnd = Boolean.parseBoolean(argv[1]);
                }

                // delim regex, delim mode
                Pair<String, DelimMode> mode = extractDelimContext(ctx);
                if (mode == null) {
                    // normal '\n' delimiter in exclude mode
                    return new TailSource(new File(argv[0]), 0, tailPollPeriod, startFromEnd);
                }

                return new TailSource(new File(argv[0]), 0, tailPollPeriod, startFromEnd, mode.getLeft(), mode.getRight());
            }
        };
    }

    public static SourceFactory.SourceBuilder multiTailBuilder() {
        return new SourceFactory.SourceBuilder() {

            @Override
            public EventSource build(Context ctx, String... argv) {
                Preconditions.checkArgument(argv.length >= 1,
                        "usage: multitail(file1[, file2[, ...]]) ");
                boolean startFromEnd = false;
                long pollPeriod = tailPollPeriod;
                TailSource src = null;

                // delim regex, delim mode
                Pair<String, DelimMode> mode = extractDelimContext(ctx);

                for (int i = 0; i < argv.length; i++) {
                    if (mode == null) {
                        // default '\n' exclude mode
                        if (src == null) {
                            src = new TailSource(new File(argv[i]), 0, pollPeriod,
                                    startFromEnd);
                        } else {
                            src.addCursor(new Cursor(src.sync, new File(argv[i])));
                        }
                    } else {
                        // custom delimiters and delimiter modes
                        if (src == null) {
                            src = new TailSource(new File(argv[i]), 0, pollPeriod,
                                    startFromEnd, mode.getLeft(), mode.getRight());
                        } else {
                            src.addCursor(new CustomDelimCursor(src.sync, new File(argv[i]),
                                    mode.getLeft(), mode.getRight()));
                        }

                    }
                }
                return src;
            }
        };
    }

}
