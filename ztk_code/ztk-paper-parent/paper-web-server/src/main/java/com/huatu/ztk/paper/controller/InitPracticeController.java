package com.huatu.ztk.paper.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.huatu.ztk.commons.ModuleConstants;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.RabbitMqConstants;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.PracticePointsSummaryDao;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.paper.service.PaperUserMetaService;
import com.huatu.ztk.paper.service.PracticeCardService;
import com.huatu.ztk.paper.util.GzipUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionCorrectType;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.self.generator.core.WaitException;
import com.yxy.ssdb.client.SsdbConnection;
import com.yxy.ssdb.client.pool.SsdbPooledConnectionFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shaojieyue
 * Created time 2016-05-09 18:23
 */

@RestController
public class InitPracticeController extends BaseInitController {
    private static final Logger logger = LoggerFactory.getLogger(InitPracticeController.class);

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private PracticeCardService practiceCardService;

    @Autowired
    private PaperService paperService;
    private static volatile boolean importPcPractice = false;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    @Autowired
    private AnswerCardDao answerCardDao;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    public static final ConcurrentMap<Integer, Question> question_map = new ConcurrentHashMap<>();
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PracticePointsSummaryDao practicePointsSummaryDao;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SsdbPooledConnectionFactory ssdbPooledConnectionFactory;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    /**
     * 将答题卷的知识点分拆到mysql里面
     */
    @RequestMapping("/spilt_points")
    public void change(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (true) {
            return;
        }
        long id = 0;
        AtomicLong count = new AtomicLong();
        while (true) {
            Criteria criteria = Criteria.where("_id").gt(id);
            Query query = new Query(criteria);
            query.with(new Sort(new Sort.Order(Sort.Direction.ASC, "_id"))).limit(500);
            final List<AnswerCard> answerCards = mongoTemplate.find(query, AnswerCard.class);
            if (CollectionUtils.isEmpty(answerCards)) {
                break;
            }
            for (AnswerCard answerCard : answerCards) {
                if (count.incrementAndGet() % 5000 == 0) {
                    System.out.println("proccess=" + count.get());
                }
                id = Math.max(id, answerCard.getId());

                try {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                splitPoint(answerCard);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });
                } catch (RejectedExecutionException executionException) {
                    try {
                        Thread.sleep(200000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                splitPoint(answerCard);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });

                }

            }
        }

        System.out.println("------>知识点汇总分拆完成");


    }

    private void splitPoint(AnswerCard answerCard) {
        try {
            final List<QuestionPointTree> points = answerCard.getPoints();
            if (points != null) {
                answerCard.setPoints(null);
                answerCardDao.save(answerCard);

                final PracticePointsSummary pointsSummary = PracticePointsSummary.builder()
                        .points(points)
                        .practiceId(answerCard.getId())
                        .build();
                practicePointsSummaryDao.insert(pointsSummary);
            }
        } catch (Exception e) {
            logger.error("ex", e);
        }
    }


    @RequestMapping("/importPcPractice")
    public Object importPractice(HttpServletRequest httpServletRequest) throws SQLException, IOException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (true) {
            return null;
        }
        if (importPcPractice) {
            Map map = new HashMap();
            map.put("message", "已经在进行处理");
            return map;
        }

        importPcPractice = true;

        boolean more = true;

        initQuestions();
        while (true) {
            final String maxImportId = redisTemplate.opsForValue().get("pc_paper_max_import_id");
            long maxId = 0;
            if (maxImportId != null) {
                maxId = Long.valueOf(maxImportId);
            }
            String sql = "select * from v_exam_paper WHERE bl_subject=1 and pukey>" + maxId + " order by pukey ASC limit 0,500";
            final SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql);
            AtomicLong pukey = new AtomicLong(0);
            more = importPractice2mongo(resultSet, pukey);
            if (pukey.get() > 0) {//大于0才处理
                redisTemplate.opsForValue().set("pc_paper_max_import_id", pukey.longValue() + "");
            }
            try {
                Thread.sleep(500);//睡1s
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.warn("--->proocess=" + pukey);
            if (!more) {
                break;
            }
        }

        System.out.println("====>处理完毕");
        Map map = new HashMap();
        map.put("message", "处理完毕");
        importPcPractice = false;
        return map;
    }

    private volatile boolean sleep = false;
    private AtomicInteger count = new AtomicInteger();

    public boolean importPractice2mongo(final SqlRowSet resultSet, AtomicLong pukey) {
        boolean moreData = false;
        while (resultSet.next()) {
            moreData = true;
            try {
                final int id = resultSet.getInt("pukey");
                pukey.set(Math.max(id, pukey.get()));
                final String name = resultSet.getString("name");
                final int exampaper_type = resultSet.getByte("exampaper_type");

                //网站做题数据存的uid都是ucenter的id
                final int ucId = resultSet.getInt("uid");

                if (ucId == 0) {
                    continue;
                }

                int userId = 0;
                //查询ucId对应的uid
                String sql = "SELECT PUKEY FROM v_qbank_user WHERE bb105=" + ucId;
                SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
                if (rowSet.next()) {
                    userId = rowSet.getInt("PUKEY");
                } else {
                    //查询不到不处理
                    continue;
                }

                final int uid = userId;
                final int pastpaper_id = resultSet.getInt("pastpaper_id");
                final String tactics = resultSet.getString("tactics");
                final long createTime = resultSet.getLong("BB103") * 1000;//转换为毫秒
                if (sleep) {
                    try {
                        Thread.sleep(200000);
                        sleep = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                proccessWrongTrain(id, name, exampaper_type, uid, pastpaper_id, tactics, createTime);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });
                } catch (RejectedExecutionException executionException) {
                    sleep = true;//报错则休眠一段时间
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                proccessWrongTrain(id, name, exampaper_type, uid, pastpaper_id, tactics, createTime);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });

                }
            } catch (Exception e) {
                logger.error("ex", e);
            }
        }
        return moreData;
    }


    private ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    private void proccessWrongTrain(int id, String name, int exampaper_type, int uid, int pastpaper_id, String tactics, long createTime) throws Exception {
        if (StringUtils.isBlank(tactics)) {//没有试题
            logger.info("answer card id={} no data ,skip it.tactics={}", id, tactics);
            return;
        }
        try {
            //试着解压
            String tmp = GzipUtil.uncompress(tactics);
            if (StringUtils.isNoneBlank(tmp)) {
                tactics = tmp;
            }
        } catch (Exception e) {

        }

        final JsonNode jsonNode = objectMapper.readTree(tactics);
        if (!jsonNode.isArray()) {
            logger.warn("vilad tactics={},skip it.", tactics);
            return;
        }
        final ArrayNode arr = (ArrayNode) jsonNode;
        final List<Integer> ids = getQuestionIds(arr);
        int qcount = ids.size();//试题个数
        if (qcount == 0) {
            logger.info("answer card id={} no questions ,skip it.", id);
            return;
        }

        //试题和答案
        Map<Integer, Integer> answerKey = new HashMap<>();
        final ArrayListMultimap<Integer, Integer> listMultimap = ArrayListMultimap.create();
        Set<Question> questionSet = new HashSet<>();
        try {

            //做一层本地缓存，减少请求，防止网卡打满
            for (Integer qid : ids) {
                Question question = question_map.get(qid);
                if (question == null) {
                    question = questionDubboService.findById(qid);
                }
                if (question != null) {
                    question_map.put(question.getId(), question);
                    questionSet.add(question);
                }
            }
        } catch (Exception e) {
            logger.error("ex,ids={}", ids, e);
        }
        int allDifficulty = 0;//难度系数
        for (Question question : questionSet) {//遍历试题,对其进行归类
            GenericQuestion genericQuestion = (GenericQuestion) question;
            if (genericQuestion == null) {
                continue;
            }
            final Integer moduleId = genericQuestion.getPoints().get(0);
            allDifficulty = allDifficulty + genericQuestion.getDifficult();
            listMultimap.put(moduleId, question.getId());
            answerKey.put(question.getId(), ((GenericQuestion) question).getAnswer());
        }
        List<Integer> tmpQuestionsIds = new ArrayList();//试题id列表
        long practiceId = -1;
        List<Answer> answerList = new ArrayList<>();
        if (pastpaper_id > 0) {//真题或者模拟卷
            Paper paper = paperService.findById(pastpaper_id);
            if (paper == null) {//尝试查询模拟题
                //模拟题id
                pastpaper_id = Integer.valueOf("200" + Strings.padStart(pastpaper_id + "", 4, '0'));
                paper = paperService.findById(pastpaper_id);
            }

            if (paper == null) {
                logger.info("can`t find paper={}", pastpaper_id);
                return;
            }
            tmpQuestionsIds = paper.getQuestions();
            answerList = getAnswers(id, answerKey, tmpQuestionsIds);
            if (answerList.size() < 1) {//试卷没有作答
                return;
            }
            try {
                final StandardCard practicePaper = paperAnswerCardService.create(paper, 1, uid, TerminalType.PC);
                practiceId = practicePaper.getId();
                practicePaper.setCreateTime(createTime);
                practicePaper.setSubject(1);
                practiceCardService.save(practicePaper);//更新时间
            } catch (WaitException e) {
                e.printStackTrace();
            }

        } else {//练习卷
            List<Module> modules = new ArrayList<>();
            List<Integer> questionIds = new ArrayList();
            for (Integer moduleId : listMultimap.keySet()) {
                final String moduleName = knowledge.get(moduleId);
                final List<Integer> list = listMultimap.get(moduleId);
                final Module module = Module.builder().category(moduleId)
                        .name(moduleName)
                        .qcount(list.size()).build();
                questionIds.addAll(list);//试题列表
                modules.add(module);
            }
            if (qcount == 0) {
                logger.info("qcount=0,skip paper card. cardId={}", id);
                return;
            }
            final PracticePaper practicePaper = PracticePaper.builder().difficulty(new BigDecimal(allDifficulty).divide(new BigDecimal(qcount), 1, RoundingMode.HALF_UP).doubleValue())
                    .modules(modules)
                    .name(name)
                    .qcount(qcount)
                    .subject(1)
                    .questions(questionIds)
                    .build();
            tmpQuestionsIds = practicePaper.getQuestions();
            answerList = getAnswers(id, answerKey, tmpQuestionsIds);
            if (answerList.size() < 1) {//试卷没有作答
                return;
            }
            try {
                //创建试卷
                final PracticeCard practiceCard = practiceCardService.create(practicePaper, TerminalType.PC, cardTypeMap.get(exampaper_type), uid);
                practiceCard.setCreateTime(createTime);
                practiceCardService.save(practiceCard);//更新时间
                practiceId = practiceCard.getId();
            } catch (BizException e) {
                e.printStackTrace();
            }

        }


        try {
            AnswerCard answerCard = paperAnswerCardService.submitPractice(practiceId, uid, answerList, -9, TerminalType.ANDROID, "7.0.0");
            //如果是套题交卷，增加练习统计数据
            if (answerCard instanceof StandardCard) {
                //添加完成的练习
                paperUserMetaService.addFinishPractice(uid, ((StandardCard) answerCard).getPaper().getId(), practiceId);
            }
        } catch (BizException e) {
            e.printStackTrace();
        }

    }

    private List<Answer> getAnswers(int id, Map<Integer, Integer> answerKey, List<Integer> tmpQuestionsIds) throws SQLException {
        String answerSql = "select * from v_user_answer_paper_log  where exampaper_id=" + id + " and status=1 and  BB102=1 ORDER BY PUKEY desc limit 0,1";
        final SqlRowSet answerResultSet = jdbcTemplate.queryForRowSet(answerSql);
        int expend_time = 0;
        int[] answers = new int[tmpQuestionsIds.size()];
        int[] corrects = new int[tmpQuestionsIds.size()];
        int[] times = new int[tmpQuestionsIds.size()];
        int speed = 0;
        int rcount = 0;
        int wcount = 0;
        long createTime = 0;
        String answer_info = null;
        if (answerResultSet.next()) {
            createTime = answerResultSet.getLong("start_time") * 1000;
            expend_time = answerResultSet.getInt("expend_time");
            answer_info = answerResultSet.getString("answer_info");
        }
        JsonNode elements = null;
        boolean zip = true;
        try {
            //试着解压缩
            String tmp = GzipUtil.uncompress(answer_info);
            if (StringUtils.isNoneBlank(answer_info)) {
                answer_info = tmp;
            }
        } catch (Exception e) {
            zip = false;
        }

        JsonNode answerJson = null;
        Map<Integer, Integer> myAnswerMap = null;
        try {
            answerJson = objectMapper.readTree(answer_info);
            if (zip) {
                elements = answerJson.get("Param");
            } else {
                elements = answerJson;
            }
            final Iterator<String> questionsAnswerJson = elements.fieldNames();
            myAnswerMap = new HashMap();
            while (questionsAnswerJson.hasNext()) {
                String str = questionsAnswerJson.next();
                int qid = Integer.valueOf(str);
                final String answer = elements.get(str).textValue();
                final int answer1 = toAnswer(answer);
                myAnswerMap.put(qid, answer1);
            }
        } catch (Exception e) {
            myAnswerMap = getAnswer(id);
        }
        int count = 0;
        List<Answer> answerList = new ArrayList<>();
        for (Integer qid : myAnswerMap.keySet()) {
            final int questionIndex = tmpQuestionsIds.indexOf(qid);
            if (questionIndex < 0) {
                //没有作答则跳过
                continue;
            }
            final int answer1 = myAnswerMap.get(qid);

            if (answer1 < 1) {
                //没有作答则跳过
                continue;
            }

            int correct = QuestionCorrectType.UNDO;
            if (answer1 > 0) {
                if (answerKey.get(qid) != null && answerKey.get(qid) == answer1) {//答案正确
                    correct = QuestionCorrectType.RIGHT;
                    rcount++;//正确个数
                } else {
                    correct = QuestionCorrectType.WRONG;
                    wcount++;//错误个数
                }
                //答题个数
                count++;
            }
            if (count > 0) {
                //答题速度
                speed = expend_time / count;
            }
            if (questionIndex > 0) {//说明试题存在
                corrects[questionIndex] = correct;
                answers[questionIndex] = answer1;
            }
            if (answer1 > 0) {
                Answer answer = new Answer();
                answer.setCorrect(correct);
                answer.setQuestionId(qid);
                answer.setAnswer(answer1 + "");
                answer.setTime(speed);
                answerList.add(answer);
            }
        }
        return answerList;
    }

    /**
     * 从v_user_answer里面获取试题答案
     *
     * @return
     */
    public Map<Integer, Integer> getAnswer(int exampaperId) throws SQLException {
        String answerSql = "SELECT question_id,answer FROM v_user_answer WHERE exampaper_id=" + exampaperId;
        final SqlRowSet answerResultSet = jdbcTemplate.queryForRowSet(answerSql);
        Map<Integer, Integer> data = new HashMap();
        while (answerResultSet.next()) {
            final int questionId = answerResultSet.getInt("question_id");
            final String answer = answerResultSet.getString("answer");
            data.put(questionId, toAnswer(answer));
        }
        return data;
    }

    public static final List<Integer> getQuestionIds(ArrayNode arrayNode) {
        if (arrayNode == null || arrayNode.size() == 0) {
            return new ArrayList<>();
        }

        List<Integer> ids = new ArrayList();
        if (arrayNode.get(0).has("moduleId")) {//带有module的
            if (arrayNode.isArray()) {//遍历模块
                final ArrayNode arr = (ArrayNode) arrayNode;
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
                }
                return ids;
            }
        } else {
            for (JsonNode jsonNode1 : arrayNode) {
                String qId = null;
                try {
                    qId = jsonNode1.get("qId").asText();
                } catch (Exception e) {
                    logger.error("ex,data={}", arrayNode, e);
                }
                if (qId == null) {
                    continue;
                }
                int mmid = 0;
                if (qId.startsWith("o")) {
                    mmid = Integer.valueOf(qId.substring(1));
                    if (mmid > 0) {
                        ids.add(mmid);
                    }
                } else if (qId.startsWith("m")) {//复合题
                    final ArrayNode qSubList = (ArrayNode) jsonNode1.get("qSubList");
                    for (JsonNode jsonNode2 : qSubList) {
                        mmid = Integer.valueOf(jsonNode2.asText().substring(1));
                        if (mmid > 0) {
                            ids.add(mmid);
                        }
                    }
                } else {
                    System.out.println("--->不知道的qid=" + qId);
                }

            }
        }
        return ids;
    }


    /**
     * 转换答案
     *
     * @param answer
     * @return
     */
    public static final int toAnswer(String answer) {
        if (StringUtils.isBlank(answer)) {
            return 0;
        }
        char[] chars = answer.toUpperCase().toCharArray();
        Arrays.sort(chars);//排序
        StringBuilder sb = new StringBuilder();
        for (char aChar : chars) {
            final String ss = answerMap.get(aChar + "");
            if (ss != null) {
                sb.append(ss);
            }
        }
        return Integer.valueOf(sb.toString());
    }

    @RequestMapping(value = "kryotest")
    public void kryo(@RequestParam int uid, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        String sql = "SELECT * FROM v_exam_paper WHERE uid=" + uid;
        final SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql);
        AtomicLong pubKey = new AtomicLong();
        importPractice2mongo(resultSet, pubKey);
    }


    /**
     * 重新导入一个用户的数据
     *
     * @param uid
     */
    @RequestMapping(value = "initSingleUserQuestion")
    public void initSingleUserQuestion(@RequestParam long uid, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        String sql = "SELECT PUKEY,area FROM v_qbank_user where PUKEY =?";
        final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, uid);
        if (sqlRowSet.next()) {
            final int area = sqlRowSet.getInt("area");
            final long PUKEY = sqlRowSet.getLong("PUKEY");
            //删除旧的数据
            redisTemplate.delete("wrong_count_" + PUKEY + "_1");
            List<Integer> pointIds = new ArrayList<>();
            for (com.huatu.ztk.commons.Module module : ModuleConstants.getModulesBySubject(1)) {
                final int moduleId = module.getId();
                pointIds.add(moduleId);
                final List<QuestionPoint> children = questionPointDubboService.findChildren(moduleId);
                children.forEach(questionPoint -> {//遍历二级知识点
                    pointIds.add(questionPoint.getId());
                    final List<QuestionPoint> children1 = questionPointDubboService.findChildren(questionPoint.getId());
                    children1.forEach(questionPoint1 -> {//遍历三级知识点
                        pointIds.add(questionPoint1.getId());
                    });
                });
            }

            final SsdbConnection connection = ssdbPooledConnectionFactory.getConnection();

            try {
                pointIds.forEach(pointId -> {
                    redisTemplate.delete("finish_" + PUKEY + "_1" + "_" + pointId);
                    redisTemplate.delete("wrong_" + PUKEY + "_1" + "_" + pointId);
                    connection.zclear("finish_" + PUKEY + "_1" + "_" + pointId);
                });
                connection.hclear("finish_count_" + PUKEY + "_1");
            } catch (Exception e) {
                logger.error("ex", e);
            } finally {
                ssdbPooledConnectionFactory.returnConnection(connection);
            }

            redisTemplate.delete("finish_count_" + PUKEY + "_1");
            redisTemplate.delete("finish_point_" + PUKEY + "_1");

            //删除试题统计
            final Query query = Query.query(Criteria.where("_id").is(PUKEY + "_1"));
            mongoTemplate.remove(query, "user_question_summary");
            proccessUserQuestions(area, PUKEY, 100);
        }
    }

    public volatile boolean running = false;

    @RequestMapping(value = "importUserQuestion")
    public void importUserQuestion(@RequestParam(defaultValue = "175871") long minUid,
                                   @RequestParam(defaultValue = "657910") long maxUid,
                                   HttpServletRequest httpServletRequest
    ) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (true) {
            return;
        }
        if (running) {
            return;
        }
        running = true;
        boolean moreData = true;
        AtomicInteger count = new AtomicInteger(1);
        while (moreData) {
            String sql = "SELECT PUKEY,area,bb108 FROM v_qbank_user where BB108> ? and BB108<?  order by BB108 asc limit 500";
            Object[] params = {
                    minUid, maxUid
            };
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, params);
            moreData = false;
            while (sqlRowSet.next()) {
                moreData = true;
                if (count.incrementAndGet() % 5000 == 0) {
                    logger.info("procce users = {}", count.get());
                }

                final int area = sqlRowSet.getInt("area");
                final long PUKEY = sqlRowSet.getLong("PUKEY");
                final long bb108 = sqlRowSet.getLong("bb108");
                //删除旧的数据
                redisTemplate.delete("wrong_count_" + PUKEY + "_1");
                redisTemplate.delete("finish_count_" + PUKEY + "_1");
                redisTemplate.delete("finish_point_" + PUKEY + "_1");
                //删除试题统计
                final Query query = Query.query(Criteria.where("_id").is(PUKEY + "_1"));
                mongoTemplate.remove(query, "user_question_summary");
                minUid = Math.max(minUid, bb108);
                logger.info("proccess user bb108={}", bb108);
                try {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                proccessUserQuestions(area, PUKEY, 60);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });
                } catch (RejectedExecutionException executionException) {
                    try {
                        Thread.sleep(300000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                proccessUserQuestions(area, PUKEY, 60);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });

                }
            }
        }
        logger.info("importUserQuestion处理完成");

        running = false;

    }

    private void proccessUserQuestions(int area, long PUKEY, int size) {
        Criteria criteria = Criteria.where("userId").is(PUKEY);
        Query query = new Query(criteria);
//        query.limit(size);//最多取60
        final List<AnswerCard> answerCards = mongoTemplate.find(query, AnswerCard.class);
        if (CollectionUtils.isEmpty(answerCards)) {
            return;
        }
        for (AnswerCard answerCard : answerCards) {
            try {
                final String[] answers = answerCard.getAnswers();
                final int[] corrects = answerCard.getCorrects();
                final int[] times = answerCard.getTimes();
                List<Integer> questions = null;
                if (answerCard instanceof StandardCard) {
                    StandardCard standardCard = (StandardCard) answerCard;
                    questions = standardCard.getPaper().getQuestions();
                } else if (answerCard instanceof PracticeCard) {
                    PracticeCard practiceCard = (PracticeCard) answerCard;
                    questions = practiceCard.getPaper().getQuestions();
                }
                List<Answer> answerList = new ArrayList<>(questions.size());
                for (int i = 0; i < questions.size(); i++) {
                    if (answers[i].equals("0") || corrects[i] == 0) {//没有作答的不处理
                        continue;
                    }
                    Answer answer = new Answer();
                    answer.setAnswer(answers[i]);
                    answer.setCorrect(corrects[i]);
                    answer.setTime(times[i]);
                    answer.setQuestionId(questions.get(i));
                    answerList.add(answer);
                }
                if (answerList.size() < 1) {
                    continue;
                }
                final UserAnswers userAnswers = UserAnswers.builder()
                        .area(area)
                        .practiceId(answerCard.getId())
                        .submitTime(answerCard.getCreateTime())
                        .subject(1)
                        .uid(PUKEY)
                        .answers(answerList)
                        .build();
                rabbitTemplate.convertAndSend(RabbitMqConstants.SUBMIT_ANSWERS, "", userAnswers);
            } catch (Exception e) {
                logger.error("ex", e);
            }
        }
    }

    /**
     * 重新导入练习数大于等于count的用户试题数据
     *
     * @param count
     */
    @RequestMapping("/init_some_user_question")
    public void methodnn(@RequestParam(defaultValue = "60") int count, HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (true) {
            return;
        }
        String groupStr = "{$group : {_id : \"$userId\", total : {$sum : 1}}}";
        DBObject group = (DBObject) JSON.parse(groupStr);

        String matchStr = String.format("{$match:{total:{$gte:%d}}}", count);
        DBObject match = (DBObject) JSON.parse(matchStr);

        List<DBObject> pipeline = new ArrayList<>();
        pipeline.add(group);
        pipeline.add(match);

        AggregationOutput output = mongoTemplate.getCollection("ztk_answer_card").aggregate(pipeline);
        Iterator<DBObject> iterator = output.results().iterator();
        int icount = 0;
        while (iterator.hasNext()) {
            BasicDBObject dbo = (BasicDBObject) iterator.next();
            long userId = dbo.getLong("_id");
            long cardCount = dbo.getInt("total");
            logger.info("userId={}, cardCount={}", userId, cardCount);

            try {
                initSingleUserQuestion(userId, null);
            } catch (Exception exception) {
                logger.error("init some user question fail,userId={}", userId);
            }
            icount++;
        }

        logger.info(">>>>>>> icount ={}", icount);
    }

}
