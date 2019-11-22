package com.huatu.tiku.course.netschool.api.fall;

import com.huatu.common.serialize.Serializer;
import com.huatu.common.serialize.kryo.KryoSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;

/**
 * @author hanchao
 * @date 2017/10/2 15:59
 */
@Slf4j
public class FallbackCacheHolder {
    //    private static final Cache<Object,Object> FALLBACK_CACHE  = CacheBuilder.newBuilder()
//            .expireAfterAccess(3, TimeUnit.DAYS)
//            .initialCapacity(100)
//            .maximumSize(2000)
//            .concurrencyLevel(10)
//            .build();
    private static RocksDB db;
    private static final String PATH_TO_DB = "/app/data/rocksdb/course-web-server";
    private static Serializer serializer = new KryoSerializer();

    static {
        RocksDB.loadLibrary();
        try{
            // a factory method that returns a RocksDB instance
            final Options options = new Options().setCreateIfMissing(true)
                    .optimizeForPointLookup(2048);
            FileUtils.forceMkdir(new File(PATH_TO_DB));
            db = RocksDB.open(options,PATH_TO_DB);

        } catch (RocksDBException e) {
            throw new RuntimeException("open rocksdb fail.",e);
        } catch (IOException e) {
            throw new RuntimeException("open rocksdb fail.",e);
        }
    }

    /**
     * 不能返回基本类型,否则npe
     *
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T get(Object key) {
        byte[] bytes = null;
        try {
            bytes = db.get(serializer.serialize(key));
        } catch(RocksDBException e){
            log.error("get cache from rocksdb error,key is {}",key,e);
        }
        T result = null;
        if(bytes != null){
            try {
                result = (T) serializer.deserialize(bytes);
            } catch(Exception e){
                log.error("",e);
            }
        }
        return result;
    }

    public static void put(Object key, Object value) {
        try {
            db.put(serializer.serialize(key),serializer.serialize(value));
        } catch(Exception e){
            log.error("",e);
        }
    }

}
