package com.huatu.ztk.knowledge.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.huatu.ztk.knowledge.api.KnowledgeTreeDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.constant.CustomizeEnum;
import com.huatu.ztk.knowledge.constant.CustomizeEnum.ModeEnum;
import com.huatu.ztk.knowledge.servicePandora.SubjectService;
import com.huatu.ztk.knowledge.util.QuestionPointUtil;


/**
 * Created by shaojieyue
 * Created time 2016-05-06 18:25
 */

@Service
public class QuestionPointService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionPointService.class);

    @Autowired
    private KnowledgeTreeDubboService knowledgeTreeDubboService;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private PoxyUtilService poxyUtilService;

    @Autowired
    private RedisTemplate redisTemplate;

    private final static Map<Integer, String> subjectSimpleInfo = Maps.newHashMap();

    static {
        subjectSimpleInfo.put(200100049, "综素-小学");
        subjectSimpleInfo.put(200100051, "教知-小学");
        subjectSimpleInfo.put(200100050, "综素-中学");
        subjectSimpleInfo.put(200100052, "教知-中学");
    }


    /**
     * 根据用户信息组装知识点树 - 全树
     */
    public List<QuestionPointTree> questionPointTree(long userId, int subject, CustomizeEnum.ModeEnum modeEnum) {
        List<QuestionPoint> questionPoints = poxyUtilService.getQuestionPointService().getAllQuestionPoints(subject);
        //非空判断
        if (CollectionUtils.isEmpty(questionPoints)) {
            return Lists.newArrayList();
        }
        return questionPointTree(userId, subject, questionPoints, modeEnum);
    }

    /**
     * 根据用户信息组装知识点树 - 分级加载
     */
    public List<QuestionPointTree> questionPointTreeNode(long userId, int subject, int parent, CustomizeEnum.ModeEnum modeEnum) {
        List<QuestionPoint> pointList = poxyUtilService.getQuestionPointService().getQuestionPointsByParent(subject, parent);
        return questionPointTree(userId, subject, pointList, modeEnum);
    }

    /**
     * 根据用户信息组装知识点数
     */
    private List<QuestionPointTree> questionPointTree(long userId, int subject, List<QuestionPoint> questionPoints, CustomizeEnum.ModeEnum modeEnum) {
        List<QuestionPointTree> treeList = QuestionPointUtil.transform2Trees(questionPoints);
        //知识点->试题个数
        Map<Integer, Integer> pointQuestionMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(questionPoints)) {
            questionPoints.stream()
                    .forEach(questionPoint ->
                            pointQuestionMap.put(questionPoint.getId(), questionPoint.getQnumber())
                    );
        }

        if ("on".equals(redisTemplate.opsForValue().get("_k1:point:downgrade"))) {
            logger.info("_k1:point:downgrade>>>>>>>");
            Map<Integer, Integer> map = Maps.newHashMap();
            Map<Integer, Long> unfinishedPointMap2 = Maps.newHashMap();
            poxyUtilService.getQuestionPointService().handlerCount(treeList, map, map, pointQuestionMap, unfinishedPointMap2, userId);
            return treeList;
        }
        final Map<Integer, Integer> finishCountMap = poxyUtilService.getQuestionFinishService().countAll(userId);
        final Map<Integer, Integer> wrongCountMap = poxyUtilService.getQuestionErrorService().countAll(userId);
        Map<Integer, Long> unfinishedPointMap = getUnfinishedPointMapV2(userId, subject, modeEnum);
        List<QuestionPointTree> treeResult = poxyUtilService.getQuestionPointService().handlerCount(treeList, finishCountMap, wrongCountMap, pointQuestionMap, unfinishedPointMap, userId);
        //将统计信息合并到知识点树里面

        return treeResult;
    }

    /**
     * 查询用户特定科目下未完成的答题卡和知识点的映射关系
     *
     * @param userId
     * @param subject
     * @return
     */
    public Map<Integer, Long> getUnfinishedPointMapV2(long userId, int subject,CustomizeEnum.ModeEnum modeEnum) {
        ListOperations<String, String> opsForList = redisTemplate.opsForList();
        String unfinishedPointKey = RedisKnowledgeKeys.getUnfinishedPointListKey(userId, subject);
		if (modeEnum == CustomizeEnum.ModeEnum.Look) {
			unfinishedPointKey = RedisKnowledgeKeys.getUnfinishedPointListKeyV2(userId, subject, modeEnum.getKey());
		}
        List<String> unfinishedList = opsForList.range(unfinishedPointKey, 0, 0);
        Map<Integer, Long> unfinishedPointMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(unfinishedList)) {
            //知识点id_练习id
            String last = unfinishedList.get(0);
            int unfinishedPointId = Integer.valueOf(last.split("_")[0]);
            long practiceId = Long.valueOf(last.split("_")[1]);

            unfinishedPointMap.put(unfinishedPointId, practiceId);

            QuestionPoint tmpPoint = questionPointDubboService.findById(unfinishedPointId);
            //找出上一级知识点,直到顶级
            while (null != tmpPoint && tmpPoint.getParent() != 0) {
                tmpPoint = questionPointDubboService.findById(tmpPoint.getParent());
                unfinishedPointMap.put(tmpPoint.getId(), practiceId);
            }
        }
        return unfinishedPointMap;
    }

    /**
     * @param subject
     * @return
     */
    public List<QuestionPoint> getQuestionPoints(int subject) {
        return poxyUtilService.getQuestionPointService().getAllQuestionPoints(subject);
    }


    /**
     * TODO 拆分-延后
     * 组装知识点->数量 树,例如:错题,收藏,已做过
     *
     * @param countRedisKey 存储知识点->数量 的redis key
     * @param subject
     * @param isError       是否错题  @return
     */
    public List<QuestionPointTree> findCountPointTrees(String countRedisKey, int subject, boolean isError) {
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        //知识点->收藏数量 map
        final Map<Integer, Integer> entries = convert(hashOperations.entries(countRedisKey));
        //查询并组装知识点数
        final List<QuestionPoint> questionPoints = getQuestionPoints(subject);

        if (CollectionUtils.isEmpty(questionPoints)) {
            return new ArrayList<>();
        }

        final List<Integer> pointIds = questionPoints.stream()
                .map(QuestionPoint::getId)
                .filter(pointId -> entries.containsKey(pointId) && entries.get(pointId) != 0)
                .collect(Collectors.toList());
        final List<QuestionPointTree> questionPointTrees = knowledgeTreeDubboService.findByIds(pointIds);
        //填充收藏数量数据
        if (isError) {
            fillErrorPointTree(questionPointTrees, entries);
        } else {
            fillCollectPointTree(questionPointTrees, entries);
        }
        return questionPointTrees;
    }

    /**
     * 将map 转换类型
     *
     * @param source
     * @return
     */
    private Map<Integer, Integer> convert(Map<String, String> source) {
        final HashMap<Integer, Integer> target = Maps.newHashMapWithExpectedSize(source.size());
        if (target == null) {
            return Maps.newHashMap();
        }

        for (String key : source.keySet()) {
            Integer newValue = Ints.tryParse(source.get(key));
            Integer newKey = Ints.tryParse(key);
            if (newValue == null || newKey == null) {
                logger.error("illegal key or value,can`t convert to int,key={},value={}", key, source.get(key));
                continue;
            }

            target.put(newKey, newValue);
        }
        return target;
    }

    /**
     * 填充知识点树
     *
     * @param treeList
     * @param entries
     */
    private void fillErrorPointTree(List<QuestionPointTree> treeList, Map<Integer, Integer> entries) {
        if (CollectionUtils.isEmpty(treeList)) {
            return;
        }
        for (QuestionPointTree questionPointTree : treeList) {
            //设置错题数
            questionPointTree.setWnum(entries.getOrDefault(questionPointTree.getId(), 0));
            //递归调用
            fillErrorPointTree(questionPointTree.getChildren(), entries);
        }
    }

    /**
     * 填充收藏知识点树
     *
     * @param treeList
     * @param entries
     */
    private void fillCollectPointTree(List<QuestionPointTree> treeList, Map<Integer, Integer> entries) {
        if (CollectionUtils.isEmpty(treeList)) {
            return;
        }
        for (QuestionPointTree questionPointTree : treeList) {
            //设置收藏题数
            questionPointTree.setQnum(entries.getOrDefault(questionPointTree.getId(), 0));
            //递归调用
            fillCollectPointTree(questionPointTree.getChildren(), entries);
        }
    }

    /**
     * 查询用户已经做过的知识点
     *
     * @param uid     用户id
     * @param subject 科目
     * @return 返回用户已经做过知识点集合
     */
    public Set<Integer> findUserPoints(long uid, int subject) {
        //已完成知识点 key
        final String finishedPointKey = RedisKnowledgeKeys.getFinishedPointKey(uid);
        final SetOperations setOperations = redisTemplate.opsForSet();
        //获取集合里面所有的key,即是用户已经做过的id列表
        final Set<String> keys = setOperations.members(finishedPointKey);
        Set<Integer> results = Sets.newHashSet();
        //转换为int
        for (String key : keys) {
            final Integer pointId = Ints.tryParse(key);
            if (pointId != null) {
                results.add(pointId);
            }
        }

        //该科目下的所有知识点
        //注意此处取的知识点为审核通过的知识点,没有包含隐藏的知识点
        List<QuestionPoint> questionPoints = getQuestionPoints(subject);

        if (CollectionUtils.isEmpty(questionPoints)) {
            return Sets.newHashSet();
        }

        Set<Integer> totalPointIds = questionPoints.stream().map(QuestionPoint::getId).collect(Collectors.toSet());
        //将做过的知识点中不属于该科目的去掉
        results.removeIf(pid -> !totalPointIds.contains(pid));
        return results;
    }

    /**
     * 拼接顶级知识点正确率
     *
     * @param questionPointTrees 含用户做错题量的知识树
     * @param userId
     */
    public void handleAccuracy(List<QuestionPointTree> questionPointTrees, long userId) {
        List<Integer> points = questionPointTrees.stream().map(QuestionPointTree::getId).collect(Collectors.toList());
        final Map<Integer, Integer> finishCountMap = poxyUtilService.getQuestionFinishService().countByPoints(points, userId);
        for (QuestionPointTree questionPointTree : questionPointTrees) {
            int wnum = questionPointTree.getWnum();
            int finishNum = finishCountMap.get(questionPointTree.getId());
            if (finishNum < 100) {
                questionPointTree.setAccuracy(0);
            } else {
                int rightNum = finishNum - wnum;
                questionPointTree.setAccuracy(rightNum * 100 / finishNum);
            }
        }

    }

    /**
     * 返回空的科目数
     *
     * @param category
     * @return
     */
    public Object getQuestionTreeV2(int category) {

        List<Long> subjectIdList = subjectService.getSubjectIdListByCategory(category);
        List<Long> apply = getOtherCategoryInfo(subjectService::getSubjectIdListByCategory).apply(category);
        if (CollectionUtils.isNotEmpty(apply)) {
            subjectIdList.addAll(apply);
        }
        Set<QuestionPoint> allQuestionPoints = new HashSet<>();
        for (Long subjectId : subjectIdList) {
            List<QuestionPoint> questionPoints = fillTopPointName().apply(subjectId.intValue());
            if (CollectionUtils.isNotEmpty(questionPoints)) {
                allQuestionPoints.addAll(questionPoints);
            }
        }

        Map<Integer, Integer> map = Maps.newHashMap();
        Map<Integer, Long> map2 = Maps.newHashMap();
        List<QuestionPointTree> treeList = QuestionPointUtil.transform2Trees(allQuestionPoints);
        poxyUtilService.getQuestionPointService().handlerCount(treeList, map, map, map, map2, -1);
        treeList.sort(Comparator.comparing(i -> getPointSortRule(i.getName(), i.getId())));
        return treeList;

    }


    /**
     * @param category
     * @return
     */
    public Object getPointTreesByCategory(int category) {
        List<Long> subjectIdListByCategory = subjectService.getSubjectIdListByCategory(category);
        List<Long> apply = getOtherCategoryInfo(subjectService::getSubjectIdListByCategory).apply(category);
        if (CollectionUtils.isNotEmpty(apply)) {
            subjectIdListByCategory.addAll(apply);
        }
        Set<QuestionPoint> allQuestion = new HashSet<>();
        subjectIdListByCategory.forEach(subjectId -> {
            List<QuestionPoint> allQuestionPoints = fillTopPointName().apply(subjectId.intValue());
            if (CollectionUtils.isNotEmpty(allQuestionPoints)) {
                allQuestion.addAll(allQuestionPoints);
            }
        });
        allQuestion.stream().sorted(Comparator.comparing(i -> getPointSortRule(i.getName(), i.getId())));
        return allQuestion;

    }

    /**
     * 知识树排序规则
     *
     * @param name
     * @param id
     * @return
     */
    private String getPointSortRule(String name, int id) {
        StringBuilder sb = new StringBuilder();
        if (name.indexOf("(") > -1) {
            String substring = name.substring(name.indexOf("("));
            String[] split = substring.split("-");
            if (split.length > 1) {
                sb.append(split[1]).append(split[0]);
            } else {
                sb.append(substring);
            }
        }
        sb.append(id);
        return sb.toString();
    }

    /**
     * 关联查询其他科目信息
     *
     * @param function
     * @return
     */
    Function<Integer, List<Long>> getOtherCategoryInfo(Function<Integer, List<Long>> function) {
        Function<Integer, List<Long>> result = (l -> {
            List<Long> tempList = Lists.newArrayList();
            ArrayList<Integer> ids = Lists.newArrayList(200100048, 200100053);
            if (ids.contains(l)) {
                ids.remove(l);
                Integer tempId = ids.get(0);
                tempList = function.apply(tempId);
            }
            return tempList;
        });
        return result;
    }


    /**
     * 顶级知识点添加科目信息标识
     *
     * @return
     */
    Function<Integer, List<QuestionPoint>> fillTopPointName() {
        Function<Integer, List<QuestionPoint>> function = (i -> {
            List<QuestionPoint> allQuestionPoints = getQuestionPoints(i);
            if (subjectSimpleInfo.containsKey(i)) {
                ArrayList<QuestionPoint> result = Lists.newArrayList();
                for (QuestionPoint temp : allQuestionPoints) {
                    QuestionPoint point = new QuestionPoint();
                    try {
                        BeanUtils.copyProperties(point, temp);
                        StringBuilder stringBuilder = new StringBuilder(point.getName());
                        stringBuilder.append("(" + subjectSimpleInfo.get(i) + ")");
                        point.setName(stringBuilder.toString());
                        result.add(point);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                return result;
            }
            return allQuestionPoints;
        });
        return function;
    }


    /**
     * 专项练习组装知识点试题数量
     *
     * @param subject
     * @param parent
     * @return
     */
    public List<QuestionPointTree> questionPointTreeNode(int subject, int parent) {

        List<QuestionPoint> questionPoints = getQuestionPoints(subject);
        if (CollectionUtils.isEmpty(questionPoints)) {
            return Lists.newArrayList();
        }
        List<QuestionPoint> result = questionPoints.stream()
                .filter(questionPoint -> questionPoint.getQnumber() > 0)
                .collect(Collectors.toList());

        List<QuestionPointTree> treeList = QuestionPointUtil.transform2Trees(result);
        return treeList;
    }

    /**
     * 专项练习首页,查询用户知识树,组装用户数量
     *
     * @param pointIds
     * @param userId
     * @param subject
     * @return
     */
    public List<QuestionPointTree> getUserQuestionPointTree(String pointIds, long userId, int subject, CustomizeEnum.ModeEnum modeEnum) {

        List<String> points = Arrays.stream(pointIds.split(","))
                .collect(Collectors.toList());

        //每个知识点下用户已完成数量
        final Map<Integer, Integer> finishCountMap = poxyUtilService.getQuestionFinishService().countAll(userId);

        logger.info("已经完成的试题ID:{}", finishCountMap);

        //知识点下未完成答题卡ID
        Map<Integer, Long> unfinishedPointMap = getUnfinishedPointMapV2(userId, subject, modeEnum);
        List<QuestionPointTree> list = points.stream().map(point -> {
            Integer pointId = Integer.valueOf(point);
            Integer finishCount = finishCountMap.getOrDefault(Integer.valueOf(pointId), 0);
            Long unfinishedPracticeId = unfinishedPointMap.getOrDefault(Integer.valueOf(pointId), 0L);
            return QuestionPointTree.builder().id(pointId).userQnum(finishCount)
                    .unfinishedPracticeId(unfinishedPracticeId)
                    .build();

        }).collect(Collectors.toList());
        return list;
    }


    /**
     * 根据用户信息组装知识点树 - 分级加载
     */
    public List<QuestionPointTree> getVisitorModeQuestionPointTreeNode(int subject, int parent) {
        List<QuestionPoint> pointList = poxyUtilService.getQuestionPointService().getQuestionPointsByParent(subject, parent);
        if (CollectionUtils.isEmpty(pointList)) {
            return Lists.newArrayList();
        }
        List<QuestionPoint> questionPointList = pointList.stream().filter(questionPoint -> questionPoint.getQnumber() > 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(questionPointList)) {
            return Lists.newArrayList();
        }
        Map<Integer, Integer> pointQuestionNumMap = new HashMap();
        questionPointList.stream().forEach(point -> {
            pointQuestionNumMap.put(point.getId(), point.getQnumber());
        });
        List<QuestionPointTree> treeList = QuestionPointUtil.transform2Trees(questionPointList);
        return getCombinePointTree(treeList, pointQuestionNumMap);
    }


    /**
     * 游客模式,组装知识树题目数量
     *
     * @param treeList
     * @param pointQuestionNumMap
     * @return
     */
    private List<QuestionPointTree> getCombinePointTree(List<QuestionPointTree> treeList, Map<Integer, Integer> pointQuestionNumMap) {

        if (CollectionUtils.isEmpty(treeList)) {
            return Lists.newArrayList();
        }
        List<QuestionPointTree> collect = treeList.stream().map(tree -> {
            int pointId = tree.getId();
            List<QuestionPointTree> children = tree.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                getCombinePointTree(children, pointQuestionNumMap);
            }
            Integer pointQuestionNum = pointQuestionNumMap.getOrDefault(pointId, 0);
            tree.setQnum(pointQuestionNum);
            return tree;
        }).collect(Collectors.toList());
        return collect;
    }


}
