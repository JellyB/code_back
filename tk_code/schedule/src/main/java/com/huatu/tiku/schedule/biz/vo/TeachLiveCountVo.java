package com.huatu.tiku.schedule.biz.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by duanxiangchao on 2018/5/10
 */
@Data
public class TeachLiveCountVo implements Serializable {

    private Long teacherId;

    private Integer count;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"teacherId\":")
                .append(teacherId);
        sb.append(",\"count\":")
                .append(count);
        sb.append('}');
        return sb.toString();
    }
}
