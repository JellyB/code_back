package com.huatu.ztk;

import com.google.common.collect.Maps;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

/**
 * Created by shaojieyue
 * Created time 2016-08-04 16:52
 */
public class EtcdTest {
    private static final Logger logger = LoggerFactory.getLogger(EtcdTest.class);

    public static void main(String[] args) throws IOException, EtcdAuthenticationException, TimeoutException, EtcdException, InterruptedException {
        int aa = 11234;
        System.out.println(aa/10000);
    }
}
