package com.huatu.tiku.course.hbase.api.fail;

import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.hbase.api.v1.VideoServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

/**
 * Created by lijun on 2018/5/21
 */
@Slf4j
@Component
public class VideoServiceV1Fallback implements VideoServiceV1 {

    @Override
    public Object videoProcessDetailV1(String token, int terminal, String cv, List<HashMap> params) {
        log.info(" videoServiceV1 ==> videoProcessDetailV1 fail");
        return HashMapBuilder.newBuilder().build();
    }
}
