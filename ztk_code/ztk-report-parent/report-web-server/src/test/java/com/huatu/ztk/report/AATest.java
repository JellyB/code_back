package com.huatu.ztk.report;

import com.huatu.ztk.commons.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-07-30 13:04
 */
public class AATest {
    private static final Logger logger = LoggerFactory.getLogger(AATest.class);

    public static void main(String[] args) {
        String ss ="{\"area\":1045,\"uid\":11873933,\"times\":0,\"wsum\":7,\"subject\":1,\"rsum\":12}";
        final Map map = JsonUtil.toMap(ss,String.class,Long.class);
        System.out.println(map);
    }
}
