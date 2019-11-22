package com.huatu.tiku.entity.advice;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;

/**
 * @author zhengyi
 */
@Table(name = "v_question_correction_log")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAdvice extends BaseEntity {

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    /**
     * 修改时间
     */
    private Timestamp gmtModify;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    private Timestamp gmtCreate;
    /**
     * 试题ID
     */
    private Integer questionId;

    /**
     * 试题类型
     */
    private String questionType;

    /**
     * 真题试卷ID
     */
    private Integer pastpaperId;

    /**
     * 所属考试科目
     */
    @JsonIgnore
    private Integer blSubExam;

    /**
     * 错误类型
     */
    @JsonIgnore
    private Integer errorType;

    /**
     * 纠错范围
     */
    @JsonIgnore
    private Integer errorScope;

    /**
     * 申论-问题模块
     */
    @JsonIgnore
    private Integer wrongModule;

    /**
     * 是否被采纳 1 已采纳 2未采纳  3未处理
     */
    private Integer checker;

    /**
     * 采纳人
     */
    private Integer acceptor;

    /**
     * 采纳时间
     */
    private Integer acceptTime;

    /**
     * 地区
     */
    private Integer questionArea;

    /**
     * 修改者id
     */
    @JsonIgnore
    private Integer moduleId;


    /**
     * 科目
     */
    @JsonIgnore
    private Integer subject;

    /**
     * 范围
     */
    @JsonIgnore
    private Integer catgory;

    /**
     * 提交人/用户ID
     */
    private Long userId;

    /**
     * 错误描述
     */
    private String errorDescrp;

    /**
     * 不采纳原因
     */
    private String checkContent;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 反馈内容
     */
    private String resultContent;

    /**
     * 内容
     */
    private String content;

    @Column(name = "nick_name")
    private String nickName;

    @Transient
    private String areaName;

    @Column(name = "user_area")
    private String userArea;

    @Column(name = "username")
    public String username ;

    @Column(name = "gold")
    public Integer gold;

}