package com.huatu.tiku.essay.web.controller.admin.v2.paper;


import com.huatu.common.SuccessMessage;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.CommonOperateEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.paper.EssayPaperAnswerService;
import com.huatu.tiku.essay.service.paper.EssayPaperLabelService;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.vo.admin.correct.EssayPaperLabelTotalVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author huangqingpeng
 * 试卷批改试卷
 */
@RestController
@Slf4j
@RequestMapping("/end/v2/paper/label")
public class EssayPaperCorrectController {

    @Autowired
    CorrectOrderService correctOrderService;
    @Autowired
    EssayPaperAnswerService essayPaperAnswerService;

    @Autowired
    EssayQuestionAnswerService essayQuestionAnswerService;

    @Autowired
    EssayPaperLabelService essayPaperLabelService;

    @Autowired
    EssayQuestionLabelService essayQuestionLabelService;

    /**
     * 试卷答题卡对应的主批改记录
     *
     * @param paperAnswerCardId
     */
    @GetMapping("{paperAnswerCardId}")
    public Object getMainLabelInfo(@PathVariable long paperAnswerCardId,
                                   @RequestParam String operate,
                                   @RequestParam(defaultValue = "2") int labelFlag) {
        LabelFlagEnum labelFlagEnum = LabelFlagEnum.create(labelFlag);
        CommonOperateEnum commonOperateEnum = CommonOperateEnum.create(operate);
        return essayPaperLabelService.getMainLabelInfo(paperAnswerCardId, commonOperateEnum, labelFlagEnum);
    }


    /**
     * 查询--套卷阅卷批注
     *
     * @param paperAnswerCardId
     * @return
     */
    @GetMapping("detail/{paperAnswerCardId}")
    public Object getPaperLabelMark(@PathVariable long paperAnswerCardId,
                                    @RequestParam(defaultValue = "2") int labelFlag) {
        LabelFlagEnum labelFlagEnum = LabelFlagEnum.create(labelFlag);
        return essayPaperLabelService.getPaperLabelMark(paperAnswerCardId, labelFlagEnum);
    }

    /**
     * 保存--套卷阅卷批注
     *
     * @return
     */
    @PostMapping("")
    public Object save(@RequestBody EssayPaperLabelTotalVo paperLabelTotalVo,
                       @RequestParam(defaultValue = "2") int labelFlag) {
        LabelFlagEnum labelFlagEnum = LabelFlagEnum.create(labelFlag);
        essayPaperLabelService.save(paperLabelTotalVo, labelFlagEnum);
        return SuccessMessage.create("保存成功!");
    }

    /**
     * 删除--套卷阅卷批注
     *
     * @return
     */
    @DeleteMapping("{labelId}")
    public Object delete(@PathVariable(name = "labelId") long labelId) {
        essayPaperLabelService.delete(labelId);
        return SuccessMessage.create("删除成功!");
    }

    /**
     * 套卷完成批改
     *
     * @param labelId
     * @return
     */
    @PostMapping("finish/{labelId}")
    public Object finishCorrect(@PathVariable long labelId) {
        essayPaperLabelService.labelFinish(labelId);
        return SuccessMessage.create("套卷批改完成");
    }

    @GetMapping("next/{paperAnswerCardId}")
    public Object getNextLabelInfo(@PathVariable long paperAnswerCardId) {
        CorrectOrder next = correctOrderService.getNext(paperAnswerCardId, EssayAnswerCardEnum.TypeEnum.PAPER);
        return next;
    }


}
