package com.huatu.ztk.pc.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.spring.annotation.RequestToken;
import com.huatu.ztk.pc.bean.Share;
import com.huatu.ztk.pc.common.EssaySingleQuestion;
import com.huatu.ztk.pc.service.ShareServerV4;
import com.huatu.ztk.pc.util.WxChatShareUtil;
import com.huatu.ztk.user.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * @author jbzm
 * @date 2018下午5:15
 **/

@Controller
@RequestMapping(value = "/v4/share")
public class ShareControllerV4 {
    private static final Logger logger = LoggerFactory.getLogger(ShareControllerV4.class);


    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private ShareServerV4 shareServiceV4;

    /**
     * 模考大赛分享
     *
     * @param id
     * @param type
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "match", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object matchPractice(@RequestParam long id, @RequestParam int type, @RequestToken String token,
                                @RequestHeader(defaultValue = "6.0") String cv,
                                @RequestHeader(defaultValue = "-1") int terminal) throws BizException {
        userSessionService.assertSession(token);
        int catgory = userSessionService.getCatgory(token);

        return shareServiceV4.sharePracticeWithEssayV4(id, type, token, catgory, cv, terminal);
    }

    /**
     * 查看模考大赛分享
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "match/{id}", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public Object queryMatchPractice(@PathVariable String id, Model model) throws BizException {
        Map data = shareServiceV4.findSharePracticeModelWithEssay(id);
        //分享未找到
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        if ("mark".equals(data.get("matchMark"))) {
            model.addAllAttributes(data);
            return "share_v4/mock_exam_two";
        } else if ("essay".equals(data.get("matchMark"))) {
            data.put("essay", EssaySingleQuestion.essayQuesrion);
            model.addAllAttributes(data);
            logger.info("queryMatchPractice：data={}", JsonUtil.toJson(data));
            return "share_v4/mock_exam_essay";
        } else if ("total".equals(data.get("matchMark"))) {
            data.put("essay", EssaySingleQuestion.essayQuesrion);
            model.addAllAttributes(data);
            logger.info("queryMatchPractice：data={}", JsonUtil.toJson(data));
            return "share_v4/mock_exam";
        }
        return null;
    }

    /**
     * 创建分享练习
     *
     * @param practiceId
     * @param title
     * @param token
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/practice", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object sharePractice(@RequestParam long practiceId,
                                String title,
                                @RequestParam int type,
                                @RequestToken String token) throws BizException {
        userSessionService.assertSession(token);
        Share share = shareServiceV4.sharePractice(practiceId, token, title, type);
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
        Share share = shareServiceV4.findSharePractice(id);
        Map<String, Object> data = Maps.newHashMap();
        //分享未找到
        if (share == null) {
            return null;
        }
        data.put("reportInfo", share.getReportInfo());
        WxChatShareUtil.assertWeiXinInfo(data, share);
        if (share == null) {//分享未找到
            return ResponseEntity.notFound().build();
        }
        model.addAllAttributes(data);
        switch (share.getType()) {
            //查看真题演练,精准估分,专项模考
            case (1):
                return "share_v4/accurate_estimate";
            //查看错题重练,每日特训,智能刷题,专项练习
            case (2):
                return "share_v4/special_practice";
            default:
                return null;
        }
    }


    /**
     * 分享课程|音频
     *
     * @param token
     * @param courseId 课程id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "course", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object shareCourse(@RequestToken String token,
                              @RequestParam long courseId,
                              @RequestParam(defaultValue = "-1") long shareSyllabusId,
                              @RequestParam(defaultValue = "-1") long courseWareId,
                              @RequestParam(defaultValue = "1") int type,
                              @RequestHeader int terminal,
                              @RequestHeader String cv) throws BizException {
        userSessionService.assertSession(token);
        Long uid = userSessionService.getUid(token);
        Share share = shareServiceV4.shareCourseOrVideo(courseId, uid, shareSyllabusId, courseWareId, type, terminal, cv);
        return share;
    }
    
    /**
     * 小程序点击分享报告
     * @param token
     * @param practiceId
     * @param avatar
     * @param nick
     * @param terminal
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "estimateForWechat", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object estimateForWechat(@RequestToken String token,@RequestParam String practiceId,
                              @RequestParam String avatar,@RequestParam String nick) throws BizException {
        userSessionService.assertSession(token);
        Long uid = userSessionService.getUid(token);
        Share share = shareServiceV4.estimateForWechat(uid,practiceId,avatar,nick);
        return share;
    }
    
    /**
     * 小程序查看分享报告
     * @param token
     * @param id 分享id
     * @param avatar
     * @param nick
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "estimateForWechat", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public Object getEstimateForWechat(@RequestParam String id, @RequestHeader int terminal) throws BizException {
        return shareServiceV4.getEstimateForWechat(id,terminal);
    }
    
}
