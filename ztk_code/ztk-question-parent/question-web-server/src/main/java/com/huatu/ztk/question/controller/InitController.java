package com.huatu.ztk.question.controller;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.CompositeQuestion;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.*;
import com.huatu.ztk.question.dao.QuestionDao;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.huatu.ztk.question.service.NetSchoolQuestionService;
import com.huatu.ztk.question.service.QuestionDubboServiceImpl;
import com.huatu.ztk.question.service.QuestionRecordService;
import com.huatu.ztk.question.util.SimilarityUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by shaojieyue on 4/26/16.
 */

@RestController
public class InitController {

    public static final Logger logger = LoggerFactory.getLogger(InitController.class);

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public Map<Integer,String> point_map = new HashMap<Integer, String>();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private QuestionRecordService questionRecordService;

    private static volatile boolean importAnswer = false;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final String USER_OFFSETS = "user_offsets";

    ThreadPoolExecutor executor = new ThreadPoolExecutor(30, 80, 2000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(200000));

    @RequestMapping("/sync_single_multi_question")
    public void syncMultiQuestion(@RequestParam int questionId, HttpServletRequest httpServletRequest) throws SQLException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        String sql = "select * from v_multi_question where PUKEY="+questionId;
        final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        importMulti2mongo(sqlRowSet);
    }

    @RequestMapping("/import_multi_question")
    public void importMuilQuestion(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        try {
            boolean more = true;
            int start = 0;
            int count = 1000;
            while (more) {
                String sql = "select * from v_multi_question limit " + start + "," + count;
                final SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql);
                final Map<Integer, Integer> pointMap = getPointMap();
                more = importMulti2mongo(resultSet);
                start = start + count - 1;
                System.out.println("--->proocess=" + start);
            }
            logger.info("多选题处理完毕");
        } catch (Throwable e) {
            logger.error("ex",e);
        }
    }

    private boolean importMulti2mongo(SqlRowSet resultSet) throws SQLException {
        boolean more = false;
        while (resultSet.next()) {
            more = true;
            final String item_1_type = resultSet.getString("item_1_type");
            final int id = resultSet.getInt("PUKEY");
            final String stem = resultSet.getString("stem");
            final String item_1 = resultSet.getString("item_1");
            final String item_2 = resultSet.getString("item_2");
            final String item_3 = resultSet.getString("item_3");
            final String item_4 = resultSet.getString("item_4");
            final String item_5 = resultSet.getString("item_5");
            final String item_6 = resultSet.getString("item_6");
            final String item_7 = resultSet.getString("item_7");
            final String item_8 = resultSet.getString("item_8");
            final String item_9 = resultSet.getString("item_9");
            final String item_10 = resultSet.getString("item_10");
            String source = resultSet.getString("source");
            String info_from = resultSet.getString("info_from");
            if(StringUtils.isBlank(source)){
                if(StringUtils.isBlank(info_from)){
                    source = "";
                }else{
                    source = info_from;
                }
            }
            String from = source;
            int isTrue = resultSet.getInt("is_ture_answer");
            String source_area = resultSet.getString("source_area");
            String source_year = resultSet.getString("source_year");
            Integer BB102 = Integer.valueOf(resultSet.getString("BB102"));
            Integer BB1B1 = Integer.valueOf(resultSet.getString("BB1B1"));
            String BB103 = resultSet.getString("BB103");//创建时间
            String BB105 = resultSet.getString("BB105");//创建者id
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        CompositeQuestion question = new CompositeQuestion();

                        if (item_1_type.equals("s")) {
                            return;
                        }
                        question.setId(Integer.valueOf("200" + Strings.padStart(id + "", 4, '0')));
                        question.setMaterial(stem);
                        question.setMaterials(Lists.newArrayList(stem));
                        List<Integer> subQuestions = new ArrayList<Integer>();
                        Integer idd = Ints.tryParse(item_1);
                        updateQuestion(question.getId(), stem, subQuestions, idd);

                        idd = Ints.tryParse(item_2);
                        updateQuestion(question.getId(), stem, subQuestions, idd);
                        idd = Ints.tryParse(item_3);
                        updateQuestion(question.getId(), stem, subQuestions, idd);
                        idd = Ints.tryParse(item_4);
                        updateQuestion(question.getId(), stem, subQuestions, idd);
                        idd = Ints.tryParse(item_5);
                        updateQuestion(question.getId(), stem, subQuestions, idd);
                        idd = Ints.tryParse(item_6);
                        updateQuestion(question.getId(), stem, subQuestions, idd);
                        idd = Ints.tryParse(item_7);
                        updateQuestion(question.getId(), stem, subQuestions, idd);
                        idd = Ints.tryParse(item_8);
                        updateQuestion(question.getId(), stem, subQuestions, idd);
                        idd = Ints.tryParse(item_9);
                        updateQuestion(question.getId(), stem, subQuestions, idd);
                        idd = Ints.tryParse(item_10);
                        updateQuestion(question.getId(), stem, subQuestions, idd);

                        question.setQuestions(subQuestions);
                        int mode = 0;
                        if (isTrue > 0) {//真题
                            mode = QuestionMode.QUESTION_TRUE;
                        } else {//模拟题
                            mode = QuestionMode.QUESTION_SIMULATION;
                        }
                        //同步试卷后再设置来源
                        Question compositeQuestion = QuestionCache.get(question.getId());

                        if(compositeQuestion==null){
                            compositeQuestion = questionDao.findById(question.getId());
                        }
                        if(compositeQuestion!=null&&compositeQuestion instanceof CompositeQuestion){
                            if(StringUtils.isBlank(compositeQuestion.getFrom())){
                                question.setFrom(from);
                            }else{
                                question.setFrom(compositeQuestion.getFrom());
                            }
                            question.setMode(compositeQuestion.getMode());
                        } else{
                            question.setFrom(from);

                        }
                        question.setMode(mode);
                        Integer year = Ints.tryParse(source_year);
                        if (year == null) {
                            year = 2012;
                        }
                        question.setYear(year);
                        question.setArea(Integer.valueOf(source_area));
                        int status = 0;
                        if (BB102 < 1) {//删除
                            status = QuestionStatus.DELETED;
                        }  else {//审核失败
                            status = QuestionStatus.AUDIT_SUCCESS;
                        }
                        question.setStatus(status);
                        question.setCreateTime(Long.valueOf(BB103) * 1000);
                        question.setCreateBy(Integer.valueOf(BB105));

                        //公务员考试
                        question.setSubject(SubjectType.GWY_XINGCE);
                        try {
                            questionDubboService.update(question);
                        } catch (IllegalQuestionException e) {
                            logger.error("非法的试题",e);
                        }
                    }catch (Exception e){
                        logger.error("ex",e);
                    }

                }
                private final void updateQuestion(int id, String stem, List<Integer> subQuestions, Integer idd){
                    try {
                        GenericQuestion question1;
                        if (idd != null && idd > 0) {
                            subQuestions.add(idd);
                            question1 = (GenericQuestion) questionDubboService.findById(idd);
                            if (question1 != null) {
                                question1.setParent(id);
                                question1.setMaterial(stem);
                                question1.setMaterials(Lists.newArrayList(stem));
                                try {
                                    questionDubboService.update(question1);
                                } catch (IllegalQuestionException e) {
                                    logger.error("非法的试题",e);
                                }
                            }
                        }
                    }catch (Exception e){
                        logger.error("ex",e);
                    }

                }
            });

        }
        return more;
    }



    @RequestMapping("/init_question")
    public void importData(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        try {
            //删除旧数据
//            mongoTemplate.dropCollection("ztk_question");
            logger.info("start clean question keys.");
            boolean more = true;
            int start = 0;
            int count = 1000;
            while (more) {
                String sql = "select * from v_obj_question limit " + start + "," + count;
                final SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql);
                final Map<Integer, Integer> pointMap = getPointMap();
                more = import2mongo(resultSet, pointMap, "v_question_pk_r");
                start = start + count - 1;
                System.out.println("--->proocess=" + start);
            }
            logger.info("单选试题处理完毕");
        } catch (Throwable e) {
            logger.error("ex",e);
        }

        try {
            Thread.sleep(600000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //导入多选题
        importMuilQuestion(null);
    }


    /**
     * 获取知识点的对应关系 key：pointId value：parentId
     *
     * @return
     * @throws SQLException
     */
    public Map<Integer, Integer> getPointMap() throws SQLException {
        String sql = "SELECT PUKEY,name,prev_kp FROM v_knowledge_point  order by prev_kp ASC,PUKEY ASC ";
        final SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql);
        Map<Integer, Integer> data = new HashMap<Integer, Integer>();
        while (resultSet.next()) {
            int pointId = resultSet.getInt("PUKEY");
            int parent = resultSet.getInt("prev_kp");
            String name = resultSet.getString("name");
            data.put(pointId, parent);
            point_map.put(pointId,name);
        }
        return data;
    }

    @RequestMapping("/sync_single_question")
    public void syncQuestion(@RequestParam  int questionId,HttpServletRequest httpServletRequest) throws SQLException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        String sql = "select * from v_obj_question where PUKEY="+questionId;
        try{
            final SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
            final Map<Integer, Integer> pointMap = getPointMap();
            import2mongo(sqlRowSet,pointMap, "v_question_pk_r");
        }catch (NullPointerException e){
            logger.info("question = {} 不存在，默认为物理删除" );
            try {
                Question question = questionDubboService.findById(questionId);
                GenericQuestion genericQuestion = (GenericQuestion)question;
                genericQuestion.setStatus(QuestionStatus.DELETED);
                questionDubboService.update(genericQuestion);
            } catch (IllegalQuestionException e1) {
                logger.error("非法的试题",e1);
            }catch (Throwable e2){
                e2.printStackTrace();
            }
        }

    }



    public static final Set tags = new HashSet<>();
    private boolean import2mongo(SqlRowSet resultSet, Map<Integer, Integer> pointMap, String questionPkTable) throws SQLException {
        boolean moreData = false;
        while (resultSet.next()) {
            moreData = true;
            String choice_5 = format(resultSet.getString("choice_5"));
            String choice_6 = format(resultSet.getString("choice_6"));
            String choice_7 = format(resultSet.getString("choice_7"));
            String choice_8 = format(resultSet.getString("choice_8"));
            String choice_9 = format(resultSet.getString("choice_9"));
            String choice_10 = resultSet.getString("choice_10");
            //{%NULL%}
            String stand_answer = resultSet.getString("stand_answer");
            String answer_comment = format(resultSet.getString("answer_comment"));
            String info_from = resultSet.getString("info_from");
            String source = resultSet.getString("source");
            if(StringUtils.isBlank(source)){
                if(StringUtils.isBlank(info_from)){
                    source = "";
                }else{
                    source = info_from;
                }
            }
            String from = source;
            String source_year = resultSet.getString("source_year");
            String source_area = resultSet.getString("source_area");
            String point = resultSet.getString("point");//分数
            Float difficult_grade = Float.valueOf(resultSet.getString("difficult_grade"));//难度系数
            final String stem = format(resultSet.getString("stem"));
            Integer BB102 = Integer.valueOf(resultSet.getString("BB102"));
            String BB103 = resultSet.getString("BB103");//创建时间
            String BB105 = resultSet.getString("BB105");//创建者id
            int multi_id = resultSet.getInt("multi_id");//创建者id

            int recommendTime = resultSet.getInt("reference_time_limit");

            final AtomicInteger mode = new AtomicInteger();
            try {
                int isTrue = resultSet.getInt("is_ture_answer");
                if (isTrue > 0) {//真题
                    mode.set(QuestionMode.QUESTION_TRUE);
                } else {//模拟题
                    mode.set(QuestionMode.QUESTION_SIMULATION);
                }
            }catch (InvalidResultSetAccessException e){//说明是boolean类型
                if (resultSet.getBoolean("is_ture_answer")) {
                    mode.set(QuestionMode.QUESTION_TRUE);
                }else {
                    mode.set(QuestionMode.QUESTION_SIMULATION);
                }
            }
            String choice_1 = format(resultSet.getString("choice_1"));
            String choice_2 = format(resultSet.getString("choice_2"));
            String choice_3 = format(resultSet.getString("choice_3"));
            String choice_4 = format(resultSet.getString("choice_4"));
            String extend = format(resultSet.getString("answer_expand"));
            int id = resultSet.getInt("PUKEY");
            int type = resultSet.getInt("type_id");
            executor.submit(new Runnable() {

                /**
                 * When an object implementing interface <code>Runnable</code> is used
                 * to create a thread, starting the thread causes the object's
                 * <code>run</code> method to be called in that separately executing
                 * thread.
                 * <p>
                 * The general contract of the method <code>run</code> is that it may
                 * take any action whatsoever.
                 *
                 * @see Thread#run()
                 */
                @Override
                public void run() {
                    try {
                        GenericQuestion genericQuestion = new GenericQuestion();


                        genericQuestion.setId(id);
                        genericQuestion.setType(type);
                        List<String> choices = new ArrayList<String>();
                        if (StringUtils.isNoneBlank(choice_1)&&!choice_1.equals("{%NULL%}")) {
                            choices.add(choice_1);
                        }
                        if (StringUtils.isNoneBlank(choice_2)&&!choice_2.equals("{%NULL%}")) {
                            choices.add(choice_2);
                        }
                        if (StringUtils.isNoneBlank(choice_3)&&!choice_3.equals("{%NULL%}")) {
                            choices.add(choice_3);
                        }
                        if (StringUtils.isNoneBlank(choice_4)&&!choice_4.equals("{%NULL%}")) {
                            choices.add(choice_4);
                        }

                        if (StringUtils.isNoneBlank(choice_5)&&!choice_5.equals("{%NULL%}")) {
                            choices.add(choice_5);
                        }
                        if (StringUtils.isNoneBlank(choice_6)&&!choice_6.equals("{%NULL%}")) {
                            choices.add(choice_6);
                        }
                        if (StringUtils.isNoneBlank(choice_7)&&!choice_7.equals("{%NULL%}")) {
                            choices.add(choice_7);
                        }
                        if (StringUtils.isNoneBlank(choice_8)&&!choice_8.equals("{%NULL%}")) {
                            choices.add(choice_8);
                        }
                        if (StringUtils.isNoneBlank(choice_9)&&!choice_9.equals("{%NULL%}")) {
                            choices.add(choice_9);
                        }
                        if (StringUtils.isNoneBlank(choice_10)&&!choice_10.equals("{%NULL%}")) {
                            choices.add(choice_10);
                        }
                        if (StringUtils.isBlank(extend)||extend.equals("{%NULL%}")) {
                            genericQuestion.setExtend("");
                        }else{
                            genericQuestion.setExtend(extend);
                        }
                        genericQuestion.setChoices(choices);
                        genericQuestion.setAnswer(answerParse(stand_answer));
                        genericQuestion.setAnalysis(answer_comment);
                        genericQuestion.setYear(Integer.valueOf(source_year));
                        genericQuestion.setArea(Integer.valueOf(source_area));
                        genericQuestion.setScore(Float.valueOf(point));

                        int difficult = 3;
                        if (difficult_grade <= -2.4) {
                            difficult = DifficultGrade.SO_EASY;
                        } else if (difficult_grade > -2.4 && difficult_grade <= -1.2) {
                            difficult = DifficultGrade.EASY;
                        } else if (difficult_grade > -1.2 && difficult_grade <= 0) {
                            difficult = DifficultGrade.GENERAL;
                        } else if (difficult_grade > 0 && difficult_grade <= 1.2) {
                            difficult = DifficultGrade.DIFFICULT;
                        }else if (difficult_grade > 1.2) {
                            difficult = DifficultGrade.SO_DIFFICULT;
                        } else {
                            difficult = DifficultGrade.GENERAL;
                        }
                        genericQuestion.setStem(stem);
                        genericQuestion.setDifficult(difficult);

                        //简单设置推荐时间
                        genericQuestion.setRecommendedTime(recommendTime == 0 ? difficult * 10 : recommendTime);

                        genericQuestion.setMode(mode.get());
                        int status = 0;
                        if (BB102 < 1) {//删除
                            status = QuestionStatus.DELETED;
                        } else {//审核成功
                            status = QuestionStatus.AUDIT_SUCCESS;
                        }
                        genericQuestion.setStatus(status);
                        genericQuestion.setCreateTime(Long.valueOf(BB103) * 1000);
                        if (multi_id > 0) {//有复合题id
                            genericQuestion.setParent(Integer.valueOf("200" + Strings.padStart(multi_id + "", 4, '0')));

                            //设置材料
                            String sql = "SELECT * FROM v_multi_question WHERE PUKEY=" + multi_id;
                            SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql);
                            if (rowSet.next()) {
                                genericQuestion.setMaterial(format(rowSet.getString("stem")));
                                genericQuestion.setMaterials(Lists.newArrayList(genericQuestion.getMaterial()));
                            } else {
                                //查询不到材料题,设置为删除状态
                                genericQuestion.setStatus(QuestionStatus.DELETED);
                            }
                        }
                        genericQuestion.setCreateBy(Integer.valueOf(BB105));
                        //设置知识点
                        List<Integer> newPoints = getKnowledge(genericQuestion.getId(),pointMap,questionPkTable);
                        genericQuestion.setPoints(newPoints);
                        //比较知识点变动，对未删除的试题，如果知识点发生变动，则记录日志，用于用户知识点更新
                        Question oldQuestion = null;
                        try{
                            oldQuestion = QuestionCache.get( id );
                        }catch (Exception e){
                            e.printStackTrace();
                        }catch (NoClassDefFoundError e1){
                            e1.printStackTrace();
                        }
                        if(oldQuestion==null){
                            oldQuestion = questionDubboService.findById( id );
                        }
                        try{
                            if(oldQuestion!=null){
                                if(StringUtils.isBlank(oldQuestion.getFrom())){
                                    //如果之前的试题来源为空，则使用新获取的试题自身来源
                                    genericQuestion.setFrom(from);
                                }else{
                                    //如果之前的试题来源不为空，则使用旧的试题来源
                                    genericQuestion.setFrom(oldQuestion.getFrom());
                                }
                                List<Integer> oldPoints = ((GenericQuestion)oldQuestion).getPoints();
                                List<QuestionPointChange> changes = Lists.newArrayList();
                                for(int i=0;i<newPoints.size();i++){
                                    int newPoint = newPoints.get( i );
                                    int oldPoint = oldPoints.get( i );
                                    if(oldPoint!=newPoint){
                                        insertPointChangeLog(id,newPoint,oldPoint,i);
                                    }
                                }
                            }else{
                                //试题添加，来源取自自身
                                genericQuestion.setFrom(from);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        List<String> pointsName = new ArrayList<String>();
                        for (Integer integer : genericQuestion.getPoints()) {
                            final String pointName = point_map.get(integer);
                            if (pointName != null) {
                                pointsName.add(pointName);
                            }
                        }
                        genericQuestion.setPointsName(pointsName);
                        //公务员
                        genericQuestion.setSubject(SubjectType.GWY_XINGCE);
                        try {
                            questionDubboService.update(genericQuestion);
                        } catch (IllegalQuestionException e) {
                            logger.error("非法的试题",e);
                        }catch (Throwable e){
                            e.printStackTrace();
                        }
                    }catch (Exception e){
                        logger.error("ex",e);
                    }
                }
            });
        }
        return moreData;
    }

    /**
     *将试题一、二、三级知识点变动情况记录到日志表中，方便之后定时批量处理
     * @param id
     * @param newPoint
     * @param oldPoint
     * @param i
     */
    public void insertPointChangeLog(int id, int newPoint, int oldPoint, int i) {
        long time = System.currentTimeMillis()/1000;
        String sql = "insert into v_question_pk_r_change_log (question_id,pk_id,pk_old_id,BB1B1,bb102,bb103,point_level) values (?,?,?,-1,-1,?,?)";
        Object[] params = {id,newPoint,oldPoint,time,i};
        jdbcTemplate.update( sql,params );
        String querySql = "select max(pukey) from v_question_pk_r_change_log";
        Integer maxSize = jdbcTemplate.queryForObject( querySql,Integer.class );
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put( USER_OFFSETS,-1+"",maxSize+"" );
    }

    /**
     * 获取问题的知识点id
     * @param questionId
     * @return
     */
    public List<Integer> getKnowledge(int questionId,Map<Integer,Integer> pointMap,String questionPkTable){
        String sql = "select * from " + questionPkTable + " where question_Id=" +questionId + " and bb102=1";
        List<Integer> ids = new ArrayList<Integer>();
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql);
        while (resultSet.next()){
            final int pkId = resultSet.getInt("pk_id");
            ids.add(pkId);
        }

        Integer oneLevel = 0;
        Integer twoLevel = 0;
        Integer threeLevel = 0;
        try {//[754, 761, 769])
            //panent-> sub list
            final ArrayListMultimap<Integer, Integer> multimap = ArrayListMultimap.create();
            for (Integer id : ids) {
                Integer parent = pointMap.get(id);
                multimap.put(parent,id);
                while (parent>0){
                    int sub = parent;
                    parent = pointMap.get(sub);
                    multimap.put(parent,sub);
                }
            }

            oneLevel = multimap.get(0).get(0);//one level
            twoLevel = multimap.get(oneLevel).get(0);
            threeLevel = multimap.get(twoLevel).get(0);
        }catch (Exception e){
            oneLevel = 392;
            twoLevel = 398;
            threeLevel = 403;
        }
        return Ints.asList(oneLevel,twoLevel,threeLevel);
    }

    private String format(String source) {

        if (StringUtils.isBlank(source)) {
            return "";
        }
        return source;
    }

    public int answerParse(String standAnswer) {
        Map<String, String> answerMap = new HashMap<String, String>();
        answerMap.put("A", "1");
        answerMap.put("B", "2");
        answerMap.put("C", "3");
        answerMap.put("D", "4");
        answerMap.put("E", "5");
        answerMap.put("F", "6");
        answerMap.put("G", "7");
        answerMap.put("H", "8");

        char[] chars = standAnswer.toUpperCase().toCharArray();
        Arrays.sort(chars);
        StringBuilder sb = new StringBuilder();
        for (char aChar : chars) {
            final String ss = answerMap.get(aChar + "");
            if (ss != null) {
                sb.append(ss);
            }
        }
        if (sb.length() < 1) {
            sb.append("3");
        }

        return Integer.valueOf(sb.toString());
    }


    @RequestMapping("handleImg")
    public void handleImg() throws Exception {
        handleObjImg();
        handleMultiImg();
    }

    /**
     * 处理试题中的img标签
     */
    private void handleObjImg() {
        String sql = "SELECT * FROM v_obj_question WHERE stem LIKE '%[img %' " +
                "or answer_comment LIKE '%[img %' " +
                "or choice_1 LIKE '%[img %' " +
                "or choice_2 LIKE '%[img %' " +
                "or choice_3 LIKE '%[img %' " +
                "or choice_4 LIKE '%[img %'";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        int count = 0;

        while (sqlRowSet.next()) {
            String pukey = sqlRowSet.getString("PUKEY");
            String stem = sqlRowSet.getString("stem");
            String answerComment = sqlRowSet.getString("answer_comment");
            String choice_1 = sqlRowSet.getString("choice_1");
            String choice_2 = sqlRowSet.getString("choice_2");
            String choice_3 = sqlRowSet.getString("choice_3");
            String choice_4 = sqlRowSet.getString("choice_4");

            Object[] params = {
                    makeImgStr(stem),
                    makeImgStr(answerComment),
                    makeImgStr(choice_1),
                    makeImgStr(choice_2),
                    makeImgStr(choice_3),
                    makeImgStr(choice_4),
                    pukey
            };

            jdbcTemplate.update("UPDATE v_obj_question set" +
                    "  stem = ?," +
                    "answer_comment = ?," +
                    "choice_1 = ?," +
                    "choice_2 = ?," +
                    "choice_3 = ?," +
                    "choice_4 = ? " +
                    "WHERE PUKEY=?",params);
            count++;
        }

        logger.info("handle obj img count ={}", count);
    }

    /**
     * 处理复合题中的img标签
     */
    public void handleMultiImg() {
        String sql = "SELECT * FROM v_multi_question WHERE stem LIKE '%[img %'";
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        int count = 0;
        while (sqlRowSet.next()) {
            String pukey = sqlRowSet.getString("PUKEY");
            String stem = sqlRowSet.getString("stem");

            Object[] params = {
                    makeImgStr(stem),
                    pukey
            };

            jdbcTemplate.update("UPDATE v_multi_question set" +
                    "  stem = ? " +
                    "WHERE PUKEY=?",params);
            count++;
        }
        logger.info("handle multi img count ={}",count);
    }

    private static String makeImgStr(String str) {
        return str.replaceAll("\\[img.*?\\]", "[img]");
    }


    private static String makeSpanStr(String str) {
        return str.replaceAll(" "," ").replaceAll("<span style=\"text-decoration:underline;\">[\\s]+</span>", "_____");
    }


    @RequestMapping("/import_some_question")
    public void syncQuestion2(HttpServletRequest httpServletRequest) throws SQLException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        String osql = "SELECT * FROM v_obj_question WHERE stem LIKE '%（%' or stem LIKE '%(%' ";
        final SqlRowSet osqlRowSet = jdbcTemplate.queryForRowSet(osql);
        final Map<Integer, Integer> pointMap = getPointMap();
        import2mongo(osqlRowSet, pointMap, "v_question_pk_r");
    }

    @RequestMapping("/filter_multi_questions")
    public void filterMulti(HttpServletRequest httpServletRequest){
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        String sql = "SELECT * FROM v_multi_question WHERE is_ture_answer=1 and BB102=1";
        final SqlRowSet osqlRowSet = jdbcTemplate.queryForRowSet(sql);
        Map<Integer,String> data = Maps.newHashMap();
        while (osqlRowSet.next()){
            final int pukey = osqlRowSet.getInt("pukey");
            final String stem = osqlRowSet.getString("stem");
            final String text = Jsoup.parse(stem).text();
            //带有图片的不处理
            if (stem.contains("[img") && text.length()<30) {
                continue;
            }

            if (text.length() < 10) {
                continue;
            }
            data.put(pukey, text);
        }
        proccessRepeat(data);
    }

    /**
     * 去掉重复题目（不包含图片题）
     * @return
     */
    @RequestMapping("/filter_questions")
    public Set<Integer> filterIds(HttpServletRequest httpServletRequest) throws SQLException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return Sets.newCopyOnWriteArraySet();
        }
        logger.info("start proccess repeat question");
        //获取所有题目列表
        List<GenericQuestion> questions = getAllQuestion();
        logger.info("all questions size={}", questions.size());

        //过滤出题干长度大于20的题目(目的是防止部分题干较短的题目被误杀)
        List<GenericQuestion> stemFilteredQuestions = new ArrayList<>();
        for (GenericQuestion question : questions) {
            String stem = question.getStem();
            String stemStr = removeTags(stem);
            if (StringUtils.isNoneEmpty(stemStr) && stemStr.length() >= 20) {
                stemFilteredQuestions.add(question);
            }
        }
        logger.info("stemFilteredQuestions size={}", stemFilteredQuestions.size());

        //将题干和选项信息去标签和标点符号之后存入list中
        Map<Integer,String> data = Maps.newHashMap();
        for (GenericQuestion question : questions) {
            Integer id = question.getId();
            String choice1 = question.getChoices().get(0);
            String choice2 = question.getChoices().get(1);
            String choice3 = question.getChoices().get(2);
            String choice4 = question.getChoices().get(3);
            final String stem = Jsoup.parse(question.getStem()).text();
            if (StringUtils.isBlank(stem)) {//题干是一个图片
                continue;
            }
            //图形推理不处理
            if (((GenericQuestion) questionDubboService.findById(id)).getPoints().get(1) == 643) {
                continue;
            }
            String str = stem
                    + Jsoup.parse(choice1 ).text()
                    + Jsoup.parse(choice2 ).text()
                    + Jsoup.parse(choice3 ).text()
                    + Jsoup.parse(choice4).text();
            data.put(id,str);
        }
        proccessRepeat(data);
        return new HashSet<>();
    }

    private void proccessRepeat(Map<Integer, String> data) {
        int i = 0;
        final File file = new File(System.getProperty("server_home") + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        try {
            FileUtils.touch(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final List<Integer> keys = data.keySet().stream().collect(Collectors.toList());
        for (Map.Entry<Integer, String> entry : data.entrySet()) {
            i++;
            if (i % 500 == 0) {
                logger.info("proccess count={}",i);
            }
            logger.info("proccess questionId={}",entry.getKey());
            final int length = entry.getValue().length();
            for (int j = 0; j < keys.size(); j++) {
                final Integer qid2 = keys.get(j);
                final String value2 = data.get(qid2);
                if (qid2 == entry.getKey()) {//自己
                    continue;
                }
                if (entry.getKey() > qid2) {//已经对比过,无需进行处理
                    continue;
                }

                final int length1 = value2.length();
                //两个题干差距大,说明不是同一题可能性大
                if (Math.abs(length - length1) > Math.min(length,length1)) {
                    continue;
                }
                try {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final double val = SimilarityUtil.SimilarDegree(entry.getValue(), value2) * 100;
                                if (Double.isNaN(val)) {
                                    return;
                                }
                                final int similarDegree = BigDecimal.valueOf(val).intValue();
                                if (similarDegree > 70) {
                                    logger.info("add similarDegree={},qid1={},qid2={}",similarDegree,entry.getKey(),qid2);
                                    Files.append(String.join(",",similarDegree+"",entry.getKey()+"",qid2+"")+"\r\n",file, Charset.defaultCharset());
                                }
                            }catch (Exception e){
                                logger.error("ex",e);
                            }

                        }
                    });
                }catch (Exception e){
                    try {
                        TimeUnit.SECONDS.sleep(50);
                    } catch (InterruptedException e1) {
                    }
                }
            }

        }
    }


    //去所有标签、去标点符号
    private String removeTags(String inputString) {
        if (inputString == null)
            return null;
        String htmlStr = inputString;
        String textStr = "";
        String resultStr = "";
        Pattern p_script;
        Matcher m_script;
        Pattern p_style;
        Matcher m_style;
        Pattern p_html;
        Matcher m_html;
        try {
            //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
            //定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
            String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            htmlStr = m_script.replaceAll(""); // 过滤script标签
            p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            htmlStr = m_style.replaceAll(""); // 过滤style标签
            p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            htmlStr = m_html.replaceAll(""); // 过滤html标签
            textStr = htmlStr;
            //去所有标点,去空格符号
            resultStr = textStr.replaceAll("(?i)[^a-zA-Z0-9\u4E00-\u9FA5]", "").replaceAll("nbsp","");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultStr;// 返回文本字符串

    }

    //根据id获取单个试题
    public GenericQuestion getSingleQuestion(int questionId) {
        String sql = "select * from v_obj_question where PUKEY=" + questionId;
        final SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        List<GenericQuestion> questions = new ArrayList<>();

        while (rs.next()) {
            String stem = rs.getString("stem");
            String analysis = rs.getString("answer_comment");
            GenericQuestion genericQuestion = new GenericQuestion();

            genericQuestion.setStem(stem);
            genericQuestion.setAnalysis(analysis);
            questions.add(genericQuestion);
        }
        if (questions != null && questions.size() > 0) {
            return questions.get(0);
        }
        return new GenericQuestion();

    }

    /**
     * 查询所有不包含图片试题
     *
     * @return
     */
    public List<GenericQuestion> getAllQuestion() {

        String sql = "SELECT * FROM v_obj_question where BB102 = 1 and multi_id<1 and is_ture_answer=1";

        return getGenericQuestions(sql);

    }

    /**
     * 查询所有审核通过的图片题单独处理
     * @return
     */
    public List<GenericQuestion> getAllPictureQuestion() {

        String sql = "SELECT * FROM v_obj_question where BB102 = 1 and stem like '%img%' ";

        return getGenericQuestions(sql);
    }

    private List<GenericQuestion> getGenericQuestions(String sql) {
        List<GenericQuestion> questions = new ArrayList<>();

        final SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {

            Integer id = rs.getInt("PUKEY");
            String stem = rs.getString("stem");
            String analysis = rs.getString("answer_comment");
            String choice1 = rs.getString("choice_1");
            String choice2 = rs.getString("choice_2");
            String choice3 = rs.getString("choice_3");
            String choice4 = rs.getString("choice_4");
            Integer bb102 = rs.getInt("BB102");

            List list = new ArrayList<>();
            list.add(choice1);
            list.add(choice2);
            list.add(choice3);
            list.add(choice4);

            GenericQuestion genericQuestion = new GenericQuestion();

            genericQuestion.setId(id);
            genericQuestion.setStem(stem);
            genericQuestion.setAnalysis(analysis);
            genericQuestion.setChoices(list);
            genericQuestion.setStatus(bb102);

            questions.add(genericQuestion);
        }

        return questions;
    }

    /**
     * 获取去重所需idList
     *
     * @param stemList
     * @param similarDegree 相似度阈值
     * @return
     */
    private Set<Integer> getNeedRemoveIds(List<String> stemList, double similarDegree) {
        Set<Integer> idSet = new HashSet<>();
        //遍历list，类似于从单个list取出相同元素操作
        try {
            for (int i = 0; i < stemList.size() - 1; i++) {
                //打印处理进度
                logger.info("all count={} , processing sequence={}", stemList.size(), i + 1);
                String firstStr = stemList.get(i);
                for (int j = stemList.size() - 1; j > i; j--) {
                    String secondStr = stemList.get(j);
                    if (SimilarityUtil.SimilarDegree(firstStr, secondStr) > similarDegree) {//比较两字符的相似度
                        //获取相似度较高的两个题目的id
                        String firstId = firstStr.substring(0, firstStr.indexOf(","));
                        String secondId = secondStr.substring(0, secondStr.indexOf(","));
                        //根据id获取对应的试题
                        GenericQuestion firstQuestion = getSingleQuestion(Ints.tryParse(firstId));
                        GenericQuestion secondQuestion = getSingleQuestion(Ints.tryParse(secondId));
                        //logger.info("firstStem={}", firstStr);
                        //logger.info("secondStem={}", secondStr);
                        //取出解析较短所对应的题目,以进一步去重
                        Integer firstAnalysisLength = removeTags(firstQuestion.getAnalysis()).length();
                        Integer secondAnalysisLength = removeTags(secondQuestion.getAnalysis()).length();
                        if (firstAnalysisLength >= secondAnalysisLength) {
                            idSet.add(Ints.tryParse(secondId));
                            logger.info(">>>save id={},delete id={}",Ints.tryParse(firstId),Ints.tryParse(secondId));
                        } else {
                            idSet.add(Ints.tryParse(firstId));
                            logger.info(">>>save id={},delete id={}",Ints.tryParse(secondId),Ints.tryParse(firstId));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ex", e);
        }
        return idSet;
    }

    /**
     * 处理重复试题
     *
     * @param idSet
     */
    private void filterMethod(Set<Integer>  idSet) throws SQLException {
        for (Integer qid : idSet) {
            String sql = "update v_obj_question set bb102=-3 where pukey=" + qid;
            jdbcTemplate.update(sql);
            logger.info("allCount={},update bb102=-3,qid={}", idSet.size(), qid);
        }
    }

    /**
     * 去除所有重复图片题
     * @return
     */
    @RequestMapping("/filter_picquestions")
    public Set<Integer> filterPictureQuestions(HttpServletRequest httpServletRequest) throws SQLException {

        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return Sets.newHashSet();
        }
        //获取所有题目列表
        List<GenericQuestion> questions = getAllPictureQuestion();
        logger.info("all PictureQuestion size={}",questions.size());

        //将题干和选项信息去标签和标点符号之后存入list中
        List<String> stemList = new ArrayList<>();
        for (GenericQuestion question : questions) {
            Integer id = question.getId();
            String stem = question.getStem();
            String choice1 = question.getChoices().get(0);
            String choice2 = question.getChoices().get(1);
            String choice3 = question.getChoices().get(2);
            String choice4 = question.getChoices().get(3);
            //取出图片名称（SHA_1值），会有可能出现题干只有图片的题目
            String SHA1Value = getSha1Value(stem);
            String str = stem + choice1 + choice2 + choice3 + choice4;
            String strResult = String.valueOf(id) + "," + removeTags(str) + SHA1Value;
            stemList.add(strResult);
        }

        /*
          针对图片题来说，很大一部分是这种类型：
          “请从所给的四个选项中选择最合适的一个填入问号处使之呈现一定的规律性” ---仅仅是图片不一样
          这时相似度要调整到很大才有可能避免重复，同时不误杀一大部分题
        */
        Set<Integer> idSet = getNeedRemoveIds(stemList,0.9);
        logger.info("idSet size={}", idSet.size());
        //filterMethod(idSet);
        return idSet;
    }

    //取出图片名称（SHA_1值）
    private String getSha1Value(String stem) {
        //System.out.println(stem);
        //此处为防止<p><!--[img]2f8d0e18356abbae76f87bf1b9d7e3e3de1cc8a3.png[/img]--></p>jsop解析为空
        String stem1 = "abc" + stem;
        String stemStr = stem1.replaceAll("<p>", "").replaceAll("</p>", "")
                .replaceAll("<p.*?>", "")
                .replaceAll("<span.*?>", "")
                .replaceAll("</span>","")
                .replaceAll("</br>", "")
                .replaceAll("<br>", "");
        String SHA1Value = "";
        Element body = Jsoup.parse(stemStr).body();
        //System.out.println(body);
        List<Node> roots = Lists.newArrayList(body.childNodes());
        //System.out.println(body.childNodes());
        for (Node node : roots) {
            if (node instanceof Comment) {//注释
                final Comment comment = (Comment) node;
                //<!--[img]34a88f0a1509b896e38a8e68e603a9765310bf10.png[/img]-->
                String data = comment.getData().replaceAll(" ", "");
                if (data.startsWith("[img]")) {
                    SHA1Value = proccessImage(data);
                }
            }
        }
        return SHA1Value;
    }

    /**
     * 处理图片
     * <!--[img ]5c034325fb29abe7398872a54cc24da433238d76.png[/img]-->
     * @param data
     * @return 图片的链接地址
     */
    private static String proccessImage(String data) {
        data = data.replaceAll(" ","");
        final int start = data.indexOf("[img]") + 5;
        final int end = data.indexOf("[/img]");
        if (end<=start) {
            logger.error("vilad img tag,data={}",data);
            return "";
        }
        String imageStr = data.substring(start, end);
        return imageStr;
    }

    @RequestMapping("importQuestionRecord")
    public void importAnswer(HttpServletRequest httpServletRequest){
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        if (importAnswer) {
            return;
        }
        importAnswer = true;

        long maxId = 0;
        AtomicInteger counter = new AtomicInteger();
        while (true){
            Query query = new Query(Criteria.where("_id").gt(maxId));
            query.limit(1000).with(new Sort(new Sort.Order("_id")));
            final List<AnswerCard> answerCards = mongoTemplate.find(query, AnswerCard.class);
            for (AnswerCard answerCard : answerCards) {
                if (counter.incrementAndGet() % 10000 == 0) {
                    logger.info("proccess = {}",counter.get());
                }
                maxId = Math.max(maxId,answerCard.getId());
                try {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                proccessAnswers(answerCard);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });
                }catch (RejectedExecutionException executionException){
                    try {
                        Thread.sleep(200000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                proccessAnswers(answerCard);
                            } catch (Exception e) {
                                logger.error("ex", e);
                            }
                        }
                    });

                }
            }

            if (answerCards.size() < 50) {//小于一定数量，说明已经处理完毕
                logger.info("导入答案完毕...........");
                break;
            }
        }

    }

    private void proccessAnswers(AnswerCard answerCard) {
        List<Answer> answerList = Lists.newArrayList();
        final int[] answers = Arrays.stream(answerCard.getAnswers()).mapToInt(Integer::valueOf).toArray();
        final int[] corrects = answerCard.getCorrects();
        final int[] times = answerCard.getTimes();
        List<Integer> questions = null;
        if (answerCard instanceof StandardCard) {
            StandardCard card = (StandardCard) answerCard;
            questions = card.getPaper().getQuestions();
        }else {
            PracticeCard card = (PracticeCard) answerCard;
            questions = card.getPaper().getQuestions();
        }

        for (int i = 0; i < questions.size(); i++) {
            if (answers[i] <= 0) {//没有作答的不进行处理
                continue;
            }

            final Answer answer = new Answer();
            answer.setQuestionId(questions.get(i));
            answer.setAnswer(answers[i] + "");
            answer.setCorrect(corrects[i]);
            answer.setTime(times[i]);
            answerList.add(answer);
        }
        final UserAnswers userAnswers = UserAnswers.builder()
                .uid(answerCard.getUserId())
                .area(9)
                .subject(1)
                .answers(answerList)
                .submitTime(System.currentTimeMillis())
                .build();
        questionRecordService.updateQuestionRecord(userAnswers);
    }

    /**
     * 更新标签
     */
    @RequestMapping(value = "/updateLable",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updateLable(HttpServletRequest httpServletRequest) throws IllegalQuestionException {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return null;
        }
        String osql = "SELECT *\n" +
                "FROM v_obj_question WHERE (stem LIKE '%underline%'\n" +
                "                          OR v_obj_question.choice_1 LIKE '%underline%'\n" +
                "                          OR v_obj_question.choice_2 LIKE '%underline%'\n" +
                "                          OR v_obj_question.choice_3 LIKE '%underline%'\n" +
                "                          OR v_obj_question.choice_4 LIKE '%underline%') ";
        SqlRowSet osqlRowSet = jdbcTemplate.queryForRowSet(osql);
        while (osqlRowSet.next()){
            String choice_1 = format(osqlRowSet.getString("choice_1"));
            String choice_2 = format(osqlRowSet.getString("choice_2"));
            String choice_3 = format(osqlRowSet.getString("choice_3"));
            String choice_4 = format(osqlRowSet.getString("choice_4"));
            String stem = format(osqlRowSet.getString("stem"));
            int id = osqlRowSet.getInt("PUKEY");
            final GenericQuestion question = (GenericQuestion)questionDubboService.findById(id);
            question.setStem(QuestionDubboServiceImpl.convert2MobileLayout(stem));
            System.out.println(question.getStem());
            question.getChoices().set(0,QuestionDubboServiceImpl.convert2MobileLayout(choice_1));
            question.getChoices().set(1,QuestionDubboServiceImpl.convert2MobileLayout(choice_2));
            question.getChoices().set(2,QuestionDubboServiceImpl.convert2MobileLayout(choice_3));
            question.getChoices().set(3,QuestionDubboServiceImpl.convert2MobileLayout(choice_4));
            questionDubboService.update(question);
        }

        osql = "SELECT * FROM v_multi_question WHERE stem LIKE '%underline%'";
        osqlRowSet = jdbcTemplate.queryForRowSet(osql);
        while (osqlRowSet.next()){
            final String item_1 = osqlRowSet.getString("item_1");
            final String item_2 = osqlRowSet.getString("item_2");
            final String item_3 = osqlRowSet.getString("item_3");
            final String item_4 = osqlRowSet.getString("item_4");
            final String item_5 = osqlRowSet.getString("item_5");
            final String item_6 = osqlRowSet.getString("item_6");
            String stem = format(osqlRowSet.getString("stem"));
            GenericQuestion question = (GenericQuestion)questionDubboService.findById(Ints.tryParse(item_1));
            if (question != null) {
                question.setMaterial(QuestionDubboServiceImpl.convert2MobileLayout(stem));
                questionDubboService.update(question);
            }

            question = (GenericQuestion)questionDubboService.findById(Ints.tryParse(item_2));
            if (question != null) {
                question.setMaterial(QuestionDubboServiceImpl.convert2MobileLayout(stem));
                questionDubboService.update(question);
            }

            question = (GenericQuestion)questionDubboService.findById(Ints.tryParse(item_3));
            if (question != null) {
                question.setMaterial(QuestionDubboServiceImpl.convert2MobileLayout(stem));
                questionDubboService.update(question);
            }

            question = (GenericQuestion)questionDubboService.findById(Ints.tryParse(item_4));
            if (question != null) {
                question.setMaterial(QuestionDubboServiceImpl.convert2MobileLayout(stem));
                questionDubboService.update(question);
            }

            question = (GenericQuestion)questionDubboService.findById(Ints.tryParse(item_5));
            if (question != null) {
                question.setMaterial(QuestionDubboServiceImpl.convert2MobileLayout(stem));
                questionDubboService.update(question);
            }

            question = (GenericQuestion)questionDubboService.findById(Ints.tryParse(item_6));
            if (question != null) {
                question.setMaterial(QuestionDubboServiceImpl.convert2MobileLayout(stem));
                questionDubboService.update(question);
            }

        }
        return SuccessMessage.create("更新已完成");
    }


    /**
     * 将mongo的全部试题重新导入
     * @param subject
     */
    @RequestMapping(value = "send_question_update_msg")
    public void sendUpdateMsg(@RequestParam int subject,HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        Criteria criteria = Criteria.where("subject").is(subject);
        Query query = new Query(criteria);
        List<Question> questions = mongoTemplate.find(query, Question.class);
        questions.parallelStream().forEach(q -> {
            try {
                logger.info("current qid={}",q.getId());
                questionDubboService.update(q);
            } catch (Exception e) {
                logger.error("update error,qid={}",e,q.getId());
            }
        });
    }


    /**
     * 初始化网校试题
     * 将模块（顶级知识点）下的试题id放入redis list
     *
     */
    @RequestMapping(value = "initNetSchoolQuestion")
    public void initNetSchoolQuestion() {
        List<Integer> pointIds = Arrays.asList(392, 435, 482, 642);

        ListOperations<String, String> opsForList = redisTemplate.opsForList();

        for (Integer pointId : pointIds) {
            Criteria criteria = Criteria.where("type").is(QuestionType.SINGLE_CHOICE)
                    .and("mode").is(QuestionMode.QUESTION_TRUE)
                    .and("subject").is(SubjectType.GWY_XINGCE)
                    .and("points").in(pointId)
                    .and("parent").is(0)
                    .and("choices").size(4)
                    .and("year").gte(2008)
                    .and("status").is(QuestionStatus.AUDIT_SUCCESS) //审核通过的题目
                    .and("stem").regex("^((?!img).)*$"); //不包含img字符串
            Query query = new Query(criteria);
            query.with(new Sort(Sort.Direction.DESC,"year")).limit(2000);
            List<Question> questionList = mongoTemplate.find(query, Question.class);

            List<String> qids = questionList.stream().map(i -> i.getId() + "").collect(Collectors.toList());

            String listKey = NetSchoolQuestionService.getListKey(pointId);

            redisTemplate.delete(listKey); //清空
            opsForList.rightPushAll(listKey, qids);
        }
    }


    @RequestMapping(value = "initRecommendedTime")
    public void initRecommendedTime() {
        List<GenericQuestion> questions = mongoTemplate.findAll(GenericQuestion.class);



        questions.parallelStream().forEach(q->{

            if (q.getRecommendedTime() == 0) {
                q.setRecommendedTime(q.getDifficult() * 10);
            }
            mongoTemplate.save(q);

        });
    }
    @RequestMapping(value = "clear/questionCache")
    public void clearQuestionCache(@RequestParam(defaultValue = "-1") int questionId,
                                   @RequestParam(defaultValue = "-1") int category,
                                   @RequestParam(defaultValue = "-1") int subject) throws BizException {
        if(questionId>1){
            QuestionCache.remove(questionId);
            return;
        }
        if(subject<0&&category<0){
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        List<Integer> subjects = null;
        if(subject>0){
            subjects = Lists.newArrayList(subject);
        }
        if(category>0){
            String subjectSql = "SELECT id FROM v_new_subject WHERE catgory = "+category;
            subjects = jdbcTemplate.queryForList(subjectSql,Integer.class);
        }
        if(CollectionUtils.isEmpty(subjects)){
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        int id =  0;
        int size = 100;
        for(Integer subjectId:subjects){
            logger.info("正在清空{}科目下的所有知识点");
            while(true){
                List<Question> questions = questionDao.findPageBySubject(subjectId,id,size);
                if(CollectionUtils.isEmpty(questions)){
                    id = 0;
                    break;
                }
                logger.info("subject={},id={},size={}",subjectId,id,questions.size());
                for(Question question:questions){
                    QuestionCache.remove(question.getId());
                    if(id<question.getId()){
                        id = question.getId();
                    }
                }
                if(questions.size()<size){
                    id = 0;
                    break;
                }
            }
            logger.info("已经清空{}科目下的所有知识点");
        }

    }
    @RequestMapping(value = "clear/rocksdb")
    public Object clearQuestionRocksDb(int subject) throws BizException{
        int cursor = 0;
        int size = 100;
        int total = 0;
        while(true){
            List<Question> questionList = questionDao.findQuestionsForPage(cursor,size,subject);
            if(CollectionUtils.isEmpty(questionList)){
                logger.info("已无试题需要处理，进程结束");
                break;
            }
            cursor = questionList.get(questionList.size()-1).getId();
            questionList.parallelStream().forEach(i->QuestionCache.remove(i.getId()));
            total += questionList.size();
            logger.info("total = {}",total);
        }
        Map<String,Integer> map = Maps.newHashMap();
        map.put("total",total);
        return map;
    }
}
