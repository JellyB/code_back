package com.huatu.tiku.teacher.service;

import com.huatu.tiku.entity.teacher.PaperActivitySubject;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * 活动卷-科目关系
 * Created by huangqp on 2018\6\23 0023.
 */
public interface PaperActivitySubjectService extends BaseService<PaperActivitySubject>{
    /**
     * 插入科目信息
     * @param paperId
     * @param subjectIds
     */
    void insertPaperSubject(Long paperId, List<Long> subjectIds);

    /**
     * 删除试卷相关科目信息- 物理删除
     * @param paperId
     */
    void deleteByPaperId(Long paperId);

    /**
     * 查询试卷相关科目
     * @param id
     * @return
     */
    List<Long> findSubjectByPaperId(Long id);
}

