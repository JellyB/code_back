package com.huatu.tiku.essay.web.controller.api.v4;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.huatu.tiku.essay.constant.status.EssayCorrectGoodsConstant;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.impl.correct.UserCorrectTimesUtil;
import com.huatu.tiku.essay.vo.resp.correct.CorrectTimesSimpleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.TeacherOrderTypeEnum;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.UserAnswerService;
import com.huatu.tiku.essay.service.correct.UserAnswerServiceV2;
import com.huatu.tiku.essay.service.correct.UserCorrectGoodsServiceV4;
import com.huatu.tiku.essay.service.v2.question.QuestionTypeService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayAnswerV2VO;
import com.huatu.tiku.springboot.users.support.Token;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by x6 on 2018/5/5.
 */
@RestController
@Slf4j
@RequestMapping("api/v4/answer")
public class ApiAnswerControllerV4 {

    @Autowired
    UserAnswerServiceV2 userAnswerServiceV2;

    @Autowired
    UserAnswerService userAnswerService;
    @Autowired
    private UserCorrectGoodsServiceV4 userCorrectGoodsServiceV4;
    
    @Autowired
    private EssayTeacherService essayTeacherService;
    
    @Autowired
    private QuestionTypeService questionTypeService;
    
    private String correctDesc="目前老师工作都饱和啦~提交的订单批改会顺延%s日，预计完成批改时间为%s";

    /**
     * 查询单题批改列表(小题+议论文)
     * 7.0 的批改列表：套题还是以前的接口。单题调用此接口（type 0标准答案 1套题 2议论文）
     *
     * @param type（type 1标准答案 2议论文）
     * @return
     */
    @LogPrint
    @GetMapping(value = "question/correctDetailList/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil questionCorrectList(@Token UserSession userSession,
                                        @PathVariable(value = "type", required = true) Integer type,
                                        @RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        //orders.add(new Sort.Order(Sort.Direction.ASC, "bizStatus"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "submitTime"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "gmtModify"));

        Pageable pageRequest = new PageRequest(page - 1, pageSize, new Sort(orders));
        List<EssayAnswerV2VO> l = userAnswerServiceV2.questionCorrectList(userSession.getId(), type, EssayAnswerCardEnum.ModeTypeEnum.NORMAL, pageRequest);
        long c = userAnswerService.countQuestionCorrectList(userSession.getId(), type);

        PageUtil p = PageUtil.builder()
                .result(l).next(c > page * pageSize ? 1 : 0)
                .total(c)
                .build();
        return p;
    }

    /**
     * 检测老师工作量是否饱和
     *
     * @param userSession
     * @param questionType 0为套卷否则为试题类型
     * @param answerCardId
     * @return
     */
    @LogPrint
    @GetMapping(value = "check/{answerCardId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object checkCanCorrect(@Token UserSession userSession,
                                  @RequestParam(value = "questionType") Integer questionType,
                                  @RequestParam(value = "correctMode") Integer correctMode,
                                  @RequestParam(value = "id") Integer id,
                                  @RequestParam(value = "isCourseWork", defaultValue = "0") Integer courseWork,
                                  @PathVariable(value = "answerCardId") Long answerCardId) {

        Map<String, Object> map = Maps.newHashMap();
        int userId = userSession.getId();
        CorrectModeEnum correctModeEnum = CorrectModeEnum.create(correctMode);
        EssayCorrectGoodsConstant.GoodsTypeEnum goodsType = EssayCorrectGoodsConstant.GoodsTypeEnum.getGoodsType(questionType, correctModeEnum.getMode());


        if(courseWork.intValue() > 0){
            CorrectTimesSimpleVO correctTimesSimpleVO = UserCorrectTimesUtil.initCorrectTimesVO(goodsType);
            correctTimesSimpleVO.setNum(1);
            map.put("correct", correctTimesSimpleVO);
            map.put("canCorrect", true);
            map.put("correctDesc", "");
        }else{
            map.put("correct",userCorrectGoodsServiceV4.findCorrectTimes(userId, goodsType, id));
            map.put("canCorrect", true);
            map.put("correctDesc", "");
        }


    	int orderType = questionTypeService.convertQuestionTypeToQuestionLabelType(questionType);
		TeacherOrderTypeEnum orderTypeEnum = TeacherOrderTypeEnum.create(orderType);
		Integer delayTime = orderTypeEnum.getDelayTime();
		
    	boolean flag = essayTeacherService.checkCanCorrect(orderType);
        map.put("canCorrect", flag);
    	if(!flag) {
    		//饱和
    		LocalDate plusDays = LocalDate.now().plusDays(delayTime);
            map.put("correctDesc", String.format(correctDesc, delayTime,plusDays));
    	}
        return map;
    }
    
}
