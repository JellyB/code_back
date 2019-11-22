package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.question.common.QuestionCorrectType;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by renwenlong on 2016/10/11.
 */
public class ComputeTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(DayTrainService.class);

    @Autowired
    private PaperAnswerCardService dayTrainService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    final int userId = 10476537;

    @Test
    public void test(){
        String info = "{\"userId\":7831180,\"corrects\":[1, 2, 1, 1, 1, 2, 2, 2, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 2, 2, 2, 1, 1, 2, 2, 2, 1, 1, 2, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\"questions\":[60495, 60496, 60497, 60498, 60500, 60501, 60503, 60504, 60506, 60507, 60508, 60511, 60514, 60517, 60518, 60513, 60510, 60505, 60502, 60499, 60509, 60512, 60516, 60519, 60520, 60524, 60528, 60531, 60535, 60539, 60547, 60559, 60568, 60572, 60577, 60580, 60585, 60588, 60593, 60600, 60521, 60525, 60527, 60530, 60532, 60534, 60538, 60540, 60543, 60555, 60560, 60569, 60571, 60573, 60576, 60579, 60581, 60584, 60587, 60591, 60480, 60484, 60485, 60481, 60486, 60482, 60487, 60483, 60488, 60489, 60490, 60491, 60492, 60493, 60494, 60523, 60529, 60533, 60537, 60542, 60562, 60575, 60582, 60589, 60603, 60605, 60607, 60606, 60604, 60602, 60601, 60594, 60590, 60586, 60583, 60578, 60574, 60570, 60561, 60551, 60536, 60541, 60526, 60515, 60522, 60613, 60614, 60616, 60617, 60619, 60620, 60622, 60621, 60618, 60615, 60563, 60564, 60565, 60566, 60567, 60595, 60596, 60597, 60598, 60599, 60608, 60609, 60610, 60611, 60612, 60627, 60628, 60629, 60630, 60631],\"times\":[180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 171, 160, 153, 144, 138, 130, 126, 122, 116, 110, 107, 102, 100, 97, 96, 175, 167, 163, 156, 150, 146, 141, 135, 133, 128, 124, 120, 118, 114, 112, 109, 105, 104, 101, 98, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 180, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],\"createTime\":1386113195000,\"subject\":1}";
        Map map = JsonUtil.toMap(info);
        for (int i =0;i<100;i++){
            rabbitTemplate.convertAndSend("answer-card","",map);
        }
        System.out.println("map = " + map);
    }


    @Test
    public void testCreate() throws WaitException, BizException {
        AnswerCard answerCard = dayTrainService.findById(2036352183707893760L, 233906461);
//        computeScoreCop(answerCard);
        computeScoreCop2(answerCard);


    }


    public static double computeScoreCop(AnswerCard answerCard) {

        int totalScore = 100;
        double examScore = 0D;
        List<Module> modules = new LinkedList<>();
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();

            //试卷分数
            int paperScore = paper.getScore();
            if (paperScore >= 100) {
                totalScore = paperScore;
            }
            modules = paper.getModules();
        }
        int[] corrects = answerCard.getCorrects();
        List<Integer> correctList = new ArrayList<>(corrects.length);
        for(int c:corrects){
            correctList.add(new Integer(c));
        }

        //按模块计算分数
        if(CollectionUtils.isNotEmpty(modules)){
            int startIndex = 0;
            int endIndex = 0;
            //按模块取出答对的题目个数和打错的题目个数
            int moduleIndex = 0;
            for(Module module:modules){
                double mScore = 0D;
                double rcount = 0;
                double wcount = 0;
                endIndex += module.getQcount();
                List<Integer> subList = correctList.subList(startIndex, endIndex);
                //遍历输出错题个数，和答对的题目个数

                for(Integer correct:subList){
                    if(correct.equals(QuestionCorrectType.RIGHT)){
                        rcount++;
                    }else if(correct.equals(QuestionCorrectType.WRONG)){
                        wcount++;
                    }
                }
                //计算模块得分
                switch (moduleIndex) {
                    case 0:
                        mScore = 0.2 * rcount - 0.2 * wcount;
                        break;
                    case 1:
                        mScore = 0.5 * rcount ;
                        break;
                    case 2:
                        mScore = 0.6 * rcount ;

                        break;
                    case 3:
                        mScore = 1.2 * rcount ;
                        break;
                    case 4:
                        mScore = 0.7 * rcount ;
                        break;
                    case 5:
                        mScore = 1.0 * rcount ;
                        break;
                }

                examScore +=(mScore>0 ? mScore : 0);
                moduleIndex++;
                startIndex  += module.getQcount();
            }



        }
        logger.info(examScore+"==================");
        return examScore;
    }



    private double computeScoreCop2(AnswerCard answerCard) {

        int totalScore = 100;
        double examScore = 0D;
        List<Module> modules = new LinkedList<>();
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();

            //试卷分数
            int paperScore = paper.getScore();
            if (paperScore >= 100) {
                totalScore = paperScore;
            }
            modules = paper.getModules();
        }
        int[] corrects = answerCard.getCorrects();
        List<Integer> correctList = new ArrayList<>(corrects.length);
        for(int c:corrects){
            correctList.add(new Integer(c));
        }

        //按模块计算分数
        if(CollectionUtils.isNotEmpty(modules)){
            int startIndex = 0;
            int endIndex = 0;
            //按模块取出答对的题目个数和打错的题目个数
            int moduleIndex = 0;
            for(Module module:modules){
                double mScore = 0D;
                double rcount = 0;
                double wcount = 0;
                endIndex += module.getQcount();
                List<Integer> subList = correctList.subList(startIndex, endIndex);
                //遍历输出错题个数，和答对的题目个数

                for(Integer correct:subList){
                    if(correct.equals(QuestionCorrectType.RIGHT)){
                        rcount++;
                    }else if(correct.equals(QuestionCorrectType.WRONG)){
                        wcount++;
                    }
                }
                //计算模块得分
                switch (moduleIndex) {
                    case 0:
                        mScore = 0.2 * rcount - 0.2 * wcount;
                        break;
                    case 1:
                        mScore = 0.5 * rcount ;
                        break;
                    case 2:
                        mScore = 0.6 * rcount ;

                        break;
                    case 3:
                        mScore = 1.2 * rcount ;
                        break;
                    case 4:
                        mScore = 0.7 * rcount ;
                        break;
                    case 5:
                        mScore = 1.0 * rcount ;
                        break;
                }
                examScore +=(mScore>0 ? mScore : 0);
                moduleIndex++;
                startIndex  += module.getQcount();
            }

        }
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (examScore != 0) {
            return Double.valueOf(decimalFormat.format(examScore));
        }
        logger.info(examScore+"==================");

        return examScore;
    }
}