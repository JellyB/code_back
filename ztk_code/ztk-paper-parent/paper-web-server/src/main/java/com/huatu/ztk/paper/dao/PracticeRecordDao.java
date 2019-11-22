package com.huatu.ztk.paper.dao;

import com.google.common.collect.Maps;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.sql.ParameterMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 练习记录dao层
 * Created by 李建迎
 * Created time 2016-05-16
 */

@Repository
public class PracticeRecordDao {
    private static final Logger logger = LoggerFactory.getLogger(PracticeRecordDao.class);
    public static final int ID_BASE = 2000000;//id基数，防止跟以前的id冲突
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 插入练习记录
     *
     * @param paperUserMeta
     */
    public void insert(PaperUserMeta paperUserMeta) {
        logger.info("insert practiceRecord:{}", paperUserMeta);
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

    public PaperUserMeta findByPaperIdAndSyllabusId(long uid ,int paperId,long syllabusId){
        final Criteria criteria=Criteria.where("uid").is(uid).and("paperId").is(paperId).and("syllabusId").is(syllabusId);
        return mongoTemplate.findOne(new Query(criteria),PaperUserMeta.class);
    }

    /**
     * 删除paperUserMeta
     * @param id
     */
    public void delete(String id){
        PaperUserMeta paperUserMeta = mongoTemplate.findById(id, PaperUserMeta.class);
        if(null != paperUserMeta){
            mongoTemplate.remove(paperUserMeta);
        }else{
            logger.error("数据不存在:{}", id);
        }
    }
}
