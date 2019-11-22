package com.huatu.tiku.course.web.controller.v4;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.netschool.api.v4.CourseSettingServiceV4;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hanchao
 * @date 2018/1/26 15:59
 */
@RestController
@RequestMapping("/")
@ApiVersion("v4")
public class SettingsControllerV4 {
    @Autowired
    private CourseSettingServiceV4 courseSettingServiceV4;

    /**
     * 获取直播查询配置
     * @param userSession
     * @return
     */
    @GetMapping("/live/query/_settings")
    public Object getLiveSettings(@Token UserSession userSession) {
        return ResponseUtil.build(courseSettingServiceV4.getLiveSettings(userSession.getUname()));
    }

    /**
     * 保存直播配置
     * @param userSession
     * @param categories
     * @return
     */
    @PostMapping("/live/query/_settings")
    public Object setLiveSettings(@Token UserSession userSession,
                                  String categories){
        return ResponseUtil.build(courseSettingServiceV4.setLiveSettings(userSession.getUname(),categories));
    }

    /**
     * 获取直播查询配置
     * @param userSession
     * @return
     */
    @GetMapping("/recording/query/_settings")
    public Object getRecordingSettings(@Token UserSession userSession) {
        return ResponseUtil.build(courseSettingServiceV4.getRecordingSettings(userSession.getUname()));
    }

    /**
     * 保存直播配置
     * @param userSession
     * @param categories
     * @return
     */
    @PostMapping("/recording/query/_settings")
    public Object setRecordingSettings(@Token UserSession userSession,
                                  String categories){
        return ResponseUtil.build(courseSettingServiceV4.setRecordingSettings(userSession.getUname(),categories));
    }



}
