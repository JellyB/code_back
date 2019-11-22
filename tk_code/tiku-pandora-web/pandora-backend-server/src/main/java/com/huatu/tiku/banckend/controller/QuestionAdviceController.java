package com.huatu.tiku.banckend.controller;

import com.huatu.tiku.banckend.service.QuestionAdviceService;
import com.huatu.tiku.dto.request.BatchDealAdoption;
import com.huatu.tiku.entity.AdviceBean;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhengyi
 * @date 2018/9/13 1:23 PM
 **/
@RestController
@RequestMapping(value = "/backend/advice")
public class QuestionAdviceController {


    private final QuestionAdviceService questionAdviceService;
    private final KnowledgeService knowledgeService;

    @Autowired
    public QuestionAdviceController(QuestionAdviceService questionAdviceService, KnowledgeService knowledgeService) {
        this.questionAdviceService = questionAdviceService;
        this.knowledgeService = knowledgeService;
    }

    /**
     * bizStatus  3 未处理  2 已处理
     * checker    1 已采纳  2 忽略
     *
     * @param subject      考试科目
     * @param bizStatus    状态
     * @param questionArea 试题地区
     * @param questionType 试题题型
     * @param handler      处理人
     * @param errorType    错误类型
     * @param orderBy      order by : desc or asc
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(required = false, defaultValue = "-1") int subject,
                       @RequestParam(required = false, defaultValue = "3") int bizStatus,
                       @RequestParam(required = false, defaultValue = "-1") int areaId,
                       @RequestParam(required = false, defaultValue = "-1") String questionArea,
                       @RequestParam(required = false, defaultValue = "-1") int questionType,
                       @RequestParam(required = false) String handler,
                       @RequestParam(required = false) String committer,
                       @RequestParam(defaultValue = "3") Integer checker,
                       @RequestParam(required = false, defaultValue = "-1") Long knowledgeId,
                       @RequestParam(required = false, defaultValue = "-1") int errorType,
                       @RequestParam(required = false, defaultValue = "") String startTime,
                       @RequestParam(required = false, defaultValue = "") String endTime,
                       @RequestParam(required = false, defaultValue = "desc") String orderBy,
                       @RequestParam(required = false, defaultValue = "-1") Long questionId,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size) {

        String knowledgeIds = BaseInfo.isNotDefaultSearchValue(knowledgeId) ? knowledgeService.getKnowledgeInfo(subject, knowledgeId) : BaseInfo.SEARCH_INPUT_DEFAULT;
        AdviceBean advice = AdviceBean.builder()
                .subject(subject)
                .bizStatus(bizStatus)
                .areaId(areaId)
                .orderby(orderBy)
                .questionType(questionType)
                .handler(handler)
                .errorType(errorType)
                .questionArea(questionArea)
                .committer(committer)
                .startTime(startTime)
                .endTime(endTime)
                .knowledgeId(knowledgeIds)
                .questionId(questionId)
                .checker(checker)
                .build();
        return questionAdviceService.list(advice, page, size);
    }

    /**
     * checker    1 已采纳  2 未采纳
     * bizStatus  3 未处理  2 已处理
     */
    @RequestMapping(value = "/batch/dealAdoption", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object batchDealAdoption(@RequestBody BatchDealAdoption batchDealAdoption) {
        Object o = questionAdviceService.batchUpdateUserAdvice(batchDealAdoption);
        return SuccessMessage.create("处理成功" + o.toString() + "条数据");
    }


//    @RequestMapping(value = "/dealAdoption/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Object dealAdoption(@PathVariable("id") Long id, @RequestParam(defaultValue = "") String resultContent, Integer checker) {
//
//        questionAdviceService.updateUserAdvice(id, resultContent, checker);
//        return SuccessMessage.create("处理成功");
//    }

//    /**
//     * 处理纠错的试题状态为不采纳
//     * @param id
//     * @return
//     */
//    @RequestMapping(value = "/dealNoAdoption", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Object dealNoAdoption(@RequestParam int id,
//                                 @RequestParam String reason){
//        questionAdviceService.dealNoAdoption(id,reason);
//        return SuccessMessage.create("状态修改成功");
//    }
//
//    /**
//     * 处理纠错的试题状态为不使用
//     * @param id
//     * @return
//     */
//    @RequestMapping(value = "/dealNotUse", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Object dealNotUse(@RequestParam int id){
//        questionAdviceService.dealNoUse(id);
//        return SuccessMessage.create("状态修改成功");
//    }
//
//    /**
//     * 处理纠错的试题状态为使用
//     * @param id
//     * @return
//     */
//    @RequestMapping(value = "/dealUse", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Object dealUse(@RequestParam int id){
//        questionAdviceService.dealUse(id);
//        return SuccessMessage.create("状态修改成功");
//    }
//
//    /**
//     * 删除
//     * @param id
//     * @return
//     */
//    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Object delete(@PathVariable int id){
//        questionAdviceService.delete(id);
//        return SuccessMessage.create("删除成功");
//    }

}