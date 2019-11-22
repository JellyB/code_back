package com.huatu.tiku.course.task;

import com.huatu.tiku.course.netschool.api.v3.PromoteCoreServiceV3;
import com.huatu.tiku.course.util.CourseCacheKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * TODO  待定的订单失败处理逻辑
 * 异步处理订单的问题
 * @author hanchao
 * @date 2017/10/5 11:35
 */
//@Service
@Deprecated
public class OrderPushTask implements Runnable,InitializingBean {
    @Resource(name = "redisTemplate")
    private ListOperations<String,String> listOperations;
    @Autowired
    private PromoteCoreServiceV3 promoteCoreServiceV3;

    private volatile boolean running = true;


    @Override
    public void run() {
        while(running){
            try {
                String params = listOperations.rightPop(CourseCacheKey.ORDERS_QUEUE);
                if(params == null){
                    //如果任务已经处理完，一秒后重新尝试
                    TimeUnit.SECONDS.sleep(1);
                }else{
                    promoteCoreServiceV3.createOrder(params);//如果此处fallback,那么fallback会将此任务重新入队
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    @PreDestroy
    public void destroy(){
        this.running = false;
    }

    //启动任务
    @Override
    public void afterPropertiesSet() throws Exception {
        Thread task = new Thread(this);
        task.setDaemon(true);
        task.start();
    }
}
