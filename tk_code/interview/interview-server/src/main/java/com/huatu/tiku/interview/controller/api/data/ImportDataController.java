package com.huatu.tiku.interview.controller.api.data;

import com.huatu.tiku.interview.entity.template.TemplateMsgResult;
import com.huatu.tiku.interview.service.ImportDataService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zhaoxi
 * @Description: 导出学员每日练习之后的测评报告
 * @date 2018/7/25下午7:48
 */
@RestController
@RequestMapping("/api/data")
@Slf4j
public class ImportDataController {
    @Autowired
    private ImportDataService importDataService;


    /**
     * 推送用户学习报告
     */
    @LogPrint
    @GetMapping(value="{type}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ModelAndView importData(@PathVariable  Long type){
         return importDataService. importData(type);
    }

}
