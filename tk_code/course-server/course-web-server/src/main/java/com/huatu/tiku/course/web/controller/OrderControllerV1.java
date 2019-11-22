package com.huatu.tiku.course.web.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.consts.TerminalType;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.consts.CatgoryType;
import com.huatu.tiku.course.consts.NetschoolTerminalType;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.common.NetSchoolConfig;
import com.huatu.tiku.course.netschool.api.OrderServiceV1;
import com.huatu.tiku.course.netschool.api.OtherServiceV1;
import com.huatu.tiku.course.service.VersionService;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author hanchao
 * @date 2017/8/30 15:35
 */
@RestController
@RequestMapping(value = "v1/orders")
@Slf4j
public class OrderControllerV1 {

    private static final String RECEIPT_URL = "https://sandbox.itunes.apple.com/verifyReceipt";

    @Autowired
    private OrderServiceV1 orderService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private OtherServiceV1 otherService;

    /**
     * 生成订单接口
     *
     * @param phone       收货人手机号
     * @param province    收货省份
     * @param city        收货城市
     * @param address     收货详细地址
     * @param consignee   收货人
     * @param courseId    课程id
     * @param paymentType 支付方式
     * @param terminal    终端类型
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public Object order(@Token UserSession userSession,
                        @RequestParam(required = false) String phone,
                        @RequestParam(required = false) String province,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String address,
                        @RequestParam(required = false) String consignee,
                        @RequestParam int courseId,
                        @RequestParam int paymentType,
                        @RequestHeader int terminal) throws Exception {
        String uname = userSession.getUname();
        int catgory = userSession.getCategory();

        final HashMap<String, Object> params = Maps.newHashMap();
        params.put("username", uname);
        params.put("phone", phone);
        params.put("province", province);
        params.put("city", city);
        params.put("address", address);
        params.put("consignee", consignee);
        params.put("rid", courseId);
        params.put("paymentType", paymentType);
        //事业单位需要的参数
        params.put("type", catgory == CatgoryType.GONG_WU_YUAN ? null : "sydw");

        NetSchoolResponse response = null;
        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            //通过username与网校关联，网校接口的userid不起作用
            params.put("userid", -1);
            if(catgory == CatgoryType.GONG_WU_YUAN){
                response = orderService.createOrderIos(RequestUtil.encryptJsonParams(params));
            }else{
                response = orderService.createSydwOrderIos(RequestUtil.encryptJsonParams(params));
            }
        } else if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            if(catgory == CatgoryType.GONG_WU_YUAN){
                response = orderService.createOrderAnroid(RequestUtil.encryptJsonParams(params));
            }else{
                response = orderService.createSydwOrderAnroid(RequestUtil.encryptJsonParams(params));
            }
        }

        return ResponseUtil.build(response,true);
    }


    /**
     * 添加免费课程
     * @param terminal 终端类型
     * @param courseId 课程id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "free", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public Object freeCourse(@RequestHeader int terminal,
                             @RequestParam int courseId,
                             @Token UserSession userSession) throws Exception {
        String uname = userSession.getUname();
        int catgory = userSession.getCategory();

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", uname);
        parameterMap.put("source", NetschoolTerminalType.transform(terminal));

        //通过username与网校关联，网校接口的userid不起作用
        parameterMap.put("userid", -1);
        parameterMap.put("rid", courseId);

        if(catgory == CatgoryType.GONG_WU_YUAN){
            return ResponseUtil.build(orderService.getFree(RequestUtil.encryptJsonParams(parameterMap)));
        }else{
            return ResponseUtil.build(orderService.getFreeSydw(RequestUtil.encryptJsonParams(parameterMap)));
        }
    }



    /**
     * 回调通知接口
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "iosPayVerify", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public Object iosPay(@RequestParam String tradeNo,
                         @RequestParam double totalFee,
                         @RequestParam String receiptData,
                         @RequestHeader String cv,
                         @RequestHeader int terminal,
                         @Token UserSession userSession) throws Exception {
        int catgory = userSession.getCategory();

        if (versionService.isIosAudit(catgory, terminal, cv)) {
            final HashMap<String, Object> postMap = Maps.newHashMap();
            postMap.put("receipt-data", receiptData);
            receipt(postMap);
        }


        final TreeMap<String, Object> parameterMap = Maps.newTreeMap();
        parameterMap.put("out_trade_no", tradeNo);
        parameterMap.put("total_fee", totalFee);

        List<String> paramList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            paramList.add(entry.getKey() + "=" + entry.getValue());
        }

        String netschoolToken = NetSchoolConfig.IOS_PAY_VERIFY_SECURITY_CODE + StringUtils.join(paramList, "&");

        log.info("param list={},token={}", paramList, netschoolToken);

        String sign = DigestUtils.md5Hex(netschoolToken);

        //安全校验码key
        parameterMap.put("sign", sign);

        return iosPayVerify(parameterMap);
    }

    /**
     * 内购回调
     * @param parameterMap
     * @return
     * @throws Exception
     */
    private Object iosPayVerify(Map<String, Object> parameterMap) throws Exception{
        log.info("parameterMap={}",JSON.toJSONString(parameterMap));
        String result = otherService.payOrder(parameterMap);

        if (result.equals("success")) {
            return SuccessMessage.create("订单状态更新成功");
        } else if (result.startsWith("fail")) {
            log.error("iosPayVerify fail,result=" + result);
            throw new BizException(ErrorResult.create(-1, "更新订单状态失败"));
        }
        return null;
    }


    /**
     * 苹果内购验证
     * @param parameterMap
     * @return
     * @throws Exception
     */
    private void receipt(Map<String, Object> parameterMap) throws Exception{
        if(log.isInfoEnabled()){
            log.info("parameterMap={}", JSON.toJSONString(parameterMap));
        }

        RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON_UTF8_VALUE),JSON.toJSONString(parameterMap));

        Request request = new Request.Builder().url(RECEIPT_URL).post(requestBody).build();

        Response response = okHttpClient.newCall(request).execute();

        String data = response.body().string();

        log.info("ios receipt resp: {}",data);

        if (StringUtils.isNoneBlank(data)) {
            Map map = JSON.parseObject(data);
            Object status = map.get("status");
            int statusCode = Integer.valueOf(status.toString());
            if (statusCode != 0) {
                throw new BizException(ErrorResult.create(statusCode, "验证失败"));
            }
        }
    }
}
