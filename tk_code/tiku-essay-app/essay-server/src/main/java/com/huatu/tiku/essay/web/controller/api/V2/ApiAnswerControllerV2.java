package com.huatu.tiku.essay.web.controller.api.V2;

import com.huatu.common.consts.TerminalType;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.dto.ImageSortDto;
import com.huatu.tiku.essay.essayEnum.CourseExerciseTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by x6 on 2018/5/5.
 */
@RestController
@Slf4j
@RequestMapping("api/v2/answer")
public class ApiAnswerControllerV2 {

    @Autowired
    UserAnswerService userAnswerService;
    @Autowired
    private UploadFileUtil uploadFileUtil;

    /**
     * 查询批改详情
     *
     * @param type
     * @return
     */
    @LogPrint
    @GetMapping(value = "correctDetail/{type}/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionVO> correctDetail(
            @Token UserSession userSession,
            @RequestHeader int terminal,
            @RequestHeader String cv,
            @PathVariable(value = "type", required = true) int type,
            @PathVariable(value = "answerId", required = true) int answerId) {

        List<EssayQuestionVO> essayQuestionVOS = userAnswerService.answerDetailV2(userSession.getId(), type, answerId, terminal, cv);
        //判断客户端版本,处理批改详情中的新标签（安卓6.3之后的版本支持新标签，ios7.0）
        for (EssayQuestionVO vo : essayQuestionVOS) {
            vo.setCorrectRule(null);
            // 如果没有批改规则 除应用文和议论文之外都是关键句
            if (vo.getCorrectType() == 0) {
                if (vo.getType() == 4 || vo.getType() == 5) {
                    vo.setCorrectType(2);
                } else {
                    vo.setCorrectType(1);
                }
            }
            if (StringUtils.isNotEmpty(vo.getCorrectedContent())) {
                if (((terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) && cv.compareTo("7.0") < 0)
                        || ((terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) && cv.compareTo("6.3") < 0)) {
                    vo.setCorrectedContent(vo.getCorrectedContent().replace("{", "").replace("}", ""));
                }
            }
        }
        return essayQuestionVOS;
    }


    /**
     * 删除批改记录（加入回收站）
     *
     * @param type
     * @return
     */
    @LogPrint
    @DeleteMapping(value = "{type}/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map delAnswer(
            @Token UserSession userSession,
            @RequestHeader int terminal,
            @RequestHeader String cv,
            @PathVariable(value = "type", required = true) int type,
            @PathVariable(value = "answerId", required = true) int answerId) {

        return userAnswerService.delAnswer(type, answerId);
    }

    /**
     * 查询已删除批改记录（加入回收站）
     *
     * @param type
     * @return
     */
    @LogPrint
    @GetMapping(value = "recycle/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil recycleList(@Token UserSession userSession,
                                @RequestHeader int terminal,
                                @RequestHeader String cv,
                                @PathVariable(value = "type", required = true) Integer type,
                                @RequestParam(name = "page", defaultValue = "1") int page,
                                @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "bizStatus"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "correctDate"));

        Pageable pageRequest = new PageRequest(page - 1, pageSize, new Sort(orders));
        List<EssayAnswerVO> l = userAnswerService.recycleList(userSession.getId(), type, pageRequest);
        long c = userAnswerService.countRecycleList(userSession.getId(), type);

        PageUtil p = PageUtil.builder().result(l).next(c > page * pageSize ? 1 : 0).build();
        return p;
    }


    /**
     * 查询单题批改列表(小题+议论文)
     * 7.0 的批改列表：套题还是以前的接口。单题调用此接口（type 0标准答案 1套题 2议论文）
     *
     * @param type（type 1标准答案 2议论文 1是套题这里暂时不用）
     * @return
     */
    @LogPrint
    @GetMapping(value = "question/correctDetailList/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil questionCorrectList(@Token UserSession userSession,
                                        @PathVariable(value = "type", required = true) Integer type,
                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "bizStatus"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "correctDate"));

        Pageable pageRequest = new PageRequest(page - 1, pageSize, new Sort(orders));
        List<EssayAnswerVO> l = userAnswerService.questionCorrectList(userSession.getId(), type,EssayAnswerCardEnum.ModeTypeEnum.NORMAL, pageRequest);
        long c = userAnswerService.countQuestionCorrectList(userSession.getId(), type);

        PageUtil p = PageUtil.builder()
                .result(l).next(c > page * pageSize ? 1 : 0)
                .total(c)
                .build();
        return p;
    }


    /**
     * 拍照识别
     *
     * @param userSession
     * @param answerId
     * @param sort
     * @param file
     * @return
     */
    @PostMapping(value = "/photo/{answerId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object photoDistinguish(@Token UserSession userSession,
                                   @PathVariable(value = "answerId") Integer answerId,
                                   @RequestParam(value = "sort", required = false) int sort,
                                   @RequestParam(value = "file", required = false) MultipartFile file) {

        return userAnswerService.photoDistinguish(file, userSession.getId(), answerId, sort);
    }

    /**
     * 修改答题图片排序
     *
     * @param userSession
     * @param dtoList
     * @return
     */
    @LogPrint
    @PutMapping(value = "photo/sort", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updatePhotoRoll(@Token UserSession userSession,
                                  @RequestBody List<ImageSortDto> dtoList) {
        return userAnswerService.updatePhotoSort(userSession.getId(), dtoList);
    }


    /**
     * 修改答题图片排序
     *
     * @param userSession
     * @param answerId
     * @param imageId
     * @return
     */
    @LogPrint
    @DeleteMapping(value = "photo/{imageId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deleteImage(@Token UserSession userSession,
                              @PathVariable(value = "imageId") long imageId,
                              @RequestParam(value = "answerId") long answerId) {
        return userAnswerService.deleteImageByLogic(answerId, imageId);
    }

    /**
     * 创建答题卡
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param createAnswerCardVO
     * @return
     */
    @LogPrint
    @PostMapping(value = "answerCard", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseVO createAnswerCard(@Token UserSession userSession,
                                       @RequestHeader int terminal,
                                       @RequestHeader String cv,
                                       @RequestBody CreateAnswerCardVO createAnswerCardVO) {
        return userAnswerService.createAnswerCardV2(userSession.getId(), createAnswerCardVO, terminal,userAnswerService.getUnFinishedCount(createAnswerCardVO), EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType());

    }

    /**
     * 校验图片答案
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param
     * @return
     */
    @LogPrint
    @GetMapping(value = "content/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getImageContent(@Token UserSession userSession, @RequestHeader int terminal,
                                  @RequestHeader String cv, @PathVariable Long answerId) {
        return userAnswerService.getImageContent(userSession.getId(), answerId);

    }

    /**
     * 提交或保存答题卡
     *
     * @param userSession
     * @param terminal
     * @param cv
     * @param paperCommitVO
     * @return
     */
    @LogPrint
    @PostMapping(value = "paperAnswerCard", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object commit(@Token UserSession userSession, @RequestHeader int terminal, @RequestHeader String cv,
                         @RequestBody PaperCommitVO paperCommitVO) {
        if (paperCommitVO == null) {
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
        paperCommitVO.setExercisesType(CourseExerciseTypeEnum.normal.getCode());
        return userAnswerService.paperCommitV2(userSession, paperCommitVO, terminal, cv);

    }

}
