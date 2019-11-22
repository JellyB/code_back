package com.huatu.tiku.essay.web.controller.api.V3;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.impl.correct.UserAnswerServiceImplV2;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by x6 on 2017/11/23.
 * 用户答题
 */
@RestController
@Slf4j
@RequestMapping("api/v3/answer")
public class ApiAnswerControllerV3 {

    @Autowired
    UserAnswerServiceImplV2 userAnswerServiceImplV2;
    @Autowired
    StringRedisTemplate stringRedisTemplate;



    /**
     * 查询批改记录列表
     *
     * @param type
     * @return
     */
    @LogPrint
    @GetMapping(value = "correctDetailList/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil correctList(@Token UserSession userSession,
                                @RequestHeader int terminal,
                                @RequestHeader String cv,
                                @PathVariable(value = "type", required = true) Integer type,
                                @RequestParam(name = "page", defaultValue = "1") int page,
                                @RequestParam(name = "pageSize", defaultValue = "20") int pageSize) {

        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        //orders.add(new Sort.Order(Sort.Direction.ASC, "bizStatus"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "submitTime"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "gmtModify"));
        
        if(type != EssayAnswerCardEnum.TypeEnum.PAPER.getType()){
            throw new BizException(ErrorResult.create(100010, "参数类型错误！"));
        }
        Pageable pageRequest = new PageRequest(page - 1, pageSize, new Sort(orders));
        List<EssayAnswerV2VO> l = userAnswerServiceImplV2.correctPaperList(userSession.getId(), pageRequest,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
        long c = userAnswerServiceImplV2.countCorrectPaperList(userSession.getId(),EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
        PageUtil p = PageUtil.builder().result(l).next(c > page * pageSize ? 1 : 0).build();
        return p;
    }
}
