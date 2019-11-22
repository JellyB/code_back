package com.huatu.ztk.paper.util;

/**
 * Created by huangqingpeng on 2019/3/20.
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


    public static void main(String[] args) {
        System.out.println(compare("7.1.8", "7.1.11") < 0);
        System.out.println(compare("7.1", "7.1.11") < 0);
        System.out.println(compare("7.1.8", "7.1.140") < 0);
    }
}
