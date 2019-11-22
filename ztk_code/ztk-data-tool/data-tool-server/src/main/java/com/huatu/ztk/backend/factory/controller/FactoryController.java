package com.huatu.ztk.backend.factory.controller;

import com.huatu.ztk.backend.factory.service.FactoryService;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by huangqp on 2018\7\5 0005.
 */
@RestController
@RequestMapping("test")
public class FactoryController {
    @Autowired
    FactoryService factoryService;
    @RequestMapping(value = "/{id}" ,method = RequestMethod.GET)
    public Object findType(@PathVariable int id){
        factoryService.insertFactory(id);
        return SuccessMessage.create("test");
    }
}

