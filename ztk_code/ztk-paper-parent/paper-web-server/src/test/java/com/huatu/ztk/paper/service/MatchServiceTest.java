package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.MatchDao;
import com.huatu.ztk.question.common.QuestionCorrectType;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class MatchServiceTest extends BaseTest{
    @Autowired
    private MatchService matchService;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    MatchDao matchDao;
    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private PracticeCardService practiceCardService;

    @Autowired
    private MatchServiceComponent matchServiceComponent;

    @Test
    public void getHistory() throws Exception {
        long userId = 239;
        Object history = matchService.getHistory(userId, 1,1, 1, "7.10.11");

        System.out.println(JsonUtil.toJson(history));

    }

    @Test
    public void addToSet() throws Exception{
        AnswerCard answerCard = answerCardDao.findById(1776552394423795712L);
        matchService.addMatchPracticeSet((StandardCard)answerCard);
    }

    @Test
    public void testMatchSubmit() throws Exception{
        final int paperId = 3526116;
        ExecutorService service = Executors.newFixedThreadPool(200);
        AtomicLong uid = new AtomicLong(1);
        for (int i = 1; i <= 1; i++) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        long userId = uid.getAndIncrement();
//                        matchService.enroll(paperId, userId, 1);
                        StandardCard card = matchService.createPractice(paperId, 1, userId, 1);


                        List<Answer> answers = new ArrayList<>();
                        for (Integer qid : card.getPaper().getQuestions()) {
                            Answer an = new Answer();
                            an.setAnswer("1");
                            an.setQuestionId(qid);
                            an.setTime(RandomUtils.nextInt(30, 80));
                            an.setCorrect(RandomUtils.nextInt(1, 100) > 55 ? QuestionCorrectType.RIGHT : QuestionCorrectType.WRONG);
                            answers.add(an);
                        }

                        paperAnswerCardService.submitPractice(card.getId(), userId, answers, -9,TerminalType.ANDROID,"7.0.0");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        service.shutdown();

        while (!service.isTerminated()) {

        }
        System.out.println(">>>>end.");
    }


    @Test
    public void clearEmptyAnswerCard(){
        int paperId = 4001520;
        List<MatchUserMeta> metas = matchDao.findMetaByPaperWithPractice(paperId);
        if(CollectionUtils.isEmpty(metas)){
            return;
        }
        for (MatchUserMeta meta : metas) {
            AnswerCard answerCard = answerCardDao.findById(meta.getPracticeId());
            int[] corrects = answerCard.getCorrects();
            System.out.println(corrects.length);
            if(null == corrects||corrects.length==0){
                meta.setPracticeId(-1);
                matchDao.saveUserMeta(meta);
            }
        }

    }

    @Test
    public void test() throws BizException {
        String ids  = "234687148,11214262,234548039,234115402,234880980,236062807,234231370,236173119,233394856,236192527,12183970,235987515,234043004,233243935,235667638,236127944,236150625,234607245,233549373,233728176,235361176,233540814,236193150,236210831,236042903,235810211,234841428,234693500,234136113,235996839,233017485,235936683,12790120,234923070,235116352,236080494,235794945,235267402,236157036,234488996,236169252,10798288,236163622,235054262,9913243,233081555,235530938,235444174,9365772,233801692,234742832,233456200,235723196,235565321,235594658,233168080,8151051,236098936,233094878,236051859,236199501,233138197,235557895,234230393,10697539,234058659,236207427,233277125,234170809,233393458,236169372,234496518,234860820,234331863,234430906,236140065,233746531,236063570,235712164,235936710,235787026,236205245,235852123,234740196,234377859,235097145,236122210,234639268,235763850,235637996,236120720,235013856,236205225,10016904,233264226,232942191,234383164,236002790,234901067,234055576,233205744,235358757,236178596,235722843,234320473,235135389,11639081,233390457,12873481,233953856,235751440,236019082,234384669,233778817,234187733,12323343,235443895,234366083,236018806,236075533,236143255,233858349,235973512,234290137,235699138,235350119,235271789,233628118,234505726,234490459,234344598,235149899,235660851,234899097,235824723,235822288,236153450,235673270,234381378,234361141,235026798,235896158,3017637,236014340,234189729,233125660,236125219,235237303,235054323,236038644,235976182,234669539,235987248,236110758,236197865,234820147,236073772,9829227,235761064,236127532,235726905,233671836,233555371,233035911,12890388,236070600,235368363,235127861,236196375,234345300,234336361,236103532,234170744,234561353,235315453,235812277,235855158,236191633,236011956,233863897,233064024,233489882,235390212,235488163,233162763,235590765,235324908,235797584,233816129,236081858,234386171,236193480,235682000,236140793,235785680,10219137,12494466,233587422,9925551,235783962,235386734,235238319,236148528,233986379,232877769,234654208,236077192,233239026,234935330,236040217,234424734,235939648,235981447,235830130,10101551,234448438,235975684,234386285,236129555,236113334,236122342,233066192,234026126,234019318";
        for (String s : ids.split(",")) {
            matchService.enrollV2(4001636,Long.parseLong(s),-9,2,1);
        }
    }

    @Test
    public void count(){
        int i = matchServiceComponent.countMatchEnrollSize(4001694);
        System.out.println("i = " + i);
        int x = matchServiceComponent.countMatchSubmitSize(4001694);
        System.out.println("x = " + x);
    }
}