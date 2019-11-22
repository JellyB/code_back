package com.huatu.tiku.essay.task;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.label.LabelRedisKeyConstant;
import com.huatu.tiku.essay.service.EssayLabelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * 获取下一篇的锁
 * @author zhaoxi
 */
@Component
@Slf4j
public class GetNextLabelTask{
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private EssayLabelService essayLabelService;


    public Long getNextLabel(String admin,long areaId, String year, double examScoreMin, int wordNumMin, double subScoreRatioMin,
                             double examScoreMax, int wordNumMax, double subScoreRatioMax,
                             int labelStatus, long questionId, String stem) throws BizException {
        long next = 0L;
        if (!getLock(admin)) {
                return next;
            }
            try {
                //业务具体实现
                next = essayLabelService.findNext(admin,areaId,year,examScoreMin,wordNumMin,subScoreRatioMin,examScoreMax,wordNumMax,subScoreRatioMax,labelStatus,questionId,stem);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                unlock(admin);
            }
            return next;
        }



        /**
         * 释放定时任务锁
         */
        private void unlock(String admin) {
            String lockKey = LabelRedisKeyConstant.getNextLabelLockKey();
            String currentAdmin = (String)redisTemplate.opsForValue().get(lockKey);

            log.info("current admin={}",currentAdmin);

            if (admin.equals(currentAdmin)) {
                redisTemplate.delete(lockKey);

                log.info("release lock,admin={},timestamp={}",currentAdmin,System.currentTimeMillis());
            }
        }

        /**
         *
         * @return 是否获得锁
         */
        private boolean getLock(String admin) {
            ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();

            String lockKey = LabelRedisKeyConstant.getNextLabelLockKey();
            String value = opsForValue.get(lockKey);


            if (StringUtils.isBlank(value)) { //值为空
                boolean booleanValue = opsForValue.setIfAbsent(lockKey, admin).booleanValue();
                log.info("booleanValue：{}，当前定时器被用户：{}锁定",booleanValue,opsForValue.get(lockKey));
                if(booleanValue){
                    redisTemplate.expire(lockKey,2, TimeUnit.MINUTES);
                }
                if(booleanValue || admin.equals(opsForValue.get(lockKey))){
                    return true;
                }else{
                    return false;
                }

            } else if (StringUtils.isNoneBlank(value) && !value.equals(admin)) {
                //被其它服务器锁定
                log.info("get next label lock admin={},return", value);
                return false;
            } else { //被自己锁定
                return true;
            }
        }
}