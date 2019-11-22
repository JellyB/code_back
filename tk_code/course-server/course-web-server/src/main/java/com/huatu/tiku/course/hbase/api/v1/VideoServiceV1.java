package com.huatu.tiku.course.hbase.api.v1;

import com.huatu.tiku.course.hbase.api.fail.VideoServiceV1Fallback;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;
import java.util.List;


@FeignClient(value = "hbase-service",fallback = VideoServiceV1Fallback.class,path = "/hbase")
public interface VideoServiceV1 {

    @PostMapping(value = "video/process/detail")
    Object videoProcessDetailV1(
            @RequestHeader("token") String token,
            @RequestHeader("terminal") int terminal,
            @RequestHeader("cv") String cv,
            @RequestBody List<HashMap> params
    );
}
