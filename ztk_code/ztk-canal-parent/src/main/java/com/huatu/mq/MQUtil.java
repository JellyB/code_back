package com.huatu.mq;

import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.huatu.constants.Constant.MQ_PASSWORD;
import static com.huatu.constants.Constant.MQ_URL;
import static com.huatu.constants.Constant.MQ_USER;

/**
 * Created by ismyway on 16/5/10.
 */
public class MQUtil {

    private static final Logger logger = LoggerFactory.getLogger(MQUtil.class);

    private  static com.rabbitmq.client.ConnectionFactory factory ;

    private static Connection getConnection() throws IOException, TimeoutException {
        // 创建连接工厂
        if(factory == null) {
            factory =new ConnectionFactory();
        }
//      设置RabbitMQ地址
        factory.setHost(MQ_URL);
        factory.setUsername(MQ_USER);
        factory.setPassword(MQ_PASSWORD);

//      创建一个新的连接
        Connection connection = factory.newConnection();

        return connection;
    }


    /**
     * 向activemq中发送消息
     *
     * @param queueName 队列名称,对应destination
     * @param msg       消息内容哦
     */
    public static void sendMessage(String queueName, String msg) throws IOException, TimeoutException {
        //创建一个频道

        Connection connection = getConnection();
        Channel channel = connection.createChannel();

//      发送消息到队列中
        channel.basicPublish("", queueName, null, msg.getBytes("UTF-8"));
//      关闭频道和连接
        try {
            channel.close();
            connection.close();
        } catch (TimeoutException e) {
            logger.error("mq关闭连接失败!",e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            MQUtil.sendMessage("v_obj_question","asdf");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

}
