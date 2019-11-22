package com.huatu.ztk.commons.spring.serializer;

import com.huatu.ztk.commons.spring.serializer.kyro.KryoSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author hanchao
 * @date 2017/9/6 17:05
 */
public class KryoRedisSerializer<T> implements RedisSerializer {
    private KryoSerializer kryoSerializer = new KryoSerializer();
    @Override
    public byte[] serialize(Object o) throws SerializationException {
        return kryoSerializer.serialize(o);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return kryoSerializer.deserialize(bytes);
    }

}
