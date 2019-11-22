package com.huatu.ztk.pc.util;

import com.huatu.ztk.pc.bean.Share;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Random;

/**
 * Created by huangqp on 2018\5\24 0024.
 */
public class WxChatShareUtil {
    public static void assertWeiXinInfo(Map data, Share share){
        data.put("title",share.getTitle());
        data.put("description",share.getDesc());
        data.put("url",share.getUrl());
        data.put("imgUrl","http://tiku.huatu.com/cdn/share/img/icon_app.png");
    }


}

