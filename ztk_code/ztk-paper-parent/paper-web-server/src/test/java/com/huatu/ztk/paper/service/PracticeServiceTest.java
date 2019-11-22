package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.activity.Estimate;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.paper.dao.MatchDao;
import com.huatu.ztk.paper.task.MatchAutoSubmitTaskV2;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shaojieyue
 * Created time 2016-08-10 09:55
 */
public class PracticeServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(PracticeServiceTest.class);

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private PaperAnswerCardUtilComponent paperAnswerCardUtilComponent;

    @Autowired
    private com.huatu.ztk.paper.task.MatchAutoSubmitTaskV2 MatchAutoSubmitTaskV2;

    @Autowired
    private MatchDao matchDao;

    @Test
    public void findCardsTest() {
        final PageBean cards = practiceService.findCards(12710393, Arrays.asList(1), Long.MAX_VALUE, 20,
                0, null, false, Lists.newArrayList(-1));
        System.out.println(cards.getResutls().size());
    }


    /**
     * 测试链接pandora数据库
     */
    @Test
    public void testPandoraSource() {
        //Integer isHas = paperAnswerCardUtilComponent.isHasGet("app_ztk1083808528", "86905,86906");
        Set<Integer> set = new HashSet<>();
        set.add(86907);
        set.add(86908);
        Integer isHas = paperAnswerCardUtilComponent.judgeIsHasGetGiftBag(set, 233982375L,
                "app_ztk896889356", 3527624);
        //course:86906:2005444
        logger.info("配置信息是：{}", isHas);
    }

    /**
     * 精准估分活动列表
     */
    @Test
    public void testAddGiftInfoForEstimateAnswerCard() throws BizException {
        StandardCard standardCard = new StandardCard();
        standardCard.setId(821);
        standardCard.setType(12);
        Paper paper = new Paper();
        paper.setId(821);
        paper.setScore(90);
        standardCard.setPaper(paper);
        paperAnswerCardUtilComponent.addGiftInfoForEstimateAnswerCard(standardCard);
    }

    @Test
    public void testEstimate() throws BizException {
        //List<Estimate> estimateList = paperAnswerCardUtilComponent.getCache(2005408);
        //logger.info("estimateList is :{}", estimateList);
        //paperAnswerCardUtilComponent.getEstimateGiftInfo(2005408);
        paperAnswerCardUtilComponent.getEstimateGiftInfoHash(3527585);
    }


    @Test
    public void testDeleteEstimate() throws BizException {
        //删除key
        //paperAnswerCardUtilComponent.deete(3527585);
    }


    @Test
    public void testGetCardUserMetaForTruePaper() {
        Paper paper = new Paper();
        paper.setId(3527466);
        StandardCard standardCard = StandardCard.builder()
                .paper(paper).build();
        standardCard.setId(456581891730120541L);
        standardCard.setScore(0);
        standardCard.setType(AnswerCardType.MATCH_AFTER);

        //CardUserMeta cardUserMetaForTruePaper = paperAnswerCardService.getCardUserMetaForTruePaper(standardCard);
    }



    @Test
    public void stringDouble() {
        Double score = 0.7D;
        logger.info("score内容是：{}", score.intValue());
    }

    @Test
    public void autoSubmit(){
        Match match = matchDao.findById(4001674);
        MatchAutoSubmitTaskV2.autoSubmit(match);
    }

}
