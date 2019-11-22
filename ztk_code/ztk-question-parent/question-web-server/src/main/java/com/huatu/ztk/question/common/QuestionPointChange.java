package com.huatu.ztk.question.common;

/**
 * @author hanchao
 * @date 2017/11/1 17:05
 */

public class QuestionPointChange {
    private int id;
    private int questionId;
    private int newPointId;
    private int oldPointId;
    private int subject;
    private int level;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getNewPointId() {
        return newPointId;
    }

    public void setNewPointId(int newPointId) {
        this.newPointId = newPointId;
    }

    public int getOldPointId() {
        return oldPointId;
    }

    public void setOldPointId(int oldPointId) {
        this.oldPointId = oldPointId;
    }

    public int getSubject() {
        return subject;
    }

    public void setSubject(int subject) {
        this.subject = subject;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
