package com.huatu.tiku.essay.test;

import com.google.common.collect.Maps;
import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.admin.answer.AdminPaperAnswerCountVO;
import com.huatu.tiku.essay.vo.resp.MockQuestionAnswerVO;
import com.huatu.tiku.essay.entity.vo.report.Line;
import com.huatu.tiku.essay.entity.vo.report.LineSeries;
import com.huatu.tiku.essay.entity.vo.report.MatchHistory;
import com.huatu.tiku.essay.entity.vo.report.MockScoreReportVO;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayPaperBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangqp on 2018\1\1 0001.
 */
@Slf4j
public class PaperTest extends BaseWebTest{
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Test
    public void test(){
        List<EssayPaperBase> papers = essayPaperBaseRepository.findByStatusNotAndNameLike(-1,"%正式%");
        for(EssayPaperBase paper:papers){
            double score = paper.getScore();
            long paperId = paper.getId();
            int limitTime = paper.getLimitTime();
            log.info("paperId={},score={},limitTime={}",paperId,score,limitTime);
            if(score!=120&&score!=100&&score!=150){
                System.out.println("paperId is not legal，score="+score);
            }
            double total = (double)score;
            boolean flag = true;
            List<EssayQuestionBase> questions = essayQuestionBaseRepository.findByPaperIdAndStatusNot(paperId,-1);
            for(EssayQuestionBase questionBase:questions){
                EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionBase.getDetailId());
                total = total - (int)questionDetail.getScore();
                if (total<0) {
                    flag =false;
                    break;
                }
                Double tmp = limitTime*questionDetail.getScore()/total;
                questionBase.setLimitTime(tmp.intValue());
            }
            if(flag){
                essayQuestionBaseRepository.save(questions);
            }else{
                System.out.println("paperId is not legal，paperId="+paperId);
            }
        }
    }
    @Test
    public void testLimitTime(){
        List<EssayQuestionBase> essayQuestionBases = essayQuestionBaseRepository.findAll();
        Set<Long> quesitonIds = Sets.newHashSet();
        for(EssayQuestionBase essayQuestionBase:essayQuestionBases){
            System.out.println("id={}"+essayQuestionBase.getId());
            if(essayQuestionBase.getDetailId()<=0){
                quesitonIds.add(essayQuestionBase.getId());
                continue;
            }
            EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(essayQuestionBase.getDetailId());
            if(questionDetail==null||questionDetail.getScore()<=0){
                quesitonIds.add(essayQuestionBase.getId());
                continue;
            }
            int limtTime = 0;
            switch(questionDetail.getType()){
                case 1:
                case 2:
                case 3: {limtTime = new Double(questionDetail.getScore()).intValue() + 15; break;}
                case 4: {limtTime = new Double(questionDetail.getScore()).intValue() + 20; break;}
                case 5: {limtTime = new Double(questionDetail.getScore()).intValue() + 35; break;}
            }
            limtTime = limtTime * 60;
            essayQuestionBase.setLimitTime(limtTime);
            essayQuestionBaseRepository.save(essayQuestionBase);
            System.out.println("limitTime={}"+limtTime+";id={}"+essayQuestionBase.getId());

        }
        log.info("errorIds = {}",quesitonIds);
    }
    @Test
    public void testReport(){
        int userId = 267317;
//        int userId = 232891389;
//        int userId = 233906356;
        int paperId = 318;
        getHistory(userId,paperId);
        MockScoreReportVO mockScoreReportVO = MockScoreReportVO.builder()
                .areaEnrollCount(2345)
                .areaRank(13)
                .examScore(45.5)
                .maxScore(123.5)
                .name("2018省模考大赛-申论")
                .score(150)
                .spendTime(9000)
                .totalCount(1)
                .totalEnroll(3456)
                .totalRank(2)
                .unfinishedCount(2)
                .questionList(getQuestionAnswerList())
                .line(getLine())
                .build();

        String mockExamReportPrefix = RedisKeyConstant.getMockExamReportPrefix(paperId, userId);
        log.info(mockExamReportPrefix);
        redisTemplate.opsForValue().set(mockExamReportPrefix,mockScoreReportVO);
    }
    public Line getLine(){
        List categories = Lists.newArrayList();
        categories.add("全站平均得分");
        categories.add("模考得分");
        List data = Lists.newArrayList();
        data.add(80);
        data.add(60.5);
        LineSeries lineSeries = LineSeries.builder()
                .data(data)
                .name("8-13")
                .build();
        return Line.builder()
                .categories(categories)
                .series(Lists.newArrayList(lineSeries))
                .build();

    }
    public List<MockQuestionAnswerVO> getQuestionAnswerList(){
        MockQuestionAnswerVO questionAnswerVO = MockQuestionAnswerVO.builder()
                .examScore(80.5)
                .inputWordNum(185)
                .score(150)
                .sort(1)
                .spendTime(143)
                .type(1)
                .build();
        return Lists.newArrayList(questionAnswerVO);
    }
//    @Test
    public void getHistory(int userId, int paperId) {
        //从缓存中获取最近的一次模考id
//        int userId = 267317;
//        long paperId = 318;
        String mockHistoryKey = RedisKeyConstant.getMockHistoryKey(userId,paperId);
        List<MatchHistory> historyList = Lists.newArrayList();
        MatchHistory matchHistory = MatchHistory.builder()
                .total(10)//总人数
                .name("TEST")//模考名称
                .essayPaperId(318)//模考id
                .startTime(System.currentTimeMillis())//开始时间
                .flag(2)
                .build();
        historyList.add(matchHistory);
        Map map = Maps.newHashMap();
        map.put("line", getLine());
        map.put("list", historyList);
        log.info("mapResult = {}",map);
        log.info("mockHistoryKey = {}",mockHistoryKey);
        redisTemplate.opsForValue().set(mockHistoryKey,map);
    }
    @Test
    public void testPaperCount(){
    	
    	 List<AdminPaperAnswerCountVO> paperCorrectCountInfo = essayPaperAnswerRepository.getPaperCorrectCountInfo(53L, EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType());
    	log.info(paperCorrectCountInfo.toString());
    }
}
