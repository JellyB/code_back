package com.huatu.ztk.report.dao;

import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * 答题卡dao层
 * Created by shaojieyue
 * Created time 2016-05-03 14:09
 */

@Repository
public class AnswerCardDao {
    private static final Logger logger = LoggerFactory.getLogger(AnswerCardDao.class);

    private static final String collection = "ztk_answer_card";
    /**
     * 存储试题的集合名字
     */
    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 保存答题卡
     *
     * @param answerCard
     */
    public void save(AnswerCard answerCard) {
        answerCard.setPoints(null);//points不进行存储,目的为了节省mongo内存
        mongoTemplate.save(answerCard);
    }

    /**
     * 遍历答题卡信息（分页）
     * @param startIndex
     * @param offset
     * @return
     */
    public List<AnswerCard> findForPage(long startIndex, int offset) {
        Criteria criteria = Criteria.where("_id").gt(startIndex).and("status").is(AnswerCardStatus.FINISH);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "_id"));
        query.limit(offset);
        return mongoTemplate.find(query, AnswerCard.class, collection);
    }

    public void findAndHandlerAnswerCard(Consumer<List<AnswerCard>> consumer, long startIndex) {
        //分片查询mongo的数据比较
        int offset = 1000;
        while (true) {
            //查询MONGO复合条件的id（左开右闭）
            List<AnswerCard> answerCardList = findForPage(startIndex, offset);
            if (CollectionUtils.isEmpty(answerCardList)) {
                break;
            } else {
                consumer.accept(answerCardList);
            }
            long endIndex = answerCardList.stream().map(AnswerCard::getId).max(Comparator.comparing(Long::longValue)).get();
            startIndex = endIndex;
        }
    }

}
