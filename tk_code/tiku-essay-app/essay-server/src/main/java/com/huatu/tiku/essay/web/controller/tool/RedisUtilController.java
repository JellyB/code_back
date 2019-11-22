package com.huatu.tiku.essay.web.controller.tool;

import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.huatu.tiku.essay.constant.cache.RedisKeyConstant.ESSAY_GOODS_FREE_KEY;
import static com.huatu.tiku.essay.constant.cache.RedisKeyConstant.PHOTO_ANSWER_KEY;
import static com.huatu.tiku.essay.constant.cache.RedisKeyConstant.VOICE_ANSWER_KEY;

/**
 * @author zhouwei
 * @Description: 无敌工具类
 *
 * 1、修改试题 材料 绑定关系需要d2
 * 2、修改材料具体内容需要调用d3
 *
 *
 * @create 2017-12-16 下午2:50
 **/
@RestController
@RequestMapping("api/util")
@Slf4j
public class RedisUtilController {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private EssayQuestionPdfService essayQuestionPdfService;

   /**
     * 删除单题列表五个类型的缓存数据接口
     *
     * @return
     */
    @GetMapping("d1")
    public Object deleteSingleQuestionTypeCache() {
        redisTemplate.opsForValue().getOperations().delete(RedisKeyConstant.SINGLE_QUESTION_TYPE_PREFIX);
        return "哈哈哈，这功能牛逼了。";
    }

    /**
     *
     * 删除指定QuestionBaseId 对应材料ID列表接口
     *
     * @return
     */
    @GetMapping("d2")
    public Object deleteQuestionBaseIdRelativeMaterialsCache(long id) {

        redisTemplate.opsForValue().getOperations().delete(RedisKeyConstant.getSingleQuestionMaterialKey(id));
        return "哈哈哈，这功能牛逼了。";
    }

    /**
     * 更改 批改是否免费(0  免费  1收费)
     * @return
     */
    @GetMapping("free/{free}")
    public Object updateCorrectFree(@PathVariable(required = true) int free) {
       redisTemplate.opsForValue().set(ESSAY_GOODS_FREE_KEY,free);
        return "哈哈哈，这功能牛逼了。";
    }
    /**
     * 更改 是否支持拍照答题(0  开启  1关闭)
     * @return
     */
    @GetMapping("photo/{photoAnswer}")
    public Object updatePhotoAnswer(@PathVariable(required = true) int photoAnswer) {
        int o = (int )redisTemplate.opsForValue().get(PHOTO_ANSWER_KEY);
        redisTemplate.opsForValue().set(PHOTO_ANSWER_KEY,photoAnswer);
        return "哈哈哈，这功能牛逼了。";
    }

    /**
     * 更改 是否支持语音答题(0  开启  1关闭)
     * @return
     */
    @GetMapping("voice/{voiceAnswer}")
    public Object updateVoiceAnswer(@PathVariable(required = true) int voiceAnswer) {
        redisTemplate.opsForValue().set(VOICE_ANSWER_KEY,voiceAnswer);
        return "哈哈哈，这功能牛逼了。";
    }
    /**
     * 处理html中的标签问题
     *
     * @return
     */
    @GetMapping("html")
    public Object htmlProcess() {

        essayQuestionPdfService.htmlProcess();
        return "html标签统一处理结束";
    }




    /**
     * 手动清除模考材料的缓存
     */
    @GetMapping("mock/material/{id}")
    public Object deleteMockMaterialCache(@PathVariable(required = true) int id) {
        redisTemplate.opsForValue().getOperations().delete(RedisKeyConstant.getPaperMaterialKey(id));
        return "手动清除模考材料的缓存成功";
    }



    /**
     * 手动清除模考试卷信息的缓存
     */
    @GetMapping("mock/paper/{id}")
    public Object deleteMockPaperCache(@PathVariable(required = true) int id) {
        redisTemplate.opsForValue().getOperations().delete(RedisKeyConstant.getPaperBaseKey(id));
        return "手动清除模考试卷信息的缓存成功";
    }



    /**
     * 手动清除模考信息的缓存
     */
    @GetMapping("mock/detail/{id}")
    public Object deleteMockDetailCache(@PathVariable(required = true) int id) {
        redisTemplate.opsForValue().getOperations().delete(RedisKeyConstant.getMockDetailPrefix(id));
        return "手动清除模考信息的缓存成功";
    }



}
