package com.huatu.tiku.essay.web.controller.api.V2;

/**
 * Created by x6 on 2018/1/30.
 */

import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.service.v2.collect.UserCollectionServiceV2;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by x6
 * 题目收藏相关
 */
@RestController
@Slf4j
@RequestMapping("api/v2/collect")
public class ApiCollectionControllerV2 {

    @Autowired
    UserCollectionServiceV2 userCollectionServiceV2;


    /**
     * 查询收藏题目
     * 单题0  套题1 议论文2
     * @return
     */
    @LogPrint
    @GetMapping(value = "list/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@Token UserSession userSession,
                   @RequestHeader int terminal,
                   @RequestHeader String cv,
                   @PathVariable int type,
                   @RequestParam (name = "page", defaultValue = "1")int page,
                   @RequestParam (name = "pageSize", defaultValue = "20") int pageSize) {

        return userCollectionServiceV2.list(userSession, type, page, pageSize, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }
}
