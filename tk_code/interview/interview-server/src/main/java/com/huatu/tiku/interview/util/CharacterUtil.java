package com.huatu.tiku.interview.util;

/**
 * Created by x6 on 2018/3/9.
 */
public class CharacterUtil {


    /**
     * 过滤掉表情符号
     *
     * @param content
     * @return
     */
    public static String removeFourChar(String content) {
        byte[] conbyte = content.getBytes();
        for (int i = 0; i < conbyte.length; i++) {
            if ((conbyte[i] & 0xF8) == 0xF0) {
                for (int j = 0; j < 4; j++) {
                    conbyte[i + j] = 0x30;
                }
                i += 3;
            }
        }
        content = new String(conbyte);
        return content.replaceAll("0000", "");
    }
}
