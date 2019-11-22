package com.huatu.ztk.utils.encode;

/**
 * @author hanchao
 * @date 2017年4月12日 下午1:28:40
 */
public class Test {
    private static final long MAX_NUM = 1099511627775L;
    public static void main(String[] args) {
        System.out.println(Radis32Encode.numToStr(0));
        System.out.println(Radis32Encode.strToInt("7777"));
    }
    
}
