package com.huatu.bigdataanalyzeserver.controller;

import com.huatu.bigdataanalyzecommon.bean.PlayRecordDtoWrapper;
import com.huatu.bigdataanalyzeserver.service.DataCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class DataCollectionController {


    @Autowired
    private DataCollectionService dataCollectionService;


    @PostMapping("")
    public boolean miniVideo(@RequestBody PlayRecordDtoWrapper pr) {


        return dataCollectionService.sendObject2Kafka(pr);
    }

}
