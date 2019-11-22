package com.huatu.tiku.interview.entity.template;

import com.huatu.tiku.interview.constant.TemplateEnum;
import com.huatu.tiku.interview.util.json.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/12 16:28
 * @Description 模板消息
 */
@Data
@AllArgsConstructor
@Builder
public class WechatTemplateMsg {

    private String touser; //接收者openid

    private String template_id; //模板ID

    private String url; //模板跳转链接

//    private Map<String, TemplateData> data;

    // "miniprogram":{ 未加入
    // "appid":"xiaochengxuappid12345",
    // "pagepath":"index?foo=bar"
    // },

    public WechatTemplateMsg(TemplateEnum templateEnum,Map<String, String> requestMap) {
        this.template_id = templateEnum.getTemplateId();
        this.url = templateEnum.getUrl();
        this.data = templateEnum.getData();
        this.touser = requestMap.get("FromUserName");
    }

    public WechatTemplateMsg(String touser,TemplateEnum templateEnum) {
        this.url = templateEnum.getUrl();
        this.data = templateEnum.getData();
        this.touser = touser;
        this.template_id = templateEnum.getTemplateId();
    }



    public static String getJson(TemplateEnum templateEnum,Map<String, String> requestMap){
        return JsonUtil.toJson(new WechatTemplateMsg(templateEnum,requestMap));
    }

    private TreeMap<String, TreeMap<String, String>> data; //data数据

//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//


    /**
     * 参数
     * @param value
     * @param color 可不填
     * @return
     */
    public static TreeMap<String, String> item(String value, String color) {
        TreeMap<String, String> params = new TreeMap<String, String>();
        params.put("value", value);
        params.put("color", color);
        return params;
    }
}