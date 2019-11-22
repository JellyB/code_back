package com.huatu.ztk.user.common;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/9/4
 * @描述 定义渠道类型
 */
public class SourceType {

    /**
     * 大有回调地址
     */
    public static final String DA_YOU_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/322";
    /**
     * 懒猫回调地址
     */
    public static final String LAN_MAO_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/323";
    /**
     * 七麦回调地址
     */
    public static final String QI_MAI_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/314";
    /**
     * 小猫回调地址
     */
    public static final String XIAO_MAO_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/315";
    /**
     * 天天有量回调地址
     */
    public static final String TIAN_TIAN_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/325";

    /**
     * 禅大师回调地址
     */
    public static final String CHAN_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/470/1ac71c06c3aa8c0f/326";

    /**
     * 爱盈利回调地址
     */
    public static final String AI_YING_LI_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/470/1ac71c06c3aa8c0f/327";

    /**
     * 默认回调地址
     */
    public static final String DEFAULT_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/331";

    /**
     * 备用地址1
     */
    public static final String BACKUP_NUMBER1_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/328";

    /**
     * 备用地址2
     */
    public static final String BACKUP_NUMBER2_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/329";

    /**
     * 备用地址3
     */
    public static final String BACKUP_NUMBER3_CALLBACK = "http://click.track.ddashi.com/v2/5/ios/10/1ac71c06c3aa8c0f/330";


    /**
     * 小猫渠道号
     */
    public static final int XIAO_MAO_ID = 812011;
    /**
     * 七麦渠道号
     */
    public static final int QI_MAI_ID = 812012;
    /**
     * 懒猫渠道号
     */
    public static final int LAN_MAO_ID = 812013;
    /**
     * 大有渠道号
     */
    public static final int DA_YOU_ID = 812014;

    /**
     * 天天有量
     */
    public static final int TIAN_TIAN_ID = 812015;

    /**
     * 禅大师
     */
    public static final int CHAN_ID = 812016;

    /**
     * 爱盈利
     */
    public static final int AI_YING_LI_ID = 812017;
    /**
     * 备用ID1
     */
    public static final int BACKUP_NUMBER1_ID = 812018;
    /**
     * 备用ID2
     */
    public static final int BACKUP_NUMBER2_ID = 812019;
    /**
     * 备用ID3
     */
    public static final int BACKUP_NUMBER3_ID = 812020;

    /**
     * 判断是否包含指定渠道
     *
     * @param source
     * @return
     */
    public static Boolean isContains(int source) {
        List<Integer> sourceList = Lists.newArrayList(SourceType.XIAO_MAO_ID, SourceType.QI_MAI_ID,
                SourceType.LAN_MAO_ID, SourceType.DA_YOU_ID, SourceType.TIAN_TIAN_ID, SourceType.CHAN_ID
                , SourceType.AI_YING_LI_ID, SourceType.BACKUP_NUMBER1_ID, SourceType.BACKUP_NUMBER2_ID,
                SourceType.BACKUP_NUMBER3_ID);
        if (sourceList.contains(source)) {
            return true;
        }
        return false;
    }


    public static String deviceTokenKey(String deviceToken) {
        StringBuffer deviceTokenKey = new StringBuffer(64);
        return deviceTokenKey.append("user:device:token").toString();
    }
}
