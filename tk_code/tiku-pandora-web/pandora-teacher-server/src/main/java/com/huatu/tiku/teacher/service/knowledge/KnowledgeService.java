package com.huatu.tiku.teacher.service.knowledge;

import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.request.knowledge.InsertKnowledgeReq;
import com.huatu.tiku.request.knowledge.UpdateKnowledgeReq;
import com.huatu.tiku.service.BaseService;
import com.huatu.ztk.question.bean.KnowledgeInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouwei
 * @Description: service
 * @create 2018-04-23 下午3:14
 **/
public interface KnowledgeService extends BaseService<Knowledge> {

    /**
     * 查询某一科目的知识点
     *
     * @param subject
     * @param countFlag 是否附带统计数据
     * @return
     */
    List<KnowledgeVO> showKnowledgeTreeBySubject(Long subject, boolean countFlag);

    /**
     * 添加 - 知识点
     * 新增知识点需要保证选定科目在上级知识点对应的科目范围内
     *
     * @param knowledge
     */
    long insertKnowledge(InsertKnowledgeReq knowledge);

    /**
     * 更新 - 知识点
     *
     * @param updateKnowledgeReq
     */
    long updateKnowledge(UpdateKnowledgeReq updateKnowledgeReq);

    /**
     * 删除 - 知识点
     *
     * @param knowledgeId
     * @return
     */
    int deleteKnowledge(Long knowledgeId);

    /**
     * 修改试题知识点关联信息
     *
     * @param knowledgeIds
     * @param questionId
     */
    void updateQuestionKnowledge(List<Long> knowledgeIds, Long questionId);

    /**
     * 批量获取科目的子节点
     *
     * @param parentSubjectIds
     */
    List<Long> findChildSubject(List<Long> parentSubjectIds);

    /**
     * 根据知识点批量查询知识点完整名称
     *
     * @param ids
     * @return
     */
    List<String> getKnowledgeNameByIds(List<Long> ids);

    /**
     * 通过知识点信息查询知识点id
     * 知识点信息格式 （一级知识点1*二级知识点1*三级知识点1）
     *
     * @param knowledgeList
     * @param knowledgeVOS
     * @return
     */
    List<Long> getKnowledgeIdByInfo(List<String> knowledgeList, List<KnowledgeVO> knowledgeVOS);


    /**
     * 查询知识点所属的科目信息
     *
     * @param knowledgeIds 知识点ID
     * @return knowledgeId, subjectName, subjectId
     */
    List<HashMap<String, Object>> findSubjectInfoByIds(List<Long> knowledgeIds);

    /**
     * 知识树维护逻辑
     *
     * @param subject
     * @return
     */
    List treeBySubject(Long subject);

    /**
     * 获取知识点的名称
     *
     * @param ids 知识点ID
     * @return 一级+二级+三级
     */
    List<Map> getKnowledgeInfoByIds(List<Long> ids);


    /**
     * 返回知识点相对的所有层级情况
     *
     * @param knowledgeIds 底层知识点IDS
     * @return 对应的所有层级知识点ID和名称
     */
    List<KnowledgeInfo> getPointListByIds(List<Long> knowledgeIds);

    /**
     * 查询某一节点下的所有叶子节点
     *
     * @param subjectId
     * @return
     */
    String getKnowledgeInfo(long subjectId, Long parentId);

    /**
     * 获取所有的字节点（包含自身）
     */
    List<Knowledge> getAllChildrenKnowledge(long subjectId, Long parentId);

    /**
     * 缓存查询全部知识点
     *
     * @return
     */
    List<Knowledge> findAll();

    /**
     * 将知识点列表转换为树形结构
     *
     * @param knowledgeList 知识点列表
     * @param parentId      顶级结点ID
     * @return
     */
    List<KnowledgeVO> assertKnowledgeTree(List<Knowledge> knowledgeList, Long parentId);

    /**
     * 把一个 不存在的 knowledgeId 转换成 同级的 '其他（' ID
     *
     * @param knowledgeId 待转换ID
     * @param parentId    父类ID
     * @return 知识点存在、无法转换 返回本来ID；其他情况返回 转换后的ID
     */
    Long transKnowledgeId(Long knowledgeId, Long parentId);

    /**
     * 根据知识点ID批量查询知识点信息
     *
     * @param knowledgeIds
     * @return
     */
    List<Knowledge> findKnowledgeInfoByKnowIds(List<Long> knowledgeIds);
}
