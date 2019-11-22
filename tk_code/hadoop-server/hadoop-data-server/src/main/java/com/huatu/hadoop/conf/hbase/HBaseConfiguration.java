package com.huatu.hadoop.conf.hbase;

import com.huatu.hadoop.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;

@Configuration
public class HBaseConfiguration {

//    @Bean
//    public Connection createHBaseConnection() throws IOException {
//
//
//        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
//        conf.set("hbase.zookeeper.quorum", "192.168.100.68,192.168.100.70,192.168.100.72");
//        conf.set("zookeeper.znode.parent", "/hbase");
//        conf.set("hbase.zookeeper.property.clientPort", "2181");
//        Connection connection = ConnectionFactory.createConnection(conf);
//
//        return connection;
//    }
    @Bean
    public HBaseUtil createHBaseUtil() throws IOException {


        HBaseUtil hBaseUtil = new HBaseUtil();
        return hBaseUtil;
    }

}
