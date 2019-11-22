package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.EssayQuestionMaterial;

import java.util.List;

/**
 * @author zhouwei
 * @Description: 试题材料关联表service
 * @create 2017-12-17 下午4:35
 **/
public interface EssayQuestionMaterialService {
    /*   优先从缓存中获取  */
    List<EssayQuestionMaterial> getEssayQuestionMaterialList(long id);

}
