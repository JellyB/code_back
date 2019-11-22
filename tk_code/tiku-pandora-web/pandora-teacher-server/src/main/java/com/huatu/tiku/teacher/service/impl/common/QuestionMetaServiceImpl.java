package com.huatu.tiku.teacher.service.impl.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.constants.QuestionWeightConstant;
import com.huatu.tiku.teacher.service.common.QuestionMetaService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import com.huatu.ztk.question.common.QuestionReidsKeys;
import com.huatu.ztk.question.common.QuestionType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/11/8.
 */
@Service
public class QuestionMetaServiceImpl implements QuestionMetaService {


    @Autowired
    RedisTemplate redisTemplate;
    //question meta信息，保留答案统计最多个数
    public static final int MAX_ANSWER_COUNT = 4;


    /**
     * 组装试题的meta信息
     *
     * @param genericQuestion
     * @return
     */
    public QuestionMeta findMeta(GenericQuestion genericQuestion) {
        if (genericQuestion == null) {
            return null;
        }
        //本地没有统计数据，从reids缓存中获取（redis中存储着某一试题所有答案被选中的次数）
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        final String questionMetaKey = QuestionReidsKeys.getQuestionMetaKey(genericQuestion.getId());
        /**
         * entry 结构
         * key 0=该试题所有耗时
         * 其它key 用户作答的该答案 value为答题次数
         */
        Map<byte[], byte[]> map;
        try {
            map = connection.hGetAll(questionMetaKey.getBytes());
        } finally {
            connection.close();
        }
        if (MapUtils.isEmpty(map)) {
            return getInitQuestionMeta(genericQuestion);
        }
        final Map<String, String> metaMap = map.entrySet().stream().collect(Collectors.toMap(i -> new String(i.getKey()), i -> new String(i.getValue())));
        if (MapUtils.isEmpty(metaMap)) {
            return getInitQuestionMeta(genericQuestion);
        }

        long expendTime = 0;
        //答案各个选项的选中次数统计
        Map<Integer, Integer> answersMap = Maps.newHashMap();
        for (String key : metaMap.keySet()) {
            final Integer integer = Integer.valueOf(key);
            if (integer == 0) {//花费时间合计
                expendTime = MapUtils.getLong(metaMap, key);
            } else {//答案统计
                answersMap.put(integer, Integer.valueOf(metaMap.get(key)));
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
            return getInitQuestionMeta(genericQuestion);
        }

        //是否包含正确答案
        boolean hasRightAnswer = fianlAnswersMap.containsKey(genericQuestion.getAnswer());
        if (!hasRightAnswer) {//没有包含,把正确答案添加到列表，添加到最后一位
            answers[answers.length - 1] = genericQuestion.getAnswer();
            //防止出现，还没有答对的情况
            counts[answers.length - 1] = Optional.ofNullable(answersMap.get(genericQuestion.getAnswer())).orElse(0);
        }

        //所有做题数量
        int allCount = answersMap.values().stream().mapToInt(value -> value.intValue()).sum();
        //遍历计算百分比，由于没有保留小数位数，可能所有百分比之和不会是100%
        for (int i = 0; i < answers.length; i++) {
            percents[i] = 100 * counts[i] / allCount;

        }
        //对于单选题，对错题，把percent补全为100%
        if (genericQuestion.getType() == QuestionType.SINGLE_CHOICE ||
                genericQuestion.getType() == QuestionType.WRONG_RIGHT) {
            percents[percents.length - 1] = 100 + percents[percents.length - 1] - Arrays.stream(percents).sum();
        }

        int yc = 0;
        //处理易错项
        if (genericQuestion.getType() == QuestionType.MULTIPLE_CHOICE
                || genericQuestion.getType() == QuestionType.SINGLE_OR_MULTIPLE_CHOICE) {
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

        } else if (genericQuestion.getType() == QuestionType.SINGLE_CHOICE) {//单选题
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

        return questionMeta;
    }

    /**
     * TODO 知识点下载功能完善
     * @param question
     * @return
     */
    @Override
    public Double getQuestionWeight(Question question) {
        //本地没有统计数据，从reids缓存中获取（redis中存储着某一试题所有答案被选中的次数）
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Function<Question,List<Integer>> getPointId = (q->{
            List<KnowledgeInfo> list = q.getPointList();
            List<Integer> ids = Lists.newArrayList();
            if(CollectionUtils.isNotEmpty(list)){
                list.forEach(i->{
                    List<Integer> points = i.getPoints();
                    ids.add(points.get(points.size()-1));
                });
            }else{
                ids.add(0);
            }
            return ids;
        });
        List<Integer> ids = getPointId.apply(question);

        return 0D;
    }


    private QuestionMeta getInitQuestionMeta(GenericQuestion genericQuestion) {
        return QuestionMeta.builder()
                .avgTime(60)
                .count(1)
                .answers(new int[]{genericQuestion.getAnswer()})
                .counts(new int[]{1})
                .percents(new int[]{100})
                .yc(genericQuestion.getAnswer() == 1 ? 2 : 1)//易错项
                .rindex(0)
                .build();
    }
}
