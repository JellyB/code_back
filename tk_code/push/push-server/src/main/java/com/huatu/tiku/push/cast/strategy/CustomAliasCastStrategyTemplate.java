package com.huatu.tiku.push.cast.strategy;

import com.huatu.tiku.push.cast.UmengNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-22 上午10:57
 **/


@Component(value = "customAliasCastStrategyTemplate")
@Slf4j
public class CustomAliasCastStrategyTemplate extends AbstractPushTemplate{

    @Override
    public List<UmengNotification> getNotificationList() {
        return super.getNotificationList();
    }

    @Override
    public void setNotificationList(List<UmengNotification> notificationList) {
        super.setNotificationList(notificationList);
    }

}
