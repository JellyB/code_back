package com.huatu.tiku.course.service;

import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v3.PromoteCoreServiceV3;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 针对特殊下单使用的
 * @author hanchao
 * @date 2017/10/18 15:22
 */
public class OrderBizService {
    @Autowired
    private PromoteCoreServiceV3 promoteCoreServiceV3;

    public NetSchoolResponse createOrder(String p){
        return null;
    }
}
