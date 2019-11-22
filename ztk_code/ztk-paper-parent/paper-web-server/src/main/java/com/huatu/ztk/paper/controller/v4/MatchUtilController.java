package com.huatu.ztk.paper.controller.v4;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.service.MatchServiceComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/11/27
 */
@RestController
@RequestMapping(value = "v4/_matchUtil", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MatchUtilController {

    @Autowired
    private MatchServiceComponent matchServiceComponent;

    /**
     * 白名单 or 全量
     */
    @RequestMapping(value = "{paperId}/createMatchCacheAnswerCard", method = RequestMethod.PUT)
    public Object createMatchCacheAnswerCard(@PathVariable int paperId, @RequestParam boolean isWhite) throws BizException {
        matchServiceComponent.createMatchCacheAnswerCard(paperId, isWhite);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 自定义 用户ID
     */
    @RequestMapping(value = "{paperId}/createMatchCacheAnswerCardSelf", method = RequestMethod.PUT)
    public Object createMatchCacheAnswerCardSelf(@PathVariable int paperId, @RequestParam String userIds) throws BizException {
        if (StringUtils.isNotBlank(userIds)) {
            List<Long> userIdList = Arrays.stream(userIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
            matchServiceComponent.createMatchCacheAnswerCard(paperId, userIdList);
        }
        return SuccessMessage.create("操作成功");
    }

    @RequestMapping(method = RequestMethod.GET)
    public Object testAsync() throws InterruptedException {
        matchServiceComponent.test();
        return SuccessMessage.create("操作成功");
    }
}
