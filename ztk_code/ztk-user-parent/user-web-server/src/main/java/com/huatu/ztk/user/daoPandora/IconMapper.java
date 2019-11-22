package com.huatu.ztk.user.daoPandora;

import com.huatu.tiku.entity.Icon;
import com.huatu.ztk.user.daoPandora.provider.IconProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-25 1:36 PM
 **/
@Repository
public interface IconMapper extends Mapper<Icon>{

    @SelectProvider(type = IconProvider.class, method = "iconList")
    List<HashMap<String,Object>> list(@Param(value = "subject") Integer subject);
}
