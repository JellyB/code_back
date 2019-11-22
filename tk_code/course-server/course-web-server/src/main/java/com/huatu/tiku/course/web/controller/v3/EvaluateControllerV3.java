package com.huatu.tiku.course.web.controller.v3;

import com.huatu.common.spring.event.EventPublisher;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v3.EvaluateServiceV3;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEvent;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/20 9:28
 */
@RequestMapping("/v3/evaluates")
@RestController
public class EvaluateControllerV3 {
    @Autowired
    private EvaluateServiceV3 evaluateServiceV3;
    @Autowired
    private EventPublisher eventPublisher;

    /**
     * 获取某个课程的评价列表
     * @param classid
     * @param lessionid
     * @param page
     * @return
     */
    @GetMapping
    public Object findEvaluateList(@RequestParam("classid")int classid,
                                   @RequestParam("lessionid")int lessionid,
                                   @RequestParam("page")int page,
                                   @Token UserSession userSession) {
        return ResponseUtil.build(evaluateServiceV3.findEvaluateList(classid,lessionid,page,userSession.getUname()));
    }

    /**
     * 获取自己对某个课程的评价
     * @param classid
     * @param lessionid
     * @param userSession
     * @return
     */
    @GetMapping("/self")
    public Object getEvaluate(@RequestParam("classid")int classid,
                                   @RequestParam("lessionid")int lessionid,
                                   @Token UserSession userSession) {
        Map<String,Object> params = HashMapBuilder.<String,Object>newBuilder()
                .put("classid",classid)
                .put("lessionid",lessionid)
                .put("username",userSession.getUname())
                .build();
        return ResponseUtil.build(evaluateServiceV3.getEvaluete(params));
    }

    /**
     * 提交评价
     * @param classid
     * @param courseRemark
     * @param coursescore
     * @param lessionid
     * @param userSession
     * @return
     */
    @PostMapping
    public Object saveEvaluate(@RequestParam("classid")int classid,
                               @RequestParam("courseRemark")String courseRemark,
                               @RequestParam("coursescore")int coursescore,
                               @RequestParam("lessionid")int lessionid,
                               @RequestParam int type,
                               @Token UserSession userSession) {
        Map<String,Object> params = HashMapBuilder.<String,Object>newBuilder()
                .put("classid",classid)
                .put("lessionid",lessionid)
                .put("username",userSession.getUname())
                .put("courseRemark",courseRemark)
                .put("coursescore",coursescore)
                .build();
        NetSchoolResponse response = evaluateServiceV3.saveEvaluate(params);

        if(ResponseUtil.isSuccess(response)){
            eventPublisher.publishEvent(RewardActionEvent.class,
                    this,
                    (event) -> event.setAction(type == 1 ? RewardAction.ActionType.EVALUATE_AFTER: RewardAction.ActionType.EVALUATE)
                                .setUid(userSession.getId())
                                .setUname(userSession.getUname())
                    );
        }

        return ResponseUtil.build(response);
    }
}
