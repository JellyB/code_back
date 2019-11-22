package com.huatu.tiku.teacher.dao.mongo;

import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.ztk.paper.bean.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by huangqp on 2018\7\7 0007.
 */
@Repository
public class MatchDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询模考大赛信息
     * @param paperId
     * @return
     */
    public Match findById(int paperId) {
        return mongoTemplate.findById(paperId, Match.class);
    }

    public void save(Match match) {
        mongoTemplate.save(match);
    }

    public List<Match> findBySubject(Integer subjectId) {
        Criteria criteria = Criteria.where("subject").is(subjectId)
                .and("status").is(MatchInfoEnum.BackendStatusEnum.AUDIT_SUCCESS.getKey());
        Query query = new Query(criteria);
        List<Match> matches = mongoTemplate.find(query, Match.class);
        return matches;
    }
}

