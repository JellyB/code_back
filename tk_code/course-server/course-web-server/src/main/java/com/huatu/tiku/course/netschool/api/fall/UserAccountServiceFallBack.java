package com.huatu.tiku.course.netschool.api.fall;

import com.google.common.collect.Maps;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.UserAccountServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-06 3:37 PM
 **/
@Component
@Slf4j
public class UserAccountServiceFallBack implements UserAccountServiceV1{

    /**
     * 根据userName换区 userId
     *
     * @param userNames
     * @return
     */
    @Override
    public NetSchoolResponse getUIdByUsernameBatch(List<String> userNames) {
        return NetSchoolResponse.newInstance(Maps.newHashMap());
    }

    /**
     * 判断 ios 版本是否为审核版本
     *
     * @param cv
     * @return
     */
    @Override
    public NetSchoolResponse isIosAudit(String cv) {
        return NetSchoolResponse.newInstance(false);
    }
}
