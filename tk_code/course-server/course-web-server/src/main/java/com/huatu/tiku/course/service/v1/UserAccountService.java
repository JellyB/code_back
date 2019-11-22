package com.huatu.tiku.course.service.v1;

import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.UserAccountServiceV1;
import com.huatu.tiku.course.util.CourseCacheKey;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-06 3:41 PM
 **/
@Service
@Slf4j
public class UserAccountService {

    @Autowired
    private UserAccountServiceV1 userAccountService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据 userName 获取 userId
     * @param userName
     * @return
     * @throws BizException
     */
    public Long userId(String userName) throws BizException{
        String key = CourseCacheKey.getUserAccountInfoKey();
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        if(hashOperations.hasKey(key, userName)){
             return Long.valueOf(hashOperations.get(key, userName));
        }else{
            List<String> userNames = Lists.newArrayList();
            userNames.add(userName);
            NetSchoolResponse netSchoolResponse = userAccountService.getUIdByUsernameBatch(userNames);
            if(ResponseUtil.isFailure(netSchoolResponse)){
                return null;
            }
            List<LinkedHashMap<String,Object>> data = (List<LinkedHashMap<String,Object>>) netSchoolResponse.getData();
            LinkedHashMap<String,Object> userInfo = data.get(0);
            Long userId =  Long.valueOf(String.valueOf(userInfo.getOrDefault("userId", "0")));
            hashOperations.put(key, userName, userId.toString());
            return userId;
        }
    }
}
