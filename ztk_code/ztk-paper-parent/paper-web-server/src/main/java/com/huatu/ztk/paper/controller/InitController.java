package com.huatu.ztk.paper.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.EstimateConstants;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.dao.PaperDao;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionMode;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据迁移接口
 * Created by shaojieyue
 * Created time 2016-04-28 13:29
 */

@RestController
public class InitController {
    private static final Logger logger = LoggerFactory.getLogger(InitController.class);

    @Autowired
    private PaperDao paperDao;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;


    /**
     * 导入针对题试卷
     *
     * @param paperId
     * @return
     */
    @RequestMapping("/import2mongo")
    public Object import2mongo(@RequestParam(defaultValue = "-1") int paperId, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (true) {
            return null;
        }
        try {
            boolean more = true;
            String sql = "SELECT * FROM v_pastpaper_info where bl_exam_sub=1";
            if (paperId > 0) {
                sql = "SELECT * FROM v_pastpaper_info where bl_exam_sub=1 and pukey=" + paperId;
            }
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
            more = importPaper2mongo(sqlRowSet, true);
            System.out.println("====>处理完毕");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Map map = new HashMap();
        map.put("message", "处理完毕");
        return map;
    }

    /**
     * 导入模拟题试卷
     *
     * @param paperId
     * @return
     */
    @RequestMapping("/importTestPaper2mongo")
    public Object importTestPaper2mongo(@RequestParam(defaultValue = "-1") int paperId,HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (true) {
            return null;
        }
        try {
            boolean more = true;

            String sql = "select a.testpaper_id,a.bl_act,a.test_area,b.name,b.total_point,a.bb106," +
                    "b.pass_point,b.limit_time,b.tactics,a.start_time,a.end_time," +
                    "b.BB103,b.BB105,b.BB102,b.BB1B1,a.look_parse_time,b.descrp,b.fb1z3,b.EB1B1 " +
                    "from v_modetest_paper_info as a,v_testpaper_info as b" +
                    " where a.testpaper_id=b.PUKEY";

            if (paperId > 0) {
                sql = "select a.testpaper_id,a.bl_act,a.test_area,b.name,b.total_point,a.bb106," +
                        "b.pass_point,b.limit_time,b.tactics,a.start_time,a.end_time," +
                        "b.BB103,b.BB105,b.BB102,b.BB1B1,a.look_parse_time,b.descrp,b.fb1z3,b.EB1B1 " +
                        "from v_modetest_paper_info as a,v_testpaper_info as b" +
                        " where a.testpaper_id=b.PUKEY and b.PUKEY=" + paperId;
            }
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
            more = importPaper2mongo(sqlRowSet, false);
            System.out.println("====>处理完毕");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Map map = new HashMap();
        map.put("message", "处理完毕");
        return map;
    }

    /**
     * @param resultSet
     * @param truePaper 是否是真题
     * @return
     * @throws SQLException
     * @throws IOException
     */
    private boolean importPaper2mongo(SqlRowSet resultSet, boolean truePaper) throws SQLException, IOException {
        while (resultSet.next()) {
            int id = 0;
            try {
                String past_year = null;
                String past_area = null;
                String name = null;
                int type = 0;
                long startTime = 0;
                long endTime = 0;
                long onlineTime = 0;
                long offlineTime = 0;
                //默认为立即查看
                int lookParseTime = 1;

                String url = "";
                String descrp = "";

                if (truePaper) {//真题
                    id = resultSet.getInt("PUKEY");
                    past_year = resultSet.getString("past_year");
                    past_area = resultSet.getString("past_area");
                    name = resultSet.getString("pastpaper_name");
                } else {//模拟题
                    id = Integer.valueOf("200" + Strings.padStart(resultSet.getInt("testpaper_id") + "", 4, '0'));
                    past_year = "0";
                    past_area = resultSet.getString("test_area");
                    type = Integer.valueOf(resultSet.getString("bl_act"));
                    name = resultSet.getString("name");

                    //时间转换成毫秒
                    startTime = Long.valueOf(resultSet.getString("start_time")) * 1000;
                    endTime = Long.valueOf(resultSet.getString("end_time")) * 1000;

                    lookParseTime = resultSet.getInt("look_parse_time");
                    //没有设置查看报告时间的设置成立即查看
                    if (lookParseTime == 0) {
                        lookParseTime = 1;
                    }

                    url = resultSet.getString("fb1z3");
                    descrp = resultSet.getString("descrp");
                    String offlineTimeStr = resultSet.getString("EB1B1");
                    if (StringUtils.isBlank(offlineTimeStr)) {
                        offlineTimeStr = "0";
                    }
                    offlineTime = Long.valueOf(offlineTimeStr) * 1000;
                    onlineTime = resultSet.getLong("bb106") * 1000;
                }

                logger.info("proccess pid={}", id);

                String total_point = resultSet.getString("total_point");
                String pass_point = resultSet.getString("pass_point");
                int total_question = 0;
                String limit_time = resultSet.getString("limit_time");
                String tactics = resultSet.getString("tactics");
                String BB103 = resultSet.getString("BB103");//创建时间
                String BB105 = resultSet.getString("BB105");//创建者
                int BB102 = Integer.valueOf(resultSet.getString("BB102"));//有效标识
                int BB1B1 = Integer.valueOf(resultSet.getString("BB1B1"));//审核标识
                Paper paper = null;
                if (truePaper) {
                    paper = new Paper();
                    paper.setType(PaperType.TRUE_PAPER);
                } else {
                    paper = new EstimatePaper();

                    ((EstimatePaper) paper).setHideFlag(EstimateConstants.NOT_HIDE);

                    ((EstimatePaper) paper).setStartTime(startTime);
                    ((EstimatePaper) paper).setEndTime(endTime);

                    //发布时间为0时，取创建时间
                    ((EstimatePaper) paper).setOnlineTime(onlineTime > 0 ? onlineTime : Long.valueOf(BB103) * 1000);

                    //下线时间没有设置时，取结束时间
                    ((EstimatePaper) paper).setOfflineTime(offlineTime > 0 ? offlineTime : endTime);

                    ((EstimatePaper) paper).setUrl(StringUtils.trimToEmpty(url));
                    ((EstimatePaper) paper).setDescrp(StringUtils.trimToEmpty(descrp));
                    ((EstimatePaper) paper).setLookParseTime(lookParseTime);

                    if (type == PaperType.ESTIMATE_PAPER) {  //估分试卷
                        paper.setType(type);
                    } else if (type == PaperType.CUSTOM_PAPER) {  //万人模考
                        paper.setType(type);
                    } else if (type == 0) {  //定期模考
                        paper.setType(PaperType.REGULAR_PAPER);
                    } else {
                        //其他活动的试卷不导入
                        continue;
                    }
                }
                paper.setId(id);
                paper.setYear(Integer.valueOf(past_year));
                paper.setName(name);
                paper.setArea(Integer.valueOf(past_area));
                paper.setScore(Integer.valueOf(total_point));
                paper.setPassScore(Integer.valueOf(pass_point));
                paper.setCatgory(1);
                Integer time = Ints.tryParse(limit_time);
                if (time == null || time == 0) {//默认120分钟
                    time = 120;
                }
                paper.setTime(time * 60);//转换为秒
                paper.setCreateTime(new Date(Integer.valueOf(BB103) * 1000L));
                List<Integer> ids = new ArrayList();
                List<Integer> multiIds = new ArrayList();
                int status = 0;

                // bb102=1,bb1b1=1为有效模考卷
                if (BB102 < 1) {//删除
                    status = PaperStatus.DELETED;
                } else if (BB1B1 > 0) {//审核通过
                    status = PaperStatus.AUDIT_SUCCESS;
                } else if (BB102 > 0 && BB1B1 < 1) {//新建状态
                    status = PaperStatus.CREATED;
                } else {//审核失败
                    status = PaperStatus.AUDIT_REJECT;
                }

                if (Strings.isNullOrEmpty(tactics)) {
//                System.out.println("没有处理 id="+id);
//                continue;
                    tactics = "[]";
                }
                final JsonNode jsonNode = Jackson2ObjectMapperBuilder.json().build().readTree(tactics);
                List<Module> modules = new ArrayList<Module>();
                if (jsonNode.isArray()) {//遍历模块
                    final ArrayNode arr = (ArrayNode) jsonNode;
                    for (JsonNode node : arr) {
                        Module module = Module.builder().build();
                        final int moduleId = node.get("moduleId").asInt();
                        final String moduleName = node.get("moduleName").asText();
                        module.setCategory(moduleId);
                        module.setName(moduleName);
                        final ArrayNode itemsList = (ArrayNode) node.get("itemsList");
                        int qcount = 0;
                        for (JsonNode jsonNode1 : itemsList) {
                            final String qId = jsonNode1.get("qId").asText();
                            int mmid = 0;
                            if (qId.startsWith("o")) {
                                mmid = Integer.valueOf(qId.substring(1));
                                if (mmid > 0) {
                                    ids.add(mmid);
                                    qcount++;
                                }
                            } else if (qId.startsWith("m")) {//复合题
                                final ArrayNode qSubList = (ArrayNode) jsonNode1.get("qSubList");
                                int multiId =  Integer.valueOf(qId.substring(1));
                                multiIds.add(multiId);
                                for (JsonNode jsonNode2 : qSubList) {
                                    mmid = Integer.valueOf(jsonNode2.asText().substring(1));
                                    if (mmid > 0) {
                                        ids.add(mmid);
                                        qcount++;
                                    }
                                }
                            } else {
                                System.out.println("--->不知道的qid=" + qId);
                            }

                        }
                        module.setQcount(qcount);
                        modules.add(module);
                        total_question = total_question + qcount;
                    }
                }
                paper.setModules(modules);
                paper.setStatus(status);
                paper.setQcount(total_question);
                paper.setCreatedBy(Integer.valueOf(BB105));
                paper.setQuestions(ids);
                logger.info("qcount={}",total_question);
                //试题数为0，或question列表为空
                if (paper.getQcount() == 0 || CollectionUtils.isEmpty(paper.getQuestions())) {
                    continue;
                }

                if (paper.getQuestions() != null) {
                    //查询到所有试题
                    List<Question> questions = questionDubboService.findBath(paper.getQuestions());
                    if (CollectionUtils.isEmpty(questions)) {
                        continue;
                    } else {
                        //如果试题全部为null，删除试卷
                        int nullCount = 0;
                        for (Question question : questions) {
                            if (question == null) {
                                nullCount++;
                            }
                        }
                        if (nullCount == questions.size()) {
                            logger.warn("all questions are null, paperid={},name={}", paper.getId(), paper.getName());
                            continue;
                        }
                    }
                }

                paperDao.save(paper);

                multiIds.addAll(ids);
                //更新题目来源
                for (Integer questionId : multiIds) {
                    try {
                        final Question question = questionDubboService.findById(questionId);
                        if (question != null) {

                            //如果已经设置来源，不再进行设置
//                            if (StringUtils.isNoneBlank(question.getFrom())) {
//                                continue;
//                            }
                            //如果没有更新来源，则不处理
                            logger.info("question={}",question.getId());
                            if (paper.getName() == null || paper.getName().equals(question.getFrom())) {
                                continue;
                            }
                            //设置来源(如果用试题时真题但是试卷是模拟题则跳过不做处理)
                            if(question.getMode()==1&&!truePaper){
                                continue;
                            }
                            question.setFrom(paper.getName());
                            //只有当试卷的属性时真题的时候，更新试卷状态为真题;模拟试卷的题目状态，在试题创建时会被赋予，之后不会变动
                            if (truePaper) {//设置题的模式
                                question.setMode(QuestionMode.QUESTION_TRUE);
                            }
//                            else {
//                                question.setMode(QuestionMode.QUESTION_SIMULATION);
//                            }
                            try {
                                questionDubboService.update(question);
                            } catch (IllegalQuestionException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        logger.error("ex", e);
                    }

                }
            } catch (Exception e) {
                logger.error("ex,paperId={},truePaper={}", id, truePaper, e);
            }
        }
        return true;
    }
}
