package com.huatu.tiku.teacher.service.impl.common;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.constants.RabbitKeyConstant;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.question.BaseQuestionSearchMapper;
import com.huatu.tiku.teacher.dao.question.DuplicatePartProviderMapper;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.tiku.teacher.service.common.ImportPaperService;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.util.SearchBaseRequest;
import com.huatu.tiku.teacher.util.SearchQuestionUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.lookup.MapDataSourceLookup;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * Created by x6 on 2018/6/25.
 */
@Slf4j
@Service
public class ImportServiceImpl extends BaseServiceImpl<BaseQuestion> implements ImportService {

    public ImportServiceImpl() {
        super(BaseQuestion.class);
    }

    @Value("${spring.profiles}")
    public String env;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    private NewQuestionDao questionDao;

    @Autowired
    CommonQuestionServiceV1 commonQuestionService;

    @Autowired
    @Qualifier("importPaperServiceImpl")
    private ImportPaperService importPaperService;

    @Autowired
    private DuplicatePartProviderMapper duplicatePartProviderMapper;

    @Autowired
    private PaperQuestionService paperQuestionService;

    @Autowired
    private PaperEntityService paperEntityService;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    KnowledgeService knowledgeService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    BaseQuestionSearchMapper baseQuestionSearchMapper;

    @Override
    public void importQuestion(long questionId) {
        log.info("同步试卷信息到mongo中，questionId={}", questionId);
        //将试题信息导入mongo
        Question question = commonQuestionService.parseQuestion2Mongo(questionId);
        log.info("生成的mongo数据：{}", question);
        if (null == question) {
            log.error("question parse error ,questionId={}", questionId);
            return;
        }
        //更新mongo数据
        questionDao.save(question);
        Map map = Maps.newHashMap();
        map.put("id", question.getId());
        rabbitTemplate.convertAndSend("", "sync_question_cache", map);
        //search搜索引擎队列发送
        sendQuestion2Search(question.getId());
    }

    @Override
    public void sendQuestion2Mongo(int questionId) {
        Map map = Maps.newHashMap();
        map.put("id", new Long(questionId));
        rabbitTemplate.convertAndSend("", RabbitKeyConstant.getQuestion_2_mongo(env), map);
    }

    @Override
    public void sendQuestion2Mongo(List<Integer> questionIdList) {
        if (CollectionUtils.isNotEmpty(questionIdList)) {
            questionIdList.stream().forEach(questionId -> sendQuestion2Mongo(questionId));
        }
    }

    public void sendQuestion2Search(int questionId) {
        Question question = questionDao.findById(questionId);
        if (null == question) {
            log.error("question is existed in new mongo ,id = {}", questionId);
            return;
        }
        Map map = Maps.newHashMap();
        map.put("index", "pandora-question");
        map.put("type", "question");
        if (question.getStatus() != QuestionStatus.AUDIT_SUCCESS) {
            map.put("operation", "delete");
        } else {
            map.put("operation", "save_clean");
        }
        //试题数据转换
        Map mapData = SearchQuestionUtil.transToMap(question);
        if (mapData.size() == 0) {
            return;
        }
        map.put("data", mapData);
        //新版搜索引擎数据上传
        rabbitTemplate.convertAndSend("pandora_search", "com.ht.pandora.search", JsonUtil.toJson(map));
        log.info("发送搜索引擎队列信息：id={},operation={}", question.getId(), map.get("operation"));
    }

    /**
     * 发送到ES查重使用
     *
     * @param questionId
     */
    public void sendQuestion2SearchForDuplicate(Long questionId) {


        BaseQuestion question = commonQuestionService.selectByPrimaryKey(questionId);
        if (null == question) {
            Map map = Maps.newHashMap();
            map.put("id", questionId);
            map.put("status", StatusEnum.DELETE.getValue());
            sendQuestion2Search(map,question);
            return;
        }
        try {
            Map mapData = checkoutQuestion(question);
            if (mapData.size() == 0) {
                return;
            }
            sendQuestion2Search(mapData, question);
        } catch (Exception e) {
            log.info("试题ID是:{}", question.getId());
        }
    }

    private void sendQuestion2Search(Map mapData, BaseQuestion question) {
        //组装ES结构
        String mapDataString = JSON.toJSONString(mapData);
        List<String> list = new LinkedList<>();
        list.add(mapDataString);
        SearchBaseRequest searchBaseRequest = new SearchBaseRequest();
        searchBaseRequest.setData(list);
        searchBaseRequest.setIndex("pandora-duplicate");
        searchBaseRequest.setType("doc");
        if (null != question && question.getStatus() == StatusEnum.NORMAL.getValue()) {
            searchBaseRequest.setOperation("save_clean");
        } else {
            searchBaseRequest.setOperation("delete");
        }
        //log.info("需要传递的信息是：{}", searchBaseRequest);
        rabbitTemplate.convertAndSend("pandora_search", "com.ht.pandora.duplicate", JsonUtil.toJson(searchBaseRequest));
        log.info("发送去重搜索引擎队列信息：id={},operation={}", MapUtils.getInteger(mapData,"id",-1), (null != question && question.getStatus() == StatusEnum.NORMAL.getValue()));
    }


    public Map checkoutQuestion(BaseQuestion baseQuestion) {
        HashMap map = new HashMap();
        int questionType = baseQuestion.getQuestionType();
        if (questionType == QuestionInfoEnum.QuestionTypeEnum.COMPOSITE.getCode() ||
                questionType == QuestionInfoEnum.QuestionTypeEnum.COMPOSITE_SUBJECTIVE.getCode()) {
            return map;
        }
        if (null != baseQuestion) {
            map.put("id", baseQuestion.getId());
            map.put("questionType", baseQuestion.getQuestionType());
            map.put("status", StatusEnum.NORMAL.getValue());
            map.put("subjectId", baseQuestion.getSubjectId());
            List<HashMap<String, Object>> list = null;
            QuestionInfoEnum.QuestionDuplicateTypeEnum duplicateType = QuestionInfoEnum.getDuplicateTypeByQuestionType(questionType);
            if (duplicateType.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.JUDGE_OBJECT)) {
                list = duplicatePartProviderMapper.buildObjective(baseQuestion.getId().toString(), questionType);
            } else if (duplicateType.equals(QuestionInfoEnum.QuestionDuplicateTypeEnum.COMPOSITE_SUBJECTIVE)) {
                list = duplicatePartProviderMapper.buildSubjective(baseQuestion.getId().toString(), questionType);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                Map duplicateMap = list.get(0);
                map.put("stem", duplicateMap.get("stem"));
                map.put("answer", duplicateMap.get("answer"));
                map.put("analysis", duplicateMap.get("analysis"));
                map.put("extend", duplicateMap.get("extend"));
                map.put("choices", duplicateMap.get("choicesStr"));
            }
        }
        return map;
    }


    /**
     * 以试卷为单位，mysql同步试题到mongo中
     * isUpdateBizStatus 表识是否修改其发布状态
     */
    public void sendQuestion2MongoByPaper(Boolean isUpdateBizStatus, Long paperId, PaperInfoEnum.TypeInfo type) {
        PaperEntity entity = paperEntityService.selectByPrimaryKey(paperId);
        if (null == entity) {
            throwBizException("该试卷不存在");
        }

        List<PaperQuestion> paperQuestionList = paperQuestionService.findByPaperIdAndType(paperId, type);
        if (CollectionUtils.isEmpty(paperQuestionList)) {
            throwBizException("该试卷未绑定试题");
        }
        List<Integer> questionIds = paperQuestionList.stream().map(PaperQuestion::getQuestionId).
                map(Long::intValue).collect(Collectors.toList());
        //批量同步mongo中
        log.info("试卷ID是：{},需同步试题数目:{}", entity.getId(), questionIds.size());
        sendQuestion2Mongo(questionIds);
    }

    @Override
    public void sendQuestion2MongoByKnowledge() {

        Consumer<List<Question>> consumer = (list -> {
            for (Question mongoQuestion : list) {
                if (mongoQuestion instanceof GenericQuestion) {
                    List<Integer> pointList = ((GenericQuestion) mongoQuestion).getPoints();
                    //482数量关系 754资料分析
                    if (pointList.contains(482) || pointList.contains(754)) {
                        sendQuestion2Mongo(mongoQuestion.getId());
                    }
                }

            }
        });
        commonQuestionService.findAndHandlerQuestion(consumer, 1);
    }

    @Override
    public void importQuestionSource(int subject) {
        final String syncCacheKey = "sync_question_source_cache";
        redisTemplate.delete(syncCacheKey);
        Consumer<List<Question>> consumer = ((questions) -> {
            if (CollectionUtils.isEmpty(questions)) {
                log.info("题源同步结束");
                return;
            }
            final List<Long> ids = questions.stream().map(Question::getId).map(Long::valueOf).collect(Collectors.toList());
            List<HashMap<String, Object>> questionSources = baseQuestionSearchMapper.getQuestionSourceForList(ids);
            HashMap<Integer, Object> map = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(questionSources)) {
                map.putAll(questionSources.stream().collect(Collectors.toMap(i -> MapUtils.getInteger(i, "question_id"), i -> MapUtils.getString(i, "source"))));
            }
            for (Question question : questions) {
                String from = question.getFrom();
                Object source = map.getOrDefault(question.getId(), "");
                if (StringUtils.isNotBlank(from) && from.equals(source)) {
                    continue;
                }
                System.out.println(from + ">>>>" + source);
                question.setFrom(from);
                questionDao.save(question);
                Map tempMap = Maps.newHashMap();
                tempMap.put("id", question.getId());
                rabbitTemplate.convertAndSend("", "sync_question_cache", tempMap);
            }
            redisTemplate.opsForValue().increment(syncCacheKey, questions.size());
            log.info("题源同步进度：{}", redisTemplate.opsForValue().get(syncCacheKey));
        });
        commonQuestionService.findAndHandlerQuestion(consumer, subject);
        redisTemplate.expire(syncCacheKey, 1, TimeUnit.DAYS);
    }

    /**************************************处理试卷同步*****************************************************************************/

    @Override
    public void importPaper(List<Long> paperIds) {
        importPaperService.importPaper(paperIds);
    }

    @Override
    public void importPaper(long paperId) {
        importPaperService.importPaper(paperId);
    }

}
