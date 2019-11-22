package com.huatu.tiku.teacher.util.dingTalkNotice;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/29
 * @描述
 */
@Data
@NoArgsConstructor
public class DingLinkVo extends DingBaseVo {

    private HashMap link;

    @Builder
    public DingLinkVo(String messageUrl, String picUrl, String title, String text) {
        this.msgtype = "link";
        this.link = new HashMap();
        link.put("messageUrl", messageUrl);
        link.put("picUrl", picUrl);
        link.put("title", title);
        link.put("text", text);
    }
}
