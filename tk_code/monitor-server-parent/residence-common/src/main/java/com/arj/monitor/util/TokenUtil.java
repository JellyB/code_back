package com.arj.monitor.util;


import java.util.UUID;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-04 下午10:05
 **/
public class TokenUtil {

    public TokenUtil() {
    }

    /**
     * uuid token
     * @return
     */
    public static String generateToken() {
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        return token;
    }
}
