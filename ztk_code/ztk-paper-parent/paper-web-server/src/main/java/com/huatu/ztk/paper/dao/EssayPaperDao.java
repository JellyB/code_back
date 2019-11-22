package com.huatu.ztk.paper.dao;

import com.huatu.ztk.paper.bean.EssayPaper;
import com.huatu.ztk.paper.bean.EstimateEssayPaper;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.common.PaperStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EssayPaperDao {

    private static final Logger logger = LoggerFactory.getLogger(EssayPaperDao.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final int MOCK_TYPE = 0;
    private static final int TRUE_TYPE = 1;

    private static final String essay_paper_collection = "ztk_essay_paper";


    public List<EssayPaper> findAll(){
        List<EssayPaper> all = mongoTemplate.findAll(EssayPaper.class);
        return all;
    }

    /**
     *
     * @param type
     * @return
     */
    public List<EssayPaper> findByType(int type){
        final Criteria criteria = Criteria.where("type").is(type)
                .and("status").is(PaperStatus.AUDIT_SUCCESS);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "_id"));
        return mongoTemplate.find(query, EssayPaper.class);
    }

    public List<EstimateEssayPaper> findMockList() {
        final Criteria criteria = Criteria.where("type").is(MOCK_TYPE)
                .and("status").is(PaperStatus.AUDIT_SUCCESS);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, "startTime"));
        return mongoTemplate.find(query, EstimateEssayPaper.class);
    }

    public List<EssayPaper> findBTruePaper() {
        return findByType(TRUE_TYPE);
    }
}
