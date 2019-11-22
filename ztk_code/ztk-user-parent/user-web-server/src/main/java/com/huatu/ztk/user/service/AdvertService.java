package com.huatu.ztk.user.service;

import com.huatu.ztk.user.bean.Advert;
import com.huatu.ztk.user.dao.AdvertDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 广告service层
 * Created by shaojieyue
 * Created time 2016-06-29 15:07
 */

@Service
public class AdvertService {
    private static final Logger logger = LoggerFactory.getLogger(AdvertService.class);

    @Autowired
    private AdvertDao advertDao;

    /**
     * 查询手机端广告列表
     * @return
     */
    public List<Advert> queryMobileAdverts(){
        final List<Advert> adverts = advertDao.queryMobileAdverts();
        return adverts;
    }

    /**
     * 根据id查询广告对象
     * @param id
     * @return
     */
    public Advert findById(long id) {
        return advertDao.findById(id);
    }
}
