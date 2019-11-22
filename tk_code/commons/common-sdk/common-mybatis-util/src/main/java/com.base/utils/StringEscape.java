package com.base.utils;

/**
 * Created by junli on 2017/9/20.
 */
public class StringEscape {

    /**
     * 字符串是否需要转义
     *
     * @return
     */
    private static boolean isEscapeNeededForString(String str, int len) {

        boolean needsHexEscape = false;

        for (int i = 0; i < len; ++i) {
            char c = str.charAt(i);

            switch (c) {
                case 0: /* Must be escaped for 'mysql' */

                    needsHexEscape = true;
                    break;

                case '\n': /* Must be escaped for logs */
                    needsHexEscape = true;

                    break;

                case '\r':
                    needsHexEscape = true;
                    break;

                case '\\':
                    needsHexEscape = true;

                    break;

                case '\'':
                    needsHexEscape = true;

                    break;

                case '"': /* Better safe than sorry */
                    needsHexEscape = true;

                    break;

                case '\032': /* This gives problems on Win32 */
                    needsHexEscape = true;
                    break;
            }

            if (needsHexEscape) {
                break; // no need to scan more
            }
        }
        return needsHexEscape;
    }

    /**
     * 转义字符串
     *
     * @param escapeStr
     * @return 转义后的字符串
     */
    public static String escapeString(String escapeStr) {

        if (escapeStr.matches("\'(.+)\'")) {
            escapeStr = escapeStr.substring(1, escapeStr.length() - 1);
        }

        String parameterAsString = escapeStr;
        int stringLength = escapeStr.length();
        if (isEscapeNeededForString(escapeStr, stringLength)) {

            StringBuilder buf = new StringBuilder((int) (escapeStr.length() * 1.1));

            for (int i = 0; i < stringLength; ++i) {
                char c = escapeStr.charAt(i);

                switch (c) {
                    case 0: /* Must be escaped for 'mysql' */
                        buf.append('\\');
                        buf.append('0');

                        break;

                    case '\n': /* Must be escaped for logs */
                        buf.append('\\');
                        buf.append('n');

                        break;

                    case '\r':
                        buf.append('\\');
                        buf.append('r');

                        break;

                    case '\\':
                        buf.append('\\');
                        buf.append('\\');

                        break;

                    case '\'':
                        buf.append('\\');
                        buf.append('\'');

                        break;

                    case '"': /* Better safe than sorry */
                        buf.append('\\');
                        buf.append('"');

                        break;

                    case '\032': /* This gives problems on Win32 */
                        buf.append('\\');
                        buf.append('Z');

                        break;

                    default:
                        buf.append(c);
                }
            }

            parameterAsString = buf.toString();
        }
        return "" + parameterAsString + "";
    }
}
