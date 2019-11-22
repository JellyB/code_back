package com.huatu.tiku.course.web.controller.v3;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.huatu.tiku.course.netschool.api.v3.UserAccountServiceV3;
import com.huatu.tiku.course.service.VersionService;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/13 16:59
 */
@RestController
@RequestMapping("/v3/account")
@Slf4j
public class UserAccountControllerV3 {
    @Autowired
    private UserAccountServiceV3 userAccountServiceV3;

    @Autowired
    private VersionService versionService;

    /**
     * 我的代金券列表
     * @param type 1，可用 2，不可用
     * @param userSession
     * @return
     */
    @GetMapping("/coupons")
    public Object findUserCoupons(@RequestParam int type,
                                  @Token UserSession userSession) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("action",type);
        params.put("username",userSession.getUname());

        return ResponseUtil.build(userAccountServiceV3.findUserCouponList(RequestUtil.encrypt(params)),true);
    }


    /**
     * 我的积分余额
     * @param userSession
     * @return
     */
    @GetMapping("/point")
    public Object getUserPointLeft(@Token UserSession userSession) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("action",1);//积分余额默认
        params.put("username",userSession.getUname());

        return ResponseUtil.build(userAccountServiceV3.getUserPointLeft(RequestUtil.encrypt(params)),true);
    }

    /**
     * 我的积分记录
     * @param userSession
     * @return
     */
    @GetMapping("/point/records")
    public Object findUserPointRecords(@Token UserSession userSession,
                                       @RequestParam int page) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("action",2);//积分余额默认
        params.put("username",userSession.getUname());
        params.put("page",page);

        return ResponseUtil.build(userAccountServiceV3.findUserPointRecords(RequestUtil.encrypt(params)),true);
    }


    /**
     * 积分兑换代金券
     * @param userSession
     * @param integrat
     * @param voucherid
     * @return
     */
    @PostMapping("/point/exchangeCoupon")
    public Object exchangeCoupon(@Token UserSession userSession,
                                 @RequestParam String integrat,
                                 @RequestParam String voucherid) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("integrat",integrat);//积分余额默认
        params.put("username",userSession.getUname());
        params.put("voucherid",voucherid);

        return ResponseUtil.build(userAccountServiceV3.exchangeCoupon(RequestUtil.encrypt(params)),true);
    }


    /**
     * 账户充值记录，消费记录(不用单独做路由了)
     * @param type
     * @param page
     * @return
     */
    @GetMapping("/records")
    public Object findAccountRecords(
            @RequestParam int type,
            @RequestParam int page,
            @Token UserSession userSession,
            @RequestHeader(value = "terminal") Integer terminal,
            @RequestHeader(value = "cv") String cv
    ) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("action",type == 1 ? 2:3);//
        params.put("username",userSession.getUname());
        params.put("cv",cv);
        params.put("page",page);

        Object object = ResponseUtil.build(userAccountServiceV3.findAccountRecortds(RequestUtil.encrypt(params)),true);
        if(versionService.isIosAudit(terminal, cv) && type == 2){
            try{
                if(!(object instanceof JSONObject)){
                    return object;
                }
                JSONObject data = (JSONObject) object;
                if(!data.containsKey("consumeRes")){
                    return object;
                }
                JSONArray consumes = (JSONArray) data.get("consumeRes");
                for(int i = 0; i < consumes.size(); i ++){
                    JSONObject current = (JSONObject)consumes.get(i);
                    if(!current.containsKey("MoneyReceipt")){
                       continue;
                    }
                    String moneyReceipt = current.getString("MoneyReceipt");
                    if(moneyReceipt.indexOf(" ") > 0){
                        moneyReceipt = moneyReceipt.split(" ")[1];
                        current.put("MoneyReceipt", moneyReceipt);
                    }
                }
                return object;
            }catch (Exception e){
                log.error("versionService.isIosAudit 异常");
                return object;
            }
        }
        return object;
    }


    /**
     * 获取用户账户余额
     * @param userSession
     * @return
     */
    @GetMapping("/balance")
    public Object getBalance(
            @Token(check = false) UserSession userSession,
            @RequestHeader("cv") String terminal

    ) {
    	if(userSession == null) {
    		Map ret = Maps.newHashMap();
    		Map userMoneyMap = Maps.newHashMap();
    		userMoneyMap.put("UserMoney", 0);
    		ret.put("userCountres", userMoneyMap);
    		return ret;
    	}
        Map<String,Object> params = Maps.newHashMap();
        params.put("action",1);//
        params.put("username",userSession.getUname());
        params.put("cv",terminal);
        return ResponseUtil.build(userAccountServiceV3.getAccountBalance(RequestUtil.encrypt(params)),true);
    }


    /**
     * 学习币充值
     */
    @PostMapping("/charge")
    public Object charge(@Token UserSession userSession,
                         @RequestParam String Amount,
                         @RequestParam String payType) {
        Map<String,Object> params = Maps.newHashMap();
        //params.put("action","recharge");
        params.put("username",userSession.getUname());
        params.put("Amount",Amount);
        params.put("payType",payType);
        return ResponseUtil.build(userAccountServiceV3.chargeAccount(params),true);
    }

}
