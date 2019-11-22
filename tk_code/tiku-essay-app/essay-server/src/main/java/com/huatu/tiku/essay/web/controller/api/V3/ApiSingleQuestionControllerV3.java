package com.huatu.tiku.essay.web.controller.api.V3;


import com.huatu.common.consts.TerminalType;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.EssaySimilarQuestionGroupInfo;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.question.SingleQuestionSearch;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionTypeVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 单题管理V3（7.0）
 */
@RestController
@RequestMapping("api/v3/single")
@Slf4j
public class ApiSingleQuestionControllerV3 {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;

    @Autowired
    private SingleQuestionSearch singleQuestionSearch;

    /**
     * 单题列表接口（分页）
     * V3修改：1.支持游客模式
     *         2.将地区列表相关信息在该接口返回
     * @param type
     * @param page
     * @param pageSize
     * @RequestHeader String cv,
     * @RequestHeader int terminal,
     * @return
     */
    @LogPrint
    @GetMapping(value = "questionList/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil singleQuestionList(@Token(check = false) UserSession userSession,
                                       @RequestHeader int terminal,
                                       @RequestHeader String cv,
                                       @PathVariable(name = "type") int type,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                       @RequestParam(defaultValue = "1") int modeType) {

        Pageable pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "showMsg", "gmtCreate");
        int userId = (userSession == null) ? -1 : userSession.getId();

        PageUtil<List<EssaySimilarQuestionGroupInfo>> pageUtil = singleQuestionSearch.findSimilarQuestionPageInfo(pageRequest, type);
        if (null == pageUtil || CollectionUtils.isEmpty(pageUtil.getResult())) {
            return pageUtil;
        }
        List<EssayQuestionVO> singleQuestionList = singleQuestionSearch.findSimilarQuestionList(pageUtil.getResult(), userId,EssayAnswerCardEnum.ModeTypeEnum.create(modeType));
        PageUtil<Object> result = PageUtil.builder()
                .total(pageUtil.getTotal())
                .totalPage(pageUtil.getTotalPage())
                .next(pageUtil.getNext())
                .result(singleQuestionList)
                .build();
        return result;
    }

    /**
     * 根据题目查询材料列表
     * @modify zw
     * @param questionBaseId
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value = "materialList/{questionBaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayMaterialVO> stemList(@PathVariable(name = "questionBaseId") long questionBaseId,
                                          @RequestParam(name = "page", defaultValue = "1") int page,
                                          @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        log.info("questionBaseId: {}", questionBaseId);


        return  essaySimilarQuestionService.findMaterialList(questionBaseId);
    }


    /**
     * 根据相似题目id查询地区列表
     * @param similarId
     * @return
     */
    @LogPrint
    @GetMapping(value = "areaList/{similarId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionAreaVO> paperList(@Token UserSession userSession,
                                               @PathVariable(name = "similarId") long similarId) {

        log.info("questionDetailId: {}", similarId);

        return singleQuestionSearch.findSimilarQuestionAreaVOInfoList(similarId, userSession.getId());
    }

    /**
     * 根据试题查询试题详情
     * @param questionBaseId
     * @param modeType   1普通答题卡2课后作业答题卡
     * @return
     */
    @LogPrint
    @GetMapping(value = "questionDetail/{questionBaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayQuestionVO questionDetail(@Token UserSession userSession,
                                          @RequestHeader int terminal,
                                          @RequestHeader String cv,
                                          @PathVariable(name = "questionBaseId") long questionBaseId,
                                          @RequestParam(defaultValue = "1") int modeType) throws BizException {
        log.info("questionBaseId: {}", questionBaseId);
        //单个答案
//        return essaySimilarQuestionService.findQuestionDetail(questionBaseId,userSession.getId());
        //多个答案 兼容单个答案
        EssayQuestionVO detailV1 = essaySimilarQuestionService.findQuestionDetailV1(questionBaseId, userSession.getId(), EssayAnswerCardEnum.ModeTypeEnum.create(modeType));
        if((terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)
                && (cv.compareTo("6.1.3") > 0)){
            detailV1.setCommitWordNumMax(detailV1.getInputWordNumMax());//最多答题字数（提交的字数）
            detailV1.setInputWordNumMax(detailV1.getInputWordNumMax() );//最多录入字数（录入的字数）

        }

        return detailV1;
    }


    /**
     * 查询试题类型
     * 缓存中获取  永久缓存
     * @modify zw
     * @return
     */
    @GetMapping(value = "questionTypeList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionTypeVO> questionType() {
        //1。取出除所有可用题型
        List<EssayQuestionTypeVO> questionTypeList = essaySimilarQuestionService.findQuestionTypeV2();

        //2。过滤二级类目和议论文
        List<EssayQuestionTypeVO> result = new LinkedList<>();
        for(EssayQuestionTypeVO questionType:questionTypeList){
            questionType.setSubList(null);
            if(questionType.getId() != 5 && questionType.getPid() == 0){
                result.add(questionType);
            }
        }
        return result;
    }



}
