//package com.huatu.ztk.search.es;
//
//import com.baidu.disconf.client.common.annotations.DisconfFile;
//import com.baidu.disconf.client.common.annotations.DisconfFileItem;
//import org.apache.commons.lang3.StringUtils;
//import org.elasticsearch.client.Client;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.net.InetSocketAddress;
//
///**
// * 封装的es
// * Created by shaojieyue
// * Created time 2016-05-04 10:51
// */
//
//@DisconfFile(filename = "elasticsearch.properties")
//public class EsClient {
//    private static final Logger logger = LoggerFactory.getLogger(EsClient.class);
//
//
//    private static String esAddress ;
//
//    private static TransportClient client = null;
//
//
//    public static Client getInstance() {
//        if (client == null) {
//            init();
//        }
//
//        return client;
//    }
//
//    @DisconfFileItem(name = "es.address",associateField ="esAddress")
//    public static String getEsAddress() {
//        return esAddress;
//    }
//
//
//    public static void setEsAddress(String esAddress) {
//        EsClient.esAddress = esAddress;
//    }
//
//    /**
//     * 初始化es客户端
//     */
//    private static synchronized void init(){
//        if (client != null) {//已经初始化过,不进行处理
//            return;
//        }
//
//        String address = StringUtils.trimToNull(getEsAddress());
//        logger.info("es client address = {}",getEsAddress());
//        if (address == null) {
//            throw new RuntimeException("esAddress is null");
//        }
//
//        Settings settings = Settings.settingsBuilder()
//                .put("cluster.name", "huatu-ztk-cluster")
//                .build();
//        client = new TransportClient.Builder().settings(settings).build();
//
//        final String[] array = address.split(",");
//        for (String str : array) {
//            final String[] arr = str.split(":");
//            String host = arr[0];
//            int port = Integer.valueOf(arr[1]);
//            final InetSocketTransportAddress transportAddress = new InetSocketTransportAddress(new InetSocketAddress(host, port));
//            client.addTransportAddress(transportAddress);
//        }
//    }
//}
