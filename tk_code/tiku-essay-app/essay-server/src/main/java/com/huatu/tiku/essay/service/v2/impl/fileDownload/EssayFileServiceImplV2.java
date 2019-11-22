package com.huatu.tiku.essay.service.v2.impl.fileDownload;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayExportErrors;
import com.huatu.tiku.essay.constant.status.AdminPaperConstant;
import com.huatu.tiku.essay.constant.status.EssayPdfTypeConstant;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import com.huatu.tiku.essay.service.v2.fileDownload.EssayFileServiceV2;
import com.huatu.tiku.essay.util.file.FileSizeUtil;
import com.huatu.tiku.essay.vo.resp.EssayCreatePdfVO;
import com.huatu.tiku.essay.vo.resp.FileResultVO;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/1
 * @描述 文件pdf下载
 */
@Service
public class EssayFileServiceImplV2 implements EssayFileServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(EssayFileServiceImplV2.class);


    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    private EssayQuestionPdfService essayQuestionPdfService;

    @Autowired
    EssayMaterialRepository essayMaterialRepository;
    @Autowired
    EssayStandardAnswerRepository essayStandardAnswerRepository;

    @Override
    public FileResultVO saveFile(long questionBaseId, long paperId, long questionAnswerId, long paperAnswerId) {

        FileResultVO resultVO = new FileResultVO();

        if (0 != questionBaseId) {
            downloadQuestion(questionBaseId, resultVO);

        } else if (0 != paperId) {
            downloadPaper(paperId, resultVO);

        } else if (0 != questionAnswerId) {
            downloadQuestionCorrectDetail(questionAnswerId, resultVO);

        } else if (0 != paperAnswerId) {
            downloadPaperCorrectDetail(paperAnswerId, resultVO);
        }
        return resultVO;
    }

    /**
     * 只下载试题
     *
     * @param questionBaseId
     */
    private void downloadQuestion(long questionBaseId, FileResultVO resultVO) {
        //缓存试题
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        if (questionBase == null) {
            throw new BizException(EssayExportErrors.RESOURCE_NOT_FOUND);
        }

        //修改路径
        if(StringUtils.isEmpty(questionBase.getPdfPath())) {
            String singlePdfPath = essayQuestionPdfService.getSinglePdfPath(questionBaseId);
            resultVO.setPdfPath(singlePdfPath);
        }else {
            resultVO.setPdfPath(questionBase.getPdfPath());
            resultVO.setFileSize(questionBase.getPdfSize());
        }
        //修改下载次数
        essayQuestionBaseRepository.updateDownloadCount(questionBaseId);
        log.info("下载试题信息是:{}", JsonUtil.toJson(resultVO));
    }


    /**
     * 只下载试卷
     *
     * @param paperId
     * @param resultVO
     */
    public void downloadPaper(long paperId, FileResultVO resultVO) {
        //缓存试卷
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperId);
        if (paperBase == null) {
            log.info("试卷信息不存在，paperId{}" + paperId);
            throw new BizException(EssayExportErrors.RESOURCE_NOT_FOUND);
        }
        String pdfPath = paperBase.getPdfPath();
        //如果是模考，第一次下载才生成pdf，避免取不到行测的名称（真题试卷在审核通过时生成）
        if (paperBase.getType() == AdminPaperConstant.MOCK_PAPER && StringUtils.isEmpty(paperBase.getPdfPath())) {
            EssayCreatePdfVO pdfVO = EssayCreatePdfVO.builder()
                    .id(paperId)
                    .type(EssayPdfTypeConstant.PAPER)
                    .build();
            pdfPath = essayQuestionPdfService.getCoverPdfPath(pdfVO.getId());
        }
        //修改下载次数
        essayPaperBaseRepository.updateDownloadCount(paperId);
        resultVO.setPdfPath(pdfPath);
        resultVO.setFileSize(paperBase.getPdfSize());
        log.info("试卷下载信息是:{}", JsonUtil.toJson(resultVO));
    }

    /**
     * 下载学员试题批改详情
     *
     * @param questionAnswerId
     * @param resultVO
     */
    public void downloadQuestionCorrectDetail(long questionAnswerId, FileResultVO resultVO) {
        //缓存试题批改详情
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(questionAnswerId);
        if (questionAnswer == null) {
            log.info("试题答题卡信息不存在，questionAnswerId{}" + questionAnswerId);
            throw new BizException(EssayExportErrors.RESOURCE_NOT_FOUND);
        }
        String pdfPath = "";
        // if (StringUtils.isEmpty(questionAnswer.getPdfPath()) || StringUtils.isEmpty(questionAnswer.getPdfSize())) {
        pdfPath = essayQuestionPdfService.getSingleCorrectPdfPath(questionAnswerId);

        String finalPath = essayQuestionPdfService.uploadFile(pdfPath);
        log.info("文件cdn地址：" + finalPath);

        questionAnswer.setPdfPath(finalPath);
        String fileSize = FileSizeUtil.getFileSize(pdfPath);
        questionAnswer.setPdfSize(fileSize);
        log.info("批改后套题，文件大小" + fileSize);
        essayQuestionAnswerRepository.save(questionAnswer);

        File file = new File(pdfPath);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
        resultVO.setPdfPath(finalPath);
        log.info("试卷批改详情文件大小,{},文件路径是:{}", fileSize, finalPath);
        resultVO.setFileSize(fileSize);
       /* } else {
            resultVO.setPdfPath(questionAnswer.getPdfPath());
            resultVO.setFileSize(questionAnswer.getPdfSize());
        }*/
        log.info("下载试题批改详情信息是:{}", JsonUtil.toJson(resultVO));
    }

    /**
     * 下载学员套卷批改详情
     *
     * @param paperAnswerId
     * @param resultVO
     */
    public void downloadPaperCorrectDetail(long paperAnswerId, FileResultVO resultVO) {
        //缓存试卷批改详情
        EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(paperAnswerId);
        if (paperAnswer == null) {
            log.info("试卷答题卡信息不存在，paperAnswerId{}" + paperAnswerId);
            throw new BizException(EssayExportErrors.RESOURCE_NOT_FOUND);
        }
        String pdfPath = "";
        if (StringUtils.isEmpty(paperAnswer.getPdfPath()) || StringUtils.isEmpty(paperAnswer.getPdfSize())) {

            pdfPath = essayQuestionPdfService.getMultiCorrectPdfPath(paperAnswerId);
            String finalPath = essayQuestionPdfService.uploadFile(pdfPath);
            log.info("文件cdn地址：" + finalPath);

            paperAnswer.setPdfPath(finalPath);
            String fileSize = FileSizeUtil.getFileSize(pdfPath);
            paperAnswer.setPdfSize(fileSize);
            log.info("批改后套题，文件大小" + fileSize);

            essayPaperAnswerRepository.save(paperAnswer);

            File file = new File(pdfPath);
            if (file.isFile() && file.exists()) {
                file.delete();
            }
            resultVO.setPdfPath(finalPath);
            log.info("试卷批改详情文件大小:{},文件路径是:{}", fileSize, finalPath);
            resultVO.setFileSize(fileSize);
        } else {
            resultVO.setPdfPath(paperAnswer.getPdfPath());
            resultVO.setFileSize(paperAnswer.getPdfSize());
        }
        log.info("试卷批改下载信息是:{}", JsonUtil.toJson(resultVO));
    }


}
