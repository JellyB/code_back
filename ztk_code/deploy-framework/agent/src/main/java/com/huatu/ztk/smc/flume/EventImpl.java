package com.huatu.ztk.smc.flume;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * A simple in memory implementation of an event.
 * <p/>
 * I'm limiting a message to be at most 32k
 */
public class EventImpl extends EventBaseImpl {
    private byte[] body;
    private long timestamp;
    private Priority pri;
    private long nanos;
    private String host;

    final static long MAX_BODY_SIZE = 1000000;

    /**
     * Reflection based net.opentsdb.tools (like Avro) require a null constructor
     */
    public EventImpl() {
        this(new byte[0], 0, Priority.INFO, 0, "");
    }

    /**
     * Copy constructor for converting events into EventImpl (required for
     * reflection/Avro)
     */
    public EventImpl(Event e) {
        this(e.getBody(), e.getTimestamp(), e.getPriority(), e.getNanos(), e
                .getHost(), new HashMap<String, byte[]>(e.getAttrs()));
    }

    /**
     * Constructs a new event wrapping (not copying!) the provided byte array
     */
    public EventImpl(byte[] s) {
        this(s, Clock.unixTime(), Priority.INFO, Clock.nanos(), NetUtils
                .localhost());
    }

    /**
     * Constructs a new event wrapping (not copying!) the provided byte array
     */
    public EventImpl(byte[] s, Priority pri) {
        this(s, Clock.unixTime(), pri, Clock.nanos(), NetUtils.localhost());
    }

    /**
     * Constructs a new event wrapping (not copying!) the provided byte array
     */
    public EventImpl(byte[] s, long timestamp, Priority pri, long nanoTime,
                     String host) {
        this(s, timestamp, pri, nanoTime, host, new HashMap<String, byte[]>());
    }

    /**
     * Constructs a new event wrapping (not copying!) the provided byte array
     */
    public EventImpl(byte[] s, long timestamp, Priority pri, long nanoTime,
                     String host, Map<String, byte[]> fields) {
        super(fields);
        Preconditions.checkNotNull(s,
                "Failed when attempting to create event with null body");
        Preconditions.checkArgument(s.length <= MAX_BODY_SIZE, "Failed when "
                + "attempting to create event with body with length (" + s.length
                + ") > max body size (" + MAX_BODY_SIZE + "). You may want to "
                + "increase flume.event.max.size.bytes in your flume-site.xml file");
        // this string construction took ~5% of exec time!
        // , "byte length is " + s.length + " which is not < " + MAX_BODY_SIZE);
        Preconditions.checkNotNull(pri, "Failed when atttempting to "
                + "create event with null priority");
        this.body = s;
        this.timestamp = timestamp;
        this.pri = pri;
        this.nanos = nanoTime;
        this.host = host;
    }

    /**
     * Returns reference to mutable body of event. NOTE: the contents of the
     * returned byte array should not be modified.
     */
    public byte[] getBody() {
        return body;
    }

    public Priority getPriority() {
        return pri;
    }

    protected void setPriority(Priority p) {
        this.pri = p;
    }

    /**
     * Returns unix time stamp in millis
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set unix time stamp in millis
     */
    protected void setTimestamp(long stamp) {
        this.timestamp = stamp;
    }

    public String toString() {
        String mbody = StringEscapeUtils.escapeJava(new String(getBody()));
        StringBuilder attrs = new StringBuilder();
        SortedMap<String, byte[]> sorted = new TreeMap<String, byte[]>(this.fields);
        for (Entry<String, byte[]> e : sorted.entrySet()) {
            attrs.append("{ " + e.getKey() + " : ");

            String o = Attributes.toString(this, e.getKey());
            attrs.append(o + " } ");
        }

        return getHost() + " [" + getPriority().toString() + " "
                + new Date(getTimestamp()) + "] " + attrs.toString() + mbody;
    }

    @Override
    public long getNanos() {
        return nanos;
    }

    @Override
    public String getHost() {
        return host;
    }

    /**
     * This takes an event and a list of attribute names. It returns a new event
     * that has the same net.opentsdb.core event values but *only * the attributes specified by
     * the list.
     */
    public static Event select(Event e, String... attrs) {
        Event e2 = new EventImpl(e.getBody(), e.getTimestamp(), e.getPriority(),
                e.getNanos(), e.getHost());
        for (String a : attrs) {
            byte[] data = e.get(a);
            if (data == null) {
                continue;
            }
            e2.set(a, data);
        }
        return e2;
    }

    /**
     * This takes an event and a list of attribute names. It returns a new event
     * that has the same net.opentsdb.core event values and all of the attribute/values
     * *except* for those attributes specified by the list.
     */
    public static Event unselect(Event e, String... attrs) {
        Event e2 = new EventImpl(e.getBody(), e.getTimestamp(), e.getPriority(),
                e.getNanos(), e.getHost());
        List<String> as = Arrays.asList(attrs);
        for (Entry<String, byte[]> ent : e.getAttrs().entrySet()) {
            String a = ent.getKey();
            if (as.contains(a)) {
                continue; // don't add it if it is in the unselect list.
            }

            byte[] data = e.get(a);
            e2.set(a, data);
        }
        return e2;

    }
}
