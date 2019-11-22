package com.huatu.tiku.match.dao.document;

import com.google.common.collect.Lists;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-25 下午6:36
 **/
@Repository
@Slf4j
public class PaperDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 通过ID 查询一张试卷
     *
     * @param paperId 试卷ID
     * @return 试卷信息
     */
    public Paper findPaperById(Integer paperId) {
        return mongoTemplate.findById(paperId, Paper.class);
    }

    /**
     * 根据 paperIds 批量查询
     *
     * @param paperIds
     * @return
     */
    public List<Paper> findBatchByIds(Set<Integer> paperIds) {
        final Criteria criteria = Criteria
                .where("_id").in(paperIds);
        Query query = new Query(criteria);
        List<Paper> papers = mongoTemplate.find(query, Paper.class);
        if (CollectionUtils.isEmpty(paperIds)) {
            return Lists.newArrayList();
        }
        return papers;
    }


    /**
     * 根据 id 批量查询
     *
     * @param paperIds
     * @return
     */
    public List<PaperUserMeta> findPaperUserMetaByIds(List<String> paperIds) {
        if (CollectionUtils.isEmpty(paperIds)) {
            return Lists.newArrayList();
        }
        Criteria criteria = Criteria
                .where("_id")
                .in(paperIds);
        List<PaperUserMeta> paperUserMetas = mongoTemplate.find(new Query(criteria), PaperUserMeta.class);
        return paperUserMetas;
    }

}
