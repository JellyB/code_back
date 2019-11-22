package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.knowledge.BaseTest;
import com.yxy.ssdb.client.SsdbConnection;
import com.yxy.ssdb.client.exception.CommunicationException;
import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by shaojieyue
 * Created time 2016-07-30 20:42
 */
public class SSdbTest  {
    private static final Logger logger = LoggerFactory.getLogger(SSdbTest.class);

    public static void main(String[] args) {
        zsetTest();
    }

    public static void zsetTest(){
        SsdbPooledConnectionFactory ssdbPooledConnectionFactory = new SsdbPooledConnectionFactory("192.168.100.111",8991,2,20);
        try {
            final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();
            final Map<String, String> hgetall = connection.hgetall("wrong_count_12252065_1");
            System.out.println(hgetall);
        }catch (Exception e){
            logger.error("ex",e);
        }finally {
        }

    }
}
