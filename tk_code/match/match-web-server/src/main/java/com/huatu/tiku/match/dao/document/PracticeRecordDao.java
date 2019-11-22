package com.huatu.tiku.match.dao.document;

import com.huatu.ztk.paper.bean.PaperUserMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 练习记录dao层
 * Created by 李建迎
 * Created time 2016-05-16
 */
@Slf4j
@Repository
public class PracticeRecordDao {
    public static final int ID_BASE = 2000000;//id基数，防止跟以前的id冲突
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 插入练习记录
     *
     * @param paperUserMeta
     */
    public void insert(PaperUserMeta paperUserMeta) {
        log.info("insert practiceRecord:{}", paperUserMeta);
        mongoTemplate.insert(paperUserMeta);
    }

    /**
     * 根据id，批量查询
     * @param ids id列表
     * @return
     */
    public List<PaperUserMeta> findByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {//空直接返回
            return new ArrayList<>();
        }

        final Criteria criteria = Criteria.where("_id").in(ids);
        final List<PaperUserMeta> paperUserMetas = mongoTemplate.find(new Query(criteria), PaperUserMeta.class);
        return paperUserMetas;
    }

    public PaperUserMeta findById(String id) {
        return mongoTemplate.findById(id, PaperUserMeta.class);
    }

    public void save(PaperUserMeta paperUserMeta) {
        mongoTemplate.save(paperUserMeta);
    }
}
