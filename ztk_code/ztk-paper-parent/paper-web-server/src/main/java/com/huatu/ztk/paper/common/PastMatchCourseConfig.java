package com.huatu.ztk.paper.common;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/1/2
 * @描述
 */
@Component
@DisconfFile(filename = "past-match-course.properties")
public class PastMatchCourseConfig {

    private static final Logger logger = LoggerFactory.getLogger(PastMatchCourseConfig.class);
    private String pastMatchCourseJson = "[{\"advertUrl\":\"http://tiku.huatu.com/cdn/pandora/img/8db8410b-e789-434f-9af8-d05bc8c5781a..png\",\"tag\":1,\"collectionCourseId\":123,\"subjectId\":1},{\"advertUrl\":\"http://tiku.huatu.com/cdn/pandora/img/8db8410b-e789-434f-9af8-d05bc8c5781a..png\",\"tag\":2,\"collectionCourseId\":567,\"subjectId\":1}]";

    @DisconfFileItem(name = "pastMatchCourseJson",associateField = "pastMatchCourseJson")
    public String getPastMatchCourseJson() {
        return pastMatchCourseJson;
    }

    public void setPastMatchCourseJson(String pastMatchCourseJson) {
        this.pastMatchCourseJson = pastMatchCourseJson;
    }


}
