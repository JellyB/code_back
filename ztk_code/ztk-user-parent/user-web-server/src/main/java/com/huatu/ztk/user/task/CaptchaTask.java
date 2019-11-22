package com.huatu.ztk.user.task;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.huatu.ztk.user.common.UserRedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by linkang on 2/22/17.
 */
@Component
public class CaptchaTask {
    private static final Logger logger = LoggerFactory.getLogger(CaptchaTask.class);

    @Autowired
    private RedisTemplate redisTemplate;


    private static final String commond = "grep 'send sms,mobile=' /data/logs/user-web-server/user-web-server.log  | awk -F 'client=' '{print $2}' | awk -F ',' -f /root/count_mobile.sh | sort -t = -k3 -n -r ";

    //5分钟执行一次
    @Scheduled(cron = "0 0/5 * * * ?")
    public void captchaTask() {
        logger.info("captcha task start.");

        try {
            //注:如果sh中含有awk,一定要按new String[]{"/bin/sh","-c",shStr}写,才可以获得流
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh","-c",commond},null,null);

            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";

            //等待一分钟
            process.waitFor(1, TimeUnit.MINUTES);

            logger.info("process done.");

            //手机号，发送次数 map
            Map<String, Integer> countMap = new LinkedHashMap<>();
            while ((line = input.readLine()) != null) {
                //mobile=******=4
                String[] strings = line.split("=");

                //跳过格式有问题的数据
                if (strings.length != 3) {
                    continue;
                }

                String mobile = strings[1];
                int count = Integer.valueOf(strings[2]);
                countMap.put(mobile, count);
            }

            //发送超过3次，加到拒绝
            List<String> mobiles = countMap.keySet().stream()
                    .filter(k -> countMap.get(k) > 3)
                    .collect(Collectors.toList());
            logger.info("add reject_mobiles={}", mobiles);

            final SetOperations operations = redisTemplate.opsForSet();

            //清空set
            redisTemplate.delete(UserRedisKeys.REJECT_MOBILES);

            if (CollectionUtils.isNotEmpty(mobiles)) {
                //批量添加
                operations.add(UserRedisKeys.REJECT_MOBILES, mobiles.toArray());
            }


            input.close();
        } catch (Exception ex) {
            logger.error("ex",ex);
        }
    }
}
