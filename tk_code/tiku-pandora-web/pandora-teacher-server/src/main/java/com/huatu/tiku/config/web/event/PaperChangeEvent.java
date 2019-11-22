package com.huatu.tiku.config.web.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * 试卷信息改变事件处理
 * Created by lijun on 2018/9/19
 */
@Slf4j
public class PaperChangeEvent extends ApplicationContextEvent {

    private Long paperId;

    public PaperChangeEvent(ApplicationContext source, Long paperId) {
        super(source);
        this.paperId = paperId;
    }

    @Override
    public Object getSource() {
        return super.getSource();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public Long getPaperId() {
        return paperId;
    }
}
