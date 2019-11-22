package com.huatu.morphling.service.remote.endpoint;

import com.alibaba.fastjson.JSON;
import com.huatu.morphling.common.consts.EndpointConsts;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/11/9 10:40
 */
@Service
public class HealthEndpointService extends AbstractEndpointService<Map>{
    public static final String UP = "UP";

    @Override
    public String endpoint() {
        return EndpointConsts.HEALTH;
    }

    @Override
    public Map request(String host, int port, String contextPath) {
        try {
            return super.request(host, port, contextPath);
        } catch(HttpServerErrorException e){
            String response = e.getResponseBodyAsString();
            return JSON.parseObject(response);
        }
    }
}
