package com.huatu.bigdataanalyzeserver.conf.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfiguration {

    @Bean
    public Producer<String, String> createProducer() {

        Properties props = new Properties();
        props.put("zookeeper.connect", "slave01:2181,slave02:2181,slave03:2181");
        props.put("metadata.broker.list", "slave01:9092,slave02:9092,slave03:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("num.partitions", "3");

        ProducerConfig config = new ProducerConfig(props);


        Producer<String, String> producer = new Producer<>(config);

        return producer;
    }
}
