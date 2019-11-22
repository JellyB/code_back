package com.huatu.tiku.interview.service;


import net.sf.json.JSONObject;


/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-04 下午1:58
 **/
public interface MenuService {
    public JSONObject getMenu(String accessToken);
    public int createMenu(String menu, String accessToken);
    public int deleteMenu(String accessToken);
}
