package com.huatu.tiku.teacher.util.dingTalkNotice;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/29
 * @描述
 */
@Data
@NoArgsConstructor
public class DingTextVo extends DingBaseVo {


    private Map text;
    private Map at;

    @Builder
    public DingTextVo(String content, Boolean atAll, String mobiles) {
        msgtype = "text";
        text = new HashMap();
        HashMap contentMap = new HashMap();
        contentMap.put("content", content);
        text = contentMap;
        at = new HashMap();
        if (StringUtils.isNotEmpty(mobiles)) {
            String[] mobileArray = mobiles.split(",");
            HashMap atMap = new HashMap();
            atMap.put("atMobiles", mobileArray);
            atMap.put("isAtAll", atAll);
            at = atMap;
        }
    }
}
