package com.huatu.tiku.essay.web.controller.admin.v2.question;


import com.google.common.collect.Lists;
import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.CommonOperateEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.EssayLabelService;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelDetailVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelTotalVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelVO;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("end/v2/question")
public class EssayQuestionCorrectController {

    @Autowired
    EssayQuestionLabelService essayQuestionLabelService;
    @Autowired
    CorrectOrderService correctOrderService;
    @Autowired
    EssayLabelService essayLabelService;
    /**
     * 试卷答题卡对应的主批改记录
     *
     * @param questionAnswerCardId
     */
    @GetMapping("label/{questionAnswerCardId}")
    public QuestionLabelVO getMainLabelInfo(@PathVariable long questionAnswerCardId,
                                            @RequestParam String operate) {
        CommonOperateEnum commonOperateEnum = CommonOperateEnum.create(operate);
        return essayQuestionLabelService.getMainLabelInfo(questionAnswerCardId, commonOperateEnum);
    }

    /**
     * 4.0 查询-单题详细批注
     *
     * @param labelDetailId
     * @return
     */
    @LogPrint
    @GetMapping("label/detail/{labelDetailId}")
    public QuestionLabelDetailVO getQuestionLabelDetail(@PathVariable long labelDetailId) {
        return essayQuestionLabelService.findDetailById(labelDetailId);
    }

    /**
     * 4.1保存-单题详细批注
     *
     * @param labelDetailVO
     * @return
     */
    @LogPrint
    @PostMapping("label/detail")
    public Object saveQuestionLabelDetail(@RequestBody QuestionLabelDetailVO labelDetailVO) {
        return essayQuestionLabelService.saveLabelDetail(labelDetailVO);
    }

    /**
     * 4.2删除-单题详细批注
     *
     * @param labelDetailId
     * @return
     */
    @LogPrint
    @PostMapping("label/detail/{labelDetailId}/del")
    public Object delQuestionDetail(@PathVariable long labelDetailId, @RequestBody Map map) {
        return essayQuestionLabelService.delQuestionDetail(labelDetailId, MapUtils.getString(map, "labelContent"));
    }

    /**
     * 5.0查询-单题阅卷批注
     *
     * @return
     */
    @LogPrint
    @GetMapping("label/total/{totalId}")
    public QuestionLabelTotalVO getQuestionLabelTotal(@PathVariable long totalId) {
        return essayQuestionLabelService.findTotalInfoById(totalId);
    }

    /**
     * 5.1保存-单题阅卷批注
     *
     * @param questionLabelTotalVO
     * @return
     */
    @LogPrint
    @PostMapping("label/total")
    public Object saveQuestionLabel(@RequestBody QuestionLabelTotalVO questionLabelTotalVO) {
        return essayQuestionLabelService.saveLabelTotal(questionLabelTotalVO);
    }

    /**
     * 5.2删除-单题阅卷批注
     *
     * @return
     */
    @LogPrint
    @DeleteMapping("label/total/{totalId}")
    public Object delQuestionLabelTotal(@PathVariable long totalId) {
        essayQuestionLabelService.delQuestionTotal(totalId);
        return "删除成功";
    }

    /**
     * 5.3批注完成-单题阅卷批注
     *
     * @return
     */
    @LogPrint
    @PutMapping("label/total/finish/{totalId}")
    public QuestionLabelTotalVO LabelFinish(@PathVariable long totalId) {
        essayQuestionLabelService.labelFinish(totalId);
        return essayQuestionLabelService.findTotalInfoById(totalId);
    }


    @GetMapping("label/next/{questionAnswerCardId}")
    public Object getNextLabelInfo(@PathVariable long questionAnswerCardId) {
        CorrectOrder next = correctOrderService.getNext(questionAnswerCardId, EssayAnswerCardEnum.TypeEnum.QUESTION);
        return next;
    }


    /**
     * 根据批注id查询所有的论点批注
     */
    @LogPrint
    @GetMapping(value = "label/thesis/{totalId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayLabelDetail> getThesisList(@PathVariable long totalId) {

        return essayQuestionLabelService.getThesisList(totalId);
    }
}
