package com.huatu.ztk.paper.dao;

import com.huatu.ztk.paper.bean.PaperSyllabus;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;


/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/21
 * @描述
 */
@Repository
public class PaperSyllabusDao {

    public static final org.slf4j.Logger log = LoggerFactory.getLogger(PaperSyllabusDao.class);
    /**
     * 存储的表
     */
    private static final String collection = "ztk_paper_syllabus";

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存
     *
     * @param paperSyllabus
     */
    public void save(PaperSyllabus paperSyllabus) {
        mongoTemplate.save(paperSyllabus);
    }


}
