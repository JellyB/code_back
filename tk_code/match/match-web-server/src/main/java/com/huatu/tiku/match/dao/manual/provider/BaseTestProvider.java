package com.huatu.tiku.match.dao.manual.provider;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

/**
 * 基础测试类 - 自定义sql 语句
 * 根据官方文档，避免参数注入出现问题，此处建议直接保留注解。
 * Created by lijun on 2018/10/11
 */
public class BaseTestProvider {

    /**
     * 根据状态列表查询
     */
    public String listAllByState(@Param("state") Integer state) {
        return new SQL() {{
            SELECT(" * ");
            FROM("area");
            AND().WHERE(" status = " + state);
            AND().WHERE(" biz_status = 1");
        }}.toString();
    }

}
