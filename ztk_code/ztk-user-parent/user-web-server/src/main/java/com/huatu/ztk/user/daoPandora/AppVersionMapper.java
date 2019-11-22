package com.huatu.ztk.user.daoPandora;

import com.huatu.tiku.entity.AppVersion;
import com.huatu.ztk.user.daoPandora.provider.AppVersionProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-09-06 下午5:41
 **/
@Repository
public interface AppVersionMapper extends Mapper<AppVersion>{

    @SelectProvider(type = AppVersionProvider.class, method = "findLastedVersion")
    HashMap<String,Object> findLatestVersion(@Param("terminal") int terminal, @Param("appName") int appName);
}
