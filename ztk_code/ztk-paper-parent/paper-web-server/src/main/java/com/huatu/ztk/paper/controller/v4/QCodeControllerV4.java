package com.huatu.ztk.paper.controller.v4;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.service.QCodeService;
import com.huatu.ztk.user.service.UserSessionService;
import com.self.generator.core.WaitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 处理用户扫面 试卷二维码之后的业务逻辑
 * 1.不存在答题卡-生成答题卡
 * 2.存在答题卡
 * 2.1 生成答题报告
 * 2.2 继续生成答题卡
 * Created by lijun on 2018/11/6
 */
@RestController
@RequestMapping(value = "v4/qCode", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QCodeControllerV4 {
    private static final Logger logger = LoggerFactory.getLogger(QCodeControllerV4.class);

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private QCodeService qCodeService;

    /**
     * 通过二维码ID 获取信息
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Object get(
            @RequestHeader String token,
            @RequestHeader int terminal,
            @RequestHeader(defaultValue = "1") String cv,
            @PathVariable("id") int paperId) throws BizException, WaitException {
        logger.info("》》》》试卷扫描，{}",paperId);
        userSessionService.assertSession(token);
        Long uid = userSessionService.getUid(token);
        int subject = userSessionService.getSubject(token);
        Object info = qCodeService.getInfoByPaperId(paperId, subject, uid, terminal,cv,token);
        if (null == info) {
            return SuccessMessage.create("请前往App答题");
        }
        logger.info("》》》》试卷扫描，返回信息-> {}",info);
        return info;
    }
}
