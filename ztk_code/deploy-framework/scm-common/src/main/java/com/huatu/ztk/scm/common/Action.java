package com.huatu.ztk.scm.common;

/**
 * server自动化部署启动命令集
 *
 * @author shaojieyue
 * @date 2013-07-15 14:27:26
 */
public enum Action {
    // 工程打包
    PACKAGE("package",10),

    // 工程部署分发
    DEPLOY("deploy",10),

    // 启动指定的server
    SERVER_START("start",10),

    // 停止指定的server
    SERVER_STOP("stop",10),

    // dump指定的server
    SERVER_DUMP("dump",100),

    // 重启指定server
    SERVER_RESTART("restart",10),

    // 初始化server
    SERVER_INIT("init_server",10),

    // delete server instance
    SERVER_DELETE("delete_server",10),
    
    // server的基本配置更新
    SERVER_UPDATE("update",10),
    
    // 公共lib库
    lib("lib",10);

    private String command;

    private int timeOut;

    public static Action getAction(String actionCommand) {
        for (Action action : Action.values()) {
            if (action.getCommand().equals(actionCommand)) {
                return action;
            }
        }
        return null;
    }

    public String getCommand() {
        return this.command;
    }

    public int getTimeOut() {
        return timeOut;
    }

    private Action(String action, int timeOut) {
        this.command = action;
        this.timeOut = timeOut;
    }
}
