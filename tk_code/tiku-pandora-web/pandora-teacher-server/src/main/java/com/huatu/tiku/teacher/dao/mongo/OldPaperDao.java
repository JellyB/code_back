package com.huatu.tiku.teacher.dao.mongo;

import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by huangqp on 2018\7\6 0006.
 */
@Repository
public class OldPaperDao {
    /**
     * 存储试题的集合名字
     */
    public static final String collection = "ztk_paper";
    public static final int PAPER_LIST_LIMIT = 20000;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 获取试卷详情
     *
     * @param id
     * @return
     */
    public Paper findById(int id) {
        return mongoTemplate.findById(id, Paper.class);
    }

    public void save(Paper paper) {
        mongoTemplate.save(paper);
    }

    public List<Paper> findAll() {
        return mongoTemplate.findAll(Paper.class);
    }

    public List<Paper> findByTypeAndSubject(int subject,int type) {
        final Criteria criteria = Criteria.where("catgory").in(subject)
                .and("type").is(type)
                .and("status").is(PaperStatus.AUDIT_SUCCESS);
        return mongoTemplate.find(new Query(criteria), Paper.class);
    }
}

