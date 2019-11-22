package com.huatu.ztk.paper.task;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.controller.InitController;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 真题导入
 * Created by shaojieyue
 * Created time 2016-07-19 09:23
 */
public class PastpaperSyncTask implements MessageListener{
    private static final Logger logger = LoggerFactory.getLogger(PastpaperSyncTask.class);

    @Autowired
    private InitController initController;

    @Override
    public void onMessage(Message message) {
        String text = new String(message.getBody());
        logger.info("receive message={}",text);
        try {
            final Map data = JsonUtil.toMap(text);
            final int puKey = MapUtils.getIntValue(data, "puKey",-1);
            if (puKey<0) {
                return;
            }
            //导入新试卷
            initController.import2mongo(puKey,null);
        }catch (Exception e){
            logger.error("ex，data={}",text,e);
        }
    }
}
