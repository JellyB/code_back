package com.huatu.tiku.match.web.controller.edu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.UserDto;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.bo.MatchHeadUserBo;
import com.huatu.tiku.match.service.impl.v1.search.SearchHandler;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.search.MatchStatusService;
import com.huatu.tiku.match.util.DateUtil;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.Match;
import lombok.Getter;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * 描述： 1.模考大赛 首页相关接口
 *
 * @author huangqingpeng
 **/
@RestController
@RequestMapping(value = "search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ApiVersion("edu")
@Slf4j
public class SearchEduController {

    @Autowired
    private SearchHandler searchHandler;

    @Autowired
    private MatchUserMetaService matchUserMetaService;

    @Value("${spring.profiles}")
    public String env;

    @Autowired
    private MatchStatusService matchStatusService;
    //考试时间大于等于今天开始时间的都展示，（小于今天考试时间的考试均已过去一天以上，不再展示）
    private static final Predicate<Match> EDU_MATCH_HEADER_FILTER = match -> match.getStartTime() >= DateUtil.getTodayStartMillions();

    @LogPrint
    @GetMapping("list")
    public Object matches(@RequestParam(defaultValue = "-1") int subjectId,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "100") int size) throws BizException {
        if (subjectId < 0) {
            return Lists.newArrayList();
        }
        //游客模式返回字段
        return searchHandler.dealSearch(null, subjectId, page, size, EDU_MATCH_HEADER_FILTER
        );
    }

    @LogPrint
    @GetMapping("list/user")
    public Object matchHeaderUserInfo(@Token UserSession userSession,
                                      @RequestHeader(defaultValue = "-1") int terminal,
                                      @RequestHeader(defaultValue = "1") String cv,
                                      @RequestHeader(defaultValue = "-1") int subject,
                                      @RequestParam(defaultValue = "-1") int subjectId,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "100") int size) throws BizException {
        if (subjectId > 0) {
            subject = subjectId;
        }
        List<MatchHeadUserBo> matchHeadUserInfo = matchStatusService.getMatchHeadUserInfo(userSession.getId(), subject);
        return matchHeadUserInfo;
    }

    /**
     * 返回用户某一场考试的首页信息
     * @param paperId
     * @param subjectId
     * @param userSession
     * @param terminal
     * @param cv
     * @return
     * @throws BizException
     */
    @LogPrint
    @GetMapping("/{paperId}")
    public Object matchInfo(@PathVariable int paperId,
                            @RequestParam(defaultValue = "-1") int subjectId,
                            @Token(required = false, check = false) UserSession userSession,
                            @RequestHeader(defaultValue = "21") int terminal,
                            @RequestHeader(defaultValue = "1.0") String cv) throws BizException {
        return searchHandler.dealSearchById(userSession, subjectId, paperId);
    }

    @LogPrint
    @GetMapping("match/{paperId}")
    public Object matchInfo(@PathVariable int paperId,
                            @RequestParam String phone) throws BizException {
        List<LinkedHashMap<String, Object>> data = Lists.newArrayList();
        List<UserDto> userDtos = Lists.newArrayList(UserDto.builder().mobile(phone).build());
        assertUserInfo(userDtos,data);
        if(CollectionUtils.isEmpty(data)){
            throw new BizException(ErrorResult.create(1000123,"用户不存在"));
        }
        Integer userId = MapUtils.getInteger(data.get(0), "id");
        MatchUserMeta matchUserEnrollInfo = matchUserMetaService.findMatchUserEnrollInfo(userId, paperId);
        Map map = Maps.newHashMap();
        map.put("phone",phone);
        map.put("userId",userId);
        if(null!=matchUserEnrollInfo){
            map.put("positionId",matchUserEnrollInfo.getPositionId());
            map.put("positionName",matchUserEnrollInfo.getPositionName());
        }
        return map;

    }

    private void assertUserInfo(List<UserDto> userDtos, List<LinkedHashMap<String, Object>> data) {
        String url = "";
        if (!"test".equalsIgnoreCase(env)) {
            url = "https://ns.huatu.com/u/essay/statistics/user";
        } else {
            url = "http://192.168.100.22:11453/u/essay/statistics/user";
//            url = "https://ns.huatu.com/u/essay/statistics/user";
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        data.addAll(userDtoList.getData());
    }
}
