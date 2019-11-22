package com.huatu.tiku.teacher.service.subject;

import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.response.subject.SubjectNodeResp;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
public interface TeacherSubjectService extends BaseService<Subject> {


    /**
     * 查询试题绑定的科目（查询试题详情-修改回显）
     *
     * @param subject
     * @return
     */
    SubjectNodeResp parseSubject(Long subject);


    /**
     * 查询科目（subjectId 可能对应不同层级的两个科目，需要做筛选）
     *
     * @param subjectId
     * @return
     */
    Subject selectById(Long subjectId);

    /**
     * 科目统计信息（科目包含的子节点，及每个节点下包含的题量，试卷数量）
     *
     * @return
     */
    Object getSubjectCount();

    /**
     * 查询科目名称
     *
     * @param grades
     * @return
     */
    List<String> getNameByIds(List<Long> grades);

    /**
     * @param subjectId 学科ID
     * @return
     */
    List<Subject> findChildren(Long subjectId, int level);

    /**
     * 获取当前科目的子级科目
     *
     * @param parentId 科目ID
     * @return 子集科目
     */
    List<Subject> findChildren(Long parentId);

    /**
     * 知识点可选科目树
     *
     * @param knowledgeId
     * @return 学科+学段
     */
    Object treeForKnowledge(Long knowledgeId);

    /**
     * 查找一级节点
     *
     * @param subjectId
     * @param level
     * @return
     */
    Long findParent(Long subjectId, int level);

    /**
     * 查找 兄弟节点信息
     *
     * @param subjectId 当前的节点
     * @return 查找兄弟节点信息（包含当前节点）
     */
    List<Subject> findFriendNodes(Long subjectId);

    /**
     * 根据级别获取子科目ID
     *
     * @param subjectId 父科目ID
     * @param level     级别
     * @return
     */
    List<Integer> findChildrenId(Long subjectId, int level);

    /**
     * 查询所有的一级考试类别
     *
     * @param level  级别
     * @param parent 父科目ID
     * @return
     */
    List<Subject> findAllCategory(int level, int parent);

}
