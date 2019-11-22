package com.huatu.bigdataanalyzeserver.util;

import com.huatu.bigdataanalyzecommon.constant.RabbitmqConstant;
import com.rabbitmq.client.*;

import java.io.IOException;

public class RabbitMQUtils {

    public final static String QUEUE_NAME = RabbitmqConstant.RABBIT_TOPIC_RECORD_QUEUE_NAME;


    public static void sendMessageToRmq(ConnectionFactory rabbitmqConnection, String message) throws IOException {

        Connection connection = rabbitmqConnection.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));

        System.out.println("Producer Send +'" + message + "'");
        //关闭通道和连接
        channel.close();
        connection.close();
    }
}
