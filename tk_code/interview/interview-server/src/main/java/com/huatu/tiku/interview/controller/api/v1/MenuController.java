package com.huatu.tiku.interview.controller.api.v1;

import com.huatu.tiku.interview.constant.WeChatUrlConstant;
import com.huatu.tiku.interview.service.MenuService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;


/**
 * @author zhouwei
 * @Description: 对服务号的菜单的操作
 * @create 2018-01-04 下午2:02
 **/
@RestController
@RequestMapping("/api/menu")
@Slf4j
public class MenuController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private MenuService menuService;

    //查询全部菜单
    @LogPrint
    @GetMapping("menus")
    public String getMenu() {
        // 调用接口获取access_token
        String at = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
        JSONObject jsonObject = null;
        if (at != null) {
            // 调用接口查询菜单
            jsonObject = menuService.getMenu(at);
            log.info("菜单列表 {}", jsonObject.toString());
            // 判断菜单创建结果
            return String.valueOf(jsonObject);
        }
        log.info("token为" + at);
        return "无数据";
    }

    //创建菜单
    @LogPrint
    @PostMapping(value = "menus")
    public int createMenu() {
        // 调用接口获取access_token
        String at = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
        int result = 0;
        if (at != null) {
            // 调用接口创建菜单
            result = menuService.createMenu(WeChatUrlConstant.MENU_CONTENT, at);
            // 判断菜单创建结果
            if (0 == result) {
                log.info("菜单创建成功！");
            } else {
                log.info("菜单创建失败，错误码：" + result);
            }
        }
        return result;
    }

    //删除菜单
    @LogPrint
    @DeleteMapping(value = "menus")
    public int deleteMenu() {
        // 调用接口获取access_token
        String at = redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN);
        int result = 0;
        if (at != null) {
            // 删除菜单
            result = menuService.deleteMenu(at);
            // 判断菜单删除结果
            if (0 == result) {
                log.info("菜单删除成功！");
            } else {
                log.info("菜单删除失败，错误码：" + result);
            }
        }
        return result;
    }



}

