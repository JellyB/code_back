package com.huatu.ztk.paper.controller;

import com.huatu.tiku.entity.activity.Estimate;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.service.PaperAnswerCardUtilComponent;
import com.huatu.ztk.user.service.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/9
 * @描述
 */
@RestController
@RequestMapping("estimate/paper/bag")
public class EstimatePaperGiftBagController {


    @Autowired
    private PaperAnswerCardUtilComponent paperAnswerCardUtilComponent;

    @Autowired
    private UserSessionService userSessionService;

    @RequestMapping(value = "/{paperId}", method = RequestMethod.GET)
    public HashMap getEstimatePaperInfo(@PathVariable("paperId") Long paperId,
                                        @RequestHeader String token) throws BizException {

        HashMap map = new HashMap();
        userSessionService.assertSession(token);
        Estimate estimateGiftInfo = paperAnswerCardUtilComponent.getEstimateGiftInfoHash(paperId.intValue());
        if (null != estimateGiftInfo) {
            //二维码
            map.put("qrCodeImageUrl", estimateGiftInfo.getQrCodeImageUrl());
            map.put("notGetBagUrl", estimateGiftInfo.getNotGetBagUrl());
            map.put("hasGetBagUrl", estimateGiftInfo.getHasGetBagUrl());
        }
        return map;
    }
}