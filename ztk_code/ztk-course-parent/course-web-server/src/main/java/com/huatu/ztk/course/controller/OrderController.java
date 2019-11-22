package com.huatu.ztk.course.controller;

import com.google.common.collect.Maps;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.course.common.NetSchoolConfig;
import com.huatu.ztk.course.common.NetSchoolSydwUrl;
import com.huatu.ztk.course.common.NetSchoolUrl;
import com.huatu.ztk.course.service.CourseService;
import com.huatu.ztk.course.service.OrderService;
import com.huatu.ztk.user.common.CourseSourceType;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 订单
 * Created by linkang on 11/30/16.
 */

@RestController
@RequestMapping(value = "v1/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);


    @Autowired
    private OrderService orderService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private CourseService courseService;

    /**
     * 生成订单接口
     *
     * @param token
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
    public Object order(@RequestHeader(required = false) String token,
                        @RequestParam(required = false) String phone,
                        @RequestParam(required = false) String province,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String address,
                        @RequestParam(required = false) String consignee,
                        @RequestParam int courseId,
                        @RequestParam int paymentType,
                        @RequestHeader int terminal) throws Exception {
        userSessionService.assertSession(token);
        String uname = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", uname);
        parameterMap.put("phone", phone);
        parameterMap.put("province", province);
        parameterMap.put("city", city);
        parameterMap.put("address", address);
        parameterMap.put("consignee", consignee);
        parameterMap.put("rid", courseId);
        parameterMap.put("paymentType", paymentType);
        //事业单位需要的参数
        parameterMap.put("type", catgory == CatgoryType.GONG_WU_YUAN ? null : "sydw");

        if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            //通过username与网校关联，网校接口的userid不起作用
            parameterMap.put("userid", -1);

            return orderService.createOrder(parameterMap, getCreateIosOrderUrl(catgory), true);
        } else if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            return orderService.createOrder(parameterMap, getCreateAndroidOrderUrl(catgory), true);
        }

        return null;
    }

    private String getCreateIosOrderUrl(int catgory) {
        return catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolUrl.CREATE_ORDER_IOS : NetSchoolSydwUrl.SYDW_CREATE_ORDER_IOS;
    }

    private String getCreateAndroidOrderUrl(int catgory) {
        return catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolUrl.CREATE_ORDER_ANDROID : NetSchoolSydwUrl.SYDW_CREATE_ORDER_ANDROID;
    }

    /**
     * 添加免费课程
     * @param terminal 终端类型
     * @param courseId 课程id
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "free", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public Object freeCourse(@RequestHeader int terminal,
                             @RequestParam int courseId,
                             @RequestHeader(required = false) String token) throws Exception {
        userSessionService.assertSession(token);
        String uname = userSessionService.getUname(token);
        int catgory = userSessionService.getCatgory(token);

        final HashMap<String, Object> parameterMap = Maps.newHashMap();
        parameterMap.put("username", uname);
        parameterMap.put("source", getSource(terminal));

        //通过username与网校关联，网校接口的userid不起作用
        parameterMap.put("userid", -1);
        parameterMap.put("rid", courseId);

        return orderService.createOrder(parameterMap, getFreeOrderUrl(catgory),false);
    }

    private String getFreeOrderUrl(int catgory) {
        return catgory == CatgoryType.GONG_WU_YUAN ? NetSchoolUrl.FREE_COURSE : NetSchoolSydwUrl.SYDW_FREE_COURSE;
    }


    private int getSource(int terminal) {
        int source = CourseSourceType.IOS;
        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            source = CourseSourceType.ANDROID;
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD){
            source = CourseSourceType.IOS;
        }
        return source;
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
                         @RequestHeader(required = false) String token) throws Exception {
        userSessionService.assertSession(token);
        int catgory = userSessionService.getCatgory(token);

        if (courseService.isIosAudit(catgory, terminal, cv)) {
            final HashMap<String, Object> postMap = Maps.newHashMap();
            postMap.put("receipt-data", receiptData);
            orderService.receipt(postMap);
        }


        final TreeMap<String, Object> parameterMap = Maps.newTreeMap();
        parameterMap.put("out_trade_no", tradeNo);
        parameterMap.put("total_fee", totalFee);

        List<String> paramList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            paramList.add(entry.getKey() + "=" + entry.getValue());
        }

        String netschoolToken = NetSchoolConfig.IOS_PAY_VERIFY_SECURITY_CODE + StringUtils.join(paramList, "&");

        logger.info("param list={},token={}", paramList, netschoolToken);

        String sign = DigestUtils.md5Hex(netschoolToken);

        //安全校验码key
        parameterMap.put("sign", sign);


        return orderService.iosPayVerify(parameterMap);
    }
}
