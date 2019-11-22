package com.huatu.tiku.essay.web.controller.api.correct;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.correct.CorrectFeedBack;
import com.huatu.tiku.essay.service.v2.CorrectFeedBackService;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/9
 * @描述 学员批改意见反馈
 */
@RestController
@RequestMapping("api/v1/correctFeedBack")
public class CorrectFeedBackController {

    @Autowired
    CorrectFeedBackService correctFeedBackService;

    @PostMapping
    public Object save(@Token UserSession userSession,
                       @RequestBody CorrectFeedBack correctFeedBack) {
        return correctFeedBackService.save(correctFeedBack, userSession);
    }


}
