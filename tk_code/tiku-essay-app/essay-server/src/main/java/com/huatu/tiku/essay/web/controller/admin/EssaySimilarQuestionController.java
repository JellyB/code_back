package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.tiku.essay.vo.admin.AdminSingleQuestionGroupVO;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Created by x6 on 2017/12/19.
 */

@RestController
@Slf4j
@RequestMapping("/end/question")
public class EssaySimilarQuestionController {

    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;

    /**
     * 新增单题组
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "question", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminSingleQuestionGroupVO addSingleQuestion(@RequestBody AdminSingleQuestionGroupVO singleQuestionGroupVO) {
        return essaySimilarQuestionService.saveSingleQuestion(singleQuestionGroupVO);
    }

    /**
     * 题组状态修改
     *
     * @param
     * @return
     */
    @LogPrint
    @PutMapping(value = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public AdminSingleQuestionGroupVO updateStatus(@RequestBody AdminSingleQuestionGroupVO singleQuestionGroupVO) {
        return essaySimilarQuestionService.updateSingleQuestion(singleQuestionGroupVO);
    }

    /**
     * 查询单题组
     *
     * @return
     */
    @LogPrint
    @GetMapping(value = "questionList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil<AdminSingleQuestionGroupVO> findSingleQuestion(@RequestParam(name = "page", defaultValue = "1") int page,
                                                                   @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                                                   @RequestParam(name = "title", defaultValue = "") String title,
                                                                   @RequestParam(name = "type", defaultValue = "0") int type,
                                                                   @RequestParam(name = "bizStatus", defaultValue = "-1") int bizStatus,
                                                                   @RequestParam(name = "questionId", defaultValue = "-1") long questionId,
                                                                   @RequestParam(name = "groupId", defaultValue = "-1") long groupId) {

        Pageable pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtCreate");


        return essaySimilarQuestionService.findSingleQuestionList(pageRequest, title, type,bizStatus,questionId,groupId);
    }


}
