package com.arj.monitor.common;

import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.type.LongType;

import java.sql.Types;

/**
 * 自定义方言，bigint和po中的long无法转换
 * 使用MySQL5Dialect 不使用MySQLDialect 是因为MySQLDialect生成表的时候回带上 type=MyISAM ，mysql5.1以后使用engine=MyISAM，type会报错
 * @author zhouwei
 */
public class CustomDialect extends MySQL5Dialect {
    public CustomDialect() {  
        super();  
        registerHibernateType(Types.BIGINT, LongType.INSTANCE.getName());
    }  
} 
