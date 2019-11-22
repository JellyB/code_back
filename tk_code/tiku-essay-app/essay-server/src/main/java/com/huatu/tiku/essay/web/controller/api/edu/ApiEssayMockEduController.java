package com.huatu.tiku.essay.web.controller.api.edu;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.CommonErrors;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.EssayMaterial;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.UserDto;
import com.huatu.tiku.essay.entity.vo.report.Match;
import com.huatu.tiku.essay.entity.vo.report.MatchUserMeta;
import com.huatu.tiku.essay.service.EssayMatchService;
import com.huatu.tiku.essay.service.EssayMockExamService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.web.controller.api.V3.EssayMockUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author huangqingpeng
 * @title: ApiEssayMockEduController
 * @description: 申论教育模考大赛相关
 * @date 2019-07-2915:12
 */
@RestController
@RequestMapping("api/edu/mock")
@Slf4j
public class ApiEssayMockEduController {
    @Autowired
    EssayMockExamService essayMockExamService;
    @Autowired
    EssayMatchService essayMatchService;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Value("${extra_time}")
    private long extraTime;
    @Value("${enterLimitTime}")
    private Integer enterLimitTime;
    @Value("${commitLimitTime}")
    private Integer commitLimitTime;
    //    距离可以查看试题信息XXmin时，按钮置成灰色的”开始考试”。
    @Value("${startLimitTime}")
    private Integer startLimitTime;

    @Value("${spring.profiles}")
    public String env;


    /**
     * 模考大赛入口接口
     *
     * @param userSession
     * @return
     * @throws BizException
     */
    @LogPrint
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getMatch(@Token(required = false, check = false) UserSession userSession) throws BizException {
        int userId = -1;
        if (null != userSession) {
            userId = userSession.getId();
        }
        // 查当前可见的模考大赛(单独的申论模考&&和行测绑定的申论模考)
        List<EssayMockExam> matches = essayMatchService.getCurrent();
        LinkedList<Match> list = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(matches)) {
            for (EssayMockExam mock : matches) {
                //题冲模考信息和用户报名信息
                Match match = EssayMockUtil.packMatchInfo(mock, userId,
                        enterLimitTime, commitLimitTime, startLimitTime,
                        essayMatchService, redisTemplate,
                        extraTime);
                //设置match的flag标识（）
                EssayMockUtil.checkMatchFlag(match);
                //当状态为未报名和停止报名时，stage 统一置为 0
                match.setStage(2);
                list.add(match);
            }
        }
        return list;

    }

    /**
     * 模考大赛入口接口
     *
     * @param userSession
     * @return
     * @throws BizException
     */
    @LogPrint
    @RequestMapping(value = "/{paperId}", method = RequestMethod.GET)
    public Object getMatch(@Token(required = false, check = false) UserSession userSession,
                           @PathVariable long paperId) throws BizException {
        int userId = -1;
        if (null != userSession) {
            userId = userSession.getId();
        }
        // 查当前可见的模考大赛(单独的申论模考&&和行测绑定的申论模考)
        EssayMockExam mock = essayMockExamService.getMock(paperId);
        if (null == mock) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        Match match = EssayMockUtil.packMatchInfo(mock, userId,
                enterLimitTime, commitLimitTime, startLimitTime,
                essayMatchService, redisTemplate,
                extraTime);

        //设置match的flag标识（）
        EssayMockUtil.checkMatchFlag(match);
        //当状态为未报名和停止报名时，stage 统一置为 0
        match.setStage(2);
        return match;
    }


    /**
     * 查询模考试卷材料
     * （MQ创建答题卡）
     */
    @LogPrint
    @GetMapping(value = "materialList/{paperId}")
    public Object materialList(@Token UserSession userSession,
                               @RequestHeader int terminal,
                               @RequestHeader String cv,
                               @PathVariable(name = "paperId") long paperId) throws BizException {
        List<EssayMaterial> essayMaterials = essayMockExamService.materialList(userSession.getId(), paperId, terminal);
        EssayMockExam mockDetail = essayMockExamService.getMockDetail(paperId);
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("material", essayMaterials);
        map.put("essayPaper", mockDetail);
        map.put("startTime", mockDetail.getStartTime().getTime());
        map.put("endTime", mockDetail.getEndTime().getTime());
        return map;
    }

    @LogPrint
    @GetMapping("/match/{paperId}")
    public Object matchInfo(@PathVariable int paperId,
                            @RequestParam String phone) throws BizException {
        List<LinkedHashMap<String, Object>> data = Lists.newArrayList();
        List<UserDto> userDtos = Lists.newArrayList(UserDto.builder().mobile(phone).build());
        assertUserInfo(userDtos, data);
        if (CollectionUtils.isEmpty(data)) {
            throw new BizException(ErrorResult.create(1000123, "用户不存在"));
        }
        Integer userId = MapUtils.getInteger(data.get(0), "id");
        MatchUserMeta userMeta = essayMatchService.findMatchUserMeta(userId, paperId);
        Map map = Maps.newHashMap();
        map.put("phone", phone);
        map.put("userId", userId);
        if (null != userMeta) {
            map.put("positionId", userMeta.getPositionId());
            map.put("positionName", userMeta.getPositionName());
        }
        return map;

    }

    private void assertUserInfo(List<UserDto> userDtos, List<LinkedHashMap<String, Object>> data) {
        String url = "";
        if (!"test".equalsIgnoreCase(env)) {
            url = "https://ns.huatu.com/u/essay/statistics/user";
        } else {
            url = "http://192.168.100.22:11453/u/essay/statistics/user";
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        data.addAll(userDtoList.getData());
    }
}
