package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.tiku.essay.constant.cache.AmswerCommitRedisKey;
import com.huatu.tiku.essay.constant.cache.AreaRedisKeyConstant;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.label.LabelRedisKeyConstant;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.admin.SystemConstantVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * @author zhaoxi
 * @Description: 系统相关配置
 * @date 2018/10/8上午10:43
 */
@RestController
@Slf4j
@RequestMapping("/end/system")
public class EssaySystemController {

    @Autowired
    RedisTemplate redisTemplate;


    // 语音答题开关
    public static final String VOICE_ANSWER_KEY_ANDROID = "voice_answer_android";
    public static final String VOICE_ANSWER_KEY_IOS = "voice_answer_ios";

    // 拍照答题开关
    public static final String PHOTO_ANSWER_KEY_ANDROID = "photo_answer_android";
    public static final String PHOTO_ANSWER_KEY_IOS = "photo_answer_ios";

    // 拍照答题对接第三方
    public static final String PHOTO_ANSWER_TYPE_IOS = "photo_answer_type_ios";
    public static final String PHOTO_ANSWER_TYPE_ANDROID = "photo_answer_type_android";
    public static final String PHOTO_ANSWER_MSG = "photo_answer_msg";

    /**
     * 系统相关配置
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object essaySystem() {
        List<SystemConstantVO> list = new LinkedList<>();

        //申论搜索是否展示题干和材料的开关（true:展示,false:不展示）
        String essaySearchSwitchKey = RedisKeyConstant.getEssaySearchSwitchKey();
        SystemConstantVO essaySearchSwitchVO = SystemConstantVO.builder()
                .key(essaySearchSwitchKey)
                .value(redisTemplate.opsForValue().get(essaySearchSwitchKey))
                .desc("【搜索】是否展示题干和材料的开关，默认不展示（true:展示,false:不展示）")
                .build();
        list.add(essaySearchSwitchVO);


        //申论批改是否免费
        String essayGoodsFreeKey = RedisKeyConstant.ESSAY_GOODS_FREE_KEY;
        SystemConstantVO essayGoodsFreeVO = SystemConstantVO.builder()
                .key(essayGoodsFreeKey)
                .value(redisTemplate.opsForValue().get(essayGoodsFreeKey))
                .desc("【批改】是否免费,默认收费（0:收费 1:免费）")
                .build();
        list.add(essayGoodsFreeVO);

        //申论估分地区是否展示
        String showGufenAreaKey = AreaRedisKeyConstant.getShowGufenAreaKey();
        Integer showGufenArea = (Integer) redisTemplate.opsForValue().get(showGufenAreaKey);
        if (showGufenArea != null && showGufenArea != 0) {
            showGufenArea = 1;
        }else{
            showGufenArea = 0;
        }
        SystemConstantVO essayGufenshowVO = SystemConstantVO.builder()
                .key(showGufenAreaKey)
                .value(showGufenArea)
                .desc("【估分】估分地区是否展示（0:不展示 1:展示）")
                .build();
        list.add(essayGufenshowVO);

        //申论估分试卷列表
        String essayGuFenPaperListKey = RedisKeyConstant.getEssayGuFenPaperListKey();
        Set members = redisTemplate.opsForSet().members(essayGuFenPaperListKey);
        SystemConstantVO essayGufenPaperListVO = SystemConstantVO.builder()
                .key(essayGuFenPaperListKey)
                .value(members)
                .desc("【估分】估分试卷列表")
                .build();
        list.add(essayGufenPaperListVO);

        //语音答题开关（iOS）
        SystemConstantVO voiceAnswerAndroid = SystemConstantVO.builder()
                .key(VOICE_ANSWER_KEY_ANDROID)
                .value(null == redisTemplate.opsForValue().get(VOICE_ANSWER_KEY_ANDROID) ? 0 : redisTemplate.opsForValue().get(VOICE_ANSWER_KEY_ANDROID))
                .desc("【ANDROID】语音答题开关,默认开启（0:开 1:关）")
                .build();
        list.add(voiceAnswerAndroid);

        //语音答题开关（iOS）
        SystemConstantVO voiceAnswerIOS = SystemConstantVO.builder()
                .key(VOICE_ANSWER_KEY_IOS)
                .value(null == redisTemplate.opsForValue().get(VOICE_ANSWER_KEY_IOS) ? 0 : redisTemplate.opsForValue().get(VOICE_ANSWER_KEY_IOS))
                .desc("【IOS】语音答题开关,默认开启（0:开 1:关）")
                .build();
        list.add(voiceAnswerIOS);

        //语音答题开关（iOS）
        SystemConstantVO photoAnswerAndroid = SystemConstantVO.builder()
                .key(PHOTO_ANSWER_KEY_ANDROID)
                .value(null == redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY_ANDROID) ? 0 : redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY_ANDROID))
                .desc("【ANDROID】拍照答题开关,默认开启（0:开 1:关）")
                .build();
        list.add(photoAnswerAndroid);

        //语音答题开关（iOS）
        SystemConstantVO photoAnswerIOS = SystemConstantVO.builder()
                .key(PHOTO_ANSWER_KEY_IOS)
                .value(null == redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY_IOS) ? 0 : redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY_IOS))
                .desc("【IOS】拍照答题开关,默认开启（0:开 1:关）")
                .build();
        list.add(photoAnswerIOS);


        //定时关闭订单锁
        String orderCloseLockKey = RedisKeyConstant.getOrderCloseLockKey();
        SystemConstantVO orderCloseLockVO = SystemConstantVO.builder()
                .key(orderCloseLockKey)
                .value(null == redisTemplate.opsForValue().get(orderCloseLockKey) ? "" : redisTemplate.opsForValue().get(orderCloseLockKey))
                .desc("【定时任务锁】关闭超时订单")
                .build();
        list.add(orderCloseLockVO);

        //自动交卷
        String mockAutoSubmitLockKey = RedisKeyConstant.getMockAutoSubmitLockKey();
        SystemConstantVO mockAutoSubmitLockVO = SystemConstantVO.builder()
                .key(mockAutoSubmitLockKey)
                .value(null == redisTemplate.opsForValue().get(mockAutoSubmitLockKey) ? "" : redisTemplate.opsForValue().get(mockAutoSubmitLockKey))
                .desc("【定时任务锁】模考自动交卷")
                .build();
        list.add(mockAutoSubmitLockVO);

        //关闭超时批注
        String labelCloseLockKey = LabelRedisKeyConstant.getLabelCloseLockKey();
        SystemConstantVO labelCloseLockVO = SystemConstantVO.builder()
                .key(labelCloseLockKey)
                .value(null == redisTemplate.opsForValue().get(labelCloseLockKey) ? "" : redisTemplate.opsForValue().get(labelCloseLockKey))
                .desc("【定时任务锁】关闭超时批注")
                .build();
        list.add(labelCloseLockVO);

        //获取下一篇批注
        String nextLabelLockKey = LabelRedisKeyConstant.getNextLabelLockKey();
        SystemConstantVO nextLabelLockVO = SystemConstantVO.builder()
                .key(nextLabelLockKey)
                .value(null == redisTemplate.opsForValue().get(nextLabelLockKey) ? "" : redisTemplate.opsForValue().get(nextLabelLockKey))
                .desc("【定时任务锁】获取下一篇批注")
                .build();
        list.add(nextLabelLockVO);

        //处理批改完成答题卡
        String unCommitAnswerLockKey = AmswerCommitRedisKey.getUnCommitAnswerLockKey();
        SystemConstantVO unCommitAnswerLockVO = SystemConstantVO.builder()
                .key(unCommitAnswerLockKey)
                .value(null == redisTemplate.opsForValue().get(unCommitAnswerLockKey) ? "" : redisTemplate.opsForValue().get(unCommitAnswerLockKey))
                .desc("【定时任务锁】处理批改完成答题卡")
                .build();
        list.add(unCommitAnswerLockVO);


        //议论文批注用户名单（终审）
        String finalLabelTeacherListKey = LabelRedisKeyConstant.getFinalLabelTeacherList();
        SystemConstantVO finalLabelTeacherList = SystemConstantVO.builder()
                .key(finalLabelTeacherListKey)
                .value(redisTemplate.opsForSet().members(finalLabelTeacherListKey))
                .desc("【批注】终审名单")
                .build();
        list.add(finalLabelTeacherList);


        //议论文批注用户名单（VIP）
        String vipLabelTeacherListKey = LabelRedisKeyConstant.getVIPLabelTeacherList();
        SystemConstantVO vipLabelTeacherList = SystemConstantVO.builder()
                .key(vipLabelTeacherListKey)
                .value(redisTemplate.opsForSet().members(vipLabelTeacherListKey))
                .desc("【批注】VIP名单")
                .build();
        list.add(vipLabelTeacherList);


        //议论文批注用户名单（VIP）
        String giveUpListKey = LabelRedisKeyConstant.getGiveUpListKey();
        SystemConstantVO giveUpListVO = SystemConstantVO.builder()
                .key(giveUpListKey)
                .value(redisTemplate.opsForSet().members(giveUpListKey))
                .desc("【批注】过滤答题卡ID")
                .build();
        list.add(giveUpListVO);
        
        String jyUserListKey = RedisKeyConstant.getJYUserKey();
        SystemConstantVO jyUserListVo = SystemConstantVO.builder()
                .key(jyUserListKey)
                .value(redisTemplate.opsForSet().members(jyUserListKey))
                .desc("教育后台用户uname")
                .build();
        list.add(jyUserListVo);
        

        return list;

    }


}
