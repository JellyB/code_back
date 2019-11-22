package com.huatu.tiku.schedule.biz.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by duanxiangchao on 2018/5/10
 */
@Data
public class ScheduleTeacherVo implements Serializable {

    private static final long serialVersionUID = -8966755238781574342L;

    private Long teacherId;

    private String teacherName;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"teacherId\":")
                .append(teacherId);
        sb.append(",\"teacherName\":\"")
                .append(teacherName).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
