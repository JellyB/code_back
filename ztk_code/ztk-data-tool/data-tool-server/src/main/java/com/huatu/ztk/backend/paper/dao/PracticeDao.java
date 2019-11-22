package com.huatu.ztk.backend.paper.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.bean.TikuQuestionType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by aatrox on 2017/3/6.
 */
@Repository
public class PracticeDao {
    private static final Logger logger = LoggerFactory.getLogger(PaperDao.class);
    public static final String collection = "ztk_paper";//试卷集合
    public static final int PAPER_LIST_LIMIT = 200;
    public static final String collectionExtend = "question_extend";
    public static final String collectionQuestion = "ztk_question_new";
    public static final int QUESTION_LIST_LIMIT = 200;
    public static final int AUDIT_SUCCESS_NOT_PUBLISH = 5; //审核未发布
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存试卷
     *
     * @param paper
     */

    public void createPaper(Paper paper) {
        logger.info("create paper={}", JsonUtil.toJson(paper));
        mongoTemplate.insert(paper);
    }

    /**
     * 获取试卷列表
     *
     * @param catgorys
     * @param areas
     * @param year
     * @param name
     * @param type
     * @param onStatus
     * @param createTime
     * @return
     */
    public List<EstimatePaper> list(List<Integer> catgorys, List<Integer> areas, int year,
                                    String name, int type, int onStatus, long createTime, int creator) {
        final Criteria criteria = Criteria.where("status").ne(BackendPaperStatus.DELETED);
        if (type > 0) {
            criteria.and("type").in(Lists.newArrayList(PaperType.CUSTOM_PAPER, PaperType.REGULAR_PAPER, PaperType.MATCH));
        }

        if (creator > 0) {
            criteria.and("createdBy").is(creator);
        }

        if (CollectionUtils.isNotEmpty(catgorys)) {
            criteria.and("catgory").in(catgorys);
        }
        if (CollectionUtils.isNotEmpty(areas)) {
            criteria.and("area").in(areas);
        }
        if (year > 0) {
            criteria.and("year").is(year);
        }
        if (StringUtils.isNoneBlank(name)) {
            criteria.and("name").regex(".*" + name + ".*");
        }
        if (createTime > 0) {
            criteria.andOperator(Criteria.where("createTime").lte(new Date(createTime + 28800000 * 3)), Criteria.where("createTime").gte(new Date(createTime)));
        }
        long time = new Date().getTime();
        logger.info("time  :{} ", time);
        if (onStatus > 90) {
            onStatus = 0;
        }
        if (onStatus == BackendPaperStatus.OFFLINE) {//下线
            criteria.and("offlineTime").lt(time);
        } else if (onStatus == BackendPaperStatus.BEFORE_ONLINE) {//未上线
            criteria.and("onlineTime").gt(time);
        } else if (onStatus == BackendPaperStatus.ONLINE) {//上线
            criteria.andOperator(Criteria.where("onlineTime").lte(time), Criteria.where("offlineTime").gte(time));
        }
        Query query = new Query(criteria);
        query.limit(PAPER_LIST_LIMIT);

        //按年份排序
        query.with(new Sort(Sort.Direction.DESC, "createTime"));

        System.out.println(query.toString());
        logger.info("findAll PracticePaper :{} ", query.toString());
        final List<EstimatePaper> papers = mongoTemplate.find(query, EstimatePaper.class);
        return papers;
    }

    //根据试卷id,获取模拟试卷
    public EstimatePaper findById(int id) {
        return mongoTemplate.findById(id, EstimatePaper.class);
    }


    /**
     * 根据模块id查找该模块下试题扩展对象集合
     *
     * @param moduleId
     * @return
     */
    public List<QuestionExtend> findExtendByModule(int moduleId) {
        Criteria criteria = Criteria.where("moduleId").is(moduleId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, QuestionExtend.class, collectionExtend);
    }

    /**
     * 根据试题qid集合查找该模块下试题扩展对象集合
     *
     * @param qids
     * @return
     */
    public List<QuestionExtend> findExtendByQids(List<Integer> qids) {
        Criteria criteria = Criteria.where("qid").in(qids);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, QuestionExtend.class, collectionExtend);
    }

    //根据试卷pid 获取其中所有试题扩展信息
    public List<QuestionExtend> findExtendByPid(int pid) {
        Criteria criteria = Criteria.where("paperId").in(pid);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, QuestionExtend.class, collectionExtend);
    }

    //模考试卷从数据库选题，根据知识点条件进行查找试题
    public List<Question> findQuestions(int catgory, List<Integer> ids, String pointName, int difficult, int mode, String stem, int questionType) {
        List<Question> questions = Lists.newArrayList();
        Criteria criteria = Criteria.where("subject").is(catgory);
        criteria.and("status").in(Arrays.asList(QuestionStatus.AUDIT_SUCCESS, AUDIT_SUCCESS_NOT_PUBLISH));
        if (CollectionUtils.isNotEmpty(ids)) {
            criteria.and("id").in(ids);
        }
        if (StringUtils.isNotEmpty(pointName)) {
            criteria.and("pointsName").all(pointName);
        }
        if (difficult > 0) {
            criteria.and("difficult").is(difficult);
        }
        if (mode > 0) {
            criteria.and("mode").is(mode);
        }
        if (StringUtils.isNotEmpty(stem)) {//题干或者材料中含有关键字
            criteria.orOperator(Criteria.where("stem").regex(".*" + stem + ".*"), Criteria.where("material").regex(".*" + stem + ".*"));
        }
        Query query = new Query(criteria);
        query.limit(QUESTION_LIST_LIMIT);
        //按年份排序
        criteria.orOperator(Criteria.where("parent").is(0), Criteria.where("parent").exists(false));
        query.with(new Sort(Sort.Direction.DESC, "createTime"));
        if (questionType == TikuQuestionType.SINGLE_OBJECTIVE) {
            criteria.and("type").in(Arrays.asList(99, 100, 101, 109));
        } else if (questionType == TikuQuestionType.MULTI_OBJECTIVE) {
            criteria.and("type").in(Arrays.asList(QuestionType.COMPOSITED));
        } else if (questionType == TikuQuestionType.SINGLE_SUBJECTIVE) {
            criteria.and("type").in(Arrays.asList(106));
        } else if (questionType == TikuQuestionType.MULTI_SUBJECTIVE) {
            criteria.and("type").in(Arrays.asList(107));
        }
        List<Question> genericQuestions = mongoTemplate.find(query, Question.class, collectionQuestion);
        questions.addAll(genericQuestions);
        return questions;
    }

    //模考试卷从数据库选题，根据试卷条件进行查找试题
    public List<Question> getQuestions(List<Integer> qidList, int catgory, int area, int difficult, int year, String stem, int questionType) {
        List<Question> questions = Lists.newArrayList();
        Criteria criteria = Criteria.where("subject").is(catgory);
        if (CollectionUtils.isNotEmpty(qidList)) {
            criteria.and("id").in(qidList);
        }
        if (difficult > 0) {
            criteria.and("difficult").is(difficult);
        }
        if (area > 0 || area == -9) {
            criteria.and("area").is(area);
        }
        if (year > 0) {
            criteria.and("year").is(year);
        }
        if (StringUtils.isNotEmpty(stem)) {//题干或者材料中含有关键字
            criteria.orOperator(Criteria.where("stem").regex(".*" + stem + ".*"), Criteria.where("material").regex(".*" + stem + ".*"));
        }
        Query query = new Query(criteria);
        query.limit(QUESTION_LIST_LIMIT);
        //按年份排序
        criteria.orOperator(Criteria.where("parent").is(0), Criteria.where("parent").exists(false));
        query.with(new Sort(Sort.Direction.DESC, "createTime"));
        if (questionType == TikuQuestionType.SINGLE_OBJECTIVE) {
            criteria.and("type").in(Arrays.asList(99, 100, 101, 109));
        } else if (questionType == TikuQuestionType.MULTI_OBJECTIVE) {
            criteria.and("type").in(Arrays.asList(105));
        } else if (questionType == TikuQuestionType.SINGLE_SUBJECTIVE) {
            criteria.and("type").in(Arrays.asList(106));
        } else if (questionType == TikuQuestionType.MULTI_SUBJECTIVE) {
            criteria.and("type").in(Arrays.asList(107));
        }
        List<Question> genericQuestions = mongoTemplate.find(query, Question.class, collectionQuestion);
        questions.addAll(genericQuestions);
        return questions;
    }

    public List<Paper> queryPaper(int area, int year, int catgory) {
        Criteria criteria = Criteria.where("catgory").is(catgory);
        criteria.and("status").ne(BackendPaperStatus.DELETED);
        if (year > 0) {
            criteria.and("year").is(year);
        }
        if (area > 0 || area == -9) {
            criteria.and("area").is(area);
        }
        Query query = new Query(criteria);
        //按年份排序
        query.with(new Sort(Sort.Direction.DESC, "createTime"));
        return mongoTemplate.find(query, Paper.class, collection);
    }

    public List<Question> findAllQuestion(List<Integer> qids) {
        if (CollectionUtils.isNotEmpty(qids)) {
            Criteria criteria = Criteria.where("id").in(qids);
            Query query = new Query(criteria);
            return mongoTemplate.find(query, Question.class, collectionQuestion);
        }
        return Lists.newArrayList();
    }
}
