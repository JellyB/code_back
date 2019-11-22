package com.huatu.one.biz.controller.api.v1;

import com.huatu.one.biz.dto.FeedBackDto;
import com.huatu.one.biz.service.FeedBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 意见反馈
 */
@RestController
@RequestMapping("/v1/feedback")
public class FeedBackController {
    @Autowired
    private FeedBackService feedBackService;

    @PostMapping(value = "/add")
    public Object add(@RequestHeader String openid, @RequestBody FeedBackDto feedBackDto){
        return feedBackService.add(openid, feedBackDto);
    }
}
