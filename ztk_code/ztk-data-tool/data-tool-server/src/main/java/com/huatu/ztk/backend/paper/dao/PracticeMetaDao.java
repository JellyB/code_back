package com.huatu.ztk.backend.paper.dao;

import com.huatu.ztk.paper.bean.PaperUserMeta;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/3/29
 * @描述 查询用户做题情况
 */

@Repository
public class PracticeMetaDao {

    private static final Logger logger = LoggerFactory.getLogger(PaperDao.class);
    public static final String collection = "ztk_paper_user_meta";//试卷集合

    @Autowired
    MongoTemplate mongoTemplate;


    public List<Long> getPracticeIs(List<Integer> paperId) {
        Criteria criteria = Criteria.where("paperId").in(paperId).and("currentPracticeId").lte(0L);
        Query query = new Query(criteria);
        List<PaperUserMeta> list = mongoTemplate.find(query, PaperUserMeta.class);
        List<Long> practices = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            list.stream().forEach(paperUserMeta -> {
                List<Long> practiceIds = paperUserMeta.getPracticeIds();
                if (CollectionUtils.isNotEmpty(practiceIds)) ;
                practices.add(practiceIds.get(0));
            });
        }
        logger.info("答题记录ID是:{}", practices.size());
        return practices;
    }
}
