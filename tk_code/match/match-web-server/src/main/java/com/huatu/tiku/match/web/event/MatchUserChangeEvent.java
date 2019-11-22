package com.huatu.tiku.match.web.event;

import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * 用户考试信息统计表维护事件
 * Created by huangqingpeng on 2018/11/2.
 */
@Getter
public class MatchUserChangeEvent extends ApplicationContextEvent {

    private MatchUserMeta matchUserMeta;

    private OperationEnum operationEnum;

    public MatchUserChangeEvent(ApplicationContext source, MatchUserMeta matchUserMeta, OperationEnum operationEnum) {
        super(source);
        this.matchUserMeta = matchUserMeta;
        this.operationEnum = operationEnum;
    }

    @Override
    public Object getSource() {
        return super.getSource();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Getter
    @AllArgsConstructor
    public enum OperationEnum {
        INSERT(0),
        UPDATE(1);
        int key;
    }
}
