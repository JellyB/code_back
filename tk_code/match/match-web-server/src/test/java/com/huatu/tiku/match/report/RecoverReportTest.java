package com.huatu.tiku.match.report;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.dao.document.AnswerCardDao;
import com.huatu.tiku.match.dao.document.MatchUserMetaDao;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.sync.MatchMetaService;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.common.AnswerCardType;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by huangyitian on 2019/3/3.
 */
public class RecoverReportTest extends BaseWebTest {

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private MatchUserMetaDao matchUserMetaDao;

    @Autowired
    MatchMetaService matchMetaService;

    @Autowired
    MatchUserMetaService matchUserMetaService;

    @Test
    public void recoverPracticeId(){
        int matchId = 4001303;
        List<MatchUserMeta> userMetas = matchMetaService.findUserMetaByMatch(matchId);
        for (MatchUserMeta userMeta : userMetas) {
            long userId = userMeta.getUserId();
            List<AnswerCard> answers = answerCardDao.findMatchCardByUserId(userId);
            Optional<AnswerCard> first = answers.stream().filter(answerCard -> {
                if (answerCard instanceof StandardCard) {
                    Paper paper = ((StandardCard) answerCard).getPaper();
                    if (paper.getId() == matchId) {
                        return true;
                    }
                }
                return false;
            }).findFirst();
            if(first.isPresent()){
                System.out.println("first = " + first.get().getId()+",userId =" + userId);
                System.out.println("修改答题卡ID信息和报名信息");
                AnswerCard answerCard = first.get();
                answerCard.setType(AnswerCardType.MATCH);
                answerCardDao.save(answerCard);
                userMeta.setPracticeId(first.get().getId());
                matchUserMetaDao.save(userMeta);
                System.out.println(" 修改答题卡和用户报名数据成功");
            }
        }
    }
    @Test
    public void recoverAnswerCard(){
        ArrayList<Integer> matchIds = Lists.newArrayList(4001303,4001304,4001305,4001306);
        Example example = new Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
        example.and().andIn("matchId",matchIds).andGreaterThan("practiceId",-1);
        List<com.huatu.tiku.match.bean.entity.MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        List<Long> practiceIds = matchUserMetas.stream().map(com.huatu.tiku.match.bean.entity.MatchUserMeta::getPracticeId).collect(Collectors.toList());
        for (Long practiceId : practiceIds) {
            AnswerCard answerCard = answerCardDao.findById(practiceId);
            answerCard.setType(AnswerCardType.MATCH);
            answerCardDao.save(answerCard);
        }
    }
}
