package com.huatu.ztk.user.common;

import com.huatu.ztk.commons.TerminalType;

/**
 * Created by linkang on 8/11/16.
 */
public class CourseSourceType {
    public final static int IOS = 1;
    public final static int ANDROID = 2;


    /**
     * 来源，转换
     * @param terminal
     * @return
     */
    public final static int getSourceTypeByTerminal(int terminal) {
        int source = CourseSourceType.IOS;
        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            source = CourseSourceType.ANDROID;
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD){
            source = CourseSourceType.IOS;
        }
        return source;
    }
}
