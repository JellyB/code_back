package com.huatu.ztk.smc.flume;

/**
 * Some array helper methods not found in apache commons or guava.
 */
public final class ArrayUtils {
    private ArrayUtils() {
    }

    /**
     * Convert all arguments into strings by calling toString. Nulls are converted
     * to "".
     *
     * @param args
     * @return
     */
    public static String[] toStrings(Object... args) {
        String[] sargs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                sargs[i] = "";
            } else {
                sargs[i] = args[i].toString();
            }
        }
        return sargs;
    }

}
