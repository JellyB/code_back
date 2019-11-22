package com.huatu.tiku.course.test;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huatu.common.test.BaseTest;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.hbase.api.v1.VideoServiceV1;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * Created by lijun on 2018/6/1
 */
@Slf4j
public class HbaseTest extends BaseTest {

    @Autowired
    private VideoServiceV1 videoService;

    @Test
    public void test(){
        HashMap<Object, Object> data = HashMapBuilder.newBuilder()
                .put("rid", "895889")
                .put("JoinCode", "")
                .put("bjyRoomId", "")
                .put("bjySessionId", "")
                .put("bjyVideoId", "")
                .put("hasTeacher","0")
                .build();

        HashMap params = HashMapBuilder.<String, Object>newBuilder()
                .put("rid", String.valueOf(data.get("rid")))
                .put(": ", data.get("JoinCode") == null ? "" : String.valueOf(data.get("JoinCode")))
                .put("roomId", data.get("bjyRoomId") == null ? "" : String.valueOf(data.get("bjyRoomId")))
                .put("bjySessionId", data.get("bjySessionId") == null ? "" : String.valueOf(data.get("bjySessionId")))
                .build();
        String hasTeacher = String.valueOf(data.get("hasTeacher"));
        params.put(hasTeacher.equals("0") ? "videoIdWithTeacher" : "videoIdWithoutTeacher",
                data.get("bjyVideoId") == null ? "" : String.valueOf(data.get("bjyVideoId")));

        Object o = videoService.videoProcessDetailV1(
                "4c32d806c4c14c829e3f107b7c3ba9a8",
                2,
                "6.1.0",
                Lists.newArrayList(params)
        );
        System.out.println(JSON.toJSON(o).toString());
    }
}
