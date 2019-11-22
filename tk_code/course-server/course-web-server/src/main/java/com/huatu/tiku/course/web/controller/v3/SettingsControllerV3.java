package com.huatu.tiku.course.web.controller.v3;

import com.huatu.common.SuccessResponse;
import com.huatu.common.spring.cache.Cached;
import com.huatu.tiku.course.netschool.api.v3.CourseSettingServiceV3;
import com.huatu.tiku.course.netschool.api.v3.UserLevelServiceV3;
import com.huatu.tiku.course.service.ConfigBizService;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.basic.reward.RewardActionService;
import com.huatu.tiku.springboot.basic.subject.SubjectEnum;
import com.huatu.tiku.springboot.basic.subject.SubjectService;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.huatu.tiku.course.util.CourseCacheKey.*;

/**
 * @author hanchao
 * @date 2017/9/13 21:19
 */
@RestController
@RequestMapping("/v3")
public class SettingsControllerV3 {
    @Resource(name = "redisTemplate")
    private ValueOperations valueOperations;
    @Autowired
    private CourseSettingServiceV3 courseSettingServiceV3;
    @Autowired
    private ConfigBizService configBizService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private RewardActionService rewardActionService;
    @Autowired
    private UserLevelServiceV3 userLevelServiceV3;


    @GetMapping("/address/_settings")
    public Object getAddressSettings(){
        Map config = configBizService.getConfig();
        //直接返回result,可以减少wrapper的拦截流程
        return new SuccessResponse(config);
    }

    @Cached(name = "等级特权说明",
            key = "T(com.huatu.tiku.course.util.CourseCacheKey).LEVEL_SETTINGS")
    @GetMapping("/level/_settings")
    public Object getLevelSettings(){
        String cacheKey = LEVEL_SETTINGS;
        Object result = valueOperations.get(cacheKey);
        if(result == null){
            result = ResponseUtil.build(userLevelServiceV3.getLevelSettings());
            valueOperations.set(cacheKey,result,1,TimeUnit.DAYS);
        }
        return result;
    }


    @GetMapping("/reward/_settings")
    public Object getRewardSettings(){
        return rewardActionService.all();
    }

    /**
     * 获取直播查询配置
     * @param userSession
     * @return
     */
    @Cached(name = "直播查询条件",
            key = "T(com.huatu.tiku.course.util.CourseCacheKey).LIVE_SETTINGS")
    @GetMapping("/live/query/_settings")
    public Object getLiveSettings(@Token UserSession userSession) {
        String cacheKey = LIVE_SETTINGS;
        Object result = valueOperations.get(cacheKey);
        if(result == null){
            result = ResponseUtil.build(courseSettingServiceV3.getLiveSettings());
            valueOperations.set(cacheKey,result,1, TimeUnit.DAYS);
        }
        return result;
    }

    /**
     * 获取录播查询配置
     * @param userSession
     * @return
     */
    @Cached(name = "录播查询条件",
            key = "T(com.huatu.tiku.course.util.CourseCacheKey).RECORDING_SETTINGS")
    @GetMapping("/recording/query/_settings")
    public Object getRecordingSettings(@Token UserSession userSession) {
        String cacheKey = RECORDING_SETTINGS;
        Map<String,Object> settings = (Map<String, Object>) valueOperations.get(cacheKey);
        if(settings == null){
            settings = (Map) ResponseUtil.build(courseSettingServiceV3.getRecordingSettings());
            settings.remove("province");//省份的由用户自己选择
            valueOperations.set(cacheKey,settings,1,TimeUnit.DAYS);
        }
        int subject = userSession.getSubject();
        int top = subjectService.top(subject);

        SubjectEnum[] enums = SubjectEnum.values();
        String selKey = "";
        for (SubjectEnum subjectEnum : enums) {
            if(subjectEnum.code() == top){
                selKey = subjectEnum.meaning();
                break;
            }
        }

        Map<String,Object> categories = (Map<String, Object>) settings.get("category");
        Map<String,Object> subjects = (Map<String, Object>) settings.get("subject");
        settings.put("category",categories.get(selKey));
        settings.put("subject",subjects.get(selKey));
        return settings;
    }

}
