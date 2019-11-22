package com.huatu.tiku.match.dao.document;

import com.huatu.ztk.paper.bean.MatchUserMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by huangqingpeng on 2018/12/26.
 */
@Repository
@Slf4j
public class MatchUserMetaDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 根据模考大赛ID查询报名信息(分页处理)
     * @return
     */
    public List<MatchUserMeta> findByMatchId(int matchId, long index, int limit){
        Criteria criteria = Criteria.where("paperId").is(matchId)
                .and("userId").gt(index);
        Query query = new Query(criteria);
        query.limit(limit).with(new Sort(Sort.Direction.ASC, "userId"));
        return mongoTemplate.find(query, MatchUserMeta.class);
    }

    public MatchUserMeta findOneByUserId(int matchId,long userId){
        String id = getMatchUserMetaId(userId,matchId);
        return  mongoTemplate.findById(id, MatchUserMeta.class);
    }

    public MatchUserMeta findById(String id){
        long start = System.currentTimeMillis();
        MatchUserMeta meta = mongoTemplate.findById(id, MatchUserMeta.class);
        System.out.println("MatchUserMeta||"+ id +"||"+(System.currentTimeMillis()-start));
        return meta;
    }


    public String getMatchUserMetaId(long userId, int paperId) {
        return new StringBuilder().append(userId)
                .append("_").append(paperId).toString();
    }

    public void save(MatchUserMeta userMeta) {
        mongoTemplate.save(userMeta);
    }
}
