package com.huatu.tiku.essay.web.controller.tool;

import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by x6 on 2018/1/4.
 */
@RestController
@RequestMapping("api/pdf")
@Slf4j
public class PdfUtilController {


    @Autowired
    private EssayQuestionPdfService essayQuestionPdfService;
    /**
     * 删除单题列表五个类型的缓存数据接口
     *
     * @return
     */
    @GetMapping("")
    public Object createPdf() {
        essayQuestionPdfService.createPdf();
        return "pdf统一处理结束";
    }


}
