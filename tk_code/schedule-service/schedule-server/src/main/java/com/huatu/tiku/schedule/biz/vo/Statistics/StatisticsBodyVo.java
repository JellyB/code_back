package com.huatu.tiku.schedule.biz.vo.Statistics;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**外层vo
 * @author wangjian
 **/
@Data
public class StatisticsBodyVo implements Serializable {

    private static final long serialVersionUID = -8684121420331268391L;

    private Long teacherId;
    private String teacherName;

    private String count;
    private List<Object> body;
    private String countLiveSK;
    private String countLiveLX;
    private String countLiveZJ;
    private String countXXKSK;
    private String countXXKLX;
    private String countXXKZJ;
    private String countReally;
    private String countSimulation;
    private String countVideo;
    private String countArticle;
    private String countAudio;
    private String countXXKSchoolSK;
    private String countOnline;
    private String countOffline;
    private String countSSKSK;
    private String countSSKLX;
    private String countSSKZJ;
    private String countDMJZSK;
    private String countDMJZLX;
}
