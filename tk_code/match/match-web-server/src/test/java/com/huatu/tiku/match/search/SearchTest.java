package com.huatu.tiku.match.search;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.match.bo.paper.AnswerCardSimpleBo;
import com.huatu.tiku.match.bo.paper.AnswerResultBo;
import com.huatu.tiku.match.common.PaperErrorInfo;
import com.huatu.tiku.match.dto.paper.AnswerDTO;
import com.huatu.tiku.match.enums.AnswerCardInfoEnum;
import com.huatu.tiku.match.service.impl.v1.paper.AnswerCardUtil;
import com.huatu.tiku.match.service.impl.v1.paper.PaperMatchComponent;
import com.huatu.tiku.match.service.v1.meta.MatchQuestionMetaService;
import com.huatu.tiku.match.service.v1.meta.MetaHandlerService;
import com.huatu.tiku.match.service.v1.paper.AnswerCardDBService;
import com.huatu.tiku.match.service.v1.paper.AnswerHandleService;
import com.huatu.tiku.match.service.v1.paper.PaperUserMetaService;
import com.huatu.tiku.match.service.v1.search.CourseService;
import com.huatu.tiku.springboot.users.service.UserSessionService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.sun.xml.internal.ws.api.model.MEP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-02-20 下午2:33
 **/
@Slf4j
public class SearchTest extends BaseWebTest{

    @Autowired
    private CourseService courseService;

    @Autowired
    AnswerCardDBService answerCardDBService;

    @Autowired
    AnswerHandleService answerHandleService;

    @Autowired
    PaperMatchComponent paperMatchComponent;

    @Autowired
    UserSessionService userSessionService;

    @Autowired
    MetaHandlerService metaHandlerService;

    @Autowired
    PaperUserMetaService paperUserMetaService;

    @Autowired
    MatchQuestionMetaService matchQuestionMetaService;

    @Test
    public void testCourseInfo(){
        int classId = 72009;
        Object object = courseService.courseInfo(classId);
        log.info("courseInfo:{}", JSONObject.toJSONString(object));
    }

    @Test
    public void test(){
        long practiceId = 96693252098423555L;
        int userId = 233959879;
        AnswerCard answerCard = answerCardDBService.findById(practiceId);
        System.out.println("answerCard = " + JsonUtil.toJson(answerCard));
        String answerStr = "[{\"questionId\":40064525,\"answer\":\"1\",\"expireTime\":2,\"doubt\":0},{\"questionId\":40064527,\"answer\":\"2\",\"expireTime\":1,\"doubt\":0},{\"questionId\":40064542,\"answer\":\"2\",\"expireTime\":1,\"doubt\":0},{\"questionId\":40064544,\"answer\":\"3\",\"expireTime\":1,\"doubt\":0},{\"questionId\":40064546,\"answer\":\"3\",\"expireTime\":2,\"doubt\":0},{\"questionId\":40064551,\"answer\":\"1\",\"expireTime\":1,\"doubt\":0},{\"questionId\":40064554,\"answer\":\"2\",\"expireTime\":1,\"doubt\":0},{\"questionId\":40064557,\"answer\":\"2\",\"expireTime\":1,\"doubt\":0},{\"questionId\":40064563,\"answer\":\"3\",\"expireTime\":21,\"doubt\":0},{\"questionId\":40064564,\"answer\":\"1\",\"expireTime\":36,\"doubt\":0},{\"questionId\":40064565,\"answer\":\"3\",\"expireTime\":12,\"doubt\":0},{\"questionId\":40064566,\"answer\":\"4\",\"expireTime\":12,\"doubt\":0},{\"questionId\":40064567,\"answer\":\"1\",\"expireTime\":41,\"doubt\":0},{\"questionId\":40064568,\"answer\":\"2\",\"expireTime\":36,\"doubt\":0},{\"questionId\":40064569,\"answer\":\"3\",\"expireTime\":6,\"doubt\":0},{\"questionId\":40064570,\"answer\":\"1\",\"expireTime\":48,\"doubt\":0},{\"questionId\":40064571,\"answer\":\"3\",\"expireTime\":38,\"doubt\":0},{\"questionId\":40064572,\"answer\":\"3\",\"expireTime\":34,\"doubt\":0},{\"questionId\":40064573,\"answer\":\"3\",\"expireTime\":47,\"doubt\":0},{\"questionId\":40064574,\"answer\":\"4\",\"expireTime\":10,\"doubt\":0},{\"questionId\":40064509,\"answer\":\"1\",\"expireTime\":13,\"doubt\":0},{\"questionId\":40064510,\"answer\":\"4\",\"expireTime\":30,\"doubt\":0},{\"questionId\":40064511,\"answer\":\"4\",\"expireTime\":51,\"doubt\":0},{\"questionId\":40064512,\"answer\":\"4\",\"expireTime\":26,\"doubt\":0},{\"questionId\":40064514,\"answer\":\"2\",\"expireTime\":18,\"doubt\":0},{\"questionId\":40064516,\"answer\":\"1\",\"expireTime\":60,\"doubt\":0},{\"questionId\":40064518,\"answer\":\"4\",\"expireTime\":36,\"doubt\":0},{\"questionId\":40064520,\"answer\":\"3\",\"expireTime\":43,\"doubt\":0},{\"questionId\":40064522,\"answer\":\"2\",\"expireTime\":63,\"doubt\":0},{\"questionId\":40064524,\"answer\":\"2\",\"expireTime\":45,\"doubt\":0},{\"questionId\":40064526,\"answer\":\"1\",\"expireTime\":82,\"doubt\":0},{\"questionId\":40064528,\"answer\":\"3\",\"expireTime\":32,\"doubt\":0},{\"questionId\":40064529,\"answer\":\"2\",\"expireTime\":39,\"doubt\":0},{\"questionId\":40064530,\"answer\":\"2\",\"expireTime\":16,\"doubt\":0},{\"questionId\":40064531,\"answer\":\"4\",\"expireTime\":25,\"doubt\":0},{\"questionId\":40064532,\"answer\":\"3\",\"expireTime\":29,\"doubt\":0},{\"questionId\":40064533,\"answer\":\"3\",\"expireTime\":27,\"doubt\":0},{\"questionId\":40064534,\"answer\":\"2\",\"expireTime\":50,\"doubt\":0},{\"questionId\":40064535,\"answer\":\"2\",\"expireTime\":21,\"doubt\":0},{\"questionId\":40064536,\"answer\":\"1\",\"expireTime\":40,\"doubt\":0},{\"questionId\":40064537,\"answer\":\"3\",\"expireTime\":70,\"doubt\":0},{\"questionId\":40064538,\"answer\":\"3\",\"expireTime\":98,\"doubt\":0},{\"questionId\":40064539,\"answer\":\"3\",\"expireTime\":31,\"doubt\":0},{\"questionId\":40064540,\"answer\":\"2\",\"expireTime\":73,\"doubt\":0},{\"questionId\":40064541,\"answer\":\"3\",\"expireTime\":73,\"doubt\":0},{\"questionId\":40064543,\"answer\":\"2\",\"expireTime\":136,\"doubt\":0},{\"questionId\":40064545,\"answer\":\"4\",\"expireTime\":32,\"doubt\":0},{\"questionId\":40064547,\"answer\":\"4\",\"expireTime\":30,\"doubt\":0},{\"questionId\":40064548,\"answer\":\"3\",\"expireTime\":33,\"doubt\":0},{\"questionId\":40064549,\"answer\":\"3\",\"expireTime\":37,\"doubt\":0},{\"questionId\":40064550,\"answer\":\"1\",\"expireTime\":20,\"doubt\":0},{\"questionId\":40064552,\"answer\":\"3\",\"expireTime\":39,\"doubt\":0},{\"questionId\":40064553,\"answer\":\"1\",\"expireTime\":43,\"doubt\":0},{\"questionId\":40064555,\"answer\":\"3\",\"expireTime\":48,\"doubt\":0},{\"questionId\":40064556,\"answer\":\"2\",\"expireTime\":60,\"doubt\":0},{\"questionId\":40064558,\"answer\":\"3\",\"expireTime\":40,\"doubt\":0},{\"questionId\":40064559,\"answer\":\"2\",\"expireTime\":72,\"doubt\":0},{\"questionId\":40064560,\"answer\":\"1\",\"expireTime\":57,\"doubt\":0},{\"questionId\":40064561,\"answer\":\"4\",\"expireTime\":94,\"doubt\":0},{\"questionId\":40064562,\"answer\":\"3\",\"expireTime\":60,\"doubt\":0},{\"questionId\":40064480,\"answer\":\"1\",\"expireTime\":27,\"doubt\":0},{\"questionId\":40064481,\"answer\":\"4\",\"expireTime\":7,\"doubt\":0},{\"questionId\":40064482,\"answer\":\"3\",\"expireTime\":78,\"doubt\":0},{\"questionId\":40064483,\"answer\":\"3\",\"expireTime\":1,\"doubt\":0},{\"questionId\":40064484,\"answer\":\"2\",\"expireTime\":5,\"doubt\":0},{\"questionId\":40064485,\"answer\":\"2\",\"expireTime\":4,\"doubt\":0},{\"questionId\":40064486,\"answer\":\"4\",\"expireTime\":4,\"doubt\":0},{\"questionId\":40064487,\"answer\":\"1\",\"expireTime\":3,\"doubt\":0},{\"questionId\":40064488,\"answer\":\"2\",\"expireTime\":3,\"doubt\":0},{\"questionId\":40064489,\"answer\":\"2\",\"expireTime\":1,\"doubt\":0},{\"questionId\":40064575,\"answer\":\"4\",\"expireTime\":62,\"doubt\":0},{\"questionId\":40064576,\"answer\":\"3\",\"expireTime\":50,\"doubt\":0},{\"questionId\":40064577,\"answer\":\"1\",\"expireTime\":45,\"doubt\":0},{\"questionId\":40064578,\"answer\":\"4\",\"expireTime\":21,\"doubt\":0},{\"questionId\":40064579,\"answer\":\"2\",\"expireTime\":21,\"doubt\":0},{\"questionId\":40064580,\"answer\":\"3\",\"expireTime\":75,\"doubt\":0},{\"questionId\":40064581,\"answer\":\"4\",\"expireTime\":37,\"doubt\":0},{\"questionId\":40064582,\"answer\":\"3\",\"expireTime\":72,\"doubt\":0},{\"questionId\":40064583,\"answer\":\"3\",\"expireTime\":32,\"doubt\":0},{\"questionId\":40064584,\"answer\":\"2\",\"expireTime\":31,\"doubt\":0},{\"questionId\":40064585,\"answer\":\"4\",\"expireTime\":38,\"doubt\":0},{\"questionId\":40064586,\"answer\":\"3\",\"expireTime\":15,\"doubt\":0},{\"questionId\":40064587,\"answer\":\"2\",\"expireTime\":50,\"doubt\":0},{\"questionId\":40064588,\"answer\":\"4\",\"expireTime\":44,\"doubt\":0},{\"questionId\":40064589,\"answer\":\"4\",\"expireTime\":75,\"doubt\":0},{\"questionId\":40064590,\"answer\":\"3\",\"expireTime\":74,\"doubt\":0},{\"questionId\":40064591,\"answer\":\"4\",\"expireTime\":36,\"doubt\":0},{\"questionId\":40064592,\"answer\":\"2\",\"expireTime\":37,\"doubt\":0},{\"questionId\":40064593,\"answer\":\"2\",\"expireTime\":42,\"doubt\":0},{\"questionId\":40064594,\"answer\":\"3\",\"expireTime\":14,\"doubt\":0},{\"questionId\":40064595,\"answer\":\"4\",\"expireTime\":22,\"doubt\":0},{\"questionId\":40064596,\"answer\":\"4\",\"expireTime\":18,\"doubt\":0},{\"questionId\":40064597,\"answer\":\"2\",\"expireTime\":12,\"doubt\":0},{\"questionId\":40064598,\"answer\":\"3\",\"expireTime\":25,\"doubt\":0},{\"questionId\":40064599,\"answer\":\"3\",\"expireTime\":43,\"doubt\":0},{\"questionId\":40064600,\"answer\":\"2\",\"expireTime\":10,\"doubt\":0},{\"questionId\":40064601,\"answer\":\"3\",\"expireTime\":12,\"doubt\":0},{\"questionId\":40064602,\"answer\":\"3\",\"expireTime\":17,\"doubt\":0},{\"questionId\":40064603,\"answer\":\"2\",\"expireTime\":14,\"doubt\":0},{\"questionId\":40064604,\"answer\":\"2\",\"expireTime\":16,\"doubt\":0},{\"questionId\":40064605,\"answer\":\"4\",\"expireTime\":77,\"doubt\":0},{\"questionId\":40064606,\"answer\":\"3\",\"expireTime\":37,\"doubt\":0},{\"questionId\":40064607,\"answer\":\"1\",\"expireTime\":42,\"doubt\":0},{\"questionId\":40064608,\"answer\":\"1\",\"expireTime\":116,\"doubt\":0},{\"questionId\":40064609,\"answer\":\"3\",\"expireTime\":108,\"doubt\":0},{\"questionId\":40064611,\"answer\":\"4\",\"expireTime\":74,\"doubt\":0},{\"questionId\":40064612,\"answer\":\"4\",\"expireTime\":46,\"doubt\":0},{\"questionId\":40064613,\"answer\":\"3\",\"expireTime\":12,\"doubt\":0},{\"questionId\":40064614,\"answer\":\"2\",\"expireTime\":44,\"doubt\":0},{\"questionId\":40064615,\"answer\":\"3\",\"expireTime\":14,\"doubt\":0},{\"questionId\":40064491,\"answer\":\"2\",\"expireTime\":192,\"doubt\":0},{\"questionId\":40064492,\"answer\":\"1\",\"expireTime\":171,\"doubt\":0},{\"questionId\":40064493,\"answer\":\"1\",\"expireTime\":167,\"doubt\":0},{\"questionId\":40064494,\"answer\":\"1\",\"expireTime\":97,\"doubt\":0},{\"questionId\":40064495,\"answer\":\"3\",\"expireTime\":164,\"doubt\":0},{\"questionId\":40064498,\"answer\":\"3\",\"expireTime\":59,\"doubt\":0},{\"questionId\":40064499,\"answer\":\"3\",\"expireTime\":51,\"doubt\":0},{\"questionId\":40064500,\"answer\":\"1\",\"expireTime\":81,\"doubt\":0},{\"questionId\":40064501,\"answer\":\"2\",\"expireTime\":91,\"doubt\":0},{\"questionId\":40064502,\"answer\":\"3\",\"expireTime\":81,\"doubt\":0},{\"questionId\":40064504,\"answer\":\"2\",\"expireTime\":206,\"doubt\":0},{\"questionId\":40064505,\"answer\":\"3\",\"expireTime\":71,\"doubt\":0},{\"questionId\":40064506,\"answer\":\"2\",\"expireTime\":135,\"doubt\":0},{\"questionId\":40064507,\"answer\":\"2\",\"expireTime\":101,\"doubt\":0},{\"questionId\":40064508,\"answer\":\"4\",\"expireTime\":42,\"doubt\":0},{\"questionId\":40064515,\"answer\":\"2\",\"expireTime\":67,\"doubt\":0},{\"questionId\":40064517,\"answer\":\"2\",\"expireTime\":142,\"doubt\":0},{\"questionId\":40064519,\"answer\":\"1\",\"expireTime\":174,\"doubt\":0},{\"questionId\":40064521,\"answer\":\"4\",\"expireTime\":64,\"doubt\":0},{\"questionId\":40064523,\"answer\":\"3\",\"expireTime\":62,\"doubt\":0}]";
        List<AnswerDTO> answerList = JsonUtil.toList(answerStr,AnswerDTO.class);
        log.info("submit:practice {} status is {}", practiceId, answerCard.getStatus());
        if(CollectionUtils.isNotEmpty(answerList)){
            answerList.removeIf(i-> (StringUtils.isBlank(i.getAnswer()) || "0".equals(i.getAnswer()) || !NumberUtils.isDigits(i.getAnswer())));
        }
        //答题卡状态设置成已完成
        answerCard.setStatus(AnswerCardInfoEnum.Status.FINISH.getCode());
        if(CollectionUtils.isNotEmpty(answerList)){
            save(userId, practiceId, answerList,answerCard);
            List<AnswerResultBo> answerResultBos = answerHandleService.handleQuestionAnswer(answerList);
            saveAnswerInfoToAnswerCard(answerCard, answerResultBos);
        }
        answerCardDBService.saveToDB(answerCard);
        metaHandlerService.saveScore(answerCard.getId());
        if (answerCard instanceof StandardCard) {
            paperUserMetaService.addFinishPractice(answerCard.getUserId(), ((StandardCard) answerCard).getPaper().getId(), practiceId);
        }
        matchQuestionMetaService.handlerQuestionMeta(answerCard);
        System.out.println("answerCard = " + JsonUtil.toJson(answerCard));
    }

    private void save(int userId, long practiceId, List<AnswerDTO> answerList, AnswerCard answerCard) {
        log.info("save:practice {} status is {}", practiceId, answerCard.getStatus());
        validateUserAnswerCardInfo(userId, answerCard);
        List<AnswerResultBo> answerResultBos = answerHandleService.handleQuestionAnswer(answerList);
        //答题卡状态设置成已答
        answerCard.setStatus(AnswerCardInfoEnum.Status.UNDONE.getCode());
        saveAnswerInfoToAnswerCard(answerCard, answerResultBos);
//        answerCardDBService.save(answerCard);
    }

    /**
     * 试题平均值计算
     */
    private final static Function<AnswerCard, Double> questionAverage = (answerCard) -> {
        if (null == answerCard) {
            return NumberUtils.DOUBLE_ZERO;
        }
        if (answerCard.getRcount() == NumberUtils.INTEGER_ZERO) {
            return NumberUtils.DOUBLE_ZERO;
        }
        int total = 100;
        if(answerCard instanceof StandardCard){
            total = ((StandardCard) answerCard).getPaper().getScore();
        }
        BigDecimal source = new BigDecimal((double) answerCard.getRcount() / (double) answerCard.getCorrects().length * total).setScale(2, BigDecimal.ROUND_HALF_UP);
        return source.doubleValue();
    };

    private final static Function<AnswerCard,Double> questionScoreSum = (answerCard -> {
        if (null == answerCard) {
            return NumberUtils.DOUBLE_ZERO;
        }
        if(answerCard instanceof StandardCard){
            Paper paper = ((StandardCard) answerCard).getPaper();
            List<Double> scores = paper.getScores();
            int[] corrects = answerCard.getCorrects();
            if(corrects.length == scores.size()){
                Double score = 0D;
                for (int i = 0; i< corrects.length;i++){
                    if(corrects[i] == 1){
                        score += scores.get(i);
                    }
                }
                return score;
            }
        }
        return NumberUtils.DOUBLE_ZERO;

    });
    private void saveAnswerInfoToAnswerCard(AnswerCard answerCard, List<AnswerResultBo> answerResultBos) {
        if (answerCard instanceof StandardCard) {
            Integer scoreFlag = 0;
            try {
                scoreFlag = ((StandardCard) answerCard).getPaper().getScoreFlag();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (null == scoreFlag || scoreFlag.intValue() == 0) {
                answerHandleService.saveAnswerInfoToAnswerCard(answerCard, answerResultBos, questionAverage);
            } else {
                answerHandleService.saveAnswerInfoToAnswerCard(answerCard, answerResultBos, questionScoreSum);
            }
        }
    }

    private static void validateUserAnswerCardInfo(Integer userId, AnswerCard answerCard) {
        //答题卡不存在
        if (null == answerCard) {
            PaperErrorInfo.AnswerCard.PAPER_INFO_NOT_EXIT.exception();
        }
        //用户信息不匹配
        if (answerCard.getUserId() != userId.intValue()) {
            PaperErrorInfo.AnswerCard.USER_NOT_EXIT_IN_PAPER.exception();
        }
        //答题卡状态不匹配
//        if (AnswerCardInfoEnum.Status.CREATE.getCode() != answerCard.getStatus() && AnswerCardInfoEnum.Status.UNDONE.getCode() != answerCard.getStatus()) {
//            log.error("answerCard status error,id ={},message={}", answerCard.getId(), PaperErrorInfo.AnswerCard.ANSWER_CARD_HAS_FINISHED.getMessage());
//            PaperErrorInfo.AnswerCard.ANSWER_CARD_HAS_FINISHED.exception();
//        }
    }

    @Test
    public void test12(){
        long userId = 233959879;
        int paperId = 4001836;
        int terminal = 1;

        String token = userSessionService.getTokenById(userId);
        if(StringUtils.isBlank(token)){
            System.out.println("token = " + token);
            return;
        }
        UserSession userSession = userSessionService.getUserSession(token);
        AnswerCard answerCard = paperMatchComponent.createAnswerCard(userSession, paperId, terminal);
        System.out.println("JsonUtil.toJson(answerCard) = " + JsonUtil.toJson(answerCard));
    }

}
