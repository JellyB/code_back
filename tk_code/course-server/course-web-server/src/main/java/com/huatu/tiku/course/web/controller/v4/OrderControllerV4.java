package com.huatu.tiku.course.web.controller.v4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.common.spring.web.MediaType;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.course.bean.NetSchoolResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-30 1:59 PM
 **/

@Slf4j
@RestController
@RequestMapping("/orders")
@ApiVersion("/v4")
public class OrderControllerV4 {


    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${seckill.pay.url}")
    private String seckillPayUrl;

    @Autowired
    private ObjectMapper objectMapper;

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
                           @Token(check = false) UserSession userSession) throws IOException {
        Map<String, Object> params = HashMapBuilder.<String, Object>newBuilder()
                .put("action", "pay")
                .put("ordernum", ordernum)
                .put("payment", payment)
                .put("payTag", 1)
                .build();
        String p = RequestUtil.encrypt(params);
        okhttp3.RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_FORM_URLENCODED_UTF8_VALUE), "p=" + p);
        Request request = new Request.Builder().url(seckillPayUrl).post(requestBody).build();
        Response responseBody = okHttpClient.newCall(request).execute();
        NetSchoolResponse response = objectMapper.readValue(responseBody.body().string(), NetSchoolResponse.class);
        return ResponseUtil.build(response, true);
    }
}
