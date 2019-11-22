package com.huatu.tiku.schedule.biz.service.intelligence.dataevent;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by duanxiangchao on 2018/5/10
 */
@Data
public class TeacherDataEvent {

    private Date date;

    private Integer timeBegin;

    private Integer timeEnd;

    private List<TeacherData> teacherDates;

    @Data
    public static class TeacherData implements Serializable, Cloneable {
        private Long teacherId;

        private String teacherName;

        private Integer weight;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("\"teacherId\":")
                    .append(teacherId);
            sb.append(",\"teacherName\":\"")
                    .append(teacherName).append('\"');
            sb.append(",\"weight\":")
                    .append(weight);
            sb.append('}');
            return sb.toString();
        }

        @Override
        protected TeacherData clone() throws CloneNotSupportedException {
            return (TeacherData)super.clone();
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"date\":\"")
                .append(date).append('\"');
        sb.append(",\"timeBegin\":")
                .append(timeBegin);
        sb.append(",\"timeEnd\":")
                .append(timeEnd);
        sb.append(",\"teacherDates\":")
                .append(teacherDates);
        sb.append('}');
        return sb.toString();
    }
}
