package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**微信关注列表返回数据
 * @author wangjian
 **/
@Getter
@Setter
public class MsgOpenIdData implements Serializable {
    private static final long serialVersionUID = 5082808210547505209L;

    private Integer total;//总数

    private Integer count;//条数

    private Data data;//列表数据

    @Getter
    @Setter
    public class Data{

        private List<String> openid;//openId列表

        private String next_openid;//最后一个用户的OPENID
    }

    private String errcode;

    private String errmsg;
}
