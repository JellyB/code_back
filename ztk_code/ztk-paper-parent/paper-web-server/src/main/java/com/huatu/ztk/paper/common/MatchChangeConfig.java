package com.huatu.ztk.paper.common;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author huangqp
 * @date 2017/10/4 9:44
 */
@Component
@DisconfFile(filename = "match-change.properties")
public class MatchChangeConfig {
    private static final Logger log = LoggerFactory.getLogger(MatchChangeConfig.class);

    /**
     * 是否是旧模板
     */
    private String defaultMatchOldFlag = "false";

    /**
     * 安卓版本界限
     */
    private String matchAndroidCvDeadline = "7.1.8";
    /**
     * ios版本界限
     */
    private String matchIphoneCvDeadline = "7.1";
    /**
     * 特定的旧版本科目
     */
    private String matchOldSubject = "";
    /**
     * 特定的新版本
     */
    private String matchNewSubject = "";

    /**
     * 模考大赛首页降级标识
     */
    private String degrade = "false";

    private String degradeHour = "11";       //降级时间点（小时）

    private String degradeMinute = "0";      //降级时间点（分钟数）
    @DisconfFileItem(name = "defaultMatchOldFlag", associateField = "defaultMatchOldFlag")
    public String getDefaultMatchOldFlag() {
        log.info("defaultMatchOldFlag={}",defaultMatchOldFlag);
        return defaultMatchOldFlag;
    }

    public void setDefaultMatchOldFlag(String defaultMatchOldFlag) {
        this.defaultMatchOldFlag = defaultMatchOldFlag;
    }
    @DisconfFileItem(name = "matchAndroidCvDeadline", associateField = "matchAndroidCvDeadline")
    public String getMatchAndroidCvDeadline() {
        log.info("matchAndroidCvDeadline={}",matchAndroidCvDeadline);
        return matchAndroidCvDeadline;
    }


    public void setMatchAndroidCvDeadline(String matchAndroidCvDeadline) {
        this.matchAndroidCvDeadline = matchAndroidCvDeadline;
    }

    @DisconfFileItem(name = "matchIphoneCvDeadline", associateField = "matchIphoneCvDeadline")
    public String getMatchIphoneCvDeadline() {
        log.info("matchIphoneCvDeadline={}",matchIphoneCvDeadline);
        return matchIphoneCvDeadline;
    }

    public void setMatchIphoneCvDeadline(String matchIphoneCvDeadline) {
        this.matchIphoneCvDeadline = matchIphoneCvDeadline;
    }

    @DisconfFileItem(name = "matchOldSubject", associateField = "matchOldSubject")
    public String getMatchOldSubject() {
        log.info("matchOldSubject={}",matchOldSubject);
        return matchOldSubject;
    }

    public void setMatchOldSubject(String matchOldSubject) {
        this.matchOldSubject = matchOldSubject;
    }

    @DisconfFileItem(name = "matchNewSubject", associateField = "matchNewSubject")
    public String getMatchNewSubject() {
        log.info("matchOldSubject={}",matchOldSubject);
        return matchNewSubject;
    }

    public void setMatchNewSubject(String matchNewSubject) {
        this.matchNewSubject = matchNewSubject;
    }

    @DisconfFileItem(name = "degrade", associateField = "degrade")
    public String getDegrade() {
        log.info("degradeFlag={}",degrade);
        return degrade;
    }

    public void setDegrade(String degrade) {
        this.degrade = degrade;
    }

    @DisconfFileItem(name = "degradeHour", associateField = "degradeHour")
    public String getDegradeHour() {
        log.info("degradeHour={}",degradeHour);
        return degradeHour;
    }

    public void setDegradeHour(String degradeHour) {
        this.degradeHour = degradeHour;
    }

    @DisconfFileItem(name = "degradeMinute", associateField = "degradeMinute")
    public String getDegradeMinute() {
        log.info("degradeMinute={}",degradeMinute);
        return degradeMinute;
    }

    public void setDegradeMinute(String degradeMinute) {
        this.degradeMinute = degradeMinute;
    }

}
