package com.huatu.tiku.essay.web.controller.tool;

import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by x6 on 2018/1/9.
 */
@RestController
@RequestMapping("api/html")
@Slf4j
public class HtmlController {

    @Autowired
    private EssayQuestionPdfService essayQuestionPdfService;
    /**
     * 处理html中的标签问题
     *
     * @return
     */
    @GetMapping("")
    public Object htmlProcess() {

        essayQuestionPdfService.htmlProcess();
        return "pdf统一处理结束";
    }

}
