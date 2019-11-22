package com.huatu.tiku.course.netschool.api.v5;

import com.google.common.collect.Maps;
import com.huatu.common.Result;
import com.huatu.common.exception.BizException;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 描述：红包降级接口
 *
 * @author biguodong
 * Create time 2019-03-18 1:24 PM
 **/
@Slf4j
@Component
public class RedPackageDegradeServiceV5 {

    @Autowired
    private RedPackageServiceV5 redPackageServiceV5;

    /**
     * 判断用户是否有发起红包或领取红包
     * @param params
     * @return
     */
    @Degrade(key = "checkRedEnvV5", name="判断用户红包是否可领")
    public NetSchoolResponse checkRedEnv(Map<String, Object> params) throws BizException{
        return redPackageServiceV5.checkRedEnv(params);
    }

    /**
     * 判断用户是否有发起红包或领取红包,降级实现
     * @param params
     * @return
     * @throws BizException
     */
    public NetSchoolResponse checkRedEnvDegrade(Map<String, Object> params) throws BizException{
        Map<String,Object> result = Maps.newHashMap();
        result.put("hasRedEnvelope", 0);
        return NetSchoolResponse.builder().code(Result.SUCCESS_CODE).msg("").data(result).build();
    }

    /**
     * 判断红包是否显示
     * @return
     * @throws BizException
     */
    @Degrade(key = "showRedEvnV5", name="红包是否显示")
    public NetSchoolResponse showRedEvn()throws BizException{
        return redPackageServiceV5.showRedEvn();
    }


    /**
     * 判断红包是否显示,降级实现
     * @return
     * @throws BizException
     */
    public NetSchoolResponse showRedEvnDegrade() throws BizException{
        Map<String,Object> result = Maps.newHashMap();
        result.put("showRedEnvelope", 0);
        return NetSchoolResponse.builder().code(Result.SUCCESS_CODE).msg("").data(result).build();
    }
}
