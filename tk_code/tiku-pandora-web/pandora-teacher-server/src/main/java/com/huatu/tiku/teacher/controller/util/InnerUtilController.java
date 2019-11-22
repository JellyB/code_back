package com.huatu.tiku.teacher.controller.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.question.BaseQuestionSearchMapper;
import com.huatu.tiku.teacher.service.InnerServiceImpl;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeSubjectService;
import com.huatu.tiku.teacher.service.knowledge.QuestionKnowledgeService;
import com.huatu.tiku.teacher.util.file.ExcelManageUtil;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import com.huatu.ztk.question.bean.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用以查询三端的数量 - 内部工具类
 * 所有的实现都放在该类中避免对其他的代码污染
 * Created by lijun on 2018/9/12
 */
@Slf4j
@RestController
@RequestMapping("innerUtil")
public class InnerUtilController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private InnerServiceImpl innerService;

    @Autowired
    private KnowledgeService knowledgeService;

    @Autowired
    private BaseQuestionSearchMapper baseQuestionSearchMapper;

    @Autowired
    KnowledgeSubjectService knowledgeSubjectService;

    @Autowired
    QuestionKnowledgeService questionKnowledgeService;

    @Autowired
    NewQuestionDao questionDao;

    /**
     * 获取三端数据
     */
    @GetMapping(value = "allCount/{subjectId}")
    public Object allCount(@PathVariable long subjectId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("mysql1");
        //mysql所有一级知识点对应的题数
        Map<Integer, Integer> mysqlData = getMysqlData(subjectId);
        stopWatch.stop();
        stopWatch.start("mongo1");
        //mongo所有知识点对应题数
        Map<Integer, Integer> mongoCountData = getMongoCountData(subjectId);
        stopWatch.stop();
        stopWatch.start("redis1");
        Map<Integer, Integer> redisCountData = getRedisCountData();
        stopWatch.stop();
        /**
         * 查询是否有subject
         */
        List<KnowledgeVO> knowledgeInfo = getKnowledgeInfo(subjectId);
        //递归 数据转换
        Function<KnowledgeVO, DataInfo>[] getDataInfo = new Function[1];
        getDataInfo[0] = (knowledgeVO) -> {
            Long knowledgeId = knowledgeVO.getKnowledgeId();
            DataInfo dataInfo = DataInfo.builder()
                    .id(knowledgeId)
                    .title(knowledgeVO.getKnowledgeName())
                    .mysqlNum(mysqlData.getOrDefault(knowledgeId.intValue(), 0))
                    .redisNum(redisCountData.getOrDefault(knowledgeId.intValue(), 0))
                    .mongoDBNum(mongoCountData.getOrDefault(knowledgeId.intValue(), 0))
                    .build();
            ArrayList<DataInfo> childrenList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(knowledgeVO.getKnowledgeTrees())) {
                knowledgeVO.getKnowledgeTrees().forEach(vo -> getDataInfo[0].apply(vo));
                dataInfo.setChildren(childrenList);
            }
            return dataInfo;
        };

        List<DataInfo> dataInfoArrayList = knowledgeInfo.parallelStream()
                .map(knowledgeVO -> getDataInfo[0].apply(knowledgeVO))
                .collect(Collectors.toList());
        log.info("整体耗时：{}", stopWatch.prettyPrint());
        return dataInfoArrayList;
    }

    /**
     * 查询某个知识点下的mysql和redis试题ID对比情况
     */
    @GetMapping("compare/mysql/redis/{point}")
    public Object compareMysqlAndRedis(@PathVariable long point) {
        long subjectId = checkAndGetSubject(point);
        //获取所有的试题节点信息(知识点ID和试题ID集合)
        Map<Integer, List<Integer>> mysqlMap = getMysqlQuestions(subjectId);

        List<Knowledge> all = knowledgeService.findAll();
        List<Long> points = Lists.newArrayList();
        innerService.findTailNodes(all, point, points);
        List<Integer> mysqlIds = Lists.newArrayList();
        List<Integer> redisIds = Lists.newArrayList();


        for (Long knowledgeId : points) {
            List<Integer> ids = mysqlMap.get(knowledgeId.intValue());
            if (CollectionUtils.isNotEmpty(ids)) {
                mysqlIds.addAll(ids);
            }
            List<Integer> questionIds = getQuestionsByKnowledgeInRedis(knowledgeId.intValue());
            if (CollectionUtils.isNotEmpty(questionIds)) {
                redisIds.addAll(questionIds);
            }
        }
        HashMap<Object, Object> result = Maps.newHashMap();
        result.put("mysql", mysqlIds.stream().distinct().count());
        result.put("redis", redisIds.stream().distinct().count());
        Collection<Integer> intersection = CollectionUtils.intersection(redisIds, mysqlIds);
        mysqlIds.removeAll(intersection);
        redisIds.removeAll(intersection);
        result.put("mysqlIds", mysqlIds);
        result.put("redisIds", redisIds);

        return result;

    }

    /**
     * 缓存redis中某个知识点下的试题
     * @param knowledgeId
     * @return
     */
    private List<Integer> getQuestionsByKnowledgeInRedis(int knowledgeId) {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try {
            final String pointQuesionIdsKey = RedisKnowledgeKeys.getPointQuesionIds(knowledgeId);
            Set<byte[]> members = connection.sMembers(pointQuesionIdsKey.getBytes());
            return  members.stream().map(String::new).map(Integer::parseInt).collect(Collectors.toList());
        } finally {
            connection.close();
        }
    }

    /**
     * 查询某个知识点下的mysql和redis试题ID对比情况
     */
    @GetMapping("compare/mysql/mongo/{point}")
    public Object compareMysqlAndMongo(@PathVariable long point) {
        long subjectId = checkAndGetSubject(point);
        //获取所有的试题节点信息
        Map<Integer, List<Integer>> mysqlMap = getMysqlQuestions(subjectId);
        Map<Integer, List<Integer>> mongoMap = getMongoQuestions(subjectId);
        List<Knowledge> all = knowledgeService.findAll();
        List<Long> points = Lists.newArrayList();
        innerService.findTailNodes(all, point, points);
        Set<Integer> mysqlIds = Sets.newHashSet();
        Set<Integer> mongoIds = Sets.newHashSet();

        for (Long knowledgeId : points) {
            List<Integer> ids = mysqlMap.get(knowledgeId.intValue());
            if (CollectionUtils.isNotEmpty(ids)) {
                mysqlIds.addAll(ids);
            }
            List<Integer> questionIds = mongoMap.get(knowledgeId.intValue());
            if (CollectionUtils.isNotEmpty(questionIds)) {
                mongoIds.addAll(questionIds);
            }
        }
        HashMap<Object, Object> result = Maps.newHashMap();
        result.put("mysql", mysqlIds.stream().distinct().count());
        result.put("mongo", mongoIds.stream().distinct().count());
        Collection<Integer> intersection = CollectionUtils.intersection(mongoIds, mysqlIds);
        mysqlIds.removeAll(intersection);
        mongoIds.removeAll(intersection);
        result.put("mysqlIds", mysqlIds);
        result.put("mongoIds", mongoIds);

        return result;

    }

    /**
     * 统计知识点变动导致的redis下试题绑定多个知识点的情况
     *
     * @param subjectId
     * @return
     */
    @GetMapping("handler/redis/knowledge/change/{subjectId}")
    public Object questionId(@PathVariable int subjectId) {
        List<KnowledgeVO> knowledgeVOList = getKnowledgeInfo(subjectId);
        Map<Integer,List<Integer>> questionMap = Maps.newHashMap();
        handlerRedisQuestionKownledge(knowledgeVOList,questionMap);
        List<Map.Entry<Integer, List<Integer>>> list = questionMap.entrySet().stream().filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
                .filter(entry -> entry.getValue().size() > 1).collect(Collectors.toList());
        Function<List<Map.Entry<Integer, List<Integer>>>,Map<Integer,List<Integer>>> findQuestionKnowledge = (entryList->{
            List<Long> questionIds = entryList.stream().map(Map.Entry::getKey).map(Long::new).collect(Collectors.toList());
            List<Question> questions = questionDao.findByIds(questionIds.stream().map(Long::intValue).collect(Collectors.toList()));
            HashMap<Integer,List<Integer>> map = Maps.newHashMap();
            for (Question question : questions) {
                List<KnowledgeInfo> pointList = question.getPointList();
                if(CollectionUtils.isNotEmpty(pointList)){
                    List<Integer> knowledges = pointList.stream().map(i -> {
                        List<Integer> points = i.getPoints();
                        int index = Math.min(2, points.size() - 1);
                        return points.get(index);
                    }).collect(Collectors.toList());
                    map.put(question.getId(),knowledges);
                }else if(question instanceof GenericQuestion){
                    List<Integer> points = ((GenericQuestion) question).getPoints();
                    int index = Math.min(2, points.size() - 1);
                    map.put(question.getId(),Lists.newArrayList(points.get(index)));
                }
            }
            return map;
        });
        Map<Integer, List<Integer>> questionKnowledges = findQuestionKnowledge.apply(list);
        List<List> result = Lists.newArrayList();
        getChangeKowledgeInfo(result,questionKnowledges,list);
        result.sort((a,b)->(String.valueOf(a.get(2)).compareTo(String.valueOf(b.get(2)))));
        String[] temp = new String[]{
                "试题ID", "旧知识点", "在缓存中的新知识点", "不在缓存中的新知识点"
        };
        try {
            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH,"知识树对比表格","xls",result,temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void getChangeKowledgeInfo(List<List> result, Map<Integer, List<Integer>> questionKnowledges, List<Map.Entry<Integer, List<Integer>>> list) {
        ArrayList<Long> knowldegeIds = Lists.newArrayList();
        if(questionKnowledges.isEmpty()|| CollectionUtils.isEmpty(list)){
            return;
        }
        list.stream().map(Map.Entry::getValue).forEach(i->knowldegeIds.addAll(i.stream().map(Long::new).collect(Collectors.toList())));
        questionKnowledges.entrySet().stream().map(Map.Entry::getValue).forEach(i->knowldegeIds.addAll(i.stream().map(Long::new).collect(Collectors.toList())));
        List<Map> knowledgeInfos = knowledgeService.getKnowledgeInfoByIds(knowldegeIds.stream().distinct().collect(Collectors.toList()));
        if(CollectionUtils.isEmpty(knowledgeInfos)){
            return;
        }
        Map<Integer, String> knowlegeMap = knowledgeInfos.stream().collect(Collectors.toMap(i -> MapUtils.getLong(i, "key").intValue(), i -> Arrays.stream((String[]) i.get("value")).collect(Collectors.joining("*")) ));
        for (Map.Entry<Integer, List<Integer>> entry : list) {
            Integer questionId = entry.getKey();
            List<Integer> redisKnowledgeIds = entry.getValue();
            List<Integer> mysqlKnowledgeIds = questionKnowledges.get(questionId);
            addChangeKnowledgeInfo(redisKnowledgeIds,mysqlKnowledgeIds,knowlegeMap,result,questionId);
        }
    }


    private void addChangeKnowledgeInfo(List<Integer> redisKnowledgeIds, List<Integer> mysqlKnowledgeIds, Map<Integer, String> knowlegeMap, List<List> result, Integer questionId) {
        Function<Collection<Integer>,String> getKnowledgeName = (ids-> {
            if(CollectionUtils.isEmpty(ids)){
                return "-";
            }
            return ids.stream().map(knowlegeMap::get).filter(i -> null != i).collect(Collectors.joining(","));
        });
        if(CollectionUtils.isEmpty(mysqlKnowledgeIds)){
            ArrayList<String> temp = Lists.newArrayList(
                    questionId+"",
                    getKnowledgeName.apply(redisKnowledgeIds),
                    "-",
                    "-"
            );
            result.add(temp);
            return;
        }
        Collection intersection = CollectionUtils.intersection(redisKnowledgeIds, mysqlKnowledgeIds);
        if(CollectionUtils.isNotEmpty(intersection)){
            mysqlKnowledgeIds.removeAll(intersection);
            redisKnowledgeIds.removeAll(intersection);
        }
        ArrayList<String> temp = Lists.newArrayList(questionId + "",
                getKnowledgeName.apply(redisKnowledgeIds),
                getKnowledgeName.apply(intersection),
                getKnowledgeName.apply(mysqlKnowledgeIds));
        result.add(temp);
    }

    /**
     * 对缓存中的知识点进行统计，根据试题ID
     * @param knowledgeVOList
     * @param questionMap
     */
    private void handlerRedisQuestionKownledge(List<KnowledgeVO> knowledgeVOList, Map<Integer, List<Integer>> questionMap) {
        if(CollectionUtils.isEmpty(knowledgeVOList)){
            return;
        }
        for (KnowledgeVO knowledgeVO : knowledgeVOList) {
            Long knowledgeId = knowledgeVO.getKnowledgeId();
            List<Integer> questionIds = getQuestionsByKnowledgeInRedis(knowledgeId.intValue());
            if(CollectionUtils.isNotEmpty(questionIds)){
                questionIds.forEach(id->{
                    List<Integer> knowledges = questionMap.getOrDefault(id, Lists.newArrayList());
                    knowledges.add(knowledgeId.intValue());
                    questionMap.put(id,knowledges);
                });
            }
            handlerRedisQuestionKownledge(knowledgeVO.getKnowledgeTrees(),questionMap);
        }
    }

    /**
     * 检查知识点并获取科目
     *
     * @param point
     * @return
     */
    private long checkAndGetSubject(long point) {
        Knowledge knowledge = knowledgeService.selectByPrimaryKey(point);
        if (null == knowledge) {
            throw new BizException(ErrorResult.create(10000123, "无效的知识点ID" + point));
        }
        Function<Long, Long> getSubjectId = (id -> {
            Example example = new Example(KnowledgeSubject.class);
            example.and().andEqualTo("knowledgeId", id);
            List<KnowledgeSubject> knowledgeSubjects = knowledgeSubjectService.selectByExample(example);
            if (null == knowledgeSubjects) {
                return -1L;
            } else {
                return knowledgeSubjects.get(0).getSubjectId();
            }
        });
        long subjectId = getSubjectId.apply(point);
        if (subjectId < -1) {
            throw new BizException(ErrorResult.create(10000123, "知识点" + knowledge.getName() + "无科目属性"));
        }
        return subjectId;
    }

    private Map<Integer, List<Integer>> getMysqlQuestions(long subjectId) {
        List<HashMap<String, Object>> hashMapList = baseQuestionSearchMapper.listAllMongoDBQuestionBySubject(subjectId);
        transDate(hashMapList);
        /**
         * 知识点ID对应试题知识点HASH的集合
         */
        Map<Integer, List<HashMap<String, Object>>> listMap = hashMapList.parallelStream()
                .collect(Collectors.groupingBy(
                        map -> MapUtils.getInteger(map, "knowledgeId", -1),
                        Collectors.toList()
                ));
        Map<Integer, List<Integer>> pointMap = Maps.newConcurrentMap();
        listMap.entrySet().parallelStream().forEach(entry -> {
            pointMap.put(entry.getKey(), entry.getValue().stream().map(i -> MapUtils.getInteger(i, "id", -1)).collect(Collectors.toList()));
        });
        return pointMap;
    }

    /**
     * 获取mongoDB 某个科目下数据
     */
    public Map<Integer, Integer> getMongoCountData(long subject) {
        //知识点下试题Id集合
        Map<Integer, List<Integer>> knowledgeMap = getMongoQuestions(subject);

        Map<Integer, Integer> resultMap = Maps.newHashMap();
        knowledgeMap.entrySet().stream().forEach(entry -> resultMap.put(entry.getKey(), entry.getValue().size()));
        return resultMap;
    }

    /**
     * @param subject
     * @return 返回知识点-》questionIDs（三级）
     */
    private Map<Integer, List<Integer>> getMongoQuestions(long subject) {
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
        Map<Integer, List<Integer>> questionPointsMap = innerService.findByAttrs(attrMap, "ztk_question_new");
        if (null == questionPointsMap && questionPointsMap.isEmpty()) {
            return Maps.newHashMap();
        }
        Map<Integer, List<Integer>> pointQuestionsMap = Maps.newHashMap();
        for (Map.Entry<Integer, List<Integer>> entry : questionPointsMap.entrySet()) {
            for (Integer pointId : entry.getValue()) {
                List<Integer> questionIds = pointQuestionsMap.getOrDefault(pointId, Lists.newArrayList());
                questionIds.add(entry.getKey());
                pointQuestionsMap.put(pointId, questionIds);
            }
        }
        return pointQuestionsMap;
    }

    /**
     * 获取 redis 中的全量数据
     */
    public Map<Integer, Integer> getRedisCountData() {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Map<Integer, Integer> resultMap = Maps.newHashMap();
        connection.hGetAll(RedisKnowledgeKeys.getPointSummaryKey().getBytes())
                .entrySet().stream()
                .forEach(entry -> resultMap.put(
                        Integer.valueOf(new String(entry.getKey())),
                        Integer.valueOf(new String(entry.getValue())))
                );
        return resultMap;
    }

    /**
     * 获取 mysql 中 某个科目下的数据
     */
    public Map<Integer, Integer> getMysqlData(long subjectId) {
        StopWatch stopWatch = new StopWatch("mysql数据查询");
        stopWatch.start("查询知识点");
        //获取所有的知识点信息
        List<KnowledgeVO> knowledgeInfoNoChildren = getKnowledgeInfo(subjectId);
        List<Knowledge> all = knowledgeService.findAll();
        HashMap<Long, List<Long>> knowledgeMap = Maps.newHashMap();

        stopWatch.stop();
        stopWatch.start("查询试题知识点信息");
        //获取所有的试题节点信息
        List<HashMap<String, Object>> hashMapList = baseQuestionSearchMapper.listAllMongoDBQuestionBySubject(subjectId);
        transDate(hashMapList);
        stopWatch.stop();
        //按照知识点分组
        stopWatch.start("知识点分组");
        Map<Integer, List<HashMap<String, Object>>> listMap = hashMapList.parallelStream()
                .collect(Collectors.groupingBy(
                        map -> MapUtils.getInteger(map, "knowledgeId", -1),
                        Collectors.toList()
                ));
        Map<Integer, Integer> pointNum = Maps.newConcurrentMap();
        listMap.entrySet().parallelStream()
                .forEach(entry ->
                        pointNum.put(
                                entry.getKey(),
                                entry.getValue().size()//此处获取 list 为所有的试题信息
                        )
                );
        stopWatch.stop();
        stopWatch.start("一二级知识点计算");
        //处理一二级情况
        knowledgeInfoNoChildren.stream().filter(i -> i.getLevel() == 1).forEach(i -> {
            List<Long> points = Lists.newArrayList();
            innerService.findTailNodes(all, i.getKnowledgeId(), points);
            int count = 0;
            for (Long knowledgeId : points.stream().distinct().collect(Collectors.toList())) {
                Integer num = pointNum.getOrDefault(knowledgeId.intValue(), 0);
                count += num;
            }
            pointNum.put(i.getKnowledgeId().intValue(), count);
        });
        stopWatch.stop();
        log.info("mysql查询详情：{}", stopWatch.prettyPrint());
        return pointNum;
    }

    /**
     * 筛选多知识点试题--只保留知识点id最小的
     *
     * @param hashMapList
     */
    private void transDate(List<HashMap<String, Object>> hashMapList) {
        if (CollectionUtils.isNotEmpty(hashMapList)) {
            HashMap<Integer, HashMap<String, Object>> questionMap = Maps.newHashMap();  //试题ID->知识点试题绑定关系
            for (HashMap<String, Object> questionPointMap : hashMapList) {
                Integer questionId = MapUtils.getInteger(questionPointMap, "id", -1);
                if (questionId <= 0) {
                    log.error("innerUtilController:transDate questionId get error,questionPointMap={}",questionPointMap);
                    continue;
                }
                HashMap<String, Object> temp = questionMap.get(questionId);
                if (null == temp) {
                    questionMap.put(questionId, questionPointMap);
                } else {
                    Integer knowledgeId = MapUtils.getInteger(temp, "knowledgeId", -1);
                    Integer newId = MapUtils.getInteger(questionPointMap, "knowledgeId", -1);
                    if (newId <= 0) {
                        log.error("innerUtilController:transDate knowledgeId get error,questionPointMap={}", questionPointMap);
                        continue;
                    }
                    if (newId < knowledgeId && newId != -1) {
                        questionMap.put(questionId, questionPointMap);
                    }
                }
            }
            hashMapList.clear();
            hashMapList.addAll(questionMap.values().stream().collect(Collectors.toList()));
        }
    }


    /**
     * 获取某个科目下的 所有的节点
     *
     * @param subjectId 科目ID
     */
    public List<KnowledgeVO> getKnowledgeInfo(long subjectId) {
        //1.获取一级节点
        List<KnowledgeVO> knowledgeVOList = knowledgeService.showKnowledgeTreeBySubject(subjectId, false);
        return knowledgeVOList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class DataInfo {
        private Long id;
        private String title;
        private Integer mysqlNum;
        private Integer redisNum;
        private Integer mongoDBNum;
        private List<DataInfo> children;
    }
}
