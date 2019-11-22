package com.huatu.common.spring.serializer;

import com.huatu.common.spring.compression.Compression;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author hanchao
 * @date 2018/3/16 19:29
 */
public class CompressionRedisSerializerDecorator<T> implements RedisSerializer<T> {
    private RedisSerializer<T> serializer;
    private Compression compression;

    public CompressionRedisSerializerDecorator(RedisSerializer<T> serializer, Compression compression){
        this.serializer = serializer;
        this.compression = compression;
    }

    @Override
    public byte[] serialize(T o) throws SerializationException {
        byte[] serialize = serializer.serialize(o);
        return compression.compress(serialize);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        byte[] decompress = compression.decompress(bytes);
        return serializer.deserialize(decompress);
    }
}
