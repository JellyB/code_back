package com.huatu.ztk.knowledge.common;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * disconf中存储客户端展示的科目属性（json）
 * Created by huangqp on 2018\3\7 0007.
 */
@Component
@DisconfFile(filename = "subjectTree.properties")
public class SubjectTreeConfig {
    private static final Logger log = LoggerFactory.getLogger(DatacleanConfig.class);
    private String subjectJson = "[{\"id\":1,\"name\":\"公务员\",\"childrens\":[{\"id\":1,\"name\":\"行测\",\"childrens\":[],\"tiku\":false},{\"id\":14,\"name\":\"申论\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":3,\"name\":\"事业单位\",\"childrens\":[{\"id\":2,\"name\":\"公基\",\"childrens\":[],\"tiku\":false},{\"id\":3,\"name\":\"职测\",\"childrens\":[],\"tiku\":false},{\"id\":24,\"name\":\"综合应用\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100047,\"name\":\"招警考试\",\"childrens\":[{\"id\":100100175,\"name\":\"公安招警科目\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100045,\"name\":\"教师\",\"childrens\":[{\"id\":400,\"name\":\"教师\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100000,\"name\":\"医疗\",\"childrens\":[{\"id\":410,\"name\":\"医疗\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100002,\"name\":\"金融\",\"childrens\":[{\"id\":420,\"name\":\"金融\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100046,\"name\":\"其他\",\"childrens\":[{\"id\":430,\"name\":\"其他\",\"childrens\":[],\"tiku\":false}],\"tiku\":false}]";

    @DisconfFileItem(name = "subjectJson", associateField = "subjectJson")
    public String getSubectJson() {
        return subjectJson;
    }

    public void setSubjectJson(String subjectJson) {
        this.subjectJson = subjectJson;
    }
}
