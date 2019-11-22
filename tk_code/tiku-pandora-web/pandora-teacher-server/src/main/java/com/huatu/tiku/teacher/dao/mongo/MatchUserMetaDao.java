package com.huatu.tiku.teacher.dao.mongo;

import com.google.common.collect.Lists;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/10/17.
 */
@Repository
public class MatchUserMetaDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<MatchUserMeta> findByPaperId(int matchId) {
        Criteria criteria = new Criteria();
        criteria.and("paperId").is(matchId);
        List<com.huatu.ztk.paper.bean.MatchUserMeta> matchUserMetas = mongoTemplate.find(new Query(criteria), com.huatu.ztk.paper.bean.MatchUserMeta.class);
        if (CollectionUtils.isEmpty(matchUserMetas)) {
            return Lists.newArrayList();
        }
        List<MatchUserMeta> result = matchUserMetas.stream().map(i -> {
            MatchUserMeta matchUserMeta = new MatchUserMeta();
            matchUserMeta.setMatchId(matchId);
            matchUserMeta.setUserId(new Long(i.getUserId()).intValue());
            matchUserMeta.setPracticeId(i.getPracticeId());
            Long schoolId = i.getSchoolId();
            if (null != schoolId && schoolId > 0) {
                matchUserMeta.setSchoolId(schoolId.intValue());
                matchUserMeta.setSchoolName(i.getSchoolName());
            }
            matchUserMeta.setPositionId(i.getPositionId());
            matchUserMeta.setPositionName(i.getPositionName());
            return matchUserMeta;
        }).collect(Collectors.toList());
        return result;
    }
}
