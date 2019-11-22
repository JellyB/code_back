package com.huatu.ztk.scm.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
/**
 * 通过spring上下文获取bean工具类
 * @author shaojieyue
 * @date 2013-08-20 11:52:34
 */
public class SpringBeanUtils implements ApplicationContextAware {
	
	public static ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext=applicationContext;

	}
	
	/**
	 * 通过bean名称获取指定的类型的bean
	 * @param beanName bean name
	 * @param T 该bean的类型
	 * @return
	 */
	public static <T> T getBean(String beanName,Class T){
		return (T)applicationContext.getBean(beanName, T);
	}
	
	/**
	 * 根据bean名称获取bean
	 * @param beanName
	 * @return
	 */
	public static Object getBean(String beanName){
		return applicationContext.getBean(beanName);
	}

}
