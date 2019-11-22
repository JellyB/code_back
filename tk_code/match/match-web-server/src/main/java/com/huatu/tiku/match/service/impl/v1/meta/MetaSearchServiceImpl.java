package com.huatu.tiku.match.service.impl.v1.meta;

import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.dao.document.MatchDao;
import com.huatu.tiku.match.service.impl.v1.paper.AnswerCardUtil;
import com.huatu.tiku.match.service.v1.meta.MatchQuestionMetaService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.meta.MetaSearchService;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/1/9.
 */
@Slf4j
@Service
public class MetaSearchServiceImpl implements MetaSearchService {

    @Autowired
    private MatchUserMetaService matchUserMetaService;

    @Autowired
    private MatchQuestionMetaService matchQuestionMetaService;

    @Autowired
    private MatchDao matchDao;

    /**
     * 组装答题卡报告数据
     *
     * @param answerCard
     */
    @Override
    public void handlerStandAnswerCard(StandardCard answerCard,String cv,int terminal) throws BizException {
        StopWatch stopWatch = new StopWatch("handlerStandAnswerCard");
        stopWatch.start("getReport");
        Paper paper = answerCard.getPaper();
        if(null == paper){
            return;
        }
        int paperId = paper.getId();
        long userId = answerCard.getUserId();
        MatchUserMeta matchUserMeta = matchUserMetaService.getReport(paperId, new Long(userId).intValue());
        stopWatch.stop();
        stopWatch.start("questionPointSummaryWithTotalNumber");
        if(null == matchUserMeta){
            return;
        }

        Double averageScore = matchUserMeta.getAverage();
        Double maxScore = matchUserMeta.getMaxScore();

        //平均分保留一位小数
        if (!AnswerCardUtil.judgeUserCv(terminal, cv)) {
            matchUserMeta.setAverage(AnswerCardUtil.transInt.apply(averageScore));
            matchUserMeta.setMaxScore(AnswerCardUtil.transInt.apply(maxScore));
        } else {
            matchUserMeta.setAverage(AnswerCardUtil.transDouble.apply(averageScore));
            matchUserMeta.setMaxScore(AnswerCardUtil.transDouble.apply(maxScore));
        }

        List<QuestionPointTree> questionPointTrees = matchQuestionMetaService.questionPointSummaryWithTotalNumber(paper.getQuestions(), answerCard.getCorrects(), answerCard.getTimes());
        stopWatch.stop();
        stopWatch.start("dealMatchCardUserMeta");
        if(CollectionUtils.isNotEmpty(questionPointTrees)){
            answerCard.setPoints(questionPointTrees);
        }
        final int beatRate = ((matchUserMeta.getRankCount() - matchUserMeta.getRank()) * 100 / matchUserMeta.getRankCount());
        CardUserMeta cardUserMeta = CardUserMeta
                .builder()
                .rank(matchUserMeta.getRank())
                .total(matchUserMeta.getRankCount())
                .average(matchUserMeta.getAverage())
                .max(matchUserMeta.getMaxScore())
                .beatRate(beatRate)
                .build();
        MatchCardUserMeta matchCardUserMeta = dealMatchCardUserMeta(matchUserMeta,cv,terminal);
        answerCard.setCardUserMeta(cardUserMeta);
        answerCard.setMatchMeta(matchCardUserMeta);
        answerCard.setLastIndex(Integer.max(0, answerCard.getLastIndex()));
        stopWatch.stop();
        log.info("handlerStandAnswerCard stopWatch:{}",stopWatch.prettyPrint());
    }

    @Override
    public void handlerQuestionMeta(Question question) {
        //暂时只处理客观题的统计信息
        if(question instanceof GenericQuestion){
            QuestionMeta questionMeta = matchQuestionMetaService.getQuestionMeta(question.getId());
            ((GenericQuestion) question).setMeta(questionMeta);
        }
    }

    /**
     * 模考大赛统计信息
     * 统计模考大赛才有的数据（曲线图，地区成绩）
     * @return
     */
    public MatchCardUserMeta dealMatchCardUserMeta(final MatchUserMeta matchUserMeta,String cv,int terminal) {
        StopWatch stopWatch = new StopWatch("dealMatchCardUserMeta");
        stopWatch.start("findMatchById");
        MatchCardUserMeta userMeta = new MatchCardUserMeta();
        userMeta.setPositionName(matchUserMeta.getPositionName());
        userMeta.setPositionRank(matchUserMeta.getRankForPosition());
        userMeta.setPositionCount(matchUserMeta.getRankCountForPosition());
        userMeta.setPositionBeatRate(0);
        userMeta.setPositionId(matchUserMeta.getPositionId());
        Integer matchId = matchUserMeta.getMatchId();
        Match match = matchDao.findById(matchId);
        stopWatch.stop();
        stopWatch.start("getAvailableMatchMeta");
        List<MatchUserMeta> availableMatchMeta = matchUserMetaService.getAvailableMatchMeta(matchUserMeta.getUserId(), match.getTag(), match.getSubject());
        List<MatchUserMeta> temp = availableMatchMeta.stream()
                .filter(i -> !i.getSubmitTime().after(matchUserMeta.getSubmitTime())        //交卷缓存时间和持久化时间有略微出入
                    || i.getMatchId().equals(matchUserMeta.getMatchId()))               //保证当前的考试在折线图中存在
                .collect(Collectors.toList());
        stopWatch.stop();
        stopWatch.start("getMatchLine");


        Line matchLine = matchUserMetaService.getMatchLine(temp);
        if(!AnswerCardUtil.judgeUserCv(terminal,cv)){
            AnswerCardUtil.handlerLine(matchLine,AnswerCardUtil.transInt);
        }
        stopWatch.stop();
        log.info("dealMatchCardUserMeta stopWatch:{}",stopWatch.prettyPrint());
        userMeta.setScoreLine(matchLine);
        return userMeta;
    }


}
