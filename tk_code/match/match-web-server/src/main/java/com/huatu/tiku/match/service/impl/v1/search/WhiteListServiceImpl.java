package com.huatu.tiku.match.service.impl.v1.search;

import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.service.v1.search.WhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

/**
 * 描述：白名单
 *
 * @author biguodong
 * Create time 2018-10-16 下午2:02
 **/
@Service
@Slf4j
public class WhiteListServiceImpl implements WhiteListService{

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Boolean isWhiteMember(int userId) {
        SetOperations opsForSet = redisTemplate.opsForSet();
        Boolean member = opsForSet.isMember(MatchInfoRedisKeys.getMatchWhitUserReportKey(), String.valueOf(userId));
//        return member;
        return false;
    }
}
