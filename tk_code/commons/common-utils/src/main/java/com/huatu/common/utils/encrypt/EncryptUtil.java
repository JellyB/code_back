package com.huatu.common.utils.encrypt;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author hanchao
 * @date 2017/4/9 22:33
 */
public class EncryptUtil {
    public static String sha1(String str){
        return DigestUtils.sha1Hex(str);
    }

    public static String sha1(String str,Object salt){
        return sha1(mergeSalt(str,salt,false));
    }

    public static String md5(String str){
        return DigestUtils.md5Hex(str);
    }

    public static String md5(String str,Object salt){
        return md5(mergeSalt(str,salt,false));
    }

    /**
     * copy from spring security
     * @param str
     * @param salt
     * @param strict
     * @return
     */
    public static String mergeSalt(String str, Object salt, boolean strict) {
        if (str == null) {
            str = "";
        }

        if (strict && (salt != null)) {
            if ((salt.toString().lastIndexOf("{") != -1)
                    || (salt.toString().lastIndexOf("}") != -1)) {
                throw new IllegalArgumentException("Cannot use { or } in salt.toString()");
            }
        }

        if ((salt == null) || "".equals(salt)) {
            return str;
        }
        else {
            return str + "{" + salt.toString() + "}";
        }
    }
}
