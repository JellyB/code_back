package com.huatu.tiku.position.base.config;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

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