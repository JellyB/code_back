package com.huatu.tiku.schedule.biz.vo;

import com.huatu.tiku.schedule.biz.enums.TeacherType;
import lombok.Data;

/**待沟通vo
 * @author wangjian
 **/
@Data
public class TaskLiveDGTVo  extends TaskLiveVo{

    /**
     * 角色
     */
    private TeacherType role;

    private Long liveTeacherId;
    /**
     * 教师名
     */
    private String teacherName;
}
