package com.huatu.tiku.interview.controller.api.v1;

import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.vo.request.PaperCommitVO;
import com.huatu.tiku.interview.service.ClassInteractionService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by x6 on 2018/4/11.
 * 课堂互动题目管理接口
 */
@RestController
@RequestMapping("/api/interaction")
@Slf4j
public class ApiClassInteractionController {
    @Autowired
    private ClassInteractionService classInteractionService;


    /**
     * 查询试卷信息
     */
    @LogPrint
    @GetMapping("paper")
    public Result paper(@RequestParam String openId,
                          @RequestParam long paperId) {

        log.debug("用户openId:{},试卷id：{}",openId,paperId);
        return Result.ok(classInteractionService.getPaperDetail(openId,paperId));
    }


    /**
     * 用户交卷
     */
    @LogPrint
    @PostMapping("answer")
    public Result answer(@RequestBody PaperCommitVO vo) {

        return classInteractionService.answer(vo);
    }

}
