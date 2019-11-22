package com.huatu.tiku.match.dao.document;

import com.huatu.tiku.match.common.PaperErrorInfo;
import com.huatu.tiku.match.enums.AnswerCardInfoEnum;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.common.AnswerCardType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * 描述： 答题卡dao
 *
 * @author biguodong
 * Create time 2018-10-29 下午4:48
 **/
@Repository
public class AnswerCardDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据id查询答题卡
     */
    public AnswerCard findById(long practiceId) {
        final AnswerCard answerCard = mongoTemplate.findById(practiceId, AnswerCard.class);
        if(null != answerCard && null != answerCard.getAnswers()){
            answerCard.setAnswers(Arrays.stream(answerCard.getAnswers()).map(i-> StringUtils.isBlank(i)?"0":i).toArray(String[]::new));
        }
        return answerCard;
    }

    /**
     * 保存答题卡
     */
    public void save(AnswerCard answerCard) {
        mongoTemplate.save(answerCard);
    }


    public List<AnswerCard> findById(List<Long> ids) {
        final Criteria criteria = Criteria.where("_id").in(ids);
        return mongoTemplate.find(new Query(criteria),AnswerCard.class);
    }

    /**
     * 补救答题卡专用查询逻辑，其他逻辑禁止使用
     * @param userId
     * @return
     */
    @Deprecated
    public List<AnswerCard> findMatchCardByUserId(long userId) {
        final Criteria criteria = Criteria.where("userId").is(userId).and("type").is(AnswerCardType.SIMULATE);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC,"createTime"));
        query.limit(10);
        return mongoTemplate.find(query, AnswerCard.class);
    }
}
