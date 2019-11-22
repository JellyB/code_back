package com.huatu.bigdataanalyzeserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.huatu.bigdataanalyzecommon.bean.CourseWareDTO;
import com.huatu.bigdataanalyzecommon.bean.TopicRecordEntity;
import com.huatu.bigdataanalyzeserver.service.DataStatisticsSystemService;
import com.huatu.bigdataanalyzeserver.util.RabbitMQUtils;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.*;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@RestController
//@RequestMapping(value = "/v1/topoc/record")
//@RestController(value = "")
public class DataStatisticsSystemController {

    @Autowired
    private DataStatisticsSystemService dataService;



    @GetMapping("/v1/topoc/record/function/analysis")
    public Object userFunAnalysis(@RequestParam(value = "user_id", required = false) Long user_id
            , @RequestParam(value = "fun_id", required = false) String fun_id) {
        System.out.println(user_id + "" + fun_id);
        return 1;
    }

    @PostMapping(value = "/v1/topoc/record/coureWare/correctAnalyze",consumes = "application/json")
    public Object userCorrectAnalyeze(@RequestParam(value = "userId", required = false) Long userId,
                                      @RequestBody List<CourseWareDTO> list,
                                      HttpServletRequest request) throws Exception {

        return dataService.queryAccuracy(userId, list);
    }


}
