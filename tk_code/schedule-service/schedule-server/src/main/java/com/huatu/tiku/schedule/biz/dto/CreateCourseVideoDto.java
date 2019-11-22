package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class CreateCourseVideoDto implements Serializable {
    private static final long serialVersionUID = -3105962501675868234L;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;//课程id

    @NotNull(message = "房间不能为空")
    private Long roomId;

    @NotBlank(message = "录制内容不能为空")
    private String videoName;//录制内容

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "录课日期不能为空")
    private Date date;

    @NotBlank(message = "开始时间不能为空")
    private String timeBegin;//开始时间

    @NotBlank(message = "结束时间不能为空")
    private String timeEnd;//结束时间

    private Long subjectId;//科目

    @NotNull(message = "讲师不能为空")
    private Long teacherId;//教师

    @NotEmpty(message = "摄影师不能为空")
    private List<Long> photographerIds;//摄影师

    private Long zkTeacherId;//质控师

}
