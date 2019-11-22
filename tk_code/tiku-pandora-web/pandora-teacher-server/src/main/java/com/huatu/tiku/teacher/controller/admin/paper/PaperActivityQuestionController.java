package com.huatu.tiku.teacher.controller.admin.paper;

import com.google.common.collect.Lists;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.paper.ChangePaperQuestionService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/8/16
 * @描述 处理活动卷跟试题相关逻辑
 */
@RestController
@RequestMapping("paperActivity/question")
public class PaperActivityQuestionController {

    @Autowired
    PaperQuestionService paperQuestionService;

    @Autowired
    PaperActivityService paperActivityService;

    @Autowired
    ChangePaperQuestionService changePaperQuestionService;

    /**
     * 验证编号
     */
    @GetMapping(value = "/{paperId}/validateSort")
    public Object validateSort(
            @PathVariable("paperId") long paperId,
            @RequestParam("sort") int sort
    ) {
        return paperQuestionService.validateSort(paperId, PaperInfoEnum.TypeInfo.SIMULATION, sort);
    }

    /**
     * 保存 试卷-试题关联关系
     */
    @PostMapping("{paperId}/{questionId}")
    public Object saveRelation(
            @PathVariable("paperId") long paperId,
            @PathVariable("questionId") long questionId,
            @RequestParam("moduleId") int moduleId,
            @RequestParam("score") double score,
            @RequestParam("sort") int sort
    ) {
        paperQuestionService.checkScore(score);
        paperQuestionService.savePaperQuestionWithSort(questionId, paperId, moduleId, sort, score, paperActivityService.createPaperQuestionValidate(), PaperInfoEnum.TypeInfo.SIMULATION);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 批量保存 试卷-试题信息
     * isContinue 阶段测试,试卷绑定试题,会判断试题是否已经绑定了其他试卷;
     * 如果已经绑定其他试卷isContinue=true;此方法会跳过绑定校验，此试卷会继续绑定此题
     * isContinue true,继续绑定
     */
    @PostMapping
    public Object saveAllRelation(
            @RequestBody List<PaperQuestion> list,
            @RequestParam(defaultValue = "false") Boolean isContinue
    ) {
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        List<PaperQuestion> collect = list.stream()
                .map(paperQuestion -> {
                    paperQuestion.setPaperType(PaperInfoEnum.TypeInfo.SIMULATION.getCode());
                    return paperQuestion;
                })
                .collect(Collectors.toList());
        List<PaperQuestion> failList = paperQuestionService.savePaperQuestion(collect, paperActivityService.createPaperQuestionValidate(), isContinue);
        return failList;
    }


    /**
     * 批量移除试卷-试题关系
     */
    @DeleteMapping("{paperId}")
    public Object removeRelation(
            @PathVariable("paperId") long paperId,
            @RequestParam("questionIds") String questionIds) {
        if (StringUtils.isBlank(questionIds)) {
            return SuccessMessage.create("操作成功");
        }
        List<Long> questionList = Arrays.stream(questionIds.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        paperQuestionService.deletePaperQuestion(paperId, PaperInfoEnum.TypeInfo.SIMULATION, questionList);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 根据模块ID批量更新分数
     */
    @PutMapping("{paperId}/updateScoreByModuleId")
    public Object updateQuestionScoreByModuleId(
            @PathVariable("paperId") long paperId,
            @RequestParam("moduleId") int moduleId,
            @RequestParam("score") double score
    ) {
        paperQuestionService.checkScore(score);
        paperQuestionService.updateQuestionScoreByModuleId(paperId, PaperInfoEnum.TypeInfo.SIMULATION, moduleId, score);
        return SuccessMessage.create("操作成功");
    }


    /**
     * 修改活动名称
     */
    @PostMapping("activityInfo")
    public Object updateActivity(@RequestBody PaperActivity paperActivity) {
        return paperActivityService.saveActivityInfo(paperActivity);
    }

    /**
     * @param oldQuestionId 需要被替换的试旧题ID
     * @param newQuestionId 替换的新试题ID
     * @return
     * @Description 此方法主要用户去重题时,（1）将旧试题ID从试卷中解绑（2）然后用新的试题ID替换旧试题ID,绑定此试卷
     * （3）删除旧试题 （4）ztk_reflect表中建立新旧试题的绑定关系
     */
    @PostMapping("changePaperQueBindRelation")
    public Object changePaperQueBindRelation(@RequestParam Long oldQuestionId,
                                             @RequestParam Long newQuestionId) {
        //传入
        changePaperQuestionService.changePaperQueBindRelation(oldQuestionId, newQuestionId);
        return SuccessMessage.create("替换成功");
    }


}
