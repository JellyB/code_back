package com.huatu.ztk.smc.flume;


import java.util.Set;


/**
 * Simple interface for building EventSource's
 * <p/>
 * If build fails due to bad arguments, it should throw Illegal*Exceptions
 */
abstract public class SourceFactory {
    abstract public static class SourceBuilder {
        @Deprecated
        public EventSource build(String... argv) {
            return build(new Context(), argv);
        }

        @Deprecated
        public abstract EventSource build(Context ctx, String... argv);

        public EventSource create(Context ctx, Object... argv) {
            return build(ctx, ArrayUtils.toStrings(argv));
        }
    }

    ;

    public EventSource createSource(Context ctx, String name, Object... args)
            throws Exception {
        return getSource(ctx, name, ArrayUtils.toStrings(args));
    }

    @Deprecated
    abstract public EventSource getSource(Context ctx, String name,
                                          String... args) throws Exception;

    /**
     * Returns the list of sources that we can instantiate
     */
    abstract public Set<String> getSourceNames();
}
