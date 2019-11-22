package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.match.EssayMockTypeConstant;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.DownloadElementEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.repository.v2.EssayCorrectImageRepository;
import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.util.CheckIsNull;
import com.huatu.tiku.essay.util.file.FileSizeUtil;
import com.huatu.tiku.essay.util.file.PdfUtil;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import com.huatu.tiku.essay.vo.file.TagPosition;
import com.huatu.tiku.essay.vo.resp.EssayDetailAndSortVO;
import com.huatu.tiku.essay.vo.resp.EssayPaperPdfVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionPdfVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import com.huatu.ztk.commons.JsonUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.huatu.tiku.essay.constant.status.EssayDetailedScoreType.CONTENT_SCORE;
import static com.huatu.tiku.essay.constant.status.EssayDetailedScoreType.SUB_SCORE;
import static com.huatu.tiku.essay.util.file.FunFileUtils.ESSAY_FILE_SAVE_PATH;
import static com.huatu.tiku.essay.util.file.FunFileUtils.PDF_ESSAY_URL;


/**
 * Create by jbzm on 171213
 */
//@Transactional
@Service
@Slf4j
public class EssayQuestionPdfServiceImpl implements EssayQuestionPdfService {

    @Autowired
    private EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;
    @Autowired
    private EssaySimilarQuestionRepository essaySimilarQuestionRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;

    @Autowired
    EssayQuestionMaterialRepository essayQuestionMaterialRepository;

    @Autowired
    EssayMaterialRepository essayMaterialRepository;

    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;

    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    EssayUserAnswerQuestionDetailedScoreRepository essayUserAnswerQuestionDetailedScoreRepository;

    @Autowired
    EssayMockExamRepository essayMockExamRepository;
    @Autowired
    EssayQuestionService essayQuestionService;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    EssayCorrectImageRepository essayCorrectImageRepository;

    @Autowired
    EssayQuestionAnswerService essayQuestionAnswerService;


    @Value("${pdf.essay}")
    private String pdfEssay;
    @Value("${pdf.picture}")
    private String pdfPicture;

    @Value("${pdf.logo}")
    private String oldLogo;
    @Value("${pdf.new.logo}")
    private String newLogo;
    @Value("${pdf.slogan}")
    private String slogan;
    @Value("${pdf.vhuatu}")
    private String vhuatu;

    @Autowired
    private UploadFileUtil uploadFileUtil;

    Pattern p = Pattern.compile("<img[^>]+>");
    Pattern p1 = Pattern.compile("\\\"([^\\\"]*)+\\\"");
    Pattern pForUnLine = Pattern.compile("`u`(.*?)`/u`");

    /**
     * 封装单题pdf
     *
     * @param questionId
     * @return 返回pdf地址
     */
    @Override
    public String getSinglePdfPath(long questionId) {
        log.info("-------------------开始创建pdf--------------------");
        long start = System.currentTimeMillis();
        //创建返回文件的地址
        String newPath = "";
        //创建VO对象
        EssayQuestionPdfVO essayQuestionPdfVO = new EssayQuestionPdfVO();
        EssayQuestionBase essayQuestionBase;
        EssayQuestionDetail essayQuestionDetail;
        List<EssayMaterial> essayMaterialList;
        //获取考试时间和地区名称
        try {
            essayQuestionBase = essayQuestionBaseRepository.findOne(questionId);
            //获取字数要求、答题要求、题干内容
            essayQuestionDetail = essayQuestionDetailRepository.findOne(essayQuestionBase.getDetailId());
            //获取资料集合
            List<EssayQuestionMaterial> essayQuestionMaterialList = essayQuestionMaterialRepository.findByQuestionBaseId(questionId);
            essayMaterialList = new LinkedList<>();
            for (EssayQuestionMaterial essayQuestionMaterial : essayQuestionMaterialList) {
                if (essayQuestionMaterial.getStatus() != -1) {
                    EssayMaterial essayMaterial = essayMaterialRepository.findOne(essayQuestionMaterial.getMaterialId());
                    essayMaterialList.add(essayMaterial);
                }
            }
            BeanUtils.copyProperties(essayQuestionBase, essayQuestionPdfVO);
            BeanUtils.copyProperties(essayQuestionDetail, essayQuestionPdfVO);

            //填充试题名称（题组名称）
            String stem = essayQuestionDetail.getStem();
            List<EssaySimilarQuestion> similarQuestionList = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(questionId, EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isNotEmpty(similarQuestionList)) {
                long similarId = similarQuestionList.get(0).getSimilarId();
                EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(similarId);
                if (similarQuestionGroupInfo != null) {
                    stem = similarQuestionGroupInfo.getShowMsg();
                }
            }
            essayQuestionPdfVO.setStem(stem);
            essayQuestionPdfVO.setContensList(essayMaterialList);
        } catch (Exception e) {
            throw new BizException(EssayErrors.PDF_OBJ_NULL);
        }
        log.info("------------------------完成映射---------------------------");
        //完成映射传递
        CheckIsNull.checkObjFieldIsNull(essayQuestionPdfVO, EssayErrors.PDF_OBJ_NULL);
        log.info("----------------------完成空值校验---------------------------");
        //设置临时文件的地址
        String filePath = pdfEssay;
        //创建文件
        Document document = new Document(PageSize.A4, 50, 50, 100, 55);
        //这段代码可以提取出来
        log.info("--------------------写入document创建完成----------------------");
        try {
            PdfUtil pdf = new PdfUtil(filePath, document);
            document.open();
            //添加pdf head
            if (!checkStringNull(essayQuestionBase.getSubAreaName())) {
                insertPdfHead(true, essayQuestionPdfVO.getStem(), essayQuestionBase.getAreaName(), essayQuestionPdfVO.getLimitTime(), essayMaterialList, document, pdf);
            } else {
                insertPdfHead(true, essayQuestionPdfVO.getStem(), essayQuestionBase.getSubAreaName(), essayQuestionPdfVO.getLimitTime(), essayMaterialList, document, pdf);
            }
            //添加答题要求
            if (StringUtils.isNotEmpty(essayQuestionPdfVO.getAnswerRequire())) {
                pdf.addBlank(10, document);
                pdf.addBaseTitle("【作答要求】", document);
                insertQuestionRequire(essayQuestionPdfVO.getAnswerRequire(), pdf, document);
            }
//            pdf.addBlank(20, document);
//            pdf.addBaseTitle("【答案书写】", document);
            document.newPage();
            //设置格子
            pdf.addLittleTitle("答题纸", document);
            pdf.addBlank(20, document);
            pdf.addForm(essayQuestionPdfVO.getInputWordNumMax(), document);
            document.close();
            newPath = pdf.addWaterImage(filePath, newLogo, slogan, vhuatu, oldLogo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("插入异常");
        }
        log.info("---------------------------完成pdf构建--------------------------");
        //上传文件，保存最终的cdn访问地址
        log.info("---------------------------开始上传pdf--------------------------");
        log.info("临时文件地址：" + newPath);
        String finalPath = uploadFile(newPath);
        String fileSize = FileSizeUtil.getFileSize(newPath);
        log.info("文件cdn地址：" + finalPath);
        log.info("单题文件大小：" + fileSize);
        essayQuestionBase.setPdfSize(fileSize);
        essayQuestionBase.setPdfPath(finalPath);
        log.info("---------------------------本地地址保存--------------------------");
        essayQuestionBaseRepository.save(essayQuestionBase);
        //删除临时文件
        deleteFile(newPath, start);
        return finalPath;
    }

    /**
     * 封装套题pdf
     *
     * @param paperId
     * @return
     */
    @Override
    public String getCoverPdfPath(long paperId) {
        long start = System.currentTimeMillis();
        //创建返回文件的地址
        String newPath = "";
        EssayPaperPdfVO essayPaperPdfVO = new EssayPaperPdfVO();
        EssayPaperBase essayPaperBase;
        List<EssayMaterial> essayMaterialList;
        List<EssayQuestionBase> essayQuestionBaseList;
        List<EssayDetailAndSortVO> essayDetailAndSortVOList;
        try {
            //查询试卷的地点名称,考试时间,总分数,detilid
            essayPaperBase = essayPaperBaseRepository.findOne(paperId);
            //获取试卷内容
            essayMaterialList = essayMaterialRepository.findByPaperIdAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatus(paperId, new Sort(Sort.Direction.ASC, "sort"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            //获取题干
            essayDetailAndSortVOList = new LinkedList<>();
            for (EssayQuestionBase essayQuestionBase : essayQuestionBaseList) {
                EssayDetailAndSortVO essayDetailAndSortVO = new EssayDetailAndSortVO();
                essayDetailAndSortVO.setEssayQuestionDetail(essayQuestionDetailRepository.findOne(essayQuestionBase.getDetailId()));
                essayDetailAndSortVO.setSort(essayQuestionBase.getSort());
                essayDetailAndSortVOList.add(essayDetailAndSortVO);
            }
            //给vo赋值
            BeanUtils.copyProperties(essayPaperBase, essayPaperPdfVO);
            essayMaterialList.sort(Comparator.comparing(EssayMaterial::getSort));
            essayPaperPdfVO.setContent(essayMaterialList);
        } catch (Exception e) {
            throw new BizException(EssayErrors.PDF_OBJ_NULL);
        }
        //校验vo是否为空
        CheckIsNull.checkObjFieldIsNull(essayPaperPdfVO, EssayErrors.PDF_OBJ_NULL);
        //开始画pdf
        //设置临时文件的地址
        String filePath = pdfEssay;
        //创建文件
        Document document = new Document(PageSize.A4, 50, 50, 100, 55);
		try {
			PdfUtil pdf = new PdfUtil();
			PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(new File(filePath)));
		    pdfWriter.setStrictImageSequence(true);
			document.open();
			EssayMockExam mock = essayMockExamRepository.findOne(paperId);
			// 添加封皮
			pdf.addCover(document, mock, essayPaperBase.getName());
			// addImage(url, imageTail, document, pdf);
			// 添加pdf head
			// if (mock != null && mock.getMockType() ==
			// EssayMockTypeConstant.ESSAY_MOCK_WITH_LINE) {
			// String practiceInfoKey = RedisKeyConstant.getPracticeInfoKey();
			// log.info("进入申论模考了lol~~~~~");
			// Object obj = redisTemplate.opsForHash().get(practiceInfoKey,
			// mock.getPracticeId() + "");
			insertMockPdfHead(essayPaperPdfVO.getContent(), document, pdf);
			pdf.addBlank(10, document);
			//作答要求新增一页
			document.newPage();
			pdf.addBaseTitle("【作答要求】", document);
			pdf.addBlank(8, document);
			for (EssayDetailAndSortVO essayDetailAndSortVO : essayDetailAndSortVOList) {
				if (StringUtils.isNotEmpty(essayDetailAndSortVO.getEssayQuestionDetail().getAnswerRequire())) {
					pdf.addBlank(8, document);
					insertQuestionRequire(essayDetailAndSortVO.getEssayQuestionDetail().getAnswerRequire(), pdf,
							document);
				}
			}
//            pdf.addBlank(20, document);
//            pdf.addBaseTitle("【答案书写】", document);
			document.newPage();
			// 设置格子
			pdf.addLittleTitle("答题纸", document);
			pdf.addBlank(20, document);
			for (Iterator iterator = essayDetailAndSortVOList.iterator(); iterator.hasNext();) {
				EssayDetailAndSortVO essayDetailAndSortVO = (EssayDetailAndSortVO) iterator.next();
				pdf.addBaseTitle("第" + getChineseCharacterNum(essayDetailAndSortVO.getSort()) + "题", document);
				pdf.addBlank(8, document);
				pdf.addForm(essayDetailAndSortVO.getEssayQuestionDetail().getInputWordNumMax(), document);
				if (iterator.hasNext()) {
					if (mock != null && mock.getId() == 793 && essayDetailAndSortVO.getSort() == 3) {
						log.info("生成pdf特殊处理");
					} else {
						pdf.addBlank(20, document);
					}
				}
				
			}
			pdf.addLastPageAdvert(pdfWriter, document);
			document.close();
			newPath = pdf.addWaterImage(filePath, newLogo, slogan, vhuatu, oldLogo);
		} catch (Exception e) {
            e.printStackTrace();
            log.info("插入异常");
        }
        log.info("临时文件地址：" + newPath);
        String finalPath = uploadFile(newPath);
        log.info("文件cdn地址：" + finalPath);
        essayPaperBase.setPdfPath(finalPath);
        String fileSize = FileSizeUtil.getFileSize(newPath);
        log.info("套题文件大小" + fileSize);
        essayPaperBase.setPdfSize(fileSize);
        essayPaperBaseRepository.save(essayPaperBase);
        //删除临时文件并打印运行时间
        deleteFile(newPath, start);
        return finalPath;
    }

    private String getChineseCharacterNum(int num) {

        String numStr = "" + num;
        switch (num) {
            case 1: {
                numStr = "一";
                break;
            }
            case 2: {
                numStr = "二";
                break;
            }
            case 3: {
                numStr = "三";
                break;
            }
            case 4: {
                numStr = "四";
                break;
            }
            case 5: {
                numStr = "五";
                break;
            }
            case 6: {
                numStr = "六";
                break;
            }
            case 7: {
                numStr = "七";
                break;
            }
            case 8: {
                numStr = "八";
                break;
            }
            case 9: {
                numStr = "九";
                break;
            }

        }
        return numStr;

    }

    /**
     * 封装单题批改pdf
     *
     * @param answerId
     * @return
     */
    @Override
    public String getSingleCorrectPdfPath(long answerId) {
        long start = System.currentTimeMillis();
        //创建返回文件的地址
        String newPath = "";
        //创建VO对象
        EssayQuestionPdfVO essayQuestionPdfVO = new EssayQuestionPdfVO();
        EssayQuestionBase essayQuestionBase;
        EssayQuestionDetail essayQuestionDetail;
        List<EssayMaterial> essayMaterialList;
        EssayQuestionAnswer essayQuestionAnswer;
        List<EssayUserAnswerQuestionDetailedScore> essayUserAnswerQuestionDetailedScoreList;
        //获取考试时间和地区名称
        try {
            essayQuestionAnswer = essayQuestionAnswerRepository.findOne(answerId);
            essayQuestionBase = essayQuestionBaseRepository.findOne(essayQuestionAnswer.getQuestionBaseId());
            //获取字数要求、答题要求、题干内容
            essayQuestionDetail = essayQuestionDetailRepository.findOne(essayQuestionBase.getDetailId());
            //获取资料集合
            List<EssayQuestionMaterial> essayQuestionMaterialList = essayQuestionMaterialRepository.findByQuestionBaseIdAndStatus(essayQuestionAnswer.getQuestionBaseId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            essayMaterialList = new LinkedList<>();
            essayQuestionMaterialList.forEach(essayQuestionMaterial -> {
                if (essayQuestionMaterial.getStatus() != -1) {
                    EssayMaterial essayMaterial = essayMaterialRepository.findOne(essayQuestionMaterial.getMaterialId());
                    essayMaterialList.add(essayMaterial);
                }
            });
            BeanUtils.copyProperties(essayQuestionBase, essayQuestionPdfVO);
            BeanUtils.copyProperties(essayQuestionDetail, essayQuestionPdfVO);

            //填充试题名称（题组名称）
            String stem = "";
            List<EssaySimilarQuestion> similarQuestionList = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(essayQuestionAnswer.getQuestionBaseId(), EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isNotEmpty(similarQuestionList)) {
                long similarId = similarQuestionList.get(0).getSimilarId();
                EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(similarId);
                if (similarQuestionGroupInfo != null) {
                    stem = similarQuestionGroupInfo.getShowMsg();
                }
            }
            if (StringUtils.isNotEmpty(essayQuestionBase.getSubAreaName())) {
                essayQuestionPdfVO.setAreaName(essayQuestionBase.getSubAreaName());
            }
            essayMaterialList.sort(Comparator.comparing(EssayMaterial::getSort));
            essayQuestionPdfVO.setContensList(essayMaterialList);
            essayUserAnswerQuestionDetailedScoreList = essayUserAnswerQuestionDetailedScoreRepository.findByQuestionAnswerIdAndStatus(essayQuestionAnswer.getId(), 1, new Sort(Sort.Direction.ASC, "sequenceNumber"));
        } catch (Exception e) {
            throw new BizException(EssayErrors.PDF_OBJ_NULL);
        }
        //完成映射传递
        CheckIsNull.checkObjFieldIsNull(essayQuestionPdfVO, EssayErrors.PDF_OBJ_NULL);
        //设置临时文件的地址
        String filePath = pdfEssay;
        //创建文件
        Document document = new Document(PageSize.A4, 50, 50, 100, 55);
        //这段代码可以提取出来
        try {
            PdfUtil pdf = new PdfUtil(filePath, document);
            document.open();
            //添加pdf head
            insertPdfHeadCorrect(essayQuestionAnswer.getExamScore(), essayQuestionAnswer.getSpendTime(), true, essayQuestionPdfVO.getStem(), essayQuestionPdfVO.getAreaName(), essayQuestionPdfVO.getLimitTime(), essayMaterialList, document, pdf);
            //添加答题要求&批改得分&原因
            boolean isIntelligence = null == essayQuestionAnswer.getCorrectMode() || essayQuestionAnswer.getCorrectMode() == CorrectModeEnum.INTELLIGENCE.getMode();
            // TODO 人工批改或者智能转人工
            insertCorrectContent(essayQuestionPdfVO.getAnswerRequire(), essayQuestionAnswer, pdf, document, essayUserAnswerQuestionDetailedScoreList, isIntelligence);


            //添加答题规则细节

            //v6.3版本答案换成多个，从新表里查答案
            List<EssayStandardAnswer> standardAnswerList = essayQuestionService.findStandardAnswer(essayQuestionDetail.getId());
            insertAnswerRule(essayQuestionDetail, standardAnswerList, document, pdf);
            document.close();
            //添加水印
            newPath = pdf.addWaterImage(filePath, newLogo, slogan, vhuatu, oldLogo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("插入异常");
        }
        //上传文件放在  api  缓存时上传（否则 文件大小获取不到）
        log.info("临时文件地址：" + newPath);
        return newPath;
    }

    /**
     * 关于用户作答批改情况写入
     *
     * @param answerRequire
     * @param essayQuestionAnswer
     * @param pdf
     * @param document
     * @param essayUserAnswerQuestionDetailedScoreList
     * @param isIntelligence
     * @throws Exception
     */
    private void insertCorrectContent(String answerRequire,
                                      EssayQuestionAnswer essayQuestionAnswer,
                                      PdfUtil pdf, Document document,
                                      List<EssayUserAnswerQuestionDetailedScore> essayUserAnswerQuestionDetailedScoreList,
                                      boolean isIntelligence) throws Exception {
        // TODO 人工批改或者智能转人工
        if (!isIntelligence) {      //所有人工批改的答题卡处理逻辑
            insertManualCorrectScore(answerRequire, essayQuestionAnswer, pdf, document);
            return;
        }
        boolean isArgument = essayQuestionAnswer.getQuestionType() == 5;
        if (!isArgument) {
            insertCorrectScore(answerRequire, essayQuestionAnswer, pdf, document, essayUserAnswerQuestionDetailedScoreList);
        } else {
            insertManualCorrectScore(answerRequire, essayQuestionAnswer, pdf, document);
        }
    }

    /**
     * 封装套题批改pdf
     *
     * @param paperAnswerId
     * @return
     */
    @Override
    public String getMultiCorrectPdfPath(long paperAnswerId) {
        long start = System.currentTimeMillis();
        //创建返回文件的地址
        String newPath = "";
        EssayPaperPdfVO essayPaperPdfVO = new EssayPaperPdfVO();
        EssayPaperBase essayPaperBase;
        EssayPaperAnswer essayPaperAnswer;
        List<EssayMaterial> essayMaterialList;
        List<EssayQuestionBase> essayQuestionBaseList;
        List<EssayQuestionAnswer> essayQuestionAnswerList;
        List<EssayDetailAndSortVO> essayDetailAndSortVOList;
        Map<Long, List<EssayUserAnswerQuestionDetailedScore>> paperDetailedScore = new HashMap<>();
        try {
            //查询是题答题卡（detail 升序排列）
            essayQuestionAnswerList = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus(paperAnswerId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "questionDetailId"));
            //查询每道题的得分点（序号升序排列）
            for (EssayQuestionAnswer essayQuestionAnswer : essayQuestionAnswerList) {
                log.info("=======" + essayQuestionAnswer.getId());
                List<EssayUserAnswerQuestionDetailedScore> essayUserAnswerQuestionDetailedScoreList = essayUserAnswerQuestionDetailedScoreRepository.findByQuestionAnswerIdAndStatus(essayQuestionAnswer.getId(), 1, new Sort(Sort.Direction.ASC, "sequenceNumber"));
                log.info("=======" + essayUserAnswerQuestionDetailedScoreList.size());
                paperDetailedScore.put(essayQuestionAnswer.getQuestionDetailId(), essayUserAnswerQuestionDetailedScoreList);
            }
            //查询作答结束的试卷答题卡
            essayPaperAnswer = essayPaperAnswerRepository.findOne(paperAnswerId);
            //查询试卷的地点名称,考试时间,总分数
            essayPaperBase = essayPaperBaseRepository.findOne(essayPaperAnswer.getPaperBaseId());
            //获取试卷材料
            essayMaterialList = essayMaterialRepository.findByPaperIdAndStatusOrderBySortAsc(essayPaperAnswer.getPaperBaseId(), EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus());
            //获取试卷的试题列表
            essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatus(essayPaperAnswer.getPaperBaseId(), new Sort(Sort.Direction.ASC, "sort"), EssayStatusEnum.NORMAL.getCode());
            //获取题干
            essayDetailAndSortVOList = new LinkedList<>();
            essayQuestionBaseList.forEach(essayQuestionBase -> {
                EssayDetailAndSortVO essayDetailAndSortVO = new EssayDetailAndSortVO();
                essayDetailAndSortVO.setEssayQuestionDetail(essayQuestionDetailRepository.findOne(essayQuestionBase.getDetailId()));
                essayDetailAndSortVO.setSort(essayQuestionBase.getSort());
                essayDetailAndSortVOList.add(essayDetailAndSortVO);
            });
            //给vo赋值
            BeanUtils.copyProperties(essayPaperBase, essayPaperPdfVO);

            if (StringUtils.isNotEmpty(essayPaperBase.getSubAreaName())) {
                essayPaperPdfVO.setAreaName(essayPaperBase.getSubAreaName());
            }
            essayPaperPdfVO.setContent(essayMaterialList);
        } catch (Exception e) {
            throw new BizException(EssayErrors.PDF_OBJ_NULL);
        }
        CheckIsNull.checkObjFieldIsNull(essayPaperPdfVO, EssayErrors.PDF_OBJ_NULL);
        //开始画pdf
        //设置临时文件的地址
        String filePath = pdfEssay;
        //创建文件
        Document document = new Document(PageSize.A4, 50, 50, 100, 55);
        try {
            PdfUtil pdf = new PdfUtil(filePath, document);
            document.open();
            EssayMockExam mock = essayMockExamRepository.findOne(essayPaperBase.getId());
            //添加pdf head
            if (mock != null && mock.getMockType() == EssayMockTypeConstant.ESSAY_MOCK_WITH_LINE) {
                String practiceInfoKey = RedisKeyConstant.getPracticeInfoKey();
                Object obj = redisTemplate.opsForHash().get(practiceInfoKey, mock.getPracticeId() + "");
                insertPdfHead(false, (String) obj + "-申论", "", essayPaperPdfVO.getLimitTime(), essayPaperPdfVO.getContent(), document, pdf);
            } else {
                insertPdfHeadCorrect(essayPaperAnswer.getExamScore(), essayPaperAnswer.getSpendTime(), false, essayPaperPdfVO.getName(), "", essayPaperPdfVO.getLimitTime(), essayPaperPdfVO.getContent(), document, pdf);
            }
            boolean isIntelligence = null == essayPaperAnswer.getCorrectMode() || essayPaperAnswer.getCorrectMode() == CorrectModeEnum.INTELLIGENCE.getMode();

            for (int i = 0; i < essayDetailAndSortVOList.size(); i++) {

                long detailId = essayDetailAndSortVOList.get(i).getEssayQuestionDetail().getId();
                EssayQuestionAnswer questionAnswer = new EssayQuestionAnswer();
                for (EssayQuestionAnswer answer : essayQuestionAnswerList) {
                    if (answer.getQuestionDetailId() == detailId) {
                        questionAnswer = answer;
                    }
                }
                //添加答题要求&批改得分&原因
                List<EssayUserAnswerQuestionDetailedScore> essayUserAnswerQuestionDetailedScoreList = essayUserAnswerQuestionDetailedScoreRepository.findByQuestionAnswerIdAndStatus(questionAnswer.getId(), 1, new Sort(Sort.Direction.ASC, "sequenceNumber"));
                insertCorrectContent(essayDetailAndSortVOList.get(i).getEssayQuestionDetail().getAnswerRequire(),
                        questionAnswer,
                        pdf,
                        document,
                        essayUserAnswerQuestionDetailedScoreList, isIntelligence);
//                insertCorrectScore(essayDetailAndSortVOList.get(i).getEssayQuestionDetail().getAnswerRequire(), questionAnswer, pdf, document, essayUserAnswerQuestionDetailedScoreList);
//                if (paperDetailedScore.get(detailId) != null && essayDetailAndSortVOList.get(i).getEssayQuestionDetail().getType() != 5) {
//                    //得分项
////                    insertAnswerResult(essayQuestionAnswerList.get(i), paperDetailedScore.get(i), 1, "+", document, pdf);
//                    //格式得分
////                    insertAnswerResult(questionAnswer, paperDetailedScore.get(detailId), 2, "+", document, pdf);
//                    //扣分项
////                    insertAnswerResult(questionAnswer, paperDetailedScore.get(detailId), 3, "-", document, pdf);
//                }
                //添加答题规则细节
                //v6.3版本答案换成多个，从新表里查答案
                List<EssayStandardAnswer> standardAnswerList = essayQuestionService.findStandardAnswer(essayDetailAndSortVOList.get(i).getEssayQuestionDetail().getId());
                insertAnswerRule(essayDetailAndSortVOList.get(i).getEssayQuestionDetail(), standardAnswerList, document, pdf);
                pdf.addBlank(15, document);
                //试卷综合阅卷
                getPaperRemark(essayPaperAnswer, pdf, document);
            }
            document.close();
            newPath = pdf.addWaterImage(filePath, newLogo, slogan, vhuatu, oldLogo);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("插入异常");
        }
        //上传文件放在  api  缓存时上传（否则 文件大小获取不到）
        log.info("临时文件地址：" + newPath);
        return newPath;
    }


    @Override
    public String uploadFile(String filePath) {

        File file = new File(filePath);
        String fileName = getFileName(filePath);
        //向文件中写数据
        try {
            uploadFileUtil.ftpUploadFile(file, new String(fileName.getBytes("UTF-8"), "iso-8859-1"), ESSAY_FILE_SAVE_PATH);
        } catch (BizException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return PDF_ESSAY_URL + fileName.replaceAll("null", "");
    }

    /**
     * 添加答题要求
     *
     * @param answerRequire
     * @param pdf
     * @param document
     * @throws Exception
     */
    private void insertQuestionRequire(String answerRequire, PdfUtil pdf, Document document) throws Exception {
        pdf.addBaseContent(changeString(answerRequire), document);
    }

    /**
     * 添加资料内容
     *
     * @param essayMaterial
     * @param pdf
     * @param document
     * @throws Exception
     */
    private void insertDatum(EssayMaterial essayMaterial, PdfUtil pdf, Document document) throws Exception {
//        pdf.addBlank(5, document);
//        pdf.addBaseTitle("资料" + essayMaterial.getSort(), document);
        String img = essayMaterial.getContent();
        //切割<img标签
        String[] imgSpl = img.split("<img[^>]+>");
        if (imgSpl.length >= 1) {
            log.debug("----------------开始添加图片-------------");
            Matcher m = p.matcher(img);
            pdf.addBlank(5, document);
            //添加内容如果有下划线则会添加下划线
            changeUnLine(imgSpl[0], pdf, document);
            int i = 1;
            while (m.find()) {
                String lol = m.group();
                Matcher m1 = p1.matcher(lol);
                if (m1.find()) {
                    log.debug("--------------添加临时图片---------------");
                    //取出url
                    String url = m1.group().substring(1, m1.group().length() - 1);
                    //取出尾缀
                    String imageTail = url.substring(url.length() - 4, url.length());
                    addImage(url, imageTail, document, pdf);
                    changeUnLine(imgSpl[i], pdf, document);
                    log.debug("-------------临时文件已经删除-------------");
                }
                i++;
            }
        } else {
//            pdf.addBlank(5, document);
            pdf.addBaseContent(changeString(img), document);
        }
    }

    private void addImage(String url, String imageTail, Document document, PdfUtil pdf) throws IOException, DocumentException {
        //拼接文件地址
        String imageName = pdfPicture + UUID.randomUUID().toString().replaceAll("-", "") + imageTail;
        //下载图片
        pdf.downloadPicture(url, imageName);
        //读取一个图片
        Image image = Image.getInstance(imageName);
        if(image.getPlainWidth() > 480){
            image.scaleToFit(480, image.getPlainHeight() * 480/image.getPlainWidth());
        }
        image.setScaleToFitHeight(true);
        image.setAlignment(Image.MIDDLE | Image.TEXTWRAP);
        //插入一个图片
        document.add(image);

        log.debug("--------------添加图片结束---------------");
        File file1 = new File(imageName);
        file1.deleteOnExit();
    }

    /**
     * 添加答题规细节规则
     *
     * @param essayQuestionDetail
     * @param document
     * @param pdf
     * @throws Exception
     */
    private void insertAnswerRule(EssayQuestionDetail essayQuestionDetail, List<EssayStandardAnswer> standardAnswerList, Document document, PdfUtil pdf) throws Exception {
        //答案
        int answerCount = 1;
        for (EssayStandardAnswer answer : standardAnswerList) {
            if (StringUtils.isNotEmpty(changeString(answer.getAnswerComment()))) {
                pdf.addBlank(10, document);

                //参考答案

                if (answer.getAnswerFlag() == 0) {
                    if (standardAnswerList.size() == 1) {
                        pdf.addBaseTitle("【参考答案】", document);
                    } else {
                        pdf.addBaseTitle("【参考答案" + answerCount + "】", document);
                    }
                    //标准答案
                } else {
                    pdf.addBaseTitle("【标准答案】", document);
                }

                if (checkStringNull(answer.getTopic())) {
                    Paragraph paragraph1 = new Paragraph(answer.getTopic(), pdf.CONTENT_FONT);
                    paragraph1.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragraph1);
                }
                if (checkStringNull(answer.getSubTopic())) {
                    Paragraph paragraph2 = new Paragraph(answer.getSubTopic(), pdf.CONTENT_FONT);
                    paragraph2.setAlignment(Element.ALIGN_CENTER);
                    document.add(paragraph2);
                }
                if (checkStringNull(answer.getCallName())) {
                    Paragraph paragraph3 = new Paragraph(answer.getCallName(), pdf.CONTENT_FONT);
                    paragraph3.setIndentationLeft(5);
                    document.add(paragraph3);
                }
                //添加答案
                pdf.addBaseContent(changeString(answer.getAnswerComment()), document);
                if (checkStringNull(answer.getInscribedName())) {
                    String inscribedName = answer.getInscribedName();
                    if (StringUtils.isNotEmpty(inscribedName)) {
                        String[] split = inscribedName.split("<br/>");
                        for (int i = 0; i < split.length; i++) {
                            Paragraph paragraph5 = new Paragraph(split[i], pdf.CONTENT_FONT);
                            paragraph5.setAlignment(Element.ALIGN_RIGHT);
                            document.add(paragraph5);
                        }
                    }

                }
                if (checkStringNull(answer.getInscribedDate())) {
                    Paragraph paragraph4 = new Paragraph(answer.getInscribedDate(), pdf.CONTENT_FONT);
                    paragraph4.setAlignment(Element.ALIGN_RIGHT);
                    document.add(paragraph4);
                }

            }
            answerCount++;
        }
        if (StringUtils.isNotEmpty(changeString(essayQuestionDetail.getAnalyzeQuestion()))) {
            pdf.addBlank(10, document);
            pdf.addBaseTitle("【试题分析】", document);
            double difficultGrade = essayQuestionDetail.getDifficultGrade();
            String difficultGradeStr = "";
            switch ((int) difficultGrade) {
                case 0:
                    break;
                case 1:
                    difficultGradeStr = "难度：较小";
                    break;
                case 2:
                    difficultGradeStr = "难度：适中";
                    break;
                case 3:
                    difficultGradeStr = "难度：较大";
                    break;
                default:
                    break;
            }

            pdf.addBaseContent(PdfUtil.PDF_HEAD_BLANK + changeString(difficultGradeStr), document);
            pdf.addBaseContent(changeString(essayQuestionDetail.getAnalyzeQuestion()), document);
        }
        if (StringUtils.isNotEmpty(changeString(essayQuestionDetail.getAuthorityReviews()))) {
            pdf.addBlank(10, document);
            pdf.addBaseTitle("【经验小结】", document);
            pdf.addBaseContent(changeString(essayQuestionDetail.getAuthorityReviews()), document);
        }
    }

    //正则匹配是否为空（只有标签  空格）
    private boolean checkStringNull(String str) {
        if ("<p></p>".equals(str)) {
            return false;
        }
        if (StringUtils.isEmpty(changeString(str))) {
            return false;
        }
        if ("".equals(str) || " ".equals(str)) {
            return false;
        }
        return true;
    }


//    /**
//     * 添加答案展示
//     *
//     * @param essayUserAnswerQuestionDetailedScoreList
//     * @param type
//     * @param sign
//     * @param document
//     * @throws DocumentException
//     */
//    public void insertAnswerResult(EssayQuestionAnswer essayQuestionAnswer, List<EssayUserAnswerQuestionDetailedScore> essayUserAnswerQuestionDetailedScoreList, int type, String sign, Document document, PdfUtil pdf) throws DocumentException {
//        if (type == 1 && essayQuestionAnswer.getExamScore() != 0 && essayQuestionAnswer.getQuestionType() > 3) {
//            pdf.addBaseTitle("得分项：", document);
//        } else if (type == 2 && essayQuestionAnswer.getExamScore() != 0 && essayUserAnswerQuestionDetailedScoreRepository.countByQuestionAnswerIdAndTypeAndStatus(essayQuestionAnswer.getId(), 2, 1) > 0) {
//            pdf.addBaseTitle("格式得分：", document);
//        } else if (type == 3 && essayQuestionAnswer.getExamScore() != 0 && essayUserAnswerQuestionDetailedScoreRepository.countByQuestionAnswerIdAndTypeAndStatus(essayQuestionAnswer.getId(), 3, 1) > 0) {
//            pdf.addBaseTitle("扣分项：", document);
//        } else if (type == 4 && essayQuestionAnswer.getExamScore() != 0 && essayUserAnswerQuestionDetailedScoreRepository.countByQuestionAnswerIdAndTypeAndStatus(essayQuestionAnswer.getId(), 4, 1) > 0) {
//            pdf.addBaseTitle("其他：", document);
//        } else {
//            return;
//        }
//        int index = 1;
//        for (int i = 0; i < essayUserAnswerQuestionDetailedScoreList.size(); i++) {
//            Paragraph paragraph = new Paragraph();
//            String sort = "";
//            if (essayUserAnswerQuestionDetailedScoreList.get(i).getType() == type) {
//                if (essayQuestionAnswer.getQuestionType() > 3 && type == 1) {
//                    sort = "(" + index + ")";
//                }
//                Phrase phrase1 = new Phrase(sort + essayUserAnswerQuestionDetailedScoreList.get(i).getScorePoint() + "(" + sign, PdfUtil.CONTENT_FONT);
//                Phrase phrase2 = new Phrase(essayUserAnswerQuestionDetailedScoreList.get(i).getScore() + "", PdfUtil.CONTENT_FONT_RED_UNDERLINE);
//                Phrase phrase3 = new Phrase("分)", PdfUtil.CONTENT_FONT);
//                paragraph.add(phrase1);
//                paragraph.add(phrase2);
//                paragraph.add(phrase3);
//                index++;
//            }
//            document.add(paragraph);
//        }
//    }

    /**
     * 添加批改得分
     *
     * @param essayQuestionAnswer
     * @param pdf
     * @param document
     * @throws Exception
     */
    public void insertCorrectScore(String answerRequire, EssayQuestionAnswer essayQuestionAnswer, PdfUtil pdf, Document document, List<EssayUserAnswerQuestionDetailedScore> essayUserAnswerQuestionDetailedScoreList) throws Exception {
        if (StringUtils.isNotEmpty(answerRequire)) {
            pdf.addBlank(10, document);
            pdf.addBaseTitle("【作答要求】", document);
            pdf.addBaseContent(changeString(answerRequire), document);
        }
        pdf.addBlank(10, document);
        pdf.addBaseTitle("【批改得分】", document);
        int spendMinute = essayQuestionAnswer.getSpendTime() / 60;
        int spendSecond = essayQuestionAnswer.getSpendTime() % 60;
        pdf.addBaseContent(PdfUtil.PDF_HEAD_BLANK + "得分：" + essayQuestionAnswer.getExamScore() + "分/" + essayQuestionAnswer.getScore() + "分   用时 : " + spendMinute + "分" + spendSecond + "秒" + "   字数 :" + essayQuestionAnswer.getInputWordNum() + "字", document);

        /**
         * 0806  内容分
         */
        double contentScore = 0D;
        double formatScore = 0D;
        LinkedList<EssayUserAnswerQuestionDetailedScore> formatScoreDetailList = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(essayUserAnswerQuestionDetailedScoreList)) {
            for (EssayUserAnswerQuestionDetailedScore detailScore : essayUserAnswerQuestionDetailedScoreList) {
                if (detailScore.getType() == CONTENT_SCORE) {
                    contentScore += detailScore.getScore();
                } else {
                    if (detailScore.getType() == SUB_SCORE) {
                        formatScore -= detailScore.getScore();
                    } else {
                        formatScore += detailScore.getScore();
                    }
                    formatScoreDetailList.add(detailScore);
                }
            }

        }
        //格式得分不为0 展示内容分。
        if (formatScore != 0 && contentScore != 0) {
            pdf.addBaseContent("内容分：" + contentScore + "分", document);
        }

        if (StringUtils.isNotEmpty(essayQuestionAnswer.getContent())) {
            if (essayQuestionAnswer.getQuestionType() == 5) {
                pdf.addBaseTitle("【我的作答】", document);
                pdf.correct(essayQuestionAnswer.getContent(), document, essayQuestionAnswer.getQuestionType(), essayQuestionAnswer.getId());
            } else {
                if (StringUtils.isNotEmpty(essayQuestionAnswer.getCorrectedContent())) {
                    pdf.addBlank(5, document);
                    String correctedContent = essayQuestionAnswer.getCorrectedContent();
                    if (essayQuestionAnswer.getQuestionType() == 4) {
                        correctedContent = produceDetailScore(essayQuestionAnswer.getCorrectedContent(), essayUserAnswerQuestionDetailedScoreList);
                    }
                    pdf.correct(correctedContent, document, essayQuestionAnswer.getQuestionType(), essayQuestionAnswer.getId());
                }
            }
        }

        if (formatScore != 0) {
            pdf.addBaseContent("形式分：" + formatScore + "分", document);
            for (int i = 0; i < formatScoreDetailList.size(); i++) {
                int index = i + 1;
                Paragraph paragraph = new Paragraph();
                String sort = "(" + index + ")";
                String sign = formatScoreDetailList.get(i).getType() == SUB_SCORE ? "-" : "+";

                Phrase phrase1 = new Phrase(sort + formatScoreDetailList.get(i).getScorePoint() + "(" + sign, PdfUtil.CONTENT_FONT);
                Phrase phrase2 = new Phrase(formatScoreDetailList.get(i).getScore() + "", PdfUtil.CONTENT_FONT);
                Phrase phrase3 = new Phrase("分)", PdfUtil.CONTENT_FONT);
                paragraph.add(phrase1);
                paragraph.add(phrase2);
                paragraph.add(phrase3);
                document.add(paragraph);
            }
        }
    }

    private String produceDetailScore(String correctedContent, List<EssayUserAnswerQuestionDetailedScore> essayUserAnswerQuestionDetailedScoreList) {
        if (StringUtils.isNotEmpty(correctedContent) && CollectionUtils.isNotEmpty(essayUserAnswerQuestionDetailedScoreList)) {
            for (int i = 1; i <= essayUserAnswerQuestionDetailedScoreList.size(); i++) {
                correctedContent = correctedContent.replace("(" + i + ")", "(+" + essayUserAnswerQuestionDetailedScoreList.get(i - 1).getScore() + "分)");
            }

        }

        return correctedContent;
    }

    /**
     * 删除文件
     *
     * @param newPath
     * @param start
     */
    private void deleteFile(String newPath, long start) {
        //删除临时文件
        File file = new File(newPath);
        if (file.isFile() && file.exists()) {
            file.delete();
        }
        //打印运行时间
        long end = System.currentTimeMillis();
        log.info("文件上传总用时" + (end - start) + "毫秒");
    }

    /**
     * 添加 pdf head
     *
     * @param stem
     * @param areaName
     * @param limitTime
     * @param essayMaterialList
     * @param document
     * @param pdf
     * @throws Exception
     */
    private void insertPdfHead(boolean b, String stem, String areaName, int limitTime, List<EssayMaterial> essayMaterialList, Document document, PdfUtil pdf) throws Exception {

        if (StringUtils.isNotEmpty(areaName)) {
            pdf.addTitle(stem + "(" + areaName + ")", document);
        } else {
            pdf.addTitle(stem, document);
        }
        if (!b) {
            pdf.addBlank(10, document);
            pdf.addLittleTitle("考试时间：" + limitTime / 60 + "分钟", document);
        }
        pdf.addBlank(20, document);
        pdf.addBaseTitle("【给定资料】", document);
        for (EssayMaterial essayMaterial : essayMaterialList) {
            insertDatum(essayMaterial, pdf, document);
        }
    }
    
    /**
     * 模考pdf不添加试卷名称和时间的头部
     * @param b
     * @param stem
     * @param areaName
     * @param limitTime
     * @param essayMaterialList
     * @param document
     * @param pdf
     * @throws Exception
     */
    private void insertMockPdfHead(List<EssayMaterial> essayMaterialList, Document document, PdfUtil pdf) throws Exception {
        pdf.addBaseTitle("【给定资料】", document);
        for (EssayMaterial essayMaterial : essayMaterialList) {
            insertDatum(essayMaterial, pdf, document);
        }
    }
    

    private void insertPdfHeadCorrect(double examScore, int time, boolean b, String stem, String areaName, int limitTime, List<EssayMaterial> essayMaterialList, Document document, PdfUtil pdf) throws Exception {
        if (StringUtils.isNotEmpty(areaName)) {
            pdf.addTitle(stem + "(" + areaName + ")", document);
        } else {
            pdf.addTitle(stem, document);
        }
        if (!b) {
            pdf.addBlank(10, document);
            pdf.addLittleTitle("考试时间：" + limitTime / 60 + "分钟", document);
        }
        pdf.addBlank(10, document);
        pdf.addLittleTitle("考生用时：" + time / 60 + "分钟" + time % 60 + "秒       考试得分：" + examScore + "分", document);
        pdf.addBlank(20, document);
        pdf.addBaseTitle("【给定资料】", document);
        for (EssayMaterial essayMaterial : essayMaterialList) {
            insertDatum(essayMaterial, pdf, document);
        }
    }

    /**
     * 截取文件路径
     *
     * @param filePath
     * @return
     */
    private String getFileName(String filePath) {
        int i = filePath.lastIndexOf("/");
        String fileName = filePath.substring(i + 1, filePath.length());
        log.info(fileName);
        return fileName;
    }

    public String changeString(String str) {
        if (StringUtils.isNotEmpty(str)) {
            str = str.replaceAll("(&nbsp;){6,8}", PdfUtil.PDF_HEAD_BLANK);
            //切割</br>和</p>
            String text = str.replaceAll("<br/>", "\n").replaceAll("</p>", "\n").replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ");
            text = text.replace("&lt;", "<");
            text = text.replace("&gt;", ">");
            return text;
        } else {
            return str;
        }

    }

    public static String changeStringForUnLine(String str) {
        if (StringUtils.isNotEmpty(str)) {
            //切割</br>和</p>
            str = str.replaceAll("(&nbsp;){6,8}", PdfUtil.PDF_HEAD_BLANK);
            String text = str.replaceAll("<br/>", "\n").replaceAll("</p>", "\n").replaceAll("<u>", "`u`").replaceAll("</u>", "`/u`").replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ");
            text = text.replace("&lt;", "<");
            text = text.replace("&gt;", ">");
            return text;
        } else {
            return str;
        }

    }


    /*
     *生成pdf
     */
    @Override
    public void createPdf() {
        //查询所有可用的试卷
        List<EssayPaperBase> paperList = essayPaperBaseRepository.findByStatusNotOrderByIdAsc(EssayPaperBaseConstant.EssayPaperStatusEnum.DELETED.getStatus());

        for (EssayPaperBase paperBase : paperList) {
            //生成试卷pdf
            getCoverPdfPath(paperBase.getId());
            //生成试题pdf
            List<EssayQuestionBase> essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc
                    (paperBase.getId(), EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            for (EssayQuestionBase essayQuestionBase : essayQuestionBaseList) {
                getSinglePdfPath(essayQuestionBase.getId());
            }
            log.info("试卷及试卷下题目相关pdf处理结束。paperBaseId：" + paperBase.getId());
        }
    }

    @Override
    public void htmlProcess() {
        //处理标签
        List<EssayQuestionDetail> all = essayQuestionDetailRepository.findByStatus(EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
        for (EssayQuestionDetail detail : all) {
            String answerComment = detail.getAnswerComment();
            String answerRequire = detail.getAnswerRequire();
            String analyzeQuestion = detail.getAnalyzeQuestion();
            String answerThink = detail.getAnswerThink();
            String authorityReviews = detail.getAuthorityReviews();
            String correctRule = detail.getCorrectRule();

            detail.setAnswerComment(processHtml(answerComment));
            detail.setAnswerRequire(processHtml(answerRequire));
            detail.setAnalyzeQuestion(processHtml(analyzeQuestion));
            detail.setAnswerThink(processHtml(answerThink));
            detail.setAuthorityReviews(processHtml(authorityReviews));
            detail.setCorrectRule(processHtml(correctRule));

            log.info("================处理完毕，题目detailId 为：{}===============" + detail.getId());
            essayQuestionDetailRepository.save(detail);
        }
    }

    private void changeUnLine(String tempString, PdfUtil pdfUtil, Document document) throws Exception {
        //在该方法内使用同一个paragraph保持所有文字都在同一行内
        Paragraph paragraph = new Paragraph();
        paragraph.setIndentationLeft(5);
        paragraph.setAlignment(Element.ALIGN_LEFT);
        String newString = changeStringForUnLine(tempString);
        Matcher m = pForUnLine.matcher(newString);
        //正则切割
        String[] split = newString.split("`u`(.*?)`/u`");
        paragraph.add(PdfUtil.instantiateChunk(split[0], PdfUtil.CONTENT_FONT));
        int i = 1;
        while (m.find()) {
            paragraph.add(PdfUtil.instantiateChunk(m.group(1), PdfUtil.CONTENT_FONT_UNDERLINE));
            if (split.length >= i) {
                String content = split[i];
                Chunk chunk = PdfUtil.instantiateChunk(content, PdfUtil.CONTENT_FONT);
                paragraph.add(chunk);
                i++;
            }
        }
        //添加文件
        document.add(paragraph);
    }


    private String processHtml(String html) {
        if (StringUtils.isNotEmpty(html) && html.endsWith("&nbsp;</p>")) {
            log.info("修正前的标准答案" + html);
            String regEx1 = "(&nbsp;){2,}</p>";//识别连续多个&nbsp;出现在段落结尾的情况
            html = html.replaceAll(regEx1, "</p>");
            log.info("修正后的标准答案111" + html);
            String regEx2 = "(<br/>){1,}</p>";//识别连续多个&nbsp;出现在段落结尾的情况
            html = html.replaceAll(regEx2, "</p>");
            log.info("修正后的标准答案222" + html);
            String regEx3 = "<p></p>";//识别连续多个&nbsp;出现在段落结尾的情况
            html = html.replaceAll(regEx3, "");
        }
        return html;
    }


    @Override
    public void getPdfBatch() {

        //批量刷试卷
        //查询所有可用试卷
        List<EssayPaperBase> paperList = essayPaperBaseRepository.findByStatusAndBizStatusAndType(4, 1, AdminPaperConstant.TRUE_PAPER);
        LinkedList<Long> errorList = new LinkedList<>();

        //遍历试卷
        for (EssayPaperBase paper : paperList) {
            long paperId = paper.getId();

            try {
                log.info("试题pdf生成开始：{}", paperId);
                //生成试卷pdf
                getCoverPdfPath(paperId);
                log.info("试题pdf生成成功：{}", paperId);
            } catch (Exception e) {
                errorList.add(paperId);
            }
            log.info("试卷pdf生成成功：{}", paper.getId());
        }
        log.info(errorList.toString());
        /*List<EssayQuestionBase> questionBases = essayQuestionBaseRepository.findByStatus(1);
        LinkedList<Long> errorList = new LinkedList<>();

        for(EssayQuestionBase base:questionBases){
            try{
                log.info("试题pdf生成开始：{}", base.getId());
                getSinglePdfPath(base.getId());
                log.info("试题pdf生成成功：{}", base.getId());
            }catch(Exception e){
                errorList.add(base.getId());
            }
        }

        log.info(errorList.toString());*/
    }


    /**
     * 添加批改得分
     *
     * @param essayQuestionAnswer
     * @param pdf
     * @param document
     * @throws Exception
     */
    public void insertManualCorrectScore(String answerRequire, EssayQuestionAnswer essayQuestionAnswer, PdfUtil pdf, Document document) throws Exception {
        if (StringUtils.isNotEmpty(answerRequire)) {
            pdf.addBlank(10, document);
            pdf.addBaseTitle("【作答要求】", document);
            pdf.addBaseContent(changeString(answerRequire), document);
        }
        pdf.addBlank(10, document);
        pdf.addBaseTitle("【批改得分】", document);
        int spendMinute = essayQuestionAnswer.getSpendTime() / 60;
        int spendSecond = essayQuestionAnswer.getSpendTime() % 60;
        pdf.addBaseContent(PdfUtil.PDF_HEAD_BLANK + "得分：" + essayQuestionAnswer.getExamScore() + "分/" + essayQuestionAnswer.getScore() + "分   用时 : " + spendMinute + "分" + spendSecond + "秒" + "   字数 :" + essayQuestionAnswer.getInputWordNum() + "字", document);
        List<TagPosition> tagPositions = Lists.newArrayList();
        //如果是用户人工批改，展示图片及批注信息，不展示文字，如果是智能批改，展示文字内容，如果是智能批改，则展示文字内容和批注信息
        if (null != essayQuestionAnswer.getCorrectMode() && essayQuestionAnswer.getCorrectMode() == CorrectModeEnum.MANUAL.getMode()) {
            //1.获取批改图片
            long questionAnswerId = essayQuestionAnswer.getId();
            List<CorrectImage> correctImageList = essayCorrectImageRepository.findByQuestionAnswerIdAndStatusOrderBySort(questionAnswerId, EssayStatusEnum.NORMAL.getCode());

            //2.解析correctContented (需要更新 title)
            pdf.addBlank(10, document);
            pdf.addBaseTitle(DownloadElementEnum.USER_ANSWER.getValue(), document);
            if (CollectionUtils.isNotEmpty(correctImageList)) {
                for (CorrectImage correctImage : correctImageList) {
                    String finalUrl = correctImage.getFinalUrl();
                    String imageTail = finalUrl.substring(finalUrl.lastIndexOf("."));
                    addImage(finalUrl, imageTail, document, pdf);
                }
            }
            String correctedContent = essayQuestionAnswer.getCorrectedContent();
            if (StringUtils.isNotEmpty(correctedContent)) {
                //获取详细批注信息
                tagPositions.addAll(pdf.correctManual(correctedContent, document, false));
            }
        } else {          //文字批注内容解析，及详细批注信息
            if (StringUtils.isNotEmpty(essayQuestionAnswer.getContent())) {
                pdf.addBaseTitle(DownloadElementEnum.USER_ANSWER.getValue(), document);
                String correctedContent = essayQuestionAnswer.getCorrectedContent();
                if (StringUtils.isNotEmpty(correctedContent)) {
                    pdf.addBlank(5, document);
                    //写入用户答题内容并获得详细批注信息
                    tagPositions.addAll(pdf.correctManual(correctedContent, document, true));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(tagPositions)) {
            pdf.addBlank(5, document);
            for (TagPosition tagPosition : tagPositions) {
                StringBuilder sb = new StringBuilder();
                sb.append(tagPosition.getTagName()).append(".").append(tagPosition.getDescription());
                if (tagPosition.getScore() > 0) {
                    sb.append("  ").append(tagPosition.getScore()>0?"+":"")
                            .append(tagPosition.getScore()).append("分");
                }
                pdf.addBaseContent(PdfUtil.PDF_HEAD_BLANK + sb.toString(),
                        document);
            }
        }
        //3.本题阅卷
        //本题阅卷批注 && 扣分项
        String correctRemark = essayQuestionAnswer.getCorrectRemark();
        if (StringUtils.isNotEmpty(correctRemark)) {
            RemarkListVo remarkListVo = JsonUtil.toObject(correctRemark, RemarkListVo.class);
            List<RemarkVo> questionRemarkList = remarkListVo.getQuestionRemarkList();
            List<String> questionRemarkStrList = getRemarkStrList(questionRemarkList);
            // 本题阅卷
            if (CollectionUtils.isNotEmpty(questionRemarkStrList)) {
                pdf.addBlank(10, document);
                pdf.addBaseTitle(DownloadElementEnum.QUESTION_REMARK.getValue(), document);
                for (String remarkContent : questionRemarkStrList) {
                    pdf.addBaseContent(PdfUtil.PDF_HEAD_BLANK + remarkContent, document);
                }

            }
            //扣分项
            List<RemarkVo> deRemarkList = remarkListVo.getDeRemarkList();
            List<String> delRemarkStrList = getRemarkStrList(deRemarkList);
            if (CollectionUtils.isNotEmpty(delRemarkStrList)) {
                pdf.addBlank(10, document);
                pdf.addBaseTitle(DownloadElementEnum.QUESTION_DE_REMARK.getValue(), document);
                for (String remarkContent : delRemarkStrList) {
                    pdf.addBaseContent(PdfUtil.PDF_HEAD_BLANK + remarkContent, document);
                }
            }
        }
    }

    /**
     * 试卷综合评价
     *
     * @param essayPaperAnswer
     * @param pdf
     * @param document
     * @throws Exception
     */
    public void getPaperRemark(EssayPaperAnswer essayPaperAnswer, PdfUtil pdf, Document document) throws Exception {
        if (null == essayPaperAnswer || StringUtils.isEmpty(essayPaperAnswer.getCorrectRemark())) {
            return;
        }

        RemarkListVo remarkListVo = JsonUtil.toObject(essayPaperAnswer.getCorrectRemark(), RemarkListVo.class);
        if (null != remarkListVo || CollectionUtils.isEmpty(remarkListVo.getPaperRemarkList())) {
            return;
        }

        List<String> remarkStrList = getRemarkStrList(remarkListVo.getPaperRemarkList());
        if (CollectionUtils.isNotEmpty(remarkStrList)) {
            pdf.addBlank(10, document);
            pdf.addBaseTitle(DownloadElementEnum.PAPER_REMARK.getValue(), document);
            //TODo
            for (String remarkContent : remarkStrList) {
                pdf.addBaseContent(PdfUtil.PDF_HEAD_BLANK + remarkContent, document);
            }

        }
    }

    public List<String> getRemarkStrList(List<RemarkVo> remarkVoList) {
        if (CollectionUtils.isNotEmpty(remarkVoList)) {
            List<String> questionRemarkStrList = new ArrayList<>();
            for (RemarkVo remarkVo : remarkVoList) {
                String content = remarkVo.getContent();
                int sort = remarkVo.getSort();
                StringBuffer remarkStr = new StringBuffer();
                remarkStr.append(sort).append(".").append(content);
                if (null != remarkVo.getScore() && remarkVo.getScore().floatValue() != 0f) {
                    remarkStr.append(" ")
                            .append(remarkVo.getScore() > 0 ? "+" : "")
                            .append(remarkVo.getScore()).append("分");
                }
                String result = changeString(remarkStr.toString());
                questionRemarkStrList.add(result);
            }
            return questionRemarkStrList;
        }
        return Lists.newArrayList();
    }


    /**
     * 添加资料内容
     *
     * @param pdf
     * @param document
     * @throws Exception
     */
    private void insertImage(String content, PdfUtil pdf, Document document) throws Exception {

        //切割<img标签
        //String[] imgSpl = content.split("<img[^>]+>");
        // if (imgSpl.length >= 1) {
        log.debug("----------------开始添加图片-------------");
        Matcher m = p.matcher(content);
        pdf.addBlank(5, document);
        //添加内容如果有下划线则会添加下划线
        // changeUnLine(imgSpl[0], pdf, document);
        int i = 1;
        while (m.find()) {
            String lol = m.group();
            Matcher m1 = p1.matcher(lol);
            if (m1.find()) {
                log.debug("---添加临时图片---");
                //取出url
                String url = m1.group().substring(1, m1.group().length() - 1);
                //取出尾缀
                String imageTail = url.substring(url.length() - 4, url.length());
                //拼接文件地址
                String imageName = pdfPicture + UUID.randomUUID().toString().replaceAll("-", "") + imageTail;
                //下载图片
                pdf.downloadPicture(url, imageName);
                //读取一个图片
                Image image = Image.getInstance(imageName);
                image.scaleToFit(480, 800);
                //插入一个图片
                document.add(image);
                //changeUnLine(imgSpl[i], pdf, document);
                log.debug("---添加图片结束");
                File file1 = new File(imageName);
                file1.delete();
                log.debug("---临时文件已经删除");
            }
            i++;
        }
     /*} else {
            pdf.addBaseContent(changeString(content), document);
        }*/
    }


}