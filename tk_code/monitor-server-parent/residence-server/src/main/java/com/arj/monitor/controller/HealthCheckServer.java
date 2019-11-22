package com.arj.monitor.controller;

import com.arj.monitor.common.RedisConstantKey;
import com.arj.monitor.entity.ServerInfo;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2019-01-02 下午8:36
 **/
@RestController
@Slf4j
@RequestMapping("/api/monitor")
public class HealthCheckServer {
    @Autowired
    private RedisTemplate redisTemplate;
    @ApiOperation(value = "服务列表")
    @GetMapping(value = "")
    public Object query() {
      List<ServerInfo> serverInfoList = redisTemplate.opsForList().range(RedisConstantKey.ALL_SERVER_INFO, 0, -1);
      Set<Object> set =  redisTemplate.opsForZSet().range(RedisConstantKey.EXCEPTION_SERVER_INFO,0,-1);
Map<String,Object> map = Maps.newHashMap();
map.put("serverInfoList",serverInfoList);
map.put("set",set);

        return map;
    }

}
