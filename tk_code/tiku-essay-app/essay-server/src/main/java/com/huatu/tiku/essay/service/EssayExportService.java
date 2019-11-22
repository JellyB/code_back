package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.vo.export.EssayExportReqVO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;

public interface EssayExportService {

    String createFile(EssayExportReqVO vo);

    String exportAnswer(EssayExportReqVO vo);

    void addContent(String content, Document document, Font font, int alignment) throws DocumentException;
}
