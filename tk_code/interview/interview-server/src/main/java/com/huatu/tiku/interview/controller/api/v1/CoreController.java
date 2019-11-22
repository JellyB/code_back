package com.huatu.tiku.interview.controller.api.v1;

import com.google.common.collect.Maps;
import com.huatu.tiku.interview.constant.BasicParameters;
import com.huatu.tiku.interview.service.CoreService;
import com.huatu.tiku.interview.util.LogPrint;
import com.huatu.tiku.interview.util.MessageUtil;
import com.huatu.tiku.interview.util.SignUtil;
import com.huatu.tiku.interview.util.WeiXinJsSdk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 *  核心业务类
 * @author zhouwei
 * @Description:
 * @create 2018-01-04 上午11:53
 **/
@RestController
@Slf4j

public class CoreController {
    @Autowired
    private CoreService coreService;

    @LogPrint
    @PostMapping("process")
    public String post(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 调用核心业务类接收消息、处理消息跟推送消息
        log.info("--------------core-------------------");
        return coreService.processRequest(MessageUtil.parseXml(request), request, response);
    }

    @LogPrint
    @GetMapping(value = "process")
    public void checkSignature(@RequestParam(name = "signature", required = false) String signature,
                               @RequestParam(name = "nonce", required = false) String nonce,
                               @RequestParam(name = "timestamp", required = false) String timestamp,
                               @RequestParam(name = "echostr", required = false) Object echostr, HttpServletResponse resp) {
        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        log.info("-------开始验证----------signature:{},nonce:{},timestamp:{},echostr:{}", signature, nonce, timestamp, echostr);
        try {

            PrintWriter out = resp.getWriter();
            if (SignUtil.checkSignature(signature, timestamp, nonce)) {
                log.info("接入成功");
                //  return echostr;
                out.print(echostr);
                return;
            }
            log.error("接入失败");
            out.print(echostr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取前端wx.conf的各个参数
     * @return
     */
    @LogPrint
    @GetMapping(value = "info")
    public Object getWeiXinInfo(){
//        appId: 'wx4e6e2dc624081b58', // 必填，公众号的唯一标识
//                nonceStr: '701886111', // 必填，生成签名的随机串
//                signature: 'b77cc486e2f705a2415ced25a70c41381d4d33da',// 必填，签名
        Map<String,String> map = Maps.newHashMap();
        map.put("appId","wx4e6e2dc624081b58");
        map.put("nonceStr","701886111");
        map.put("signature","b77cc486e2f705a2415ced25a70c41381d4d33da");
        return map;
    }

    /**
     * 获取签名信息
     * @param url
     * @return
     */
    @LogPrint
    @GetMapping(value = "signInfo")
    public Object getSignPackage(@RequestParam String url){
        WeiXinJsSdk jsSdk = new WeiXinJsSdk(BasicParameters.appID,BasicParameters.appsecret,url);
        return jsSdk.getSignPackage();
    }
}
