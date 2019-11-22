package com.huatu.tiku.course.web.controller.v3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.huatu.common.SuccessMessage;
import com.huatu.common.spring.web.MediaType;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.consts.NetschoolTerminalType;
import com.huatu.tiku.course.netschool.api.v3.OrderServiceV3;
import com.huatu.tiku.course.netschool.api.v3.PromoteCoreServiceV3;
import com.huatu.tiku.course.service.cache.OrderCacheQPS;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/14 16:31
 */
@Slf4j
@RestController
@RequestMapping("/v3/orders")
public class OrderControllerV3 {
    @Autowired
    private OrderServiceV3 orderServiceV3;
    @Autowired
    private PromoteCoreServiceV3 promoteCoreServiceV3;

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${seckill.pay.url}")
    private String seckillPayUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderCacheQPS orderCacheQPS;

    /**
     * 下单页面相关信息（结算信息，收货地址）
     *
     * @param rid
     * @param userSession
     * @return
     */
    @GetMapping("/previnfo")
    public Object getPrevInfo(
            @RequestParam int rid,
            @RequestHeader(required = false) int terminal,
            @RequestHeader(required = false) String cv,
            @RequestParam(required = false, defaultValue = "")String pageSource,
            @Token UserSession userSession) {
        //设置QPS
        orderCacheQPS.orderPreInfoQPS();
        Map<String, Object> params = Maps.newHashMap();
        params.put("rid", rid);
        params.put("action", "placeOrder");
        params.put("terminal",terminal);
        params.put("cv",cv);
        params.put("username", userSession.getUname());
        params.put("pageSource", pageSource);
        log.warn("5$${}$${}$${}$${}$${}$${}", rid, userSession.getId(), userSession.getUname(), String.valueOf(System.currentTimeMillis()), cv, terminal);
        //释放
        Object result;
        try {
            result = ResponseUtil.build(promoteCoreServiceV3.getPrevInfo(RequestUtil.encrypt(params)), true);
        } finally {
            orderCacheQPS.orderPreInfoQPSRelease();
        }
        return result;
    }

    /**
     * 添加免费课程
     *
     * @param terminal 终端类型
     * @param courseId 课程id
     * @return
     * @throws Exception
     */
    @PostMapping("/free")
    public Object freeCourse(@RequestHeader int terminal, @RequestHeader(required = false) String cv,
                             @RequestParam int courseId,
                             @Token UserSession userSession,
                             @RequestParam(required = false, defaultValue = "") String pageSource) throws Exception {
        String uname = userSession.getUname();
        int catgory = userSession.getCategory();
        final HashMap<String, Object> parameterMap = HashMapBuilder.newBuilder()
                .put("username", uname)
                .put("source", NetschoolTerminalType.transform(terminal))
                .put("terminal", terminal)
                .put("userid", -1)
                .put("rid", courseId)
                .put("pageSource", pageSource)
                .buildUnsafe();

        // return ResponseUtil.build(orderServiceV3.getFree(RequestUtil.encryptJsonParams(parameterMap)));
        orderServiceV3.getFree(RequestUtil.encryptJsonParams(parameterMap));
        log.warn("11$${}$${}$${}$${}$${}$${}", courseId, userSession.getId(), userSession.getUname(), String.valueOf(System.currentTimeMillis()), cv, terminal);

        return SuccessMessage.create("下单成功");

    }


    /**
     * 创建订单
     *
     * @param addressid
     * @param rid
     * @param fromuser
     * @param tjCode
     * @param userSession
     * @return
     */
    @PostMapping("/create")
    public Object createOrder(@RequestParam String addressid,
                              @RequestParam int rid,
                              @RequestParam(required = false, defaultValue = "0") String fromuser,
                              @RequestParam String tjCode,
                              @RequestParam(required = false) String FreeCardID,
                              @RequestHeader(required = false) int terminal,
                              @RequestHeader(required = false) String cv,
                              @Token UserSession userSession) {
        //QPS
        orderCacheQPS.orderCreateQPS();
        Map<String, Object> params = Maps.newHashMap();
        params.put("action", "createOrder");
        params.put("terminal",terminal);
        params.put("cv",cv);
        params.put("addressid", addressid);
        params.put("FreeCardID", FreeCardID);
        params.put("fromuser", fromuser);
        params.put("rid", rid);
        params.put("source", (terminal == 2 || terminal == 5) ? 'I' : 'A');//不是ios，就传android
        params.put("tjCode", tjCode);
        params.put("username", userSession.getUname());
        log.info(" createOrder param ={}",params);
        log.warn("6$${}$${}$${}$${}$${}$${}$${}$${}$${}$${}", addressid, rid, userSession.getId(), userSession.getUname(), String.valueOf(System.currentTimeMillis()), cv, terminal, fromuser, tjCode, FreeCardID);
        Object result = null;
        try {
            result = ResponseUtil.build(promoteCoreServiceV3.createOrder(RequestUtil.encrypt(params)), true);
        } finally {
            //释放
            orderCacheQPS.orderCreateQPSRelease();
        }
        return result;
    }

    /**
     * 订单详情
     *
     * @param orderNo
     * @return
     */
    @GetMapping("/{orderNo}")
    public Object getOrderDetail(@PathVariable String orderNo,
                                 @RequestParam String type) {
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("action", "orderDetail")
                .put("ordernum", orderNo)
                .put("type", type)
                .build();
        return ResponseUtil.build(promoteCoreServiceV3.getOrderDetail(RequestUtil.encrypt(params)), true);
    }


    /**
     * 取消订单
     *
     * @param orderNo
     * @return
     */
    @PostMapping("/{orderNo}/cancel")
    public Object cancelOrder(@PathVariable String orderNo, @RequestHeader(required = false) int terminal,
                              @RequestHeader(required = false) String cv,
                              @Token UserSession userSession) {
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("action", "cancel")
                .put("ordernum", orderNo)
                .build();
        log.warn("12$${}$${}$${}$${}$${}$${}", orderNo, userSession.getId(), userSession.getUname(), String.valueOf(System.currentTimeMillis()), cv, terminal);
        return ResponseUtil.build(orderServiceV3.cancelOrder(RequestUtil.encrypt(params)), true);
    }

    /**
     * 我的订单列表
     *
     * @param session
     * @param page
     * @param type
     * @return
     */
    @GetMapping("/my")
    public Object myOrders(@Token UserSession session,
                           @RequestParam int page,
                           @RequestParam int type) {
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("action", "myOrder")
                .put("page", page)
                .put("type", type)
                .put("username", session.getUname())
                .build();
        return ResponseUtil.build(orderServiceV3.myOrder(RequestUtil.encrypt(params)), true);
    }


    /**
     * 支付订单
     *
     * @return
     */
    @PostMapping("/{orderNo}/pay")
    public Object payOrder(@PathVariable String orderNo,
                           @RequestParam int payment,
                           @Token UserSession session, @RequestHeader(required = false) int terminal,
                           @RequestHeader(required = false) String cv) {
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("action", "pay")
                .put("ordernum", orderNo)
                .put("payment", payment)
                .put("username", session.getUname())
                .build();
        log.warn("7$${}$${}$${}$${}$${}$${}$${}", orderNo, payment, session.getId(), session.getUname(), String.valueOf(System.currentTimeMillis()), cv, terminal);
        return ResponseUtil.build(promoteCoreServiceV3.payOrder(RequestUtil.encrypt(params)), true);
    }


    /**
     * 秒杀的支付方式
     *
     * @param ordernum
     * @param userSession
     * @return
     * @throws IOException
     */
    @PostMapping("/seckill/payWay")
    public Object payWay(@RequestParam String ordernum,
                         @Token UserSession userSession) throws IOException {
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("action", "payWay")
                .put("ordernum", ordernum)
                .build();
        String p = RequestUtil.encrypt(params);
        okhttp3.RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_FORM_URLENCODED_UTF8_VALUE), "p=" + p);
        Request request = new Request.Builder().url(seckillPayUrl).post(requestBody).build();
        Response responseBody = okHttpClient.newCall(request).execute();
        NetSchoolResponse response = objectMapper.readValue(responseBody.body().string(), NetSchoolResponse.class);
        return ResponseUtil.build(response, true);
    }

    /**
     * 秒杀支付
     *
     * @param ordernum
     * @param payment
     * @param userSession
     * @return
     * @throws IOException
     */
    @PostMapping("/seckill/pay")
    public Object payOrder(@RequestParam String ordernum,
                           @RequestParam String payment,
                           @Token UserSession userSession) throws IOException {
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("action", "pay")
                .put("ordernum", ordernum)
                .put("payment", payment)
                .build();
        String p = RequestUtil.encrypt(params);
        okhttp3.RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_FORM_URLENCODED_UTF8_VALUE), "p=" + p);
        Request request = new Request.Builder().url(seckillPayUrl).post(requestBody).build();
        Response responseBody = okHttpClient.newCall(request).execute();
        NetSchoolResponse response = objectMapper.readValue(responseBody.body().string(), NetSchoolResponse.class);
        return ResponseUtil.build(response, true);
    }

}
