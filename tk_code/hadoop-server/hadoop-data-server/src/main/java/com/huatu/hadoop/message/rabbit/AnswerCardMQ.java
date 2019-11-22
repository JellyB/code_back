package com.huatu.hadoop.message.rabbit;

import com.alibaba.fastjson.JSONObject;
import com.facebook.presto.jdbc.internal.guava.util.concurrent.ThreadFactoryBuilder;
import com.huatu.hadoop.bean.ZtkAnswerCardCTO;
import com.huatu.hadoop.util.HBaseUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class AnswerCardMQ {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();

    private ThreadPoolExecutor pool = new ThreadPoolExecutor(10,
            200,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024),
            namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
    private static final BlockingQueue queue = new ArrayBlockingQueue(10);
    private static final String AC_R_USERNAME = "rabbitmq_ztk";
    private static final String AC_R_HOST = "192.168.100.2";
    private static final String AC_R_PASSWORD = "rabbitmq_ztk";
    private static final String AC_R_EXECHANGE = "answer-card";
    private static final String AC_R_QUEUE = "answer_card_qu1";

    @Autowired
    private Producer<String, String> producer;


    @PostConstruct
    public void init() {

        pool.execute(() -> {

            try {
                ConnectionFactory factory = new ConnectionFactory();
                //设置RabbitMQ相关信息
                factory.setHost(AnswerCardMQ.AC_R_HOST);
                factory.setPort(35672);
                factory.setUsername(AnswerCardMQ.AC_R_USERNAME);
                factory.setPassword(AnswerCardMQ.AC_R_PASSWORD);

                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.exchangeDeclare(AnswerCardMQ.AC_R_EXECHANGE, "fanout", false);

                channel.queueDeclare(AnswerCardMQ.AC_R_QUEUE, false, false, false, null);
                channel.queueBind(AnswerCardMQ.AC_R_QUEUE, "answer-card", "");

                QueueingConsumer consumer = new QueueingConsumer(channel);
                channel.basicConsume(AnswerCardMQ.AC_R_QUEUE, true, consumer);

                do {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    String message = new String(delivery.getBody());
                    try {
                        ZtkAnswerCardCTO ztkAnswerCardCTO = JSONObject.parseObject(message, ZtkAnswerCardCTO.class);
                        log.info("kafka had send ztkAnswerCardCTO : {}  ", ztkAnswerCardCTO);
                        producer.send(new KeyedMessage<>("question-record", ztkAnswerCardCTO.toString()));
                        saveToHbase(ztkAnswerCardCTO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (true);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) throws Exception {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            //设置RabbitMQ相关信息
            factory.setHost(AnswerCardMQ.AC_R_HOST);
            factory.setUsername(AnswerCardMQ.AC_R_USERNAME);
            factory.setPassword(AnswerCardMQ.AC_R_PASSWORD);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

			channel.exchangeDeclare(AnswerCardMQ.AC_R_EXECHANGE, "fanout", false);

            channel.queueDeclare(AnswerCardMQ.AC_R_QUEUE, false, false, false, null);
            channel.queueBind(AnswerCardMQ.AC_R_QUEUE, "answer-card", "");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(AnswerCardMQ.AC_R_QUEUE, true, consumer);

            do {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                try {
                    ZtkAnswerCardCTO ztkAnswerCardCTO = JSONObject.parseObject(message, ZtkAnswerCardCTO.class);
//                    log.info("kafka had send ztkAnswerCardCTO : {}  ", ztkAnswerCardCTO);
                    System.out.println(ztkAnswerCardCTO);
                    saveToHbase(ztkAnswerCardCTO);
// producer.send(new KeyedMessage<>("question-record", ztkAnswerCardCTO.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveToHbase(ZtkAnswerCardCTO za) {

        String reverseUserId = reverseCharArray(za.getUserId().toString());
        String reverseTime = reverseCharArray(za.getCreateTime().toString());

        int[] corrects = za.getCorrects();
        int undo_num = 0;
        int corr_num = 0;
        for (int i = 0; i < corrects.length; i++) {
            if (corrects[i] == 0) {
                undo_num += 1;
            }
            if (corrects[i] == 1) {
                corr_num += 1;
            }
        }
        int[] questions = za.getQuestions();
        int[] answer_tiem = za.getTimes();
        int sum_time = 0;
        for (int i = 0; i < answer_tiem.length; i++) {
            sum_time += answer_tiem[i];
        }
        Integer subject = za.getSubject();
        Long createTime = za.getCreateTime();
        Map<String, Object> data = new HashMap<>();
        data.put("ques_num", questions.length - undo_num);
        data.put("answer_tiem", sum_time);
        data.put("corr_num", corr_num);
        data.put("timestamp", createTime);
        data.put("subject", subject);


        data.put("questions", za.getQuestions2Str());
        data.put("answer_times", za.getTimes2Str());
        data.put("corrects", za.getCorrects2Str());
        try {
            HBaseUtil.putMultiOnTime("user_answer", reverseUserId + "-" + reverseTime + "-" + sdf.format(new Date(za.getCreateTime())), "answer_info", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String reverseCharArray(String s) {
        char[] array = s.toCharArray();
        String reverse = "";
        for (int i = array.length - 1; i >= 0; i--) {
            reverse += array[i];
        }
        return reverse;

    }
}

