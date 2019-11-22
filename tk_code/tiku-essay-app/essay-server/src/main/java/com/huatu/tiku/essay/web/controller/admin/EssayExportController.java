package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.common.CommonErrors;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayExportErrors;
import com.huatu.tiku.essay.service.EssayExportService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.vo.export.EssayExportReqVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author zhaoxi
 * @Description: 后台导出试题相关
 * @date 2018/10/8上午10:43
 */
@RestController
@Slf4j
@RequestMapping("/end/export")
public class EssayExportController {

    @Autowired
    EssayExportService essayExportService;

    /**
     * 导出pdf或者word
     *
     * @return
     */
    @LogPrint
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object essayPdf(@RequestBody EssayExportReqVO vo) {

        if (vo == null) {
            log.warn("参数错误，请求参数不能为空");
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        if (CollectionUtils.isEmpty(vo.getPaperIdList())) {
            log.warn("参数错误，所选试卷列表为空。");
            throw new BizException(EssayExportErrors.EMPTY_LIST);
        }

        //导出word
        if (vo.getType() != EssayExportReqVO.BASE_CONTENT && vo.getType() != EssayExportReqVO.BASE_CONTENT_WITH_ANSWER && vo.getType() != EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS
                && vo.getType() != EssayExportReqVO.BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS_ARITHMETIC) {
            log.warn("参数错误，导出范围错误。fileType:{}", vo.getFileType());
            throw new BizException(EssayExportErrors.ERROR_FILE_TYPE);
        }

        //导出word
        if (vo.getFileType() != EssayExportReqVO.FILE_TYPE_WORD && vo.getFileType() != EssayExportReqVO.FILE_TYPE_PDF) {
            log.warn("参数错误，文件类型错误。fileType:{}", vo.getFileType());
            throw new BizException(EssayExportErrors.ERROR_TYPE);
        } else {
            return essayExportService.createFile(vo);
        }


    }


}
