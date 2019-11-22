package com.huatu.tiku.interview.controller.api.v3;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.ClassInteractionService;
import com.huatu.tiku.interview.service.PaperInfoService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by x6 on 2018/4/11.
 * 课堂互动题目管理接口
 */
@RestController
@RequestMapping("/api/v3/interaction")
@Slf4j
public class ApiClassInteractionControllerV3 {
    @Autowired
    private ClassInteractionService classInteractionService;
    @Autowired
    private PaperInfoService paperInfoService;

    /**
     * 查询试卷信息
     */
    @LogPrint
    @GetMapping("paper")
    public Result paper(@RequestParam String openId,
                          @RequestParam long paperId,
                        @RequestParam long pushId) {

        log.debug("用户openId:{},试卷id：{},推送id：{}",openId,paperId,pushId);
        Map<String, Object> meta = paperInfoService.metaV2(paperId,0,0,pushId);
        List<Map<String,Object>> userAnswer = paperInfoService.findUserAnswer(openId, pushId);
        meta.put("answerInfo",userAnswer);

        return Result.ok(meta);
    }





}
