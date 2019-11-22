package com.huatu.ztk.paper.common;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huangqp
 * @date 2017/10/4 9:44
 */
@Component
@DisconfFile(filename = "matchdata.properties")
public class MatchConfig {
    private static final Logger log = LoggerFactory.getLogger(MatchConfig.class);
    /**
     * 申论提前进入阶段时间（分钟）
     */
    private int essayLeadTime = 60;
    /**
     * 申论延迟查看报告时间（分钟）
     */
    private int essayDelayReportTime = 15;
    /**
     * 模考大赛下一场展示延时（分钟）
     */
    private int nextMatchDelayTime = 60;

    /**
     * 报名方式为无地区报名的科目
     */
    private String enrollNoAreaSubjects = "";
    /**
     * 展示多个模考的模考数最大值
     *
     * @return
     */
    private int maxMatchShowSize = 5;

    /**
     * PC 展示的模考大赛权重最高的ID
     */
    private String pcMatchShowPaperIdInfo = "";

    /**
     * app 暂时的模考大赛权最高的ID
     */
    private String appMatchShowPaperIdInfo = "";

    /**
     * app旧版本标签配置
     */
    private String tagInfo = "";

    @DisconfFileItem(name = "tagInfo", associateField = "tagInfo")
    public String getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(String tagInfo) {
        this.tagInfo = tagInfo;
    }


    @DisconfFileItem(name = "essayLeadTime", associateField = "essayLeadTime")
    public int getEssayLeadTime() {
        log.info("essayLeadTime={}", essayLeadTime);
        return essayLeadTime;
    }

    public void setEssayLeadTime(int essayLeadTime) {
        this.essayLeadTime = essayLeadTime;
    }

    @DisconfFileItem(name = "essayDelayReportTime", associateField = "essayDelayReportTime")
    public int getEssayDelayReportTime() {
        log.info("essayDelayReportTime={}", essayDelayReportTime);
        return essayDelayReportTime;
    }

    public void setEssayDelayReportTime(int essayDelayReportTime) {
        this.essayDelayReportTime = essayDelayReportTime;
    }

    @DisconfFileItem(name = "nextMatchDelayTime", associateField = "nextMatchDelayTime")
    public long getNextMatchDelayTime() {
        return nextMatchDelayTime;
    }

    public void setNextMatchDelayTime(int nextMatchDelayTime) {
        this.nextMatchDelayTime = nextMatchDelayTime;
    }

    @DisconfFileItem(name = "maxMatchShowSize", associateField = "maxMatchShowSize")
    public int getMaxMatchShowSize() {
        return maxMatchShowSize;
    }

    public void setMaxMatchShowSize(int maxMatchShowSize) {
        this.maxMatchShowSize = maxMatchShowSize;
    }

    @DisconfFileItem(name = "enrollNoAreaSubjects", associateField = "enrollNoAreaSubjects")
    public String getEnrollNoAreaSubjects() {
        return enrollNoAreaSubjects;
    }

    public List<Integer> getEnrollNoAreaSubjectCollection() {
        String subjectStr = getEnrollNoAreaSubjects();
        return stringToList(subjectStr);
    }

    public void setEnrollNoAreaSubjects(String enrollNoAreaSubjects) {
        this.enrollNoAreaSubjects = enrollNoAreaSubjects;
    }

    @DisconfFileItem(name = "pcMatchShowPaperIdInfo", associateField = "pcMatchShowPaperIdInfo")
    public String getPcMatchShowPaperIdInfo() {
        return pcMatchShowPaperIdInfo;
    }

    public List<Integer> getPcMatchShowPaperIdInfoCollection() {
        String pcMatchShowPaperIdInfo = getPcMatchShowPaperIdInfo();
        return stringToList(pcMatchShowPaperIdInfo);
    }

    @DisconfFileItem(name = "appMatchShowPaperIdInfo", associateField = "appMatchShowPaperIdInfo")
    public String getAppMatchShowPaperIdInfo() {
        return appMatchShowPaperIdInfo;
    }

    public List<Integer> getAppMatchShowPaperIdInfoCollection() {
        return stringToList(appMatchShowPaperIdInfo);
    }

    private static List<Integer> stringToList(String baseString) {
        if (StringUtils.isBlank(baseString)) {
            return Lists.newArrayList();
        }
        try {
            return Arrays.asList(baseString.split(",")).stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Lists.newArrayList();
        }
    }
}
