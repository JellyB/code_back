package com.huatu.tiku.essay.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.CommonErrors;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.PaperReportRedisKeyConstant;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.edu.EssayEduConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.AdminPaperConstant;
import com.huatu.tiku.essay.constant.status.AnswerSaveTypeConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayPaperBaseConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.constant.status.QuestionTypeConstant;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.BaseInfo;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayPaperBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionDetailRepository;
import com.huatu.tiku.essay.repository.EssayStandardAnswerRepository;
import com.huatu.tiku.essay.repository.EssayUserAnswerQuestionDetailedScoreRepository;
import com.huatu.tiku.essay.service.EssayEduService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.util.sign.OCRSign;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.vo.edu.EssayEduPaperVO;
import com.huatu.tiku.essay.vo.resp.EssayAnswerVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.PaperCommitAnswerVO;
import com.huatu.tiku.essay.vo.resp.PaperCommitVO;
import com.huatu.tiku.essay.vo.resp.ScoreVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
public class EssayEduServiceImpl implements EssayEduService {

    @Value("${OCRAppID}")
    private long OCRAppID;
    @Value("${OCRSecretID}")
    private String OCRSecretID;
    @Value("${OCRSecretKey}")
    private String OCRSecretKey;
    @Value("${eduAnswerCorrectUrl}")
    private String eduAnswerCorrectUrl;
    @Value("${user-web-server}")
    private String url;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    EssayStandardAnswerRepository essayStandardAnswerRepository;
    @Autowired
    private EssayUserAnswerQuestionDetailedScoreRepository essayUserAnswerQuestionDetailedScoreRepository;
    @Autowired
    private BjyHandler bjyHandler;


    /**
     * 根据地区id查询试卷信息（分页）
     *
     * @param areaId
     * @param userId
     * @param pageable
     * @param modeTypeEnum
     * @return
     * @throws BizException
     */
    @Override
    public List<EssayEduPaperVO> findPaperListByArea(long areaId, int userId, Pageable pageable, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) throws BizException {

        List<EssayEduPaperVO> paperList = getPaperList(areaId, pageable,modeTypeEnum);
        packUserInfo(paperList, userId,modeTypeEnum);
        return paperList;
    }

    @Override
    public List<EssayEduPaperVO> findPaperAllListByArea(long areaId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) throws BizException {

        List<EssayEduPaperVO> paperList = getPaperAllList(areaId,modeTypeEnum);
        packUserInfo(paperList, userId, modeTypeEnum);
        return paperList;
    }

    /**
     * 查询用户批改列表（分页）
     *
     * @param userId
     * @param pageRequest
     * @return
     */
    @Override
    public List<EssayAnswerVO> paperCorrectList(int userId, Pageable pageRequest,EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        //查询已交卷记录
        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByUserIdAndStatusAndAnswerCardTypeAndTypeAndBizStatusIn
                (userId,
                        EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        modeTypeEnum.getType(),
                        AdminPaperConstant.TRUE_PAPER,
                        bizStatusList,
                        pageRequest);
        //组装信息
        List<EssayAnswerVO> list = new LinkedList<EssayAnswerVO>();
        for (EssayPaperAnswer paperAnswer : paperAnswerList) {
            EssayAnswerVO answerVO = EssayAnswerVO.builder()
                    .paperName(paperAnswer.getName())//试卷名称
                    .paperId(paperAnswer.getPaperBaseId())//base试卷id
                    .correctDate(DateUtil.convertDateFormat(paperAnswer.getCorrectDate()))//批改时间
                    .examScore(paperAnswer.getExamScore())//学员得分
                    .score(paperAnswer.getScore())//试卷总分
                    .answerId(paperAnswer.getId())
                    .bizStatus(paperAnswer.getBizStatus())//答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
                    .paperReportFlag(true)
                    .build();

            //所属地区
            EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperAnswer.getPaperBaseId());
            if (essayPaperBase != null) {
                answerVO.setAreaId(essayPaperBase.getAreaId());//地区id
                answerVO.setAreaName(essayPaperBase.getAreaName());//地区名称
            }
            answerVO.setVideoAnalyzeFlag(essayPaperBase.getVideoAnalyzeFlag());
            list.add(answerVO);
        }
        return list;
    }

    /**
     * 查询用户批改数量
     *
     * @param userId
     * @param modeTypeEnum
     * @return
     */
    @Override
    public long countPaperCorrectList(int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        return essayPaperAnswerRepository.countByUserIdAndStatusAndAnswerCardTypeAndBizStatus
                (userId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        modeTypeEnum.getType(),
                        EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
    }

    /**
     * 创建答题卡
     *
     * @param userId
     * @param paperBaseId
     * @param terminal
     * @param modeTypeEnum
     * @return
     */
    @Override
    public Long createAnswerCard(int userId, long paperBaseId, int terminal, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        //判断用户是否该试卷已存在答题卡（未完成）
        List<EssayPaperAnswer> unfinishedList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndTypeAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc
                (userId, paperBaseId, AdminPaperConstant.TRUE_PAPER, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(),
                        modeTypeEnum.getType());

        if (CollectionUtils.isNotEmpty(unfinishedList)) {
            return unfinishedList.get(0).getId();
        }
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperBaseId);
        if (null == paperBase) {
            log.info("答题卡创建失败,试卷信息获取为空 ==>>,userId = {},terminal => {},paperBaseId => {}", userId, terminal, paperBaseId);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }

        List<EssayQuestionBase> essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatus
                (paperBaseId, new Sort(Sort.Direction.ASC, "sort"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

        //试卷答题卡paperAnswer
        EssayPaperAnswer essayPaperAnswer = EssayPaperAnswer.builder()
                .paperBaseId(paperBaseId)
                .areaId(paperBase.getAreaId())
                .areaName(paperBase.getAreaName())
                .name(paperBase.getName())
                .score(paperBase.getScore())
                .userId(userId)
                .unfinishedCount(essayQuestionBaseList.size())
                .type(AdminPaperConstant.TRUE_PAPER)//真题
                .build();

        essayPaperAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
        essayPaperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
        essayPaperAnswer.setCreator(userId + "");
        essayPaperAnswer = essayPaperAnswerRepository.save(essayPaperAnswer);
        long paperAnswerId = essayPaperAnswer.getId();

        //批量创建试题答题卡
        List<EssayQuestionAnswer> essayQuestionAnswers = new LinkedList<>();
        essayQuestionBaseList.forEach(questionBase -> {
            long questionDetailId = questionBase.getDetailId();//题目的detailId
            EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionDetailId);
            EssayQuestionAnswer essayQuestionAnswer = EssayQuestionAnswer.builder()
                    .userId(userId)
                    .terminal(terminal)
                    .questionType(questionDetail.getType())
                    .areaId(questionBase.getAreaId())
                    .areaName(questionBase.getAreaName())
                    .questionBaseId(questionBase.getId())
                    .questionYear(questionBase.getQuestionYear())
                    .questionDetailId(questionDetailId)
                    .score(questionDetail.getScore())
                    .paperId(paperBaseId)
                    .labelStatus(0)
                    .subScore(-1D)
                    .paperAnswerId(paperAnswerId)
                    .correctType(questionDetail.getCorrectType())
                    .subScoreRatio(0D)
                    .build();
            essayQuestionAnswer.setCreator(userId + "");
            essayQuestionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
            essayQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());

            essayQuestionAnswers.add(essayQuestionAnswer);
        });
        essayQuestionAnswerRepository.save(essayQuestionAnswers);

        return paperAnswerId;
    }

    /**
     * 试卷保存和交卷
     *
     * @param userId
     * @param paperCommitVO
     * @param terminal
     * @param cv
     * @return
     */
    @Override
    public boolean paperCommit(int userId, PaperCommitVO paperCommitVO, int terminal, String cv) {
        Integer examType = paperCommitVO.getExamType();
        if (null == examType) {
            examType = AdminPaperConstant.TRUE_PAPER;
        }
        Long paperAnswerCardId = paperCommitVO.getAnswerCardId();
        EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(paperAnswerCardId);
        if (null == paperAnswer) {
            log.warn("答题卡ID 有误 {}", paperAnswerCardId);
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        //已交卷,return
        if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus() ||
                paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
            return true;
        }
        int saveType = paperCommitVO.getSaveType();//操作类型(0 保存  1交卷)
        Integer totalSpendTime = paperCommitVO.getAnswerList().stream()
                .mapToInt(answer -> answer.getSpendTime() == null ? 0 : answer.getSpendTime()).sum();
        paperCommitVO.setSpendTime(totalSpendTime);//花费时间

        //校验试卷
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperAnswer.getPaperBaseId());
        if (null == paperBase) {
            log.warn("试卷id有误 {}", paperBase);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }

        if (null != paperCommitVO.getUnfinishedCount()) {
            paperAnswer.setUnfinishedCount(paperCommitVO.getUnfinishedCount());
        }
        if (null != paperCommitVO.getLastIndex()) {
            paperAnswer.setLastIndex(paperCommitVO.getLastIndex());
        }
        paperAnswer.setSpendTime(totalSpendTime);

        //更新业务状态
        if (AnswerSaveTypeConstant.SAVE == saveType) {
            paperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
        } else if (AnswerSaveTypeConstant.COMMIT == saveType) {
            paperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        } else if (AnswerSaveTypeConstant.UPDATE_TIME == saveType) {
            paperAnswer.setSpendTime(totalSpendTime);
        } else {
            log.info("答题卡保存类型有误 saveType {}:" + saveType);
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        //处理QuestionAnswer
        int speed = dealQuestionAnswer(paperCommitVO, paperAnswer, saveType, userId, terminal);

        paperAnswer.setSpeed(speed);
        essayPaperAnswerRepository.saveAndFlush(paperAnswer);

        long paperAnswerId = paperAnswer.getId();
        paperAnswer = new EssayPaperAnswer();
        paperAnswer.setId(paperAnswerId);
        if (AnswerSaveTypeConstant.COMMIT == saveType && AdminPaperConstant.MOCK_PAPER != examType) {
            try {
                String url = eduAnswerCorrectUrl + "?answerCardId=" + paperAnswerCardId + "&type=" + QuestionTypeConstant.PAPER;
                long start = System.currentTimeMillis();
                ResponseMsg body = restTemplate.postForObject(url, null, ResponseMsg.class);
                if (null != body) {
                    long end = System.currentTimeMillis();
                    log.info("请求用时：" + (end - start) + "毫秒");
                    if (1000000 == body.getCode()) {
                        if (null != body.getData()) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("批改试卷服务异常 {}", e.getMessage());
                e.printStackTrace();
            }

        }
        return true;
    }

    /**
     * 查询批改详情
     *
     * @param userId
     * @param answerId
     * @param terminal
     * @param cv
     * @return
     */
    @Override
    public List<EssayQuestionVO> answerDetail(int userId, long answerId, int terminal, String cv) {

        LinkedList<EssayQuestionVO> essayQuestionVOList;

        //根据试卷答题卡查询试题答题卡
        String userPaperAnswerKey = RedisKeyConstant.getUserPaperAnswerKey(answerId);
        essayQuestionVOList = (LinkedList<EssayQuestionVO>) redisTemplate.opsForValue().get(userPaperAnswerKey);
        if (CollectionUtils.isNotEmpty(essayQuestionVOList)) {
            return essayQuestionVOList;
        }
        essayQuestionVOList = new LinkedList<>();
        EssayPaperAnswer paperAnswer = essayPaperAnswerRepository.findOne(answerId);
        if (null == paperAnswer) {
            log.warn("答题卡id错误");
            throw new BizException(EssayErrors.ANSWER_CARD_ID_ERROR);
        }
        List<EssayQuestionAnswer> answers = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus
                (answerId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "id"));

        if (CollectionUtils.isNotEmpty(answers)) {
            for (EssayQuestionAnswer answer : answers) {
                EssayQuestionVO vo = getQuestionAnswerDetail(answer.getId());
                vo.setTotalExamScore(paperAnswer.getExamScore());
                vo.setTotalSpendTime(paperAnswer.getSpendTime());
                essayQuestionVOList.add(vo);
            }
        }
        if (paperAnswer.getBizStatus() == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()
                && CollectionUtils.isNotEmpty(essayQuestionVOList)) {
            redisTemplate.opsForValue().set(userPaperAnswerKey, essayQuestionVOList, 5, TimeUnit.MINUTES);
        }

        return essayQuestionVOList;
    }

    /**
     * 组装题目批改详情
     *
     * @param answerId
     * @return
     */
    public EssayQuestionVO getQuestionAnswerDetail(long answerId) {
        //根据答题卡id查询答题卡信息
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(answerId);
        EssayQuestionVO vo = null;
        if (null != questionAnswer) {
            //查询对应的base信息（取时限）
            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionAnswer.getQuestionBaseId());
            //根据detailId查询题目详情
            EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionAnswer.getQuestionDetailId());
            // 获取标准答案
            List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                    (questionAnswer.getQuestionDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(standardAnswerList)) {
                log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + questionAnswer.getQuestionDetailId());
                throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
            }
            EssayStandardAnswer essayStandardAnswer = standardAnswerList.get(0);
            double difficultGrade = questionDetail.getDifficultGrade();
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
            }

            vo = EssayQuestionVO.builder()
                    .type(questionDetail.getType())
                    .questionDetailId(questionBase.getDetailId())//试题的detailId
                    .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明
                    .stem(questionDetail.getStem())//题干信息
                    .score(questionDetail.getScore())//题目分数
                    .inputWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数
                    .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                    .limitTime(questionBase.getLimitTime())//答题限时
                    .questionBaseId(questionAnswer.getQuestionBaseId())//题目的baseId
                    .examScore(questionAnswer.getExamScore())//学员得分
                    .spendTime(questionAnswer.getSpendTime())//答题时间
                    .inputWordNum(questionAnswer.getInputWordNum())//答案字数
                    .correctedContent(StringUtils.isNotEmpty(questionAnswer.getCorrectedContent()) ? questionAnswer.getCorrectedContent() : questionAnswer.getContent())//批改后的答案
                    .answerDetails(questionDetail.getAnswerDetails())
                    .answerRange(questionDetail.getAnswerRange())
                    .answerTask(questionDetail.getAnswerTask())
                    .correctRule(questionDetail.getCorrectRule())//规则描述
                    .authorityReviews(questionDetail.getAuthorityReviews())//权威点评
                    .analyzeQuestion(questionDetail.getAnalyzeQuestion())//试题分析
                    .sort(questionBase.getSort())//题目序号
                    .bizStatus(questionAnswer.getBizStatus())//答题状态（0 空白，未开始 1未完成 2 已交卷 3已批改）
                    .topic(essayStandardAnswer.getTopic())
                    .callName(essayStandardAnswer.getCallName())
                    .subTopic(essayStandardAnswer.getSubTopic())
                    .answerList(standardAnswerList)
                    .answerFlag(StringUtils.isNotEmpty(questionDetail.getCorrectRule()) ? 1 : 0)
                    .inscribedDate(essayStandardAnswer.getInscribedDate())
                    .inscribedName(essayStandardAnswer.getInscribedName())
                    .difficultGrade(difficultGradeStr)
                    .videoId(questionBase.getVideoId() == null ? 0 : questionBase.getVideoId())
                    .videoAnalyzeFlag((questionBase.getVideoId() != null && questionBase.getVideoId() > 0) ? true : false)
                    .build();

            if (replaceLabel(questionDetail.getAnswerRequire())) {
                vo.setAnswerRequire(null);
            }

            if (replaceLabel(questionDetail.getCorrectRule())) {
                vo.setCorrectRule(null);
            }

            if (replaceLabel(questionDetail.getAuthorityReviews())) {
                vo.setAuthorityReviews(null);
            }

            if (replaceLabel(questionDetail.getAnalyzeQuestion())) {
                vo.setAnalyzeQuestion(null);
            }

            if (replaceLabel(questionDetail.getAnswerDetails())) {
                vo.setAnswerDetails(null);
            }
            if (replaceLabel(questionDetail.getAnswerRange())) {
                vo.setAnswerRange(null);
            }
            if (replaceLabel(questionDetail.getAnswerTask())) {
                vo.setAnswerTask(null);
            }

            //查询得分点
            LinkedList<ScoreVO> addScoreList = new LinkedList<>();
            LinkedList<ScoreVO> subScoreList = new LinkedList<>();
            LinkedList<ScoreVO> elseScoreList = new LinkedList<>();
            List<EssayUserAnswerQuestionDetailedScore> detailedScores = essayUserAnswerQuestionDetailedScoreRepository.findByQuestionAnswerIdAndStatusOrderBySequenceNumberAsc(answerId, 1);
            if (CollectionUtils.isNotEmpty(detailedScores)) {
                //1为内容得分，2为格式得分，3为减分
                for (EssayUserAnswerQuestionDetailedScore detailedScore : detailedScores) {
                    ScoreVO scoreVO = new ScoreVO();
                    BeanUtils.copyProperties(detailedScore, scoreVO);
                    if (1 == detailedScore.getType()) {
                        addScoreList.add(scoreVO);
                    } else if (3 == detailedScore.getType()) {
                        subScoreList.add(scoreVO);
                    } else {
                        elseScoreList.add(scoreVO);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(elseScoreList)) {
                addScoreList.addAll(elseScoreList);
            }
            vo.setAddScoreList(addScoreList);
            vo.setSubScoreList(subScoreList);


        }
        vo.setToken(bjyHandler.getToken(vo.getVideoId()));
        return vo;

    }

    /**
     * 云笔图片识别测试
     *
     * @return
     */
    @Override
    public ModelAndView photo() {

        try {
            String path = "/image";        //要遍历的路径
            File file = new File(path);        //获取其file对象
            File[] fs = file.listFiles();    //遍历path下的文件和目录，放在File数组中
            Map<String, String> result = new HashMap<>();
            for (File f : fs) {                    //遍历File[]数组
                if (!f.isDirectory()) {
                    //若非目录(即文件)，则打印
                    String absolutePath = f.getAbsolutePath();
                    System.out.println(absolutePath);
                    String fileStr = fileToBase64(absolutePath);
                    Map<String, String> hashMap = new LinkedHashMap<String, String>();
                    hashMap.put("appid", OCRAppID + "");
                    //方式一：base64上传
                    hashMap.put("image", fileStr);
                    //方式二：图片地址上传
                    hashMap.put("bucket", "tencentyun");


                    String appSign = OCRSign.appSign(OCRAppID,
                            OCRSecretID,
                            OCRSecretKey,
                            "tencentyun",//（可不填）
                            TimeUnit.DAYS.toMillis(30) / 1000
                    );
                    //拼接头部信息
                    HttpHeaders headers = new HttpHeaders();
                    MediaType contentType = MediaType.parseMediaType("application/json");
                    headers.setContentType(contentType);
                    headers.add("Host", "recognition.image.myqcloud.com");
                    headers.add("Content-Type", "application/json");
                    headers.add("Content-Length", hashMap.size() + "");
                    headers.add("Authorization", appSign);
                    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(hashMap, headers);

                    // String url = "http://recognition.image.myqcloud.com/ocr/generalfast";

                    String url = "https://recognition.image.myqcloud.com/ocr/handwriting";
                    //发送post请求
                    long start = System.currentTimeMillis();

                    ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
                    long end = System.currentTimeMillis();
                    log.info("OCR拍照识别，总用时" + (end - start) + "毫秒");

                    if (null != resp) {
                        String body = resp.getBody();
                        if (StringUtils.isNotEmpty(body)) {
                            Map map = JSON.parseObject(body);
                            String msg = map.get("message").toString();
                            String code = map.get("code").toString();
                            //响应成功，取出其中文字内容
                            if ("OK".equals(msg) && "0".equals(code)) {
                                Map respData = (Map) map.get("data");
                                if (respData.isEmpty()) {
                                }
                                List<Map> items = (List<Map>) respData.get("items");
                                if (CollectionUtils.isNotEmpty(items)) {
                                    StringBuilder stringBuilder = new StringBuilder();
                                    //当前段落
                                    Integer paraNum = 0;
                                    for (Map item : items) {
                                        stringBuilder.append(item.get("itemstring"));
                                        Map parag = (Map) item.get("parag");
                                        String paragNo = parag.get("parag_no").toString();
                                        Integer paraNumTemp = Integer.parseInt(paragNo);
                                        if (paraNumTemp > paraNum) {
                                            stringBuilder.append("$$$");
                                            paraNum = paraNumTemp;
                                        }
                                        result.put(absolutePath, stringBuilder.toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Set<String> keySet = result.keySet();

            for (String key : keySet) {
                log.info(key + "====" + result.get(key));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //字段替换标签后是否为空
    private static boolean replaceLabel(String sourceStr) {
        String regEx1 = "<br>";//识别所有<br><br/>的标签
        String regEx2 = "<br/>";
        String regEx3 = "<p>";
        String regEx4 = "&nbsp;";
        String regEx5 = "</p>";
        if (StringUtils.isNotEmpty(sourceStr)) {
            sourceStr = sourceStr.replaceAll(regEx1, "")
                    .replaceAll(regEx2, "")
                    .replaceAll(regEx3, "")
                    .replaceAll(regEx4, "")
                    .replaceAll(regEx5, "");
            return StringUtils.isEmpty(sourceStr);
        } else {
            return true;
        }

    }

    /**
     * 处理QuestionAnswer，并返回答题速度
     *
     * @param paperCommitVO
     * @param paperAnswer
     * @param saveType
     * @param userId
     * @param terminal
     * @return
     */
    private int dealQuestionAnswer(PaperCommitVO paperCommitVO, EssayPaperAnswer paperAnswer, int saveType, int userId, int terminal) {
        long paperAnswerId = paperAnswer.getId();
        List<EssayQuestionAnswer> essayQuestionAnswers = new LinkedList<>();
        List<EssayQuestionAnswer> existQuestionAnswers = essayQuestionAnswerRepository.findByPaperAnswerIdAndStatus
                (paperAnswerId, EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "id"));

        int answeredQuestionCount = 0;//已作答题目数
        List<PaperCommitAnswerVO> answerList = paperCommitVO.getAnswerList();
        if (CollectionUtils.isNotEmpty(answerList)) {
            for (PaperCommitAnswerVO commitAnswerVO : answerList) {
                Long questionBaseId = commitAnswerVO.getQuestionBaseId();
                EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
                EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(commitAnswerVO.getQuestionDetailId());

                EssayQuestionAnswer questionAnswer = EssayQuestionAnswer.builder()
                        .areaName(paperAnswer.getAreaName())//地区名称
                        .areaId(paperAnswer.getAreaId())//地区id
                        .questionYear(questionBase.getQuestionYear())//试题年份
                        .score(questionDetail.getScore())//试题分数
                        .content(commitAnswerVO.getContent())//学员作答答案
                        .userId(userId)//用户id
                        .questionBaseId(questionBaseId)//题目baseId
                        .questionDetailId(commitAnswerVO.getQuestionDetailId())//题目detailId
                        .terminal(terminal)//答题终端
                        .paperId(paperAnswer.getPaperBaseId())//试卷baseId
                        .paperAnswerId(paperAnswerId)//试卷答题卡id
                        .spendTime(commitAnswerVO.getSpendTime())
                        .questionType(questionDetail.getType())
                        .inputWordNum(commitAnswerVO.getInputWordNum())
                        .labelStatus(0)
                        .subScore(-1D)
                        .correctType(questionDetail.getCorrectType())
                        .subScoreRatio(0D)
                        .build();

                questionAnswer.setStatus(EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus());
                if (0 != commitAnswerVO.getSpendTime()) {
                    answeredQuestionCount += 1;
                }
                if (null != commitAnswerVO.getAnswerId()) {
                    questionAnswer.setId(commitAnswerVO.getAnswerId());//试题答题卡id
                } else {
                    Optional<EssayQuestionAnswer> any = existQuestionAnswers.stream().filter(existQuestionAnswer -> existQuestionAnswer.getQuestionBaseId() == questionBaseId).findAny();
                    if (any.isPresent()) {
                        questionAnswer.setId(any.get().getId());
                    }
                }
                if (AnswerSaveTypeConstant.SAVE == saveType) {
                    questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus());
                } else {
                    questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
                }
                essayQuestionAnswers.add(questionAnswer);
            }
        }

        int speed = 0;
        if (0 != answeredQuestionCount) {
            speed = paperAnswer.getSpendTime() / answeredQuestionCount;
        }

        //更新questionAnswer
        for (EssayQuestionAnswer questionAnswer : essayQuestionAnswers) {
            questionAnswer.setSpeed(speed);
        }
        essayQuestionAnswerRepository.save(essayQuestionAnswers);

        return speed;
    }

    /**
     * 根据试卷包装用户信息
     *  @param paperList
     * @param userId
     * @param modeTypeEnum
     */
    public void packUserInfo(List<EssayEduPaperVO> paperList, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        //封装用户答题信息
        List<Long> paperIds = paperList.stream().map(EssayEduPaperVO::getPaperId).collect(Collectors.toList());
        List<EssayPaperAnswer> essayPaperAnswers = essayPaperAnswerRepository.findByUserIdAndStatusAndPaperBaseIdInAndAnswerCardType(userId,
                EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(), paperIds, modeTypeEnum.getType());
        Map<Long, List<EssayPaperAnswer>> paperAnswerMap = Maps.newHashMap();

        for (EssayPaperAnswer essayPaperAnswer : essayPaperAnswers) {
            long paperId = essayPaperAnswer.getPaperBaseId();
            if (paperAnswerMap.get(paperId) == null) {
                List<EssayPaperAnswer> subPaperAnswers = Lists.newArrayList();
                subPaperAnswers.add(essayPaperAnswer);
                paperAnswerMap.put(paperId, subPaperAnswers);
            } else {
                List<EssayPaperAnswer> subPaperAnswers = paperAnswerMap.get(paperId);
                subPaperAnswers.add(essayPaperAnswer);
            }
        }
        paperList.forEach(paper -> getUserPaperAnswerStatus(paperAnswerMap.get(paper.getPaperId()), paper));
    }

    /**
     * 根据用户做过的该试卷的历史答题卡确定试卷最近的答题卡，和批改状态
     *
     * @param answers
     * @param paper
     */
    private void getUserPaperAnswerStatus(List<EssayPaperAnswer> answers, EssayEduPaperVO paper) {
        int times = 0;
        int commitTimes = 0;
        EssayPaperAnswer last = null;
        if (CollectionUtils.isNotEmpty(answers)) {

            for (EssayPaperAnswer answer : answers) {
                if (EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus() == answer.getBizStatus()) {
                    times++;
                    commitTimes++;
                }
                if (EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus() == answer.getBizStatus()) {
                    // 交卷未批改次数+已批改总数
                    commitTimes++;
                }
                if (last == null || last.getGmtModify().compareTo(answer.getGmtModify()) < 0) {
                    last = answer;
                }
            }
            paper.setTimes(commitTimes);
        }

        paper.setCorrectNum(times);
        if (last != null) {
            paper.setAnswerCardId(last.getId());
            paper.setRecentStatus(last.getBizStatus());
        } else {
            paper.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }
    }

    /**
     * 根据地区查询用户试卷列表（分页）
     *
     * @param areaId
     * @param pageable
     * @param modeTypeEnum
     * @return
     */
    private List<EssayEduPaperVO> getPaperList(long areaId, Pageable pageable, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        String eduPaperListKey = EssayEduConstant.getEduPaperList(areaId, pageable.getPageNumber(), pageable.getPageSize());
        Object paperBaseListCache = redisTemplate.opsForValue().get(eduPaperListKey);
        List<EssayEduPaperVO> essayEduPaperVOS = new LinkedList<>();
        if (null != paperBaseListCache) {
            essayEduPaperVOS = (List<EssayEduPaperVO>) paperBaseListCache;
        } else {
            List<EssayPaperBase> paperBaseList = essayPaperBaseRepository.findByAreaIdAndBizStatusAndStatus
                    (areaId, EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(), EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus());
            for (EssayPaperBase paperBase : paperBaseList) {
                EssayEduPaperVO eduPaperVO = EssayEduPaperVO.builder()
                        .paperId(paperBase.getId())
                        .areaId(paperBase.getAreaId())
                        .limitTime(paperBase.getLimitTime())
                        .score(paperBase.getScore())
                        .name(paperBase.getName())
                        .totalCount(getQuestionCount(paperBase.getId()))
                        .correctSum(getQuestionCorrectSum(paperBase.getId(),modeTypeEnum))
                        .modifyTime(paperBase.getGmtModify() == null ? paperBase.getGmtCreate().getTime() : paperBase.getGmtModify().getTime())
                        .status(paperBase.getBizStatus() == EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() ? 2 : 1)
                        .build();

                essayEduPaperVOS.add(eduPaperVO);
            }
            if (CollectionUtils.isNotEmpty(essayEduPaperVOS)) {
                redisTemplate.opsForValue().set(eduPaperListKey, essayEduPaperVOS, 5, TimeUnit.MINUTES);
            }
        }

        return essayEduPaperVOS;

    }

    /**
     * 根据地区查询用户试卷列表（无分页）
     *
     * @param areaId
     * @param modeTypeEnum
     * @return
     */
    private List<EssayEduPaperVO> getPaperAllList(long areaId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        String eduPaperListKey = EssayEduConstant.getEduPaperList(areaId);
        Object paperBaseListCache = redisTemplate.opsForValue().get(eduPaperListKey);
        List<EssayEduPaperVO> essayEduPaperVOS = new LinkedList<>();
        if (null != paperBaseListCache) {
            essayEduPaperVOS = (List<EssayEduPaperVO>) paperBaseListCache;
        } else {
            List<EssayPaperBase> paperBaseList = essayPaperBaseRepository.findByAreaIdAndBizStatusAndStatus
                    (areaId, EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(), EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus());
            for (EssayPaperBase paperBase : paperBaseList) {
                EssayEduPaperVO eduPaperVO = EssayEduPaperVO.builder()
                        .paperId(paperBase.getId())
                        .areaId(paperBase.getAreaId())
                        .limitTime(paperBase.getLimitTime())
                        .score(paperBase.getScore())
                        .name(paperBase.getName())
                        .totalCount(getQuestionCount(paperBase.getId()))
                        .correctSum(getQuestionCorrectSum(paperBase.getId(), modeTypeEnum))
                        .modifyTime(paperBase.getGmtModify() == null ? paperBase.getGmtCreate().getTime() : paperBase.getGmtModify().getTime())
                        .status(paperBase.getBizStatus() == EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() ? 2 : 1)
                        .build();

                essayEduPaperVOS.add(eduPaperVO);
            }
            if (CollectionUtils.isNotEmpty(essayEduPaperVOS)) {
                redisTemplate.opsForValue().set(eduPaperListKey, essayEduPaperVOS, 5, TimeUnit.MINUTES);
            }
        }

        return essayEduPaperVOS;

    }

    /**
     * 获取试卷全站批改数量
     *
     * @param paperBaseId
     * @param modeTypeEnum
     * @return
     */
    private int getQuestionCorrectSum(long paperBaseId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        return essayPaperAnswerRepository.countByPaperBaseIdAndStatusAndAnswerCardType(paperBaseId, EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),modeTypeEnum.getType()).intValue();
    }

    /**
     * 获取试卷题量
     *
     * @param paperBaseId
     * @return
     */
    private int getQuestionCount(long paperBaseId) {
        return (int) essayQuestionBaseRepository.countByPaperIdAndBizStatusAndStatus
                (paperBaseId, EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
    }


    /**
     * 文件转换成base64编码
     *
     * @param path
     * @return
     */
    public static String fileToBase64(String path) {
        String base64 = null;
        InputStream in = null;
        try {
            File file = new File(path);
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64 = Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return base64;
    }

    @Override
    public List<EssayAnswerVO> paperCorrectList(int userId, long paperId,EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        //查询已交卷记录
        LinkedList<Integer> bizStatusList = new LinkedList<>();
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        bizStatusList.add(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
        List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndStatusAndTypeAndBizStatusInAndAnswerCardTypeOrderByGmtModifyDesc
                (userId, paperId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), AdminPaperConstant.TRUE_PAPER, bizStatusList,
                        modeTypeEnum.getType());
        //组装信息
        List<EssayAnswerVO> list = new LinkedList<EssayAnswerVO>();
        for (EssayPaperAnswer paperAnswer : paperAnswerList) {
            EssayAnswerVO answerVO = EssayAnswerVO.builder()
                    .paperName(paperAnswer.getName())//试卷名称
                    .paperId(paperAnswer.getPaperBaseId())//base试卷id
                    .correctDate(DateUtil.convertDateFormat(paperAnswer.getCorrectDate()))//批改时间
                    .examScore(paperAnswer.getExamScore())//学员得分
                    .score(paperAnswer.getScore())//试卷总分
                    .answerId(paperAnswer.getId())
                    .bizStatus(paperAnswer.getBizStatus())//答题状态(0 空白，未开始   1未完成    2 已交卷  3已批改)
                    .paperReportFlag(true)
                    .modifyDate(paperAnswer.getGmtModify())//修改时间
                    .createDate(paperAnswer.getGmtCreate().getTime()) //创建时间
                    .correctDateStr(DateUtil.getFormatDateStyleString(
                            paperAnswer.getCorrectDate() == null ? 0L : paperAnswer.getCorrectDate().getTime()))// 批改时间年月日
                    .build();

            //所属地区
            EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperAnswer.getPaperBaseId());
            if (essayPaperBase != null) {
                answerVO.setAreaId(essayPaperBase.getAreaId());//地区id
                answerVO.setAreaName(essayPaperBase.getAreaName());//地区名称
            }
            answerVO.setVideoAnalyzeFlag(essayPaperBase.getVideoAnalyzeFlag());
            list.add(answerVO);
        }
        return list;

    }

    @Override
    public PageUtil<HashMap> findUserMetas(long paperId, PageRequest pageable) {
        PageUtil p = PageUtil.builder()
                .result(new LinkedList<>())
                .build();
        Page<EssayPaperAnswer> all = essayPaperAnswerRepository.findAll(querySpecific(paperId, EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()), pageable);
        List<EssayPaperAnswer> content = all.getContent();
        if (CollectionUtils.isEmpty(content)) {
            return p;
        }
        List<UserDto> collect = content.parallelStream().map(EssayPaperAnswer::getUserId)
                .map(i -> UserDto.builder().id(i).build()).collect(Collectors.toList());
        List<LinkedHashMap<String, Object>> data = Lists.newArrayList();
        assertUserInfo(collect, data);
        List<HashMap<String, Object>> result = content.stream().map(i -> assemblingMetas(i, data, all.getTotalElements())).collect(Collectors.toList());
        long totalElements = all.getTotalElements();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        p = PageUtil.builder()
                .result(result)
                .next(totalElements > (pageNumber + 1) * pageSize ? 1 : 0)
                .total(totalElements)
                .totalPage((0 == totalElements % pageSize) ? (totalElements / pageSize) : (totalElements / pageSize + 1))
                .build();
        return p;
    }

    private HashMap<String, Object> assemblingMetas(EssayPaperAnswer answer, List<LinkedHashMap<String, Object>> data, long totalElements) {
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("userId", answer.getUserId());
        map.put("score", answer.getScore());
        int rank = -1;
        if (answer.getTotalRank() == 0) {
            String paperReportScoreZsetKey = PaperReportRedisKeyConstant.getPaperReportScoreZsetKey(answer.getPaperBaseId());
            Long reverseRank = redisTemplate.opsForZSet().reverseRank(paperReportScoreZsetKey, answer.getId());
            rank = null == reverseRank ? new Long(totalElements).intValue() : reverseRank.intValue();
        } else {
            rank = answer.getTotalRank();
        }
        map.put("rank", rank);
        map.put("areaId", answer.getAreaId());
        map.put("areaName", answer.getAreaName());
        Optional<String> first = data.stream().filter(i -> MapUtils.getInteger(i, "id", -1).intValue() == answer.getUserId())
                .map(i -> MapUtils.getString(i, "mobile",""))
                .findFirst();
        if (first.isPresent()) {
            map.put("phone", first.get());
        } else {
            map.put("phone", "");
        }
        return map;
    }

    private Specification querySpecific(long paperId, int bizStatus) {
        Specification querySpecific = new Specification<EssayPaperAnswer>() {
            @Override
            public Predicate toPredicate(Root<EssayPaperAnswer> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("status"), EssayStatusEnum.NORMAL.getCode()));
                predicates.add(criteriaBuilder.equal(root.get("type"), 0));

                if (bizStatus > 0) {
                    predicates.add(criteriaBuilder.equal(root.get("bizStatus"), bizStatus));
                }
                if (BaseInfo.isNotDefaultSearchValue(paperId)) {
                    predicates.add(criteriaBuilder.equal(root.get("paperBaseId"), paperId));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }

    private void assertUserInfo(List<UserDto> userDtos, List<LinkedHashMap<String, Object>> data) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        data.addAll(userDtoList.getData());
    }
}
