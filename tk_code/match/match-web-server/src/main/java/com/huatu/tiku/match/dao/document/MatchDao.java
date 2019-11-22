package com.huatu.tiku.match.dao.document;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.match.common.MatchConfig;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.tiku.match.util.Page;
import com.huatu.tiku.match.util.PageUtil;
import com.huatu.ztk.paper.bean.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * @author biguodong
 * Create time 2018-10-16 下午1:28
 **/

@Repository
@Slf4j
public class MatchDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MatchConfig matchConfig;


    /**
     * 查询大赛信息
     *
     * @param paperId
     * @return
     */
    public Match findById(int paperId) {
        return mongoTemplate.findById(paperId, Match.class);
    }


    public List<Match> findByIds(List<Integer> ids){
        Criteria criteria = Criteria.where("_id").in(ids);
        Query query = new Query(criteria);
        return mongoTemplate.find(query,Match.class);
    }
    /**
     * 根据 subject 获取模考大赛列表
     *
     * @param subject
     * @return
     */
    public List<Match> findMatchBySubject(int subject) {

        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchInfoEnum.BackendStatusEnum.AUDIT_SUCCESS.getKey());
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "startTime"));

        List<Match> list = mongoTemplate.find(query, Match.class);
        return list;
    }

    public Page<Match> findMatchBySubjectPageable(int subject, @NotNull Pageable pageable) {

        Criteria criteria = Criteria.where("subject").is(subject)
                .and("status").is(MatchInfoEnum.BackendStatusEnum.AUDIT_SUCCESS.getKey());
        Query query = new Query(criteria).with(pageable);
        query.with(new Sort(Sort.Direction.ASC, "startTime"));

        List<Match> list = mongoTemplate.find(query, Match.class);
        PageImpl<Match> pageImpl = (PageImpl<Match>) PageableExecutionUtils.getPage(list, pageable, () -> mongoTemplate.count(query, Match.class));
       // log.info(JSONObject.toJSONString(pageImpl));
        Page<Match> pageInfo = PageUtil.parseMongoPageInfo(pageImpl);
        return pageInfo;
    }

    /**
     * 根据 subject 获取模考大赛列表
     *
     * @param subject
     * @param tagId
     * @return
     */
    public List<Match> findBySubjectAndTag(int subject, int tagId) {

        Criteria criteria = Criteria.where("subject").is(subject)
                .and("tag").is(tagId)
                .and("status").is(MatchInfoEnum.BackendStatusEnum.AUDIT_SUCCESS.getKey());
        Query query = new Query(criteria);
//        query.with(new Sort(Sort.Direction.ASC, "startTime")).limit(10);
        List<Match> matches = mongoTemplate.find(query, Match.class);
        return matches;
    }

    public List<Match> findAll() {
        return mongoTemplate.findAll(Match.class);
    }


    /**
     * 活动范围内的考试
     *
     * @return
     */
    public List<Match> findUsefulMatch() {
        Criteria criteria = Criteria
                .where("endTime").lt(System.currentTimeMillis())
                .and("status").is(MatchInfoEnum.BackendStatusEnum.AUDIT_SUCCESS.getKey());
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "startTime"))
                .with(new Sort(Sort.Direction.ASC, "_id"));

        log.info("query={}", query);
        return mongoTemplate.find(query, Match.class);
    }

}
