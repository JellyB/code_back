package com.huatu.ztk.user.controller.channel;

import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.commons.exception.SuccessResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2018/6/25.
 * 市场评价反馈controller
 */
@RestController
@RequestMapping(value = "/MarketAssessment")
public class MarketAssessmentController {

    //保存市场评价
    @Deprecated
    @RequestMapping(value = "/saveMarketAssess", method = RequestMethod.POST)
    public Object saveMarketAssess(@RequestParam String version, @RequestParam String token,
                                   int type) {
        //TODO保存方法
        return  SuccessMessage.create("success");
    }


}
