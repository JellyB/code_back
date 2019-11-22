package com.huatu.tiku.course.web.controller;

import com.google.common.collect.Lists;
import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.course.service.cache.OrderCacheKey;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/3/28
 * @描述
 */
@Slf4j
@RestController
@RequestMapping(value = "estimateSendCoins")
public class EstimateSendCoinsController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    //线上用户名称（app_ztk1656238565,app_ztk338350685）
    //测试代码
        /*userNames.add("app_ztk1656238565");
        userNames.add("app_ztk338350685");*/

    @RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public void estimateSendCoins(@RequestParam String courseIds) {
        List<Integer> courseIdList = Arrays.stream(courseIds.split(",")).map(Integer::new).collect(Collectors.toList());
        log.info("课程ID是:{}", courseIdList);

        Set<String> userNames = new HashSet();
        courseIdList.forEach(courseId -> {
            SetOperations setOperations = redisTemplate.opsForSet();
            String zeroOrderKey = OrderCacheKey.zeroOrder(courseId);
            log.info("课程key是:{}", zeroOrderKey);
            Set<String> members = setOperations.members(zeroOrderKey);
            userNames.addAll(members);
        });

        log.info("参加用户总数是:{}", userNames.size());
        List<String> removeUserNames = getRemoveIds();
        log.info("需要剔除的用户数量是:{}", removeUserNames.size());
        userNames.removeAll(removeUserNames);
        log.info("赠送用户数量是:{},赠送的用户name是:{}", userNames.size());

        userNames.stream().forEach(userName -> {
            RewardMessage msg = RewardMessage.builder().gold(500).action(RewardAction.ActionType.ACTIVTY.name())
                    .experience(1).bizId(System.currentTimeMillis() + "").uname(userName)
                    .timestamp(System.currentTimeMillis()).build();
            rabbitTemplate.convertAndSend("", RabbitConsts.QUEUE_REWARD_ACTION, msg);

        });
        log.info("赠送金币完成");
    }

    @RequestMapping(value = "second", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public void estimateSendCoinsTest(@RequestParam String courseIds) {
        List<Integer> courseIdList = Arrays.stream(courseIds.split(",")).map(Integer::new).collect(Collectors.toList());
        log.info("课程ID是:{}", courseIdList);

        Set<String> userNames = new HashSet<>();
        userNames.add("app_ztk1656238565");
        userNames.add("app_ztk338350685");

        userNames.stream().forEach(userName -> {
            RewardMessage msg = RewardMessage.builder().gold(500).action(RewardAction.ActionType.ACTIVTY.name())
                    .experience(1).bizId(System.currentTimeMillis() + "").uname(userName)
                    .timestamp(System.currentTimeMillis()).build();
            rabbitTemplate.convertAndSend("", RabbitConsts.QUEUE_REWARD_ACTION, msg);

        });
    }


    public List<String> getRemoveIds() {
        // TODO 调用paper接口获取所有的用户名称
        List<String> removeUserNames = Lists.newArrayList("2848310640", "httk_2878477", "httk_2900666", "httk_2924287", "app_8895035", "httk_54d99e3f", "httk_54dab8f2", "httk_550fc9df", "httk_55668b24", "httk_55c1be73e7491", "httk_55d5820e2ccac", "ht_5916902", "htwxm_4284178", "httk_5667c8342ea26", "htwx_4513543", "app_ztk1279658096", "httk_5689e5266af1f", "httk_56cd1919d5043", "app_ztk795975766", "httk_56cfcbad7646d", "httk_57070e23f416c", "httk_570cb05bb1a39", "htwxm_4338576", "httk_57888343cbf6e", "httk_578d8f5744edc", "httk_57b2b92478f06", "httk_57b7cdba3c015", "app_ztk1689992750", "httk_57c8df3c187c8", "ht_1986881", "app_ztkwx90958388", "app_ztk2051693607", "app_ztk887888998", "htwx_6011009", "htwx_3558087", "14785839267", "app_ztk660615658", "httk_589c452ece435", "ht_63561461", "htwx_6231623", "htwx_6368213", "app_ztk1890682185", "httk_58ba2a9545250", "htwxm_6273424", "app_ztk1039772218", "htwxmm_6332368", "htwx_6595425", "18353120290", "httk_58cfb4af46eac", "015912150530", "18136480093", "httk_581ffb207f26e", "015094570521", "httk_58fc8c02c30d1", "app_ztk2023209186", "18602240372", "htwx_7101060", "013293502438", "app_ztk1738966916", "app_ztk1982889225", "app_ztk533208852", "htwx_6462227", "app_ztk832976977", "app_ztk1031013639", "app_ztk1161675323", "app_ztk125530998", "app_ztk875896919", "app_ztk226751980", "013987241420", "15979566826", "app_ztk1232168386", "app_ztk185080070", "app_ztk2037386192", "xue_app_17826856244", "15840624950", "18826616691", "app_ztk737019860", "xue_app_13413654378", "app_ztk1028885267", "app_ztk106985109", "app_ztk1013595215", "app_ztk1783729008", "app_ztk608991835", "app_ztk232281985", "xue_app_18734797805", "app_ztk321381828", "18616262092", "019908771234", "app_ztk1392836998", "app_ztk1269389516", "app_ztk226983057", "app_ztk98198819", "app_ztk1987605651", "app_ztk195659698", "app_ztk1875683657", "htwx_8622265", "app_ztk1056858827", "app_ztk286756588", "xue_app_15171050216", "app_ztk561063526", "app_ztk1872788301", "app_ztk1258285738", "app_ztk898893786", "app_ztk912690892", "app_ztk211885978", "app_ztk819028025", "app_ztk236038888", "xue_app_13070595429", "app_ztk60831871", "app_ztk1518997886", "app_ztk1398313860", "18284908262", "app_ztk501717182", "app_ztk1989068108", "app_ztk621261715", "xue_app_15873146415", "xue_15935600732", "xue_app_15770665611", "app_ztk1710920385", "app_ztk823875373", "app_ztk62379289", "app_ztk929816185", "app_ztk951188358", "app_ztk316288591", "app_ztk1828573553", "xue_web_18220037923", "app_ztk1388523680", "xue_13239914138", "app_ztk1763886310", "xue_13407173189", "app_ztk522628190", "app_ztk1731785799", "app_ztk980285683", "app_ztk1162856765", "xue_app_15577489804", "app_ztk2050670821", "app_ztk1959186927", "app_ztk858671592", "ht_95797279", "xue_app_13694010196", "app_ztk1909095268", "app_ztk28213581", "xue_app_13634421415", "htwx_6114526", "xue_app_18168778589", "app_ztk969112516", "xue_app_13225335483", "app_ztk1121500133", "app_ztk178080815", "app_ztk190059357", "app_ztk90055258", "app_ztk209228987", "app_ztk1118659276", "app_ztk1888795828", "app_ztk333768208", "xue_app_15824215807", "app_ztk1335093819", "xue_app_18088732578", "xue_app_17693298328", "app_ztk1835101250", "app_ztk1891839766", "app_ztk609568675", "app_ztk1122898613", "app_ztk708001776", "app_ztk1126861832", "app_ztk2098588988", "app_ztk587298588", "app_ztk1235868660", "app_ztk701000793", "app_ztk860538086", "xue_app_15398721008", "app_ztk1529719327", "xue_wap_15597939397", "app_ztk962723601", "app_ztk977267809", "app_ztk962977995", "app_ztk1751370916", "app_ztk619768520", "htwx_6504015", "app_ztk157871292", "app_ztk1870260321", "htwxm_4913247", "app_ztk1297522773", "app_ztk888979196", "app_ztk1712887157", "ht_21308230", "app_ztk1396807633", "app_ztk972870878", "app_ztk86528839", "app_ztk985321850", "app_ztk1986666823", "app_ztk183788755", "app_ztk1588098083", "app_ztk1331073861", "app_ztk1306682618", "xue_15087276733", "xue_13290046110", "app_ztk1568378883", "app_ztk132682789", "app_ztk1890130869", "ht_47499757", "app_ztk1977288550", "app_ztk1687158602", "app_ztk873310780", "app_ztk882163887", "app_ztk827598287", "xue_17784563337", "app_ztk2061595257", "app_ztk778618961", "app_ztk816097152", "app_ztk121768837", "app_ztk1105707562", "xue_app_18831810989", "app_ztk1826659395", "app_ztk6587116", "app_ztk1820117318", "app_ztk2026398508", "app_ztk1779180381", "app_ztk868518853", "app_ztk1818026051", "app_ztk1067133751", "app_ztk287877788", "app_ztk52867950", "app_ztk807988508", "xue_app_17394511512", "app_ztk198052236", "app_ztk1563907986", "xue_15262890229", "app_ztk858758237", "app_ztk592658818", "app_ztk1588569536", "app_ztk1111521815", "app_ztk1880861101");
        return removeUserNames;
    }
}
