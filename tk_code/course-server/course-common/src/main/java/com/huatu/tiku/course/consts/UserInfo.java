package com.huatu.tiku.course.consts;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-06 9:37 PM
 **/
@Getter
@Setter
@NoArgsConstructor
public class UserInfo implements Serializable {
    private String uname;
    private String ucId;
    private String time;
    private int coins;

    @Builder
    public UserInfo(String uname, String ucId, String time, int coins) {
        this.uname = uname;
        this.ucId = ucId;
        this.time = time;
        this.coins = coins;
    }
}
