package com.huatu.ztk.knowledge.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.utils.encrypt.EncryptUtil;
import com.huatu.tiku.entity.download.QuestionErrorDownloadTask;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.cacheTask.util.QuestionPersistenceUtil;
import com.huatu.ztk.knowledge.cacheTask.util.RedisKnowledgeKeysAdapter;
import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
import com.huatu.ztk.knowledge.common.analysis.config.SaProperties;
import com.huatu.ztk.knowledge.daoPandora.QuestionErrorDownloadTaskMapper;
import com.huatu.ztk.knowledge.util.Crypt3Des;
import com.huatu.ztk.knowledge.util.PageUtil;
import com.huatu.ztk.paper.common.ResponseMsg;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import io.netty.util.internal.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 错题业务层
 * Created by shaojieyue
 * Created time 2016-06-15 16:30
 */

@Service
public class QuestionErrorService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionErrorService.class);
    private static final String RABBIT_TAIL_ENV;
    static {
        RABBIT_TAIL_ENV = Optional.ofNullable(System.getProperty("disconf.env"))
                .map(i -> i.equalsIgnoreCase("qa") ? "test" : "product")
                .orElse("test");
    }

    public static final String SECRET = "!@#$%^&*()qazxswedc";
    public static final int PAY_BY_COIN_SUCCESS = 1;
    public static final int PAY_BY_COIN_FAIL = -4;
    public static final long COIN_NOT_ENOUGH = -4L;//金币不足
    //    public static final String payByCoinUrl = "http://tk.htexam.com/v3/goldenPay.php";

    private static final String ERROR_MESSAGE = "本次导出有%s题因题目变更无法导出，实际可导出%s题，将消耗%s图币，是否确认使用？";
    private static final String SUCCESS_MESSAGE = "本次导出将消耗%s图币，是否确认使用？";
    @Autowired
    SaProperties payProperties;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private QuestionPersistenceUtil questionPersistenceUtil;

    @Autowired
    private PoxyUtilService poxyUtilService;

    @Autowired
    private QuestionErrorDownloadTaskMapper questionErrorDownloadTaskMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private static final Integer MAX_DOWNLOAD_SIZE = 200;

    private static final Integer FREE_DOWN_SIZE = 5;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 根据知识点,查询用户的错题列表
     *
     * @param pointId
     * @param userId
     * @return
     */
    public PageBean<Integer> findByPoint(int pointId, long userId, int end) {
        Set<Integer> set = poxyUtilService.getQuestionErrorService().getQuestionIds(pointId, userId, 0, end);

        List<Integer> questions = set.stream().collect(Collectors.toList());
        PageBean<Integer> pageBean = new PageBean<Integer>(questions, 0, questions.size());
        return pageBean;
    }

    /**
     * 查询错题知识点数
     *
     * @param userId
     * @param subject
     * @return
     */
    public List<QuestionPointTree> queryErrorPointTrees(long userId, int subject) {
        poxyUtilService.getQuestionErrorService().checkErrorPointRedis(userId, subject);     //检查错题本缓存数据是否有问题
        return poxyUtilService.getQuestionErrorService().queryErrorPointTrees(userId, subject);
    }


    /**
     * 删除错题
     *
     * @param qid
     * @param uid
     * @param subject
     */
    public void deleteErrorQuestion(int qid, long uid, int subject) throws BizException {
        logger.info("delete error question. qid = {}, uid = {}, subject = {}", qid, uid, subject);

        Question question = questionDubboService.findById(qid);
        final GenericQuestion genericQuestion = (GenericQuestion) question;

        //试题不存在,资源未发现
        if (genericQuestion == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        String questionIdStr = String.valueOf(genericQuestion.getId());

        //对每个知识点都进行处理
        for (Integer point : genericQuestion.getPoints()) {
            //做错的
            boolean changeFlag = isChangeForQuestionPoint(uid, point, questionIdStr);
            if (!changeFlag) {
                deleteErrorQuestionCache(uid, point, questionIdStr);
            } else {
                deleteWrongCacheById(qid, uid);
                break;
            }
        }
    }

    /**
     * 异步删除错题集中的试题（遍历所有知识点）
     *
     * @param qid
     * @param uid
     */
    @Async
    public void deleteWrongCacheById(int qid, long uid) {

        Map<Integer, Integer> entries = poxyUtilService.getQuestionErrorService().countAll(uid);
        if (null == entries || entries.isEmpty()) {
            return;
        }
        String questionIdStr = String.valueOf(qid);
        entries.entrySet().stream().map(Map.Entry::getKey)
                .filter(i -> !isChangeForQuestionPoint(uid, i, questionIdStr))
                .forEach(i -> deleteErrorQuestionCache(uid, i, questionIdStr));
    }

    /**
     * 判断试题是否切换了知识点
     *
     * @param uid
     * @param point
     * @param questionIdStr
     * @return false 试题 在
     */
    private boolean isChangeForQuestionPoint(long uid, Integer point, String questionIdStr) {
        return poxyUtilService.getQuestionErrorService().isExist(uid, point, Integer.parseInt(questionIdStr));
    }

    /**
     * 在某个知识点下删除试题ID,并重新计数
     *
     * @param uid
     * @param point
     * @param questionIdStr
     */
    private void deleteErrorQuestionCache(long uid, Integer point, String questionIdStr) {

        poxyUtilService.getQuestionErrorService().deleteQuestion(uid, point, Integer.parseInt(questionIdStr));
        poxyUtilService.getQuestionErrorService().deleteLookMode(uid, point, Integer.parseInt(questionIdStr));
        String errorSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(uid, point);
        /**
         * 缓存需要持化的key 值信息
         * add by lijun 2018-03-20
         */
        questionPersistenceUtil.addWrongQuestionPersistence(errorSetKey);
    }

    public PageUtil findByPointV2(int pointId, long userId, int page, int pageSize) {
        final String wrongSetKey = RedisKnowledgeKeysAdapter.getInstance().getWrongSetKey(userId, pointId);


        //v1 查询所有收藏题  ---》 v2分页查询收藏题
        int start = (page - 1) * pageSize;
        int end = pageSize * page - 1;


        final Set<Integer> set = poxyUtilService.getQuestionErrorService().getQuestionIds(pointId, userId, start, end);
        int size = poxyUtilService.getQuestionErrorService().count(userId, QuestionPoint.builder().id(pointId).build());
        PageUtil.PageUtilBuilder<Object> builder = PageUtil.builder()
                .next((size > end) ? 1 : 0)
                .total(size);
        builder.result(set.stream().collect(Collectors.toList()));

        return builder.build();
    }


    /**
     * 清理错题本
     *
     * @param userId
     * @param subject
     */
    public void clearErrorQuestion(long userId, int subject) throws BizException {
        poxyUtilService.getQuestionErrorService().clearAll(userId, subject);
    }

    private List<Integer> getPointId(List<QuestionPointTree> countPointTrees) {
        ArrayList<Integer> ids = Lists.newArrayList();
        for (QuestionPointTree countPointTree : countPointTrees) {
            ids.add(countPointTree.getId());
            if (CollectionUtils.isNotEmpty(countPointTree.getChildren())) {
                ids.addAll(getPointId(countPointTree.getChildren()));
            }
        }
        return ids;
    }

    /**
     * 创建下载任务
     *
     * @param uid
     * @param userName
     * @param pointIds
     * @param subject
     * @param subjectTree
     * @return
     */
    public Object createDownloadTask(long uid, String userName, String pointIds, int subject, SubjectTree subjectTree) throws BizException {

        List<GenericQuestion> questions = preDownList(uid, pointIds, subject);
        List<QuestionErrorDownloadTask> list = Lists.newArrayList();
        Map<String, Object> retMap = Maps.newHashMap();
        int coin = count(uid, questions);
        Long orderNum = payCoin(uid, userName, coin, questions);
        if (orderNum == COIN_NOT_ENOUGH) {
            retMap.put("payStatus", PAY_BY_COIN_FAIL);
            retMap.put("list", list);
            return retMap;
        }
        //拆分任务
        boolean topFlag = true;
        int index = 0;
        while (true) {
            String collect = questions.stream().skip(index * MAX_DOWNLOAD_SIZE).limit(MAX_DOWNLOAD_SIZE)
                    .map(Question::getId).map(String::valueOf).collect(Collectors.joining(","));
            if (StringUtils.isBlank(collect)) {
                break;
            }
            QuestionErrorDownloadTask task = QuestionErrorDownloadTask.builder().build();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("错题下载-")
                    .append(subjectTree.getName()).append(questions.size()).append("题");
            if (questions.size() > MAX_DOWNLOAD_SIZE) {
                stringBuilder.append((char) ('A' + index));
            }
            stringBuilder.append("-")
                    .append(DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmm"));
            task.setUserId(uid);
            task.setNum(Optional.ofNullable(collect).map(i->i.split(",")).map(i->i.length).orElse(0));
            task.setName(stringBuilder.toString());
            task.setSum(questions.size());
            task.setQuestionIds(collect);
            task.setGmtCreate(new Timestamp(System.currentTimeMillis()));
            task.setSubject(subjectTree.getId());
            task.setStatus(1);
            task.setOrderNum(orderNum);
            if (topFlag) {
                task.setTotal(new Long(coin));
                topFlag = false;
            } else {
                task.setTotal(0L);
            }
            logger.info("insert task = {}", JsonUtil.toJson(task));
            questionErrorDownloadTaskMapper.insertSelective(task);
            list.add(task);
            rabbitTemplate.convertAndSend("", "question_error_download_task_" + RABBIT_TAIL_ENV, task);
            index++;
        }
        retMap.put("payStatus", PAY_BY_COIN_SUCCESS);
        retMap.put("list", list);
        return retMap;
    }

    private int count(long uid, List<GenericQuestion> questions) {
        List<Integer> questionIds = questions.stream().map(i -> i.getId()).collect(Collectors.toList());
        long l = filterQuestion(questionIds, uid);
        int size = questions.size();
        int coin = (int) Math.max(size - FREE_DOWN_SIZE - l, 0);
        return coin;
    }

    private List<GenericQuestion> preDownList(long uid, String pointIds, int subject) throws BizException {
        List<Integer> points = splitPointIds(pointIds);
        int sum = 0;
        List<Integer> questionIds = Lists.newArrayList();
        if (CollectionUtils.isEmpty(points)) {
            List<QuestionPointTree> questionPointTrees = poxyUtilService.getQuestionErrorService().queryErrorPointTrees(uid, subject);
            sum = questionPointTrees.stream().mapToInt(QuestionPointTree::getWnum).sum();
            questionIds.addAll(questionPointTrees.stream().map(QuestionPointTree::getId)
                    .flatMap(i -> poxyUtilService.getQuestionErrorService().getQuestionIds(i, uid).stream())
                    .collect(Collectors.toList()));
        } else {
            Map<Integer, Integer> countMap = poxyUtilService.getQuestionErrorService().countAll(uid);
            sum = points.stream().mapToInt(countMap::get).sum();
            questionIds.addAll(points.stream()
                    .flatMap(i -> poxyUtilService.getQuestionErrorService().getQuestionIds(i, uid).stream())
                    .collect(Collectors.toList()));
        }

        if (CollectionUtils.isEmpty(questionIds)) {
            throw new BizException(ErrorResult.create(1231000, "错题本数据缺失"));
        }

        List<GenericQuestion> questions = handlerQuestions(questionIds);
        logger.info("create task result:uid={},pointIds={},sum={},questionId={}", uid, pointIds, questions.size(), questionIds);
        return questions;
    }

    private List<Integer> splitPointIds(String pointIds) {
        ArrayList<Integer> points = Lists.newArrayList();
        if (StringUtils.isNotBlank(pointIds) && !"-1".equals(pointIds)) {
            List<Integer> collect = Arrays.stream(pointIds.split(","))
                    .filter(NumberUtils::isDigits)
                    .map(Integer::parseInt)
                    .filter(i -> i > 0)
                    .collect(Collectors.toList());
            points.addAll(collect);
        }
        return points;
    }

    private Long payCoin(long uid, String userName, int coin, List<GenericQuestion> questions) throws BizException {
        if (coin <= 0) {        //免费下载
            return -1l;
        }
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
        Random r = new Random();
        String format = date.format(new Date());
        long orderNum = Long.parseLong(format) * 10000 + r.nextInt(1000);
        int payByCoin = payByCoin(orderNum, userName, coin);
        if (PAY_BY_COIN_SUCCESS == payByCoin) {
            addFreeQuestionId(uid, questions.stream().map(Question::getId).collect(Collectors.toList())); //支付成功后，将付费过的题目ID加入免费队列管理
            return orderNum;
        } else if (PAY_BY_COIN_FAIL == payByCoin) {
            logger.info("当前账户图币余额不足");
            return COIN_NOT_ENOUGH;
            //throw new BizException(ErrorResult.create(1000513, "账户图币余额不足"));
        } else {
            logger.info("图币支付异常");
            throw new BizException(ErrorResult.create(1000513, "图币支付异常"));
        }
    }

    private void addFreeQuestionId(long uid, List<Integer> questionIds) {
        String wrongDownloadIds = RedisKnowledgeKeys.getWrongDownloadIds(uid);
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        String[] ids = questionIds.stream().map(String::valueOf).toArray(i -> new String[i]);
        setOperations.add(wrongDownloadIds, ids);
    }

    @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(value = 1000))
    public int payByCoin(long orderNum, String userName, int total) {
        RestTemplate restTemplate = new RestTemplate();

        //进入金币支付环节
        String url = payProperties.getPayByCoinUrl();
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String encodeUserName = "";
        try {
            encodeUserName = URLEncoder.encode(userName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodeUserName = userName;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("gold=" + total);
        sb.append("&ordernum=" + orderNum);
        sb.append("&timestamp=" + timestamp);
        sb.append("&useType=" + 2);     //错题下载金币消费类型
        sb.append("&username=" + encodeUserName);
        sb.append(SECRET);
        logger.info(sb.toString());
        String sign = EncryptUtil.md5(sb.toString());

        StringBuffer sbSign = new StringBuffer();
        sbSign.append("useType=" + 2);
        sbSign.append("&username=" + encodeUserName);
        sbSign.append("&ordernum=" + orderNum);
        sbSign.append("&gold=" + total);
        sbSign.append("&timestamp=" + timestamp);
        sbSign.append("&sign=" + sign);
        logger.info("sbSign:{}", sbSign);
        String p = Crypt3Des.encryptMode(sbSign.toString());

        /**
         * 1 :支付成功
         * -4 ：账户金币余额不足
         */
        logger.info("金币支付：发送get请求，url = {}", url + "?p=" + p);
        ResponseEntity<ResponseMsg> forEntity = restTemplate.getForEntity(url + "?p=" + p, ResponseMsg.class);
        ResponseMsg body = forEntity.getBody();
        if (null != body) {
            logger.info("get 请求发送成功");
            System.out.println("body = " + JsonUtil.toJson(body));
            return body.getCode();
        } else {
            return -1;
        }

    }

    private List<GenericQuestion> handlerQuestions(List<Integer> questionIds) {
        int index = 0;
        List<Question> questions = Lists.newArrayList();
        while (true) {
            List<Integer> ids = questionIds.stream().skip(index * 100).limit(100).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            List<Question> bath = questionDubboService.findBath(ids);
            if (CollectionUtils.isNotEmpty(bath)) {
                questions.addAll(bath.stream().filter(i -> i.getStatus() != 4).distinct().collect(Collectors.toList()));
            }

            index++;
        }
        Map<Integer, List<GenericQuestion>> groupMap = questions
                .stream()
                .filter(i -> i instanceof GenericQuestion)
                .map(i -> (GenericQuestion) i)
                .collect(Collectors.groupingBy(i -> i.getPoints().get(0)));
        List<GenericQuestion> results = groupMap.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .flatMap(i -> i.getValue().stream())
                .distinct()
                .collect(Collectors.toList());
        return results;
    }

    /**
     * 预创建下载任务
     *
     * @param uid
     * @param pointIds
     * @param subject
     * @param num
     * @return
     */
    public Object createPreDownloadInfo(long uid, String pointIds, int subject, int num) throws BizException {
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("uid", uid);
        map.put("pointIds", pointIds);
        map.put("subject", subject);
        List<GenericQuestion> genericQuestions = preDownList(uid, pointIds, subject);
        long l = filterQuestion(genericQuestions.stream().map(i -> i.getId()).collect(Collectors.toList()), uid);
        int size = genericQuestions.size();
        int coin = (int) Math.max(size - FREE_DOWN_SIZE - l, 0);
        map.put("num", size);
        map.put("coin", coin);
        if (num != size && num > 0) {
            int abs = Math.abs(num - size);
            map.put("message", String.format(ERROR_MESSAGE, abs, size, coin));
        } else {
            map.put("message", String.format(SUCCESS_MESSAGE, coin));
        }
        return map;
    }

    private long filterQuestion(List<Integer> questionIds, long uid) {
        String wrongDownloadIds = RedisKnowledgeKeys.getWrongDownloadIds(uid);
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        if (redisTemplate.hasKey(wrongDownloadIds)) {
            Set<String> members = setOperations.members(wrongDownloadIds);
            long count = members.stream().map(Integer::parseInt).filter(i -> questionIds.contains(i)).count();
            return count;
        }
        Example example = new Example(QuestionErrorDownloadTask.class);
        example.and().andEqualTo("userId", uid);
        List<QuestionErrorDownloadTask> questionErrorDownloadTasks = questionErrorDownloadTaskMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(questionErrorDownloadTasks)) {
            String[] ids = questionErrorDownloadTasks.stream().map(QuestionErrorDownloadTask::getQuestionIds)
                    .flatMap(i -> Arrays.stream(i.split(",")).filter(NumberUtils::isDigits))
                    .distinct().toArray(i -> new String[i]);
            System.out.println("taskQuestionIds = " + Arrays.stream(ids).collect(Collectors.joining(",")));
            redisTemplate.opsForSet().add(wrongDownloadIds, ids);        //补偿错题下载过的试题数据
            redisTemplate.expire(wrongDownloadIds, 30, TimeUnit.DAYS);
            return Arrays.stream(ids).map(Integer::parseInt).filter(i -> questionIds.contains(i)).count();
        }
        return 0l;

    }


    public Object findDownList(long uid, int subject, int page, int size) {

//        Example example = new Example(QuestionErrorDownloadTask.class);
//        example.and().andEqualTo("userId", uid).andEqualTo("subject", subject).andEqualTo("status", 1);
//        int count = questionErrorDownloadTaskMapper.selectCountByExample(example);
        Map countMap = questionErrorDownloadTaskMapper.countDownList(uid, subject);
        List<HashMap> result = questionErrorDownloadTaskMapper.findDownList(uid, subject, page, size);
        for (HashMap hashMap : result) {
            hashMap.put("orderNum", hashMap.get("order_num"));
            hashMap.put("questionIds", hashMap.get("question_ids"));
            hashMap.put("answerId", hashMap.get("answer_id"));
            hashMap.put("fileUrl", hashMap.get("file_url"));
            hashMap.put("size", hashMap.getOrDefault("size", null));
            hashMap.put("fileSize", hashMap.getOrDefault("file_size", 0));
        }
        PageUtil pageUtil = convertResult(result, page, size, MapUtils.getInteger(countMap, "fileCount", 0));
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("next", pageUtil.getNext());
        map.put("result", pageUtil.getResult());
        map.put("total", pageUtil.getTotal());
        map.put("totalPage", pageUtil.getTotalPage());
        map.put("size", MapUtils.getLongValue(countMap, "fileSize", 0));
        return map;
    }

    private PageUtil convertResult(List<HashMap> result, int page, int size, int count) {
        PageUtil<List<HashMap>> pageUtil = PageUtil.<List<HashMap>>builder()
                .result(result)
                .total(count)
                .totalPage(count / size + (count % size == 0 ? 0 : 1))
                .next(page * size >= count ? 0 : 1)
                .build();
        return pageUtil;
    }

    public Object getDownDescription() {
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("maxSize", MAX_DOWNLOAD_SIZE);
        map.put("freeSize", FREE_DOWN_SIZE);
        map.put("description", Lists.newArrayList("1. 可最多免费导出5题；", "2. 5题以上1题消耗1图币;"
                , "3. 已扣除过图币的题目再次导出时不再重复扣除;", "4. 因题目变更、知识点变更等原因，导致实际导出题目数和扣除的图币数可能小于选择时，请以实际导出为准。"));
        return map;
    }

    public Object findOrderByUserId(long uid, int page, int size) {
        System.out.println("findOrderByUserId uid = " + uid);
        List<HashMap> result = questionErrorDownloadTaskMapper.findOrderByUserId(uid, page, size);
        System.out.println("JsonUtil.toJson(result) = " + JsonUtil.toJson(result));
        for (HashMap hashMap : result) {
            hashMap.put("name", "错题下载");
            String payMsg = MapUtils.getString(hashMap, "payMsg", "0");
            hashMap.put("payMsg", "-" + payMsg + "图币");
            String payTime = MapUtils.getString(hashMap, "payTime", "");
            if (StringUtils.isNotBlank(payMsg)) {
                hashMap.put("payTime", payTime.substring(0, payTime.lastIndexOf(".")));
            }
        }
        Long count = questionErrorDownloadTaskMapper.countOrderByUserId(uid);
        return convertResult(result, page, size, count.intValue());
    }

    public Object findDownInfo(Long taskId) {
        QuestionErrorDownloadTask task = questionErrorDownloadTaskMapper.selectByPrimaryKey(taskId);
        return task;
    }

    public Object deleteDownTask(long uid, List<Long> collect) throws BizException {
        Example example = new Example(QuestionErrorDownloadTask.class);
        example.and().andEqualTo("userId", uid).andIn("id", collect);
        int i = questionErrorDownloadTaskMapper.updateByExampleSelective(QuestionErrorDownloadTask.builder().status(-1).build(), example);
        if (i > 0) {
            return SuccessMessage.create("删除成功");
        }
        throw new BizException(ErrorResult.create(1000012, "删除失败"));
    }


    public void restCheckLock(long userId) {
        String wrongCountKey = RedisKnowledgeKeys.getWrongCountKey(userId) + "_check";
        redisTemplate.delete(wrongCountKey);
    }
}
