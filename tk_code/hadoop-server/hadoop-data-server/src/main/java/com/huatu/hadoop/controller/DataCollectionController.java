package com.huatu.hadoop.controller;

import com.huatu.hadoop.bean.PlayRecordDtoWrapper;
import com.huatu.hadoop.service.DataCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController()
public class DataCollectionController {


    @Autowired
    private DataCollectionService dataCollectionService;


    @PostMapping("/v1/data/collection")
    public boolean miniVideo(@RequestBody PlayRecordDtoWrapper pr) {

//        try {
//
//            dataCollectionService.sendObject2Kafka(pr);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return true;
        return true;
    }

    @PostMapping("/v1/data/collection/recommend")
    public boolean recommend(@RequestBody PlayRecordDtoWrapper pr) {


        return true;
//        return dataCollectionService.sendObject2Kafka(pr);
    }


    public static void main(String[] args) {

        Date date = new Date(556698035);
        System.out.println(date);
    }

}
