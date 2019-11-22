package com.huatu.mq;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by ismyway on 16/5/11.
 */
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
