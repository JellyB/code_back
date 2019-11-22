package com.huatu.tiku.course.web.controller.v3;

import com.google.common.collect.Maps;
import com.huatu.tiku.course.netschool.api.v3.SaleServiceV3;
import com.huatu.tiku.course.service.VersionService;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.course.web.controller.SaleControllerV1;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/27 18:13
 */
@RestController
@RequestMapping(value = "/v3/sales")
public class SaleControllerV3 extends SaleControllerV1{
    @Autowired
    private SaleServiceV3 saleServiceV3;
    @Autowired
    private VersionService versionService;

    /**
     * 支付详情接口
     * @param courseId 课程id
     * @return
     * @throws Exception
     */
    @GetMapping(value = "detail")
    public Object getSaleDetail(@RequestParam int courseId,
                             @Token UserSession userSession,
                             @RequestHeader String cv,
                             @RequestHeader int terminal) throws Exception {
        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", userSession.getUname());
        parameterMap.put("rid", courseId);
        Object response = ResponseUtil.build(saleServiceV3.getDetail(RequestUtil.encryptJsonParams(parameterMap)), true);
        if(versionService.isIosAudit(userSession.getCategory(), terminal, cv)){
            if(response instanceof Map){
                ((Map)response).put("inPurchaseProductId", productIdMap.get(courseId));
            }
        }
        return response;
    }
}
