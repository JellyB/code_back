package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.vo.admin.UserAccountDetailVO;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by x6 on 2017/12/18.
 * 用户剩余金币
 */
@Data
public class UserAccountVO implements Serializable {

    private UserAccountDetailVO userCountres;

}
