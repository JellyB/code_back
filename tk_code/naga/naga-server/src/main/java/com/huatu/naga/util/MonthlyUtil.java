package com.huatu.naga.util;

import com.huatu.common.utils.date.DateFormatUtil;

/**
 * @author hanchao
 * @date 2018/1/24 14:54
 */
public class MonthlyUtil {
    public static String current(){
        return DateFormatUtil.NORMAL_MONTH_FORMAT.format(System.currentTimeMillis());
    }
    public static String minute(){
        return String.valueOf(System.currentTimeMillis()/60000L);
    }
}
