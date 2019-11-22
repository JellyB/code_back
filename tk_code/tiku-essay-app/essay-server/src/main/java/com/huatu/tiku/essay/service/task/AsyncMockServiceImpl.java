package com.huatu.tiku.essay.service.task;

import com.huatu.tiku.essay.service.EssayMatchService;
import com.huatu.tiku.essay.service.EssayMockExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * @author zhouwei
 * @Description: 异步任务demo
 * @create 2017-12-27 下午1:22
 **/
@Slf4j
@Component
public class AsyncMockServiceImpl {

    @Autowired
    EssayMockExamService essayMockExamService;
    @Autowired
    EssayMatchService essayMatchService;
    @Async
    public  void resolvePicture(){
        log.info("异步方法被调用");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("异步方法结束调用");
    }

    @Async
    public Future<String> resolvePicture(String url){
        log.info("异步方法被调用");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("异步方法结束调用");
      return   new AsyncResult<String>("");
    }


    @Async
    public Future<String> saveMockAnswerToMySql(String redisKey){
        log.info("异步方法被调用,将用户答案存入MySql");
        essayMockExamService.saveMockPaperAnswer(redisKey);
        log.info("异步方法调用结束,用户答案存入MySql完成");
        return   new AsyncResult<String>("");
    }


}
