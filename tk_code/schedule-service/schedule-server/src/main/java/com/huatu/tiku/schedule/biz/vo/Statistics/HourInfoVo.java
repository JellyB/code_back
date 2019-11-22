package com.huatu.tiku.schedule.biz.vo.Statistics;

import com.huatu.tiku.schedule.biz.domain.ClassHourInfo;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**日期排序vo
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class HourInfoVo extends CourseLive implements Serializable{
    private static final long serialVersionUID = -4360875011636997297L;

    private Date date;

    private Long teacherId;

    private Long feedbackId;

    private Double reallyExam;//真题题数

    private Double reallyHour;//真题课时

    private Double simulationExam;//模拟题数

    private Double simulationHour;//模拟题课时

    private Integer year;//年

    private Integer month;//月

    private Double articleHour;//文章课时

    private Double audioHour;//音频课时


    public HourInfoVo(ClassHourInfo info){
        this.year=info.getFeedback().getYear();
        this.month=info.getFeedback().getMonth();
        this.date=info.getFeedback().getDate();
        this.teacherId=info.getTeacherId();
        this.feedbackId=info.getFeedbackId();
        this.reallyExam=info.getReallyExam();
        this.reallyHour=info.getReallyHour();
        this.simulationExam=info.getSimulationExam();
        this.simulationHour=info.getSimulationHour();
        this.articleHour=info.getArticleHour();
        this.audioHour=info.getAudioHour();
    }
}
