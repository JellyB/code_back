package com.huatu.ztk.knowledge.servicePandora;

import com.huatu.tiku.entity.subject.Subject;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import service.BaseServiceHelper;

import java.util.List;

/**
 * Created by lijun on 2018/8/22
 */
public interface SubjectService extends BaseServiceHelper<Subject> {

    /**
     * 通过学科 获取考试类型
     * 此方法替换 SubjectDao.getCatgoryBySubjectId(int subject)
     *
     * @param subjectId 学科ID
     * @return 考试类型
     */
    int getCategoryBySubjectId(int subjectId);

    /**
     * 通过考试类型ID 获取所有的子节点信息
     * 此方法替换 SubjectTreeDao.findChildrens(int sid)
     *
     * @param subjectTree 考试类型
     * @return 学科
     */
    List<SubjectTree> findChildren(SubjectTree subjectTree);

    /**
     * 根据ID 查询数据
     *
     * @param subjectId 科目ID
     * @return 学科信息
     */
    SubjectTree findById(int subjectId);

    /**
     * 通过考试类型 获取科目
     * @param category 考试类型
     * @return 科目列表
     */
    List<Long> getSubjectIdListByCategory(int category);
}
