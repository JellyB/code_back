package com.huatu.tiku.mapper;

import com.huatu.common.bean.BaseEntity;
import com.huatu.tiku.provider.InsertCompeteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by lijun on 2018/7/5
 */
@Mapper
public interface InsertCompeteMapper<T extends BaseEntity> {

    /**
     * 批量插入
     */
    @InsertProvider(type = InsertCompeteProvider.class, method = "insertAll")
    int insertAll(@Param("arg0") List<T> list);
}
