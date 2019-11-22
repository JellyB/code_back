package com.huatu.tiku.course.netschool.api.v5;

import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * 描述：订单服务接口降级处理
 *
 * @author biguodong
 * Create time 2019-03-18 2:35 PM
 **/

@Service
@Slf4j
public class OrderDegradeService {

    @Autowired
    private OrderServiceV5 orderService;

    /**
     * 查询我的订单列表 - ZTK
     * @param params
     * @return
     */
    @Degrade(key="userOrderListV5", name = "我的订单v5")
    public NetSchoolResponse userOrderListZTK(HashMap<String, Object> params){
        return orderService.userOrderListZTK(params);
    }

    /**
     * 查询我的订单列表 - ZTK，降级
     * @param params
     * @return
     */
    public NetSchoolResponse userOrderListZTKDegrade(HashMap<String, Object> params){
        log.info("userOrderListZTKDegrade 降级处理:{}", params);
        return ResponseUtil.DEFAULT_PHP_PAGE_RESPONSE;
    }
}
