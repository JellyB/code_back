package com.huatu.ztk.backend.paper.controller;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.bean.ModuleBean;
import com.huatu.ztk.backend.paper.dao.PaperQuestionDao;
import com.huatu.ztk.backend.paper.service.PaperService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.bean.*;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 数据导入/初始化
 * Created by linkang on 3/16/17.
 */

@RestController
public class InitController {
    private static final Logger logger = LoggerFactory.getLogger(InitController.class);


    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperQuestionDao paperQuestionDao;


    /**
     * 初始化questionextend
     */
    @RequestMapping(value = "init_questionExtend")
    public void initQuestionExtend() {
        Criteria criteria = Criteria.where("status").ne(BackendPaperStatus.DELETED);

        List<Paper> papers = mongoTemplate.find(new Query(criteria), Paper.class);

        for (Paper paper : papers) {
            List<ModuleBean> moduleBeanList = paperService.getModuleBeanList(paper);


            for (ModuleBean bean : moduleBeanList) {
                Map<Integer, Integer> indexQidMap = bean.getQuestions();

                if (MapUtils.isEmpty(indexQidMap)) {
                    continue;
                }

                for (Integer index : indexQidMap.keySet()) {

                    int qid = indexQidMap.get(index);

                    logger.info("current pid={},qid={}", paper.getId(), qid);

                    QuestionExtend oldQe = paperQuestionDao.findExtendById(qid);

                    if (oldQe != null) {
                        continue;
                    }

                    QuestionExtend questionExtend = QuestionExtend.builder()
                            .sequence(index)
                            .moduleId(bean.getId())
                            .paperId(paper.getId())
                            .qid(qid)
                            .build();
                    mongoTemplate.save(questionExtend);

                    Question question = paperQuestionDao.findQuestionById(qid);

                    if (question instanceof GenericQuestion) {
                        GenericQuestion genericQuestion = (GenericQuestion) question;

                        int parent = genericQuestion.getParent();

                        QuestionExtend old = paperQuestionDao.findExtendById(parent);

                        if (parent != 0 && old == null) {
                            logger.info("current pid={},qid={}", paper.getId(), parent);
                            QuestionExtend parentQe = QuestionExtend.builder()
                                    .sequence(index)
                                    .moduleId(bean.getId())
                                    .paperId(paper.getId())
                                    .qid(parent)
                                    .build();
                            mongoTemplate.save(parentQe);
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化试卷年份
     */
    @RequestMapping("estimate_year")
    public void initYear() {
        Criteria criteria = Criteria.where("year").is(0);

        criteria.orOperator(Criteria.where("type").is(PaperType.ESTIMATE_PAPER),
                Criteria.where("type").is(PaperType.CUSTOM_PAPER));

        List<Paper> papers = mongoTemplate.find(new Query(criteria), Paper.class);

        for (Paper paper : papers) {
            //取试卷创建时间的年份
            String year = DateFormatUtils.format(paper.getCreateTime(), "yyyy");
            paper.setYear(Integer.valueOf(year));
            mongoTemplate.save(paper);
        }
    }

    /**
     * 初始化试题材料
     */
    @RequestMapping(value = "init_materials")
    public void initMaterials() {
        Criteria criteria = Criteria.where("parent").ne(0);

        List<Question> questions = mongoTemplate.find(new Query(criteria), Question.class,"ztk_question_new");

        Set<Integer> parentIds = new HashSet<>();

        for (Question question : questions) {

            if (!(question instanceof GenericQuestion)) {
                continue;
            }

            GenericQuestion child = (GenericQuestion) question;
            int pid = child.getParent();
            Question parent = paperQuestionDao.findQuestionById(pid);

            if (parent != null && parent instanceof CompositeQuestion) {

                //子题设置Materials
                question.setMaterials(Lists.newArrayList(parent.getMaterial()));

                //如果父题没有处理
                if (!parentIds.contains(parent.getId())) {
                    //父题设置Materials
                    parent.setMaterials(Lists.newArrayList(parent.getMaterial()));
                    logger.info("save parent,qid={},obj={}", question.getId(), JsonUtil.toJson(question));
                    mongoTemplate.save(parent);
                }
            }

            logger.info("save qid={},obj={}", question.getId(), JsonUtil.toJson(question));
            mongoTemplate.save(question);
        }
    }
}
