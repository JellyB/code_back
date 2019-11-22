package com.huatu.ztk.knowledge.cacheTask.model;

import java.util.Date;

/**
 * Created by junli on 2018/3/19.
 */
public class QuestionPersistenceModel {

    private String userId;//用户ID
    private String questionPointId;//试题考点ID - 本字段只用存储时数据区分,不作为最终标识字段
    private String questionId;//试题ID
    private Date updateTime;//数据更新时间

    private Integer state;//数据状态,用以逻辑删除:1.正常数据,0.删除状态

    public QuestionPersistenceModel() {
    }

    //默认为可用状态
    public QuestionPersistenceModel(String userId, String questionPointId, String questionId) {
        this.userId = userId;
        this.questionPointId = questionPointId;
        this.questionId = questionId;
        this.updateTime = new Date();
        this.state = QuestionPersistenceEnum.DataState.IN_USING.getState();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(QuestionPersistenceEnum.DataState dataState) {
        this.state = dataState.getState();
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public String getQuestionPointId() {
        return questionPointId;
    }

    public void setQuestionPointId(String questionPointId) {
        this.questionPointId = questionPointId;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
