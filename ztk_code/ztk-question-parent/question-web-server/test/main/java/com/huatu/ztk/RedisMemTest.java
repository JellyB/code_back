package com.huatu.ztk;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.AreaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by shaojieyue
 * Created time 2016-07-13 16:14
 */
public class RedisMemTest {
    private static final Logger logger = LoggerFactory.getLogger(RedisMemTest.class);

    public static void main(String[] args) {
        final ArrayList<Integer> integers = Lists.newArrayList(1045, 1, 1168, 2945, 1963, 2502, 2106, -9, 41, 1532, 1709, 1826, 586, 823, 1263, 2600, 356, 3046, 1374, 225, 802, 1988, 2299, 21, 3125, 943, 2257, 2827);
        for (Integer integer : integers) {
            final String s = AreaConstants.getArea(integer).getName();
            System.out.println(s);
        }
    }
}
