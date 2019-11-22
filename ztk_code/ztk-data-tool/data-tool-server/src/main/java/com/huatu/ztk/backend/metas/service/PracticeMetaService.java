package com.huatu.ztk.backend.metas.service;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeBasedTable;
import com.google.common.primitives.Ints;
import com.huatu.ztk.backend.constant.RedisKeyConstant;
import com.huatu.ztk.backend.metas.bean.AnswerCardSub;
import com.huatu.ztk.backend.metas.bean.MatchQuestionMeta;
import com.huatu.ztk.backend.metas.bean.MatchTimeBean;
import com.huatu.ztk.backend.metas.bean.MatchUserBean;
import com.huatu.ztk.backend.metas.constants.OldPositionConstants;
import com.huatu.ztk.backend.metas.dao.UserMetaDao;
import com.huatu.ztk.backend.paper.dao.AnswerCardDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.dao.PracticeDao;
import com.huatu.ztk.backend.paper.service.EstimateService;
import com.huatu.ztk.backend.user.dao.UserHuatuDao;
import com.huatu.ztk.backend.util.*;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.LineSeries;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PositionConstants;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.user.bean.UserDto;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2017\11\16 0016.
 */
@Service
public class PracticeMetaService {
    private final static Logger logger = LoggerFactory.getLogger(PracticeMetaService.class);
    @Autowired
    private PracticeDao practiceDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserMetaDao userMetaDao;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private AnswerCardDao answerCardDao;
    @Autowired
    private UserHuatuDao userHuatuDao;
    @Autowired
    private EstimateService estimateService;
    private final static int SPLIT_PAPERID = 2005327;   //模考大赛第十三季作为分界线，之后的所有考试地区使用新的position
    private final static int USER_ORDER_COUNT = 10;    //处理排名前count的成绩
    private final static int MIN_PER_SIZE = 10;        //曲线图的最小分组单位
    private final static float MIN_UTIL_SCORE = 0.001f;   //分数的最小单位
    private final static int ORDER_IN_POSITION_SIZE = 3;   //以省份为单位排前n的名次
    private final static long PER_FIND_MONGO_SIZE = 20000;   //每次批量查询的最大量级
    private final static int queryUserMetaSize = 20000;   //每次批量查询的最大量级
//    private final static String MATCH_COUNT_DATA_FILE_PATH = "E:\\Temp\\";
    private final static String MATCH_COUNT_DATA_FILE_PATH = "/ztk/logs/backend/";

    /**
     * 模考大赛交卷人数查询
     * @return
     */
    public Long getCountSubmit(int paperId){
        SetOperations setOperations = redisTemplate.opsForSet();
        String submitSetKey = MatchRedisKeys.getMatchSubmitPracticeIdSetKey(paperId);
        long size = setOperations.size(submitSetKey);
        logger.info("模考大赛交卷人数为：{}",size);
        return size;
    }
    /**
     * 查询模考最高分
     * 查询最高分所在省份
     * 查询模考前10的账号、分数、报考城市（职位）
     */
    public Map getMaxScoreInfo(int paperId){
        int size = 1;
        int index = 0;
        double max = getMaxScore(index,size,paperId);
        Map<Double,Set<String>> setMap = getScoreMap(USER_ORDER_COUNT,paperId);
        //获取最高分数所在省份
        List<MatchUserMeta> userMetas = getMaxUserMeta(setMap,max);
//        final Map<Long,String> positionMap = Maps.newHashMap();
//        userMetas.forEach(userMeta->positionMap.put(userMeta.getPracticeId(),userMeta.getPositionName()));
//        logger.info("最高分所在省份是{}",positionMap);
        List<String> positionNames = Lists.newArrayList();
        List<Integer> positionIds = Lists.newArrayList();
        userMetas.forEach(i->{positionIds.add(i.getPositionId());positionNames.add(i.getPositionName());});
        List<Map<String,String>> orderInfo = Lists.newArrayList();
        for(Double score:setMap.keySet()){
            List<MatchUserMeta> userMetaList = getMaxUserMeta(setMap,score);
            for(MatchUserMeta userMeta:userMetaList){
                Map<String,String> tempMap = Maps.newHashMap();
                String positionName = userMeta.getPositionName();
                tempMap.put("userId",userMeta.getUserId() + "");
//                tempMap.put("userName",findUserName(userId));
                tempMap.put("positionName",positionName);
                tempMap.put("score",score+"");
                orderInfo.add(tempMap);
            }
        }
        logger.info("前十名的信息实际有{}个，具体情况为：{}",orderInfo.size(),orderInfo);
        Map mapData = Maps.newHashMap();
        mapData.put("maxScore",max);
        mapData.put("maxScoreInPositionId",positionIds);
        mapData.put("maxScoreInPositionName",positionNames);
        mapData.put("orderScoreInfo",orderInfo);

        return mapData;
    }

    /**
     * 查询最高分和最高分所在地区
     * @return
     */
    public Map getMaxScoreAndPosition(int paperId){
        //最高分对应的答题卡
        Map<Double,Set<String>> setMap = getScoreMap(1,paperId);
        Map mapData = Maps.newHashMap();
        Double maxScore = setMap.keySet().stream().findFirst().get();
        mapData.put("maxScore",maxScore);
        List<MatchUserMeta> userMetaList = getMaxUserMeta(setMap,maxScore);
        mapData.put("maxScoreInPositionName",userMetaList.stream().map(i->i.getPositionName()).distinct().collect(Collectors.toList()));
        return mapData;
    }
    /**
     *  获得平均分数
     * @param paperId
     * @return
     */
    public double getAverage(int paperId) {
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
        logger.info("平均分数为：{}",average);
        return average;
    }
    /**
     *  获得曲线图
     * @param paperId
     * @return
     */
    public Line getLine(int paperId) {
        final String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        final ZSetOperations<String,String> zSetOperations = redisTemplate.opsForZSet();
        int maxScore  = paperDao.findPaperScore(paperId);
        if(maxScore%MIN_PER_SIZE!=0){
            logger.error("分组统计不均匀");
            return null;
        }
        double size = (double) MIN_PER_SIZE/2;
        final TreeBasedTable<Double, String, Number> basedTable = TreeBasedTable.create();
        while(size<maxScore){
            long count = zSetOperations.count(paperPracticeIdSore,size-5-MIN_UTIL_SCORE,size+5-MIN_UTIL_SCORE);
            basedTable.put(size, "区段人数", count);
            logger.info("{}-{}分数区段的人数为{}",size-5,size+5,count);
            size+=MIN_PER_SIZE;
        }
        Line line = table2LineSeries(basedTable);
        logger.info("曲线图的对象为：{}",line);
        return line;
    }
    /**
     * 获取参考和平均分最高的前三名的省份
     */
    public Map getCountByPosition(int paperId){
        //按省份统计参考人数和平均值
        Map<Integer,Map<String,String>> positionMap = getPositionMap(paperId);
        Map<Long,Set<Integer>> numMap = Maps.newHashMap();
        Map<Double,Set<Integer>> scoreMap = Maps.newHashMap();
        for(Map.Entry<Integer,Map<String,String>> entry:positionMap.entrySet()){
            //省份
            Integer positionId = entry.getKey();
            //人数和平均值
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
        Set<Integer> numIds = getPreNumPosition(numList,numMap, ORDER_IN_POSITION_SIZE);
        Set<Integer> scoreIds = getPreScorePosition(scoreList,scoreMap, ORDER_IN_POSITION_SIZE);
        Map<Integer,Map<String,String>> numResult = Maps.newHashMap();
        List<Map<String,String>> numResultList = Lists.newArrayList();
        for(Integer id:numIds){
            numResult.put(id,positionMap.get(id));
            Map<String,String> tempMap = positionMap.get(id);
            if(tempMap!=null){
                tempMap.put("id",id+"");
            }
            numResultList.add(tempMap);
        }
        Map<Integer,Map<String,String>> scoreResult = Maps.newHashMap();
        List<Map<String,String>> scoreResultList = Lists.newArrayList();
        for(Integer id:scoreIds){
            scoreResult.put(id,positionMap.get(id));
            Map<String,String> tempMap = positionMap.get(id);
            if(tempMap!=null){
                tempMap.put("id",id+"");
            }
            scoreResultList.add(tempMap);
        }
        logger.info("参考人数排名前{}的职位情况:{}", ORDER_IN_POSITION_SIZE,numResult);
        logger.info("平均分排名前{}的职位情况:{}", ORDER_IN_POSITION_SIZE,scoreResult);
        Map mapData = Maps.newHashMap();
        numResultList.sort((a,b)-> comparingMap(a,b,"count"));
        scoreResultList.sort((a,b)-> comparingMap(a,b,"average"));
//        mapData.put("enrollPositionSort",numResultList);
//        mapData.put("averagePositionSort",scoreResultList);
        mapData.put("enrollPositionSort",numResult);
        mapData.put("averagePositionSort",scoreResult);
        return mapData;
    }

    private int comparingMap(Map<String, String> a, Map<String, String> b, String key) {
        String aValue = a.get(key);
        String bValue = b.get(key);
        if(Double.parseDouble(aValue)-Double.parseDouble(bValue)>0){
            return -1;
        }else if(aValue.equals(bValue)){
            return 0;
        }
        return 1;
    }

    /**
     * 统计试题的错题率
     */
    public List<Map> getWrongQuestionMeta(int paperId){
        Map<Integer,MatchQuestionMeta> mapData = getQuestionMeta(paperId);
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
        List<Map> resultList = Lists.newArrayList();
        for(Map.Entry<Integer,List<MatchQuestionMeta>> entry:mapByPart.entrySet()){
            List<MatchQuestionMeta> tempList = entry.getValue();
            sortMatchQuestionMetaByDesc(tempList);
            double percent = tempList.get(0).getPercent();
            List<MatchQuestionMeta> result = tempList.stream().filter(i->i.getPercent()==percent).collect(Collectors.toList());
            final List<Integer> ids = Lists.newArrayList();
            final List<Integer> seqs = Lists.newArrayList();
            result.forEach(i->ids.add(i.getQuestion().getId()));
            result.forEach(i->seqs.add(i.getQuestionSeq()+1));
            logger.info("科目{}的错题率最大为{}，题目有{}",entry.getKey(),percent,ids);
            Map tempMap = Maps.newHashMap();
            tempMap.put("subject",entry.getKey());
            tempMap.put("subjectName",moduleMap.get(entry.getKey()));
            tempMap.put("percent",percent);
            tempMap.put("questions",ids);
            tempMap.put("questionOrders",seqs);
            tempMap.put("detail",result);
            resultList.add(tempMap);
        }
        return resultList;
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
    public Map<Integer,MatchQuestionMeta> getQuestionMeta(int paperId){
        long start = System.currentTimeMillis();
        List<Integer> ids = practiceDao.findById(paperId).getQuestions();
        //获取所有的试题信息
        long s1 = System.currentTimeMillis();
        logger.info("1阶段耗时{}",s1-start);
        List<Question> questions = practiceDao.findAllQuestion(ids);
        final Map<Integer,MatchQuestionMeta> questionMetaMap = Maps.newHashMap();
        questions.forEach(question->questionMetaMap.put(question.getId(),MatchQuestionMeta.builder().question((GenericQuestion)question ).build()));
        long s2 = System.currentTimeMillis();
        logger.info("2阶段耗时{}",s2-s1);
        SetOperations setOperations = redisTemplate.opsForSet();
        String matchSubmitKey = MatchRedisKeys.getMatchSubmitPracticeIdSetKey(paperId);
        Set<String> setIds = setOperations.members(matchSubmitKey);
        List<Long> practiceIds = Lists.newArrayList();
        setIds.forEach(id->practiceIds.add(Long.parseLong(id)));
        long s3 = System.currentTimeMillis();
        logger.info("3阶段耗时{}",s3-s2);
        long size = practiceIds.size();
        long index =0;
        Long[] wrongCount = new Long[ids.size()];
        Long[] finishedCount = new Long[ids.size()];
        List<Map<String,Long>> choiceTimes = Lists.newArrayListWithCapacity(ids.size());
        for (int i=0;i<wrongCount.length;i++) {
            wrongCount[i] = 0L;
            finishedCount[i] = 0L;
        }
        while(true){
            long end = index + PER_FIND_MONGO_SIZE;
            if(size<end){
                end = size;
            }
            List<Long> tempList = practiceIds.subList((int)index,(int)end);
            List<AnswerCardSub> answerCards = answerCardDao.findAllByIds(tempList,"corrects");
            for(AnswerCardSub answerCard:answerCards){
                int[] results = Arrays.stream(answerCard.getArrays()).mapToInt(k->Integer.parseInt(String.valueOf(k))).toArray();
                if(results.length!=ids.size()){
                    continue;
                }
                for(int i = 0;i<results.length;i++){
                    if(results[i]==2){
                        wrongCount[i]++;
                    }
                    if(results[i]!=0){
                        finishedCount[i]++;
                    }
                }
            }
            long s4 = System.currentTimeMillis();
            logger.info("查询题目正确与否耗时{},size={},end={}",s4-s3,size,end);
            List<AnswerCardSub> answers = answerCardDao.findAllByIds(tempList,"answers");
            for(AnswerCardSub answer:answers){
                List<String> submitAnswer = Arrays.stream(answer.getArrays()).map(k->String.valueOf(k)).collect(Collectors.toList());
                for (int a = 0;a<submitAnswer.size();a++) {
                    Map<String,Long> choiceTimeMap = null;
                    if(choiceTimes.size()==a || choiceTimes.get(a)==null){
                        choiceTimeMap = Maps.newHashMap();
                        choiceTimes.add(choiceTimeMap);
                    }else{
                        choiceTimeMap = choiceTimes.get(a);
                    }
                    String choiceTemp = submitAnswer.get(a).equals("0")?"":(char)(Integer.parseInt(submitAnswer.get(a))-1+'A')+"";
                    if(StringUtils.isNotBlank(choiceTemp)){
                        long submitCount = choiceTimeMap.getOrDefault(choiceTemp,0L);
                        submitCount++;
                        choiceTimeMap.put(choiceTemp,submitCount);
                    }
                }
            }
            long s5 = System.currentTimeMillis();
            logger.info("查询用户答题数据耗时{},size={},end={}",s5-s4,size,end);
            if(end==size){
                break;
            }
            index = end +1;
        }
        long s5 = System.currentTimeMillis();
        logger.info("5阶段耗时{}",s5-s3);
        logger.info("错误次数为{}，完成题数{}",wrongCount,finishedCount);
        for(int i=0;i<ids.size();i++){
            float fPercent = 0;
            if(finishedCount[i].intValue()>0){
                fPercent = (float)wrongCount[i]/finishedCount[i];
            }
            double percent = new BigDecimal(fPercent).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            int id = ids.get(i);
            MatchQuestionMeta tempMeta = questionMetaMap.get(id);
            tempMeta.setFinishCount(finishedCount[i]);
            tempMeta.setQuestionSeq(i);
            tempMeta.setWrongCount(wrongCount[i]);
            if (i < choiceTimes.size() - 1){
                tempMeta.setChoiceTime(choiceTimes.get(i));
            }
            tempMeta.setRightCount(finishedCount[i]-wrongCount[i]);
            tempMeta.setPercent(percent);
        }
        long s6 = System.currentTimeMillis();
        logger.info("6阶段耗时{}",s6-s5);
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
    public Map<Integer,Map<String,String>> getPositionMap(int paperId) {
        List<Position> positions = Lists.newArrayList();
        if(SPLIT_PAPERID>=paperId){
            positions = OldPositionConstants.getPositions();
        }else{
            positions = PositionConstants.getPositions();
        }
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
     *  生成曲线对象
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
     *
     * @param userId
     * @return
     */
    private String findUserName(String userId) {
        String sql = "select uname from v_qbank_user where pukey = ?";
        Object[] params = {userId};
        String uname = "";
        try{
//            uname = jdbcTemplate.queryForObject(sql,params,String.class);
        }catch (Exception e ){
            e.printStackTrace();
        }
        return uname;
    }

    /**
     * 获取摸一个分值的所有用户报名信息
     * @param setMap
     * @param max
     * @return
     */
    private List<MatchUserMeta> getMaxUserMeta(Map<Double, Set<String>> setMap, double max) {
        logger.info("setMap = {}",setMap);
        Set<String> set;
        if(max>0){
            set = setMap.get(max);
        }else{
            final  Set<String> tempSet = Sets.newHashSet();
            setMap.values().forEach(i->tempSet.addAll(i));
            set = tempSet;
        }
        Set<Long> longSet = Sets.newHashSet();
        set.forEach(i->longSet.add(Long.parseLong(i)));
        logger.info("分数最高的答题卡有{}个",set.size());
        List<MatchUserMeta> userMetas = userMetaDao.getUserMetas(longSet);
        if (CollectionUtils.isEmpty(userMetas)){
            //如果当前是非模考大赛，此处获取数据失败
            ArrayList<Long> list = new ArrayList<>(longSet);
            List<AnswerCard> answerCards = answerCardDao.findByIds(list);
            List<MatchUserMeta> collect = answerCards.stream()
                    .map(answerCard -> {
                        StandardCard standardCard = (StandardCard) answerCard;
                        return MatchUserMeta.builder()
                                .paperId(standardCard.getPaper().getId())
                                .practiceId(standardCard.getId())
                                .userId(standardCard.getUserId())
                                .build();

                    })
                    .collect(Collectors.toList());
            userMetas.addAll(collect);
        }
        logger.info("分数最高的报名信息，共{}条，信息{}",userMetas.size(),userMetas);
        return userMetas;
    }
    /**
     * 分数前size名的练习id（答题卡）按分数分组
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
        //查看size后的答题卡分数是否与size内的最后一名同分
        double minScore = -1;
        for(ZSetOperations.TypedTuple<String> tuple:withScores){
            String value = tuple.getValue();
            double score = tuple.getScore();
            if(minScore<0||minScore>score){
                minScore = score;
            }
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
        Set<String> extendIds =  findMinScoreInfo(minScore,paperId);
        if(CollectionUtils.isEmpty(extendIds)){
            return setMap;
        }else{
            Set<String> set = setMap.get(minScore);
            set.addAll(extendIds);
        }
        logger.info("排名数据 = {}",setMap);
        return setMap;
    }
    /**
     * 获取最高分
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
     * 获取分数排名index+1至index+size的答题卡和分数
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
        logger.info("获取前{}名的答题信息{}",withScores.size(),str);
        return withScores;
    }
    /**
     * 获取某一个分数的所有成员
     * @param minScore
     * @param paperId
     * @return
     */
    public Set<String> findMinScoreInfo(double minScore,int paperId){
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String resultScoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        Set<String> setIds =
                zSetOperations.reverseRangeByScore(resultScoreKey, minScore,minScore);
        logger.info("获取分数为{}的答题信息共{}个，具体信息：{}",minScore,setIds.size(),setIds);
        return setIds;
    }

    /**
     * 将模考大赛的统计信息存入大赛数据库中
     * @param paperId
     * @return
     * @throws BizException
     */
    public int saveMatchUserTODB(int paperId) throws BizException{
        long start1 = System.currentTimeMillis();
        int size = userMetaDao.findLogByPaperId(paperId);
        if(size>0){
            throw new BizException(ErrorResult.create(100001,"该试卷统计信息已被持久化"));
        }
        Match match = paperDao.findMatchById(paperId);
        long index = 0L;
        int total = 0;
        while(true){
            List<MatchUserMeta> userMetaList = userMetaDao.getUserMetasByPaperId(paperId,index,queryUserMetaSize);
            if(CollectionUtils.isEmpty(userMetaList)){
                break;
            }
            long userStart = System.currentTimeMillis();
            List<Long> userIds = userMetaList.stream().map(i->i.getUserId()).collect(Collectors.toList());
            Map<Long,String> userMap = Maps.newHashMap();
            userMetaDao.findUserNameByIDs(userIds).stream().forEach(i->userMap.putAll(i));
            long userEnd = System.currentTimeMillis();
            logger.info("find userInfo use time :{}",userEnd-userStart);
            List<Long> practiceIds = userMetaList.stream().map(i->i.getPracticeId()).collect(Collectors.toList());
            practiceIds.removeIf(i->i==-1);
            Map<Long,AnswerCard> cardMap = Maps.newHashMap();
            Paper paper = paperDao.findById(paperId);

            if(paper==null||paper.getModules()==null){
                throw new BizException(ErrorResult.create(100001,"试卷信息有误"));
            }
            List<Module> modules = paper.getModules();
            Map<Long,AnswerCardSub> timeMap = Maps.newHashMap();
            if(CollectionUtils.isNotEmpty(practiceIds)){
                List<AnswerCard> cards = answerCardDao.findByIdsV2(practiceIds);
                cardMap.putAll(cards.stream().collect(Collectors.toMap(i->i.getId(),i->i)));
                List<AnswerCardSub> answerCards = answerCardDao.findAllByIds(practiceIds,"times");
                timeMap.putAll(answerCards.stream().collect(Collectors.toMap(i->i.getId(),i->i)));
            }

            long cardEnd = System.currentTimeMillis();
            logger.info("find card use time :{}",cardEnd-userEnd);
            final LinkedList<MatchUserBean> matchUserBeans = Lists.newLinkedList();
            final LinkedList<MatchTimeBean> matchTimeBeans = Lists.newLinkedList();
            total += userMetaList.size();
            long start = System.currentTimeMillis();
            userMetaList.stream().forEach(matchUserMeta -> {
                setMatchUserInfo(matchUserMeta,cardMap,match,userMap,matchUserBeans);
                setMatchTimeInfo(matchUserMeta,timeMap,modules,matchTimeBeans);
            });
            long end = System.currentTimeMillis();
            logger.info("add matchUserBeans use time:{}",end-start);
            try {
//                userMetaDao.insertUserMatches1(matchUserBeans);
                userMetaDao.insertUserMatches2(matchUserBeans.stream().collect(Collectors.toList()));
                userMetaDao.insertUserMatchTime(matchTimeBeans.stream().collect(Collectors.toList()));
//                userMetaDao.insertUserMatches(matchUserBeans);
            } catch (Exception e) {
                logger.info("sql state execute failed,index = {},size = {}",matchUserBeans.get(0).getUserId(),queryUserMetaSize);
                throw e;
            }

            if(userMetaList.size()<queryUserMetaSize){
                break;
            }
            index = userMetaList.get(userMetaList.size()-1).getUserId();
        }
        logger.info("插入数据库完事！！！");
        long end1 = System.currentTimeMillis();
        logger.info("收集数据耗时：{}",(end1-start1)/1000);
        postByIndex();
        return total;
    }

    private void setMatchTimeInfo(MatchUserMeta matchUserMeta, Map<Long, AnswerCardSub> timeMap, List<Module> modules, LinkedList<MatchTimeBean> matchTimeBeans) {
        long practiceId = matchUserMeta.getPracticeId();
        if(practiceId==-1){
            return;
        }
        AnswerCardSub answerCardSub = timeMap.get(practiceId);
        if(answerCardSub==null){
            return;
        }
        int i = 0 ;
        int[] array = Arrays.stream(answerCardSub.getArrays()).mapToInt(k->Integer.parseInt(String.valueOf(k))).toArray();
        if(array==null||array.length<modules.stream().mapToInt(module->module.getQcount()).sum()){
            return;
        }
        for(Module module:modules){
            int j = i+ module.getQcount();
            int time = sumArray(array,i,j);
            if(time!=0){
                matchTimeBeans.addLast(MatchTimeBean.builder().paperId(matchUserMeta.getPaperId()).questionNum(module.getQcount()).time(time).moduleId(module.getCategory()).moduleName(module.getName()).userId(matchUserMeta.getUserId()).build());
            }
            i = j;
        }
    }

    private int sumArray(int[] array, int i, int j) {
        int total = 0;
        for(int k = i;k<j;k++){
            total += array[k];
        }
        return total;
    }

    /**
     * 模考大赛用户数据入库
     * @param matchUserMeta
     * @param cardMap
     * @param match
     * @param userMap
     * @param matchUserBeans
     */
    private void setMatchUserInfo(MatchUserMeta matchUserMeta, Map<Long, AnswerCard> cardMap, Match match, Map<Long, String> userMap, LinkedList<MatchUserBean> matchUserBeans) {
        MatchUserBean matchUserBean = MatchUserBean.builder().paperId(match.getPaperId()).paperName(match.getName()).startTime(match.getStartTime()).endTime(match.getEndTime()).subjectId(match.getSubject()).
                areaId(matchUserMeta.getPositionId()).areaName(matchUserMeta.getPositionName()).enrolled(1).userId(matchUserMeta.getUserId()).practiceId(matchUserMeta.getPracticeId()).
                userName(userMap.get(matchUserMeta.getUserId())).build();
        if(matchUserMeta.getPracticeId()==-1){
            matchUserBeans.addLast(matchUserBean);
            return;
        }
        AnswerCard card = cardMap.get(matchUserMeta.getPracticeId());
        if(card==null){
            matchUserBeans.addLast(matchUserBean);
            return;
        }
        if(card instanceof StandardCard){
            matchUserBean.setSubmitTime(card.getCreateTime());
            matchUserBean.setLooked(1);
        }
        matchUserBean.setScore(card.getScore());
        if(card.getLastIndex()>0){
            matchUserBean.setJoined(1);
        }
        matchUserBeans.addLast(matchUserBean);
    }

    public void postByIndex(){

        long index = 0L;
        int size = 500;
        while(index>=0){
            Map map = (Map) RestTemplateUtil.getIndex(1);
            logger.info(JsonUtil.toJson(map));
            if(map!=null&&map.get("index")!=null&&map.get("long")!=null){
                index = Long.parseLong(String.valueOf(map.get("index")));
                size = Integer.parseInt(String.valueOf(map.get("long")));
            }
            index = postLogs(index,size);

        }


    }

    private long postLogs(long index, int size) {
        List<Map<String,Object>> matchUserBeans =  userMetaDao.findLogsByIndex1(index,size);
//        List<MatchUserBean> matchUserBeans =  userMetaDao.findLogsByIndex(index,size);
        if(CollectionUtils.isEmpty(matchUserBeans)){
            return -1;
        }
//        //发送失败则直接跳出
//        if(!RestTemplateUtil.postLog(matchUserBeans)){
//            return -1;
//        }
        if(!RestTemplateUtil.postLogs(matchUserBeans,RestTemplateUtil.STORM_URL)){
            return -1;
        }
        //发送成功，但是数量不足size个，也需要跳出
        if(matchUserBeans.size()<size){
            return -1;
        }
        return Integer.parseInt(String.valueOf(matchUserBeans.get(matchUserBeans.size()-1).get("id")));
    }

    public void parseStatements(Map mapData, int paperId) {
        StringBuilder sb = new StringBuilder("");
        //交卷人数
        Object submitCount  = mapData.get("submitCount");
        sb.append("交卷人数：").append(submitCount).append("\r\n");
        Object maxScore = mapData.get("maxScore");
        sb.append("最高分：").append(maxScore).append("\r\n");
        Object average = mapData.get("average");
        sb.append("平均分：").append(average).append("\r\n");
        Line line =(Line)mapData.get("line");
        sb.append("分数分布情况：\r\n");
        List<LineSeries> series = line.getSeries();
        for (LineSeries lineSeries : series) {
            sb.append("分数在").append(lineSeries.getName()).append("区间的人数有：").append(lineSeries.getData().get(0)).append("\r\n");
        }
        List<String> maxScoreInPositionName = (List)mapData.get("maxScoreInPositionName");
        List<Integer> maxScoreInPositionId = (List)mapData.get("maxScoreInPositionId");
        sb.append("最高分所在的省份(名称+id)有：");
        for (int i=0;i< maxScoreInPositionName.size();i++) {
            sb.append(maxScoreInPositionName.get(i)).append("(").append(maxScoreInPositionId.get(i)).append("),");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("\r\n");
        Map<Integer,Map<String,String>> numResult = (Map)mapData.get("enrollPositionSort");
        List<Map<String,String>> numPositionList = numResult.values().stream().collect(Collectors.toList());
        numPositionList.sort((a,b)->(Integer.parseInt(b.get("count"))-Integer.parseInt(a.get("count"))));
        sb.append("参加人数排名前三的省份有：");
        for(Map<String,String> position:numPositionList){
            sb.append(position.get("name")).append("(").append(position.get("count")).append("人) ");
        }
        sb.append("\r\n");
        sb.append("平均分排名前三的省份有：");
        Map<Integer,Map<String,String>> averagePositionSort = (Map)mapData.get("averagePositionSort");
        List<Map<String,String>> averagePositionList = averagePositionSort.values().stream().collect(Collectors.toList());
        averagePositionList.sort((a,b)->(Integer.parseInt(b.get("count"))-Integer.parseInt(a.get("count"))));
        for(Map<String,String> position:averagePositionList){
            sb.append(position.get("name")).append("(").append(position.get("average")).append(") ");
        }
        sb.append("\r\n");
        sb.append("前十名学员的情况如下：\r\n");
        List<Map<String,String>> orderInfo = (List)mapData.get("orderScoreInfo");
        Map<Long,String> userMap = Maps.newHashMap();
        long[] ids = orderInfo.stream().mapToLong(info->Long.parseLong(String.valueOf(info.get("userId")))).toArray();
        List<Long> userIds = Lists.newArrayList();
        for (long id : ids) {
            userIds.add(id);
        }
        userMetaDao.findUserNameByIDs(userIds).stream().forEach(i->userMap.putAll(i));
        orderInfo.sort((a,b)->(int)(Double.parseDouble(b.get("score"))-Double.parseDouble(a.get("score"))));
        for(Map<String,String> tempMap:orderInfo){
            sb.append("用户名：").append(userMap.get(Long.parseLong(tempMap.get("userId")))).append("|分数：").append(tempMap.get("score")).append("|省份：").append(tempMap.get("positionName")).append("\r\n");
        }

        List<Map> wrongList = (List<Map>) mapData.get("wrongList");
        sb.append("每个模块下错题率最大的试题有：\r\n");
        for (Map map : wrongList) {
            sb.append("---------------------------------------------").append("\r\n");
            sb.append(map.get("subjectName")).append("|错题率:").append(map.get("percent")).append("\r\n");
            List<MatchQuestionMeta> list = (List)map.get("detail");
            for(MatchQuestionMeta meta:list){
                sb.append("考点类型:").append(meta.getQuestion().getPointsName().get(2)).append("\r\n");
                sb.append(meta.getQuestionSeq()+1).append("、").append(meta.getQuestion().getStem().trim()).append("\r\n");
                int i= 0;
                for(String choice:meta.getQuestion().getChoices()){
                    sb.append("选项").append((char)('A'+i)).append(":").append(choice.trim()).append("\r\n");
                    i++;
                }
                sb.append("解析:").append(meta.getQuestion().getAnalysis().trim()).append("\r\n");
                sb.append("答案:").append(convertAnswer(meta.getQuestion().getAnswer()+"")).append("\r\n");
            }

        }
        String path = MATCH_COUNT_DATA_FILE_PATH + paperId +".txt";
        File file = new File(path);
        try {
            FileUtils.write(file,sb.toString());
            //转换发送数据
            Paper paper = paperDao.findById(paperId);
            MailUtil.sendMail("模考数据信息统计-" + paper.getName() + "-" + paper.getId(),"模考数据统计txt",path,"attach"+paperId);
            //logger.info("写入文件{}中",file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.print(sb.toString());
    }
    private String convertAnswer(String answer){
        String answers=answer.replace("1","A")
                .replace("2","B")
                .replace("3","C")
                .replace("4","D")
                .replace("5","E")
                .replace("6","F")
                .replace("7","G")
                .replace("8","H");
        return answers;
    }

    /**
     * 返回参考用户id集合
     * @param paperId
     * @return
     */
    public Set<Long> countEstimatePaper(int paperId) {
        String resultKey = RedisKeyConstant.getEstimateUserIdKey(paperId);
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        String result = valueOperations.get(resultKey);
        if(StringUtils.isNotEmpty(result)){
            Set<Long> userIds = Arrays.stream(result.split(",")).map(i -> Long.parseLong(i)).collect(Collectors.toSet());
            if(userIds.size()>100){
                return userIds;
            }
        }
        String key = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set<String> set = zSetOperations.range(key,0,Long.MAX_VALUE);
        if(CollectionUtils.isEmpty(set)){
            logger.info("没有人参考");
        }
        logger.info("参考答题卡数量有{}",set.size());
        List<Long> practiceIds = set.stream().map(Long::new).collect(Collectors.toList());
        DBObject query1 = new BasicDBObject(); //setup the query criteria 设置查询条件
        query1.put("_id", new BasicDBObject("$in", practiceIds));
        DBObject fields = new BasicDBObject(); //only get the needed fields. 设置需要获取哪些域
        fields.put("_id", 1);
        fields.put("userId", 1);
        DBCursor dbCursor =mongoTemplate.getCollection("ztk_answer_card").find(query1,fields);
        Set<Long> userIds= Sets.newHashSet();
        while (dbCursor.hasNext()){
            DBObject object=dbCursor.next();
            if(StringUtils.isNumeric(object.get("userId").toString())){
                userIds.add(Long.parseLong(object.get("userId").toString()));
            }
        }
        logger.info("查询到有答题卡的用户有{}个，有{}",userIds.size(),userIds);
        result = String.join(",",userIds.stream().map(String::valueOf).collect(Collectors.toList()));
        valueOperations.set(resultKey,result);
        return userIds;
    }

    public Set<Long> getMatchAllUserInfoByPaperId(int paperId){
        Criteria criteria = Criteria.where("paperId").is(paperId);
        Query query = new Query(criteria);
        List<MatchUserMeta> userMetas = mongoTemplate.find(query, MatchUserMeta.class);
        return userMetas.stream()
                .map(MatchUserMeta::getUserId)
                .distinct()
                .collect(Collectors.toSet());
    }


    public Double getMinScore(int paperId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String resultScoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        Set<ZSetOperations.TypedTuple<String>> withScores =
                zSetOperations.rangeWithScores(resultScoreKey, 0, 1);
        Double minScore = withScores.stream().map(i->i.getScore()).sorted().findFirst().get();
        return minScore;
    }

    /**
     * 地区平均分排名
     * @param paperId
     * @return
     */
    public List<Map> getAveragePositionSort(int paperId) {
        return getPositionMap(paperId).values().stream().map(i->{
            Map temp = Maps.newHashMap();
            double average = Double.parseDouble(i.get("average"));
            average = (double)Math.round(average*100)/100;
            temp.put("average",average);
            temp.put("name",i.get("name"));
            return temp;
        }).sorted(Comparator.comparingDouble(a -> 0 - new Double(a.get("average").toString())))
        .collect(Collectors.toList());
    }
    /**
     * 地区参赛人数排名
     * @param paperId
     * @return
     */
    public List<Map> getEnrollPositionSort(int paperId) {
        List<Map> sortList = getPositionMap(paperId).values().stream()
                .sorted(Comparator.comparingDouble(a -> 0 - new Double(a.get("count").toString())))
                .collect(Collectors.toList());
        Integer total = 0;
        for (Map stringStringMap : sortList) {
            total += Integer.parseInt(stringStringMap.get("count").toString());
        }
        final int finalTotal = total;
        int length = 10;
        List<Map> result = sortList.subList(0,length);
        while(sortList.size()>length){
            if(sortList.get(length).get("count").equals(result.get(length-1).get("count"))){
                result.add(sortList.get(length));
            }else{
                break;
            }
            length++;
        }
        if(sortList.size()>length){
            Map map = Maps.newHashMap();
            map.put("name","其他");
            int count = 0;
            for (Map temp : sortList.subList(length, sortList.size())) {
                count += Integer.parseInt(temp.get("count").toString());
            }
            logger.info("其他省数量：{}，length={}",sortList.subList(length, sortList.size()),length);
            map.put("count",count);
            result.add(map);
        }
        return result.stream().map(i->{
            Map temp = Maps.newHashMap();
            temp.put("count",i.get("count"));
            temp.put("name",i.get("name"));
            double percent = Double.parseDouble(i.get("count").toString())/finalTotal;
            percent = (double)Math.round(percent*10000)/10000;
            temp.put("percent",percent);
            return temp;
        }).collect(Collectors.toList());
    }

    /**
     * 根据分数区间查询人数
     * @param paperId
     * @param min
     * @param max
     * @return
     */
    public Integer getCountByScore(int paperId, int min, int max) {
        final String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        final ZSetOperations<String,String> zSetOperations = redisTemplate.opsForZSet();
        Long count = zSetOperations.count(paperPracticeIdSore,min,max+MIN_UTIL_SCORE);
        if(count==null){
            return 0;
        }
        return count.intValue();
    }

    /**
     * 用户模考数据查询
     * @param paperId
     * @param skip
     * @param size
     * @return
     */
    public List<Map> getUserInfoByPage(int paperId, int skip, int size) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String resultScoreKey = PaperRedisKeys.getPaperPracticeIdSore(paperId);
        Set<ZSetOperations.TypedTuple<String>> withScores =
                zSetOperations.reverseRangeWithScores(resultScoreKey, skip, skip+size-1);
        List<Long> practices = withScores.stream().map(i->Long.parseLong(i.getValue())).collect(Collectors.toList());
        List<AnswerCard> answerCards = answerCardDao.findByIds(practices);
        List<MatchUserMeta> matchUserMetas = userMetaDao.getUserMetas(practices);
        Map<Long,Map> userMateMap = matchUserMetas.stream().collect(Collectors.toMap(i->i.getUserId(),i->{
            Map map = Maps.newHashMap();
            map.put("positionId",i.getPositionId());
            map.put("positionName",i.getPositionName());
            return map;
        }));
        List<Map> userList = userMetaDao.findUserInfoByIDs(answerCards.stream().map(i->i.getUserId()).collect(Collectors.toList()));
        Map<String,Map> userMap = userList.stream().collect(Collectors.toMap(i->String.valueOf(i.get("userId")),i->i));
        return answerCards.stream().sorted((a,b)->(int)((b.getScore()-a.getScore()))).map(i->{
            Map temp = Maps.newHashMap();
            temp.putAll(userMap.get(i.getUserId()+""));
            temp.putAll(userMateMap.get(i.getUserId()));
            temp.put("score",i.getScore());
            double percent = (double) (Arrays.stream(i.getCorrects()).filter(s->s==1).count())/Arrays.stream(i.getAnswers()).filter(s->!s.equals("0")).count();
            percent = (double)Math.round(percent*10000)/10000;
            temp.put("percent",percent);
            temp.put("finishCount", Arrays.stream(i.getAnswers()).filter(s->!s.equals("0")).count()+"/"+i.getAnswers().length);
            temp.put("time",i.getExpendTime());
            return temp;
        }).collect(Collectors.toList());
    }

    /**
     * 分页查询试题统计信息
     * @param paperId
     * @param moduleId
     * @param page
     * @param size
     * @return
     */
    public Object findQuestionInfo(int paperId, int moduleId, int page, int size) {
        Map resultMap = Maps.newHashMap();
        Map<Integer,MatchQuestionMeta> mapData = getQuestionMeta(paperId);
        List<MatchQuestionMeta> result = mapData.values().stream().collect(Collectors.toList());
        Map<Integer,String> moduleMap = Maps.newHashMap();
        result.stream().map(i->i.getQuestion()).forEach(i->{
            moduleMap.put(i.getPoints().get(0),i.getPointsName().get(0));
        });
        List<Map> modules =moduleMap
        .entrySet().stream().map(i->{
            Map temp = Maps.newHashMap();
            temp.put("key",i.getKey());
            temp.put("value",i.getValue());
            return temp;
        }).collect(Collectors.toList());
        resultMap.put("module",modules);
        if(moduleId!=-1){
            result.removeIf(i->i.getQuestion().getPoints().get(0).intValue()!=moduleId);
        }
        result.sort(Comparator.comparingInt(MatchQuestionMeta::getQuestionSeq));
        int skip = (page-1)*size;
        if(result.size()<=skip){
            return Lists.newArrayList();
        }
        int end = page*size>result.size()?result.size():page*size;
        List<Map> list =  result.subList(skip,end).stream().map(i->{
            Map temp = Maps.newHashMap();
            temp.putAll(JsonUtil.toMap(JsonUtil.toJson(i)));
            long errorCount = i.getChoiceTime().values().stream().max(Comparator.comparing(Long::intValue)).get();
            double errorPercent = new Double(errorCount)/i.getFinishCount();
            errorPercent = (double)Math.round(errorPercent*10000)/10000;
            double rightPercent = new Double(i.getRightCount())/i.getFinishCount();
            rightPercent = (double)Math.round(rightPercent*10000)/10000;
            temp.put("errorPercent",errorPercent);
            temp.put("rightPercent",rightPercent);
            temp.put("errorChoice",i.getChoiceTime().entrySet().stream().filter(k->k.getValue().longValue()==errorCount)
                    .map(k->k.getKey()).findFirst().get());
            return temp;
        }).collect(Collectors.toList());
        resultMap.put("total",result.size());
        resultMap.put("list",list);
        resultMap.put("next",page*size<result.size());
        return resultMap;
    }

    /**
     * 转化格式
     * @param line
     * @return
     */
    public Object parseLine(Line line) {
        List<String> columns = Lists.newArrayList("分段","人数");
        List<Map> rows = Lists.newArrayList();
        line.getSeries().stream().forEach(i->{
            Map map = Maps.newHashMap();
            map.put("分段",i.getName());
            map.put("人数",i.getData().get(0));
            rows.add(map);
        });
        Map mapData = Maps.newHashMap();
        mapData.put("columns",columns);
        mapData.put("rows",rows);
        return mapData;
    }

    /**
     * 转化格式
     * @param scoreResult
     * @return
     */
    public Object parseScoreResult(List<Map> scoreResult) {
        List<String> columns = Lists.newArrayList("省份","分数");
        List<Map> rows = Lists.newArrayList();
        scoreResult.stream().forEach(i->{
            Map map = Maps.newHashMap();
            map.put("省份",i.get("name"));
            map.put("分数",i.get("average"));
            rows.add(map);
        });
        Map mapData = Maps.newHashMap();
        mapData.put("columns",columns);
        mapData.put("rows",rows);
        return mapData;
    }

    /**
     * 转化格式
     * @param numResult
     * @return
     */
    public Object parseNumResult(List<Map> numResult) {
        List<String> columns = Lists.newArrayList("省份","人数");
        List<Map> rows = Lists.newArrayList();
        numResult.stream().forEach(i->{
            Map map = Maps.newHashMap();
            map.put("省份",i.get("name").toString()+i.get("count").toString()+"人");
            map.put("人数",i.get("count"));
            rows.add(map);
        });
        Map mapData = Maps.newHashMap();
        mapData.put("columns",columns);
        mapData.put("rows",rows);
        return mapData;
    }

    /**
     * 按照试卷统计参考人数
     * @param
     * @return
     */
    public Map countByYear(List<Integer> ids){
        List<Long> totalSet = Lists.newLinkedList();
        Integer total = 0;
        for(Integer id:ids){
            Set<String> practiceIds = estimateService.findResultSet(id);
            if(CollectionUtils.isNotEmpty(practiceIds)){
                total += practiceIds.size();
                int start = 0;
                int size = 100;
                while(true){
                    List<Long> tempIds = practiceIds.stream().map(i->Long.parseLong(i)).collect(Collectors.toList());
                    int tempSize = tempIds.size();
                    if(start<tempSize){
                        int end = (start + size) > tempSize ? tempSize : (start + size);
                        List<Long> longs = tempIds.subList(start, end);
                        totalSet.addAll(answerCardDao.findUserIdByIds(longs).values());
                    }else{
                        break;
                    }
                    start = start + size;
                }
            }
        }
        Long num = totalSet.stream().distinct().count();
        logger.info("total={},totalSet.size={}",total,num);
        Map map = Maps.newHashMap();
        map.put("total",total);
        map.put("num",num);
        map.put("totalSet",totalSet.stream().distinct().collect(Collectors.toList()));
        return map;
    }

    /**
     * 通过模考大赛ID 获取报名信息
     *
     * @param paperId
     * @return
     */
    public List<MatchUserMeta> getMatchUserListByMatchId(int paperId) {
        long start = System.currentTimeMillis();
        List<MatchUserMeta> metas = Lists.newArrayList();
        long cursor = 0L;
        int size = 100;
        while(true){
            Criteria criteria = Criteria.where("paperId").is(paperId).and("userId").gt(cursor);
            Query query = new Query(criteria);
            query.with(new Sort(Sort.Direction.ASC,"userId")).limit(size);
            List<MatchUserMeta> list = mongoTemplate.find(new Query(criteria), MatchUserMeta.class);
            if(CollectionUtils.isNotEmpty(list)){
                metas.addAll(list);
                cursor = list.stream().map(i->i.getUserId()).max(Comparator.comparing(i->i)).get();
            }else{
                break;
            }
        }
        long end = System.currentTimeMillis();
        logger.info("查询mongo用时{}",(end-start)/1000);
        return metas;
    }

    public List<UserDto> getUserInfoListByUserMate(List<MatchUserMeta> list) {
        long start = System.currentTimeMillis();
        List<Long> userIds = list.stream().map(i->i.getUserId()).sorted().collect(Collectors.toList());
        int indexStart = 0;
        int size = 100;
        int length = userIds.size();
        List<UserDto> userDtoList = Lists.newArrayList();
        while(indexStart<length){
            int end = indexStart+size>length?length:indexStart+size;
            userDtoList.addAll(userHuatuDao.findByIds(userIds.subList(indexStart,end)));
            indexStart = end;
        }
        long end = System.currentTimeMillis();
        logger.info("查询mysql用时{}",(end-start)/1000);
        return userDtoList;
    }

    /**
     * 提供测试
     * @param list
     * @param name
     */
    public void assertCourseUserPracticeMeta(List<List> list, String name) {
        long startTime = System.currentTimeMillis();
        logger.info("list={}",list.size());
        List title = list.get(0);
        title.add("参考模考次数");
        List<List> result = list.subList(1,list.size());
        logger.info("title={}",title);
        String[] titleRow = new String[title.size()];
        for(int i=0;i<title.size();i++){
            titleRow[i] = title.get(i).toString();
        }
        int start = 0;
        int size = 1000;
        Map<String,Integer> userCountMap = Maps.newHashMap();
        List<String> uNames = result.stream().filter(i->i!=null).map(i->String.valueOf(i.get(1))).distinct().collect(Collectors.toList());
        int length = uNames.size();
        while(start<length){
            int end = start+size>length?length:start+size;
            userCountMap.putAll(assertTest(uNames.subList(start,end)));
            start = end;
            logger.info("进度：{}/{}",end,length);
        }
        for (List userInfo : result) {
            if(userInfo.get(1)==null){
                userInfo.add(0);
                continue;
            }
            String uname = userInfo.get(1).toString().trim();
            userInfo.add(userCountMap.getOrDefault(uname,0));
            if(userInfo.size()!=10){
                userInfo.add(userInfo.get(8));
                userInfo.set(8,userInfo.get(7));
                userInfo.set(7,"");
            }
        }
        try {
            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH,name,"xls",result,titleRow);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        logger.info("总用时：{}",(endTime-startTime)/1000);
    }

    private Map<String,Integer>  assertTest(List<String> unames) {
        List<UserDto> users = userHuatuDao.findByNames(unames);
        if(CollectionUtils.isEmpty(users)){
            return Maps.newHashMap();
        }
        logger.info("Ids={}",users.stream().map(i->i.getId()).collect(Collectors.toList()));
        Map<Long,Integer> userMeta = userMetaDao.groupByUserId(users.stream().map(i->i.getId()).collect(Collectors.toList()));
        logger.info("meta={}",userMeta);
        logger.info("result={}",users.stream().filter(i->i!=null).filter(i->i.getName()!=null).collect(Collectors.toMap(i->i.getName(),i->userMeta.getOrDefault(i.getId(),0))));
        return users.stream().filter(i->i!=null).filter(i->i.getName()!=null).collect(Collectors.toMap(i->i.getName(),i->userMeta.getOrDefault(i.getId(),0)));
    }

    /**
     * 所有模考参考用户手机号和最后一次考试的时间
     */
    public void countEstimatePhoneWithTimt(){
        long start = System.currentTimeMillis();
        List<Integer> ids2017 = Lists.newArrayList(2005319,2005380,2005379,3526744,2005376,2005375,2005374,
                2005373,2005372,2005369,3526734,2005356,3526701,2005352,2005351,3526689,2005346,2005345,2005342,2005341,2005327,2005323,3526618,
                2005320,2005318,2005316,2005294,2005282,2005244,2005243,2005173,2005172,2005167,2005149,2005129,
                2005251,2005245,2005240,2005174,2005169,2005168,2005136,2005130,2005065,2005064,2005049,2005038,2005030,2005010,2005005,
                2005090,2005061,2004992);
        List<Integer> ids2018 = Lists.newArrayList(3525752,3525951,3525993,3525999,3526002,3526087,
                3526088,3526090,3526092,3526098,3526158,3526165,3526553,3526600,3526623,3526628,3526644,3526653,3526690,3526693,
                3526718,3526756,3526792,3526809,3526830,3526843,3526890,2005421,3526885,2005420,2005419,2005418,3526881,3526876,
                3526831);
        List<Integer> paperIds = Lists.newArrayList();
        paperIds.addAll(ids2017);
        paperIds.addAll(ids2018);
        List<Paper> papers = paperDao.findByIds(paperIds);
        List<EstimatePaper> estimatePapers = papers.stream().filter(i->i instanceof EstimatePaper).map(i->(EstimatePaper)i).collect(Collectors.toList());
        Long startTime = DateUtil.parseYYYYMMDDDate("2017/05/01").getTime();
        Long endTime = DateUtil.parseYYYYMMDDDate("2018/07/01").getTime();
        List<EstimatePaper> finalPapers = estimatePapers.stream().filter(i->i.getStartTime()>startTime).filter(i->i.getEndTime()<endTime).collect(Collectors.toList());
        finalPapers.sort(Comparator.comparingLong(i->i.getStartTime()));
        Map<Integer,Long> paperTimeMap  = finalPapers.stream().collect(Collectors.toMap(i->i.getId(),i->i.getStartTime()));
        Map<String,Long> phoneTimeMap = new HashedMap();
        for (EstimatePaper finalPaper : finalPapers) {
            int paperId = finalPaper.getId();
            phoneTimeMap.putAll(countPhoneTime(paperTimeMap.get(paperId),paperId));
        }
        long end = System.currentTimeMillis();
        logger.info("数据生成用时{}",(end-start)/1000);
        sendMail(phoneTimeMap);
    }

    private void sendMail(Map<String, Long> phoneTimeMap) {
        List<List> dataList = Lists.newArrayList();
        List<Map.Entry<String, Long>> entries = phoneTimeMap.entrySet().stream().collect(Collectors.toList());
        entries.sort(Comparator.comparing(Map.Entry::getValue));
        for (Map.Entry<String, Long> entry : entries) {
            Long time = entry.getValue();
            String phone  = entry.getKey();
            List list = Lists.newArrayList(phone, DateFormat.dateTostr(new Date(time)));
            dataList.add(list);
        }

        long start2 = System.currentTimeMillis();
        String[] title = {"手机号","时间"};
        try {
            ExcelManageUtil.writer(FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH,"MatchEnrollInfo_all","xls",dataList,title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map mapData = Maps.newHashMap();
        mapData.put("title","MatchEnrollInfo_all");
        mapData.put("text","MatchEnrollInfo_all"+"_"+new Date());
        mapData.put("filePath",FunFileUtils.TMP_EXCEL_SOURCE_FILEPATH+"MatchEnrollInfo_all"+".xls");
        mapData.put("attachName","MatchEnrollInfo_"+System.currentTimeMillis());

        try {
            MailUtil.sendMail(mapData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long start3 = System.currentTimeMillis();
        logger.info("写入excel用时：{}",start3-start2);
    }

    private Map<? extends String,? extends Long> countPhoneTime(Long time, int paperId) {
        long start = System.currentTimeMillis();
        Map map = countByYear(Lists.newArrayList(paperId));
        long start1 = System.currentTimeMillis();
        logger.info("查询统计人数耗时：{}",start1-start);
        List<Long> userIds = (List<Long>)map.get("totalSet");
        List<UserDto> userDtos = userHuatuDao.findByIds(userIds);
        long end = System.currentTimeMillis();
        logger.info("查询用户数据耗时：{}",end-start);
        Map<String, Long> result = Maps.newHashMap();
        if(CollectionUtils.isNotEmpty(userDtos)){
            userDtos.stream().forEach(i->result.put(i.getMobile(),time));
        }
        return result;
    }

}
