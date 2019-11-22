package com.huatu.tiku.schedule.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

/**
 * 处理Hibernate关系映射序列化
 * 
 * @author Geek-S
 *
 */
@Component
public class HibernateAwareObjectMapper {

	@Bean
	public Hibernate5Module hibernate5Module() {
		return new Hibernate5Module();
	}
}