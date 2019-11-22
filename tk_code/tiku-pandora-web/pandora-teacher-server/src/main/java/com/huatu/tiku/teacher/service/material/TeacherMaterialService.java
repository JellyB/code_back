package com.huatu.tiku.teacher.service.material;

import com.huatu.tiku.entity.material.Material;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.request.material.MaterialReq;
import com.huatu.tiku.service.BaseService;

import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\4\25 0025.
 */
public interface TeacherMaterialService extends BaseService<Material>{

    /**
     * 查询试题下的所有材料
     * @param questionId
     * @return
     */
    List<Material> findByQuestionId(Long questionId);

    /**
     * 修改试题关联材料
     * @param materials
     * @param questionId
     */
    void updateQuestionMaterial(List<MaterialReq> materials, Long questionId);

    /**
     * 添加试题关联材料
     * @param materials
     * @param questionId
     */
    void insertQuestionMaterial(List<MaterialReq> materials, Long questionId);

    /**
     * 添加|更新子題和材料的綁定关系
     * @param materialIds
     * @param questionId
     */
    void saveQuestionMaterialBindings(List<Long> materialIds, Long questionId);


    /**
     * 查询子题选中的材料id
     * @param baseQuestion
     * @return
     */
    List<Long> findMaterialIdsByQuestion(BaseQuestion baseQuestion);

    /**
     * 查询材料的重复情况
     * @param content 查重材料内容
     * @return 材料id->相似度比
     */
    Map<Long,Double> findDuplicate(String content);


    /**
     * 批量查询材料信息
     * @param materialIds
     * @return
     */
    List<Material> findByIds(List<Long> materialIds);
}
