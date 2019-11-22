//package com.huatu.ztk.knowledge.task;
//
//import com.huatu.ztk.commons.exception.BizException;
//import com.huatu.ztk.knowledge.common.DatacleanConfig;
//import com.huatu.ztk.knowledge.common.RedisKnowledgeKeys;
//import org.apache.commons.collections.CollectionUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import javax.annotation.Resource;
//import java.util.List;
//import java.util.concurrent.*;
//
///**
// * @author hanchao
// * @date 2017/10/3 17:49
// */
//@Service
//public class DataCleanTask {
//    private static final Logger log = LoggerFactory.getLogger(DatacleanConfig.class);
//    private static ThreadPoolExecutor threadPoolExecutor;
//    private static SynchronousQueue<Runnable> workQueue = new SynchronousQueue(false);
//
//    @Autowired
//    @Qualifier("redisTemplate")
//    private RedisTemplate redisTemplate;
//    @Resource(name = "redisTemplate")
//    private ValueOperations valueOperations;
//
//    @Autowired
//    public UserPointTask userPointTask;
//
//    @Autowired
//    private DatacleanConfig datacleanConfig;
//
//
//    //手动触发开关
//    @PostConstruct
//    public void init(){
//        threadPoolExecutor = new ThreadPoolExecutor(20,20,5, TimeUnit.MINUTES, workQueue, Executors.defaultThreadFactory(),new BlockingPolicy());
//        Thread monitor = new Thread() {
//            @Override
//            public void run() {
//                int uidCount = userPointTask.getUserCount();
//                while (true){
//                    String incrementKey = RedisKnowledgeKeys.getUserPointUpdateIncrementKey();//之后查询limitMax-5000到limitMax的userId
//                    ValueOperations valueOperations = redisTemplate.opsForValue();
//                    int maxLimit = 0;
//                    if(valueOperations.get( incrementKey )!=null){
//                        maxLimit = Integer.parseInt( String.valueOf( valueOperations.get( incrementKey ) ) );
//                    }
//                    if(maxLimit>=uidCount){
//                        valueOperations.set( incrementKey,"0" );
//                        log.info( "update user questions in  point all finished!" );
//                        return;
//                    }
//                    if(datacleanConfig.getBreaker() != 1){
//                        try {
//                            TimeUnit.SECONDS.sleep(5);
//                        } catch (InterruptedException e) {
//                        }
//                    }else{
//                        work();
//                    }
//                }
//            }
//        };
//        monitor.setDaemon(true);
//        monitor.start();
//    }
//
//    public void work(){
//        int subject =1;
//        //初始化需要变动的试题知识点（缓存+）
//        userPointTask.getUserPointChangeCache(subject,0,100000);
//
//        String incrementKey = RedisKnowledgeKeys.getUserPointUpdateIncrementKey();//之后查询limitMax-5000到limitMax的userId
//
//        log.info(">>> 执行任务开始:{},{}",datacleanConfig.getBreaker(), datacleanConfig.getFetchSize());
//        while(true){
//            try {
//                int fetchSize = datacleanConfig.getFetchSize();
//                long end = valueOperations.increment(incrementKey, fetchSize);
//
//                List<Integer> uids = userPointTask.getNextUserIds(end-fetchSize,fetchSize);
//
//                if(CollectionUtils.isEmpty(uids)){
//                    log.info(">>> 任务执行完毕,目前的postion是{},{}...",end-fetchSize,end);
//                    break;
//                }
//
//                for (Integer uid : uids) {
//                    threadPoolExecutor.execute( new UserDealTask(uid,subject) );
//                }
//
//                //下一次开始之前，检查开关
//                if(datacleanConfig.getBreaker() != 1){
//                    log.error(">>> 任务中断，目前的postion是{},{}...",end-fetchSize,end);
//                    break;
//                }
//
//            } catch(Exception e){
//                log.error(">>> 出现未知异常:",e);
//            }
//        }
//
//    }
//
//
//    @PreDestroy
//    public void destroy(){
//        if(threadPoolExecutor != null && !threadPoolExecutor.isShutdown()){
//            try {
//                threadPoolExecutor.shutdown();
//            } catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 拒绝策略，保证阻塞顺序放入,而不是直接丢掉任务或者异常
//     */
//    public static class BlockingPolicy implements RejectedExecutionHandler{
//        @Override
//        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//            try {
//                executor.getQueue().put(r);
//            } catch (InterruptedException e) {
//                if(r instanceof UserDealTask){
//                    //打印出userid相关日志
//                    log.error(">>> 用户任务入队异常,用户id是：{},科目是{}",((UserDealTask) r).getUserId(),((UserDealTask) r).getSubject(),e);
//                }
//            }
//        }
//    }
//
//
//    public class UserDealTask implements Runnable{
//        private int userId;
//        private int subject;
//
//        public UserDealTask(int userId, int subject) {
//            this.userId = userId;
//            this.subject = subject;
//        }
//
//        @Override
//        public void run() {
//            //处理逻辑
//            try {
//                userPointTask.bathUpdateUserPoint(userId,subject);
//            } catch (BizException e) {
//                log.error(">>> 用户任务处理失败，用户id是：{},科目是{}",getUserId(),getSubject(),e);
//            }
//        }
//
//        public int getUserId() {
//            return userId;
//        }
//
//        public void setUserId(int userId) {
//            this.userId = userId;
//        }
//
//        public int getSubject() {
//            return subject;
//        }
//
//        public void setSubject(int subject) {
//            this.subject = subject;
//        }
//    }
//}
