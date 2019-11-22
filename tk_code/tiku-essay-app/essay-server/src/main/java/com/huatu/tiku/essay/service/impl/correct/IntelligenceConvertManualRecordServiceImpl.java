package com.huatu.tiku.essay.service.impl.correct;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.cache.QuestionReportRedisKeyConstant;
import com.huatu.tiku.essay.entity.correct.IntelligenceConvertManualRecord;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.v2.IntelligenceConvertManualRecordRepository;
import com.huatu.tiku.essay.service.correct.IntelligenceConvertManualRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/9
 * @描述 智能转人工记录
 */
@Service
public class IntelligenceConvertManualRecordServiceImpl implements IntelligenceConvertManualRecordService {


    @Autowired
    IntelligenceConvertManualRecordRepository recordRepository;

    @Autowired
    RedisTemplate redisTemplate;

    public List<Long> getConvertOrderIds(Long answerId, int answerType) {
        String cacheNull = "-1";
        List<Long> orderIds = new ArrayList<>();
        String key = QuestionReportRedisKeyConstant.getPaperConvertCount(answerType, answerId);
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object redisCount = valueOperations.get(key);
        if (null != redisCount) {
            if (redisCount.equals(cacheNull)) {
                return Lists.newArrayList();
            }
            orderIds = Arrays.stream(redisCount.toString().split(",")).map(id -> Long.valueOf(id))
                    .collect(Collectors.toList());
            return orderIds;
        } else {
            List<IntelligenceConvertManualRecord> records = recordRepository.findByIntelligenceAnswerIdAndAnswerTypeAndStatus(answerId, answerType, EssayStatusEnum.NORMAL.getCode());
            if (CollectionUtils.isNotEmpty(records)) {
                orderIds = records.stream().map(IntelligenceConvertManualRecord::getOrderId).collect(Collectors.toList());
                String collect = orderIds.stream().map(orderId -> String.valueOf(orderId)).collect(Collectors.joining(","));
                valueOperations.set(key, collect);
            } else {
                valueOperations.set(key, cacheNull);
            }
            redisTemplate.expire(key, 15, TimeUnit.MINUTES);
        }
        return orderIds;
    }
}
