package com.huatu.ztk.course.service;

import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.course.common.CourseClient;
import com.huatu.ztk.user.common.VersionRedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by linkang on 11/30/16.
 */

@Service
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private CourseClient courseClient;

    @Resource(name = "redisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 一般的直接组装参数的请求
     *
     * @param parameterMap
     * @param basicUrl
     * @param needDecrypt
     * @return
     * @throws Exception
     */
    public Object getJson(Map<String, Object> parameterMap, String basicUrl,boolean needDecrypt) throws Exception {
        return courseClient.getJson(basicUrl,parameterMap, needDecrypt);
    }

    /**
     * 参数加密的请求
     *
     * @param parameterMap
     * @param basicUrl
     * @param needDecrypt
     * @return
     * @throws Exception
     */
    public Object getJsonByEncryptParams(Map<String, Object> parameterMap, String basicUrl,boolean needDecrypt) throws Exception {
        return courseClient.getJsonByEncryptParams(parameterMap, basicUrl, needDecrypt);
    }

    /**
     * json参数加密的请求
     *
     * @param parameterMap
     * @param basicUrl
     * @param needDecrypt
     * @return
     * @throws Exception
     */
    public Object getJsonByEncryptJsonParams(Map<String, Object> parameterMap, String basicUrl, boolean needDecrypt) throws Exception {
        return courseClient.getJsonByEncryptJsonParams(parameterMap, basicUrl, needDecrypt);
    }


    /**
     * 是否审核版本
     * @param catgory 考试类型
     * @return
     */
    public boolean isIosAudit(int catgory,int terminal,String cv) {
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            String iosAuditSetKey = VersionRedisKey.getIosAuditSetKey(catgory);
            return redisTemplate.opsForSet().isMember(iosAuditSetKey, cv);
        }
        return false;
    }

    public void setCourseClient(CourseClient courseClient) {
        this.courseClient = courseClient;
    }
}
