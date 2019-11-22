package com.huatu.hadoop.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import kafka.javaapi.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.*;

//import com.huatu.tiku.springboot.users.support.Token;

//@Service
//@Slf4j
public class MqService {


    //ExecutorService executorService = Executors.newFixedThreadPool(3);
    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
    //Common Thread Pool
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(10,
            200,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024),
            namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    private static final BlockingQueue queue = new ArrayBlockingQueue(10);
    @Autowired
    private VideoProcessService videoProcessService;

    @Autowired
    private Producer<String, String> producer;
    /**
     * rabbitmq
     */
    @Autowired
    private ConnectionFactory rabbitmqConnection;
    @Autowired
    private Connection connection;

    @Resource(name = "createChannel")
    private Channel channel;
    /**
     * 启动mq接收消息
     */
    @PostConstruct
    public void init() {

//        pool.execute(() -> {
//
//            try {
//                System.out.println("fanout videoplay 消费者");
//                RabbitMQUtils.doMessage(channel, queue);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        pool.execute(() -> {
//
//            while (true) {
//                String message = null;
//                try {
//                    message = queue.take().toString();
//
//                    String[] fileds = message.split("=");
//                    String cv = fileds[0];
//                    int terminal = Integer.parseInt(fileds[1]);
//                    String uname = fileds[2];
//                    CourseProcessDTO courseProcessDTO = JSONObject.parseObject(fileds[3], CourseProcessDTO.class);
//
//                    boolean b = videoProcessService.saveProcess(courseProcessDTO, uname, terminal, cv);
//                    producer.send(new KeyedMessage("videoplay-record", message));
//
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }
}