package com.huatu.tiku.teacher.service.material;

import com.huatu.tiku.entity.material.QuestionMaterial;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * Created by huangqp on 2018\6\12 0012.
 */
public interface QuestionMaterialService extends BaseService<QuestionMaterial> {

    /**
     * 物理删除材料试题关系
     * @param id
     */
    int deleteByQuestionId(Long id);

    /**
     * 批量添加材料试题关系
     * @param materialIds
     * @param id
     */
    void insertBatch(List<Long> materialIds, Long id);

    /**
     * 逻辑删除材料试题关系
     */
    void deleteByLogic(Long id);

}

