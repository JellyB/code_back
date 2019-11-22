package com.huatu.ztk.user.common;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/1
 * @描述 app_channel 记录deviceToken的激活状态
 */
public class DeviceTokenState {
    //未激活
    public static final int UN_ACTIVE_STATE = 1;
    //激活
    public static final int ACTIVE_STATE = 2;
    //未激活 && 库中已经存在
    public static final int EXIST_UN_ACTIVE_STATE = 3;
    //库中不存在
    public static final int NOT_EXIST = 4;
    //华图在线的表
    public static final String HUA_TU_ONLINE = "app_channel";
    //面库表
    public static final String MIAN_KU = "cool_app_channel";
    //重复
    public static final int REPEAT = 1;
    //不重复
    public static final int NO_REPEAT = 0;
     //是否越狱 1 非越狱 设备,2 越狱设备
    public static final int IS_BREAK_PRISON = 1;




}
