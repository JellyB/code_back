package com.huatu.tiku.course.consts;


import com.huatu.common.consts.TerminalType;

/**
 * Created by linkang on 8/11/16.
 */
public class NetschoolTerminalType {
    public final static int IOS = 1;
    public final static int ANDROID = 2;


    /**
     * 来源，转换
     * @param terminal
     * @return
     */
    public final static int transform(int terminal) {
        int source = NetschoolTerminalType.IOS;
        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            source = NetschoolTerminalType.ANDROID;
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD){
            source = NetschoolTerminalType.IOS;
        }
        return source;
    }

    /**
     * 答题卡id是否需要转换为 str
     * @param terminal
     * @return
     */
    public final static boolean needTrans2Str(int terminal){
        if(terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD){
            return false;
        }else{
            return true;
        }
    }
}
