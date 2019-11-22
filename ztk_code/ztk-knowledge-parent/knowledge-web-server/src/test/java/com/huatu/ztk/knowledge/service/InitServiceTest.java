package com.huatu.ztk.knowledge.service;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.huatu.ztk.commons.spring.serializer.KryoRedisSerializer;
import com.huatu.ztk.knowledge.bean.QuestionGeneticBean;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-13  22:40 .
 */
public class InitServiceTest {
    @Test
    public void questionToRedis() throws Exception {
        KryoRedisSerializer kryoRedisSerializer = new KryoRedisSerializer();
        QuestionGeneticBean geneticBean = new QuestionGeneticBean();
        byte[] serialize = kryoRedisSerializer.serialize(geneticBean);;

        Object deserialize = kryoRedisSerializer.deserialize(serialize);
        System.out.print(deserialize);
    }

}