package com.huatu.ztk.backend.question.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.question.service.DereplicateService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.ErrorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangqp on 2017\11\22 0022.
 */
@RestController
@RequestMapping(value = "/dereplicate", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DereplicateController {
    private final static Logger logger = LoggerFactory.getLogger(DereplicateController.class);
    @Autowired
    private DereplicateService dereplicateService;

    @RequestMapping(value = "excute", method = RequestMethod.GET)
    public Object pointAdd(@RequestParam String str) {
        logger.info("point add json={}", JsonUtil.toJson(str));
        String[] idStrs = str.split(",");
        Integer[] ids = new Integer[idStrs.length];
        for(int i=0;i<idStrs.length;i++){
            ids[i] = Integer.parseInt(idStrs[i]);
        }
        ErrorResult result =ErrorResult.create(1000000,"成功");
        try{
            int l = dereplicateService.dereplicate(ids);
            Map map = Maps.newHashMap();
            map.put("id",l);
            result.setData(map);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;

    }

}
