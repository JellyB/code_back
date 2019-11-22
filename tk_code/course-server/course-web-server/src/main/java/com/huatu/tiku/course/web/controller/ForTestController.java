package com.huatu.tiku.course.web.controller;

import com.huatu.common.exception.UnauthorizedException;
import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.springboot.report.annotation.WebReport;
import com.huatu.springboot.report.core.RabbitMqReportQueueEnum;
import com.huatu.springboot.report.product.ExtraDataHandler;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.springboot.web.version.mapping.annotation.ClientVersion;
import com.huatu.springboot.web.version.mapping.annotation.TerminalVersion;
import com.huatu.springboot.web.version.mapping.core.VersionOperator;
import com.huatu.tiku.common.bean.report.ReportMessage;
import com.huatu.tiku.common.bean.report.WebReportMessage;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author hanchao
 * @date 2018/1/18 9:54
 */
@RequestMapping("/test")
@RestController
@ApiVersion("4")
public class ForTestController {
    @GetMapping("/ex")
    public void exceptionTest() {
        throw new NullPointerException("抛出了一个空指针！");
    }

    @WebReport(value = "test", extraHandler = TestReportExtraHandler.class, holdResult = true)
    @RequestMapping("/report")
    public Object report(@Token UserSession session) {
        return HashMapBuilder.newBuilder().put("llll-1", "llsdfdfd").buildUnsafe();
    }

    @GetMapping("/exm")
    public void exMessage() {
        throw new UnauthorizedException("测试异常-1");
    }


    @RequestMapping(value = "/get")
    public String get1() {
        return "旧接口";
    }


    @RequestMapping(value = "/get", params = "data=tree")
    @ApiVersion("4.1")
    //method的apiversion会优先于class上的,方便升级小版本
    public String get2() {
        return "新数据";
    }


    @GetMapping("/c")
    @ClientVersion(expression = {"1>6.0.0", "2>6.0.0"})
    public String cvcheck1() {
        return "6.0.0以上版本的1类型";
    }

    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2, op = VersionOperator.GT, version = "6.0.0")})
    public String cvcheck2() {
        return "6.0.0以上版本的2类型";
    }


    @GetMapping("/c")
    @ClientVersion({@TerminalVersion(terminals = 2, op = VersionOperator.LTE, version = "6.0.0")})
    public String cvcheck3() {
        return "6.0.0以下版本的2类型";
    }


    public String c() {
        return "aaaa";
    }

    @WebReport(value = "testReport",
            queueName = RabbitMqReportQueueEnum.QUEUE_GALAXY_SEARCH,
            extraHandler = TestReportExtraHandlerForQueue.class,
            holdResult = true)
    @GetMapping(value = "testReport")
    public Object testReport(
            @Token UserSession userSession
    ) {
        HashMap map = new HashMap() {{
            put("userName", userSession.getUname());
        }};
        return map;
    }

    @Component
    public static class TestReportExtraHandlerForQueue implements ExtraDataHandler {
        @Override
        public Object extra(ReportMessage reportMessage) {
            if (reportMessage instanceof WebReportMessage) {
                return new HashMap<String, Object>() {{
                    put("time", reportMessage.getTimestamp());
                    put("type", 2);
                    put("data",
                            new HashMap<String, Object>() {{
                                put("userName", ((WebReportMessage) reportMessage).getUserSession().getUname());
                                put("token", ((WebReportMessage) reportMessage).getUserSession().getId());
                            }}
                    );
                }};
            }
            return null;
        }
    }


    @Component
    public static class TestReportExtraHandler implements ExtraDataHandler {
        @Override
        public Object extra(ReportMessage reportMessage) {
            if (reportMessage instanceof WebReportMessage) {
                String body = ((WebReportMessage) reportMessage).getBody();
                //比如用户请求的是json或者什么的，我们需要保存某些信息，或者计算某些信息(无法从response直接取得数据去判断，而且和controller异步执行，所以可能需要重复逻辑)
                return body.length();
            }
            return null;
        }
    }

}
