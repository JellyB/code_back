//package com.huatu.tiku.match.web.event;
//
//import com.huatu.tiku.match.bean.entity.MatchEssayUserMeta;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.event.ApplicationContextEvent;
//
///**
// * 申论考试数据持久化事件
// * Created by huangqingpeng on 2018/12/21.
// */
//@Getter
//public class MatchEssayUserChangeEvent extends ApplicationContextEvent{
//
//    private MatchEssayUserMeta matchEssayUserMeta;
//
//    private OperationEnum operationEnum;
//    public MatchEssayUserChangeEvent(ApplicationContext source,MatchEssayUserMeta matchEssayUserMeta,OperationEnum operationEnum) {
//        super(source);
//        this.matchEssayUserMeta = matchEssayUserMeta;
//        this.operationEnum = operationEnum;
//    }
//
//
//    @Override
//    public Object getSource() {
//        return super.getSource();
//    }
//
//    @Override
//    public String toString() {
//        return super.toString();
//    }
//
//    @Getter
//    @AllArgsConstructor
//    public enum OperationEnum {
//        INSERT(0),
//        UPDATE(1);
//        int key;
//    }
//}
