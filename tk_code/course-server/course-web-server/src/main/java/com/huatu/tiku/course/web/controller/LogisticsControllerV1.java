package com.huatu.tiku.course.web.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.consts.CatgoryType;
import com.huatu.tiku.course.bean.ExpressListResponse;
import com.huatu.tiku.course.bean.ExpressResult;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.ExpressStatus;
import com.huatu.tiku.course.common.NetSchoolConfig;
import com.huatu.tiku.course.netschool.api.LogisticsServiceV1;
import com.huatu.tiku.course.netschool.api.OtherServiceV1;
import com.huatu.tiku.course.util.Crypt3Des;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @author hanchao
 * @date 2017/8/30 14:47
 */
@RestController
@RequestMapping(value = "v1/logistics")
public class LogisticsControllerV1 {

    @Autowired
    private LogisticsServiceV1 logisticsService;

    @Autowired
    private OtherServiceV1 otherService;

    /**
     * 物流列表
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public Object logistics(@Token UserSession userSession) throws Exception {
        String username = userSession.getUname();
        int catgory = userSession.getCategory();

        HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", username);
        params.put("categoryid", catgory == CatgoryType.GONG_WU_YUAN ?
                NetSchoolConfig.CATEGORY_GWY : NetSchoolConfig.CATEGORY_SHIYE);

        NetSchoolResponse response = logisticsService.queryList(params);
        if (response.getCode() >= NetSchoolConfig.SUCCESS_CODE) {
            ExpressListResponse exResponse = JSON.parseObject(JSON.toJSONString(response),ExpressListResponse.class);
            exResponse.getData().stream().forEach(item->item.setExpressNo(Crypt3Des.decryptMode(item.getExpressNo())));
            return exResponse.getData();
        }else{
            throw new BizException(ErrorResult.create(response.getCode(), response.getMsg()));
        }
    }


    /**
     * 物流详情
     * @param id
     * @param company
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "{id}",produces = MediaType.TEXT_HTML_VALUE+ ";charset=UTF-8")
    public Object detail(@PathVariable String id, @RequestParam String company, Model model) throws Exception{

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("com", company);
        parameterMap.put("num", id);
        ExpressResult result = otherService.detail(RequestUtil.encryptParams(parameterMap));

        if (result.getCode() >= NetSchoolConfig.SUCCESS_CODE) {
            //运单号
            model.addAttribute("num", id);
            //运单状态描述
            int statusCode = result.getData().getStatus();
            model.addAttribute("statusDescription", ExpressStatus.getByCode(statusCode).getDes());
            model.addAttribute("result", result);
            model.addAttribute("noMsgFlag", false);
        } else {
            model.addAttribute("noMsgFlag", true);
        }

        return "logistics_detail";
    }
}
