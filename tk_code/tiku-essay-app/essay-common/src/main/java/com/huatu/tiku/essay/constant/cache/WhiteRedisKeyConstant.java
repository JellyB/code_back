package com.huatu.tiku.essay.constant.cache;


import com.google.common.base.Joiner;

/**
 * 白名单用户相关缓存
 **/
public class WhiteRedisKeyConstant {
    /*
    *  白名单用户列表
    */
    public static String  getWhiteList() {
        return "white_user_list";
    }



    /**
     * 仅白名单用户可见  某地区下试卷列表
     */
    public static String  getWhitePaperList(long areaId) {
        return Joiner.on("_").join("white_paper_list",areaId);
    }



}
