package com.huatu.ztk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Created by shaojieyue
 * Created time 2016-06-03 16:48
 */
public class MobileTest {
    private static final Logger logger = LoggerFactory.getLogger(MobileTest.class);

    public static void main(String[] args) {
        String REGEX_MOBILE = "(^(13\\d|14[57]|15[^4,\\D]|17[13678]|18\\d)\\d{8}|170[^346,\\D]\\d{7})$";
        String mobile = "17717670214";
        final boolean matches = Pattern.matches(REGEX_MOBILE, mobile);
        System.out.println(matches);
    }
}
