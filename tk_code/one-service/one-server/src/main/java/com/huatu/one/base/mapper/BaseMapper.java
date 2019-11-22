package com.huatu.one.base.mapper;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * 基础Mapper
 *
 * @author songxiao
 */
public interface BaseMapper<T> extends Mapper<T>, InsertListMapper<T> {

}
