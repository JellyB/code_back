package com.huatu.hadoop.service;

import com.alibaba.fastjson.JSONObject;
import com.huatu.hadoop.bean.PlayRecordDtoWrapper;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DataCollectionService {

    /**
     * kafka -> flume
     */
    @Autowired
    private Producer<String, String> kfkaSend;

    public boolean sendObject2Kafka(PlayRecordDtoWrapper pr) {

        boolean flag = true;
        try {


            String[] data = pr.toString().split("\r\n");

            List<Map<String,Object>> cache = new ArrayList<>();

            if (data != null && data.length >0){
                for (int i = 0; i < data.length; i++) {

                    String[] fields = data[i].split(",");
                    Map<String,Object> parseData2Map = new HashMap<>();
                    parseData2Map.put("videoId",fields[0]);
                    parseData2Map.put("yunVideoId",fields[1]);
                    parseData2Map.put("recordTimestamp",fields[2]);
                    parseData2Map.put("terminal",fields[3]);
                    parseData2Map.put("cv",fields[4]);
                    parseData2Map.put("pm",fields[5]);
                    parseData2Map.put("userId",fields[6]);

                    cache.add(parseData2Map);
                    log.info("kafka had send ztkAnswerCardCTO : {}  ", parseData2Map);

                    kfkaSend.send(new KeyedMessage("minivideo", JSONObject.toJSONString(parseData2Map)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }

        return flag;
    }
}
