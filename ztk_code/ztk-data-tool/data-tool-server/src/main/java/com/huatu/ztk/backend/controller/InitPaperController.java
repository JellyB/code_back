package com.huatu.ztk.backend.controller;

import com.google.common.base.Strings;
import com.huatu.ztk.backend.util.RestTemplateUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\16 0016.
 */
@RestController
@RequestMapping(value = "v1/paper",produces = MediaType.APPLICATION_JSON_UTF8_VALUE )
public class InitPaperController {
    @RequestMapping(value = "sync", method = RequestMethod.GET)
    public Object syncPaper(@RequestParam String pids,@RequestParam(defaultValue = "1") int type){
        if(Strings.isNullOrEmpty(pids)){
            return null;
        }
        String[] ids = pids.split(",");
        List<Integer> paperIds = Arrays.asList(ids).stream().map(Integer::new).collect(Collectors.toList());
        if(type==1){
            RestTemplateUtil.import2Mongo(paperIds);
        }else{
            RestTemplateUtil.importTest2Mongo(paperIds);
        }
        return null;
    }
}
