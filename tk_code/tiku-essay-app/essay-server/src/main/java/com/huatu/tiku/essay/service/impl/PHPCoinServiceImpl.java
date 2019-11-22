package com.huatu.tiku.essay.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.tiku.essay.service.PHPCoinService;
import com.huatu.tiku.essay.vo.resp.PHPRespWrapperVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Service
public class PHPCoinServiceImpl implements PHPCoinService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${goods_order.refund.url}")
    private String goodsOrderRefundUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Retryable
    @Override
    public void refund(Long orderId, Long orderDetailId, Integer money) {
        String responseContent = restTemplate.exchange(goodsOrderRefundUrl + "?orderNum=" + orderId + "&orderId=" + orderDetailId + "&gold=" + money, HttpMethod.PUT, null, String.class).getBody();

        log.info("PHPCoinService.refund `s response : {}", responseContent);

        try {
            PHPRespWrapperVO phpRespWrapperVO = objectMapper.readValue(responseContent, new TypeReference<PHPRespWrapperVO<?>>() {
            });

            Assert.isTrue(phpRespWrapperVO.getCode().equals(10000) ||
                    phpRespWrapperVO.getCode().equals(-10218), "退款失败");
        } catch (IOException e) {
            log.error("调用PHP金币退款失败", e);

            throw new RuntimeException(e);
        }
    }
}
