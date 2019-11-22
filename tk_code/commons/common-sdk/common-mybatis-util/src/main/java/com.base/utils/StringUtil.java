package com.base.utils;

/**
 * 字符串工具类
 * Created by junli on 2017/8/31.
 */
public class StringUtil {

    /**
     * 空字符
     */
    public static final String EMPTY = "";

    /**
     * 下划线字符
     */
    public static final char UNDERLINE = '_';

    /**
     * 判断字符串是否 为空
     *
     * @param charSequence 需要判断字符串
     * @return 判断结果
     */
    public static boolean isEmpty(final CharSequence charSequence) {
        int strLen;
        if (charSequence == null || (strLen = charSequence.length()) == 0) {
            return true;
        }
        for (int index = 0; index < strLen; index++) {
            if (!Character.isWhitespace(charSequence.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否 不为空
     *
     * @param charSequence 需要判断的字符串
     * @return 判断结果
     */
    public static boolean isNotEmpty(final CharSequence charSequence) {
        return !isEmpty(charSequence);
    }

    /**
     * 判断对象是否 为空
     *
     * @param object 需要判断的对象
     * @return 判断结果
     */
    public static boolean checkObjectNotNull(Object object) {
        if (object instanceof Character) {
            return isEmpty((CharSequence) object);
        }
        return object != null;
    }

    /**
     * 判断对象是否 不为空
     *
     * @param object 需要判断的对象
     * @return 判断结果
     */
    public static boolean checkObjectNull(Object object) {
        return !checkObjectNotNull(object);
    }

    /**
     * 驼峰转下划线格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String camelToUnderline(String param) {
        if (isEmpty(param)) {
            return EMPTY;
        }
        int len = param.length();
        StringBuilder stringBuilder = new StringBuilder(len);
        for (int index = 0; index < len; index++) {
            char c = param.charAt(index);
            if (Character.isUpperCase(c) && index > 0) {
                stringBuilder.append(UNDERLINE);
            }
            stringBuilder.append(Character.toLowerCase(c));
        }
        return stringBuilder.toString();
    }

    /**
     * 下划线转驼峰格式
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String underlineToCamel(String param) {
        if (isEmpty(param)) {
            return EMPTY;
        }
        int len = param.length();
        StringBuilder stringBuilder = new StringBuilder(len);
        for (int index = 0; index < len; index++) {
            char c = param.charAt(index);
            if (c == UNDERLINE) {
                if (++index < len) {
                    stringBuilder.append(Character.toUpperCase(param.charAt(index)));
                }
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 判断是否包含大写字母
     *
     * @param param
     * @return
     */
    public static boolean containsUpperCase(String param) {
        for (int index = 0; index < param.length(); index++) {
            char c = param.charAt(index);
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为全大写
     *
     * @param param 需要判断的字符
     * @return 判断结果
     */
    public static boolean isCapitalMode(String param) {
        return isNotEmpty(param) && param.matches("^[0-9A-Z/_]+$");
    }

    /**
     * 判断首字母是否为大写
     *
     * @param param 需要判断的字符
     * @return 判断的结果
     */
    public static boolean isCapitalFirst(String param) {
        if (isEmpty(param)) {
            return false;
        }
        String firstChar = param.substring(0, 1);
        return firstChar.matches("[A-Z]");
    }

    /**
     * 首字母转换成小写
     *
     * @param param 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String firstToLowerCase(String param) {
        if (isEmpty(param)) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder(param.length());
        sb.append(param.substring(0, 1).toLowerCase());
        sb.append(param.substring(1));
        return sb.toString();
    }

    public static boolean ArraysContains(String[] array, String param) {
        if (checkObjectNotNull(array) || isEmpty(param)) {
            return false;
        }
        for (int index = 0, len = array.length; index < len; index++) {
            if (array[index].equals(param)){
                return true;
            }
        }
        return false;
    }
}
