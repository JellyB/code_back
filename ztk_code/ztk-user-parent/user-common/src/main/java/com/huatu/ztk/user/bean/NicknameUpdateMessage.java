package com.huatu.ztk.user.bean;

import lombok.Builder;
import lombok.Data;

/**
 * @author hanchao
 * @date 2017/11/1 10:42
 */
@Data
@Builder
public class NicknameUpdateMessage {
    private String username;
    private String nickname;
    private String nicknameOld;
    private String avatar;

    public NicknameUpdateMessage() {
    }

    public NicknameUpdateMessage(String username, String nickname, String nicknameOld,String avatar) {
        this.username = username;
        this.nickname = nickname;
        this.nicknameOld = nicknameOld;
        this.avatar = avatar;
    }
}
