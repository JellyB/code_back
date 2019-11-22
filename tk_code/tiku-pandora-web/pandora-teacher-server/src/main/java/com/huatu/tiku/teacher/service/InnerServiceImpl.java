package com.huatu.tiku.teacher.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.duplicate.ObjectiveDuplicatePart;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.dao.mongo.OldQuestionDao;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.dao.paper.PaperQuestionMapper;
import com.huatu.tiku.teacher.dao.question.BaseQuestionMapper;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.duplicate.ObjectiveDuplicatePartService;
import com.huatu.tiku.teacher.service.duplicate.QuestionDuplicateService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.util.file.HtmlFileUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.tiku.util.question.StringMatch;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/9/6.
 */
@Service
@Slf4j
public class InnerServiceImpl {
    @Autowired
    OldPaperDao oldPaperDao;
    @Autowired
    OldQuestionDao oldQuestionDao;
    @Autowired
    PaperQuestionService paperQuestionService;
    @Autowired
    ReflectQuestionDao reflectQuestionDao;
    @Autowired
    PaperQuestionMapper paperQuestionMapper;
    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    KnowledgeService knowledgeService;
    @Autowired
    BaseQuestionMapper baseQuestionMapper;
    @Autowired
    QuestionDuplicateService questionDuplicateService;
    @Autowired
    ObjectiveDuplicatePartService objectiveDuplicatePartService;
    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Autowired
    ImportService importService;

    public void deleteByPaperId(long paperId) {
        Paper paper = oldPaperDao.findById(new Long(paperId).intValue());
        if (null == paper) {
            return;
        }
        //查询所有的复合题
        List<Question> questions = oldQuestionDao.findByIds(paper.getQuestions());
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }
        List<Integer> parents = questions.stream().filter(i -> i instanceof GenericQuestion).map(i -> (GenericQuestion) i).map(GenericQuestion::getParent).distinct().collect(Collectors.toList());
        parents.removeIf(i -> i <= 0);
        if (CollectionUtils.isEmpty(parents)) {
            return;
        }
        List<PaperQuestion> paperQuestions = paperQuestionService.findByPaperIdAndType(paperId, PaperInfoEnum.TypeInfo.ENTITY);
        List<Long> questionIds = paperQuestions.stream().map(i -> i.getQuestionId()).collect(Collectors.toList());
        List<BaseQuestion> baseQuestions = null;
        if (CollectionUtils.isNotEmpty(questionIds)) {
            Example example = new Example(BaseQuestion.class);
            example.and().andIn("id", questionIds);
            baseQuestions = commonQuestionServiceV1.selectByExample(example);
            if (CollectionUtils.isNotEmpty(baseQuestions)) {
                parents.addAll(baseQuestions.stream().filter(i -> i.getMultiId() > 0).map(BaseQuestion::getMultiId).map(Long::intValue).collect(Collectors.toList()));
            }
        }
        List<ReflectQuestion> reflectQuestions = reflectQuestionDao.findByIds(parents);
        if (CollectionUtils.isNotEmpty(reflectQuestions)) {
            parents.addAll(reflectQuestions.stream().map(ReflectQuestion::getNewId).collect(Collectors.toList()));
        }
        //删除所有复合题子题信息
        List<Long> delIds = Lists.newArrayList();
        if (null != baseQuestions) {
            delIds.addAll(baseQuestions.stream().filter(i -> i.getMultiId() > 0).map(BaseQuestion::getId).collect(Collectors.toList()));
        }
        delIds.addAll(parents.stream().distinct().map(Long::new).collect(Collectors.toList()));
        log.info("试卷：{}需要删除的试题ID={}", paperId, delIds);
        for (Long delId : delIds) {
            commonQuestionServiceV1.deleteQuestionPhysical(delId.intValue());
        }
        //删除掉这些复合题的映射关系
        reflectQuestionDao.deleteByIds(parents.stream().distinct().collect(Collectors.toList()));
        //删除所有的关联关系》》》》》》
        Example paperExample = new Example(PaperQuestion.class);
        paperExample.and().andEqualTo("paperId", paperId).andEqualTo("paperType", PaperInfoEnum.TypeInfo.ENTITY.getCode());
        paperQuestionMapper.deleteByExample(paperExample);
    }


    /**
     * @param subject
     * @param flag
     * @param change
     */
    public List<KnowledgeVO> getValidKnowledgeTree(int subject, int flag, boolean change) {
        Map<String, Object> attrMap = Maps.newHashMap();
        attrMap.put("status", 2);
        attrMap.put("subject", subject);
        //选填
        if (1 == subject) {
            attrMap.put("year", 1);
            attrMap.put("mode", 1);
            attrMap.put("type", 1);
        }
        //试题知识点查询
        String collectionName = flag == 1 ? "ztk_question_new" : "ztk_question";
        Map<Integer, List<Integer>> questionMap = findByAttrs(attrMap, collectionName);
        //知识点ID对应试题ID集合
        Map<Integer, List<Integer>> knowledgeMap = Maps.newHashMap();
        for (Map.Entry<Integer, List<Integer>> entry : questionMap.entrySet()) {
            Integer questionId = entry.getKey();
            List<Integer> points = entry.getValue();
            for (Integer point : points) {
                List<Integer> ids = knowledgeMap.getOrDefault(point, Lists.newArrayList());
                ids.add(questionId);
                knowledgeMap.put(point, ids);
            }
        }
        Set<Long> set = knowledgeMap.keySet().stream().map(Long::new).collect(Collectors.toSet());
        //试题涉及到的知识点
        List<Knowledge> knowledgeList = knowledgeService.findAll().stream().filter(i -> set.contains(i.getId())).collect(Collectors.toList());
        //知识树接口知识点
        List<KnowledgeVO> knowledgeVOS = knowledgeService.assertKnowledgeTree(knowledgeList, 0L);
        assertKnowledge(knowledgeVOS, knowledgeMap);
        /**
         * 更新客户端的试题缓存数据
         */
        if (CollectionUtils.isNotEmpty(knowledgeVOS) && change) {
            changeQuestionRedisInfo(knowledgeList, knowledgeMap);
            List<KnowledgeVO> knowledgeVOList = knowledgeService.showKnowledgeTreeBySubject(new Long(subject), false);
            handlerInvalidKnowledge(knowledgeVOList, set);
        }
        return knowledgeVOS;
    }

    private void handlerInvalidKnowledge(List<KnowledgeVO> knowledgeVOList, Set<Long> validIds) {
        if (CollectionUtils.isEmpty(knowledgeVOList)) {
            return;
        }
        for (KnowledgeVO knowledgeVO : knowledgeVOList) {
            Long knowledgeId = knowledgeVO.getKnowledgeId();
            if (!validIds.contains(knowledgeId)) {
                System.out.println("知识点不存在：" + knowledgeVO.getKnowledgeName());
                delRedisCache(knowledgeId);
            }
            handlerInvalidKnowledge(knowledgeVO.getKnowledgeTrees(), validIds);
        }
    }

    private void delRedisCache(Long knowledgeId) {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(knowledgeId.intValue());
        try {
            connection.hDel(pointSummaryKey.getBytes(), knowledgeId.toString().getBytes());
            connection.del(pointQuesionIdsKey.getBytes());
        } finally {
            connection.close();
        }
    }

    /**
     * 更新客户端的试题缓存数据
     *
     * @param knowledgeVOS
     * @param knowledgeMap
     */
    private void changeQuestionRedisInfo(List<Knowledge> knowledgeVOS, Map<Integer, List<Integer>> knowledgeMap) {
        if (CollectionUtils.isEmpty(knowledgeVOS)) {
            return;
        }
        Map<String, String> sumMap = knowledgeVOS.stream().collect(Collectors.toMap(i -> i.getId() + "", i -> {
            List<Integer> ids = knowledgeMap.getOrDefault(i.getId().intValue(), Lists.newArrayList());
            if (CollectionUtils.isEmpty(ids)) {
                return "0";
            }
            return ids.size() + "";
        }));
        final String pointSummaryKey = RedisKnowledgeKeys.getPointSummaryKey();
        log.info("sumMap={}", sumMap);
        HashMap<byte[], byte[]> newHashMap = Maps.newHashMap();
        sumMap.entrySet().stream()
                .forEach(stringStringEntry -> {
                    newHashMap.put(stringStringEntry.getKey().getBytes(), stringStringEntry.getValue().getBytes());
                });

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try {
            connection.hMSet(pointSummaryKey.getBytes(), newHashMap);
        } finally {
            connection.close();
        }

        List<Integer> knowledgeIds = knowledgeVOS.stream()
                .filter(i -> i.getLevel().intValue() == 3)
                .map(Knowledge::getId)
                .map(Long::intValue)
                .collect(Collectors.toList());
        for (Integer knowledgeId : knowledgeIds) {
            String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(knowledgeId);
            List<Integer> questionIds = knowledgeMap.get(knowledgeId);
            String[] ids = questionIds.parallelStream()
                    .map(String::valueOf)
                    .collect(Collectors.toList())
                    .toArray(new String[]{});
            log.info("knowledge={},ids={}", knowledgeId, ids);
            RedisConnection connection2 = redisTemplate.getConnectionFactory().getConnection();
            try {
                connection2.del(pointQuesionIdsKey.getBytes());
                byte[][] bytes = Arrays.stream(ids).map(id -> id.getBytes()).collect(Collectors.toList())
                        .toArray(new byte[][]{});
                connection2.sAdd(pointQuesionIdsKey.getBytes(), bytes);
            } finally {
                connection2.close();
            }
            // redisTemplate.delete(pointQuesionIdsKey);
            // setOperations.add(pointQuesionIdsKey,ids);

        }
    }


    private void assertKnowledge(List<KnowledgeVO> knowledgeList, Map<Integer, List<Integer>> knowledgeMap) {
        for (KnowledgeVO knowledgeVO : knowledgeList) {
            Long knowledgeId = knowledgeVO.getKnowledgeId();
            knowledgeVO.setCount(knowledgeMap.getOrDefault(knowledgeId.intValue(), Lists.newArrayList()).size());
            if (CollectionUtils.isNotEmpty(knowledgeVO.getKnowledgeTrees())) {
                assertKnowledge(knowledgeVO.getKnowledgeTrees(), knowledgeMap);
            }
        }
    }


    /**
     * 根据各种条件查询试题的某些属性（缓存刷新专用）
     *
     * @param attrMap
     */
    public Map<Integer, List<Integer>> findByAttrs(Map<String, Object> attrMap, String collectionName) {
        DBObject queryObject = new BasicDBObject();
        queryObject.put("_class", "com.huatu.ztk.question.bean.GenericQuestion");
        if (null != attrMap.get("status")) {
            queryObject.put("status", attrMap.get("status"));
        }
        if (null != attrMap.get("subject")) {
            queryObject.put("subject", attrMap.get("subject"));
        }
        if (null != attrMap.get("year")) {
            queryObject.put("year", new BasicDBObject("$gte", 2008));
        }
        if (null != attrMap.get("mode")) {
            queryObject.put("mode", 1);
        }
        if (null != attrMap.get("type")) {
            queryObject.put("type", new BasicDBObject("$in", Lists.newArrayList(99, 100, 109)));
        }
        DBObject fieldsObject = new BasicDBObject();
        fieldsObject.put("_id", 1);
        fieldsObject.put("points", 1);
        System.out.println("查询语句：" + queryObject.toString());
        DBCursor dbCursor = mongoTemplate.getCollection(collectionName).find(queryObject, fieldsObject);
        Map<Integer, List<Integer>> map = Maps.newHashMap();
        while (dbCursor.hasNext()) {
            DBObject object = dbCursor.next();
            BasicDBList basicDBList = (BasicDBList) object.get("points");
            if (basicDBList == null) {
                continue;
            }
            List<Integer> list = basicDBList.stream()
                    .map(String::valueOf)
                    .map(Double::parseDouble)
                    .map(Double::intValue)
                    .collect(Collectors.toList());
            map.put(Integer.parseInt(String.valueOf(object.get("_id"))), list);
        }
        return map;

    }

    /**
     * 从ztk_question_new中查询原有的试题，跟现有试题作比较，恢复选项里被删除的空格
     *
     * @param subject
     * @param knowledgeId
     * @return
     */
    public Object SyncQuestionChoice(long subject, long knowledgeId) {
        List<Long> knowledgeIds = null;
        if (knowledgeId > 0) {
            List<Knowledge> all = knowledgeService.findAll();
            Optional<Knowledge> knowledgeOptional = all.stream().filter(i -> i.getId().equals(knowledgeId)).findAny();
            if (knowledgeOptional.isPresent()) {
                Knowledge knowledge = knowledgeOptional.get();
                if (knowledge.getIsLeaf()) {
                    knowledgeIds = Lists.newArrayList(knowledgeId);
                } else {
                    List<Long> ids = Lists.newArrayList();
                    findTailNodes(all, knowledge.getId(), ids);
                    if (CollectionUtils.isNotEmpty(ids)) {
                        knowledgeIds = ids;
                    }
                }
            }
        }
        log.info("knowledgeIds={}", knowledgeIds);
        List<Long> questionIds = baseQuestionMapper.findIdBySubjectAndKnowledge(subject, knowledgeIds);
        List<Long> successIds = Lists.newArrayList();
        questionIds.parallelStream().forEach(id -> {
            int i = syncSingleChoice(id);
            if (i > 0) {
                successIds.add(id);
            }
        });
        log.info("success={},successIds={}", successIds.size(), successIds);
        HashMap<Object, Object> mapData = Maps.newHashMap();
        mapData.put("size", questionIds.size());
        mapData.put("success", successIds.size());
        return mapData;
    }

    /**
     * 查询所有叶子结点
     *
     * @param all 所有知识点信息
     * @param id  知识点
     * @param ids 返回的所有叶子结点（如果叶子结点大于三级，则包含三级及叶子结点）
     */
    public void findTailNodes(List<Knowledge> all, Long id, List<Long> ids) {
        if (CollectionUtils.isEmpty(all)) {
            return;
        }
        /**
         * 自身三级结点的先保留自身，再处理子节点
         */
        Optional<Knowledge> first = all.stream().filter(i -> i.getId().equals(id)).findFirst();
        if (first.isPresent() && first.get().getLevel().intValue() == 3) {
            ids.add(id);
        }
        List<Knowledge> collect = all.stream().filter(i -> i.getParentId().equals(id)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            //没有子节点则当前结点为叶子结点
            if (first.isPresent()) {
                ids.add(id);
            }
            return;
        }
        for (Knowledge knowledge : collect) {
            if (knowledge.getIsLeaf()) {
                ids.add(knowledge.getId());
            } else {
                findTailNodes(all, knowledge.getId(), ids);
            }
        }
    }

    /**
     * 单个试题检查并恢复选项数据
     *
     * @param questionId
     * @return
     */
    public int syncSingleChoice(long questionId) {
        Question oldQuestion = oldQuestionDao.findById(new Long(questionId).intValue());
        if (null == oldQuestion) {
            log.error("试题ID:{}在旧题库不存在", questionId);
            return -1;
        }
        List<String> oldChoices = Lists.newArrayList();
        int type = oldQuestion.getType();
        QuestionInfoEnum.QuestionSaveTypeEnum saveTypeByQuestionType = QuestionInfoEnum.getSaveTypeByQuestionType(type);
        if (!saveTypeByQuestionType.equals(QuestionInfoEnum.QuestionSaveTypeEnum.OBJECTIVE)) {
            log.error("试题ID:{}的题型不符合处理条件：type{}", questionId, type);
            return -1;
        }
        if (oldQuestion instanceof GenericQuestion) {
            oldChoices.addAll(((GenericQuestion) oldQuestion).getChoices());
        }
        if (CollectionUtils.isEmpty(oldChoices) || oldChoices.size() != 4) {
            log.error("试题ID:{}的选项不符合处理的条件：{}", questionId, oldChoices);
            return -1;
        }
        String content = HtmlConvertUtil.assertChoicesContent(oldChoices);
        if (StringUtils.isBlank(content)) {
            return -1;
        }
        /**
         * 无空格的选项不做处理
         */
        String temp = content.replaceAll("&nbsp;", " ").trim();
        if (StringUtils.isBlank(temp) || (temp.lastIndexOf("\u00A0") < 0)) {
            log.error("试题ID:{}的选项中无空格字段:{}", questionId, content);
            return -1;
        }
        Example relationExample = new Example(QuestionDuplicate.class);
        relationExample.and().andEqualTo("questionId", questionId);
        List<QuestionDuplicate> questionDuplicates = questionDuplicateService.selectByExample(relationExample);
        if (CollectionUtils.isEmpty(questionDuplicates)) {
            log.error("试题ID:{}的复用关联数据为空", questionId);
            return -1;
        }
        Long duplicateId = questionDuplicates.get(0).getDuplicateId();
        ObjectiveDuplicatePart objectiveDuplicatePart = objectiveDuplicatePartService.selectByPrimaryKey(duplicateId);
        if (null == objectiveDuplicatePart) {
            log.error("试题ID:{}的复用数据为空,dupliateId={}", questionId, duplicateId);
            return -1;
        }
        /**
         * 比较去掉标签后的内容，如果吻合，则替换，否则不替换
         */
        String filter = StringMatch.replaceNotChinese(content);
        if (StringUtils.isBlank(filter)) {
            log.error("试题ID:{}选项内容去标签后，无数据", questionId);
            return -1;
        }
        if (!filter.equals(objectiveDuplicatePart.getChoicesFilter())) {
            log.error("试题ID:{}的选项内容不一致，pre={},after={}", questionId, filter, objectiveDuplicatePart.getChoicesFilter());
            return -1;
        }
        List<String> collect = oldChoices.stream().map(i -> htmlFileUtil.html2DB(i, false)).collect(Collectors.toList());
        String content1 = HtmlConvertUtil.assertChoicesContent(collect);
        log.error("试题ID:{}准备替换选项：pre={}", questionId, objectiveDuplicatePart.getChoices());
        log.error("试题ID:{}准备替换选项：aft={}", questionId, content1);
        objectiveDuplicatePart.setChoices(HtmlConvertUtil.assertChoicesContent(collect));
        objectiveDuplicatePartService.save(objectiveDuplicatePart);
        importService.sendQuestion2Mongo(new Long(questionId).intValue());
        return 1;
    }
}
