package com.huatu.tiku.schedule.biz.vo;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.ClassHourFeedback;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class ClassHourFeedBackVo implements Serializable{

    private static final long serialVersionUID = -320992221046899087L;

    private Long id;

    private ExamType examType;

    private String subject;

    private Long subjectId;

    private String date;

    private Integer year;

    private Integer month;

    private FeedbackStatus status;

    private List<ClassHourInforVo> info= Lists.newArrayList();

    public ClassHourFeedBackVo(ClassHourFeedback bean){
        if(null==bean){
            throw new BadRequestException("feedback id exception");
        }
        this.id=bean.getId();
        this.examType = bean.getExamType();
        this.subjectId=bean.getSubjectId();
        Subject subject = bean.getSubject();
        if(null!=subject){
            this.subject=subject.getName();
        }
        this.date= DateformatUtil.format0(bean.getCreatedDate());
        this.year=bean.getYear();
        this.month=bean.getMonth();
        this.status=bean.getStatus();
    }
}
