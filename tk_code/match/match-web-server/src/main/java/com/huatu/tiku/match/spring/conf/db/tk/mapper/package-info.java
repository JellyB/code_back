/**
 * mybatis 多数据源配置 基于 druid
 * 如果使用多数据源，在从库的中 如果出现事物失效的情况，需要在声明事物时候声明事物名称，否则会默认使用主库的事物对象，导致事物回滚失败
 * Created by lijun on 2018/10/10
 */
package com.huatu.tiku.match.spring.conf.db.tk.mapper;