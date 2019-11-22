package com.huatu.tiku.course.web.controller;

import com.google.common.collect.Maps;
import com.huatu.tiku.common.consts.CatgoryType;
import com.huatu.tiku.course.netschool.api.SaleServiceV1;
import com.huatu.tiku.course.service.VersionService;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付
 * Created by linkang on 11/30/16.
 */
@RestController
@RequestMapping(value = "v1/sales")
public class SaleControllerV1 {

    protected static final Map productIdMap = new HashMap();
    static {
        productIdMap.put(57713, "com.huatu.BrickQuestions.product57713");
        productIdMap.put(57461, "com.huatu.BrickQuestions.product57461");
        productIdMap.put(57834, "com.huatu.BrickQuestions.product57834");
        productIdMap.put(57835, "com.huatu.BrickQuestions.product57835");
        productIdMap.put(57544, "com.huatu.BrickQuestions.product57544");
    }

    @Autowired
    private SaleServiceV1 saleService;

    @Autowired
    private VersionService versionService;


    /**
     * 支付详情接口
     * @param courseId 课程id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "detail", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object saleDetail(@RequestParam int courseId,
                             @Token UserSession userSession,
                             @RequestHeader String cv,
                             @RequestHeader int terminal) throws Exception {
        String uname = userSession.getUname();
        int catgory = userSession.getCategory();

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", uname);
        parameterMap.put("rid", courseId);
        Object response = null;
        if(catgory == CatgoryType.GONG_WU_YUAN){
            response = ResponseUtil.build(saleService.queryDetail(RequestUtil.encryptJsonParams(parameterMap)),true);
        }else{
            response = ResponseUtil.build(saleService.querySydwDetail(RequestUtil.encryptJsonParams(parameterMap)),true);
        }
        if(versionService.isIosAudit(catgory, terminal, cv)){
            if(response instanceof Map){
                ((Map)response).put("inPurchaseProductId", productIdMap.get(courseId));
            }
        }
        return response;
    }

}
