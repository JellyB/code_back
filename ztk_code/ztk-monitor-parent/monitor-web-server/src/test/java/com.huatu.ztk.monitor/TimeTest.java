package com.huatu.ztk.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by shaojieyue
 * Created time 2017-02-07 10:52
 */
public class TimeTest {
    private static final Logger logger = LoggerFactory.getLogger(TimeTest.class);

    public static void main(String[] args) {
        final long l = TimeUnit.MINUTES.toMillis(15);
        System.out.println(l);
    }
}
