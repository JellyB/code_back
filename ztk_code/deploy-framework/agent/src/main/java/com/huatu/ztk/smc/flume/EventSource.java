package com.huatu.ztk.smc.flume;

import java.io.IOException;

/**
 * This provides a synchronous interface to getting events. An state/event-based
 * architecture will likely be more efficient than a thread based one because
 * there are many sources of information and only one output.
 */
public interface EventSource {

    /**
     * This is a blocking call that gets the next message from the source.
     *
     * @return event or null if source is done/empty
     * @throws IOException
     */
    Event next() throws IOException, InterruptedException;

    public void open() throws IOException, InterruptedException;

    public void close() throws IOException, InterruptedException;


    public static class Base implements EventSource {
        /**
         * type attribute is common to all sinks
         */
        protected static final String R_TYPE = "type";
        /**
         * byte count attribute is common to all sinks
         */
        protected static final String R_NUM_BYTES = "number of bytes";
        /**
         * event count attribute is common to all sinks
         */
        protected static final String R_NUM_EVENTS = "number of events";

        /**
         * total number of events appended to this sink
         */
        private long numEvents = 0;
        /**
         * total number bytes appended to this sink
         */
        private long numBytes = 0;

        public void close() throws IOException, InterruptedException {
        }

        public Event next() throws IOException, InterruptedException {
            return null;
        }

        /**
         * This method should be called from sources which wish to track event
         * statistics.
         */
        synchronized protected void updateEventProcessingStats(Event e) {
            if (e == null)
                return;
            numBytes += e.getBody().length;
            numEvents++;
        }

        public void open() throws IOException, InterruptedException {
        }

        public String getName() {
            return this.getClass().getSimpleName();
        }


    }

}
