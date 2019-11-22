package com.huatu.springboot.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.huatu.common.db.MultiDataSources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/1/14 23:03
 */
@Configuration
@EnableConfigurationProperties(DruidProperties.class)
@ConditionalOnClass(DruidDataSource.class)
public class DruidAutoconfiguration {



    @Configuration
    @ConditionalOnProperty(prefix = "spring.datasource.druid.monitor",havingValue = "enabled",value = "enabled",matchIfMissing = false)
    protected static class DruidMonitorConfiguration{
        @Autowired
        private DruidProperties druidProperties;


        @Bean
        public ServletRegistrationBean druidStatViewServlet(){
            //org.springframework.boot.context.embedded.ServletRegistrationBean提供类的进行注册.
            ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(),druidProperties.getMonitor().getDruidStatView());

            //添加初始化参数：initParams

            //白名单：
            servletRegistrationBean.addInitParameter("allow",druidProperties.getMonitor().getAllow());
            //IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to view this page.
            servletRegistrationBean.addInitParameter("deny",druidProperties.getMonitor().getDeny());
            //登录查看信息的账号密码.
            servletRegistrationBean.addInitParameter("loginUsername",druidProperties.getMonitor().getLoginUsername());
            servletRegistrationBean.addInitParameter("loginPassword",druidProperties.getMonitor().getLoginPassword());
            //是否能够重置数据.
            servletRegistrationBean.addInitParameter("resetEnable",druidProperties.getMonitor().getResetEnable());
            return servletRegistrationBean;
        }

        @Bean
        public FilterRegistrationBean druidStatFilter(){

            FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());

            //添加过滤规则.
            filterRegistrationBean.addUrlPatterns(druidProperties.getMonitor().getDruidWebStatFilter());

            //添加不需要忽略的格式信息.
            filterRegistrationBean.addInitParameter("exclusions",druidProperties.getMonitor().getExclusions());
            return filterRegistrationBean;
        }

    }


    @Configuration
    @AutoConfigureAfter(DruidMonitorConfiguration.class)
    @ConditionalOnExpression("'${spring.datasource.url:}' != ''")
    protected static class DruidDataSourceConfiguration {
        @Autowired
        private DruidProperties druidProperties;

        @Autowired
        private DataSourceProperties dataSourceProperties;


        @Bean
        @Primary
        @ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
        public DataSource dataSource() throws SQLException {
            druidProperties.setUrl(dataSourceProperties.getUrl());
            druidProperties.setUsername(dataSourceProperties.getUsername());
            druidProperties.setPassword(dataSourceProperties.getPassword());
            return DruidDataSourceFactory.createDataSource(druidProperties);
        }

    }

    @Configuration
    @AutoConfigureAfter(DruidMonitorConfiguration.class)
    protected static class DruidMultiDataSourcesConfiguration {
        @Autowired
        private DruidProperties druidProperties;
        @Bean
        public MultiDataSources dataSourcesRegister(){
            Map<String,DataSource> datasourcesMap = new HashMap<>();
            if(druidProperties != null && druidProperties.getDatasources() != null && druidProperties.getDatasources().size() != 0){
                Map<String, DruidDataSourceProperties> datasources = druidProperties.getDatasources();
                if(datasources != null && datasources.size() > 0){
                    for (String datasourceName : datasources.keySet()) {
                        DruidDataSourceProperties druidDataSourceProperties = datasources.get(datasourceName);
                        combine(druidProperties,druidDataSourceProperties,false);
                        try {
                            DataSource dataSource = DruidDataSourceFactory.createDataSource(druidDataSourceProperties);
                            datasourcesMap.put(datasourceName,dataSource);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return new MultiDataSources(datasourcesMap);
        }

        private static void combine(Object source, Object target, boolean putNotNull) {
            if (source == null || target == null) {
                return;
            }
            Assert.state(target.getClass().isAssignableFrom(source.getClass()),"类型不一致");
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
                PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor descriptor : descriptors) {
                    Method readMethod = descriptor.getReadMethod();
                    Method writeMethod = descriptor.getWriteMethod();
                    //只设置为null的
                    if (putNotNull || readMethod.invoke(target) == null) {
                        Object value = readMethod.invoke(source);
                        if(value != null){
                            writeMethod.invoke(target,value);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IntrospectionException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}

/**
 * 用法示例:
    spring:
        druid:
            initialSize: 10
            minIdle: 10
            maxActive: 60
            maxWait: 60000
            timeBetweenEvictionRunsMillis: 60000
            minEvictableIdleTimeMillis : 300000
            poolPreparedStatements: true
            maxPoolPreparedStatementPerConnectionSize: 20
            validationQuery: "select 'x'"
            filters: stat,slf4j
            monitor:
                enabled: enabled # 配置此属性Monitor才生效
                druid-stat-view: /druid/*
                druid-web-stat-filter: /*
                allow: 219.230.50.107,127.0.0.1
                deny: 219.230.50.108
                login-username: admin
                login-password: 123456
                exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'
                reset-enable: false
            datasoruces:
                a:
                    url: ***
                    username: ***
                    initialSize: ***
                    ***
*/