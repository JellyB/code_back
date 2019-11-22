package com.huatu.common.utils.lang;

/**
 * @author hanchao
 * @date 2017/10/18 18:03
 */
public class StrUtil {
    public static String concat(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            if (string != null) {
                stringBuilder.append(string);
            }
        }
        return stringBuilder.toString();
    }
}
