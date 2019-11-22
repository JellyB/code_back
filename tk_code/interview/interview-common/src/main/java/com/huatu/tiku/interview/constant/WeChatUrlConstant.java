package com.huatu.tiku.interview.constant;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-03 下午7:48
 **/
public class WeChatUrlConstant {

    //okdNOuFFsk83SU9bvABn4rJinTvs
//739040d83f6d5c73fa961e3b1a48540f     //线上环境密钥
    /* 获取用户信息  phone=xxxxx&timeStamp=xxxxxx进行3des加密传参，返回值也是3des加密的   */
    public static String PHP_GET_USER_INFO = "http://testapi.huatu.com/lumenapi/v4/common/champion/info_by_phone?cryParam=";
//            "http://testapi.huatu.com/app_ztk/getGdmsinfo.php?p=";

    public static String ACCESS_TOKEN = "ACCESS_TOKEN";
//    public static String ACCESS_TOKEN_KEY = "we_a_t_201801119";
    /*发送模板消息接口*/
    public static String TEMPLATE_NEWS_URL = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";
    /*  秘钥  */
    // public static final String ACCESS_SECRETE = "78b688afb80faf735ebf3e2a3525ef35";
    //public static final String APP_ID = "wx4e6e2dc624081b58";
    public static String TOKEN_URL_TEST = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx4e6e2dc624081b58&secret=78b688afb80faf735ebf3e2a3525ef35";
    //online
//    public static String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx53505056175d5968&secret=739040d83f6d5c73fa961e3b1a48540f";

    public static String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + BasicParameters.appID + "&secret=" + BasicParameters.appsecret;
    // 菜单创建（POST） 限1000（次/天）
    public static String MENU_CREATE_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";

    // 菜单查询（POST） 限10000（次/天）
    public static String MENU_GET_URL = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token=ACCESS_TOKEN";

    // 菜单删除（POST） 限1000（次/天）
    public static String MENU_DELETE_URL = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token=ACCESS_TOKEN";
    //客服接口发消息
    public static String CUSTOM_TEXT_URL = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=ACCESS_TOKEN";
    //根据OpenID列表群发
    public static String MESSAGE_MANY_URL = "https://api.weixin.qq.com/cgi-bin/message/mass/send?access_token=ACCESS_TOKEN";
    //获取签名的链接
    public static String TICKET_URL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?type=jsapi&access_token=ACCESS_TOKEN";
//    http://weixin.htexam.com
//    public static String MENU_CONTENT = "{\"button\":[{\"type\":\"click\",\"name\":\"课程安排\",\"key\":\"course\"},{\"type\":\"SCAN\",\"name\":\"扫码签到\",\"key\":\"sign_in\"},{\"name\":\"我的\",\"sub_button\":[{\"type\":\"view\",\"name\":\"学习的效果\",\"url\":\"http://ns.huatu.com/wx/api/lr/pre\",\"sub_button\":[]},{\"type\":\"view\",\"name\":\"个人信息\",\"url\":\"http://ns.huatu.com/wx/api/userView\",\"sub_button\":[]},{\"type\":\"click\",\"name\":\"联系客服\",\"key\":\"conn_service\"}]}]}";


    public static String MENU_CONTENT = "{\"button\":[{\"type\":\"click\",\"name\":\"课程安排\",\"key\":\"course\",\"sub_button\":[]},{\"type\":\"scancode_push\",\"name\":\"扫码签到\",\"key\":\"sign_in\",\"sub_button\":[]},{\"name\":\"我的\",\"sub_button\":[{\"type\":\"view\",\"name\":\"学习效果\",\"url\":\"http://weixin.htexam.com/wx/api/lr/pre\",\"sub_button\":[]},{\"type\":\"view\",\"name\":\"个人信息\",\"url\":\"http://weixin.htexam.com/wx/api/userView\",\"sub_button\":[]},{\"type\":\"click\",\"name\":\"联系客服\",\"key\":\"conn_service\",\"sub_button\":[]}]}]}";
    public static String SECONT_CODE_URL = "{\"action_name\":\"QR_LIMIT_STR_SCENE\",\"action_info\":{\"scene\":{\"scene_str\":\"signIn\"}}}";

//    public  String MENU_CONTENT = "{\"button\":[{\"type\":\"click\",\"name\":\"课程安排\",\"key\":\"course\"},{\"type\":\"scancode_waitmsg\",\"name\":\"扫码签到\",\"key\":\"signIn_wx\"},{\"name\":\"我的\",\"sub_button\":[{\"type\":\"view\",\"name\":\"学习效果\",\"url\":\"/wx/api/lr/pre\",\"sub_button\":[]},{\"type\":\"view\",\"name\":\"个人信息\",\"url\":"+domainName+"\"/wx/api/userView\",\"sub_button\":[]},{\"type\":\"click\",\"name\":\"联系客服\",\"key\":\"conn_service\"}]}]}";



}
