package com.huatu.ztk.pc.dao;

import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.common.MatchBackendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by linkang on 2017/09/28 下午4:18
 */
@Repository
public class MatchDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Match findById(int paperId) {
        return mongoTemplate.findById(paperId, Match.class);
    }

    public MatchUserMeta findMatchUserMeta(long userId,int paperId){
        String id = getMatchUserMetaId(userId,paperId);
        return mongoTemplate.findById(id,MatchUserMeta.class);
    }
    private String getMatchUserMetaId(long userId, int paperId) {
        return new StringBuilder().append(userId)
                .append("_").append(paperId).toString();
    }
}
