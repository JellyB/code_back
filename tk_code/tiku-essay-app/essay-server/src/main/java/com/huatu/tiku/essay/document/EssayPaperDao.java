package com.huatu.tiku.essay.document;

import com.huatu.ztk.paper.bean.EssayPaper;
import com.huatu.ztk.paper.common.PaperStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class EssayPaperDao {

    @Autowired
    private MongoTemplate mongoTemplate;

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
    public List<EssayPaper> findBySubjectAndType(int type){
        final Criteria criteria = Criteria.where("type").is(type)
                .and("status").is(PaperStatus.AUDIT_SUCCESS);
        return mongoTemplate.find(new Query(criteria), EssayPaper.class);
    }

    public void save(EssayPaper essayPaper){
        log.info("estimateEssayPaperInfo={}",essayPaper);
        mongoTemplate.save(essayPaper,essay_paper_collection);
    }

}
