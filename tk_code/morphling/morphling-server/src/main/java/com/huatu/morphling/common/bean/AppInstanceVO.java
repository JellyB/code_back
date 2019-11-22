package com.huatu.morphling.common.bean;

import lombok.Data;

import java.util.Date;

/**
 * @author hanchao
 * @date 2017/12/5 13:18
 */
@Data
public class AppInstanceVO {
    private int id;
    private String host;
    private int port;
    private int clientPort;
    private String currentVersion;
    private String clientName;
    private String appName;
    private String contextPath;
    private byte status;
    private Date updateTime;

    private boolean registEnabled;
    private boolean registStatus;

}
