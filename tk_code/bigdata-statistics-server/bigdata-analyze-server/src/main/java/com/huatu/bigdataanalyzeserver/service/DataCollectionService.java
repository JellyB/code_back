package com.huatu.bigdataanalyzeserver.service;

import com.huatu.bigdataanalyzecommon.bean.PlayRecordDtoWrapper;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataCollectionService {

    /**
     * kafka -> flume
     */
    @Autowired
    private Producer kfkaSend;

    public boolean sendObject2Kafka(PlayRecordDtoWrapper pr) {

        boolean flag = true;
        try {

            kfkaSend.send(new KeyedMessage("minivideo", pr.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }


        return flag;
    }
}
