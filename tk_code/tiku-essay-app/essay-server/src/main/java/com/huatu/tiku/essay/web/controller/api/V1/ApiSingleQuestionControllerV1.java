package com.huatu.tiku.essay.web.controller.api.V1;


import com.huatu.common.consts.TerminalType;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionTypeVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 单题
 */
@RestController
@RequestMapping("api/v1/single")
@Slf4j
public class ApiSingleQuestionControllerV1 {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 单题列表接口（分页）
     *
     * @param type
     * @param page
     * @param pageSize
     * @return
     * @RequestHeader String cv,
     * @RequestHeader int terminal,
     */
    @LogPrint
    @GetMapping(value = "questionList/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil singleQuestionList(@Token UserSession userSession,
                                       @RequestHeader int terminal,
                                       @RequestHeader String cv,
                                       @PathVariable(name = "type") int type,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
                                       @RequestParam(defaultValue = "1") int modeType) {

        Pageable pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "showMsg", "gmtCreate");
        //如果是 IOS 并且版本号小于6.1.1  pageSize设置成 55(解决IOS旧版本翻页问题)
        if ((terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)
                && (cv.compareTo("6.1.1") < 0)) {
            pageRequest = new PageRequest(page - 1, 55, Sort.Direction.DESC, "showMsg", "gmtCreate");
        }
        log.info("type: {},pageRequest: {}： userId ：{}", type, pageRequest, userSession.getId());

        /*  v1版本（题目类型只有一级类目）*/
/*        List<EssayQuestionVO> l = essaySimilarQuestionService.findSingleQuestionList(pageRequest, type,userSession.getId());
        long c = essaySimilarQuestionService.countSingleQuestionByType(type);*/

        /*  v1版本（题目类型存在下级类目）*/
        List<EssayQuestionVO> l = essaySimilarQuestionService.findSingleQuestionListV2(pageRequest, type, userSession.getId(), EssayAnswerCardEnum.ModeTypeEnum.create(modeType));
        long c = essaySimilarQuestionService.countSingleQuestionByTypeV2(type);


        PageUtil p = PageUtil.builder().result(l).next(c > page * pageSize ? 1 : 0).build();

        return p;
    }

    /**
     * 根据题目查询材料列表
     *
     * @param questionBaseId
     * @param page
     * @param pageSize
     * @return
     * @modify zw
     */
    @LogPrint
    @GetMapping(value = "materialList/{questionBaseId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayMaterialVO> stemList(@PathVariable(name = "questionBaseId") long questionBaseId,
                                          @RequestParam(name = "page", defaultValue = "1") int page,
                                          @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {
        log.info("questionBaseId: {}", questionBaseId);


        return essaySimilarQuestionService.findMaterialList(questionBaseId);
    }


    /**
     * 根据相似题目id查询地区列表
     *
     * @param similarId
     * @return
     */
    @LogPrint
    @GetMapping(value = "areaList/{similarId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionAreaVO> paperList(@Token UserSession userSession,
                                               @PathVariable(name = "similarId") long similarId) {

        log.info("questionDetailId: {}", similarId);
        return essaySimilarQuestionService.findAreaList(similarId, userSession.getId(), EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }

    /**
     * 根据试题查询试题详情
     *
     * @param questionBaseId
     * @param modeType      //1表示普通的用户答题数据查询2表示课后作业用户答题数据查询
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
        EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum = EssayAnswerCardEnum.ModeTypeEnum.create(modeType);
        EssayQuestionVO detailV1 = essaySimilarQuestionService.findQuestionDetailV1(questionBaseId, userSession.getId(), modeTypeEnum);
        if ((terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)
                && (cv.compareTo("6.1.3") > 0)) {
            detailV1.setCommitWordNumMax(detailV1.getInputWordNumMax());//最多答题字数（提交的字数）
            detailV1.setInputWordNumMax(detailV1.getInputWordNumMax());//最多录入字数（录入的字数）

        }

        return detailV1;
    }


    /**
     * 查询试题类型
     * 缓存中获取  永久缓存
     *
     * @return
     * @modify zw
     */
    @GetMapping(value = "questionTypeList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayQuestionTypeVO> questionType() {
//        return essaySimilarQuestionService.findQuestionType();
        return essaySimilarQuestionService.findQuestionTypeV2();
    }


}
