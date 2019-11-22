package com.huatu.tiku.position.biz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**公众号用户信息返回dto
 * @author wangjian
 **/
@Getter
@Setter
public class MsgListUserInfoDto implements Serializable {

    private static final long serialVersionUID = 8781791818992937902L;

    private List<MsgUserDto> user_info_list;

    private String errcode;

    private String errmsg;

}
