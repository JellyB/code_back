package com.huatu.tiku.position.biz.service.impl;

import com.huatu.tiku.position.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.position.biz.constant.RedisConstant;
import com.huatu.tiku.position.biz.domain.BrowseRecord;
import com.huatu.tiku.position.biz.respository.BrowseRecordRepository;
import com.huatu.tiku.position.biz.service.BrowseRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**浏览记录
 * @author wangjian
 **/
@Service
public class BrowseRecordServiceImpl extends BaseServiceImpl<BrowseRecord,Long> implements BrowseRecordService   {

    @Autowired
    private BrowseRecordRepository browseRecordRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addRecord(Long userId, Long positionId) {
        BrowseRecord br = new BrowseRecord();
        br.setUserId(userId);
        br.setPositionId(positionId);
        br.setCollectionFlag(false);//默认不收藏
        try {
            browseRecordRepository.save(br);

            String key = RedisConstant.POSITION_BROWSE_COUNTER.replace("{id}", positionId.toString());

            String count = stringRedisTemplate.opsForValue().get(key);
            if (count == null) {
                stringRedisTemplate.opsForValue().set(key, browseRecordRepository.countByPositionId(positionId).toString());
                stringRedisTemplate.expire(key, 6, TimeUnit.HOURS);
            } else {
                stringRedisTemplate.opsForValue().increment(key, 1);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Integer addPositionRemark(Long userId, Long positionId, Boolean accordFlag) {
        return browseRecordRepository.addPositionRemark(userId, positionId, accordFlag);
    }

    /**
     * 查询是否符合备注条件
     */
    @Override
    public BrowseRecord findByUserIdAndPositionId(Long userId, Long positionId) {
        return browseRecordRepository.findByUserIdAndPositionId(userId, positionId);
    }

    @Override
    public Integer addPositionCollection(Long userId, Long positionId, Boolean flag) {
        return browseRecordRepository.addPositionCollection(userId, positionId, flag);
    }
}
