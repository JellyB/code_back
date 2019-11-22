package com.huatu.tiku.essay.service.impl;

import com.huatu.tiku.essay.constant.status.EssayQuestionMaterialConstant;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.entity.EssayQuestionMaterial;
import com.huatu.tiku.essay.repository.EssayQuestionMaterialRepository;
import com.huatu.tiku.essay.service.EssayQuestionMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouwei
 * @Description: 试题材料管理
 * @create 2017-12-17 下午4:35
 **/
@Slf4j
@Service
//@Transactional
public class EssayQuestionMaterialServiceImpl implements EssayQuestionMaterialService {
    @Autowired
    EssayQuestionMaterialRepository essayQuestionMaterialRepository;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<EssayQuestionMaterial> getEssayQuestionMaterialList(long id) {

        String key = RedisKeyConstant.getSingleQuestionMaterialKey(id);

        List<EssayQuestionMaterial> essayQuestionMaterialList = (List<EssayQuestionMaterial>) redisTemplate.opsForValue().get(key);

        if (CollectionUtils.isEmpty(essayQuestionMaterialList)) {
            essayQuestionMaterialList = essayQuestionMaterialRepository.findByQuestionBaseIdAndStatusAndBizStatusOrderBySortAsc(id, EssayQuestionMaterialConstant.EssayQuestionMaterialStatusEnum.NORMAL.getStatus(), EssayQuestionMaterialConstant.EssayQuestionMaterialBizStatusEnum.CONNECTED.getBizStatus());

            redisTemplate.opsForValue().set(key, essayQuestionMaterialList);
            redisTemplate.expire(key,50, TimeUnit.MINUTES);
        }      return essayQuestionMaterialList;
    }
}
