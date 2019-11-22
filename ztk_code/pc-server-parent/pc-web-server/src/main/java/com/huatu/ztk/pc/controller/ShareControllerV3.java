package com.huatu.ztk.pc.controller;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.spring.annotation.RequestToken;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.service.HuatuShareService;
import com.huatu.ztk.pc.service.ShareRewardService;
import com.huatu.ztk.pc.service.ShareService;
import com.huatu.ztk.pc.util.TemplateHashModelUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionType;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;


@Controller
@RequestMapping(value = "/v3/share")
public class ShareControllerV3 {
    private static final Logger logger = LoggerFactory.getLogger(ShareControllerV3.class);
    @Autowired
    private HuatuShareService huatuShareService;


    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private ShareService shareService;

    /**
     * 分享练习
     * 只适用模考大赛的分享
     * @param id 行测试卷id 或者申论试卷id
     * @param type   试卷类型
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "practice", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object practice(@RequestParam long id,@RequestParam int type, @RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        final long uid = userSessionService.getUid(token);
        Share share = huatuShareService.sharePracticeWithEssay(id, uid, type,token,-1);
        return share;
    }

    /**
     * 查看分享练习
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "practice/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object querySharePractice(@PathVariable String id, Model model) throws BizException {
        Map data = shareService.findSharePracticeModelWithEssay(id);
        if (data == null) {//分享未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        if("mark".equals(data.get("matchMark"))){
            return "share_huatu/practice_match_line_test";
        }else if("essay".equals(data.get("matchMark"))){
            return "share_huatu/practice_match_essay";
        }else if("total".equals(data.get("matchMark"))){
            return "share_huatu/practice_match_total";
        }
        return null;
    }
}
