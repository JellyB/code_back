package com.huatu.ztk.paper.controller.applets;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.paper.service.PaperUserMetaService;
import com.huatu.ztk.paper.util.DateUtil;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@CrossOrigin
@RestController
@RequestMapping(value = "/v1/applets", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AppletController {

    private static final Logger logger = LoggerFactory.getLogger(AppletController.class);
    @Autowired
    UserSessionService userSessionService;

    @Autowired
    PaperAnswerCardService answerCardService;

    @Autowired
    PaperUserMetaService paperUserMetaService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYY/MM/dd");

    /**
     * 小程序试卷列表查询（答题记录按照试卷为单位分组查询）
     *
     * @param token
     * @param type
     * @param subject
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/paperList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object paperList(@RequestHeader String token,
                            @RequestHeader(defaultValue = AnswerCardType.APPLETS_PAPER + "") int type,
                            @RequestHeader(defaultValue = "1") int subject) throws BizException {

        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        List<StandardCard> answerCards = answerCardService.findAnswerCardByUidAndType(userId, subject, type);
        List<HashMap> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(answerCards)) {
            return result;
        }
        Map<Integer, HashMap> date = Maps.newHashMap();
        BiConsumer<Map, AnswerCard> updateIdAndCount = ((map, answerCard) -> {
            int count = MapUtils.getIntValue(map, "count", 0);
            if (answerCard.getStatus() != 3) {      //未完成
                if (MapUtils.getString(map, "idStr", "-1").equals("-1")) {     //只记录第一个未完成的答题卡ID
                    map.put("practiceId", answerCard.getId());
                    map.put("idStr", answerCard.getId() + "");
                }
            } else {                             //已完成
                if (StringUtils.isEmpty(MapUtils.getString(map, "idStr"))) {  //没有初始值
                    map.put("practiceId", -1L);
                    map.put("idStr", "-1");
                }
                count++;
            }
            map.put("count", count);
        });
        for (StandardCard answerCard : answerCards) {
            Paper paper = answerCard.getPaper();
            Integer id = paper.getId();
            HashMap tempMap = date.getOrDefault(id, Maps.newHashMap());
            if (null != tempMap && !tempMap.isEmpty()) {       //存在则只做答题卡和交卷次数的更新
                updateIdAndCount.accept(tempMap, answerCard);
                continue;
            }
            tempMap.put("id", paper.getId());
            tempMap.put("name", paper.getName());
            updateIdAndCount.accept(tempMap, answerCard);
            date.put(id, tempMap);
            result.add(tempMap);
        }
        return result;
    }

    /**
     * 小程序试卷对应答题记录查询
     *
     * @param token
     * @param paperId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/cardList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object answerCardList(@RequestHeader(required = false) String token,
                                 @RequestParam int paperId) throws BizException {
        logger.info("answerCardList:token={},paperId={}", token, paperId);
        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);
        PaperUserMeta meta = paperUserMetaService.findById(userId, paperId);
        ArrayList<HashMap> result = Lists.newArrayList();
        if (null == meta || meta.getFinishCount() == 0) {
            return result;
        }
        List<Long> practiceIds = meta.getPracticeIds();
        List<AnswerCard> answerCards = answerCardService.findByIds(practiceIds);
        if (CollectionUtils.isEmpty(answerCards)) {
            return result;
        }
        answerCards.sort((a, b) -> (int) (b.getCreateTime() - a.getCreateTime()));
        for (AnswerCard answerCard : answerCards) {
            if (answerCard.getStatus() < AnswerCardStatus.FINISH) {
                continue;
            }
            HashMap tempMap = Maps.newHashMap();
            String dateString = getDateString(answerCard.getCreateTime());
            tempMap.put("date", dateString);
            tempMap.put("time",answerCard.getCreateTime());
            tempMap.put("score", answerCard.getScore());
            tempMap.put("practiceId", answerCard.getId());
            tempMap.put("idStr", answerCard.getId() + "");
            if (answerCard instanceof StandardCard) {
                Paper paper = ((StandardCard) answerCard).getPaper();
                tempMap.put("paperId", paper.getId());
                tempMap.put("paperScore", paper.getScore());
            }
            result.add(tempMap);
        }
        logger.info("result={}", JsonUtil.toJson(result));
        return result;
    }

    private String getDateString(long date) {
        return dateTimeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.of("Asia/Shanghai")));
    }
}
