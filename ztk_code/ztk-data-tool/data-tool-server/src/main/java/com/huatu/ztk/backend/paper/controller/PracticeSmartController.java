package com.huatu.ztk.backend.paper.controller;

import com.huatu.ztk.backend.paper.bean.SmartPaperBean;
import com.huatu.ztk.backend.paper.service.PracticeSmartService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/practice/")
public class PracticeSmartController {
    private Logger logger = LoggerFactory.getLogger(PracticeSmartController.class);


    @Autowired
    private PracticeSmartService practiceSmartService;

    @RequestMapping(value = "smart", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object smartPaper(@RequestBody SmartPaperBean smartPaperBean) throws BizException {
        logger.info("bean={}", JsonUtil.toJson(smartPaperBean));
        practiceSmartService.makeSmartPaper(smartPaperBean);
        return SuccessMessage.create("组卷成功!");
    }
}
