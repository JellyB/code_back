package com.huatu.ztk.knowledge.controller;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.service.QuestionCollectService;
import com.huatu.ztk.knowledge.task.UserAnswersTask;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shaojieyue
 * Created time 2016-07-12 13:50
 */

@RestController
public class InitUserAnswersController {
    private static final Logger logger = LoggerFactory.getLogger(InitUserAnswersController.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserAnswersTask userAnswersTask;

    private volatile boolean running=false;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

//    @Autowired
//    private QuestionPointDao questionPointDao;

    @Autowired
    private QuestionCollectService questionCollectService;

    @Autowired
    private JdbcTemplate mobileJdbcTemplate;

    Cache<Integer, List<QuestionPoint>> POINTS_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(10)
                    .expireAfterWrite(30, TimeUnit.MINUTES)
                    .build();


    @RequestMapping(value = "/initAnswers")
    public Object initUuserAnswer(HttpServletRequest httpServletRequest){
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            Map data = new HashMap<>();
            data.put("message","已经在执行任务");
            return data;
        }
        if (running) {
            Map data = new HashMap<>();
            data.put("message","已经在执行任务");
        }
        running = true;
        long currentMaxId = 0;
        AtomicLong count = new AtomicLong();
        while (true){
            final Criteria criteria = Criteria.where("_id").gt(currentMaxId);
            Query query = new Query(criteria);
            query.with(new Sort("_id")) ////排序
                    .limit(1000);
            final List<AnswerCard> answerCards = mongoTemplate.find(query, AnswerCard.class);

            //没有查询到新的说明已经处理完
            if (answerCards.size() == 0) {
                break;
            }

            //循环处理数据
            for (AnswerCard answerCard : answerCards) {
                currentMaxId = Long.max(currentMaxId, answerCard.getId());
                List<Answer> answers = new ArrayList<>();
                final UserAnswers userAnswers = UserAnswers.builder()
                        .uid(answerCard.getUserId())
                        .subject(SubjectType.GWY_XINGCE)
                        .submitTime(answerCard.getCreateTime())
                        .answers(answers)
                        .build();
                List<Integer> questions = null;
                if (answerCard instanceof StandardCard) {
                    questions = ((StandardCard) answerCard).getPaper().getQuestions();
                }else if(answerCard instanceof PracticeCard){
                    questions = ((PracticeCard) answerCard).getPaper().getQuestions();
                }

                final String[] questionAnswers = answerCard.getAnswers();
                final int[] corrects = answerCard.getCorrects();
                final int[] times = answerCard.getTimes();
                for (int i = 0; i < questions.size(); i++) {
                    final String questionAnswer = questionAnswers[i];
                    if (questionAnswer.equals("0")) {
                        continue;
                    }
                    final Answer answer = new Answer();
                    answer.setAnswer(questionAnswer);
                    answer.setCorrect(corrects[i]);
                    answer.setTime(times[i]);
                    answers.add(answer);
                }
                if (answers.size()>0) {
                    userAnswersTask.onMessage(new Message(JsonUtil.toJson(userAnswers).getBytes(),null));
                }

            }

            if (count.incrementAndGet() % 1000 == 0) {
                logger.warn("count={},id={}",count.get(),currentMaxId);
            }
        }

        Map data = new HashMap<>();
        data.put("message","处理完成");
        return data;
    }

    /**
     * 删除无用key
     * @param pukey
     */
    @RequestMapping("delUserKey")
    public void delUserKey(@RequestParam long pukey,HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
    }

    public void delUserKey(List<Integer> uids, List<Integer> pids) {
        final int subject = 1;

        //每个uid
        for (Integer uid : uids) {
            List<String> finishSetKeys = new ArrayList<>();
            String finishedCountKey = RedisKnowledgeKeys.getFinishedCountKey(uid);
            redisTemplate.delete(finishedCountKey);

            for (Integer pid : pids) {
                String finishedSetKey = RedisKnowledgeKeys.getFinishedSetKey(uid, pid);
                finishSetKeys.add(finishedSetKey);
            }
            redisTemplate.delete(finishSetKeys);
        }
    }


    /**
     * 更新redis中的复合题子题数据
     */
    @RequestMapping(value = "import_diff")
    public void importDiff(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        final Criteria criteria = Criteria.where("parent").ne(0);
        List<Question> questions = mongoTemplate.find(new Query(criteria), Question.class);
        int count = 0;
        for (Question question : questions) {

            if (question instanceof GenericQuestion) {
                GenericQuestion genericQuestion = (GenericQuestion) question;
                final String questionIdKey = RedisKnowledgeKeys.getQuestionIdKey(genericQuestion.getId());
                //把核心信息存入redis
                Map<String, String> data = new HashMap();
                try {
                    data.put("area", genericQuestion.getArea() + "");
                    data.put("difficult", genericQuestion.getDifficult() + "");
                    data.put("year", genericQuestion.getYear() + "");
                    data.put("parent", genericQuestion.getParent() + "");

                    data.put("moduleId", genericQuestion.getPoints().get(0) + "");//模块id
                    redisTemplate.opsForHash().putAll(questionIdKey, data);

                    logger.info("questionIdKey={},data={}", questionIdKey, data);
                } catch (Exception e) {
                    logger.error("error qid={},rediskey={},parent={}", question.getId(), questionIdKey, genericQuestion.getParent());
                }

                count++;
            }
        }
        logger.info("count={}", count);
    }

    /**
     * 导入网站的收藏
     */
    @RequestMapping(value = "import_collection_web")
    public void importCollectionWeb(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        int start = 0;
        final int count = 1000;

        boolean more = true;
        while (more) {
            //BB102=1为有效的收藏记录，BB102=-1说明收藏已经取消
            String sql = "SELECT uid,question_id FROM v_question_collect WHERE BB102=1 limit ?,? ";
            Object[] params = {
                    start,
                    count
            };
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, params);
            more = importCollectMethodWeb(sqlRowSet);
            start = start + count - 1;
            System.out.println("--->proocess=" + start);
        }

    }

    public boolean importCollectMethodWeb(SqlRowSet resultSet) {
        boolean more = false;
        final int subject = 1;
        while (resultSet.next()) {
            more = true;

            try {
                //uid可能为''
                String userIdStr = resultSet.getString("uid");
                if (StringUtils.isBlank(userIdStr)) {
                    continue;
                }

                long userId = Long.parseLong(userIdStr);
                int questionId = resultSet.getInt("question_id");

                try {
                    questionCollectService.collect(questionId, userId, subject);
                } catch (Exception ex) {
                    logger.error("web collect fail,qid={},userId={},ex={}", questionId, userId, ex);
                }

            } catch (Exception e) {
                logger.error("collect error:",e);
            }
        }
        return more;
    }

    /**
     * 导入移动端的收藏
     */
    @RequestMapping(value = "import_collection_mobile")
    public void importCollectionMobile(@RequestParam (defaultValue = "-1") long uid,HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        int start = 0;
        final int count = 1000;

        boolean more = true;
        while (more) {
//            String sql = "SELECT PUKEY,bb108,uname FROM v_qbank_user WHERE bb108 !=0 limit ?,? ";



            String sql = "SELECT PUKEY,uname FROM v_qbank_user WHERE bb108 =0 limit ?,? ";
            Object[] params = {
                    start,
                    count
            };

            if (uid > 0) {
                sql = "SELECT PUKEY,uname FROM v_qbank_user WHERE PUKEY=? limit ?,? ";
                Object[] params2 = {
                        uid,
                        start,
                        count
                };
                params = params2;
            }

            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, params);
            more = importCollectMethodMobile(sqlRowSet);
            start = start + count - 1;
            System.out.println("--->proocess=" + start);
        }

    }

    public boolean importCollectMethodMobile(SqlRowSet resultSet) {
        boolean more = false;
        final int subject = 1;
        while (resultSet.next()) {
            more = true;

            //网站的userId，现在使用的userId
            final Long userId = resultSet.getLong("PUKEY");
            final String uname = resultSet.getString("uname");
            //旧版，移动端的userId
//            final Long mobileUserId = resultSet.getLong("bb108");

            //移动端userId为0，不处理
//            if (mobileUserId == 0) {
//                continue;
//            }

            String msql = "SELECT id FROM ns_users WHERE username= ?";
            Object[] mparams = {
                    uname
            };
            final SqlRowSet userSet = mobileJdbcTemplate.queryForRowSet(msql, mparams);

            while (userSet.next()) {
                final Long mobileUserId = userSet.getLong("id");
                String sql = "SELECT questionid FROM ns_collectrecord_final WHERE userid=? and enable=1 ";
                Object[] params = {
                        mobileUserId
                };
                SqlRowSet ret = mobileJdbcTemplate.queryForRowSet(sql, params);

                collectQuestion(ret,userId,subject);
            }

        }
        return more;
    }


    public void collectQuestion(SqlRowSet ret,long userId,int subject) {
        while (ret.next()) {
            int questionId = ret.getInt("questionid");
            try {
                questionCollectService.collect(questionId, userId, subject);
            } catch (Exception ex) {
                logger.error("mobile collect fail,qid={},userId={}", questionId, userId,ex);
            }
        }
    }


    /**
     * 删除多导入的收藏记录,bb108 != 0,enable=0
     */
    @RequestMapping(value = "del_collection_mobile")
    public void delCollectionMobile(HttpServletRequest httpServletRequest) {
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}",url,remoteAddr);
        if(true){
            return;
        }
        int start = 0;
        final int count = 1000;

        boolean more = true;
        while (more) {
            String sql = "SELECT PUKEY,bb108,uname FROM v_qbank_user WHERE bb108 !=0 limit ?,? ";
            Object[] params = {
                    start,
                    count
            };


            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, params);
            more = delCollectMethodMobile(sqlRowSet);
            start = start + count - 1;
            System.out.println("--->proocess=" + start);
        }

    }

    public boolean delCollectMethodMobile(SqlRowSet resultSet) {
        boolean more = false;
        final int subject = 1;
        while (resultSet.next()) {
            more = true;

            //网站的userId，现在使用的userId
            final Long userId = resultSet.getLong("PUKEY");
            //旧版，移动端的userId
            final Long mobileUserId = resultSet.getLong("bb108");

            //查询取消收藏的试题id,enable=0
            String sql = "SELECT questionid FROM ns_collectrecord_final WHERE userid=? and enable=0 ";
            Object[] params = {
                    mobileUserId
            };
            SqlRowSet ret = mobileJdbcTemplate.queryForRowSet(sql, params);

            delQuestion(ret, userId, subject);

        }
        return more;
    }

    public void delQuestion(SqlRowSet ret,long userId,int subject) {
        while (ret.next()) {
            int questionId = ret.getInt("questionid");
            try {
                //取消收藏
                questionCollectService.cancel(questionId, userId, subject);
            } catch (Exception ex) {
                logger.error("del collect fail,qid={},userId={}", questionId, userId,ex);
            }
        }
    }
}
