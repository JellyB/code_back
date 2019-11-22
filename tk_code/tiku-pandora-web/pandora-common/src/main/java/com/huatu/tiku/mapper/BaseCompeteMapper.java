package com.huatu.tiku.mapper;

import com.huatu.common.bean.BaseEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by lijun on 2018/7/5
 */
@Mapper
public interface BaseCompeteMapper<T extends BaseEntity> extends
        InsertCompeteMapper<T> {
}
