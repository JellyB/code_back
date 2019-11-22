package com.huatu.common.jwt.bean;

import lombok.Data;

/**
 * @author hanchao
 * @date 2018/3/26 14:22
 */
@Data
public class JWTUser {
    private int ssoId;
    private int id;
    private String nick;
    private String uname;

    public int getSsoId() {
        return ssoId;
    }

    public JWTUser setSsoId(int ssoId) {
        this.ssoId = ssoId;
        return this;
    }

    public int getId() {
        return id;
    }

    public JWTUser setId(int id) {
        this.id = id;
        return this;
    }

    public String getNick() {
        return nick;
    }

    public JWTUser setNick(String nick) {
        this.nick = nick;
        return this;
    }

    public String getUname() {
        return uname;
    }

    public JWTUser setUname(String uname) {
        this.uname = uname;
        return this;
    }
}
