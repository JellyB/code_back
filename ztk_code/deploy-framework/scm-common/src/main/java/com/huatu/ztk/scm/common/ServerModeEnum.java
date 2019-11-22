/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm.common;

/**
 * 服务运行模式枚举
 * @author wenpingliu
 * @version v 0.1 15/6/17 13:24 wenpingliu Exp $$
 */
public enum ServerModeEnum {

    DEVELOP(1,"develop"),
    ONLINE(2,"online"),
    TEST(3,"test");

    int mode;
    String envStr;

    private ServerModeEnum(int mode,String envStr) {
        this.mode = mode;
        this.envStr = envStr;
    }

    public static ServerModeEnum getServerModeEnum(String envStr){
        for(ServerModeEnum serverModeEnum : ServerModeEnum.values()){
            if(serverModeEnum.getEnvStr().equals(envStr)){
                return serverModeEnum;
            }
        }
        return null;
    }

    public int getMode() {
        return mode;
    }

    public String getEnvStr() {
        return envStr;
    }
}
