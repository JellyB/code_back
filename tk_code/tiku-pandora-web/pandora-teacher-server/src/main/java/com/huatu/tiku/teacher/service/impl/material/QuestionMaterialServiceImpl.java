package com.huatu.tiku.teacher.service.impl.material;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.material.QuestionMaterial;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.question.QuestionMaterialMapper;
import com.huatu.tiku.teacher.service.material.QuestionMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by huangqp on 2018\6\12 0012.
 */
@Service
public class QuestionMaterialServiceImpl extends BaseServiceImpl<QuestionMaterial> implements QuestionMaterialService {
    public QuestionMaterialServiceImpl() {
        super(QuestionMaterial.class);
    }

    @Autowired
    QuestionMaterialMapper questionMaterialMapper;

    @Override
    @Transactional
    public int deleteByQuestionId(Long id) {
        Example example = new Example(QuestionMaterial.class);
        example.and().andEqualTo("questionId", id);
        return questionMaterialMapper.deleteByExample(example);
    }

    @Override
    @Transactional
    public void insertBatch(List<Long> materialIds, Long id) {
        List<QuestionMaterial> questionMaterials = Lists.newArrayList();
        for (int i = 0; i < materialIds.size(); i++) {
            Long materialId = materialIds.get(i);
            QuestionMaterial questionMaterial = QuestionMaterial.builder().questionId(id).materialId(materialId).sort(i + 1).build();
            questionMaterials.add(questionMaterial);
        }
        insertAll(questionMaterials);
    }


    /**
     * 逻辑删除材料试题关系
     */
    @Override
    @Transactional
    public void deleteByLogic(Long id) {
        Example example=new Example(QuestionMaterial.class);
        example.and().andEqualTo("questionId", id);
        this.deleteByExample(example);


    }
}

