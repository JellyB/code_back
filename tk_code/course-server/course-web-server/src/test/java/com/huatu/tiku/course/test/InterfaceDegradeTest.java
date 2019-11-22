package com.huatu.tiku.course.test;

import com.google.common.collect.Lists;
import com.huatu.common.test.BaseWebTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述：多线程获取线上用户token保存为 csv 文件->压力测试
 *
 * @author biguodong
 * Create time 2019-02-13 下午4:05
 **/
@Slf4j
public class InterfaceDegradeTest extends BaseWebTest {

    private static final int REQUEST_TOTAL = 2000000;
    private static final int CONCURRENCY_FATAL = 99;
    private static final String FILE_PATH = "/Users/biguodong";
    private static final String FILE_NAME = "token";
    //private static final int MAX_USER_ID = 233982805;
    private static final int MAX_USER_ID = 234035396;
    private static final List<String> tokenList = Lists.newArrayList();
    private static final AtomicInteger current = new AtomicInteger(0);

    @Test
    public void getToken(){
        final CountDownLatch countDownLatch = new CountDownLatch(REQUEST_TOTAL);
        final Semaphore semaphore = new Semaphore(CONCURRENCY_FATAL);
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(CONCURRENCY_FATAL, 100, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(REQUEST_TOTAL));
        for(int i = 0; i < REQUEST_TOTAL; i ++){
            threadPoolExecutor.execute(()->{
                try{
                    semaphore.acquire();
                    doGetToken();
                    semaphore.release();
                }catch (Exception e){
                    log.error("concurrent exception:{}", e);
                }
                countDownLatch.countDown();
            });
        }

        BufferedWriter fileOutputStream = null;

        try{
            countDownLatch.await();
            String fullPath_Edge = FILE_PATH + File.separator + FILE_NAME + ".csv";
            File tokenFile = new File(fullPath_Edge);
            if (!tokenFile.getParentFile().exists()) {
                tokenFile.getParentFile().mkdirs();
            }
            if (tokenFile.exists()) {
                tokenFile.delete();
            }
            tokenFile = new File(fullPath_Edge);
            tokenFile.createNewFile();
            fileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tokenFile), "UTF-8"), 1024);
            fileOutputStream.write("token");
            fileOutputStream.newLine();
            for(String token : tokenList){
                fileOutputStream.write(token);
                fileOutputStream.newLine();
            }
            fileOutputStream.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e){
            log.error("caught an interruptedException:{}", e);
        }finally {
            try{
                fileOutputStream.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }
        }
    }


    private synchronized void doGetToken(){
        current.incrementAndGet();
        log.info(">>>>>>>>>>>>>>>> :{}", current.get());
        int userId = MAX_USER_ID - current.get();
        RestTemplate restTemplate = new RestTemplate();
        String tokenUri = "http://123.103.86.52/u/v1/users/%s/token";
        tokenUri = String.format(tokenUri, userId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("secret", "123ztk");
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(tokenUri);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        HttpEntity<LinkedHashMap> exchange = restTemplate.exchange(uriComponentsBuilder.toUriString(), HttpMethod.GET, entity, LinkedHashMap.class);
        if(null != exchange && null != exchange.getBody()){
            LinkedHashMap<String,Object> linkedHashMap = exchange.getBody();
            if(null != linkedHashMap.get("data")){
                LinkedHashMap data = (LinkedHashMap)linkedHashMap.get("data");
                if(null != data.get("appToken")){
                    String token = String.valueOf(data.get("appToken"));
                    tokenList.add(token);
                    log.info(">>>>>>>>>>>>>>>>> userId:{},token:{}", userId, token);
                }
            }
        }
    }
}
