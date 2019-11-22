package com.huatu.tiku.match.service.impl.v1.meta;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.bean.BaseEntity;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.match.bean.entity.MatchQuestionMeta;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.common.MatchInfoRedisKeys;
import com.huatu.tiku.match.dao.document.PaperDao;
import com.huatu.tiku.match.dao.manual.meta.MatchQuestionMetaMapper;
import com.huatu.tiku.match.service.v1.meta.MatchQuestionMetaService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.tiku.match.service.v1.paper.QuestionService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.util.QuestionPointUtil;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import com.huatu.ztk.question.common.QuestionCorrectType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import service.impl.BaseServiceHelperImpl;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/10/16.
 */
@Slf4j
@Service
public class MatchQuestionMetaServiceImpl extends BaseServiceHelperImpl<MatchQuestionMeta> implements MatchQuestionMetaService {

    public MatchQuestionMetaServiceImpl() {
        super(MatchQuestionMeta.class);
    }

    //question meta信息，保留答案统计最多个数
    public static final int MAX_ANSWER_COUNT = 4;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PaperDao paperDao;
    @Autowired
    QuestionService questionService;
    @Autowired
    MatchUserMetaService matchUserMetaService;
    @Autowired
    MatchQuestionMetaMapper matchQuestionMetaMapper;

    @Override
    public List<QuestionPointTree> questionPointSummaryWithTotalNumber(List<Integer> questions, int[] corrects, int[] times) {
        return questionPointSummary(questions, corrects, times, false);
    }

    /**
     * 试题统计数据录入
     *
     * @param answerCard
     */
    @Override
    public void handlerQuestionMeta(AnswerCard answerCard) {
        if (answerCard instanceof StandardCard) {
            Paper paper = ((StandardCard) answerCard).getPaper();
            List<Integer> questions = paper.getQuestions();
            int[] times = answerCard.getTimes();
            String[] answers = answerCard.getAnswers();
            int[] corrects = answerCard.getCorrects();
            handlerQuestionMeta(questions, times, answers, corrects);
        }
    }

    /**
     * 试题统计数据录入
     *
     * @param questions
     * @param times
     * @param answers
     * @param corrects
     */
    private void handlerQuestionMeta(List<Integer> questions, int[] times, String[] answers, int[] corrects) {
        log.info("handlerQuestionMeta info = {}", questions);
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }
        for (int i = 0; i < questions.size(); i++) {
            if (corrects[i] != 0) {
                Integer question = questions.get(i);
                //更新试题统计
                final HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
                final String questionMetaKey = MatchInfoRedisKeys.getQuestionMetaKey(question);
                try {
                    hashOperations.increment(questionMetaKey, "0", times[i]);//原子增加答题耗时
                    hashOperations.increment(questionMetaKey, answers[i], 1);//该答案选择数量+1
                    log.info("key={},time={},answer={}", questionMetaKey, times[i], answers[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                    redisTemplate.delete(questionMetaKey);
                }
            }
        }
    }

    /**
     * 基于redis获取到数据
     *
     * @param questionId
     * @return
     */
    @Override
    public QuestionMeta getQuestionMeta(int questionId) {
        final HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        final String questionMetaKey = MatchInfoRedisKeys.getQuestionMetaKey(questionId);
        /**
         * entry 结构
         * key 0=该试题所有耗时
         * 其它key 用户作答的该答案 value为答题次数
         */
        final Map<String, Object> metaMap = hashOperations.entries(questionMetaKey);
        if (MapUtils.isEmpty(metaMap)) {
            /**
             * 查询mysql或者做初始化数据
             */
            QuestionMeta questionMeta = findQuestionMetaBySql(questionId);
            if (null == questionMeta) {           //如果无统计数据，则初始化统计数据
                questionMeta = getInitQuestionMeta(questionId);
            }
            if (null == questionMeta) {       //初始化统计数据为空，则证明该题无法统计数据
                return questionMeta;
            }
            Map<String, Object> tempMap = convertMetaObject2Map(questionMeta);
            hashOperations.putAll(questionMetaKey, tempMap);
            return questionMeta;
        }
        QuestionMeta questionMeta = convertMetaMap2Object(metaMap, questionId);
        return questionMeta;
    }

    /**
     * 批量查询试卷下的试题统计信息
     *
     * @param paperId
     * @return
     */
    @Override
    public Map<Integer, QuestionMeta> getQuestionMetaByPaperId(int paperId) {
        Paper paper = paperDao.findPaperById(paperId);
        if (null == paper) {
            log.error("试卷信息为空，paperId = {}", paperId);
            return Maps.newHashMap();
        }
        List<Integer> questions = paper.getQuestions();
        if (CollectionUtils.isEmpty(questions)) {
            return Maps.newHashMap();
        }
        Map<Integer, QuestionMeta> questionMetaMap = Maps.newHashMap();
        for (Integer question : questions) {
            QuestionMeta questionMeta = getQuestionMeta(question);
            if (null != questionMeta) {
                questionMetaMap.put(question, questionMeta);
            }
        }
        return questionMetaMap;
    }

    /**
     * 重置所有试卷的模考大赛试题统计信息
     */
    @Override
    public void reCountQuestionMeta() {
        Consumer<MatchQuestionMeta> clear = (meta -> {
            clearQuestionMeta(meta);
        });
        readAllAndOperation(clear);
    }

    private void clearQuestionMeta(MatchQuestionMeta meta) {
        if (null == meta) {
            return;
        }
        System.out.println(meta.getId());
        //更新试题统计
        final String questionMetaKey = MatchInfoRedisKeys.getQuestionMetaKey(meta.getQuestionId());
        redisTemplate.delete(questionMetaKey);
        Integer matchId = meta.getMatchId();
        String lock = MatchInfoRedisKeys.getQuestionMetaLockedMatchId();
        redisTemplate.opsForSet().add(lock, matchId + "");
        redisTemplate.expire(lock, 1, TimeUnit.DAYS);
        matchQuestionMetaMapper.deleteByPrimaryKey(meta.getId());

    }

    private void readAllAndOperation(Consumer<MatchQuestionMeta> clear) {
        int index = 0;
        int limit = 1000;
        while (true) {
            List<MatchQuestionMeta> metas = findByCursor(index, limit);
            if (CollectionUtils.isEmpty(metas)) {
                break;
            }
            Optional<Integer> max = metas.stream().map(BaseEntity::getId).map(Long::intValue).max(Integer::compare);
            if (max.isPresent()) {
                index = max.get();
            }
            for (MatchQuestionMeta meta : metas) {
                clear.accept(meta);
            }
        }
    }

    private List<MatchQuestionMeta> findByCursor(int index, int limit) {
        List<HashMap> list = matchQuestionMetaMapper.findByCursor(index, limit);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(i -> {
            return MatchQuestionMeta.builder().id(MapUtils.getLong(i, "id"))
                    .matchId(MapUtils.getInteger(i, "match_id"))
                    .questionId(MapUtils.getInteger(i, "question_id"))
                    .build();
        }).collect(Collectors.toList());
    }


    private Map<String, Object> convertMetaObject2Map(QuestionMeta questionMeta) {
        int[] answers = questionMeta.getAnswers();
        int avgTime = questionMeta.getAvgTime();
        int count = questionMeta.getCount();
        int[] counts = questionMeta.getCounts();
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("0", avgTime * count);
        for (int i = 0; i < answers.length; i++) {
            map.put(answers[i] + "", counts[i]);
        }
        return map;
    }

    /**
     * 基于mysql获取到数据
     *
     * @param questionId
     * @return
     */
    private QuestionMeta findQuestionMetaBySql(int questionId) {
        Example example = new Example(MatchQuestionMeta.class);
        example.and().andEqualTo("questionId", questionId);
        List<MatchQuestionMeta> matchQuestionMetas = selectByExample(example);
        if (CollectionUtils.isNotEmpty(matchQuestionMetas)) {
            MatchQuestionMeta matchQuestionMeta = matchQuestionMetas.get(0);
            String detail = matchQuestionMeta.getDetail();
            try {
                QuestionMeta questionMeta = JsonUtil.toObject(detail, QuestionMeta.class);
                return questionMeta;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /**
         * 初始化数据
         */
        return getInitQuestionMeta(questionId);
    }

    private QuestionMeta convertMetaMap2Object(Map<String, Object> metaMapInfo, int questionId) {

        //过滤掉空字符串
        Map<String, Object> metaMap = metaMapInfo.entrySet().stream().filter(key -> org.apache.commons.lang.StringUtils.isNotEmpty(key.getKey()))
                .collect(Collectors.toMap(id -> id.getKey(), id -> id.getValue()));

        //log.info("结果是:{}", JsonUtil.toJson(metaMap));
        long expendTime = 0;
        //答案各个选项的选中次数统计
        Map<Integer, Integer> answersMap = Maps.newHashMap();
        //log.info("试题ID:{},试题选项信息是:{}", questionId, JsonUtil.toJson(metaMap));
        for (String key : metaMap.keySet()) {
            //log.info("试题选项key是:{}", key);
            final Integer integer = Integer.valueOf(key);
            if (integer == 0) {//花费时间合计
                expendTime = MapUtils.getLong(metaMap, key);
            } else {//答案统计
                answersMap.put(integer, MapUtils.getInteger(metaMap, key));
            }
        }
        //最多保留四个答案的统计数量
        Map<Integer, Integer> fianlAnswersMap = answersMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .limit(MAX_ANSWER_COUNT)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        /**
         * 此处 如果实际答案的选项小于 四个,则整体统计全部出错
         * update by lijun
         * 2018-03-09
         */
        int[] answers = new int[fianlAnswersMap.size() < MAX_ANSWER_COUNT ? MAX_ANSWER_COUNT : fianlAnswersMap.size()];
        int[] counts = new int[answers.length];
        int[] percents = new int[answers.length];
        int index = 0;
        //遍历组装答案列表和数量
        for (Integer answer : fianlAnswersMap.keySet()) {
            answers[index] = answer;
            counts[index] = fianlAnswersMap.get(answer);

            index++;
        }
        if (answers.length < 1) {//防止出现为空的情况
            return getInitQuestionMeta(questionId);
        }

        Question question = questionService.findQuestionCacheById(questionId);
        if (!(question instanceof GenericQuestion)) {
            return getInitQuestionMeta(questionId);
        }
        GenericQuestion genericQuestion = (GenericQuestion) question;
        //是否包含正确答案
        boolean hasRightAnswer = fianlAnswersMap.containsKey(genericQuestion.getAnswer());
        if (!hasRightAnswer) {//没有包含,把正确答案添加到列表，添加到最后一位
            answers[answers.length - 1] = genericQuestion.getAnswer();
            //防止出现，还没有答对的情况
            counts[answers.length - 1] = Optional.ofNullable(answersMap.get(genericQuestion.getAnswer())).orElse(0);
        }

        //所有做题数量
        int allCount = answersMap.values().stream().mapToInt(value -> value.intValue()).sum();
        allCount = allCount == 0 ? 1 : allCount;
        //遍历计算百分比，由于没有保留小数位数，可能所有百分比之和不会是100%
        for (int i = 0; i < answers.length; i++) {
            percents[i] = 100 * counts[i] / allCount;

        }
        //对于单选题，对错题，把percent补全为100%

        if (genericQuestion.getType() == QuestionInfoEnum.QuestionTypeEnum.SINGLE.getCode() ||
                genericQuestion.getType() == QuestionInfoEnum.QuestionTypeEnum.JUDGE.getCode()) {
            percents[percents.length - 1] = 100 + percents[percents.length - 1] - Arrays.stream(percents).sum();
        }

        int yc = 0;
        //处理易错项
        if (genericQuestion.getType() == QuestionInfoEnum.QuestionTypeEnum.MULTI.getCode()
                || genericQuestion.getType() == QuestionInfoEnum.QuestionTypeEnum.INFINITIVE.getCode()) {
            //多选不定项
            int[] tmpCounts = new int[genericQuestion.getChoices().size()];
            for (Integer key : answersMap.keySet()) {
                String keyStr = key + "";
                //作答次数
                int count = Integer.valueOf(answersMap.get(key));
                for (int i = 0; i < keyStr.length(); i++) {
                    int answerIndex = Integer.valueOf(keyStr.charAt(i) + "") - 1;
                    if (answerIndex > tmpCounts.length - 1 || answerIndex < 0) {//答案越界不进行处理
                        //logger.warn("viald answer={},qid={}",key,genericQuestion.getId());
                        continue;
                    }
                    tmpCounts[answerIndex] = tmpCounts[answerIndex] + count;
                }
            }

            String answerStr = genericQuestion.getAnswer() + "";
            int max = 0;
            for (int i = 0; i < tmpCounts.length; i++) {//计算易错项
                int tmpCount = tmpCounts[i];//答案被选次数
                final int answer = i + 1;
                //不能包含正确答案
                if (!answerStr.contains(answer + "") && max < tmpCount) {
                    max = tmpCount;
                    yc = answer;//选项即为 index+1
                }
            }

        } else if (genericQuestion.getType() == QuestionInfoEnum.QuestionTypeEnum.SINGLE.getCode()) {//单选题
            int max = 0;
            for (int i = 0; i < counts.length; i++) {
                final int answer = answers[i];
                //不是正确答案，并且被选择次数最多
                if (genericQuestion.getAnswer() != answer && max < counts[i]) {
                    max = counts[i];
                    yc = answer;
                }
            }
        }

        if (yc == 0) {//不存在易错项，则随机找一个易错项，查找逻辑:非A则B
            yc = 1;//默认第一个 A
            if (yc == genericQuestion.getAnswer()) {
                yc = 2;//如果1是正确答案，则取第二个答案
            }
        }

        int rindex = 0;//正确答案所在的索引位置
        for (int i = 0; i < answers.length; i++) {
            if (answers[i] == genericQuestion.getAnswer()) {
                rindex = i;
            }
        }

        QuestionMeta questionMeta = QuestionMeta.builder()
                .answers(answers)
                .counts(counts)
                .percents(percents)
                .count(allCount)
                .avgTime((int) expendTime / allCount)
                .yc(yc)
                .rindex(rindex)
                .build();

        // logger.info("{} zhouwei20181618:"+(System.currentTimeMillis()-start),genericQuestion.getId());
        return questionMeta;
    }

    private QuestionMeta getInitQuestionMeta(int questionId) {
        Question question = questionService.findQuestionCacheById(questionId);
        if (question instanceof GenericQuestion) {
            return QuestionMeta.builder()
                    .avgTime(60)
                    .count(1)
                    .answers(new int[]{((GenericQuestion) question).getAnswer()})
                    .counts(new int[]{1})
                    .percents(new int[]{100})
                    .yc(((GenericQuestion) question).getAnswer() == 1 ? 2 : 1)//易错项
                    .rindex(0)
                    .build();
        }
        return null;
    }

    /**
     * 知识点统计
     *
     * @param questions 试题列表
     * @param corrects  答题是否正确
     * @param times     答题时间
     * @param totalType 获取错误信答题正确率时 计算总数的基数情况
     * @return update by lijun 2018-03-27
     * 此处代码原本为原本接口,此处为了区分不同情况下 知识点的正确率计算情况 重载了此方法
     */
    public List<QuestionPointTree> questionPointSummary(List<Integer> questions, int[] corrects, int[] times, boolean totalType) {
        if (null == questions) {
            return new ArrayList<>();
        }
        StopWatch stopWatch = new StopWatch("questionPointSummary");
        stopWatch.start("findQuestionCacheByIds");
        final List<Question> bath = questionService.findQuestionCacheByIds(questions);
        stopWatch.stop();
        Map<Integer, QuestionPointTree> data = new HashMap<>();
        Map<Integer, QuestionPoint> temp = Maps.newHashMap();
        if (bath != null) {
            for (int i = 0; i < bath.size(); i++) {
                Question question = bath.get(i);
                stopWatch.start("handlerQuestion:" + question.getId());
                if (question == null || !(question instanceof GenericQuestion)) {//理论上是不存在的
                    stopWatch.stop();
                    continue;
                }

                GenericQuestion genericQuestion = (GenericQuestion) question;
                if (null == genericQuestion || CollectionUtils.isEmpty(genericQuestion.getPoints())) {
                    log.info("试题缺少知识点>>>>>,试题ID = {}", genericQuestion.getId());
                    stopWatch.stop();
                    continue;
                }
                final List<Integer> points = genericQuestion.getPoints();
                final List<String> pointsName = genericQuestion.getPointsName();
                putPoints2Map(points, pointsName, temp);
                for (Integer point : points) {
                    QuestionPointTree questionPointTree = data.get(point);
                    if (questionPointTree == null) {
                        final QuestionPoint questionPoint = temp.get(point);
                        if (questionPoint == null) {//知识点没有查询到
                            stopWatch.stop();
                            continue;//不进行处理
                        }
                        questionPointTree = QuestionPointUtil.conver2Tree(questionPoint);
                        questionPointTree.setQnum(0);//初始化题数，防止conver2Tree里面设置qnum
                        //写入map
                        data.put(questionPointTree.getId(), questionPointTree);
                    }

                    if (questionPointTree == null) {//找不到对应的知识点，则处理,理论上不存在此情况
                        log.error("can`t find parent knowledge point. pointId={}", point);
                        stopWatch.stop();
                        continue;
                    }

                    int currect = corrects[i];
                    questionPointTree.setQnum(questionPointTree.getQnum() + 1);
                    questionPointTree.setTimes(questionPointTree.getTimes() + times[i]);//设置试题所花时间
                    if (QuestionCorrectType.RIGHT == currect) {//答题正确
                        questionPointTree.setRnum(questionPointTree.getRnum() + 1);
                    } else if (QuestionCorrectType.WRONG == currect) {//答题错误
                        questionPointTree.setWnum(questionPointTree.getWnum() + 1);
                    } else if (QuestionCorrectType.UNDO == currect) {//没有作答
                        questionPointTree.setUnum(questionPointTree.getUnum() + 1);
                    } else {//非法的答案视为错误的
                        questionPointTree.setWnum(questionPointTree.getWnum() + 1);
                        log.error("illegal knowledge answer status,status={},questionId={}", currect, question.getId());
                    }
                }
                stopWatch.stop();
            }
        }
        for (QuestionPointTree questionPointTree : data.values()) {//遍历知识点列表，计算平均时间
            stopWatch.start("handlerQuestionPointTree:" + questionPointTree.getName());
            /**
             * 此处处理计算正确率时候 答题总数量问题
             * (1）专项练习知识树上显示的正确率=答对数量/已答数量
             *（2）抽题或套题报告里显示的正确率=答对数量/（已答数量+未答数量）
             *
             * add by lijun 2018-03-27
             */
            int questionNum;
            if (totalType) { //系统中的原始情况
                //已经作答的题数
                questionNum = questionPointTree.getRnum() + questionPointTree.getWnum();
            } else {
                questionNum = questionPointTree.getRnum() + questionPointTree.getWnum() + questionPointTree.getUnum();
            }
            /**
             * end
             */
            int speed = 0;
            double accuracy = 0;
            if (questionNum > 0) {
                speed = questionPointTree.getTimes() / questionNum;//计算平均时间
                //正确率
                accuracy = new BigDecimal(questionPointTree.getRnum() * 100).divide(new BigDecimal(questionNum), 1, RoundingMode.HALF_UP).doubleValue();
            }
            questionPointTree.setSpeed(speed);
            questionPointTree.setAccuracy(accuracy);
            stopWatch.stop();
        }
        stopWatch.start("wapper2Trees");
        List<QuestionPointTree> questionPointTrees = QuestionPointUtil.wapper2Trees(data.values());
        stopWatch.stop();
        log.info("questionPointSummary stopWatch:{}", stopWatch.prettyPrint());
        return questionPointTrees;
    }

    private void putPoints2Map(List<Integer> points, List<String> pointsName, Map<Integer, QuestionPoint> temp) {
        for (int i = points.size() - 1; i >= 0; i--) {
            Integer id = points.get(i);
            if (null != temp.get(id)) {
                continue;
            }
            QuestionPoint questionPoint = new QuestionPoint();
            questionPoint.setId(id);
            questionPoint.setName(pointsName.get(i));
            if (i - 1 >= 0) {
                Integer parent = points.get(i - 1);
                questionPoint.setParent(parent);
            } else {
                questionPoint.setParent(0);
            }
            questionPoint.setLevel(i);
            questionPoint.setStatus(1);
            temp.put(id, questionPoint);
        }
    }

}
