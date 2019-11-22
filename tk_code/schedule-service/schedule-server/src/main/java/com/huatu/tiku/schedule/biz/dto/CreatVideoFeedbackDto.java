package com.huatu.tiku.schedule.biz.dto;

import com.huatu.tiku.schedule.base.domain.BaseDomain;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class CreatVideoFeedbackDto implements Serializable {

    private static final long serialVersionUID = -7682342432435692635L;

    @NotNull(message = "课程id不能为空")
    private Long courseId;//课程id

    @NotNull(message = "年份不能为空")
    private Integer year;

    @NotNull(message = "月份不能为空")
    private Integer month;

    @NotEmpty(message = "反馈记录不能为空")
    private List<Info> infos;

    @Getter
    @Setter
    public static class Info extends BaseDomain {

        private static final long serialVersionUID = 5907968238018200115L;

        @NotNull(message = "教师不能为空")
        private Long teacherId;

        @NotNull(message = "剪辑时长不能为空")
        private Double result;

        private String remark;
    }

}
