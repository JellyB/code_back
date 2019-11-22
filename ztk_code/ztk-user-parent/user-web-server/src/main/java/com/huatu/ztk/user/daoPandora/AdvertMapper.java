package com.huatu.ztk.user.daoPandora;

import com.huatu.tiku.entity.Advert;
import com.huatu.ztk.user.daoPandora.provider.AdvertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * 广告轮播图管理（pandora.advert）
 */
@Repository
public interface AdvertMapper extends Mapper<Advert> {


    /**
     * @return
     */
    @SelectProvider(type = AdvertProvider.class, method = "findAdvert")
    List<HashMap<String, Object>> findAdvert(int category, int type, int newVersion, int appType);

    @SelectProvider(type = AdvertProvider.class, method = "findByIds")
    List<HashMap<String, Object>> findByIds(@Param("ids") String ids);

    /**
     * 查询 m 站广告轮播图
     * @return
     */
    @SelectProvider(type = AdvertProvider.class, method = "findMAdvert")
    List<HashMap<String, Object>> findMAdvert();

}
