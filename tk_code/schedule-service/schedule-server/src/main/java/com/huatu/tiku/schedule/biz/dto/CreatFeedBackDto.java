package com.huatu.tiku.schedule.biz.dto;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.domain.BaseDomain;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class CreatFeedBackDto implements Serializable {

    @NotNull(message = "类型不能为空")
    private ExamType examType;//考试类型

    @NotNull(message = "科目不能为空")
    private Long subjectId;

    @NotNull(message = "年份不能为空")
    private Integer year;

    @NotNull(message = "月份不能为空")
    private Integer month;

    @NotEmpty(message = "内容不能为空")
    private List<ClassHourInfoDto> infos= Lists.newArrayList();

    @Getter
    @Setter
    @ToString
    public static class ClassHourInfoDto extends BaseDomain {

        private static final long serialVersionUID = 7093957592966896503L;

        @NotNull(message = "教师不能为空")
        private Long teacherId;

        private Double reallyExam=0d;//真题题数

        private Double reallyHour=0.0;//真题课时

        private Double simulationExam=0d;//模拟题数

        private Double simulationHour=0.0;//模拟题课时

        private Double articleHour=0.0;//文章课时

        private Double audioHour=0.0;//音频课时

        private String remark;
    }


}
