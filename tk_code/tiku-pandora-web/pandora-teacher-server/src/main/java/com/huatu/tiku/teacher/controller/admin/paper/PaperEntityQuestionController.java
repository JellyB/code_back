package com.huatu.tiku.teacher.controller.admin.paper;

import com.google.common.collect.Lists;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理实体试卷和试题之间的关系
 * Created by lijun on 2018/8/13
 */
@RestController
@RequestMapping("paperEntity/question")
public class PaperEntityQuestionController {

    @Autowired
    private PaperEntityService paperEntityService;

    @Autowired
    private PaperQuestionService paperQuestionService;

    /**
     * 验证编号
     */
    @GetMapping(value = "/{paperId}/validateSort")
    public Object validateSort(
            @PathVariable("paperId") long paperId,
            @RequestParam("sort") int sort
    ) {
        return paperQuestionService.validateSort(paperId, PaperInfoEnum.TypeInfo.ENTITY, sort);
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
        paperQuestionService.savePaperQuestionWithSort(questionId, paperId, moduleId, sort, score, paperEntityService.createPaperQuestionValidate(), PaperInfoEnum.TypeInfo.ENTITY);
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
            @RequestBody List<PaperQuestion> list
            /*,
            @RequestParam(defaultValue = "false") Boolean isContinue*/
    ) {
        boolean isContinue = false;
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        List<PaperQuestion> collect = list.stream()
                .map(paperQuestion -> {
                    paperQuestion.setPaperType(PaperInfoEnum.TypeInfo.ENTITY.getCode());
                    return paperQuestion;
                })
                .collect(Collectors.toList());
        List<PaperQuestion> failList = paperQuestionService.savePaperQuestion(collect, paperEntityService.createPaperQuestionValidate(), isContinue);
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
        paperQuestionService.deletePaperQuestion(paperId, PaperInfoEnum.TypeInfo.ENTITY, questionList);
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
        paperQuestionService.updateQuestionScoreByModuleId(paperId, PaperInfoEnum.TypeInfo.ENTITY, moduleId, score);
        return SuccessMessage.create("操作成功");
    }


}
