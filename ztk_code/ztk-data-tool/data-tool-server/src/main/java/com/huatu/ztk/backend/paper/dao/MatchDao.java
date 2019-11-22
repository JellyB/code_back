package com.huatu.ztk.backend.paper.dao;

import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by linkang on 17-7-26.
 */
@Repository
public class MatchDao {
    private static final Logger logger = LoggerFactory.getLogger(MatchDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    public void save(Match match) {
        mongoTemplate.save(match);
    }

    public Match findById(int paperId) {
        return mongoTemplate.findById(paperId, Match.class);
    }
    public List<Match> findAll() {
        Criteria criteria = Criteria.where("status").is(2);
        return mongoTemplate.find(new Query(criteria), Match.class);
    }

    public void updateStatus(int id, int status) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().set("status", status);
        mongoTemplate.updateFirst(new Query(criteria), update, Match.class);
    }

    public List<MatchUserMeta> findAllMatchUserMeta(String s) {
        Criteria criteria = Criteria.where("_id").regex(s);
        return mongoTemplate.find(new Query(criteria),  MatchUserMeta.class);
    }


}
