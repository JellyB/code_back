package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.user.bean.UserConfig;
import com.mongodb.WriteResult;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/10/22
 * @描述
 */
public class cleanUserConfigTest extends BaseTest {


    private static final Logger logger = LoggerFactory.getLogger(cleanUserConfigTest.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 纠正UserConfig 表中错误的考试类型和科目
     * 第一种,分为两种类型,一种是category ,已经不存在的，直接删除
     * 第二种,subject是3的,纠正为职测A
     */

    @Test
    public void cleanUserConfig() {
        //线上考试类别
        List<Integer> categoryIds = Lists.newArrayList(1, 3, 41, 42, 43, 100100633, 200100000, 200100002, 200100045, 200100046, 200100047, 200100048, 200100053, 200100058, 200100059, 200100060);
        Criteria criteria = Criteria.where("category").nin(categoryIds);
        Query query = new Query(criteria);
        WriteResult remove = mongoTemplate.remove(query, UserConfig.class);
    }


    /**
     * 修正职测科目原来科目是3,但是现在职测分为了ABCD类;更改规则,更改为职测A的subjectId
     * 输入新旧科目和考试类别，UserConfig中批量修正
     */
    @Test
    public void changeUserConfigSubject() {

        // 教师招聘 旧科目100100221,新科目是100100262，考试类别是200100045
        // 职测A的 旧科目 3(职测) ,新科目是 200100054(职测A的subjectId),考试类别是3(事业单位)
        Integer newSubjectId = 100100262;
        Integer oldSubjectId = 100100221;
        Integer category = 200100045;
        Criteria criteria = Criteria.where("subject").is(oldSubjectId).and("category").is(category);
        Query query = new Query(criteria);
        List<UserConfig> userConfigs = mongoTemplate.find(query, UserConfig.class);
        if (CollectionUtils.isNotEmpty(userConfigs)) {
            logger.info("需要纠正的用户配置数量是:{}", userConfigs.size());
            Update update = Update.update("subject", newSubjectId);
            mongoTemplate.updateMulti(query, update, UserConfig.class);
        }
    }
}
