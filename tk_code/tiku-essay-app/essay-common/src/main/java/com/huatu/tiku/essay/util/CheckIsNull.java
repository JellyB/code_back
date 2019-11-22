package com.huatu.tiku.essay.util;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

/**
 * Create by jbzm on 171216
 */
@Slf4j
public class CheckIsNull {
    public static boolean checkObjFieldIsNull(Object obj,ErrorResult errorResult)  {

        boolean flag = true;
        for(Field f : obj.getClass().getDeclaredFields()){
            f.setAccessible(true);
            log.info(f.getName());
            try {
                if (f.get(obj) == null) {
                    log.warn(">>>>字段值为空。字段名称："+f.getName()+"<<<<");
                    throw new BizException(errorResult);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return flag;
    }
    public static Double KeepTwoDecimal(Double aDouble) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (aDouble == null) {
            return 0x0.0p0;
        }else{
            return Double.valueOf(decimalFormat.format(aDouble));
        }
    }
}
