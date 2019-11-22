package com.huatu.ztk.user.daoPandora.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-25 3:11 PM
 **/
@Slf4j
public class IconProvider {

    public String iconList(@Param(value = "subject") Integer subject){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" select subject, type, name, url, sort ");
        stringBuilder.append(" from icon where status = 1 and biz_status = 2 and subject = ");
        stringBuilder.append(subject);
        stringBuilder.append(" order by sort asc");
        log.info("icon config sql:{}", stringBuilder.toString());
        return stringBuilder.toString();
    }
}
