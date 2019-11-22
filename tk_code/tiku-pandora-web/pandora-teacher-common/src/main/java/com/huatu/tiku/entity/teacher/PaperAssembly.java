package com.huatu.tiku.entity.teacher;

import com.huatu.common.bean.BaseEntity;
import com.huatu.tiku.entity.question.PaperQuestionSimpleInfo;
import com.huatu.tiku.entity.question.QuestionSimpleInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;

/**
 * 组卷 - 试卷
 * Created by lijun on 2018/8/16
 */
@NoArgsConstructor
@Data
@Table(name = "paper_assembly")
public class PaperAssembly extends BaseEntity {

    /**
     * 试卷名称
     */
    private String name;

    /**
     * 试卷类型
     */
    private Integer type;

    /**
     * 试题ID
     */
    @Transient
    private String questionIds;

    @Transient
    private Integer questionCount;

    /**
     * 科目ID
     */
    private Long subjectId;

    /**
     * 试题信息
     */
    @Transient
    private List<QuestionSimpleInfo> questionSimpleInfoList;

    @Transient
    private  List<PaperQuestionSimpleInfo> paperQuestionSimpleInfos;

    public PaperAssembly(Long id, Integer bizStatus, Integer status, Long creatorId, Timestamp gmtCreate, Long modifierId, Timestamp gmtModify, String name, Integer type, String questionIds, Integer questionCount, List<QuestionSimpleInfo> questionSimpleInfoList) {
        super(id, bizStatus, status, creatorId, gmtCreate, modifierId, gmtModify);
        this.name = name;
        this.type = type;
        this.questionIds = questionIds;
        this.questionCount = questionCount;
        this.questionSimpleInfoList = questionSimpleInfoList;
    }
}
