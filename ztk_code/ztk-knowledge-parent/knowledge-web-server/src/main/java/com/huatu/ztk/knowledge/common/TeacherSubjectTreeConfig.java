package com.huatu.ztk.knowledge.common;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/10/23
 * @描述 教师题库获取科目树
 */
@Component
@DisconfFile(filename = "teacher.subjectTree.properties")
public class TeacherSubjectTreeConfig {


    private static final Logger log = LoggerFactory.getLogger(DatacleanConfig.class);
    private String subjectJson = "[{\"id\": 200100045,\"name\": \"教师招聘\",\"childrens\": [{\"id\": 100100262,\"name\": \"教育综合知识\",\"childrens\": [],\"tiku\": false}],\"tiku\": true},{\"id\": 200100048,\"name\": \"教师资格证-小学\",\"childrens\": [{\"id\": 200100049,\"name\": \"综素\",\"childrens\": [],\"tiku\": false},{\"id\": 200100051,\"name\": \"教知\",\"childrens\": [],\"tiku\": false}],\"tiku\": true},{\"id\": 200100053,\"name\": \"教师资格证-中学\",\"childrens\": [{\"id\": 200100050,\"name\": \"综素\",\"childrens\": [],\"tiku\": false},{\"id\": 200100052,\"name\": \"教知\",\"childrens\": [],\"tiku\": false}],\"tiku\": true}] ";

    @DisconfFileItem(name = "subjectJson", associateField = "subjectJson")
    public String getSubjectJson() {
        return subjectJson;
    }

    public void setSubjectJson(String subjectJson) {
        this.subjectJson = subjectJson;
    }
}
