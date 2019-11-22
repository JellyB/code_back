package com.huatu.one.biz.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * PHP接口返回课表
 *
 * @author geek-s
 * @date 2019-08-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PHPScheduleResponse {

    /**
     * 课程ID
     */
    private String classId;

    /**
     * 参加码
     */
    private String joinCode;

    /**
     * 老师
     */
    private String joinName;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date beginTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 直播标题
     */
    private String title;

    /**
     * 课程标题
     */
    private String classTitle;
}
