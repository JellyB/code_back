package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.util.file.PdfUtil;
import com.itextpdf.text.Document;

/**
 * Create by jbzm on 171213
 */
public interface EssayQuestionPdfService {

    String getSinglePdfPath(long questionId);

    String getCoverPdfPath(long paperId);

    String uploadFile(String filePath);

    String getSingleCorrectPdfPath(long questionAnswerId);

    String getMultiCorrectPdfPath(long paperAnswerId);

    void createPdf();

    void htmlProcess();

    void getPdfBatch();

    void insertManualCorrectScore(String answerRequire, EssayQuestionAnswer essayQuestionAnswer, PdfUtil pdf, Document document)throws Exception;

}
