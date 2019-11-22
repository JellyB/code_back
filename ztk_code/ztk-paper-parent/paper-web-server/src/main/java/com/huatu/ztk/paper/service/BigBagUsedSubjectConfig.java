package com.huatu.ztk.paper.service;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/3/18
 * @描述 可以使用大礼包功能的科目, 允许哪个科目使用, 可以直接在disconf中配置
 * @描述 还可配置哪个功能使用
 */
@Component
@DisconfFile(filename = "big.bag.subject.properties")
public class BigBagUsedSubjectConfig {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BigBagUsedSubjectConfig.class);

    private String subjectId;

    @DisconfFileItem(name = "subjectId", associateField = "subjectId")
    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }


    /**
     * 判断科目是否可以使用大礼包活动
     *
     * @param subject
     * @return
     */
    public Boolean isEnabledUserSubject(int subject) {
        logger.info("大礼包可使用科目配置:{}", subjectId);
        if (StringUtils.isNotEmpty(subjectId)) {
            List<Integer> subjectIdList = Arrays.stream(subjectId.split(","))
                    .map(id -> Integer.valueOf(id))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(subjectIdList)) {
                if (subjectIdList.contains(subject)) {
                    return true;
                }
            }
        }
        return false;
    }
}