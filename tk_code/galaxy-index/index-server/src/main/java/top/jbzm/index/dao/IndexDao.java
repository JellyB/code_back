package top.jbzm.index.dao;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.jbzm.common.ErrorResult;
import top.jbzm.exception.MyException;
import top.jbzm.index.dto.DataStorm;
import top.jbzm.index.util.ElasticUtils;
import top.jbzm.index.util.JdbcUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author jbzm
 * @date Create on 2018/3/21 17:14
 */
@Slf4j
@Component
public class IndexDao {
    @Autowired
    private TransportClient transportClient;

    @Autowired
    private ElasticUtils elasticUtils;
    @Autowired
    private JdbcUtils jdbcUtils;

    /**
     * 查找游标位置
     *
     * @param type
     * @return
     */
    public String findCursor(int type) {
        GetResponse getFields = transportClient.prepareGet("galaxy", "index", type + "").get();
            String result = null;
            try {
                result = XContentFactory.jsonBuilder().startObject()
                        .field("index", String.valueOf(getFields.getSource().get("index")))
                        .field("long", String.valueOf(getFields.getSource().get("long")))
                        .endObject().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 更新索引游标
     *
     * @param type
     * @param id
     */
    public void updateIndex(int type, String id) {
        UpdateRequest updateRequest = new UpdateRequest();
        try {
            updateRequest.index("galaxy").type("index").id(type + "").doc(
                    XContentFactory.jsonBuilder().startObject()
                            .field("index", id)
                            .endObject()
            );
            transportClient.update(updateRequest).get();
            log.info("~~~~~~~~~~~~~~更新游标成功~~~~~~~~~~~");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object indexDataStorm(DataStorm dataStorm) {
        if (dataStorm.getData().size() > 1000) {
            throw new MyException(ErrorResult.create(202, "集合内数据过长"));
        }
        try {
            switch (dataStorm.getDataType()) {
                case "elasticsearch":
                    elasticUtils.indexByBulk(dataStorm.getIndex(), dataStorm.getType(), dataStorm.getData());
                    break;
                case "mysql":
                    jdbcUtils.insertData(dataStorm.getType(), dataStorm.getData());
                    break;
                case "mysql_and_elasticsearch":
                    elasticUtils.indexByBulk(dataStorm.getIndex(), dataStorm.getType(), dataStorm.getData());
                    elasticUtils.indexByBulk(dataStorm.getIndex(), dataStorm.getType(), dataStorm.getData());
                    break;
                default:
                    throw new MyException(ErrorResult.create(202, "缺少指定数据存储类型"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MyException(ErrorResult.create(202, "数据存储失败"));
        }
        if (dataStorm.getOffsetType() != 0) {
            updateIndex(dataStorm.getOffsetType(), String.valueOf(dataStorm.getData().get(dataStorm.getData().size() - 1).get("offsetId")));
        }
        return dataStorm.getData().size();
    }

    public static void main(String[] args) {
        List<Map<String, Object>> maps = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("offsetId", i);
            map.put("lioj", 213);
            map.put("lioj", 213);
            map.put("lioj", 213);
            maps.add(map);
        }
        System.out.println(Long.valueOf(String.valueOf(maps.get(maps.size() - 1).get("offsetId"))) + " " + maps.size());
    }
}
