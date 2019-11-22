package com.huatu.one.base.config;

import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Mybatis配置
 *
 * @author songxiao
 */
@Configuration
@MapperScan("com.huatu.one.biz.mapper")
public class MybatisConfig {

}
