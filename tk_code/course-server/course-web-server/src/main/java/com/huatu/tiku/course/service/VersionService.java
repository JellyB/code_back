package com.huatu.tiku.course.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.huatu.common.consts.TerminalType;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.VersionRedisKey;
import com.huatu.tiku.course.netschool.api.UserAccountServiceV1;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 * 后期把这些移到服务里，这里只用到一个查询，引入了完整的redis环境
 * @author hanchao
 * @date 2017/8/29 19:51
 */
@Component
@Deprecated
@Slf4j
public class VersionService {
    @Resource(name = "stringRedisTemplate")
    private SetOperations<String,String> setOperations;

    @Autowired
    private UserAccountServiceV1 userAccountServiceV1;

    private static final LoadingCache<String, Integer> cache = CacheBuilder.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Integer>() {
                @Override
                public Integer load(String key) throws Exception {
                    return -1;
                }
            });

    /**
     * 是否审核版本
     * @param catgory 考试类型
     * @return
     */
    @Deprecated
    public boolean isIosAudit(int catgory,int terminal,String cv) {
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            String iosAuditSetKey = VersionRedisKey.getIosAuditSetKey(catgory);
            return setOperations.isMember(iosAuditSetKey, cv);
        }
        return false;
    }

    public boolean isIosAudit(int terminal,String cv){
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            Integer member;
            try{
                member = cache.get(cv, new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        log.info("cache missed, call from remote");
                        NetSchoolResponse netSchoolResponse = userAccountServiceV1.isIosAudit(cv);
                        if(ResponseUtil.isFailure(netSchoolResponse)){
                            return -1;
                        }
                        Object value = netSchoolResponse.getData();
                        return ((Boolean)value) ? 1 : 0;
                    }
                });
                return member > 0;
            }catch (Exception e){
                log.error("VersionService isIosAudit caught an exception, :{}", e);
                return false;
            }
        }
        return false;
    }
}
