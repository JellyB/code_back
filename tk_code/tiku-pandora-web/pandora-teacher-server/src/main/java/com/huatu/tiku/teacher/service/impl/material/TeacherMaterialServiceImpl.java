package com.huatu.tiku.teacher.service.impl.material;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.entity.material.Material;
import com.huatu.tiku.entity.material.QuestionMaterial;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.request.material.MaterialReq;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.material.QuestionMaterialService;
import com.huatu.tiku.teacher.service.material.TeacherMaterialService;
import com.huatu.tiku.util.question.StringMatch;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\25 0025.
 */
@Service
public class TeacherMaterialServiceImpl extends BaseServiceImpl<Material> implements TeacherMaterialService {
    @Autowired
    private QuestionMaterialService questionMaterialService;

    public TeacherMaterialServiceImpl() {
        super(Material.class);
    }


    @Override
    public List<Material> findByQuestionId(Long questionId) {
        Example example = new Example(QuestionMaterial.class);
        example.and().andEqualTo("questionId", questionId);
        List<QuestionMaterial> questionMaterials = questionMaterialService.selectByExample(example);
        if (CollectionUtils.isEmpty(questionMaterials)) {
            return Lists.newArrayList();
        }
        Map<Long, Integer> materialSort = questionMaterials.stream().collect(Collectors.toMap(i -> i.getMaterialId(), i -> i.getSort()));
        List<Long> materialIds = questionMaterials.stream().map(i -> i.getMaterialId()).collect(Collectors.toList());
        List<Material> materials = findByIds(materialIds);
        if (CollectionUtils.isEmpty(materials)) {
            return Lists.newArrayList();
        }
        materials.sort(Comparator.comparingInt(i -> materialSort.get(i.getId())));
        return materials;
    }

    @Override
    public List<Material> findByIds(List<Long> materialIds) {
        Example example = new Example(Material.class);
        example.and().andIn("id", materialIds);
        return selectByExample(example);
    }


    @Override
    @Transactional
    public void updateQuestionMaterial(List<MaterialReq> materials, Long questionId) {
        List<Long> newIds = Lists.newArrayList();
        //材料数据添加
        for (int k = 0; k < materials.size(); k++) {
            MaterialReq materialReq = materials.get(k);
            if (StringUtils.isNotBlank(materialReq.getContent())) {
                if (materialReq.getMaterialId() == null || materialReq.getMaterialId() <= 0) {
                    Material material = insertMaterial(materialReq);
                    newIds.add(material.getId());
                } else {
                    updateMaterial(materialReq);
                    newIds.add(materialReq.getMaterialId());
                }
            }
        }
        //关联表数据修改
        saveQuestionMaterialBindings(newIds, questionId);
    }

    /**
     * 添加材料和绑定关系
     *
     * @param materials
     * @param questionId
     */
    @Override
    @Transactional
    public void insertQuestionMaterial(List<MaterialReq> materials, Long questionId) {
        for (int k = 0; k < materials.size(); k++) {
            MaterialReq materialReq = materials.get(k);
            Long materialId = materialReq.getMaterialId();
            if (materialId == null || materialId <= 0) {
                //添加材料
                Material material = insertMaterial(materialReq);
                materialReq.setMaterialId(material.getId());
            }
            //添加绑定关系
            insertQuestionMaterial(materialReq, questionId, k);
        }
    }

    /**
     * 子题和材料绑定关系批量添加
     *
     * @param materialIds
     * @param questionId
     */
    @Override
    @Transactional
    public void saveQuestionMaterialBindings(List<Long> materialIds, Long questionId) {
        //清空试题材料关系
        questionMaterialService.deleteByQuestionId(questionId);
        //批量添加
        questionMaterialService.insertBatch(materialIds, questionId);
    }


    private void updateMaterial(MaterialReq materialReq) {
        Material material = Material.builder().contentFilter(StringMatch.replaceNotChinese(materialReq.getContent())).content(materialReq.getContent()).build();
        material.setId(materialReq.getMaterialId());
        save(material);
    }

    /**
     * 添加材料绑定关系
     *
     * @param materialReq
     * @param questionId
     * @param sort
     */
    private void insertQuestionMaterial(MaterialReq materialReq, Long questionId, int sort) {
        Long materialId = materialReq.getMaterialId();
        if (materialId == null || materialId <= 0) {
            Material material = insertMaterial(materialReq);
            materialId = material.getId();
        }
        QuestionMaterial questionMaterial = QuestionMaterial.builder().questionId(questionId).materialId(materialId).sort(sort).build();
        questionMaterialService.save(questionMaterial);
    }

    /**
     * 添加材料
     *
     * @param materialReq
     * @return
     */
    private Material insertMaterial(MaterialReq materialReq) {
        Material material = Material.builder().content(materialReq.getContent()).contentFilter(StringMatch.replaceNotChinese(materialReq.getContent())).build();
        save(material);
        return material;
    }

    /**
     * 删除试题材料绑定关系
     *
     * @param questionId
     */
    public void deleteQuestionMaterial(Long questionId) {
        Example example = new Example(QuestionMaterial.class);
        example.and().andEqualTo("questionId", questionId);
        questionMaterialService.deleteByExample(example);
    }

    /**
     * 查询子题选中的材料id
     * 先查询自身是否绑定材料，如果有以自己绑定的材料id为准，如果没有，说明选中所有复合题的材料，查询复合题绑定的所有材料
     *
     * @param baseQuestion
     * @return
     */
    @Override
    public List<Long> findMaterialIdsByQuestion(BaseQuestion baseQuestion) {
        /**
         * 查询试题材料关系
         */
        Function<Long, List<QuestionMaterial>> findData = (id -> {
            Example example = new Example(QuestionMaterial.class);
            example.and().andEqualTo("questionId", id);
            return questionMaterialService.selectByExample(example);
        });
        List<QuestionMaterial> childMaterials = findData.apply(baseQuestion.getId());
        if (CollectionUtils.isNotEmpty(childMaterials)) {
            return childMaterials.stream().map(i -> i.getMaterialId()).collect(Collectors.toList());
        }
        List<QuestionMaterial> parentMaterials = findData.apply(baseQuestion.getMultiId());
        if (CollectionUtils.isEmpty(parentMaterials)) {
            return Lists.newArrayList();
        }
        return parentMaterials.stream().map(i -> i.getMaterialId()).collect(Collectors.toList());
    }

    @Override
    public Map<Long, Double> findDuplicate(String content) {
        //去除无用数据
        String contentFilter = StringMatch.replaceNotChinese(content);
        final String contentFinalFilter;
        if (contentFilter.length() > 100) {
            //如果文章够长，为防止材料之前有说明性文字，比如：阅读下面材料，……;所以取中间的内容做比较
            contentFinalFilter = contentFilter.substring(contentFilter.length() / 3, contentFilter.length() * 2 / 3);
        } else {
            contentFinalFilter = contentFilter;
        }
        //条件查询
        Example example = new Example(Material.class);
        example.and().andEqualTo("status", 1);
        if (StringUtils.isNotBlank(contentFinalFilter)) {
            example.and().andLike("contentFilter", "%" + contentFinalFilter.trim() + "%");
        }else{
            return Maps.newHashMap();
        }
        List<Material> materials = selectByExample(example);
        if (CollectionUtils.isEmpty(materials)) {
            return Maps.newHashMap();
        }
        //统计查询结果列表对应的材料跟查询条件的相似度
        Map<Long, Double> mapData = materials.stream().collect(Collectors.toMap(i -> i.getId(), i -> StringMatch.getSimilar(i.getContentFilter(), contentFilter)));
        return mapData;
    }

}
