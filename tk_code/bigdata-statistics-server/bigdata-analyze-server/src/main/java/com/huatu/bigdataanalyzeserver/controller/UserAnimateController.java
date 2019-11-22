package com.huatu.bigdataanalyzeserver.controller;

import com.huatu.bigdataanalyzecommon.bean.AnimateUser;
import com.huatu.bigdataanalyzecommon.bean.Aus;

import com.jcraft.jsch.*;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1/user/animate")
public class UserAnimateController {

    @Autowired
    private Producer<String, String> producer;

    @Autowired
    private ChannelExec openChannel;
    @Autowired
    private Session session;

    @Autowired
    private Jedis jedis;


}