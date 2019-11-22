package com.huatu.ztk.backend;//package com.huatu.ztk.backend.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeBasedTable;
import com.google.common.primitives.Ints;
import com.huatu.ztk.backend.metas.bean.AnswerCardSub;
import com.huatu.ztk.backend.metas.bean.MatchQuestionMeta;
import com.huatu.ztk.backend.paper.dao.AnswerCardDao;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.backend.paper.dao.PracticeDao;
import com.huatu.ztk.backend.user.dao.UserHuatuDao;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.LineSeries;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PositionConstants;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.user.bean.UserDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by huangqp on 2017\11\14 0014.
 */
public class MatchCalculationTest extends BaseTestW{
    private static final Logger logger = LoggerFactory.getLogger(MatchCalculationTest.class);
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PracticeDao practiceDao;
    @Autowired
    private AnswerCardDao answerCardDao;
    @Autowired
    private MatchDao matchDao;
    @Autowired
    private UserHuatuDao userHuatuDao;
    public static void main(String arges[]){

    }

    @Test
    public void test11(){
        String[] ids = {"2005323","2005327","2005341","2005342","2005345"};
        List<String> matchIds = Arrays.asList(ids);
        List<ConcurrentHashMap<String, Object>> result = getRecordInfoByMatchIds(matchIds);
    }

    /**
     * 汇总 模考大赛统计信息
     * userID - time - 是否参加
     *
     * @param matchIds 需要统计的模考大赛 ids,统计结果会按照此ID 顺序排序
     * @return 汇总结果
     */
    public List<ConcurrentHashMap<String, Object>> getRecordInfoByMatchIds(final List<String> matchIds) {
        long beginTime = System.currentTimeMillis();
        if (null == matchIds || matchIds.size() == 0) {
            return null;
        }
        //缓存每个用户的结果集合
        ConcurrentHashMap<Long, String[]> map = new ConcurrentHashMap<>();
        for (int index = 0; index < matchIds.size(); index++) {
            //1.获取一次的模考大赛数据 作为统计基点
            List<MatchUserMeta> allMatchUserMeta = matchDao.findAllMatchUserMeta(matchIds.get(index));

            //2.获取此次模考对应的提交试卷数据
            ZSetOperations<String, String> zSet = redisTemplate.opsForZSet();
            Set<String> set = zSet.range(PaperRedisKeys.getPaperPracticeIdSore(Integer.valueOf(matchIds.get(index))), 0, -1);

            //3.生成当前 成绩缺失补全信息,在Stream 中无法使用 index
            final int maxLength = matchIds.size();
            final int stepIndex = index;
            //组合数据
            allMatchUserMeta.parallelStream()
                    .forEach(matchUserMeta -> {
                                //判断当次考试是否有成绩 有成绩返回 -> 1
                                map.compute(matchUserMeta.getUserId(), (key, value) -> {
                                    String thisTimeResult = set.contains(String.valueOf(matchUserMeta.getPracticeId()))
                                            ? "1" : "0";
                                    if (value == null) {
                                        value = new String[maxLength];
                                    }
                                    value[stepIndex] = thisTimeResult;
                                    return value;
                                });
                            }
                    );
        }
        logger.info("step 1 time = {}" + (System.currentTimeMillis() - beginTime));
        //生成 数据格式 key : userId ,value : [null,0,1] null -> 未报名,0 -> 报名未考试 , 1 已考试
        //排序
        List<Map.Entry<Long, String[]>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, (o1, o2) -> {
            Integer o1Times = Stream.of(o1.getValue())
                    .filter(data -> null != data)
                    .map(Integer::valueOf).reduce(0, (a, b) -> a + b);

            Integer o2Times = Stream.of(o2.getValue())
                    .filter(data -> null != data)
                    .map(Integer::valueOf).reduce(0, (a, b) -> a + b);
            return o2Times - o1Times;
        });
        logger.info("step 2 time = {}" + (System.currentTimeMillis() - beginTime));

        //生成带有 用户数据的信息
        final int totalTimes = matchIds.size();

        List<ConcurrentHashMap<String, Object>> collect = list.stream()
                .map(entry -> {
                    ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<>();
                    Long userId = entry.getKey();
                    //User user = userDao.findById(userId);
                    UserDto user = userHuatuDao.findById(userId);
                    hashMap.put("userName", user.getNick());
                    hashMap.put("phoneNum", user.getMobile()==null?(user.getName()==null?"":user.getName()):user.getMobile());
                    String[] valueArray = entry.getValue();
                    // _0 -> 报名; _1 -> 签到
                    // 0: 未成功 ; 1: 成功
                    for (int index = 0; index < valueArray.length; index++) {
                        if (valueArray[index] == null) {
                            hashMap.put("time_" + index + "_0", "0");
                            hashMap.put("time_" + index + "_1", "0");
                        } else if (valueArray[index].equals("0")) {
                            hashMap.put("time_" + index + "_0", "1");
                            hashMap.put("time_" + index + "_1", "0");
                        } else if (valueArray[index].equals("1")) {
                            hashMap.put("time_" + index + "_0", "1");
                            hashMap.put("time_" + index + "_1", "1");
                        }
                    }

                    return hashMap;
                })
                .collect(Collectors.toList());
        logger.info("step 3 time = {}", (System.currentTimeMillis() - beginTime));
        String json = JsonUtil.toJson(collect);

        File file = new File("C:\\Users\\huangqp\\Desktop\\ztk.txt");
        try {
            FileUtils.write(file, json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    @Test
    public void testMain(){
        long start = System.currentTimeMillis();
        logger.info("开始统计……");
        testCountSubmit();
        testMaxScore();
        testAverage();
        testGraphInfo();
        testCountByPosition();
        testWrongQuestion();
        long end = System.currentTimeMillis();
        logger.info("统计结束！耗时{}",end-start);

    }
    /**
     * 查询总交卷人数
     */
    @Test
    public void testCountSubmit(){
        int paperId = 2005327;
        SetOperations setOperations = redisTemplate.opsForSet();
        String submitSetKey = MatchRedisKeys.getMatchSubmitPracticeIdSetKey(paperId);
        long size = setOperations.size(submitSetKey);
        logger.info("模考大赛交卷人数为：{}",size);
    }

    /**
     * 查询模考最高分
     * 查询最高分所在省份
     * 查询模考前10的账号、分数、报考城市（职位）
     */
    @Test
    public void testMaxScore(){
        int paperId = 2005327;
        int size = 1;
        int index = 0;
        double max = getMaxScore(index,size,paperId);
        int count = 10;    //处理排名前count的成绩
        Map<Double,Set<String>> setMap = getScoreMap(count,paperId);
        //获取最高分数所在省份
        List<MatchUserMeta> userMetas = getMaxUserMeta(setMap,max);
        final Map<Long,String> positionMap = Maps.newHashMap();
        userMetas.forEach(userMeta->positionMap.put(userMeta.getPracticeId(),userMeta.getPositionName()));
        logger.info("最高分所在省份是{}",positionMap);
        List<Map<String,String>> orderInfo = Lists.newArrayList();
        for(Double score:setMap.keySet()){
            List<MatchUserMeta> userMetaList = getMaxUserMeta(setMap,score);
            for(MatchUserMeta userMeta:userMetaList){
                Map<String,String> tempMap = Maps.newHashMap();
                String userId = userMeta.getId().split("_")[0];
                String positionName = userMeta.getPositionName();
                tempMap.put("userId",userId);
                tempMap.put("userName",findUserName(userId));
                tempMap.put("positionName",positionName);
                tempMap.put("score",score+"");
                orderInfo.add(tempMap);
            }
        }
        logger.info("前十名的信息实际有{}个，具体情况为：{}",orderInfo.size(),orderInfo);
    }

    /**
     * 测试平均值
     */
    @Test
    public void testAverage(){
        int paperId = 2005327;
        double average = getAverage(paperId);
        logger.info("平均分数为：{}",average);
    }

    /**
     * 测试曲线图
     */
    @Test
    public void testGraphInfo(){
        int paperId = 2005327;
        int perSize = 10;
        float minUtilScore = 0.001f;
        getLine(paperId,perSize,minUtilScore);
    }

    /**
     * 获取参考和平均分最高的前三名的省份
     */
    @Test
    public void testCountByPosition(){
        int paperId = 2005327;
        int total = 3;
        Map<Integer,Map<String,String>> positionMap = getPositionMap(paperId);
        Map<Long,Set<Integer>> numMap = Maps.newHashMap();
        Map<Double,Set<Integer>> scoreMap = Maps.newHashMap();
        for(Map.Entry<Integer,Map<String,String>> entry:positionMap.entrySet()){
            Integer positionId = entry.getKey();
            Map<String,String> mapData = entry.getValue();
            long count = Long.parseLong(mapData.get("count"));
            double average = Double.parseDouble(mapData.get("average"));
            if(numMap.get(count)==null){
                Set<Integer> set  = Sets.newHashSet();
                set.add(positionId);
                numMap.put(count,set);
            }else{
                Set<Integer> set  = numMap.get(count);
                set.add(positionId);
            }
            if(scoreMap.get(average)==null){
                Set<Integer> set  = Sets.newHashSet();
                set.add(positionId);
                scoreMap.put(average,set);
            }else{
                Set<Integer> set  = scoreMap.get(average);
                set.add(positionId);
            }
        }
        List<Long> numList = new ArrayList<>(numMap.keySet());
        List<Double> scoreList = new ArrayList<>(scoreMap.keySet());
        numList.sort((a,b)->((int)(b-a)));
        scoreList.sort((a,b)->((int)(b-a)));
        logger.info("numList={}",numList);
        logger.info("scoreList={}",scoreList);
        Set<Integer> numIds = getPreNumPosition(numList,numMap,total);
        Set<Integer> scoreIds = getPreScorePosition(scoreList,scoreMap,total);
        Map<Integer,Map<String,String>> numResult = Maps.newHashMap();
        for(Integer id:numIds){
            numResult.put(id,positionMap.get(id));
        }
        Map<Integer,Map<String,String>> scoreResult = Maps.newHashMap();
        for(Integer id:scoreIds){
            scoreResult.put(id,positionMap.get(id));
        }
        logger.info("参考人数排名前{}的职位情况:{}",total,numResult);
        logger.info("平均分排名前{}的职位情况:{}",total,scoreResult);
    }

    /**
     * 统计试题的错题率
     */
    @Test
    public void testWrongQuestion(){
        int paperId = 2005327;
        Map<Integer,MatchQuestionMeta> mapData = getQeustionMetas(paperId);
        Map<Integer,List<MatchQuestionMeta>> mapByPart = Maps.newHashMap();
        Map<Integer,String> moduleMap = Maps.newHashMap();
        for(Map.Entry<Integer,MatchQuestionMeta> entry:mapData.entrySet()){
            MatchQuestionMeta questionMeta = entry.getValue();
            int moduleId = questionMeta.getQuestion().getPoints().get(0);
            String moduleName = questionMeta.getQuestion().getPointsName().get(0);
            moduleMap.put(moduleId,moduleName);
            if(mapByPart.get(moduleId)==null){
                List<MatchQuestionMeta> list = Lists.newArrayList();
                list.add(questionMeta);
                mapByPart.put(moduleId,list);
            }else{
                List<MatchQuestionMeta> list = mapByPart.get(moduleId);
                list.add(questionMeta);
            }
        }
        for(Map.Entry<Integer,List<MatchQuestionMeta>> entry:mapByPart.entrySet()){
            List<MatchQuestionMeta> tempList = entry.getValue();
            sortMatchQuestionMetaByDesc(tempList);
            double percent = tempList.get(0).getPercent();
            List<MatchQuestionMeta> result = tempList.stream().filter(i->i.getPercent()==percent).collect(Collectors.toList());
            final List<Integer> ids = Lists.newArrayList();
            final List<Integer> seqs = Lists.newArrayList();
            result.forEach(i->ids.add(i.getQuestion().getId()));
            result.forEach(i->seqs.add(i.getQuestionSeq()+1));
            logger.info("科目{}的错题率最大为{}，题目是{},第{}题",moduleMap.get(entry.getKey()),percent,ids,seqs);
        }
    }

    private void sortMatchQuestionMetaByDesc(List<MatchQuestionMeta> tempList) {
        for(int i=1;i<tempList.size();i++){
            for(int j = 0;j<tempList.size()-i;j++){
                if(tempList.get(j).getPercent()<tempList.get(j+1).getPercent()){
                    MatchQuestionMeta tempMeta = tempList.get(j);
                    tempList.set(j,tempList.get(j+1));
                    tempList.set(j+1,tempMeta);
                }
            }
        }
    }

    public Map<Integer,MatchQuestionMeta> getQeustionMetas(int paperId){
        long start = System.currentTimeMillis();
        List<Integer> ids = practiceDao.findById(paperId).getQuestions();
        //获取所有的试题信息
        long s1 = System.currentTimeMillis();
//        logger.info("1阶段耗时{}",s1-start);
        List<Question> questions = practiceDao.findAllQuestion(ids);
        final Map<Integer,MatchQuestionMeta> questionMetaMap = Maps.newHashMap();
        questions.forEach(question->questionMetaMap.put(question.getId(),MatchQuestionMeta.builder().question((GenericQuestion)question ).build()));
        long s2 = System.currentTimeMillis();
//        logger.info("2阶段耗时{}",s2-s1);
        SetOperations setOperations = redisTemplate.opsForSet();
        String matchSubmitKey = MatchRedisKeys.getMatchSubmitPracticeIdSetKey(paperId);
        Set<String> setIds = setOperations.members(matchSubmitKey);
        Set<Long> practiceIds = Sets.newHashSet();
        setIds.forEach(id->practiceIds.add(Long.parseLong(id)));
        long s3 = System.currentTimeMillis();
//        logger.info("3阶段耗时{}",s3-s2);
        final List<Long> tempList = Lists.newArrayList();
        practiceIds.forEach(i->tempList.add(i));
        List<AnswerCardSub> answerCards = answerCardDao.findAllByIds(tempList,"corrects");
        long s4 = System.currentTimeMillis();
//        logger.info("4阶段耗时{}",s4-s3);
        Long[] wrongCount = new Long[ids.size()];
        Long[] finishedCount = new Long[ids.size()];
        boolean flag =false;
        for(AnswerCardSub answerCard:answerCards){
            int[] results = Arrays.stream(answerCard.getArrays()).mapToInt(t->Integer.parseInt(t.toString())).toArray();
            for(int i = 0;i<results.length;i++){
                if(!flag){
                    wrongCount[i]=0L;
                    finishedCount[i]=0L;
                }
                if(results[i]==2){
                    wrongCount[i]++;
                }
                if(results[i]!=0){
                    finishedCount[i]++;
                }
            }
            flag = true;
        }
        long s5 = System.currentTimeMillis();
//        logger.info("5阶段耗时{}",s5-s4);
        logger.info("错误次数为{}，完成题数{}",wrongCount,finishedCount);
        for(int i=0;i<ids.size();i++){
            double percent = new BigDecimal((float)wrongCount[i]/finishedCount[i]).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            int id = ids.get(i);
            MatchQuestionMeta tempMeta = questionMetaMap.get(id);
            tempMeta.setFinishCount(finishedCount[i]);
            tempMeta.setQuestionSeq(i);
            tempMeta.setWrongCount(wrongCount[i]);
            tempMeta.setRightCount(finishedCount[i]-wrongCount[i]);
            tempMeta.setPercent(percent);
        }
        long s6 = System.currentTimeMillis();
//        logger.info("6阶段耗时{}",s6-s5);
        return questionMetaMap;
    }
    private Set<Integer> getPreScorePosition(List<Double> scoreList, Map<Double, Set<Integer>> scoreMap, int total) {
        int count = 0;
        Set<Integer> result = Sets.newHashSet();
        for(Double num:scoreList){
            Set<Integer> set = scoreMap.get(num);
            result.addAll(set);
            count+=set.size();
            if(total<=count){
                break;
            }
        }
        return result;
    }

    private Set<Integer> getPreNumPosition(List<Long> numList, Map<Long, Set<Integer>> numMap, int total) {
        int count = 0;
        Set<Integer> result = Sets.newHashSet();
        for(Long num:numList){
            Set<Integer> set = numMap.get(num);
            result.addAll(set);
            count+=set.size();
            if(total<=count){
                break;
            }
        }
        return result;
    }

    /**
     * 按省份统计参考人数和平均值
     */
    private Map<Integer,Map<String,String>> getPositionMap(int paperId) {
        List<Position> positions = PositionConstants.getPositions();
        logger.info("positions info has {}",positions.size());
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Map<Integer,Map<String,String>> countInfo = Maps.newHashMap();
        for (Position position:positions){
            int positionId = position.getId();

            String zsetKey = MatchRedisKeys.getPositionPracticeIdSore(paperId,positionId);
            long size = zSetOperations.zCard(zsetKey);
            if(size>0){
                Map<String,String> tempMap = Maps.newHashMap();
                String positionName = position.getName();
                tempMap.put("name",positionName);
                tempMap.put("count",size+"");
                String valueKey = MatchRedisKeys.getPositionScoreSum(paperId,positionId);
                ValueOperations valueOperations = redisTemplate.opsForValue();
                Double sum = Double.parseDouble(valueOperations.get(valueKey).toString());
                tempMap.put("average",sum/size+"");
                countInfo.put(positionId,tempMap);
            }
        }
        return countInfo;
    }

    /**
     *
     * @param paperId
     * @param perSize
     * @param minUtilScore
     * @return
     */
    private Line getLine(int paperId, int perSize, float minUtilScore) {
        final String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        final ZSetOperations<String,String> zSetOperations = redisTemplate.opsForZSet();
        int maxScore  = findPaperScore(paperId);
        if(maxScore%perSize!=0){
            logger.error("分组统计不均匀");
            return null;
        }
        double size = (double) perSize/2;
        final TreeBasedTable<Double, String, Number> basedTable = TreeBasedTable.create();
        while(size<maxScore){
            long count = zSetOperations.count(paperPracticeIdSore,size-5+minUtilScore,size+5+minUtilScore);
            basedTable.put(size, "区段人数", count);
            logger.info("{}-{}分数区段的人数为{}",size-5,size+5,count);
            size+=perSize;
        }
        Line line = table2LineSeries(basedTable);
        logger.info("曲线图的对象为：{}",line);
        return line;
    }

    /**
     *
     * @param table
     * @return
     */
    private static final Line table2LineSeries(TreeBasedTable<Double,String,? extends Number> table){
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
                    .name((nodeScore-5)+"~"+(nodeScore+5))
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
     * 查询试卷的总分
     * @param paperId
     * @return
     */
    private int findPaperScore(int paperId) {
        Paper paper = mongoTemplate.findById(paperId, Paper.class);
        int score = 100;
        if(paper.getScore()!=0){
            score = paper.getScore();
        }
        logger.info("试卷分数为{}",score);
        return score;
    }

    /**
     *
     * @param paperId
     * @return
     */
    private double getAverage(int paperId) {
        final String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        final String paperScoreSum = PaperRedisKeys.getPaperScoreSum(paperId);
        final ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        final ZSetOperations<String,String> zSetOperations = redisTemplate.opsForZSet();
        Long total = zSetOperations.size(paperPracticeIdSore);//总记录数
        if (total == null || total == 0) {//不存在总记录数，正常来说应该不存在
            total = 1L;
        }
        final String scoreStr = valueOperations.get(paperScoreSum);//该试卷总得分
        double average = 60;//默认值
        if (scoreStr != null && total>0) {
            double allScore = Ints.tryParse(scoreStr);//答题卡所有分数和
            average = (double) (allScore/total);//平均分
        }
        return average;
    }

    /**
     *
     * @param userId
     * @return
     */
    private String findUserName(String userId) {
        String sql = "select uname from v_qbank_user where pukey = ?";
        Object[] params = {userId};
        String uname = "";
        try{
            uname = jdbcTemplate.queryForObject(sql,params,String.class);
        }catch (Exception e ){
            e.printStackTrace();
        }
        return uname;
    }

    /**
     *
     * @param setMap
     * @param max
     * @return
     */
    private List<MatchUserMeta> getMaxUserMeta(Map<Double, Set<String>> setMap, double max) {
        Set<String> set;
        if(max>0){
            set = setMap.get(max);
        }else{
            final  Set<String> tempSet = Sets.newHashSet();
            setMap.values().forEach(i->tempSet.addAll(i));
            set = tempSet;
        }
        Set<Long> longSet = Sets.newHashSet();
//        logger.info("分数最高的答题卡有{}个",set.size());
        Criteria criteria = Criteria.where("practiceId").in(longSet);
        List<MatchUserMeta> userMetas = mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
//        logger.info("分数最高的报名信息，共{}条，信息{}",userMetas.size(),userMetas);
        return userMetas;
    }

    /**
     *
     * @param size
     * @param paperId
     * @return
     */
    private Map<Double,Set<String>> getScoreMap(int size, int paperId) {
        //将前十名的分数分组统计，如果并列第一名的人数超过10个则统计第一名的全部信息
        int index = 0;
        Set<ZSetOperations.TypedTuple<String>> withScores = findMaxScoreInfo(index,size,paperId);
        //整合setMap所属的map
        Map<Double,Set<String>> setMap = Maps.newHashMap();
        if(CollectionUtils.isEmpty(withScores)){
            logger.error("无排名数据");
            return Maps.newHashMap();
        }
        for(ZSetOperations.TypedTuple<String> tuple:withScores){
            String value = tuple.getValue();
            double score = tuple.getScore();
            if(setMap.get(score)==null){
                Set<String> set = Sets.newHashSet();
                set.add(value);
                setMap.put(score,set);
            }else{
                Set<String> set = setMap.get(score);
                set.add(value);
            }
        }
        if(size>withScores.size()){
            logger.info("排名数据不超过{}个，取全部",withScores.size());
            return setMap;
        }
        index = index + size;
        while(true){
            withScores = findMaxScoreInfo(index,index+size,paperId);
            index++;
            if(CollectionUtils.isNotEmpty(withScores)){
                String value = new ArrayList<>(withScores).get(0).getValue();
                double score = new ArrayList<>(withScores).get(0).getScore();
                if(setMap.get(score)==null){
                    logger.info("第{}条记录分数发生变动",index);
                    break;
                }else{
                    Set<String> set = setMap.get(score);
                    set.add(value);
                }
            }else{
                logger.info("无第{}条分数记录",index);
                break;
            }
        }
        return setMap;
    }


    /**
     *
     * @param index
     * @param size
     * @param paperId
     * @return
     */
    public double getMaxScore(int index,int size,int paperId){
        Set<ZSetOperations.TypedTuple<String>> withScores = findMaxScoreInfo(index,size,paperId);
        double max = 0;
        if (CollectionUtils.isNotEmpty(withScores)) {
            max = new ArrayList<>(withScores).get(0).getScore();
            logger.info("最高分为：{}",max);
        }else{
            logger.error("无法获取排名信息");
        }
        return max;
    }

    /**
     *
     * @param index
     * @param size
     * @param paperId
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> findMaxScoreInfo(int index,int size,int paperId){
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String resultScoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        Set<ZSetOperations.TypedTuple<String>> withScores =
                zSetOperations.reverseRangeWithScores(resultScoreKey, index, size-1);
        String str = "";
        for(ZSetOperations.TypedTuple<String> tuple :withScores){
            str += tuple.getScore() +"-->"+tuple.getValue()+";";
        }
//        logger.info("获取前{}名的答题信息{}",withScores.size(),str);
        return withScores;
    }
    @Test
    public void testPractice(){
        String key = MatchRedisKeys.getMatchPracticeIdSetKey(2005327);

        Set<String> result = redisTemplate.opsForSet().members(key);
        for(String pid:result){
            Long id = Long.parseLong(pid);
            updatePractice(id);
        }

        logger.info("huang"+result.size());
    }
    @Test
    public void test(){
        updatePractice(1856125981222240256L);
    }
    private void updatePractice(Long id) {
        AnswerCard answerCard = answerCardDao.findById(id);
        if(answerCard.getType()==9){
            logger.info("需要修改");
            Query query = new Query(Criteria.where("id").is(id));
            Update update = new Update().set("type", 12);
            mongoTemplate.updateFirst(query, update, AnswerCard.class, "ztk_answer_card");
        }
    }

    @Test
    public void test1(){
         Criteria criteria = Criteria.where("_id").regex("[0-9]*_2005327");
         List<MatchUserMeta> userMetas = mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
         logger.info("cone:{}",userMetas.size());
         for(MatchUserMeta userMeta:userMetas){
             updateRedis(userMeta.getPracticeId(),userMeta.getPaperId(),userMeta.getPositionId());
         }
    }

    @Test
    public void minTest(){
//        updateRedis(1856127192031821825L,2005327,31);
        addRedis(1856127192031821825L,2005327,211,76);
    }
    private void addRedis(long id, int paperId, int positionId,double score){
        String paperKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        String matchKey = MatchRedisKeys.getPositionPracticeIdSore(paperId,positionId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        if(zSetOperations.score(paperKey,id+"")==null){
            addEncroll(paperId);
        }
        zSetOperations.add(paperKey,id+"",score);
        zSetOperations.add(matchKey,id+"",score);
    }

    private void addEncroll(int paperId) {
        String countKey = MatchRedisKeys.getTotalEnrollCountKey(paperId);
        redisTemplate.opsForValue().increment(countKey,1);
    }

    private void updateRedis(long id, int paperId, int positionId){
        String paperKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        String matchKey = MatchRedisKeys.getPositionPracticeIdSore(paperId,positionId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Double score = zSetOperations.score(paperKey,id+"");
        if(score!=null){
           zSetOperations.add(matchKey,id+"",score);
           logger.info("positionId={},paperId={},id={}",positionId,paperId,id);
        }
    }

    @Test
    public void testConvertToMatch(){
        //插叙所有的报名数据
        Criteria criteria = Criteria.where("_id").regex("[0-9]*_2005327");
        List<MatchUserMeta> userMetas = mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
        logger.info("cone:{}",userMetas.size());
        for(MatchUserMeta userMeta:userMetas){
            recoverUserMeta(userMeta);
        }
    }

    private void recoverUserMeta(MatchUserMeta userMeta) {
        if(userMeta==null){
            return;
        }
        long practiceId = userMeta.getPracticeId();
        if(practiceId>0){
            return;
        }
        long userId = userMeta.getUserId();
        int positionId = userMeta.getPositionId();
        double score = 0;
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("status").ne(AnswerCardStatus.DELETED);
        Query query = new Query(criteria);
        query.limit(5).with(new Sort(Sort.Direction.DESC, "_id"));
        List<AnswerCard> cards = mongoTemplate.find(query, AnswerCard.class);
        for(AnswerCard answerCard1:cards){
            if(answerCard1 instanceof StandardCard && userMeta.getPaperId() == ((StandardCard)answerCard1).getPaper().getId()){
                practiceId =  answerCard1.getId();
                score = answerCard1.getScore();
                break;
            }
        }
        if(practiceId<=0){
            logger.info("用户没有考试");
            return;
        }else{
            logger.info("用户专项模考转为模考大赛……");
        }
        updatePractice(practiceId);
        updateUserMeta(practiceId,userId,userMeta.getPaperId());
        addRedis(practiceId,userMeta.getPaperId(),positionId,score);
//        addEncrollCount(userMeta.getPaperId());

    }

    private void updateUserMeta(long practiceId, long userId, long paperId) {
        String id =  userId +"_"+ paperId;
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update().set("practiceId", practiceId);
        mongoTemplate.updateFirst(query, update, MatchUserMeta.class, "ztk_match_user_meta");
    }

}
