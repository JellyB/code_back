package com.huatu.tiku.schedule.biz.vo;

import com.huatu.tiku.schedule.biz.vo.CourseLivePackage.LiveTeacherVo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**教师任务列表封装
 * @author wangjian
 **/
@Data
public class TaskLiveVo implements Serializable{
    private static final long serialVersionUID = -8966755238781574342L;

    private Long courseId;//课程名
    private String courseName;//课程名
    private Long liveId;//直播名
    private String liveName;//直播名
    private String timeRange;//时间范围
    private String confirmKey;//确认状态
    private String confirmStatus;//确认状态
}
