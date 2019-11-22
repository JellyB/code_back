package com.huatu.tiku.match.service.impl.v1.practice;

import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.bo.GiftBo;
import com.huatu.tiku.match.bo.paper.StandAnswerCardBo;
import com.huatu.tiku.match.constant.MatchErrors;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.meta.MetaSearchService;
import com.huatu.tiku.match.service.v1.paper.AnswerCardDBService;
import com.huatu.tiku.match.service.v1.practice.PracticeService;
import com.huatu.tiku.match.service.v1.search.GiftPackageService;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;

/**
 * 描述：我的答题卡service
 *
 * @author biguodong
 * Create time 2018-10-24 下午5:52
 **/
@Slf4j
@Service
public class PracticeServiceImpl implements PracticeService {

    @Autowired
    private AnswerCardDBService answerCardDBService;
    @Autowired
    private MatchUserMetaService matchUserMetaService;
    @Autowired
    private MetaSearchService metaSearchService;
    @Autowired
    private GiftPackageService giftPackageService;

    /**
     * 获取用户答题卡信息
     *
     * @param paperId
     * @param userId
     * @param userName
     * @return
     * @throws BizException
     */
    @Override
    public StandAnswerCardBo getUserAnswerCard(int paperId, int userId, String userName, String token,String cv,int  terminal) throws BizException {
        StopWatch stopWatch = new StopWatch("getUserAnswerCard :" + userName + ":" + paperId);
        stopWatch.start("findMatchUserEnrollInfo");
        MatchUserMeta matchUserMeta = matchUserMetaService.findMatchUserEnrollInfo(userId, paperId);
        if (null == matchUserMeta || matchUserMeta.getPracticeId() < 0) {
            log.error("用户暂无统计数据,userId={},paperId={}", userId, paperId);
         throw new com.huatu.common.exception.BizException(MatchErrors.NO_REPORT);


        }
        stopWatch.stop();
        stopWatch.start("findAnswerCardById");
        AnswerCard answerCard = answerCardDBService.findById(matchUserMeta.getPracticeId());
        stopWatch.stop();
        stopWatch.start("scoreConvert");
        BigDecimal bigDecimal = new BigDecimal(answerCard.getScore());
        double score = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        answerCard.setScore(score);
        StandardCard standardCard = (StandardCard) answerCard;
        stopWatch.stop();
        stopWatch.start("handlerStandAnswerCard");
        metaSearchService.handlerStandAnswerCard(standardCard,cv,terminal);
        stopWatch.stop();
        stopWatch.start("buildGiftInfo4AnswerCard");
        AnswerCardWithGift answerCardWithGift = (AnswerCardWithGift) giftPackageService.buildGiftInfo4AnswerCard(standardCard, userName, token);
        stopWatch.stop();
        stopWatch.start("giftBoCreate");
        GiftBo giftBo = new GiftBo();
        BeanUtils.copyProperties(answerCardWithGift, giftBo);
        StandAnswerCardBo standAnswerCardBo = new StandAnswerCardBo();
        BeanUtils.copyProperties(standardCard, standAnswerCardBo);
        standAnswerCardBo.setIdStr(String.valueOf(standardCard.getId()));
        standAnswerCardBo.setGiftInfo(giftBo);
        stopWatch.stop();
        log.info(stopWatch.prettyPrint());
        //分数转化为字符串
        convertDoubleScoreToString(standAnswerCardBo);
        return standAnswerCardBo;
    }


    /**
     * 统一处理模考中分数,全都添加对应字符串字段
     *
     * @param standAnswerCardBo
     */
    public void convertDoubleScoreToString(StandAnswerCardBo standAnswerCardBo) {
        if (null == standAnswerCardBo) {
            return;
        }
        //学员得分
        standAnswerCardBo.setScoreStr(String.valueOf(standAnswerCardBo.getScore()));
        CardUserMeta cardUserMeta = standAnswerCardBo.getCardUserMeta();
        //平均分,最高分
        if (null != cardUserMeta) {
            cardUserMeta.setAverageStr(String.valueOf(cardUserMeta.getAverage()));
            cardUserMeta.setMaxStr(String.valueOf(cardUserMeta.getMax()));
        }
        //模考职位平均分,职位最高分
        MatchCardUserMeta matchMeta = standAnswerCardBo.getMatchMeta();
        if (null != matchMeta) {
            matchMeta.setPositionAverageStr(String.valueOf(matchMeta.getPositionAverage()));
            matchMeta.setPositionMaxStr(String.valueOf(matchMeta.getPositionMax()));
        }
    }

}
