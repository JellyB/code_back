package com.huatu.tiku.teacher.service.impl.match;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeBasedTable;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.metas.AnswerCardSub;
import com.huatu.tiku.teacher.dao.mongo.AnswerCardDao;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.dao.question.MatchUserMetaMapper;
import com.huatu.tiku.teacher.service.match.MatchUserMetaService;
import com.huatu.tiku.util.http.ResponseMsg;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.LineSeries;
import com.huatu.ztk.paper.bean.MatchUserMeta;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.user.bean.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2017\11\16 0016.
 */
@Service
@Slf4j
public class PracticeMetaService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OldPaperDao paperDao;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private AnswerCardDao answerCardDao;
    @Autowired
    private NewQuestionDao questionDao;
    @Autowired
    private MatchUserMetaService matchUserMetaService;

    @Autowired
    private MatchUserMetaMapper matchUserMetaMapper;
    private final static int SPLIT_PAPERID = 2005327;   //模考大赛第十三季作为分界线，之后的所有考试地区使用新的position
    private final static int USER_ORDER_COUNT = 10;    //处理排名前count的成绩
    private final static int MIN_PER_SIZE = 10;        //曲线图的最小分组单位
    private final static float MIN_UTIL_SCORE = 0.001f;   //分数的最小单位
    private final static int ORDER_IN_POSITION_SIZE = 3;   //以省份为单位排前n的名次
    private final static long PER_FIND_MONGO_SIZE = 20000;   //每次批量查询的最大量级
    private final static int queryUserMetaSize = 20000;   //每次批量查询的最大量级
    //    private final static String MATCH_COUNT_DATA_FILE_PATH = "E:\\Temp\\";
    private final static String MATCH_COUNT_DATA_FILE_PATH = "/tmp/excel";

    /**
     * 模考大赛交卷人数查询
     *
     * @return
     */
    public Long getCountSubmit(int paperId) {
        Example example = new Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
        example.and().andEqualTo("matchId", paperId).andNotEqualTo("practiceId", -1);
        int i = matchUserMetaService.selectCountByExample(example);
        return new Long(i);
    }

    /**
     * 查询模考最高分
     * 查询最高分所在省份
     * 查询模考前10的账号、分数、报考城市（职位）
     */
    public Map getMaxScoreInfo(int paperId) {
        Map<Double, Set<String>> setMap = getScoreMap(USER_ORDER_COUNT, paperId);
        double max = setMap.keySet().stream().max(Double::compareTo).get();
        //获取最高分数所在省份
        List<MatchUserMeta> userMetas = getMaxUserMeta(setMap, max);
        List<String> positionNames = Lists.newArrayList();
        List<Integer> positionIds = Lists.newArrayList();
        userMetas.forEach(i -> {
            positionIds.add(i.getPositionId());
            positionNames.add(i.getPositionName());
        });
        List<Map<String, String>> orderInfo = Lists.newArrayList();
        for (Double score : setMap.keySet()) {
            List<MatchUserMeta> userMetaList = getMaxUserMeta(setMap, score);
            for (MatchUserMeta userMeta : userMetaList) {
                Map<String, String> tempMap = Maps.newHashMap();
                String positionName = userMeta.getPositionName();
                tempMap.put("userId", userMeta.getUserId() + "");
//                tempMap.put("userName",findUserName(userId));
                tempMap.put("positionName", positionName);
                tempMap.put("score", score + "");
                orderInfo.add(tempMap);
            }
        }
        log.info("前十名的信息实际有{}个，具体情况为：{}", orderInfo.size(), orderInfo);
        Map mapData = Maps.newHashMap();
        mapData.put("maxScore", max);
        mapData.put("maxScoreInPositionId", positionIds);
        mapData.put("maxScoreInPositionName", positionNames);
        mapData.put("orderScoreInfo", orderInfo);

        return mapData;
    }

    /**
     * 查询最高分和最高分所在地区
     *
     * @return
     */
    public Map getMaxScoreAndPosition(int paperId) {
        //最高分对应的答题卡
        Map<Double, Set<String>> setMap = getScoreMap(1, paperId);
        Map mapData = Maps.newHashMap();
        Double maxScore = setMap.keySet().stream().findFirst().get();
        mapData.put("maxScore", maxScore);
        List<MatchUserMeta> userMetaList = getMaxUserMeta(setMap, maxScore);
        mapData.put("maxScoreInPositionName", userMetaList.stream().map(i -> i.getPositionName()).distinct().collect(Collectors.toList()));
        return mapData;
    }

    /**
     * 获得平均分数
     *
     * @param paperId
     * @return
     */
    public double getAverage(int paperId) {
        Integer average = matchUserMetaMapper.average(paperId);
        log.info("平均分数为：{}", average);
        if(null == average){
            return 0;
        }
        return average.doubleValue();
    }

    /**
     * 获得曲线图
     *
     * @param paperId
     * @return
     */
    public Line getLine(int paperId) {
        int maxScore = paperDao.findById(paperId).getScore();
        if (maxScore % MIN_PER_SIZE != 0) {
            log.error("分组统计不均匀");
            return null;
        }
        double size = (double) MIN_PER_SIZE / 2;
        final TreeBasedTable<Double, String, Number> basedTable = TreeBasedTable.create();
        while (size < maxScore) {
            Example example = new Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
            example.and().andEqualTo("matchId",paperId).andBetween("score",size - 5 - MIN_UTIL_SCORE,size + 5 - MIN_UTIL_SCORE);
            long count = matchUserMetaService.selectCountByExample(example);
            basedTable.put(size, "区段人数", count);
            log.info("{}-{}分数区段的人数为{}", size - 5, size + 5, count);
            size += MIN_PER_SIZE;
        }
        Line line = table2LineSeries(basedTable);
        log.info("曲线图的对象为：{}", line);
        return line;
    }

    /**
     * 获取参考和平均分最高的前三名的省份
     */
    public Map getCountByPosition(int paperId) {
        //按省份统计参考人数和平均值
        Map<Integer, Map<String, String>> positionMap = getPositionMap(paperId);
        Map<Long, Set<Integer>> numMap = Maps.newHashMap();
        Map<Double, Set<Integer>> scoreMap = Maps.newHashMap();
        for (Map.Entry<Integer, Map<String, String>> entry : positionMap.entrySet()) {
            //省份
            Integer positionId = entry.getKey();
            //人数和平均值
            Map<String, String> mapData = entry.getValue();
            long count = Long.parseLong(mapData.get("count"));
            double average = Double.parseDouble(mapData.get("average"));
            if (numMap.get(count) == null) {
                Set<Integer> set = Sets.newHashSet();
                set.add(positionId);
                numMap.put(count, set);
            } else {
                Set<Integer> set = numMap.get(count);
                set.add(positionId);
            }
            if (scoreMap.get(average) == null) {
                Set<Integer> set = Sets.newHashSet();
                set.add(positionId);
                scoreMap.put(average, set);
            } else {
                Set<Integer> set = scoreMap.get(average);
                set.add(positionId);
            }
        }
        List<Long> numList = new ArrayList<>(numMap.keySet());
        List<Double> scoreList = new ArrayList<>(scoreMap.keySet());
        numList.sort((a, b) -> ((int) (b - a)));
        scoreList.sort((a, b) -> ((int) (b - a)));
        log.info("numList={}", numList);
        log.info("scoreList={}", scoreList);
        Set<Integer> numIds = getPreNumPosition(numList, numMap, ORDER_IN_POSITION_SIZE);
        Set<Integer> scoreIds = getPreScorePosition(scoreList, scoreMap, ORDER_IN_POSITION_SIZE);
        Map<Integer, Map<String, String>> numResult = Maps.newHashMap();
        List<Map<String, String>> numResultList = Lists.newArrayList();
        for (Integer id : numIds) {
            numResult.put(id, positionMap.get(id));
            Map<String, String> tempMap = positionMap.get(id);
            if (tempMap != null) {
                tempMap.put("id", id + "");
            }
            numResultList.add(tempMap);
        }
        Map<Integer, Map<String, String>> scoreResult = Maps.newHashMap();
        List<Map<String, String>> scoreResultList = Lists.newArrayList();
        for (Integer id : scoreIds) {
            scoreResult.put(id, positionMap.get(id));
            Map<String, String> tempMap = positionMap.get(id);
            if (tempMap != null) {
                tempMap.put("id", id + "");
            }
            scoreResultList.add(tempMap);
        }
        log.info("参考人数排名前{}的职位情况:{}", ORDER_IN_POSITION_SIZE, numResult);
        log.info("平均分排名前{}的职位情况:{}", ORDER_IN_POSITION_SIZE, scoreResult);
        Map mapData = Maps.newHashMap();
        numResultList.sort((a, b) -> comparingMap(a, b, "count"));
        scoreResultList.sort((a, b) -> comparingMap(a, b, "average"));
//        mapData.put("enrollPositionSort",numResultList);
//        mapData.put("averagePositionSort",scoreResultList);
        mapData.put("enrollPositionSort", numResult);
        mapData.put("averagePositionSort", scoreResult);
        return mapData;
    }

    private int comparingMap(Map<String, String> a, Map<String, String> b, String key) {
        String aValue = a.get(key);
        String bValue = b.get(key);
        if (Double.parseDouble(aValue) - Double.parseDouble(bValue) > 0) {
            return -1;
        } else if (aValue.equals(bValue)) {
            return 0;
        }
        return 1;
    }

    /**
     * 统计试题的错题率
     */
    public List<Map> getWrongQuestionMeta(int paperId) {
        Map<Integer, MatchQuestionMeta> mapData = getQuestionMeta(paperId);
        Map<Integer, List<MatchQuestionMeta>> mapByPart = Maps.newHashMap();
        Map<Integer, String> moduleMap = Maps.newHashMap();
        for (Map.Entry<Integer, MatchQuestionMeta> entry : mapData.entrySet()) {
            MatchQuestionMeta questionMeta = entry.getValue();
            int moduleId = questionMeta.getQuestion().getPoints().get(0);
            String moduleName = questionMeta.getQuestion().getPointsName().get(0);
            moduleMap.put(moduleId, moduleName);
            if (mapByPart.get(moduleId) == null) {
                List<MatchQuestionMeta> list = Lists.newArrayList();
                list.add(questionMeta);
                mapByPart.put(moduleId, list);
            } else {
                List<MatchQuestionMeta> list = mapByPart.get(moduleId);
                list.add(questionMeta);
            }
        }
        List<Map> resultList = Lists.newArrayList();
        for (Map.Entry<Integer, List<MatchQuestionMeta>> entry : mapByPart.entrySet()) {
            List<MatchQuestionMeta> tempList = entry.getValue();
            sortMatchQuestionMetaByDesc(tempList);
            double percent = tempList.get(0).getPercent();
            List<MatchQuestionMeta> result = tempList.stream().filter(i -> i.getPercent() == percent).collect(Collectors.toList());
            final List<Integer> ids = Lists.newArrayList();
            final List<Integer> seqs = Lists.newArrayList();
            result.forEach(i -> ids.add(i.getQuestion().getId()));
            result.forEach(i -> seqs.add(i.getQuestionSeq() + 1));
            log.info("科目{}的错题率最大为{}，题目有{}", entry.getKey(), percent, ids);
            Map tempMap = Maps.newHashMap();
            tempMap.put("subject", entry.getKey());
            tempMap.put("subjectName", moduleMap.get(entry.getKey()));
            tempMap.put("percent", percent);
            tempMap.put("questions", ids);
            tempMap.put("questionOrders", seqs);
            tempMap.put("detail", result);
            resultList.add(tempMap);
        }
        return resultList;
    }

    private void sortMatchQuestionMetaByDesc(List<MatchQuestionMeta> tempList) {
        for (int i = 1; i < tempList.size(); i++) {
            for (int j = 0; j < tempList.size() - i; j++) {
                if (tempList.get(j).getPercent() < tempList.get(j + 1).getPercent()) {
                    MatchQuestionMeta tempMeta = tempList.get(j);
                    tempList.set(j, tempList.get(j + 1));
                    tempList.set(j + 1, tempMeta);
                }
            }
        }
    }

    public Map<Integer, MatchQuestionMeta> getQuestionMeta(int paperId) {
        long start = System.currentTimeMillis();
        List<Integer> ids = paperDao.findById(paperId).getQuestions();
        //获取所有的试题信息
        long s1 = System.currentTimeMillis();
        log.info("1阶段耗时{}", s1 - start);
        List<Question> questions = questionDao.findByIds(ids);
        final Map<Integer, MatchQuestionMeta> questionMetaMap = Maps.newHashMap();
        questions.stream().filter(question->question instanceof GenericQuestion).forEach(question ->questionMetaMap.put(question.getId(), MatchQuestionMeta.builder().question((GenericQuestion) question).build()));
        long s2 = System.currentTimeMillis();
        log.info("2阶段耗时{}", s2 - s1);
        Example example = new Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
        example.and().andEqualTo("matchId", paperId).andNotEqualTo("practiceId", -1);
        List<com.huatu.tiku.match.bean.entity.MatchUserMeta> metas = matchUserMetaService.selectByExample(example);
        List<Long> practiceIds = metas.stream().map(com.huatu.tiku.match.bean.entity.MatchUserMeta::getPracticeId).collect(Collectors.toList());
        long s3 = System.currentTimeMillis();
        log.info("3阶段耗时{}", s3 - s2);
        long size = practiceIds.size();
        long index = 0;
        Long[] wrongCount = new Long[ids.size()];
        Long[] finishedCount = new Long[ids.size()];
        List<Map<String, Long>> choiceTimes = Lists.newArrayListWithCapacity(ids.size());
        for (int i = 0; i < wrongCount.length; i++) {
            wrongCount[i] = 0L;
            finishedCount[i] = 0L;
        }
        while (true) {
            long end = index + PER_FIND_MONGO_SIZE;
            if (size < end) {
                end = size;
            }
            List<Long> tempList = practiceIds.subList((int) index, (int) end);
            List<AnswerCardSub> answerCards = answerCardDao.findAllByIds(tempList, "corrects");
            for (AnswerCardSub answerCard : answerCards) {
                int[] results = Arrays.stream(answerCard.getArrays()).mapToInt(k -> Integer.parseInt(String.valueOf(k))).toArray();
                if (results.length != ids.size()) {
                    continue;
                }
                for (int i = 0; i < results.length; i++) {
                    if (results[i] == 2) {
                        wrongCount[i]++;
                    }
                    if (results[i] != 0) {
                        finishedCount[i]++;
                    }
                }
            }
            long s4 = System.currentTimeMillis();
            log.info("查询题目正确与否耗时{},size={},end={}", s4 - s3, size, end);
            List<AnswerCardSub> answers = answerCardDao.findAllByIds(tempList, "answers");
            for (AnswerCardSub answer : answers) {
                List<String> submitAnswer = Arrays.stream(answer.getArrays()).map(k -> String.valueOf(k)).collect(Collectors.toList());
                for (int a = 0; a < submitAnswer.size(); a++) {
                    Map<String, Long> choiceTimeMap = null;
                    if (choiceTimes.size() == a || choiceTimes.get(a) == null) {
                        choiceTimeMap = Maps.newHashMap();
                        choiceTimes.add(choiceTimeMap);
                    } else {
                        choiceTimeMap = choiceTimes.get(a);
                    }
                    String choiceTemp = (submitAnswer.get(a).equals("0") || StringUtils.isBlank(submitAnswer.get(a)) )? "" : (char) (Integer.parseInt(submitAnswer.get(a)) - 1 + 'A') + "";
                    if (StringUtils.isNotBlank(choiceTemp)) {
                        long submitCount = choiceTimeMap.getOrDefault(choiceTemp, 0L);
                        submitCount++;
                        choiceTimeMap.put(choiceTemp, submitCount);
                    }
                }
            }
            long s5 = System.currentTimeMillis();
            log.info("查询用户答题数据耗时{},size={},end={}", s5 - s4, size, end);
            if (end == size) {
                break;
            }
            index = end + 1;
        }
        long s5 = System.currentTimeMillis();
        log.info("5阶段耗时{}", s5 - s3);
        log.info("错误次数为{}，完成题数{}", wrongCount, finishedCount);
        for (int i = 0; i < ids.size(); i++) {
            float fPercent = 0;
            if (finishedCount[i].intValue() > 0) {
                fPercent = (float) wrongCount[i] / finishedCount[i];
            }
            double percent = new BigDecimal(fPercent).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            int id = ids.get(i);
            MatchQuestionMeta tempMeta = questionMetaMap.get(id);
            if(null == tempMeta){
                continue;
            }
            tempMeta.setFinishCount(finishedCount[i]==null?0:finishedCount[i]);
            tempMeta.setQuestionSeq(i);
            tempMeta.setWrongCount(wrongCount[i]==null?0:wrongCount[i]);
            if (i < choiceTimes.size() - 1) {
                tempMeta.setChoiceTime(choiceTimes.get(i));
            }
            tempMeta.setRightCount(tempMeta.getFinishCount()-tempMeta.getWrongCount());
            tempMeta.setPercent(percent);
        }
        long s6 = System.currentTimeMillis();
        log.info("6阶段耗时{}", s6 - s5);
        return questionMetaMap;
    }

    private Set<Integer> getPreScorePosition(List<Double> scoreList, Map<Double, Set<Integer>> scoreMap, int total) {
        int count = 0;
        Set<Integer> result = Sets.newHashSet();
        for (Double num : scoreList) {
            Set<Integer> set = scoreMap.get(num);
            result.addAll(set);
            count += set.size();
            if (total <= count) {
                break;
            }
        }
        return result;
    }

    private Set<Integer> getPreNumPosition(List<Long> numList, Map<Long, Set<Integer>> numMap, int total) {
        int count = 0;
        Set<Integer> result = Sets.newHashSet();
        for (Long num : numList) {
            Set<Integer> set = numMap.get(num);
            result.addAll(set);
            count += set.size();
            if (total <= count) {
                break;
            }
        }
        return result;
    }

    /**
     * 按省份统计参考人数和平均值
     */
    public Map<Integer, Map<String, String>> getPositionMap(int paperId) {
        Example example = new Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
        example.and().andEqualTo("matchId", paperId).andNotEqualTo("practiceId", -1);
        List<com.huatu.tiku.match.bean.entity.MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        if (CollectionUtils.isEmpty(matchUserMetas)) {
            return Maps.newHashMap();
        }
        matchUserMetas.removeIf(i -> null == i.getScore());
        Map<Integer, List<com.huatu.tiku.match.bean.entity.MatchUserMeta>> collect = matchUserMetas.stream().collect(Collectors.groupingBy(com.huatu.tiku.match.bean.entity.MatchUserMeta::getPositionId));
        Map<Integer, Map<String, String>> countInfo = Maps.newHashMap();
        for (Map.Entry<Integer, List<com.huatu.tiku.match.bean.entity.MatchUserMeta>> entry : collect.entrySet()) {
            int positionId = entry.getKey();
            List<com.huatu.tiku.match.bean.entity.MatchUserMeta> value = entry.getValue();
            long size = value.size();
            if (size > 0) {
                Map<String, String> tempMap = Maps.newHashMap();
                String positionName = value.get(0).getPositionName();
                tempMap.put("name", positionName);
                tempMap.put("count", size + "");
                System.out.println(value.stream().map(com.huatu.tiku.match.bean.entity.MatchUserMeta::getScore).map(String::valueOf).collect(Collectors.joining(",")));
                Double sum = value.stream().mapToDouble(com.huatu.tiku.match.bean.entity.MatchUserMeta::getScore).sum();
                tempMap.put("average", sum / size + "");
                countInfo.put(positionId, tempMap);
            }
        }

        return countInfo;
    }

    /**
     * 生成曲线对象
     *
     * @param table
     * @return
     */
    private static final Line table2LineSeries(TreeBasedTable<Double, String, ? extends Number> table) {
        final Set<String> columnKeySet = table.columnKeySet();
        final Set<Double> rowKeySet = table.rowKeySet();
        List<LineSeries> seriesList = new ArrayList<>(rowKeySet.size());
        for (Double nodeScore : rowKeySet) {
            List data = new ArrayList(columnKeySet.size());
            for (String column : columnKeySet) {
                Number number = table.get(nodeScore, column);
                if (number == null) {//为空则进行初始化
                    number = Double.valueOf(0);
                }
                data.add(number);
            }

            final LineSeries lineSeries = LineSeries.builder()
                    .name((nodeScore - 5) + "~" + (nodeScore + 5))
                    .data(data)
                    .build();
            seriesList.add(lineSeries);
        }

        final Line line = Line.builder()
                .categories(Lists.newArrayList(columnKeySet))
                .series(seriesList)
                .build();
        return line;
    }

    /**
     * @param userId
     * @return
     */
    private String findUserName(String userId) {
        String sql = "select uname from v_qbank_user where pukey = ?";
        Object[] params = {userId};
        String uname = "";
        try {
//            uname = jdbcTemplate.queryForObject(sql,params,String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uname;
    }

    /**
     * 获取摸一个分值的所有用户报名信息
     *
     * @param setMap
     * @param max
     * @return
     */
    private List<MatchUserMeta> getMaxUserMeta(Map<Double, Set<String>> setMap, double max) {
        log.info("setMap = {}", setMap);
        Set<String> set;
        if (max > 0) {
            set = setMap.get(max);
        } else {
            final Set<String> tempSet = Sets.newHashSet();
            setMap.values().forEach(i -> tempSet.addAll(i));
            set = tempSet;
        }
        Set<Long> longSet = Sets.newHashSet();
        set.forEach(i -> longSet.add(Long.parseLong(i)));
        log.info("分数最高的答题卡有{}个", set.size());
        List<MatchUserMeta> userMetas = getUserMetas(longSet);
        log.info("分数最高的报名信息，共{}条，信息{}", userMetas.size(), userMetas);
        return userMetas;
    }

    private List<MatchUserMeta> getUserMetas(Set<Long> longSet) {
        if (CollectionUtils.isEmpty(longSet)) {
            return Lists.newArrayList();
        }
        tk.mybatis.mapper.entity.Example example = new tk.mybatis.mapper.entity.Example(com.huatu.tiku.match.bean.entity.MatchUserMeta.class);
        example.and().andIn("practiceId", longSet.stream().collect(Collectors.toList()));
        List<com.huatu.tiku.match.bean.entity.MatchUserMeta> matchUserMetas = matchUserMetaService.selectByExample(example);
        return matchUserMetas.stream().map(i ->
                MatchUserMeta.
                        builder().
                        paperId(i.getMatchId()).
                        positionId(i.getPositionId()).
                        positionName(i.getPositionName()).
                        practiceId(i.getPracticeId()).
                        userId(i.getUserId()).build())
                .collect(Collectors.toList());
    }

    /**
     * 分数前size名的练习id（答题卡）按分数分组
     *
     * @param size
     * @param paperId
     * @return
     */
    private Map<Double, Set<String>> getScoreMap(int size, int paperId) {
        //TODO ceshi
        List<com.huatu.tiku.match.bean.entity.MatchUserMeta> metas = matchUserMetaService.findOrderByScore(paperId, size * 2);
        if (CollectionUtils.isEmpty(metas)) {
            log.error("无排名数据");
            return Maps.newHashMap();
        }
        Map<Double, Set<String>> setMap = Maps.newHashMap();
        Map<Double, List<com.huatu.tiku.match.bean.entity.MatchUserMeta>> collect = metas.stream().collect(Collectors.groupingBy(com.huatu.tiku.match.bean.entity.MatchUserMeta::getScore));
        List<Map.Entry<Double, List<com.huatu.tiku.match.bean.entity.MatchUserMeta>>> scoreMaps = collect.entrySet().stream().collect(Collectors.toList());
        scoreMaps.sort(Comparator.comparing(i -> -i.getKey()));
        int index = 0;
        for (Map.Entry<Double, List<com.huatu.tiku.match.bean.entity.MatchUserMeta>> scoreMap : scoreMaps) {
            Double key = scoreMap.getKey();
            List<com.huatu.tiku.match.bean.entity.MatchUserMeta> value = scoreMap.getValue();
            setMap.put(key, value.stream().map(com.huatu.tiku.match.bean.entity.MatchUserMeta::getPracticeId).map(String::valueOf).collect(Collectors.toSet()));
            index += value.size();
            if (index >= size) {
                break;
            }
        }
        log.info("排名数据 = {}", setMap);
        return setMap;
    }

    /**
     * 获取最高分
     *
     * @param index
     * @param size
     * @param paperId
     * @return
     */
    public double getMaxScore(int index, int size, int paperId) {
        Set<ZSetOperations.TypedTuple<String>> withScores = findMaxScoreInfo(index, size, paperId);
        double max = 0;
        if (CollectionUtils.isNotEmpty(withScores)) {
            max = new ArrayList<>(withScores).get(0).getScore();
            log.info("最高分为：{}", max);
        } else {
            log.error("无法获取排名信息");
        }
        return max;
    }

    /**
     * 获取分数排名index+1至index+size的答题卡和分数
     *
     * @param index
     * @param size
     * @param paperId
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> findMaxScoreInfo(int index, int size, int paperId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String resultScoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        Set<ZSetOperations.TypedTuple<String>> withScores =
                zSetOperations.reverseRangeWithScores(resultScoreKey, index, size - 1);
        String str = "";
        for (ZSetOperations.TypedTuple<String> tuple : withScores) {
            str += tuple.getScore() + "-->" + tuple.getValue() + ";";
        }
        log.info("获取前{}名的答题信息{}", withScores.size(), str);
        return withScores;
    }

    /**
     * 获取某一个分数的所有成员
     *
     * @param minScore
     * @param paperId
     * @return
     */
    public Set<String> findMinScoreInfo(double minScore, int paperId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String resultScoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        Set<String> setIds =
                zSetOperations.reverseRangeByScore(resultScoreKey, minScore, minScore);
        log.info("获取分数为{}的答题信息共{}个，具体信息：{}", minScore, setIds.size(), setIds);
        return setIds;
    }


    private int sumArray(int[] array, int i, int j) {
        int total = 0;
        for (int k = i; k < j; k++) {
            total += array[k];
        }
        return total;
    }


    public File parseStatements(Map mapData, int paperId) throws BizException {
        StringBuilder sb = new StringBuilder("");
        //交卷人数
        Object submitCount = mapData.get("submitCount");
        sb.append("交卷人数：").append(submitCount).append("\r\n");
        Object maxScore = mapData.get("maxScore");
        sb.append("最高分：").append(maxScore).append("\r\n");
        Object average = mapData.get("average");
        sb.append("平均分：").append(average).append("\r\n");
        Line line = (Line) mapData.get("line");
        sb.append("分数分布情况：\r\n");
        if(null == line){
            throw new BizException(ErrorResult.create(10032141,"下载失败"));
        }
        List<LineSeries> series = line.getSeries();
        for (LineSeries lineSeries : series) {
            sb.append("分数在").append(lineSeries.getName()).append("区间的人数有：").append(lineSeries.getData().get(0)).append("\r\n");
        }
        List<String> maxScoreInPositionName = (List) mapData.get("maxScoreInPositionName");
        List<Integer> maxScoreInPositionId = (List) mapData.get("maxScoreInPositionId");
        sb.append("最高分所在的省份(名称+id)有：");
        for (int i = 0; i < maxScoreInPositionName.size(); i++) {
            sb.append(maxScoreInPositionName.get(i)).append("(").append(maxScoreInPositionId.get(i)).append("),");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\r\n");
        Map<Integer, Map<String, String>> numResult = (Map) mapData.get("enrollPositionSort");
        List<Map<String, String>> numPositionList = numResult.values().stream().collect(Collectors.toList());
        numPositionList.sort((a, b) -> (Integer.parseInt(b.get("count")) - Integer.parseInt(a.get("count"))));
        sb.append("参加人数排名前三的省份有：");
        for (Map<String, String> position : numPositionList) {
            sb.append(position.get("name")).append("(").append(position.get("count")).append("人) ");
        }
        sb.append("\r\n");
        sb.append("平均分排名前三的省份有：");
        Map<Integer, Map<String, String>> averagePositionSort = (Map) mapData.get("averagePositionSort");
        List<Map<String, String>> averagePositionList = averagePositionSort.values().stream().collect(Collectors.toList());
        averagePositionList.sort((a, b) -> (Integer.parseInt(b.get("count")) - Integer.parseInt(a.get("count"))));
        for (Map<String, String> position : averagePositionList) {
            sb.append(position.get("name")).append("(").append(position.get("average")).append(") ");
        }
        sb.append("\r\n");
        sb.append("前十名学员的情况如下：\r\n");
        List<Map<String, String>> orderInfo = (List) mapData.get("orderScoreInfo");
        Map<Long, String> userMap = Maps.newHashMap();
        long[] ids = orderInfo.stream().mapToLong(info -> Long.parseLong(String.valueOf(info.get("userId")))).toArray();
        List<Long> userIds = Lists.newArrayList();
        for (long id : ids) {
            userIds.add(id);
        }
        userMap.putAll(getUserInfo(userIds.stream().map(i -> UserDto.builder().id(i).build()).collect(Collectors.toList())));
        orderInfo.sort((a, b) -> (int) (Double.parseDouble(b.get("score")) - Double.parseDouble(a.get("score"))));
        for (Map<String, String> tempMap : orderInfo) {
            sb.append("用户名：").append(userMap.get(Long.parseLong(tempMap.get("userId")))).append("|分数：").append(tempMap.get("score")).append("|省份：").append(tempMap.get("positionName")).append("\r\n");
        }

        List<Map> wrongList = (List<Map>) mapData.get("wrongList");
        sb.append("每个模块下错题率最大的试题有：\r\n");
        for (Map map : wrongList) {
            sb.append("---------------------------------------------").append("\r\n");
            sb.append(map.get("subjectName")).append("|错题率:").append(map.get("percent")).append("\r\n");
            List<MatchQuestionMeta> list = (List) map.get("detail");
            for (MatchQuestionMeta meta : list) {
                sb.append("考点类型:").append(meta.getQuestion().getPointsName().get(meta.getQuestion().getPointsName().size()-1)).append("\r\n");
                sb.append(meta.getQuestionSeq() + 1).append("、").append(meta.getQuestion().getStem().trim()).append("\r\n");
                int i = 0;
                for (String choice : meta.getQuestion().getChoices()) {
                    sb.append("选项").append((char) ('A' + i)).append(":").append(choice.trim()).append("\r\n");
                    i++;
                }
                sb.append("解析:").append(meta.getQuestion().getAnalysis().trim()).append("\r\n");
                sb.append("答案:").append(convertAnswer(meta.getQuestion().getAnswer() + "")).append("\r\n");
            }

        }
        String path = MATCH_COUNT_DATA_FILE_PATH + paperId + ".txt";
        File file = new File(path);
        try {
            FileUtils.write(file, sb.toString());
            //转换发送数据
            Paper paper = paperDao.findById(paperId);
            //log.info("写入文件{}中",file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
//        System.out.print(sb.toString());
    }

    private String convertAnswer(String answer) {
        String answers = answer.replace("1", "A")
                .replace("2", "B")
                .replace("3", "C")
                .replace("4", "D")
                .replace("5", "E")
                .replace("6", "F")
                .replace("7", "G")
                .replace("8", "H");
        return answers;
    }


    private Map<Long, String> getUserInfo(List<UserDto> userDtos) {
        String url = "https://ns.huatu.com/u/essay/statistics/user";
        RestTemplate restTemplate = new RestTemplate();
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        List<LinkedHashMap<String, Object>> data = userDtoList.getData();
        Map<Long, String> result = Maps.newHashMap();
        for (LinkedHashMap<String, Object> datum : data) {
            Long id = MapUtils.getLong(datum, "id", -1L);
            String name = MapUtils.getString(datum, "name", "");
            result.put(id, name);
        }
        return result;
    }


}
