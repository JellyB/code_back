package com.huatu.ztk.question.daoPandora.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Table(name = "v_question_correction_log")
public class QuestionAdvice extends BaseEntity {
    /**
     * 要纠错的试题id
     */
    @Column(name = "question_id")
    private int questionId;
    /**
     * 试题类型
     */
    @Column(name = "question_type")
    private int questionType;
    /**
     * 试题模块id
     */
    @Column(name = "module_id")
    private int moduleId;
    /**
     * 试题区域
     */
    @Column(name = "question_area")
    private int questionArea;
    /**
     * 所属科目
     */
    @Column(name = "bl_sub_exam")
    private int blSubExam;
    /**
     * 所属类目
     */
    private int subject;
    /**
     * 错误类型
     */
    @Column(name = "error_type")
    private int errorType;
    /**
     * 联系方式
     */
    private String contacts;
    /**
     * 纠错内容
     */
    private String content;
    /**
     *
     */
    @Column(name = "error_descrp")
    private String errorDescrp;
    /**
     * userId
     */
    @Column(name = "user_id")
    private long userId;

    /**
     * checker
     */
    private Integer checker;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "user_area")
    private int userArea;

    @Column(name = "username")
    public String username;

    @Column(name = "gold")
    public Integer gold;
}