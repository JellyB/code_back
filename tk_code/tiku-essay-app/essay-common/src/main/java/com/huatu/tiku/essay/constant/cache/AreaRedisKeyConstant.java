package com.huatu.tiku.essay.constant.cache;

/**
 * Created by x6 on 2018/7/3.
 * 地区相关缓存
 */
public class AreaRedisKeyConstant {


    public static final String BASE_AREA_MAP = "essay_base_area_map";

    public static final String BASE_AREA_NAME_MAP = "essay_base_area_name_map";

    /*
    *  地区Map<Key:地区id，Value：area对象>
    */
    public static String  getAreaMapKey() {
        return "essay_area_map";
    }


    /*
     * 是否展示估分地区(0不展示（默认） 1展示)
     */
    public static String  getShowGufenAreaKey() {
        return "essay_show_gufen_area_key";
    }
}

