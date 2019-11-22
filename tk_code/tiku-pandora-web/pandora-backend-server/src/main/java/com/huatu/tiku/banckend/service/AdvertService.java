package com.huatu.tiku.banckend.service;

import com.huatu.tiku.entity.Advert;

import java.util.List;

/**
 * Created by lijun on 2018/5/31
 */
public interface AdvertService {

    /**
     * 所有条件的分页查询
     *
     * @param title       标题
     * @param target      点击跳转目的地
     * @param type        类型
     * @param status      状态
     * @param category    科目
     * @param appType     App 类型
     * @param onLineTime  上线时间
     * @param offLineTime 下线时间
     * @return
     */
    List<Advert> getAdvertByAllConditions(String title, String target, int type, int status,
                                          int category, int appType, long onLineTime, long offLineTime, String platForm, int subject, int cateId);


    /**
     * 根据类型和科目查询数据
     *
     * @return
     */
    List<Advert> selectByTypeAndCategory(int type, int category, int status);

    /**
     * 保存数据
     *
     * @param id
     * @param target      点击跳转目的地
     * @param title       标题
     * @param params      参数
     * @param category    科目
     * @param type        首页图类型
     * @param imagesSrc
     * @param newVersion  是否是新版本 默认不是
     * @param position
     * @param appType     app类型
     * @param onlineTime
     * @param offlineTime
     * @param index
     * @param padImageUrl 平板图片ImageUrl
     * @return
     */
    int save(int id, String target, String title, String params, int category, int type,
             String imagesSrc, int newVersion, int position, int appType,
             long onlineTime, long offlineTime, int index, long courseCollectionId, String padImageUrl, String platForm, int subject, int cateId, int mId, String mTitle);


    /**
     * 删除数据
     *
     * @param id 删除id
     * @return
     */
    int delete(int id);

    /**
     * 修改上线、下线状态值
     *
     * @param id 修改的ID
     * @return
     */
    int updateStatus(int id);

    /**
     * 根据id 查询数据
     *
     * @param id 查询ID
     */
    Advert detail(int id);
}
