package top.jbzm.index.util;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author jbzm
 * @date Create on 2018/4/3 23:12
 */
@Component
public class ElasticUtils {
    @Autowired
    private TransportClient transportClient;

    /**
     * 向es中通过bulk批量索引数据
     *
     * @param index
     * @param type
     * @param data
     */
    public void indexByBulk(String index, String type, List<Map<String, Object>> data) {
        BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();
        data.forEach(x -> {
            try {
                XContentBuilder xContentBuilder = XContentFactory.jsonBuilder().startObject();
                x.forEach((key, value) -> {
                    try {
                        if (key.contains("time") || key.contains("Time")) {
                            if (String.valueOf(value).length() == DateUtils.DATELONG) {
                                xContentBuilder.field(key, new Date((Long) value));
                            } else {
                                xContentBuilder.field(key, value);
                            }
                        } else {
                            xContentBuilder.field(key, value);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                xContentBuilder.endObject();
                bulkRequestBuilder.add(transportClient.prepareIndex(index, type).setSource(xContentBuilder.string(), XContentType.JSON));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        bulkRequestBuilder.get();
    }

    public static void main(String[] args) {
        String str = "timelolosdlkf";
        if (str.contains("time")) {
            System.out.println(1);
        }
    }
}
