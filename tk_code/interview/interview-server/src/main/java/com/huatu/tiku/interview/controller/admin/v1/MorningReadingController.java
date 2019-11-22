package com.huatu.tiku.interview.controller.admin.v1;

import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.NotificationType;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.MorningReadingService;
import com.huatu.tiku.interview.util.LogPrint;
import com.huatu.tiku.interview.util.file.HtmlFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 *
 * @Author ZhenYang
 * @Date Created in 2018/1/13 18:58
 * @Description
 */
@RestController
@RequestMapping("/end/mr")
public class MorningReadingController {
    @Autowired
    private MorningReadingService readingService;
    @Autowired
    HtmlFileUtil htmlFileUtil;

    @LogPrint
    @PostMapping //@requestBody --> Json
    public Result add(@RequestBody NotificationType morningReading) throws IOException {
        morningReading.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
        morningReading.setBizStatus(WXStatusEnum.BizStatus.ONLINE.getBizSatus());
        morningReading.setType(2);
        morningReading.setCreator("admin");
        morningReading.setContent(htmlFileUtil.imgManage(morningReading.getContent(),morningReading.getId()+"",0));
        Long id = readingService.add(morningReading);
        return  Result.ok(id);
    }


}
