package com.huatu.tiku.match.util;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/30
 * @描述
 */
public class VersionUtil {


    public static int compare(String version1, String version2) {
        try {
            int[] canonicalVersion1 = getCanonicalVersion(version1);
            int[] canonicalVersion2 = getCanonicalVersion(version2);
            return compareArray(canonicalVersion1, canonicalVersion2);
        } catch (Exception e) {
            return version1.compareTo(version2);
        }
    }

    private static int compareArray(int[] canonicalVersion1, int[] canonicalVersion2) {
        int minLength = Math.min(canonicalVersion1.length, canonicalVersion2.length);
        for (int i = 0; i < minLength; i++) {
            if (canonicalVersion1[i] < canonicalVersion2[i]) {
                return -1;
            }else if(canonicalVersion1[i] > canonicalVersion2[i]){
                return 1;
            }
        }
        return canonicalVersion1.length-canonicalVersion2.length;
    }

    private static int[] getCanonicalVersion(String version) {

        String[] split = version.split("\\.");

        int[] result = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = Integer.parseInt(split[i]);
        }
        return result;
    }

}
