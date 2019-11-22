//package com.huatu.tiku.essay.task;
//
//
//import com.huatu.common.exception.BizException;
//import com.huatu.tiku.essay.constant.status.AdminPaperConstant;
//import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
//import com.huatu.tiku.essay.constant.status.EssayMockExamConstant;
//import com.huatu.tiku.essay.constant.status.SystemConstant;
//import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
//import com.huatu.tiku.essay.entity.EssayMockExam;
//import com.huatu.tiku.essay.vo.resp.EssayMockExamAnswerVO;
//import com.huatu.tiku.essay.vo.resp.EssayMockVO;
//import com.huatu.tiku.essay.repository.EssayMockExamRepository;
//import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
//import com.huatu.tiku.essay.service.task.AsyncMockServiceImpl;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.data.redis.core.ZSetOperations;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.TimeUnit;
//
//
//@Component
//@Slf4j
//public class MatchAutoSubmitTask {
//    private static final Logger logger = LoggerFactory.getLogger(MatchAutoSubmitTask.class);
//
//    @Autowired
//    EssayMockExamRepository essayMockExamRepository;
//    @Autowired
//    private RedisTemplate redisTemplate;
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//    @Autowired
//    private AsyncMockServiceImpl asyncTaskServiceImpl;
//    @Value("${extra_time}")
//    private long extraTime;
//    @Autowired
//    EssayPaperAnswerRepository essayPaperAnswerRepository;
////    @Value("${server_ip}")
////    private String serverIp;
//
//    @PostConstruct
//    public void init() {
//        //添加停止任务线程
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> unlock()));
//    }
//
//    @Scheduled(fixedRate = 60000)
//    public void submitMatchAnswer() throws BizException {
//        if (!getLock()) {
//            return;
//        }
//        try {
//            //查询当前正在进行的 申论模考
//
//            List<EssayMockExam> mockExamList = essayMockExamRepository.findCurrent();
//
//            if (CollectionUtils.isNotEmpty(mockExamList)) {
//                String serverIp = getServerIp();
//                logger.info("auto submit report answer task start.server={}", serverIp);
//                for (EssayMockExam mockExam : mockExamList) {
//                    if (null != mockExam) {
//                        log.info("当前进行的模考id:" + mockExam.getId());
//                        //存在正在进行中的考试 并且当前时间大于等于结束时间（已结束）
//                        if (System.currentTimeMillis() >= mockExam.getEndTime().getTime()+TimeUnit.MINUTES.toMillis(2)) {
//                            log.info("模考结束,自动提交答案 {}",mockExam.getId());
//                            autoCommit(mockExam);
//                        }
//                    }
//                }
//                logger.info("auto submit report answer task end.server={}", serverIp);
//            }
//
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } finally {
//            unlock();
//        }
//    }
//
//    //强制交卷
//    public void autoCommit(EssayMockExam mockExam) {
//        //1.处理模考状态
//
//        //考试结束不超过15分钟并且当前状态不是已结束或者已完成 则 把模考状态置为已结束
//        if( System.currentTimeMillis() >= mockExam.getEndTime().getTime() &&
//                System.currentTimeMillis() <= mockExam.getEndTime().getTime() + TimeUnit.MINUTES.toMillis(extraTime - 1) && mockExam.getBizStatus()!=EssayMockExamConstant.EssayMockExamBizStatusEnum.FINISHED.getBizStatus()  && mockExam.getBizStatus()!= EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus()){
//            log.debug("定时任务扫描。考试结束 {}",mockExam.getId());
//
//            mockExam.setBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.FINISHED.getBizStatus());
//            essayMockExamRepository.save(mockExam);
//        }
//        //考试结束超过15分钟，模考状态置为已完成
//        if( System.currentTimeMillis() >= mockExam.getEndTime().getTime() + TimeUnit.MINUTES.toMillis(extraTime - 1)){
//            //如果模考状态还未变更成  已完成，修改模考信息
//            if (EssayMockExamConstant.EssayMockExamBizStatusEnum.FINISHED.getBizStatus() == mockExam.getBizStatus()) {
//                mockExam.setBizStatus(EssayMockExamConstant.EssayMockExamBizStatusEnum.COMPLETED.getBizStatus());
//                log.info("定时任务扫描。考试完成 {}", mockExam.getId());
//                //填充报名人数   实际考试人数  最高分 平均分
//                updateMockExam(mockExam);
//                essayMockExamRepository.save(mockExam);
//                String mockDetailKey = RedisKeyConstant.getMockDetailPrefix(mockExam.getId());
//                redisTemplate.opsForValue().set(mockDetailKey, mockExam);
//                return;
//            }
//        }
//        log.debug("判断模考是否有未交卷的答题卡");
//        long paperId = mockExam.getId();
//        //2.如果有未交卷的答题卡
//        //学员答题卡状态的set
//        String userAnswerStatusKey = RedisKeyConstant.getUserAnswerStatusKey(paperId);
//        Map<Object, Object> map = redisTemplate.opsForHash().entries(userAnswerStatusKey);
//        //  log.info("判断模考是否有未交卷的答题卡，用户答题卡状态{}"+map);
//
//        //答题卡id set为空或者模考大赛未结束
//        if (map != null) {
//            for (Map.Entry entry : map.entrySet()) {
//                int userId = Integer.parseInt(entry.getKey().toString());
//                int status = Integer.parseInt(entry.getValue().toString());
//                log.info("userId" + userId + "对应的答题卡状态status" + status);
//                //用户未交卷，自动提交，将用户数据放入三方公用的set中
//                if (EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus() == status || EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus() == status) {
//                    log.info("userId：" + userId + "对应的答题卡为交卷状态status" + status);
//                    //修改为交卷状态
//                    redisTemplate.opsForHash().put(userAnswerStatusKey, userId + "", EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
//
//                    String examAnswerKey = RedisKeyConstant.getExamAnswerKey(paperId, userId);
//                    EssayMockExamAnswerVO essayMockExamAnswerVO = (EssayMockExamAnswerVO) redisTemplate.opsForValue().get(examAnswerKey);
//
//                    EssayMockVO essayMockVO = EssayMockVO.builder()
//                            .answerCardId(essayMockExamAnswerVO.getEssayPaperAnswer().getId())
//                            .paperId(paperId)
//                            .examType(AdminPaperConstant.MOCK_PAPER)
//                            .mockRedisKey(examAnswerKey)
//                            .userId(userId)
//                            .build();
//
//                    log.info("=====进入批改试卷接口【模考】，发送消息到消息队列:" + essayMockVO + "=========");
//                    rabbitTemplate.convertAndSend(SystemConstant.MOCK_ANSWER_CORRECT_ROUTING_KEY, essayMockVO);
//                    //将userId放入三方公用的set中
//                    String publicUserSetPrefix = RedisKeyConstant.getPublicUserSetPrefix(paperId);
//                    redisTemplate.opsForSet().add(publicUserSetPrefix, userId);
//                    //2-MQ:将答题数据持久化存入MySQL
//                    asyncTaskServiceImpl.saveMockAnswerToMySql(examAnswerKey);
//                }
//            }
//
//        }
//    }
//
//    private String getServerIp() throws UnknownHostException {
//        try {
//            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//            while (networkInterfaces.hasMoreElements()) {
//                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
//                Enumeration<InetAddress> nias = ni.getInetAddresses();
//                while (nias.hasMoreElements()) {
//                    InetAddress ia = (InetAddress) nias.nextElement();
//                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
//                        return ia.getHostAddress();
//                    }
//                }
//            }
//        } catch (SocketException e) {
//        }
//        return null;
//    }
//
//    private EssayMockExam updateMockExam(EssayMockExam mockExam) {
//
//        log.info("考试结束，持久化数据" + mockExam.getId());
//        //计算平均分
//        //1.从缓存中取出总分
//        String essayScoreSumKey = RedisKeyConstant.getEssayScoreSumKey(mockExam.getId());
//        Object object = redisTemplate.opsForValue().get(essayScoreSumKey);
//        Double scoreSum = (object == null ? 0 : Double.parseDouble(object.toString()));
//        //从缓存中取出分数的Zset
//        String essayUserScoreKey = RedisKeyConstant.getEssayUserScoreKey(mockExam.getId());
//        //参加考试人数
//        Long size = redisTemplate.opsForZSet().size(essayUserScoreKey);
//        log.info("participate essay mock exams:size:{}->{}", essayUserScoreKey, size);
//        if (0 == size) {
//            size = 1L;
//        }
//        //最高分
//        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
//        Set<ZSetOperations.TypedTuple<Object>> withScores =
//                zSetOperations.reverseRangeWithScores(essayUserScoreKey, 0, 0);
//        double maxScore = 0;
//        if (CollectionUtils.isNotEmpty(withScores)) {
//            maxScore = new ArrayList<>(withScores).get(0).getScore();
//            log.info("cone:zset:add:{}->{}", essayUserScoreKey, maxScore);
//        }
//
//        //计算平均分
//        String avgScoreStr = String.format("%.1f", scoreSum / size);
//        double avgScore = Double.parseDouble(avgScoreStr);
//
//
//        int enrollCount = 0;
////        enrollCount = essayPaperAnswerRepository.countByPaperBaseIdAndTypeAndStatus(mockExam.getId(), AdminPaperConstant.MOCK_PAPER, 1);
//
//        String totalEnrollCountKey = RedisKeyConstant.getTotalEnrollCountKey(mockExam.getId());
//
//        Object enrollCountObj = redisTemplate.opsForValue().get(totalEnrollCountKey);
//        if (null != enrollCountObj) {
//            enrollCount = (Integer) enrollCountObj;
//        }
//
//        // 保存最近的一次模考id
//        String lastEssayMockIdKey = RedisKeyConstant.getLastEssayMockIdKey();
//        redisTemplate.opsForValue().set(lastEssayMockIdKey, mockExam.getId());
//
//        //更新模考信息
//        mockExam.setAvgScore(avgScore);
//        mockExam.setMaxScore(maxScore);
//        mockExam.setExamCount(size.intValue());
//        mockExam.setEnrollCount(enrollCount);
//        return mockExam;
//
//    }
//
//
//    /**
//     * 释放定时任务锁
//     */
//    private void unlock() {
//        String lockKey = RedisKeyConstant.getMockAutoSubmitLockKey();
//        String currentServer = (String) redisTemplate.opsForValue().get(lockKey);
//
//        logger.info("current server={}", currentServer);
//        String serverIp = "";
//        try {
//            serverIp = getServerIp();
//            logger.info("getServerIp:" + getServerIp());
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        if (serverIp.equals(currentServer)) {
//            redisTemplate.delete(lockKey);
//
//            logger.info("release lock,server={},timestamp={}", currentServer, System.currentTimeMillis());
//        }
//    }
//
//    /**
//     * @return 是否获得锁
//     */
//    private boolean getLock() {
//        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
//
//        String lockKey = RedisKeyConstant.getMockAutoSubmitLockKey();
//        String value = opsForValue.get(lockKey);
//
//        logger.info("get lock timestamp={},value={}", System.currentTimeMillis(), value);
//        String serverIp = "";
//        try {
//            serverIp = getServerIp();
//            logger.info("getServerIp:" + getServerIp());
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//
//        if (StringUtils.isBlank(value)) { //值为空
//            boolean booleanValue = opsForValue.setIfAbsent(lockKey, serverIp).booleanValue();
//            log.info("booleanValue：{}，当前定时器被{}锁定", booleanValue, opsForValue.get(lockKey));
//            if(booleanValue){
//                redisTemplate.expire(lockKey,2,TimeUnit.MINUTES);
//            }
//            if (booleanValue || serverIp.equals(opsForValue.get(lockKey))) {
//                return true;
//            } else {
//                return false;
//            }
//
//        } else if (StringUtils.isNoneBlank(value) && !value.equals(serverIp)) {
//            //被其它服务器锁定
//            logger.info("auto submit report lock server={},return", value);
//            return false;
//        } else { //被自己锁定
//            return true;
//        }
//    }
//}
