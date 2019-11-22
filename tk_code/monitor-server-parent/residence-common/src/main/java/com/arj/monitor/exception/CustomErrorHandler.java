package com.arj.monitor.exception;


import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * @author zhouwei
 * @date 2017/8/24 10:05
 */
public class CustomErrorHandler extends DefaultResponseErrorHandler {
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        // todo
    }}
