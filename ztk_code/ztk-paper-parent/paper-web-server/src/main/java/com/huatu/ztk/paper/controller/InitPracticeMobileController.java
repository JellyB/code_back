package com.huatu.ztk.paper.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.dao.PracticePointsSummaryDao;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.paper.service.PracticeCardService;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.common.QuestionCorrectType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 手机端练习卷数据切割
 * Created by shaojieyue
 * Created time 2016-05-11 15:16
 */

@RestController
public class InitPracticeMobileController extends BaseInitController {
    private static final Logger logger = LoggerFactory.getLogger(InitPracticeMobileController.class);
    private static final Random RANDOM = new Random();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JdbcTemplate mobileJdbcTemplate;

    @Autowired
    private QuestionDubboService questionDubboService;
    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private PracticeCardService practiceCardService;

    @Autowired
    private PaperService paperService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PracticePointsSummaryDao practicePointsSummaryDao;

    private static volatile boolean importMobilePaperPractice = false;

    ThreadPoolExecutor mobileExecutor = new ThreadPoolExecutor(30, 30, 2000, TimeUnit.HOURS,
            new ArrayBlockingQueue<Runnable>(20000));

    ThreadPoolExecutor mobileUserExecutor = new ThreadPoolExecutor(5, 10, 2000, TimeUnit.HOURS,
            new ArrayBlockingQueue<Runnable>(20000));

    @RequestMapping("/importSingleMobileUserPractice")
    public void importSingleMobileUserPractice(@RequestParam long mobileUserId,
                                               @RequestParam(defaultValue = "-1") int size,
                                               HttpServletRequest httpServletRequest){
        try {
            String remoteAddr = httpServletRequest.getRemoteAddr();
            String url = httpServletRequest.getRequestURL().toString();
            logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
            if(true){
                return;
            }
            long pcUserId = getPcUserId(mobileUserId);

            if (pcUserId < 0) {//用户不存在,不作处理
                logger.warn("userid={} not exist at pc user", mobileUserId);
                return;
            }

            int limitSize = (size > 0 ) ? size : 500;
            String sql = "select answersheet,pointpromote,trainmode,userid,paperid,recordid from " + ns_statisticsrecord_history(mobileUserId) + " where userid =  " + mobileUserId + " limit " + limitSize;

            final SqlRowSet resultSet = mobileJdbcTemplate.queryForRowSet(sql);
            try {
                importMobilePractice2mongo(resultSet, pcUserId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping("/importMobilePaperPractice")
    public Object importMobilePaperPractice(HttpServletRequest httpServletRequest) throws SQLException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return null;
        }
        if (importMobilePaperPractice) {
            Map map = new HashMap();
            map.put("message", "已经在进行处理");
            return map;
        }

        importMobilePaperPractice = true;

        initQuestions();//

        final String mobile_paper_min_import_id = "mobile_paper_min_import_id";
        final String maxImportId = redisTemplate.opsForValue().get(mobile_paper_min_import_id);
        int userId = 0;
        if (StringUtils.isNoneBlank(maxImportId)) {
            userId = Integer.parseInt(maxImportId);
        }
        boolean more = true;
        int start = 0;
        int count = 500;
        int bb108 = Integer.MAX_VALUE;
        while (more) {
            String queryUserSql = "SELECT id FROM ns_users where id>" + userId + " ORDER BY id ASC limit 0,500";
            final SqlRowSet queryUserresultSet = mobileJdbcTemplate.queryForRowSet(queryUserSql);
            more = false;
            while (queryUserresultSet.next()) {//遍历本次的用户列表
                more = true;
                final int mobileUserId = queryUserresultSet.getInt("id");
                logger.info("-->process user={}",mobileUserId);
                userId = Math.max(userId,mobileUserId);
                try {
                    mobileUserExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            final long pcUserId;
                            try {
                                long start = System.currentTimeMillis();
                                pcUserId = getPcUserId(mobileUserId);
                                if (pcUserId < 0) {//用户不存在,不作处理
                                    logger.warn("userid={} not exist at pc user", mobileUserId);
                                    return;
                                }
                                String sql = "select answersheet,pointpromote,trainmode,userid,paperid,recordid from " + ns_statisticsrecord_history(mobileUserId) + " where userid =  " + mobileUserId + " limit 0,500";
                                final SqlRowSet resultSet = mobileJdbcTemplate.queryForRowSet(sql);
                                try {
                                    importMobilePractice2mongo(resultSet, pcUserId);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }


                        }
                    });
                } catch (RejectedExecutionException executionException) {
                    try {
                        Thread.sleep(50000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mobileUserExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            final long pcUserId;
                            try {
                                pcUserId = getPcUserId(mobileUserId);
                                if (pcUserId < 0) {//用户不存在,不作处理
                                    logger.warn("userid={} not exist at pc user", mobileUserId);
                                    return;
                                }
                                String sql = "select answersheet,pointpromote,trainmode,userid,paperid,recordid from " + ns_statisticsrecord_history(mobileUserId) + " where userid =  " + mobileUserId + " limit 0,500";
                                final SqlRowSet resultSet = mobileJdbcTemplate.queryForRowSet(sql);
                                try {
                                    importMobilePractice2mongo(resultSet, pcUserId);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }


                        }
                    });

                }

            }
            //报错处理点
            redisTemplate.opsForValue().set(mobile_paper_min_import_id,userId+"");
            logger.warn("proccess={}", userId);
        }
        Map map = new HashMap();
        map.put("message", "已经在进行处理");
        return map;
    }

    private ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    private long importMobilePractice2mongo(SqlRowSet resultSet, final long pcUserId) throws SQLException, IOException {
        long minId = -1;
        while (resultSet.next()) {//遍历用户列表
            final long paperid = resultSet.getLong("paperid");
            final long userid = resultSet.getLong("userid");
            if (paperid < 1) {
                logger.info("paperid is null,skip");
                continue;
            }

            final String trainmode = resultSet.getString("trainmode");
            final String answersheet = resultSet.getString("answersheet");
            final String pointpromote = resultSet.getString("pointpromote");
            try {
                mobileExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            saveAnswerCard(paperid, pcUserId, trainmode, answersheet, pointpromote);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (RejectedExecutionException executionException) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mobileExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            saveAnswerCard(paperid, userid, trainmode, answersheet, pointpromote);
                        } catch (Exception e) {
                            logger.error("ex", e);
                        }
                    }
                });

            }


            if (minId > paperid) {
                minId = paperid;
            }
        }
        return minId;
    }

    private void saveAnswerCard(long paperid, long pcUserId, String trainmode, String answersheet, String pointpromote) throws SQLException, IOException {
        long createTime = -1;
        AnswerCard answerCard = null;
        int status = 0;
        Map<Integer, Integer> myAnswerMap = new HashMap();
        Map<Integer, Integer> myPayTimeMap = new HashMap();
        Map<Integer, Integer> correctMap = new HashMap();
        List<Integer> tmpQuestionsIds = new ArrayList();//试题id列表
        List<Integer> ids = new ArrayList<>();
        String name = trainmode;
        if ("微信答题".equals(trainmode) || "竞技练习".equals(trainmode) || "每日特训".equals(trainmode) || "考点直击".equals(trainmode) || "错题库".equals(trainmode) || "我猜你练".equals(trainmode) || "收藏夹".equals(trainmode) || "专项练习".equals(trainmode)) {
            if (StringUtils.isBlank(answersheet)) {//没有answersheet
                return;
            }
            final JsonNode jsonNode = objectMapper.readTree(answersheet);
            final JsonNode answerlist = jsonNode.get("answerlist");
            ArrayNode arrayNode = (ArrayNode) answerlist;

            for (int i = 0; i < arrayNode.size(); i++) {
                final JsonNode jsonNode1 = arrayNode.get(i);
                int questionid = jsonNode1.get("questionid").asInt();
                //status:0-未答 ，1-正确，2-错误
                status = jsonNode1.get("status").asInt();

                if (status != QuestionCorrectType.UNDO && status != QuestionCorrectType.RIGHT && status != QuestionCorrectType.WRONG) {
                    status = QuestionCorrectType.WRONG;
                }

                correctMap.put(questionid, status);
                int paytime = jsonNode1.get("paytime").asInt() / 1000;
                myPayTimeMap.put(questionid, paytime);
                final ArrayNode answers = (ArrayNode) jsonNode1.get("answers");
                StringBuilder sb = new StringBuilder();
                int answer = 0;
                for (int j = 0; j < answers.size(); j++) {
                    sb.append(answers.get(j).asText());
                }
                answer = InitPracticeController.toAnswer(sb.toString());
                myAnswerMap.put(questionid, answer);
                ids.add(questionid);
            }
            int qcount = ids.size();//试题个数
            if (qcount == 0) {
                logger.info("qcount=0,skip paper card. cardId={}", paperid);
                return;
            }
            final PracticePaper practicePaper = getPracticePaper(trainmode, ids);
            tmpQuestionsIds = practicePaper.getQuestions();
            answerCard = PracticeCard.builder().paper(practicePaper).build();
            answerCard.setDifficulty(practicePaper.getDifficulty());
            createTime = jsonNode.get("starttime").asLong();
        } else if ("真题演练".equals(trainmode)) {
            if (StringUtils.isBlank(pointpromote)) {
                return;
            }
            final JsonNode jsonNode = objectMapper.readTree(pointpromote);
            if (!jsonNode.has("sheet")) {//不包含sheet
                return;
            }
            final Iterator<Map.Entry<String, JsonNode>> sheet = jsonNode.get("sheet").fields();

            int paytime = jsonNode.get("paytime").asInt();
            while (sheet.hasNext()) {
                ArrayNode arrayNode = (ArrayNode) sheet.next().getValue();

                for (int i = 0; i < arrayNode.size(); i++) {
                    final JsonNode jsonNode1 = arrayNode.get(i);
                    int questionid = jsonNode1.get("questionid").asInt();
                    //status:0-未答 ，1-正确，2-错误
                    status = jsonNode1.get("status").asInt();

                    if (status != QuestionCorrectType.UNDO && status != QuestionCorrectType.RIGHT && status != QuestionCorrectType.WRONG) {
                        status = QuestionCorrectType.WRONG;
                    }

                    correctMap.put(questionid, status);
                    myPayTimeMap.put(questionid, paytime);
                    final ArrayNode answers = (ArrayNode) jsonNode1.get("answers");
                    StringBuilder sb = new StringBuilder();
                    int answer = 0;
                    for (int j = 0; j < answers.size(); j++) {
                        sb.append(answers.get(j).asText());
                    }
                    answer = InitPracticeController.toAnswer(sb.toString());
                    myAnswerMap.put(questionid, answer);
                    ids.add(questionid);
                }
            }

            //只能通过这个办法来做
            String sql = "SELECT PUKEY FROM v_pastpaper_info where tactics like '%o" + ids.get(0) + "%'";
            final SqlRowSet pastPaper = mobileJdbcTemplate.queryForRowSet(sql);
            int pastpaperid = -1;
            if (pastPaper.next()) {
                pastpaperid = pastPaper.getInt("PUKEY");
            }
            if (pastpaperid < 0) {
                logger.info("not found pastpaperid={},userId={} ,skip paperid={},sql={}", pastpaperid, pcUserId, paperid, sql);
            }

            final Paper paper = paperService.findById(pastpaperid);
            if (paper == null) {
                logger.info("paper not found. pastpaperid={}", pastpaperid);
                return;
            }
            tmpQuestionsIds = paper.getQuestions();
            answerCard = StandardCard.builder().paper(paper).build();
            answerCard.setDifficulty(paper.getDifficulty());
        } else if ("砖超联赛-海选".equals(trainmode)) {

        } else if ("精准估分".equals(trainmode)) {
            if (StringUtils.isBlank(pointpromote)) {
                return;
            }

            final JsonNode jsonNode = objectMapper.readTree(pointpromote);
            int pastpaperid = jsonNode.get("pastpaperid").asInt();
            //精准估分是模拟卷,所以id要处理
            pastpaperid = Integer.valueOf("200" + Strings.padStart(pastpaperid + "", 4, '0'));
            final Paper paper = paperService.findById(pastpaperid);
            tmpQuestionsIds = paper.getQuestions();
            answerCard = StandardCard.builder().paper(paper).build();
            answerCard.setDifficulty(paper.getDifficulty());
            ArrayNode arr = (ArrayNode) jsonNode.get("pointScore");
            for (JsonNode node : arr) {
                final ArrayNode arrayNode = (ArrayNode) node.get("questions");
                for (int i = 0; i < arrayNode.size(); i++) {
                    final JsonNode jsonNode1 = arrayNode.get(i);
                    int questionid = jsonNode1.get("questionid").asInt();
                    //status:0-未答 ，1-正确，2-错误
                    status = jsonNode1.get("status").asInt();

                    if (status != QuestionCorrectType.UNDO && status != QuestionCorrectType.RIGHT && status != QuestionCorrectType.WRONG) {
                        status = QuestionCorrectType.WRONG;
                    }
                    int paytime = arrayNode.get("paytime").asInt();
                    correctMap.put(questionid, status);
                    myPayTimeMap.put(questionid, paytime);
                    final ArrayNode answers = (ArrayNode) jsonNode1.get("answers");
                    StringBuilder sb = new StringBuilder();
                    int answer = 0;
                    for (int j = 0; j < answers.size(); j++) {
                        sb.append(answers.get(j).asText());
                    }
                    answer = InitPracticeController.toAnswer(sb.toString());
                    myAnswerMap.put(questionid, answer);
                    ids.add(questionid);
                }
            }
            final double score = jsonNode.get("score").asDouble();
            ((StandardCard) answerCard).setScore(score);
            name = jsonNode.get("papername").asText();


        } else {

        }

        int expend_time = 0;
        String[] answers = new String[tmpQuestionsIds.size()];
        int[] corrects = new int[tmpQuestionsIds.size()];
        int[] times = new int[tmpQuestionsIds.size()];
        int speed = 0;
        int rcount = 0;
        int wcount = 0;
        int allTime = 0;
        int count = 0;
        for (int i = 0; i < tmpQuestionsIds.size(); i++) {
            int qid = tmpQuestionsIds.get(i);
            answers[i] = myAnswerMap.get(qid) + "";
            corrects[i] = correctMap.get(qid);
            times[i] = myPayTimeMap.get(qid);
            allTime = allTime + times[i];
            if (corrects[i] == QuestionCorrectType.RIGHT) {//正确
                count++;
                rcount++;
            } else if (corrects[i] == QuestionCorrectType.WRONG) {//错误
                wcount++;
                count++;
            }
        }
        if (count > 0) {
            speed = allTime / count;
        }
        //未做的数量
        int ucount = tmpQuestionsIds.size() - wcount - rcount;


        final Integer type = mobileTypeMap.get(trainmode);
        if (type == null) {
            logger.info("unkonw trainmode={}", trainmode);
        }
//        long newId = -1;
//        for (int i = 0; i < 2; i++) {
//            try {
//                newId = IdClient.getClient().nextCommonId();
//                break;
//            } catch (WaitException e) {
//                logger.error("get commonId fail.");
//            }
//        }
        long newId = Long.valueOf(String.valueOf(System.nanoTime()) + String.valueOf(System.currentTimeMillis()).substring(11));

        answerCard.setId(newId);
        answerCard.setType(type);
        answerCard.setSpeed(speed);
        answerCard.setTimes(times);
        answerCard.setStatus(status);
        answerCard.setAnswers(answers);
        answerCard.setCorrects(corrects);
        answerCard.setSpeed(speed);
        answerCard.setName(name);
        answerCard.setCreateTime(createTime);

        answerCard.setRcount(rcount);
        answerCard.setSubject(1);
        int terminal = TerminalType.ANDROID;
        if (RANDOM.nextInt() % 3 == 0) {
            terminal = TerminalType.IPHONE;
        } else {
            terminal = TerminalType.ANDROID;
        }
        if ("微信答题".equals(trainmode)) {
            terminal = TerminalType.WEI_XIN;
        }

        answerCard.setTerminal(terminal);//pc
        answerCard.setUcount(ucount);
        answerCard.setExpendTime(speed * (wcount + rcount));
        answerCard.setUserId(pcUserId);
        answerCard.setWcount(wcount);
        answerCard.setLastIndex(tmpQuestionsIds.size());
        if (answerCard.getRcount()+answerCard.getWcount()==0) {
            return;
        }
        final List<QuestionPointTree> questionPointTrees = questionPointDubboService.questionPointSummary(tmpQuestionsIds, corrects, times);
        PracticePointsSummary pointsSummary = PracticePointsSummary.builder()
                .practiceId(answerCard.getId())
                .points(questionPointTrees)
                .build();
        //插入知识点汇总记录记录
        practicePointsSummaryDao.insert(pointsSummary);
        practiceCardService.save(answerCard);
    }

    private PracticePaper getPracticePaper(String name, List<Integer> ids) {
        int qcount = ids.size();
        //试题和答案
        Map<Integer, Integer> answerKey = new HashMap<>();
        final ArrayListMultimap<Integer, Integer> listMultimap = ArrayListMultimap.create();

        int allDifficulty = 0;//难度系数
        for (Integer qid : ids) {//遍历试题,对其进行归类
            final GenericQuestion genericQuestion = (GenericQuestion)questionDubboService.findById(qid);
            if (genericQuestion == null) {
                continue;
            }

            final Integer moduleId = genericQuestion.getPoints().get(0);
            allDifficulty = allDifficulty + genericQuestion.getDifficult();
            listMultimap.put(moduleId, genericQuestion.getId());
            answerKey.put(genericQuestion.getId(), genericQuestion.getAnswer());
        }

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
        final double difficulty = new BigDecimal(allDifficulty).divide(new BigDecimal(qcount), 1, RoundingMode.HALF_UP).doubleValue();
        final PracticePaper practicePaper = PracticePaper.builder()
                .difficulty(difficulty)
                .modules(modules)
                .name(name)
                .qcount(qcount)
                .questions(questionIds)
                .build();
        return practicePaper;
    }

    private long getPcUserId(long userid) throws SQLException {
        String sql = "SELECT PUKEY,BB108 FROM v_qbank_user where BB108 = " + userid;
        final SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql);
        long pukey = -1;
        if (resultSet.next()) {
            pukey = resultSet.getLong("PUKEY");
        }
        return pukey;
    }


    public static void main(String[] args) {
        final String s = ns_statisticsrecord_history(1511670);
        System.out.println(s);
    }

    public static String ns_pastpaper_statisticsrecord(long userid) {
        String subtable = "";
        final Integer useridPlaces = 2;
        String userid_formst = String.format("%0" + useridPlaces + "d",
                userid);

        subtable = "ns_pastpaper_statisticsrecord" + "_"
                + userid_formst.substring(userid_formst.length() - useridPlaces);
        return subtable;

    }

    public static String ns_statisticsrecord_history(long userid) {
        String subtable = "";
        final Integer useridPlaces = 2;
        String userid_formst = String.format("%0" + useridPlaces + "d",
                userid);
        subtable = "ns_statisticsrecord_history" + "_"
                + userid_formst.substring(userid_formst.length() - useridPlaces);
        return subtable;
    }
}
