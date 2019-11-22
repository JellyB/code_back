package com.huatu.ztk.backend.metas.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.metas.service.PracticeMetaService;
import com.huatu.ztk.backend.paper.dao.AnswerCardDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.util.ExcelManageUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.user.bean.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/11/19
 * @描述 模考大赛相关统计
 */
@CrossOrigin(origins = "*", maxAge = 36000)
@RestController
@RequestMapping(value = "/matchInfo", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MatchInfoController {
    private final static Logger logger = LoggerFactory.getLogger(PracticeMetaController.class);

    //导出excel路径
    private final static String IMPORT_PATH = "/Users/lizhenjuan/tool/";
    @Autowired
    private PracticeMetaService practiceMetaService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PaperDao paperDao;

    @Autowired
    AnswerCardDao answerCardDao;


    /**
     * 统计某一模考大赛参加学员的信息
     *
     * @param paperId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "enroll/user", method = RequestMethod.GET)
    public Object getMatchEnrollInfo(@RequestParam int paperId) throws BizException {

        Long start = System.currentTimeMillis();
        List<MatchUserMeta> matchUserMetas = practiceMetaService.getMatchUserListByMatchId(paperId);
        //参加模考大赛判断
        matchUserMetas = matchUserMetas.stream().filter(i -> i.getPracticeId() > 0).collect(Collectors.toList());
        Long start1 = System.currentTimeMillis();
        logger.info("查询报名信息用时：{}", start1 - start);

        List<UserDto> list = practiceMetaService.getUserInfoListByUserMate(matchUserMetas);
        long start2 = System.currentTimeMillis();
        logger.info("查询用户信息用时：{}", start2 - start1);

        List dataList = Lists.newArrayList();
        for (int index = 0; index < list.size(); index++) {
            UserDto userDto = list.get(index);
            dataList.add(Lists.newArrayList(userDto.getId(), userDto.getName(), userDto.getNick(), userDto.getMobile()));
        }
        String[] title = {"用户ID", "名称", "昵称", "手机号"};
        try {
            ExcelManageUtil.writer(IMPORT_PATH, "MatchInfo" + paperId, "xls", dataList, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map mapData = Maps.newHashMap();
        mapData.put("title", "MatchEnrollInfo_" + paperId);
        mapData.put("text", "MatchEnrollInfo_" + paperId + "_" + new Date());
        mapData.put("filePath", IMPORT_PATH + "MatchEnrollInfo_" + paperId + ".xls");
        mapData.put("attachName", "MatchEnrollInfo_" + System.currentTimeMillis());

        long start3 = System.currentTimeMillis();
        logger.info("写入excel用时：{}", start3 - start2);
        return mapData;
    }


    /**
     * 参加模考大赛使用不同设备考试的人数
     *
     * @param paperId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "userMatchTerminal", method = RequestMethod.GET)
    public Object getUserMatchTerminal(@RequestParam int paperId) throws BizException {

        //获取模考参加人数
        String paperPracticeIdKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        ZSetOperations zsetOperations = redisTemplate.opsForZSet();
        Set<String> paperPracticeIds = zsetOperations.range(paperPracticeIdKey, 0, -1);
        List<Long> paperPracticeIdList = paperPracticeIds.stream().map(id -> Long.valueOf(id)).collect(Collectors.toList());

        logger.info("模考大赛参加人数:{}", paperPracticeIds.size());
        List<AnswerCard> answerCards = answerCardDao.findByIds(paperPracticeIdList);
        //按照设备进行分组
        Map<Integer, List<AnswerCard>> answerCardList = answerCards.stream().collect(Collectors.groupingBy(AnswerCard::getTerminal));
        List<HashMap> mapList = answerCardList.entrySet().stream().map(entry -> {
            HashMap map = new HashMap();
            map.put("设备ID是", entry.getKey());
            map.put("参加人数", entry.getValue().size());
            return map;
        }).collect(Collectors.toList());
        logger.info("参加统计是:{}", JsonUtil.toJson(mapList));
        return mapList;
    }


}
