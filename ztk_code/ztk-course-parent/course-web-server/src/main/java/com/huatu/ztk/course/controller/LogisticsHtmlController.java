package com.huatu.ztk.course.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.course.bean.ExpressResult;
import com.huatu.ztk.course.common.ExpressStatus;
import com.huatu.ztk.course.common.NetSchoolConfig;
import com.huatu.ztk.course.service.LogisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

/**
 * Created by shaojieyue
 * Created time 2016-12-05 20:57
 */

@Controller
@RequestMapping(value = "v1/logistics",produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
public class LogisticsHtmlController {
    private static final Logger logger = LoggerFactory.getLogger(LogisticsHtmlController.class);

    @Autowired
    private LogisticsService logisticsService;


    /**
     * 物流详情
     * @param id
     * @param company
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{id}",produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
    public Object detail(@PathVariable String id, @RequestParam String company, Model model) throws Exception{

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("com", company);
        parameterMap.put("num", id);
        ExpressResult result = logisticsService.getDetail(parameterMap);

        if (result.getCode() == NetSchoolConfig.SUCCESS_CODE) {
            //运单号
            model.addAttribute("num", id);
            //运单状态描述
            int statusCode = result.getData().getStatus();
            model.addAttribute("statusDescription", ExpressStatus.getByCode(statusCode).getDes());
            model.addAttribute("result", result);
            model.addAttribute("noMsgFlag", false);
        } else {
            model.addAttribute("noMsgFlag", true);
        }

        return "logistics/logistics_detail";
    }
}
