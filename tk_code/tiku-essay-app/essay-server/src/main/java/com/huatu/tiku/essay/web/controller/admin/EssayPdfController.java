package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import com.huatu.tiku.essay.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by jbzm on 171213
 */
@RestController
@Slf4j
@RequestMapping("/end/essayPdf")
public class EssayPdfController {

    @Autowired
    private EssayQuestionPdfService essayQuestionPdfService;

    /**
     * 生成单题pdf
     * @param questionId
     * @return
     */
    @LogPrint
    @GetMapping("/singleQuestion")
    public String createSingleQuestionPdf(long questionId) {

        return essayQuestionPdfService.getSinglePdfPath(questionId);
    }
    /**
     * 生成套题pdf
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping("/multiQuestion")
    public String createMultiQuestionPdf(long paperId){
        return essayQuestionPdfService.getCoverPdfPath(paperId);
    }

    /**
     * 生成批改pdf
     * @param questionAnswerId
     * @return
     */
    @LogPrint
    @GetMapping("/correctSingleQuestion")
    public String createSingleCorrectPdf(long questionAnswerId){
        return essayQuestionPdfService.getSingleCorrectPdfPath(questionAnswerId);
    }

    /**
     * 生成批改pdf
     * @param paperAnswerId
     * @return1
     */
    @LogPrint
    @GetMapping("/correctMultiQuestion")
    public String createMultiCorrectPdf(long paperAnswerId){
        return essayQuestionPdfService.getMultiCorrectPdfPath(paperAnswerId);
    }

    /**
     * 上传文件
     * @return
     */
    @LogPrint
    @GetMapping(value = "file", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String uploadFile( @RequestParam  String filePath ) {
        return essayQuestionPdfService.uploadFile(filePath);
    }


    /**
     *  批量更新pdf
     */
    @LogPrint
    @GetMapping(value = "batch", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object uploadFileBatch() {
        //查询所有已上线的试卷
        essayQuestionPdfService.getPdfBatch();

        return null;
    }


}
