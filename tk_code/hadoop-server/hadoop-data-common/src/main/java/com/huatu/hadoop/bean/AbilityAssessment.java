package com.huatu.hadoop.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@JsonIgnoreProperties({"pds"})
public class AbilityAssessment {

    @JsonIgnore
    @JSONField(serialize = false)
    private List<PointDetail> pds = new ArrayList<>();


    private Map<String, String> sta = new HashMap<>();

    private boolean flag = true;

    private String scoreText = " 有点伤感呢...";
    /**
     * user id
     */
    private Long userId = 0L;
    /**
     * subject
     */
    private Integer subject = 0;
    /**
     * 全站预测分
     */
    private Double predictedScore = 0.00;

    private String reportCard = "";

    private String avatar = "http://tiku.huatu.com/cdn/images/vhuatu/avatars/default.png";

    /**
     * 排名
     */
    private Integer rank = 0;
    /**
     * 对比上周排名
     */
    private Integer dif_rank = 0;
    /**
     * 超过人数
     */
    private Integer surpass_man = 0;

    /**
     * 已击败
     */
    private Double hasBeat = 0.00;
    /**
     * 做题数量
     */
    private Long doExerciseNum = 0L;

    /**
     * 正确率
     */
    private Double accuracy = 0.00;

    /**
     * 做题速度
     */
    private Long doExerciseTime = 0l;
    /**
     * 做题时长
     */
    private Long doExerciseDay = 0L;

    /**
     * 同比变化
     */
    private Double dif_predictedScore = 0.00;

    /**
     * 做题速度
     */
    private Double doExerciseSpead = 0.00;
    /**
     * 上岸
     */
    private String[] shangAn = {"75%", "50"};
    /**
     * 更新时间
     */
    private Long updateTime = 0L;
    /**
     *
     */
    private List<Map<String, Object>> userInfo = new ArrayList<>();

    private AbilityAssessmentWeekReport weekReport;

    private List<AATopUser> top10User = new ArrayList<>();

    private String[] textArea = new String[6];
    private String[] weekTextArea = new String[4];


    public void setPds(List<PointDetail> pds) {
        this.pds = pds;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public void setPredictedScore(Double predictedScore) {

        if (predictedScore.isNaN() || predictedScore.isInfinite()) {
            this.predictedScore = 0.00;
        } else {
            this.predictedScore = predictedScore;
        }

        if (this.predictedScore >= 0.0 && this.predictedScore < 40.0) {
            this.scoreText = "有点伤感呢...";
        } else if (this.predictedScore >= 40.0 && this.predictedScore < 60.0) {
            this.scoreText = "还需要继续努力！";
        } else if (this.predictedScore >= 6.0 && this.predictedScore < 70.0) {
            this.scoreText = "不错，继续加油！";
        } else if (this.predictedScore >= 70.0 && this.predictedScore < 80.0) {
            this.scoreText = "离成功一步之遥！";
        } else if (this.predictedScore >= 80.0 && this.predictedScore < 90.0) {
            this.scoreText = "学霸，看好你呦！";
        } else if (this.predictedScore >= 90.0 && this.predictedScore <= 100.0) {
            this.scoreText = "大神，膜拜一下！";
        }
    }

    public void setReportCard(String reportCard) {
        this.reportCard = reportCard;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public void setDif_rank(Integer dif_rank) {
        this.dif_rank = dif_rank;
    }

    public void setSurpass_man(Integer surpass_man) {
        this.surpass_man = surpass_man;
    }

    public void setHasBeat(Double hasBeat) {
        if (hasBeat.isNaN() || hasBeat.isInfinite()) {
            this.hasBeat = 0.00;
        } else {
            this.hasBeat = hasBeat;
        }
    }

    public void setDoExerciseNum(Long doExerciseNum) {
        this.doExerciseNum = doExerciseNum;
    }

    public void setAccuracy(Double accuracy) {
        if (accuracy.isNaN() || accuracy.isInfinite()) {
            this.accuracy = 0.00;
        } else {
            this.accuracy = accuracy;
        }
    }

    public void setDoExerciseTime(Long doExerciseTime) {
        this.doExerciseTime = doExerciseTime;
    }

    public void setDoExerciseDay(Long doExerciseDay) {
        this.doExerciseDay = doExerciseDay;
    }

    public void setDif_predictedScore(Double dif_predictedScore) {
        if (dif_predictedScore.isNaN() || dif_predictedScore.isInfinite()) {
            this.dif_predictedScore = 0.00;
        } else {
            this.dif_predictedScore = dif_predictedScore;
        }
    }

    public void setDoExerciseSpead(Double doExerciseSpead) {
        if (doExerciseSpead.isNaN() || doExerciseSpead.isInfinite()) {
            this.doExerciseSpead = 0.00;
        } else {
            this.doExerciseSpead = doExerciseSpead;
        }
    }

    public void setShangAn(String[] shangAn) {
        this.shangAn = shangAn;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public void setUserInfo(List<Map<String, Object>> userInfo) {
        this.userInfo = userInfo;
    }

    public void setWeekReport(AbilityAssessmentWeekReport weekReport) {
        this.weekReport = weekReport;
    }

    public void setTop10User(List<AATopUser> top10User) {
        this.top10User = top10User;
    }

    public void setTextArea(String[] textArea) {
        this.textArea = textArea;
    }

    public void setWeekTextArea(String[] weekTextArea) {
        this.weekTextArea = weekTextArea;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this, true);
    }
}
