//package com.ht.galaxy.config;
//
//import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.TransportAddress;
//import org.elasticsearch.common.xcontent.XContentFactory;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.IOException;
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
///**
// * @author zhengyi
// * @date 2018/7/18 9:47 PM
// **/
//@Slf4j
//@Configuration
//@EnableApolloConfig("tiku.elastic-6.3.1")
//public class ElasticSearchConfig implements DisposableBean {
//
//    private TransportClient transportClient;
//    @Value("${elasticsearch.cluster.name}")
//    private String clusterName;
//    @Value("${elasticsearch.host}")
//    private String host;
//    @Value("${elasticsearch.port}")
//    private String port;
//    @Value("${elasticsearch.transport.sniff}")
//    private boolean sniff;
//
//    @Bean
//    public TransportClient getTransportClient() throws UnknownHostException {
//        Settings settings = Settings.builder().put("cluster.name", clusterName)
//                .put("client.transport.sniff", sniff)
//                .build();
//        transportClient = new PreBuiltTransportClient(settings)
//                .addTransportAddress(new TransportAddress(InetAddress.getByName(host), Integer.valueOf(port)));
//        log.info("elasticsearch transport connect success");
//        return transportClient;
//    }
//
//    /**
//     * if is null close
//     */
//    @Override
//    public void destroy() {
//        if (transportClient != null) {
//            transportClient.close();
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//        Settings settings = Settings.builder().put("cluster.name", "jbzm-test-search")
//                .put("client.transport.sniff", false)
//                .build();
//        TransportClient transportClient = new PreBuiltTransportClient(settings)
//                .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.100.46"), 9300));
//        transportClient.prepareIndex("lol", "lol", 1 + "").setSource(XContentFactory.jsonBuilder().startObject().field("lol", "jbzm,test").endObject()).get();
//        System.out.println("ok");
//        log.info("elasticsearch transport connect success");
//    }
//}