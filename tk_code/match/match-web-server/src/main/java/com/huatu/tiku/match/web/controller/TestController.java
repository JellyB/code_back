package com.huatu.tiku.match.web.controller;

import com.google.common.collect.Maps;
import com.huatu.common.SuccessMessage;
import com.huatu.common.utils.code.IdCenter;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.match.service.v1.meta.MatchQuestionMetaService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.sync.MatchMetaService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.common.ResponseMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/10/11
 */
@Slf4j
@RestController
@RequestMapping("test")
@ApiVersion("v1")
public class TestController {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MatchMetaService matchMetaService;

    @Autowired
    MatchUserMetaService matchUserMetaService;

    @Autowired
    MatchQuestionMetaService matchQuestionMetaService;

    private static MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");

    @Value("${spring.profiles}")
    public String env;
    @GetMapping
    public Object testNormal(){
        return SuccessMessage.create();
    }

    @GetMapping("getTokenInfo")
    public Object testToken(@Token UserSession userSession){
        return userSession.getUname();
    }

//    @PostMapping("postRabbit")
//    public Object testRabbit(@RequestBody(required = false) HashMap<String, String> content){
//        String key = MapUtils.getString(content,"key","");
//        log.info("key={}",key);
//        rabbitTemplate.convertAndSend("", RabbitMatchKeyEnum.getQueue(RabbitMatchKeyEnum.create(key),env),content);
//        return SuccessMessage.create();
//    }

    @GetMapping("sync")
    public Object testSync(){
         matchMetaService.syncMatchInfo();
         return SuccessMessage.create();
    }

    @GetMapping("sync/{paperId}")
    public Object testSync(@PathVariable int paperId){
        matchMetaService.syncMatchMetaInfo2DB(paperId, -1);
        return SuccessMessage.create();
    }

    @LogPrint
    @PostMapping("enroll/sync/{paperId}")
    public Object matchEnrollSync(@PathVariable int paperId){
        List<MatchUserMeta> metas = matchMetaService.findUserMetaByMatch(paperId);
        Example example = new Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
        example.and().andEqualTo("matchId",paperId);
        List<com.huatu.tiku.match.bean.entity.MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        if(CollectionUtils.isEmpty(matchUserMetas)){
            return SuccessMessage.create();
        }
        if(!CollectionUtils.isEmpty(metas)){
            List<Integer> oldIds = metas.stream().map(i -> i.getUserId()).map(Long::intValue).collect(Collectors.toList());
            matchUserMetas.removeIf(i->oldIds.contains(i.getUserId()));
        }
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("size",matchUserMetas.size());
        map.put("ids",matchUserMetas.stream().map(i->i.getUserId()).map(String::valueOf).collect(Collectors.joining(",")));
        return map;
    }


    @PostMapping("clear/question/meta")
    public Object matchEnrollSync(){
        matchQuestionMetaService.reCountQuestionMeta();
        matchUserMetaService.restAnswerCard();
        return SuccessMessage.create();
    }

    @GetMapping("id")
    public Object getId(){
        //long id = IdCenter.getInstance().getId();
        final long id = Long.valueOf(String.valueOf(System.nanoTime()) + String.valueOf(System.currentTimeMillis()).substring(11));
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("id",id);
        log.info("idCenter createId = {}",id);
        return id;
    }
}
