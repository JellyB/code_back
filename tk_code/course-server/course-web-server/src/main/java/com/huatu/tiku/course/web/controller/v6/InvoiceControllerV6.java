package com.huatu.tiku.course.web.controller.v6;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.InvoiceInterfaceV6;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParam;
import com.huatu.tiku.course.spring.conf.aspect.mapParam.LocalMapParamHandler;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-05-17 10:12 AM
 **/
@RequestMapping(value = "invoice")
@RestController
@ApiVersion(value = "v6")
public class InvoiceControllerV6 {


    @Autowired
    private InvoiceInterfaceV6 invoiceInterface;


    /**
     * 开具发票接口
     * @param userSession
     * @param terminal
     * @param cv
     * @param invoiceContent
     * @param invoiceMoney
     * @param invoiceTitle
     * @param invoiceType
     * @param orderId
     * @param orderNum
     * @param taxNum
     * @param titleType
     * @return
     */
    @LocalMapParam
    @PostMapping(value = "write")
    public Object writeInvoice(@Token UserSession userSession,
                               @RequestHeader(value = "terminal") int terminal,
                               @RequestHeader(value = "cv") String cv,
                               @RequestParam(value = "invoiceContent") String invoiceContent,
                               @RequestParam(value = "invoiceMoney") String invoiceMoney,
                               @RequestParam(value = "invoiceTitle") String invoiceTitle,
                               @RequestParam(value = "invoiceType") String invoiceType,
                               @RequestParam(value = "orderId") Long orderId,
                               @RequestParam(value = "orderNum") String orderNum,
                               @RequestParam(value = "taxNum") String taxNum,
                               @RequestParam(value = "titleType") Long titleType){

        HashMap<String,Object> params =  LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = invoiceInterface.writeInvoice(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 开票详情
     * @param userSession
     * @param terminal
     * @param cv
     * @param orderId
     * @return
     */
    @LocalMapParam
    @PostMapping(value = "detail")
    public Object invoiceDetail(@Token UserSession userSession,
                               @RequestHeader(value = "terminal") int terminal,
                               @RequestHeader(value = "cv") String cv,
                               @RequestParam(value = "orderId") Long orderId){

        HashMap<String,Object> params =  LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = invoiceInterface.InvoiceDetail(params);
        return ResponseUtil.build(netSchoolResponse);
    }


    /**
     * 发送到邮箱
     * @param userSession
     * @param terminal
     * @param cv
     * @param orderId
     * @param email
     * @return
     */
    @LocalMapParam
    @PostMapping(value = "email")
    public Object send2Email(@Token UserSession userSession,
                                @RequestHeader(value = "terminal") int terminal,
                                @RequestHeader(value = "cv") String cv,
                                @RequestParam(value = "orderId") Long orderId,
                                @RequestParam(value = "email") String email){

        HashMap<String,Object> params =  LocalMapParamHandler.get();
        NetSchoolResponse netSchoolResponse = invoiceInterface.SendInvoice2Email(params);
        return ResponseUtil.build(netSchoolResponse);
    }
}
