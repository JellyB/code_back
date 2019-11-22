package com.huatu.tiku.interview.service.impl;
import com.huatu.tiku.interview.constant.WeChatUrlConstant;
import com.huatu.tiku.interview.service.MenuService;
import com.huatu.tiku.interview.util.WeiXinUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-04 下午1:58
 **/
@Slf4j
@Service("menuService")
public class MenuServiceImpl implements MenuService {
    @Autowired
    RestTemplate restTemplate;

    /**
     * 查询菜单
     *
     * @param accessToken 有效的access_token
     * @return
     */
    @Override
    public JSONObject getMenu(String accessToken) {

        // 拼装创建菜单的url
        String url = WeChatUrlConstant.MENU_GET_URL.replace("ACCESS_TOKEN", accessToken);
        // 调用接口查询菜单
        JSONObject jsonObject = WeiXinUtil.httpRequest(url, "GET", null);

        return jsonObject;
    }
    /**
     * 创建菜单(替换旧菜单)
     *
     * @param accessToken 有效的access_token
     * @return 0表示成功，其他值表示失败
     */
    @Override
    public int createMenu(String menu, String accessToken) {
        int result = 0;

        // 拼装创建菜单的url
        String url = WeChatUrlConstant.MENU_CREATE_URL.replace("ACCESS_TOKEN", accessToken);
        // 将菜单对象转换成json字符串
        String jsonMenu = JSONObject.fromObject(menu).toString();
        // 调用接口创建菜单
        JSONObject jsonObject = WeiXinUtil.httpRequest(url, "POST", jsonMenu);

        if (null != jsonObject) {
            if (0 != jsonObject.getInt("errcode")) {
                result = jsonObject.getInt("errcode");
                log.error("创建菜单失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
                log.error("****"+jsonMenu+"****");
            }
        }
        return result;
    }

    /**
     * 删除菜单
     *
     * @param accessToken 有效的access_token
     * @return 0表示成功，其他值表示失败
     */
    @Override
    public int deleteMenu(String accessToken) {
        int result = 0;

        // 拼装创建菜单的url
        String url = WeChatUrlConstant.MENU_DELETE_URL.replace("ACCESS_TOKEN", accessToken);

        // 调用接口创建菜单
        JSONObject jsonObject = WeiXinUtil.httpRequest(url, "POST", null);

        if (null != jsonObject) {
            if (0 != jsonObject.getInt("errcode")) {
                result = jsonObject.getInt("errcode");
                log.error("删除菜单失败 errcode:{} errmsg:{}", jsonObject.getInt("errcode"), jsonObject.getString("errmsg"));
            }
        }
        return result;
    }
}