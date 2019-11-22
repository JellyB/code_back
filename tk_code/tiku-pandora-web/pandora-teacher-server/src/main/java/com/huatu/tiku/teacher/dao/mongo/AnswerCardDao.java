package com.huatu.tiku.teacher.dao.mongo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.entity.metas.AnswerCardSub;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.AnswerCardType;
import com.huatu.ztk.question.bean.ReflectQuestion;
import com.mongodb.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/1/15.
 */
@Repository
public class AnswerCardDao {
    private static final Logger logger = LoggerFactory.getLogger(AnswerCardDao.class);
    public static final int MAX_ANSWER_TIME = 3 * 60;//最大答题时间
    /**
     * 存储试题的集合名字
     */
    @Autowired
    private MongoTemplate mongoTemplate;

    private static final List<Integer> removeTypes = Lists.newArrayList(AnswerCardType.MATCH, AnswerCardType.ESTIMATE, AnswerCardType.SIMULATE, AnswerCardType.COURSE_EXERCISE, AnswerCardType.COURSE_BREAKPOINT);


    /**
     * 插入答题卡(创建答题卡的逻辑使用saveWithReflectQuestion方法，插入答题卡接口弃用)
     *
     * @param answerCards
     */
    @Deprecated
    public void insert(AnswerCard... answerCards) {
//        logger.info("save answer cardId={},uid={}", answerCards.getId(),answerCards.getUserId());
        for (AnswerCard answerCard : answerCards) {
            answerCard.setPoints(null);//points不进行存储,目的为了节省mongo内存
        }
        if (answerCards.length == 1) {
            mongoTemplate.insert(answerCards[0]);
        } else {//多余一个做批量插入
            mongoTemplate.insertAll(Lists.newArrayList(answerCards));
        }
    }

    /**
     * 根据id查询答题卡
     *
     * @param practiceId
     * @return
     */
    public AnswerCard findById(long practiceId) {
        final AnswerCard answerCard = mongoTemplate.findById(practiceId, AnswerCard.class);
        //logger.info("答题卡信息，{},数据 ===》{}",practiceId,answerCard);
        return answerCard;
    }

    /**
     * 保存答题卡
     *
     * @param answerCard
     */
    public void save(AnswerCard answerCard) {
        answerCard.setPoints(null);//points不进行存储,目的为了节省mongo内存
        /**
         * update by lijun 在每次修改答题卡时候，修改创建时间，保证非第一次答题时把答题卡时间更新成当前时间
         */
        answerCard.setCreateTime(new Date().getTime());
        mongoTemplate.save(answerCard);
    }

    /**
     * 保存答题卡并且映射真实存在的试题ID
     *
     * @param answerCard
     */
    public void saveWithReflectQuestion(AnswerCard answerCard) {
        save(answerCard);
        if (answerCard.getType() != AnswerCardType.MATCH) {
            //替换无效的试题ID
            try {
                if (answerCard instanceof PracticeCard) {
                    PracticePaper paper = ((PracticeCard) answerCard).getPaper();
                    logger.info("PracticeCard - paperInfo = {},Questions = {}", paper.getName(), paper.getQuestions());
                    transReflectionId(paper.getQuestions());
                } else if (answerCard instanceof StandardCard) {
                    Paper paper = ((StandardCard) answerCard).getPaper();
                    logger.info("StandardCard - paperInfo = {},Questions = {}", paper.getName(), paper.getQuestions());
                    transReflectionId(paper.getQuestions());
                }
            } catch (Exception e) {
                logger.info("saveWithReflectQuestion error,{}", e);
            }
        }

    }

    /**
     * 将试题ID换成替换ID
     *
     * @param questions
     */
    private void transReflectionId(List<Integer> questions) {
        Criteria criteria = Criteria.where("oldId").in(questions);
        Query query = new Query(criteria);
        logger.info("query={}", query);
        List<ReflectQuestion> reflectQuestions = mongoTemplate.find(query, ReflectQuestion.class);
        if (CollectionUtils.isNotEmpty(reflectQuestions)) {
            Map<Integer, Integer> tempMap = reflectQuestions.stream().collect(Collectors.toMap(i -> i.getOldId(), i -> i.getNewId()));
            questions = questions.stream().map(i -> tempMap.getOrDefault(i, i)).collect(Collectors.toList());
        }
    }

    /**
     * 分页查询个人用户答题卡
     *
     * @param userId         用户id
     * @param catgoryList    考试类型list
     * @param cursor         游标
     * @param size           大小
     * @param cardType       答题卡类型
     * @param cardTime       答题时间
     * @param removeEstimate 是否移除模考估分的记录
     * @param subject        考试科目
     * @return
     */
    public List<AnswerCard> findForPage(long userId, List<Integer> catgoryList, long cursor, int size,
                                        int cardType, String cardTime, boolean removeEstimate, int subject) {

        //返回已完成的练习并过滤掉状态为已删除的答题记录
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").ne(AnswerCardStatus.DELETED);
        long createTime = 0L;
        if (cursor != Long.MAX_VALUE) {
            AnswerCard answerCard = findById(cursor);
            if (null != answerCard) {
                createTime = answerCard.getCreateTime();
            }
        } else {
            createTime = cursor;
        }

        if (StringUtils.isBlank(cardTime) || "0".equals(cardTime)) {
            criteria.and("createTime").lt(createTime);
        }


        if (CollectionUtils.isNotEmpty(catgoryList)) {
            criteria.and("catgory").in(catgoryList);
        } else if (subject > 0) {
            criteria.and("subject").is(subject);
        }

        if (cardType > 0) {//答题卡类型
            if (cardType == AnswerCardType.SIMULATE) {
                //旧版的模考估分记录type为4
                criteria = criteria.and("type").in(cardType, AnswerCardType.MOCK_PAPER);
            } else {
                criteria = criteria.and("type").is(cardType);
            }
        } else if (removeEstimate) {
            criteria.and("type").nin(removeTypes);
        }

        //根据答题时间过滤
        if (StringUtils.isNoneBlank(cardTime) && !"0".equals(cardTime)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
            try {
                long minDate = 0;
                long maxDate = 0;
                Date parseDate = simpleDateFormat.parse(cardTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parseDate);

                //一个月最大的天数
                int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                //一个月第一天,实际上parseDate就是第一天
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                minDate = calendar.getTimeInMillis();

                //一个月最后一天
                calendar.set(Calendar.DAY_OF_MONTH, maxDay);
                maxDate = calendar.getTimeInMillis();

                criteria = criteria.and("createTime").gte(minDate).lte(Math.min(maxDate, createTime));
            } catch (Exception e) {
                logger.error("format fail.", e);
            }
        }
        Query query = new Query(criteria);
        logger.info("qury ={}", query);

        /**
         * updte by lijun 2018-05-06 此处修改排序规则
         * 原本排序规则：_id desc
         */
        query.limit(size).with(new Sort(Sort.Direction.DESC, "createTime"));

        List<AnswerCard> answerCards = mongoTemplate.find(query, AnswerCard.class);

        //对结果集做翻转，默认是按照id顺序排列的
        //answerCards = Lists.reverse(answerCards);
        return answerCards;
    }

    public List<AnswerCard> findAnserCards(long userId, long cursor, int size, int cardType, String cardTime) {
        final Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").ne(AnswerCardStatus.DELETED);
        long createTime = 0L;
        if (cursor != Long.MAX_VALUE) {
            AnswerCard answerCard = findById(cursor);
            if (null != answerCard) {
                createTime = answerCard.getCreateTime();
            }
        } else {
            createTime = cursor;
        }
        criteria.and("createTime").lt(createTime);
        if (cardType > 0) {//答题卡类型
            criteria.and("type").is(cardType);
        }

        Query query = new Query(criteria);
        query.limit(size);
        query.with(new Sort(new Sort.Order(Sort.Direction.DESC, "_id")));

        List<AnswerCard> answerCards = mongoTemplate.find(query, AnswerCard.class);
        if (answerCards == null) {
            answerCards = new ArrayList<>();
        }

        //对结果集做翻转，默认是按照id顺序排列的
//        answerCards = Lists.reverse(answerCards);
        return answerCards;
    }

    public AnswerCard findAnswerCards(long userId, int paperId) {
        final Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").ne(AnswerCardStatus.DELETED)
                .and("paper._id").is(paperId);
        Query query = new Query(criteria);
        AnswerCard answerCard = mongoTemplate.findOne(query, AnswerCard.class);
        return answerCard;
    }

    /**
     * 查询 用户的课程答题卡是否存在
     *
     * @param userId
     * @return
     */
    public AnswerCard findCourseAnswerCard(Integer userId, long courseId, Integer courseType, Integer type) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").ne(AnswerCardStatus.DELETED)
                .and("paper.courseType").is(courseType)
                .and("paper.courseId").is(courseId)
                .and("paper.type").is(type);
        Query query = new Query(criteria);
        AnswerCard answerCard = mongoTemplate.findOne(query, AnswerCard.class);
        return answerCard;
    }

    /**
     * 批量查询答题卡信息
     *
     * @param paramList 参数合集
     * @param userId    用户Id
     * @param type      试卷类型
     * @return
     */
    public List<AnswerCard> findCourseAnswerCardListInfo(List<HashMap<String, Object>> paramList, long userId, int type) {
        Criteria criteria = Criteria
                .where("userId").is(userId)
                .and("status").ne(AnswerCardStatus.DELETED)
                .and("paper.type").is(type);

        List<Criteria> collect = paramList.stream()
                .filter(data -> null != data.get("courseType") && null != data.get("courseId"))
                .map(data -> Criteria.
                        where("paper.courseType").is(data.get("courseType")).
                        and("paper.courseId").is(data.get("courseId"))
                )
                .collect(Collectors.toList());
        if (null != collect && collect.size() > 0) {
            Criteria[] criteriaArray = collect.toArray(new Criteria[collect.size()]);
            criteria.orOperator(criteriaArray);
        }
        Query query = new Query(criteria);
        List<AnswerCard> answerCardList = mongoTemplate.find(query, AnswerCard.class);
        return answerCardList;

    }


    /**
     * 根据答题卡id将答题记录状态改为已删除
     *
     * @param id
     */
    public boolean delete(long id, long uid) {
        final Criteria criteria = Criteria.where("_id").is(id).and("userId").is(uid);
        WriteResult result = mongoTemplate.updateFirst(new Query(criteria), Update.update("status", AnswerCardStatus.DELETED), AnswerCard.class);
        return result.isUpdateOfExisting();
    }

    public void update(long id, Update update) {
        final Criteria criteria = Criteria.where("_id").is(id);
        mongoTemplate.updateFirst(new Query(criteria), update, AnswerCard.class);
    }

    public PageBean findByPage(long userId, List<Integer> catgoryList, int page, int size, int cardType, String cardTime, boolean removeEstimate, int subject, int status) {
        //返回已完成的练习并过滤掉状态为已删除的答题记录
        Criteria criteria = Criteria.where("userId").is(userId);
        if (status <= 0) {
            criteria.and("status").ne(AnswerCardStatus.DELETED);
        } else if (AnswerCardStatus.UNDONE == status) {
            criteria.and("status").in(AnswerCardStatus.UNDONE, AnswerCardStatus.CREATE);
        } else {
            criteria.and("status").is(status);
        }


        if (CollectionUtils.isNotEmpty(catgoryList)) {
            criteria.and("catgory").in(catgoryList);
        } else if (subject > 0) {
            criteria.and("subject").is(subject);
        }

        if (cardType > 0) {//答题卡类型
            if (cardType == AnswerCardType.SIMULATE) {
                //旧版的模考估分记录type为4
                criteria = criteria.and("type").in(cardType, AnswerCardType.MOCK_PAPER);
            } else {
                criteria = criteria.and("type").is(cardType);
            }
        } else if (removeEstimate) {
            //排除 课中练习、课后练习
            criteria.and("type").nin(removeTypes);
        }
        //根据答题时间过滤
        if (StringUtils.isNoneBlank(cardTime) && !"0".equals(cardTime)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
            try {
                long minDate = 0;
                long maxDate = 0;
                Date parseDate = simpleDateFormat.parse(cardTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parseDate);

                //一个月最大的天数
                int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

                //一个月第一天,实际上parseDate就是第一天
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                minDate = calendar.getTimeInMillis();

                //一个月最后一天
                calendar.set(Calendar.DAY_OF_MONTH, maxDay);
                maxDate = calendar.getTimeInMillis();

                criteria = criteria.and("createTime").gte(minDate).lte(maxDate);
            } catch (Exception e) {
                logger.error("format fail.", e);
            }
        }
        Query query = new Query(criteria);
        query.limit(size).with(new Sort(Sort.Direction.DESC, "createTime"));
        query.skip((page - 1) * size).limit(size).with(new Sort(Sort.Direction.DESC, "_id"));

        List<AnswerCard> answerCards = mongoTemplate.find(query, AnswerCard.class);
        long count = mongoTemplate.count(new Query(criteria), AnswerCard.class);
        //对结果集做翻转，默认是按照id顺序排列的
        //answerCards = Lists.reverse(answerCards);
        PageBean pageBean = new PageBean(answerCards, page, (int) count);
        return pageBean;
    }

    public List<Map<String, Object>> findForPaperMeta(long index, int limit) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.and("_id").greaterThan(index).and("status").is(AnswerCardStatus.FINISH);
        DBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
        fields.put("_id", 1);
        fields.put("type", 1);
        fields.put("paper", 1);
        DBCursor dbCursor =mongoTemplate.getCollection("ztk_answer_card").find(queryBuilder.get(),fields).limit(limit);
        List<Map<String,Object>> list = Lists.newArrayList();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            Map<String,Object> map = Maps.newHashMap();
            try{
                map.put("type",object.get("type"));
                map.put("id",object.get("_id"));
                map.put("paper",object.get("paper"));
                list.add(map);
            }catch (Exception e){
                logger.error("map = {}",map);
                logger.error("object = {}",object);
                e.printStackTrace();
            }
        }
        return list;
    }


    /**
     * 查询答题卡答题的情况，比如答题用时/是否正确
     * @param practiceIds
     * @return
     */
    public List<AnswerCardSub> findAllByIds(List<Long> practiceIds ,String name) {

        DBObject queryObject = new BasicDBObject();
        queryObject.put("_id",new BasicDBObject("$in", practiceIds));
        DBObject fieldsObject = new BasicDBObject();
        fieldsObject.put("_id",1);
        fieldsObject.put(name,1);
        DBCursor dbCursor =mongoTemplate.getCollection("ztk_answer_card").find(queryObject,fieldsObject);
        List<AnswerCardSub> list= Lists.newArrayList();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            AnswerCardSub answerCard = new AnswerCardSub();
            answerCard.setId(Long.parseLong(object.get("_id").toString()));
            BasicDBList basicDBList = (BasicDBList)object.get(name);
            answerCard.setArrays(basicDBList.stream().map(i->String.valueOf(i)).toArray());
            list.add(answerCard);
        }
        return list;
    }

    public List<AnswerCard> findByIds(ArrayList<Long> list) {
        final Criteria criteria = Criteria.where("_id").in(list);
        return mongoTemplate.find(new Query(criteria),AnswerCard.class);
    }
}
