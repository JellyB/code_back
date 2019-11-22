package com.huatu.hadoop.conf.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.message.MessageAndMetadata;
import kafka.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaConfiguration {

    @Bean
    public Producer<String, String> createProducer() {

        Properties props = new Properties();
        props.put("zookeeper.connect", "192.168.100.68:2181,192.168.100.70:2181,192.168.100.72:2181");
        props.put("metadata.broker.list", "192.168.100.68:9092,192.168.100.70:9092,192.168.100.72:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("num.partitions", "3");

        ProducerConfig config = new ProducerConfig(props);


        Producer<String, String> producer = new Producer<>(config);

        return producer;
    }

    @Bean
    public Map<String, List<KafkaStream<byte[], byte[]>>> videoPlayConsumer() {

        Properties props = new Properties();
        props.put("zookeeper.connect", "192.168.100.68:2181,192.168.100.70:2181,192.168.100.72:2181");
        props.put("group.id", "flume335-1");
        props.put("metadata.broker.list", "192.168.100.68:9092,192.168.100.70:9092,192.168.100.72:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("rebalance.max.retries", "10");
        props.put("rebalance.backoff.ms", "2000");

        ConsumerConfig config = new ConsumerConfig(props);
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(config);

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put("video-record", 3);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        System.out.println("kafka");
        return consumerMap;
    }


    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("zookeeper.connect", "192.168.100.68:2181,192.168.100.70:2181,192.168.100.72:2181");
        props.put("group.id", "flume335");
        props.put("metadata.broker.list", "192.168.100.68:9092,192.168.100.70:9092,192.168.100.72:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        ConsumerConfig config = new ConsumerConfig(props);
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(config);


        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put("video-record", 1);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);

        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get("video-record");

        for (final KafkaStream<byte[], byte[]> kafkaStream : streams) {


            new Thread(() -> {
                for (MessageAndMetadata<byte[], byte[]> mm : kafkaStream) {

                    String msg = new String(mm.message());
                    System.out.println(msg);
                }
            }).start();

        }
    }
}
