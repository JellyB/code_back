//package com.huatu.ztk.search;
//
//import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.elasticsearch.common.xcontent.XContentFactory;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by shaojieyue
// * Created time 2016-06-22 18:18
// */
//public class EsTest {
//    public static void main(String[] args){
//        try {
//            buildIndexMapping();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
//    /**
//     * @throws Exception Exception
//     */
//    protected static void buildIndexMapping() throws Exception {
//        Map<String, Object> settings = new HashMap<String, Object>();
//        settings.put("number_of_shards", 4);//分片数量
//        settings.put("number_of_replicas", 0);//复制数量
//        settings.put("refresh_interval", "10s");//刷新时间
//        settings.put("cluster.name", "ghy");
//
//        CreateIndexRequestBuilder cib = GetConnection.getTransportClient().admin().indices().prepareCreate(GetConnection.myIndex);
//        cib.setSettings(settings);
//
//        XContentBuilder mapping = XContentFactory.jsonBuilder()
//                .startObject()
//                .startObject("we3r")//
//                .startObject("_ttl")//有了这个设置,就等于在这个给索引的记录增加了失效时间,
//                //ttl的使用地方如在分布式下,web系统用户登录状态的维护.
//                .field("enabled", true)//默认的false的
//                .field("default", "5m")//默认的失效时间,d/h/m/s 即天/小时/分钟/秒
//                .field("store", "yes")
//                .field("index", "not_analyzed")
//                .endObject()
//                .startObject("_timestamp")//这个字段为时间戳字段.即你添加一条索引记录后,自动给该记录增加个时间字段(记录的创建时间),搜索中可以直接搜索该字段.
//                .field("enabled", true)
//                .field("store", "no")
//                .field("index", "not_analyzed")
//                .endObject()
//                //properties下定义的name等等就是属于我们需要的自定义字段了,相当于数据库中的表字段 ,此处相当于创建数据库表
//                .startObject("properties")
//                .startObject("@timestamp").field("type", "long").endObject()
//                .startObject("name").field("type", "string").field("store", "yes").endObject()
//                .startObject("home").field("type", "string").field("index", "not_analyzed").endObject()
//                .startObject("now_home").field("type", "string").field("index", "not_analyzed").endObject()
//                .startObject("height").field("type", "double").endObject()
//                .startObject("age").field("type", "integer").endObject()
//                .startObject("birthday").field("type", "date").field("format", "YYYY-MM-dd").endObject()
//                .startObject("isRealMen").field("type", "boolean").endObject()
//                .startObject("location").field("lat", "double").field("lon", "double").endObject()
//                .endObject()
//                .endObject()
//                .endObject();
//        cib.addMapping(GetConnection.myType, mapping);
//        cib.execute().actionGet();
//    }
//
//
//}
