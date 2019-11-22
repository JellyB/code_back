package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.vo.resp.EssayHtmlPaperVO;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;
import com.huatu.tiku.essay.vo.resp.FileResultVO;

import java.util.List;

/**
 * Created by x6 on 2017/12/16.
 */
public interface EssayFileService {


     FileResultVO saveFile( long questionBaseId, long paperId, long questionAnswerId, long paperAnswerId);

    EssayHtmlPaperVO createHtml(Long paperId);

    Object getPaperList();

    EssayUpdateVO getPageTitle();

    EssayUpdateVO setPageTitle( String value);

    Object addPaper(Long paperId);
}
