package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 赠送批改次数
 * @date 2018/12/284:13 PM
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserCorrectGoodsRewardVO {

    //单题批改次数
    private int queNum;
    //套题批改次数
    private int mulNum;
    //议论文批改次数
    private int argNum;
    //账号信息列表
    private List<String> accountList;
    //文件地址
    private String url;
    //备注信息
    private String remark;

    //异常用户列表
    private List<String> errorList;

    //操作人
    private String creator;

}
