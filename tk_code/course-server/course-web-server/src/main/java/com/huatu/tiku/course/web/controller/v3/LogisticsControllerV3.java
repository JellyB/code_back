package com.huatu.tiku.course.web.controller.v3;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.bean.ExpressListResponse;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.NetSchoolConfig;
import com.huatu.tiku.course.netschool.api.v3.LogisticsServiceV3;
import com.huatu.tiku.course.util.Crypt3Des;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.basic.subject.SubjectEnum;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/20 11:25
 */
@RestController
@RequestMapping(value = "/v3/logistics")
public class LogisticsControllerV3 {
    @Autowired
    private LogisticsServiceV3 logisticsServiceV3;

    /**
     * 物流详情
     * @param number
     * @return
     */
    @GetMapping("/{number}")
    public Object getLogisticsDetail(@PathVariable String number) {
        Map<String,Object> params = HashMapBuilder.<String,Object>newBuilder()
                .put("action","getLogistics")
                .put("number",number)
                .build();
        return ResponseUtil.build(logisticsServiceV3.getLogisticsDetail(RequestUtil.encrypt(params)));
    }


    /**
     * 物流列表
     *
     * @return
     * @throws Exception
     */
    @GetMapping
    public Object logistics(@Token UserSession userSession) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        params.put("categoryid", SubjectEnum.valueOf(catgory).categoryid());

        NetSchoolResponse response = logisticsServiceV3.queryList(params);
        if (response.getCode() >= NetSchoolConfig.SUCCESS_CODE) {
            ExpressListResponse exResponse = JSON.parseObject(JSON.toJSONString(response),ExpressListResponse.class);
            exResponse.getData().stream().forEach(item->item.setExpressNo(Crypt3Des.decryptMode(item.getExpressNo())));
            return exResponse.getData();
        }else{
            throw new BizException(ErrorResult.create(response.getCode(), response.getMsg()));
        }
    }
}
