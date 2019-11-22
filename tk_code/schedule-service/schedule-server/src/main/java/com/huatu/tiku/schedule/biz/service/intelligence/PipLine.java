package com.huatu.tiku.schedule.biz.service.intelligence;

import com.huatu.tiku.schedule.biz.service.intelligence.handler.NightAndMorningLimit;
import com.huatu.tiku.schedule.biz.service.intelligence.handler.OneDayTwoLiveLimit;
import lombok.Data;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by duanxiangchao on 2018/5/10
 */
@Data
@Service
public class PipLine {

    /**
     * 默认
     */
    private DefaultSchedulePipLine defaultSchedulePipLine;
    /**
     * 讲师责任链
     */
    private DefaultSchedulePipLine lecturerSchedulePipLine;
    /**
     * 助教
     */
    private DefaultSchedulePipLine assistantSchedulePipLine;
    /**
     * 学习师
     */
    private DefaultSchedulePipLine learnerSchedulePipLine;
    /**
     * 场控
     */
    private DefaultSchedulePipLine controllerSchedulePipLine;
    /**
     * 主持人
     */
    private DefaultSchedulePipLine compereSchedulePipLine;

    @Resource
    private OneDayTwoLiveLimit oneDayTwoLiveLimitHandler;
    @Resource
    private NightAndMorningLimit nightAndMorningLimit;

    @PostConstruct
    public void init(){
        defaultSchedulePipLine = new DefaultSchedulePipLine();
        defaultSchedulePipLine.addLast(oneDayTwoLiveLimitHandler);
        defaultSchedulePipLine.addLast(nightAndMorningLimit);

        lecturerSchedulePipLine = new DefaultSchedulePipLine();
        lecturerSchedulePipLine.addLast(null);

        assistantSchedulePipLine = new DefaultSchedulePipLine();
        assistantSchedulePipLine.addLast(null);

        learnerSchedulePipLine = new DefaultSchedulePipLine();
        learnerSchedulePipLine.addLast(null);

        controllerSchedulePipLine = new DefaultSchedulePipLine();
        controllerSchedulePipLine.addLast(null);

        compereSchedulePipLine = new DefaultSchedulePipLine();
        compereSchedulePipLine.addLast(null);
    }

}
