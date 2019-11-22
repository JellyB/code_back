package com.huatu.ztk.question.service;

import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.common.*;
import com.huatu.ztk.question.dao.QuestionDao;
import com.huatu.ztk.question.dao.ReflectQuestionDao;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.huatu.ztk.question.util.ImageUtil;
import com.huatu.ztk.question.util.PageUtil;
import ij.ImagePlus;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by shaojieyue
 * Created time 2016-05-10 15:12
 */

@Service
public class QuestionDubboServiceImpl implements QuestionDubboService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionDubboServiceImpl.class);
    //question meta信息，保留答案统计最多个数
    public static final int MAX_ANSWER_COUNT = 4;
    //试题图片前缀
    private static String imgprefix = "http://tiku.huatu.com/cdn/images/vhuatu/tiku/";
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    ReflectQuestionDao reflectQuestionDao;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public static final String HTML_TAG_BR = "br";
    public static final String HTML_TAG_IMG = "img";
    public static final String HTML_TAG_P = "p";
    public static final String HTML_TAG_SPAN = "span";
    public static final String HTML_TAG_U = "u";
    public static final String HTML_TAG_STRONG = "strong";      //加粗标签
    //question meta2 信息缓存
    public static final Cache<Integer, QuestionMeta> QUESTION_META_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.MINUTES)//调整为20分钟
            .maximumSize(100000)
            .initialCapacity(40000)
            .build();
    //试题映射信息查询
    public static final Cache<Integer, Integer> REFLECTION_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.DAYS)
            .maximumSize(1000000)
            .initialCapacity(100000).build();

    /**
     * 插入试题
     *
     * @param question
     */
    @Override
    public void insert(Question question) throws IllegalQuestionException {
        if (question == null) {
            logger.warn("insert question object is null.");
            return;
        }
        checkFormatQuestion(question);
        questionDao.insert(question);
        //发送试题更新消息
        sendQuestionUpdateMessage(question);
    }

    /**
     * 检查并格式化试题
     *
     * @param question
     * @throws IllegalQuestionException
     */
    private void checkFormatQuestion(Question question) throws IllegalQuestionException {
//        //如果是非公务员的题目，不做格式化处理
//        if(question.getSubject()!= SubjectType.GWY_XINGCE){
//            return;
//        }
        if (question instanceof GenericQuestion) {
            GenericQuestion genericQuestion = (GenericQuestion) question;
            if (StringUtils.isBlank(genericQuestion.getStem())) {
                throw new IllegalQuestionException("题干不能为空,data=" + JsonUtil.toJson(question));
            }

            if (genericQuestion.getPoints().size() != 3) {
                throw new IllegalQuestionException("知识点错误,data=" + JsonUtil.toJson(question));
            }

            if (CollectionUtils.isEmpty(genericQuestion.getPoints())) {
                throw new IllegalQuestionException("知识点不能为空,data=" + JsonUtil.toJson(question));
            }
            for (String choice : genericQuestion.getChoices()) {
                if (StringUtils.isBlank(choice)) {
                    throw new IllegalQuestionException("存在为空的选项,data=" + JsonUtil.toJson(question));
                }
            }

            //格式化数据
            genericQuestion.setStem(convert2MobileLayout(genericQuestion.getStem()));
            genericQuestion.setAnalysis(convert2MobileLayout(genericQuestion.getAnalysis()));
            genericQuestion.setMaterial(convert2MobileLayout(genericQuestion.getMaterial()));
            /**
             * updaye bu lijun 2018-05-18
             */
            if (StringUtils.isNotBlank(genericQuestion.getStem())) {
                genericQuestion.setExtend(convert2MobileLayout(genericQuestion.getExtend()));
            }

            //格式化materials
            List<String> materials = genericQuestion.getMaterials();
            if (CollectionUtils.isNotEmpty(materials)) {
                List<String> newMaterials = materials.stream()
                        .map(m -> convert2MobileLayout(m))
                        .collect(Collectors.toList());
                genericQuestion.setMaterials(newMaterials);
            }

            for (int i = 0; i < genericQuestion.getChoices().size(); i++) {
                String choice = convert2MobileLayout(genericQuestion.getChoices().get(i));
                genericQuestion.getChoices().set(i, choice);
            }
        } else if (question instanceof CompositeQuestion) {
            CompositeQuestion compositeQuestion = (CompositeQuestion) question;
            if (StringUtils.isBlank(compositeQuestion.getMaterial())) {
                throw new IllegalQuestionException("材料不能为空,data=" + JsonUtil.toJson(question));
            }
            compositeQuestion.setMaterial(convert2MobileLayout(compositeQuestion.getMaterial()));

            //格式化materials
            List<String> materials = compositeQuestion.getMaterials();
            if (CollectionUtils.isNotEmpty(materials)) {
                List<String> newMaterials = materials.stream()
                        .map(m -> convert2MobileLayout(m))
                        .collect(Collectors.toList());
                compositeQuestion.setMaterials(newMaterials);
            }
        }
    }

    /**
     * 格式化试题字符串
     *
     * @param source
     * @return
     */
    public static String format(String source) {
        source = StringUtils.trimToNull(source);
        if (source == null) {
            return "";
        }
        //<div>替换成<p>,多余的<p>将在cleanContent方法去掉，</div>不用处理
        source = source.replaceAll("<p></p>", "")
                .replaceAll("<p />", "")
                .replaceAll("<p/>", "")
                .replaceAll("<br></br>", "<br/>")
                .replaceAll("<br>", "<br/>")
                .replaceAll("<div>", "<p>");

        int oldLength = source.length();
        source = source.replaceAll("<br/><br/>", "<br/>");
        int newLength = source.length();
        //循环去掉多余的br
        while (oldLength > newLength) {
            oldLength = source.length();
            source = source.replaceAll("<br/><br/>", "<br/>");
            newLength = source.length();
        }

        //处理[img style=\"float:none;\"]这样的标签
        //source = source.replaceAll("\\[img.*?\\]", "[img]");

        //括号统一转为中文括号
        source = source.replaceAll("\\(", "（").replaceAll("\\)", "）");

        //防止形如“<!--[img]4924a387ebcb16fb837b7f4b821ddea30a9f2a50.jpg[/img]-->  东盟”的题目失去图片标签
        //去掉字符串头部的不可见字符
        String tmpSource = source.replaceAll("[\\s]", "");
        if (tmpSource.startsWith("<!--") && !tmpSource.endsWith("-->")) {
            source = "<p>" + source;
        }

        return source;
    }


    /**
     * 通过id查询（有数据补偿）
     *
     * @param id
     * @return
     */
    @Override
    public Question findById(int id) {
        Question question = findBySingleId(id);
        /**
         * 补偿映射关系数据
         */
        translateQuestion(question, id);
        convert2MobileQuestionType(question);
        return question;
    }

    /**
     * 通过id查询（不做关联补偿）
     *
     * @param id
     * @return
     */
    public Question findBySingleId(int id) {

        Question question = QuestionCache.get(id);
        Function<Question, Question> setMeta = (i -> {
            if (i instanceof GenericQuestion) {
                GenericQuestion genericQuestion = (GenericQuestion) i;
                genericQuestion.setMeta(findMeta(genericQuestion));
            }
            return i;
        });
        setMeta.apply(question);
        if (question == null) {
            logger.warn("question not at rocksdb. id={},query from db.", id);
            question = questionDao.findById(id);
            //紧急bug修复 为什么变成0？？
            if (question != null && question.getStatus() != 4 && question.getStatus() != 0) {
                setMeta.apply(question);
                fillChildrens(question);
                QuestionCache.put(question);
            } else {
                logger.error("question id error {}", id);
            }
        }
        handlerQuestionKnowledge(question);
        return question;
    }

    /**
     * 兼容大于三级的知识点到原有app中
     *
     * @param question
     */
    private void handlerQuestionKnowledge(Question question) {
        if (null == question) {
            return;
        }
        if (!(question instanceof GenericQuestion)) {
            return;
        }
        List<KnowledgeInfo> pointList = question.getPointList();
        if (CollectionUtils.isEmpty(pointList)) {
            return;
        }
        List<String> pointsName = pointList.get(0).getPointsName();
        if (CollectionUtils.isNotEmpty(pointsName) && pointsName.size() > 3) {
            List<String> newPointsName = Lists.newArrayList();
            newPointsName.addAll(pointsName.subList(0, 2));
            String newThreeLevelName = pointsName.subList(2, pointsName.size()).stream().collect(Collectors.joining(","));
            newPointsName.add(newThreeLevelName);
            ((GenericQuestion) question).setPointsName(newPointsName);
        }
    }

    /**
     * 对于复合题
     * 设置childrens字段
     *
     * @param question
     */
    private void fillChildrens(Question question) {
        if (question instanceof CompositeQuestion) {
            CompositeQuestion compositeQuestion = (CompositeQuestion) question;

            //过滤null
            List<Question> childrens = findBath(compositeQuestion.getQuestions())
                    .stream()
                    .filter(q -> q != null)
                    .collect(Collectors.toList());
            compositeQuestion.setChildrens(childrens);
        } else if (question instanceof CompositeSubjectiveQuestion) {
            CompositeSubjectiveQuestion compositeSubjectiveQuestion = (CompositeSubjectiveQuestion) question;

            List<Question> childrens = findBath(compositeSubjectiveQuestion.getQuestions())
                    .stream()
                    .filter(q -> q != null)
                    .collect(Collectors.toList());

            compositeSubjectiveQuestion.setChildrens(childrens);
        }
    }

    /**
     * 组装试题的meta信息
     *
     * @param genericQuestion
     * @return
     */
    private QuestionMeta findMeta(GenericQuestion genericQuestion) {
        //   long start = System.currentTimeMillis();
        if (genericQuestion == null) {
            return null;
        }
        //判断本地缓存是否有统计数据
        QuestionMeta questionMeta = QUESTION_META_CACHE.getIfPresent(genericQuestion.getId());
        //logger.info("获取考题缓存统计信息 : questinId = {},questionMeta = {}",genericQuestion.getId(),questionMeta);

        if (questionMeta != null) {
            return questionMeta;
        }
        //本地没有统计数据，从reids缓存中获取（redis中存储着某一试题所有答案被选中的次数）
        final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        final String questionMetaKey = QuestionReidsKeys.getQuestionMetaKey(genericQuestion.getId());
        /**
         * entry 结构
         * key 0=该试题所有耗时
         * 其它key 用户作答的该答案 value为答题次数
         */
        final Map<String, String> metaMap = hashOperations.entries(questionMetaKey);
        if (MapUtils.isEmpty(metaMap)) {
            QuestionMeta initQuestionMeta = getInitQuestionMeta(genericQuestion);
            QUESTION_META_CACHE.put(genericQuestion.getId(), initQuestionMeta);
            return initQuestionMeta;
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
        allCount = allCount == 0 ? 1 : allCount;
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

        questionMeta = QuestionMeta.builder()
                .answers(answers)
                .counts(counts)
                .percents(percents)
                .count(allCount)
                .avgTime((int) expendTime / allCount)
                .yc(yc)
                .rindex(rindex)
                .build();

        QUESTION_META_CACHE.put(genericQuestion.getId(), questionMeta);
        // logger.info("{} zhouwei20181618:"+(System.currentTimeMillis()-start),genericQuestion.getId());
        return questionMeta;
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


    /**
     * 批量查询(需要映射关系补偿数据)
     *
     * @param ids
     * @return
     */
    @Override
    public List<Question> findBath(List<Integer> ids) {
        List<Question> questions = findBathSingle(ids);
        translateQuestions(questions, ids);
        for (Question question : questions) {
            convert2MobileQuestionType(question);
        }
        return questions;
    }

    /**
     * 转换成客户端适用的题型
     * 客户端题型判断：type==99,100,101,106都有对应的题型名称写死在代码中
     * type==106 如果没有teacherType则展示写死的主观题，否则展示teacherType
     * type==其他，如果没有teaherType则展示写死的单选题。否则展示teacherType
     *
     * @param question
     */
    private void convert2MobileQuestionType(Question question) {
        if (null == question) {
            return;
        }
        int type = question.getType();
        for (QuestionInfoEnum.QuestionTypeEnum questionTypeEnum : QuestionInfoEnum.QuestionTypeEnum.values()) {
            if (questionTypeEnum.getCode() == type) {
                question.setTeachType(questionTypeEnum.getValue());
                break;
            }
        }
        if (question instanceof GenericSubjectiveQuestion) {
            question.setType(QuestionInfoEnum.QuestionTypeEnum.SUBJECTIVE.getCode());
        }
    }

    /**
     * 批量查询(不做数据补偿)
     *
     * @param ids
     * @return
     */
    public List<Question> findBathSingle(List<Integer> ids) {

        if (ids == null || ids.size() == 0) {
            return Lists.newArrayList();
        }
        int size = ids.size();
        //通过缓存读取
        final Map<Integer, Question> questionMap = QuestionCache.multiGet(ids);
        if (questionMap.size() < size) {//存在没有查询到的试题
            final Set<Integer> hasKeys = questionMap.keySet();
            List missList = new ArrayList();
            for (Integer id : ids) {//遍历需要查询的id
                if (!hasKeys.contains(id)) {//没有查询到对应的试题
                    missList.add(id);//添加到缺失列表
                }
            }
            //直接从缺失列表查询试题
            final List<Question> missQuestions = questionDao.findBath(missList);
            for (Question missQuestion : missQuestions) {//遍历缺失列表
                if (missQuestion != null) {
                    if (missQuestion instanceof GenericQuestion &&
                            StringUtils.isBlank(((GenericQuestion) missQuestion).getExtend())) {
                        ((GenericQuestion) missQuestion).setExtend("");
                    }
                    fillChildrens(missQuestion);
                    //添加到结果map
                    questionMap.put(missQuestion.getId(), missQuestion);
                    //将查询到的数据添加到缓存
                    QuestionCache.put(missQuestion);
                }
            }
        }
        List<Question> results = new ArrayList(size);
        for (Integer id : ids) {//遍历需要查询的id列表，组装数据，包装id和返回的结果一一对应，不存在则设置为null
            final Question question = questionMap.get(id);
            if (question instanceof GenericQuestion) {//普通试题添加meta信息
                GenericQuestion genericQuestion = (GenericQuestion) question;
                genericQuestion.setMeta(findMeta(genericQuestion));
            }
            if (question != null) {
                results.add(question);
            }
        }
        if (CollectionUtils.isNotEmpty(results)) {
            for (Question result : results) {
                handlerQuestionKnowledge(result);
            }
        }
        return results;
    }

    @Override
    public void update(Question question) throws IllegalQuestionException {
        checkFormatQuestion(question);
        //更新数据
        questionDao.update(question);

        //从缓存中删除旧数据
        QuestionCache.remove(question.getId());

        //发送试题更新消息
        sendQuestionUpdateMessage(question);
    }

    /**
     * 发送试题更新消息
     *
     * @param question
     */
    private void sendQuestionUpdateMessage(Question question) {
        Map<String, Integer> data = new HashMap<>();
        data.put("qid", question.getId());
        rabbitTemplate.convertAndSend(QuestionRabbitMqKeys.QUESTION_UPDATE_EXCHANGE, "", data);
    }
//    public  static void main(String args[]){
//        String[]  choices = {
//                "<!--[img style=\"width:25px;height:20px;\"]lNEMgz1NXWhbJA6NClJt8dkFt6z.png[/img]-->，<!--[img style=\"width:27px;height:20px;\"]oB5XxdZqYXXOupEvSjJKZbc6r8Q.png[/img]-->", "<!--[img style=\"width:20px;height:20px;\"]gLexCeEVPEbnTa3YGe50KZNqE2L.png[/img]-->，<!--[img style=\"width:27px;height:20px;\"]qMmpyotg2KA7KIk7Awarxlrc1ft.png[/img]-->", "" +
//                "<!--[img style=\"width:27px;height:20px;\"]tfq-oXTGnS8Q5ZPgfSG3RT0cLY9.png[/img]-->，<!--[img style=\"width:20px;height:20px;\"]lBEhmf2oN8-9O4mA3TLF2ISDtVP.png[/img]-->", "<!--[img style=\"width:20px;height:20px;\"]gLexCeEVPEbnTa3YGe50KZNqE2L.png[/img]-->，<!--[img style=\"width:20px;height:20px;\"]lBEhmf2oN8-9O4mA3TLF2ISDtVP.png[/img]-->"
//        };
//        for(String str:choices){
//            logger.info("result={}",convert2MobileLayout(str));
//        }
//    }

    /**
     * 将内容转为手机支持的原生格式
     *
     * @param content
     * @return
     */
    public static final String convert2MobileLayout(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        content = format(content);
        Document document = Jsoup.parse(content);
        // Element body = document.body();
        List<Node> roots = Lists.newArrayList(document.childNodes());
        if (roots.size() == 0) {//body里面没有内容,说明只是存在图片
            StringBuilder stringBuilder = new StringBuilder();
            convert2MobileLayout(Jsoup.parse(content), stringBuilder);
            return stringBuilder.toString();
        }
        //移除以br结尾的标签
        while (roots.size() > 0 && "br".equalsIgnoreCase(roots.get(roots.size() - 1).nodeName())) {
            roots.remove(roots.size() - 1);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Node root : roots) {//根的话,更换为p标签
            convert2MobileLayout(root, stringBuilder);
        }

        //如果内容不是以<p>开头,则添加<p>标签
        if (!stringBuilder.toString().startsWith("<p>")) {
            stringBuilder.insert(0, "<p>");
            stringBuilder.append("</p>");
        }
        String finalContent = stringBuilder.toString();
        finalContent = cleanContent(finalContent);
        //对html 标签做替换
        finalContent = StringEscapeUtils.unescapeHtml4(finalContent);
        return finalContent;
    }

    /**
     * 清除没有必要的内容
     *
     * @param finalContent
     * @return
     */
    private static String cleanContent(String finalContent) {
        //把不可见字符‍‍‍‍‍‍"\u200D"替换为空
        finalContent = finalContent.replaceAll("\u200D", "");
        //去掉换行
        finalContent = finalContent.replaceAll("\n", "").replaceAll("\r\n", "");

        //去掉空白p标签
        final String ptag = "<p></p>";

        while (finalContent.indexOf(ptag) >= 0) {//循环清除空白p标签,采用循环的目的是为了方式<p><p></p></p>形式的内容
            finalContent = finalContent.replaceAll(ptag, "");
        }
        return finalContent.replaceAll("(<br/></p>)$", "</p>");
    }

    /**
     * //20161205之前将《span style="text-decoration: underline"》替换为<underline>,后将其替换为<u></u>,
     *
     * @param node
     * @param stringBuilder
     */
    private static void convert2MobileLayout(Node node, StringBuilder stringBuilder) {
        if (node instanceof Element) {
            final Element element = (Element) node;
            final String tagName = element.tagName().toLowerCase();
            if (tagName.equalsIgnoreCase(HTML_TAG_BR)) {//br
                stringBuilder.append("<br/>");
            } else if (tagName.equalsIgnoreCase(HTML_TAG_IMG)) {
                stringBuilder.append(element.outerHtml());
            } else if (tagName.equalsIgnoreCase(HTML_TAG_P)) {
                stringBuilder.append("<p>");
                final List<Node> children = node.childNodes();
                for (Node sub : children) {
                    convert2MobileLayout(sub, stringBuilder);
                }
                stringBuilder.append("</p>");
            } else if (tagName.equalsIgnoreCase(HTML_TAG_SPAN)) {
                String span1 = element.attr("style");
                if (span1 != null) {
                    String tmp = span1.replaceAll(" ", "").replaceAll(" ", "");
                    //span包含underline字符串说明是下划线
                    if (tmp.indexOf("underline") > -1) {
                        stringBuilder.append("<u>");
                        final List<Node> children = node.childNodes();
                        for (Node sub : children) {
                            if (sub instanceof TextNode) {//如果是文本节点,则特殊处理
                                TextNode textNode = (TextNode) sub;
                                //把&nbsp;替换为_
                                String text = textNode.getWholeText().replaceAll(" ", "").replaceAll("\u00a0", "_");
                                if (StringUtils.isBlank(text)) {
                                    text = "___";
                                }
                                stringBuilder.append(text);
                            } else {
                                convert2MobileLayout(sub, stringBuilder);
                            }
                        }
                        stringBuilder.append("</u>");
                    } else {
                        //不包含underline字符串的处理方法跟p标签的相同
//                        stringBuilder.append("<p>");
                        final List<Node> children = node.childNodes();
                        for (Node sub : children) {
                            convert2MobileLayout(sub, stringBuilder);
                        }
//                        stringBuilder.append("</p>");
                    }
                }
            } else if (tagName.equalsIgnoreCase(HTML_TAG_U)) {
                stringBuilder.append("<u>");
                final List<Node> children = node.childNodes();
                for (Node sub : children) {
                    convert2MobileLayout(sub, stringBuilder);
                }
                stringBuilder.append("</u>");
            } else if (tagName.equalsIgnoreCase(HTML_TAG_STRONG)) {
                stringBuilder.append("<strong>");
                final List<Node> children = node.childNodes();
                for (Node sub : children) {
                    convert2MobileLayout(sub, stringBuilder);
                }
                stringBuilder.append("</strong>");
            } else {//不识别的,则不添加任何标签
                final List<Node> children = node.childNodes();
                for (Node sub : children) {
                    convert2MobileLayout(sub, stringBuilder);
                }
            }
        } else if (node instanceof TextNode) {
            stringBuilder.append(((TextNode) node).getWholeText());
        } else if (node instanceof Comment) {//注释
            final Comment comment = (Comment) node;
            //<!--[img]34a88f0a1509b896e38a8e68e603a9765310bf10.png[/img]-->
            String data = comment.getData();
            if (data.startsWith("[img")) {
                stringBuilder.append(processImage(data));
            }
        }
    }

//    private static String processImage(String data) {
//        StringBuilder builder = new StringBuilder(data);
//        Pattern pattern = Pattern.compile("\\[img([^\\]]*)\\]([^\\[]+)\\[/img\\]");
//        Matcher matcher = pattern.matcher(builder);
//        int i = 0;
//        while (matcher.find(i)) {
//            String style = matcher.group(1);
//            String image = matcher.group(2);
//            String imgUrl = imgprefix + image.charAt(0) + "/" + image;
//            if(StringUtils.isBlank(style)){
//                final ImagePlus imagePlus = ImageUtil.parse(imgUrl);
//                final int height = imagePlus.getHeight();
//                final int width = imagePlus.getWidth();
//                style = " style=\"width:"+width+";height:"+height+"\"";
//            }
//            String imgContent = "<img src =\""+ imgUrl+"\"" +style+"/>";
//            builder.replace(matcher.start(),matcher.end(),imgContent);
//            i = matcher.start();
//        }
//        return builder.toString();
//    }


    private static String processImage(String data) {
        StringBuilder builder = new StringBuilder(data);
        Pattern pattern = Pattern.compile("\\[img([^\\]]*)\\]([^\\[]+)\\[/img\\]");
        Matcher matcher = pattern.matcher(builder);
        int i = 0;
        while (matcher.find(i)) {
            String styleAdapt = "";
            String style = matcher.group(1);
            String image = matcher.group(2);
            String imgUrl = imgprefix + image.charAt(0) + "/" + image;
            if (StringUtils.isBlank(style)) {
                final ImagePlus imagePlus = ImageUtil.parse(imgUrl);
                final int height = imagePlus.getHeight();
                final int width = imagePlus.getWidth();
                style = " width=\"" + width + "\" height=\"" + height + "\" ";
                styleAdapt = " style=\"width:" + width + ";height:" + height + "" + "\" ";
            } else {
                String width = getWidth(style);
                String height = getHeight(style);
                style = " width=\"" + width + "\" height=\"" + height + "\" ";
                styleAdapt = " style=\"width:" + width + ";height:" + height + "" + "\" ";
            }
            String imgContent = "<img src=\"" + imgUrl + "\"" + style + " " + styleAdapt + " />";
            builder.replace(matcher.start(), matcher.end(), imgContent);
            i = matcher.start();
        }
        return builder.toString();
    }

    private static String getWidth(String style) {
        StringBuilder builder = new StringBuilder(style);
        Pattern pattern = Pattern.compile("width:\\s*(\\d*)px");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String getHeight(String style) {
        StringBuilder builder = new StringBuilder(style);
        Pattern pattern = Pattern.compile("height:\\s*(\\d*)px");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    @Override
    public int getRecommendedTime(List<Integer> ids) {
        List<Question> questions = findBath(ids);

        for (Question question : questions) {
            if (question instanceof GenericQuestion) {
                GenericQuestion genericQuestion = (GenericQuestion) question;
                int recommendedTime = genericQuestion.getRecommendedTime();
                if (recommendedTime == 0) { //试题推荐用时为0
                    genericQuestion.setRecommendedTime(genericQuestion.getDifficult() * 10);
                }
            }
        }

        return questions.stream().filter(question -> question instanceof GenericQuestion)
                .mapToInt(q -> ((GenericQuestion) q).getRecommendedTime()).sum();
    }

    @Override
    public List<Question> findBatchV3(List<Integer> idList) {
        //long start = System.currentTimeMillis();
        if (idList == null || idList.size() == 0) {
            return Lists.newArrayList();
        }
        List<Question> results = new LinkedList<>();
        //查询试题列表
        List<Question> questionList = questionDao.findBath(idList);
        //   logger.info("zhouwei20181614："+(System.currentTimeMillis()-start));
        for (Question question : questionList) {
            if (question != null) {
                question = fillQuestion(question);
                handlerQuestionKnowledge(question);
                results.add(question);
            }
        }
        //调整试题顺序按照低顺序排列
        Map<Integer, Question> questionMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(results)) {
            for (Question question : results) {
                questionMap.put(question.getId(), question);
            }
        }
        List<Question> sortedResults = new LinkedList<>();
        idList.forEach(qid -> sortedResults.add(questionMap.get(qid)));
        //   logger.info("zhouwei20181615："+(System.currentTimeMillis()-start));
        return sortedResults;
    }

    Question fillQuestion(Question question) {
        if (question != null) {
            if (question instanceof GenericQuestion) {
                //普通试题添加meta信息
                GenericQuestion genericQuestion = (GenericQuestion) question;
                genericQuestion.setMeta(findMeta(genericQuestion));
                if (StringUtils.isBlank(((GenericQuestion) question).getExtend())) {
                    //处理扩展字段
                    ((GenericQuestion) question).setExtend("");
                }
            }
            question = fillChildrensV3(question);
        }
        return question;
    }


    private Question fillChildrensV3(Question question) {
        if (question instanceof CompositeQuestion) {
            CompositeQuestion compositeQuestion = (CompositeQuestion) question;

            //过滤null
            List<Question> childrens = findBatchV3(compositeQuestion.getQuestions())
                    .stream()
                    .filter(q -> q != null)
                    .collect(Collectors.toList());

            compositeQuestion.setChildrens(childrens);

            return compositeQuestion;
        } else if (question instanceof CompositeSubjectiveQuestion) {
            CompositeSubjectiveQuestion compositeSubjectiveQuestion = (CompositeSubjectiveQuestion) question;

            List<Question> childrens = findBatchV3(compositeSubjectiveQuestion.getQuestions())
                    .stream()
                    .filter(q -> q != null)
                    .collect(Collectors.toList());

            compositeSubjectiveQuestion.setChildrens(childrens);
            return compositeSubjectiveQuestion;
        }
        return question;
    }

    @Override
    public PageUtil<Question> findByConditionV3(Integer type, Integer difficult, Integer mode, String points, String content, String ids, Integer page, Integer pageSize, String subject) {
        logger.info("=======mongo查询开始=====");
        Stopwatch stopwatch = Stopwatch.createStarted();
        PageUtil result = new PageUtil();

//        List<Integer> subjectIdList = new ArrayList<>();
//        if (StringUtils.isNotEmpty(subject)) {
//            String[] subjectArray = subject.split(",");
//            for (String str : subjectArray) {
//                Integer id = Ints.tryParse(str);
//                subjectIdList.add(id);
//            }
//        }
        /**
         * 不是复合题目
         */
        if (type != 2) {
            PageUtil<Question> byConditionV3 = questionDao.findByConditionV3(type, difficult, mode, points, content, ids, page, pageSize, subject);
            List<Question> questions = new ArrayList<>();
            if (null != byConditionV3) {
                questions = buildMultiByCondition((List<Question>) byConditionV3.getResult(), ids, difficult);
            }
            result.setResult(questions);
            /**
             * 是复合题目
             */
        } else if (type == 2) {

            //查询所有符合条件的复合题目id
            List<Map> multiList = questionDao.findMultiV3(difficult, mode, points, content, ids, subject);
            if (CollectionUtils.isNotEmpty(multiList)) {
                result.setTotal(multiList.size());
                result.setTotalPage(multiList.size() / pageSize);
                List<Map> currentPage = multiList.subList((page - 1) * pageSize, Math.min(page * pageSize, multiList.size()));
                ArrayList pIdList = new ArrayList();
                if (CollectionUtils.isNotEmpty(currentPage)) {
                    for (Map multi : currentPage) {
                        pIdList.add(multi.get("parent"));
                    }
                    List<Question> bath = questionDao.findBath(pIdList);
                    if (CollectionUtils.isNotEmpty(bath)) {
                        for (Question question : bath) {
                            handlerQuestionKnowledge(question);
                        }
                    }
                    bath = buildMultiByCondition(bath, ids, difficult);

                    result.setResult(bath);
                }

            }
        }
        logger.info("=======mongo查询用时=====" + String.valueOf(stopwatch.stop()));
        return result;

    }

    @Override
    public List<Question> findBathWithFilter(List<Integer> questionIds) {
        List<ReflectQuestion> reflectQuestions = reflectQuestionDao.findByIds(questionIds);
        if(CollectionUtils.isNotEmpty(reflectQuestions)){
            questionIds.addAll(reflectQuestions.stream().map(ReflectQuestion::getNewId).collect(Collectors.toList()));
            questionIds = questionIds.stream().distinct().collect(Collectors.toList());
        }
        List<Question> questionList = questionDao.findBath(questionIds);
        return questionList;
    }

    /**
     * 根据条件过滤子题()
     */
    private List<Question> buildMultiByCondition(List<Question> list, String ids, Integer difficult) {
        //处理试题实体对象
        List<Question> questionList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            for (Question question : list) {
                question = fillQuestion(question);
                /**
                 * 如果是复合题，过滤掉子题中的已选题目
                 */
                //1.已选题目id的list
                String[] idArray = ids.split(",");
                List idList = new ArrayList();
                for (String str : idArray) {
                    Integer id = Ints.tryParse(str);
                    idList.add(id);
                }
                //2.1判断子题的id在已选题目id中，从子题列表中remove
                //2.2如果删选了难度级别，把难度级别不对的过滤掉
                if (CollectionUtils.isNotEmpty(question.getChildrens()) && CollectionUtils.isNotEmpty(idList)) {
                    List<Question> children = new LinkedList<>();
                    for (Question child : question.getChildrens()) {
                        if (!idList.contains(child.getId())) {
                            if (difficult != null && difficult > 0 && difficult == child.getDifficult()) {
                                children.add(child);
                            } else if (difficult == 0) {
                                children.add(child);
                            }
                        }
                    }
                    question.setChildrens(children);
                }
                questionList.add(question);
            }
        }
        return questionList;
    }

    /**
     * 使用关联的映射id查询的数据补偿旧ID试题数据
     *
     * @param question
     * @param questionId
     */
    public void translateQuestion(Question question, int questionId) {
        if (null == question) {
            ReflectQuestion reflectQuestion = reflectQuestionDao.findById(questionId);
            if (null == reflectQuestion) {
                return;
            }
            int newId = reflectQuestion.getNewId();
            question = findBySingleId(newId);
            if (null != question) {
                question.setId(newId);
            }
        }
    }

    /**
     * 批量补偿旧ID试题数据
     * 第一个参数为根据ID在`ztk_question_new`中查询出的所有 question 信息，
     * 第二个参数为用以查询的`question`信息
     * 如果传入 120个ID 只查询出119个试题信息 则需要考虑试题ID 是否被替换，进入数据补偿策略
     *
     * @param list        能直接查到试题的试题集合
     * @param questionIds 需要查询的试题ID集合
     */
    public void translateQuestions(List<Question> list, List<Integer> questionIds) {
        if (CollectionUtils.isEmpty(questionIds) || (CollectionUtils.isNotEmpty(list) && list.size() == questionIds.size())) {
            return;
        }
        //有效的试题ID(可以直接查到试题)
        List<Integer> validateIdList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(list)) {
            validateIdList.addAll(list.stream()
                    .map(Question::getId)
                    .distinct()
                    .collect(Collectors.toList()));
        }
        List<Integer> oldIds = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(validateIdList)) {
            //无效的试题ID(无法直接查到试题)
            oldIds.addAll(questionIds.stream().filter(questionId -> !validateIdList.contains(questionId)).collect(Collectors.toList()));
            oldIds.addAll(list.stream().filter(i -> i.getStatus() == QuestionStatus.DELETED).map(Question::getId).collect(Collectors.toList()));
        } else {
            //当前的所有试题ID 都无法查询到信息
            oldIds.addAll(questionIds);
        }
        //无效试题ID不存在直接跳出逻辑
        if (CollectionUtils.isEmpty(oldIds)) {
            return;
        }
        List<ReflectQuestion> reflectQuestions = reflectQuestionDao.findByIds(oldIds);
        if (CollectionUtils.isEmpty(reflectQuestions)) {
            return;
        }
        /**
         * 正式整合两种类型的试题
         */
        Map<Integer, Question> questionMap = Maps.newHashMap(); //整合所有试题跟原有搜索试题ID集合之间的对应关系
        if (CollectionUtils.isNotEmpty(list)) {
            for (Question question : list) {
                questionMap.put(question.getId(), question);
            }
        }
        //替换的ID 对应的新的试题信息
        List<Integer> newIds = reflectQuestions.stream()
                .map(ReflectQuestion::getNewId)
                .collect(Collectors.toList());
        //替换掉的试题集合
        List<Question> newQuestions = findBathSingle(newIds);
        if (CollectionUtils.isEmpty(newQuestions)) {          //如果被替换的试题ID也没有数据，不做后面的处理了
            logger.error("find questions error! questionIds = {}", newIds);
            return;
        }

        for (Question question : newQuestions) {
            int newId = question.getId();  //用来查询的替代ID
            Optional<ReflectQuestion> reflectQuestion = reflectQuestions.stream()
                    .filter(reflect -> reflect.getNewId().equals(newId))
                    .findFirst();
            if (reflectQuestion.isPresent()) { //真实的查询ID
                Integer oldId = reflectQuestion.get().getOldId();
                question.setId(oldId);
                questionMap.put(oldId, question);
            }
        }
        //按试题ID原有的顺序，排列可以查询到值得试题信息
        List<Question> finalListData = questionIds.stream()
                .map(questionId -> questionMap.getOrDefault(questionId, null))
                .filter(map -> null != map)
                .collect(Collectors.toList());
        list.clear();
        list.addAll(finalListData);
    }

}
