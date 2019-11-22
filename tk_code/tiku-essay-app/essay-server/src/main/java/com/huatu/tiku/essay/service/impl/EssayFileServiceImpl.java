package com.huatu.tiku.essay.service.impl;

import com.huatu.common.CommonErrors;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayFileService;
import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import com.huatu.tiku.essay.util.file.FileSizeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by x6 on 2017/12/16.
 */
@Service
@Slf4j
public class EssayFileServiceImpl  implements EssayFileService {

    private static final int PDF_WITH_ANSWER = 0;
    private static final int PDF_WITHOUT_ANSWER = 1;

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
    private RabbitTemplate rabbitTemplate;
    @Autowired
    EssayMaterialRepository essayMaterialRepository;
    @Autowired
    EssayStandardAnswerRepository essayStandardAnswerRepository;

    @Override
    public FileResultVO saveFile(long questionBaseId, long paperId, long questionAnswerId, long paperAnswerId) {

        FileResultVO resultVO = new FileResultVO();

        if(0 != questionBaseId){
                //缓存试题
                EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
                if(questionBase == null){
                    log.info("题目信息不存在，questionBaseId{}"+questionBaseId);
                    throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
                }
                if(StringUtils.isEmpty(questionBase.getPdfPath())) {
                	String singlePdfPath = essayQuestionPdfService.getSinglePdfPath(questionBaseId);
                	resultVO.setPdfPath(singlePdfPath);
                }else {
                	resultVO.setPdfPath(questionBase.getPdfPath());
                	resultVO.setFileSize(questionBase.getPdfSize());
                }
                //修改下载次数
                essayQuestionBaseRepository.updateDownloadCount(questionBaseId);
        }else if(0 != paperId){
                //缓存试卷
                EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperId);
                if(paperBase == null) {
                    log.info("试卷信息不存在，paperId{}" + paperId);
                    throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
                }

            String pdfPath = paperBase.getPdfPath();
            //如果是模考，第一次下载才生成pdf，避免取不到行测的名称（真题试卷在审核通过时生成）
                if(paperBase.getType() == AdminPaperConstant.MOCK_PAPER && StringUtils.isEmpty(paperBase.getPdfPath())){
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
        }else if(0 != questionAnswerId){

            //缓存试题批改详情
            EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(questionAnswerId);
            if(questionAnswer == null){
                log.info("试题答题卡信息不存在，questionAnswerId{}"+questionAnswerId);
                throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
            }
            String pdfPath = "";
            if(StringUtils.isEmpty(questionAnswer.getPdfPath()) || StringUtils.isEmpty(questionAnswer.getPdfSize())){
                pdfPath = essayQuestionPdfService.getSingleCorrectPdfPath(questionAnswerId);

                String finalPath = essayQuestionPdfService.uploadFile(pdfPath);
                log.info("文件cdn地址：" + finalPath);

                questionAnswer.setPdfPath(finalPath);
                String fileSize = FileSizeUtil.getFileSize(pdfPath);
                questionAnswer.setPdfSize(fileSize);
                log.info("批改后套题，文件大小"+fileSize);
                essayQuestionAnswerRepository.save(questionAnswer);

                File file = new File(pdfPath);
                if (file.isFile() && file.exists()) {
                    file.delete();
                }

                resultVO.setPdfPath(finalPath);

                log.info("试卷批改详情文件大小:"+fileSize);
                resultVO.setFileSize(fileSize);
            }else{
                resultVO.setPdfPath(questionAnswer.getPdfPath());
                resultVO.setFileSize(questionAnswer.getPdfSize());
            }

        }else if(0 != paperAnswerId){

            //缓存试卷批改详情
            EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(paperAnswerId);
            if(paperAnswer == null){
                log.info("试卷答题卡信息不存在，paperAnswerId{}"+paperAnswerId);
                throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
            }
            String pdfPath = "";
            if(StringUtils.isEmpty(paperAnswer.getPdfPath()) || StringUtils.isEmpty(paperAnswer.getPdfSize())){

                pdfPath = essayQuestionPdfService.getMultiCorrectPdfPath(paperAnswerId);
                String finalPath = essayQuestionPdfService.uploadFile(pdfPath);
                log.info("文件cdn地址：" + finalPath);

                paperAnswer.setPdfPath(finalPath);
                String fileSize = FileSizeUtil.getFileSize(pdfPath);
                paperAnswer.setPdfSize(fileSize);
                log.info("批改后套题，文件大小"+fileSize);

                essayPaperAnswerRepository.save(paperAnswer);

                File file = new File(pdfPath);
                if (file.isFile() && file.exists()) {
                    file.delete();
                }

                resultVO.setPdfPath(finalPath);

                log.info("试卷批改详情文件大小:"+fileSize);
                resultVO.setFileSize(fileSize);
            }else{
                resultVO.setPdfPath(paperAnswer.getPdfPath());
                resultVO.setFileSize(paperAnswer.getPdfSize());
            }

        }
        return resultVO;
    }

    @Override
    public EssayHtmlPaperVO createHtml(Long paperId) {
        String key = RedisKeyConstant.getEssayGuFenPaperInfoKey(paperId);
        EssayHtmlPaperVO essayHtmPaperVO = (EssayHtmlPaperVO)redisTemplate.opsForValue().get(key);
        if(essayHtmPaperVO != null ){
            return essayHtmPaperVO;
        }else{
            essayHtmPaperVO = new EssayHtmlPaperVO();

            //根据试卷id查询试卷信息
            EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperId);

            essayHtmPaperVO.setLimitTime(paperBase.getLimitTime());
            essayHtmPaperVO.setPaperName(paperBase.getName());
            essayHtmPaperVO.setScore(paperBase.getScore());

            //查询材料
            List<EssayMaterial> materialVOList = essayMaterialRepository.findByPaperIdAndStatusOrderBySortAsc
                    (paperId, EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());
            LinkedList<String> materialList = new LinkedList<>();
            for(EssayMaterial materialVO:materialVOList){
                String material = materialVO.getContent();
                materialList.add(material);
            }
            essayHtmPaperVO.setMaterialList(materialList);

            //查询试题
            List<EssayQuestionBase> questionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatus
                    (paperId, new Sort(Sort.Direction.ASC, "sort"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

            LinkedList<EssayHtmlQuestionVO> essayHtmlQuestionVOS = new LinkedList<>();
            for(EssayQuestionBase questionBase:questionBaseList){
                EssayHtmlQuestionVO essayHtmlQuestionVO = new EssayHtmlQuestionVO();
                EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionBase.getDetailId());
                List<EssayStandardAnswer> answerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc(questionBase.getDetailId(), 1);

                if(CollectionUtils.isNotEmpty(answerList) && null != answerList.get(0)){
                    BeanUtils.copyProperties(answerList.get(0),essayHtmlQuestionVO);
                }
                essayHtmlQuestionVO.setSort(questionBase.getSort());
                essayHtmlQuestionVO.setAnswerRequire(questionDetail.getAnswerRequire());

                essayHtmlQuestionVOS.add(essayHtmlQuestionVO);
            }
            essayHtmlQuestionVOS.sort((a, b) -> (a.getSort() - b.getSort()));
            essayHtmPaperVO.setQuestionList(essayHtmlQuestionVOS);
            redisTemplate.opsForValue().set(key,essayHtmPaperVO);
            return essayHtmPaperVO;
        }



    }

    @Override
    public Object getPaperList() {
        String key = RedisKeyConstant.getEssayGuFenPaperListKey();

        return  redisTemplate.opsForSet().members(key);
    }

    @Override
    public EssayUpdateVO getPageTitle() {
        String key = RedisKeyConstant.getEssayGuFenPageTitleKey();
        String pageTitle = (String)redisTemplate.opsForValue().get(key);
        return EssayUpdateVO.builder()
                .pageTitle(pageTitle)
                .build();
    }

    @Override
    public EssayUpdateVO setPageTitle(String value) {
        String key = RedisKeyConstant.getEssayGuFenPageTitleKey();
        redisTemplate.opsForValue().set(key,value);
        return EssayUpdateVO.builder()
                .pageTitle(value)
                .build();
    }

    @Override
    public Object addPaper(Long paperId) {
        String key = RedisKeyConstant.getEssayGuFenPaperListKey();
        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
        redisTemplate.opsForSet().add(key,essayPaperBase);
        return redisTemplate.opsForSet().members(key);
    }
}
