package com.huatu.ztk.backend.util;

import java.math.BigDecimal;

/**
 * Created by huangqp on 2018\6\8 0008.
 */
public class NumberUtil {
    public static Double parseDoubleWithLength(double number,int length){
        BigDecimal bigDecimal = new BigDecimal(number);
        double result = bigDecimal.setScale(length,BigDecimal.ROUND_HALF_UP).doubleValue();
        return  result;
    }
}

