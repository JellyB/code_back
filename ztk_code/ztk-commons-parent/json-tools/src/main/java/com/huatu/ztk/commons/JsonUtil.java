package com.huatu.ztk.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import com.fasterxml.jackson.databind.type.MapType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shaojieyue on 10/18/15.
 */
public final class JsonUtil{
    private final static ObjectMapper mapper = newMapper();

    private JsonUtil(){
    }

    /**
     * 将Object序列化为json字符串
     */
    public static String toJson(Object object){
        try{
            return mapper.writeValueAsString(object);
        }catch(JsonProcessingException e){
            throw new JacksonSerializeException(e);
        }
    }

    /**
     * 将json字符串序列化为java Bean
     * @param json
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> T toObject(String json, Class<T> valueType){
        try{
            return mapper.readValue(json, valueType);
        }catch(IOException e){
            throw new JacksonDeserializeException(e);
        }
    }

    /**
     * 将json转为泛型List
     * @param json
     * @param valueType
     * @param <T>
     * @return
     */
    public static final <T> List<T> toList(String json, Class<T> valueType){
        JavaType type = mapper.getTypeFactory().
            constructCollectionType(List.class, valueType);
        List<T> list = null;

        try{
            list = mapper.readValue(json, type);
        }catch(IOException e){
            throw new JacksonDeserializeException(e);
        }
        return list;
    }

    /**
     * 将json转换为泛型map
     * @param json json字符串
     * @param keyType map泛型key
     * @param valueType map泛型value
     * @param <T>
     * @param <V>
     * @return
     */
    public static final <T,V> Map<T,V> toMap(String json,Class<T> keyType,Class<V> valueType){
        final MapType mapType = mapper.getTypeFactory().
                                constructMapType(HashMap.class, keyType, valueType);
        Map<T,V> map = null;
        try{
            map = mapper.readValue(json, mapType);
        }catch(IOException e){
            throw new JacksonDeserializeException(e);
        }
        return map;
    }

    /**
     * json转为map
     * @param json
     * @return
     */
    public static final Map toMap(String json){
        return toMap(json,Object.class,Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject(String json, TypeReference<T> typeReference){
        try{
            return (T) mapper.readValue(json, typeReference);
        }catch(IOException e){
            throw new JacksonDeserializeException(e);
        }
    }

    private static final ObjectMapper newMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        // 允许单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 允许反斜杆等字符
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        // 允许出现对象中没有的字段
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true) ;
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //不进行格式化打印
        objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        objectMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        objectMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        objectMapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }
}
