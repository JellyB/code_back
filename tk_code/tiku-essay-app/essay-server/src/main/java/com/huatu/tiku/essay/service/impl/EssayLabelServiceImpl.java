package com.huatu.tiku.essay.service.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.error.EssayLabelErrors;
import com.huatu.tiku.essay.constant.label.LabelRedisKeyConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.constant.status.EssayMaterialConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayLabelService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.task.AsyncCopyRatioServiceImpl;
import com.huatu.tiku.essay.task.GetNextLabelTask;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.util.file.Label2AppUtil;
import com.huatu.tiku.essay.util.file.LabelXmlUtil;
import com.huatu.tiku.essay.vo.excel.*;
import com.huatu.tiku.essay.vo.resp.EssayLabelCheckVO;
import com.huatu.tiku.essay.vo.resp.EssayLabelInfoVO;
import com.huatu.tiku.essay.vo.resp.LabelSmallVO;
import com.huatu.tiku.essay.vo.resp.QuestionAnswerLabelListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.label.LabelQueryConstant.IS_FINAL;
import static com.huatu.tiku.essay.constant.label.LabelQueryConstant.NOT_FINAL;

/**
 * 申论议论文批注相关
 *
 * @author zhaoxi
 */
@SuppressWarnings("ALL")
@Service
@Slf4j
public class EssayLabelServiceImpl implements EssayLabelService {
    @Autowired
    EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;
    @Autowired
    EssaySimilarQuestionRepository essaySimilarQuestionRepository;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    EssayLabelDetailRepository essayLabelDetailRepository;
    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;
    @Autowired
    private AsyncCopyRatioServiceImpl asyncCopyRatioServiceImpl;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayMaterialRepository essayMaterialRepository;
    @Autowired
    GetNextLabelTask getNextLabelTask;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private LabelXmlUtil labelXmlUtil;
    @Autowired
    EssayDelAnswerRepository essayDelAnswerRepository;

    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";

    /**
     * 多条件查询批注列表
     *
     * @param examScore
     * @param wordNum
     * @param subScoreFlag
     * @param areaId
     * @param year
     * @param labelStatus
     * @param questionId
     * @param stem
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageUtil<List<QuestionAnswerLabelListVO>> findByConditions(long areaId, String year, double examScoreMin, int wordNumMin, double subScoreRatioMin, double examScoreMax, int wordNumMax, double subScoreRatioMax, int labelStatus, long questionId, String stem, long answerId, int page, int pageSize, String admin) {

        List<Long> userGiveUpList = getUserGiveUpList(admin);
        //使用stopwatch停表工具
        Stopwatch stopwatch = Stopwatch.createStarted();
        Page<EssayQuestionAnswer> questionAnswerPage = findQuestionAnswerByConditions(userGiveUpList, areaId, year, examScoreMin, wordNumMin, subScoreRatioMin, examScoreMax, wordNumMax, subScoreRatioMax, labelStatus, questionId, stem, answerId, page, pageSize);
        log.info("分页查询批注，用时" + String.valueOf(stopwatch.stop()));
        if (questionAnswerPage == null) {
            return new PageUtil<>();
        }
        long totalElements = questionAnswerPage.getTotalElements();
        List<EssayQuestionAnswer> questionAnswerList = questionAnswerPage.getContent();

        List<QuestionAnswerLabelListVO> list = new LinkedList<>();
        //将答题卡数据转变成批注列表页结构
        List<Long> answerIdList = questionAnswerList.stream().map(EssayQuestionAnswer::getId).collect(Collectors.toList());
        List<EssayLabelTotal> totalList = essayLabelTotalRepository.findByAnswerIdInAndStatusAndBizStatusIsNotOrderByGmtModifyAsc
                (answerIdList, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(), EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());

        if (CollectionUtils.isNotEmpty(questionAnswerList)) {
            questionAnswerList.forEach(i -> {
                QuestionAnswerLabelListVO labelListVO = answerConvert2LabelVO(i, totalList);
                list.add(labelListVO);
            });
        }
        PageUtil result = PageUtil.builder()
                .result(list)
                .total(questionAnswerPage.getTotalElements())
                .totalPage(questionAnswerPage.getTotalPages())
                .build();
        return result;
    }


    public Page<EssayQuestionAnswer> findQuestionAnswerByConditions(List<Long> giveUpList, long areaId, String year, double examScoreMin, int wordNumMin, double subScoreRatioMin, double examScoreMax, int wordNumMax, double subScoreRatioMax, int labelStatus, long questionId, String stem, long answerId, int page, int pageSize) {
        Pageable pageRequest = null;
        boolean flag = false;
        if (page != -1) {
            pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "correctDate");
        } else {
            pageRequest = new PageRequest(0, pageSize, Sort.Direction.DESC, "labelStatus");
            flag = true;

        }


        //1.先过滤除所有符合条件的答题卡  再做别的查询
        //1.1先确定题目范围
        List questionIdList = new LinkedList<Long>();
        if (StringUtils.isNotEmpty(stem)) {
            List<Long> detailIds = essayQuestionDetailRepository.findIdByTypeAndStem(5, "%" + stem + "%");
            //根据detailId确定baseIds
            if (CollectionUtils.isNotEmpty(detailIds)) {
                questionIdList = essayQuestionBaseRepository.findByDetailIdIn(detailIds);
            } else {
                return null;
            }
            if (CollectionUtils.isEmpty(questionIdList)) {
                return null;
            }

        }
        //1.2 确定答题卡范围
        Specification specification = querySpecific(questionIdList, areaId, year, examScoreMin, wordNumMin, subScoreRatioMin, examScoreMax, wordNumMax, subScoreRatioMax, questionId, labelStatus, answerId, flag, giveUpList);

        Page<EssayQuestionAnswer> questionAnswerPage = essayQuestionAnswerRepository.findAll(specification, pageRequest);
        return questionAnswerPage;

    }


    /**
     * 开始批注（校验是否可以批注&&返回批注的totalId）
     *
     * @param answerId
     * @param admin
     * @return
     */
    @Override
    public Map startLabel(long answerId, String admin, int isFinal) {
        Boolean isVip = isVip(admin);
        if (isVip) {
            isFinal = 1;
        }
        HashMap<String, Long> map = new HashMap<>();

        /**
         *（暂时关闭校验）检查当前用户是否有没批注完的答题卡
         */
        EssayLabelTotal unfinishedLabel = checkUnfinishedLabel(admin);
        if (null != unfinishedLabel) {
            essayLabelTotalRepository.updateToDelById(unfinishedLabel.getId());
        }
        /*
         * 1.判断
         *   1.1 用户是否已经批注过该题目
         *   1.2 根据是终审还是普通批注判断批注是否可进行
         */
        List<EssayLabelTotal> labelTotalList = essayLabelTotalRepository.findByAnswerIdAndStatus
                (answerId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        int onlineCount = 0;
        boolean finalExist = false;
        if (CollectionUtils.isNotEmpty(labelTotalList)) {
            for (EssayLabelTotal total : labelTotalList) {
                if (admin.equals(total.getCreator())) {
                    //已经批注过
                    if (total.getBizStatus() != EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus()) {
                        log.error("您已经批注过此答题卡，不可重复批注。请重新选择批注对象");
                        throw new BizException(EssayLabelErrors.ALREADY_LABEL_THIS);

                    } else {
                        //没有批完且未删除
                        essayLabelTotalRepository.updateToDelById(unfinishedLabel.getId());
                    }
                }

                if (total.getBizStatus() != EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus()) {
                    onlineCount++;
                }
                if (total.getIsFinal() == 1) {
                    finalExist = true;
                }
            }
        }


        /*
         * 判断是否是终审
         */
        if (isFinal == 0) {
            if (labelTotalList.size() >= 2) {
                log.info(labelTotalList.toString());
                log.error("该题批注次数已达最大，不可批注，请刷新页面后重试。answerId：{}", answerId);
                throw new BizException(EssayLabelErrors.ALREADY_MAX_LABEL_TIMES);
            } else if (labelTotalList.size() == 1) {
                String vipLabelTeacherList = LabelRedisKeyConstant.getVIPLabelTeacherList();
                Set<Object> vipLabelTeacher = redisTemplate.opsForSet().members(vipLabelTeacherList);
                if (CollectionUtils.isNotEmpty(vipLabelTeacher) && vipLabelTeacher.contains(labelTotalList.get(0).getCreator())) {
                    log.error("该题已被VIP批注，不可批注，请刷新页面后重试。answerId：{}", answerId);
                    throw new BizException(EssayLabelErrors.VIP_LABELING);
                }
            }
        } else {

            /**
             * vip只能批完全没有批注过的
             */
            if (isVip && labelTotalList.size() != 0) {
                log.error("终审2只能批注未被批注的答题卡，answerId:{}", answerId);
                throw new BizException(EssayLabelErrors.VIP_ONLY_LABEL_EMPTY);
            }
            /**
             * 两次批注已经完成，且尚未开始终审
             * VIP不用校验
             */
            if (!isVip && (finalExist || onlineCount != 2)) {
                log.error("当前题目状态，不可进行终审批注，请刷新页面后重试");
                throw new BizException(EssayLabelErrors.NOT_READY_FOR_FINAL_LABEL);
            }
        }

        /*
         *  2.是否可批注
         *    2.1 不可批注，结束(该题批注次数已达最大or该用户已批改过此题)
         *    2.2 可以批注，创建综合批注
         */
        /*
         * 说明：
         *  创建不一定就可用，只有点了总体批注的完成按钮才算保存成功。
         *  定时器会每隔一分钟扫描，创建超过两个小时，但是没有完成的 LabelCloseTask
         */
        EssayLabelTotal labelTotal = new EssayLabelTotal();
        labelTotal.setCreator(admin);
        labelTotal.setAnswerId(answerId);
        labelTotal.setIsFinal(isFinal);
        EssayQuestionAnswer answer = essayQuestionAnswerRepository.findOne(answerId);
        QuestionAnswerLabelListVO labelListVO = answerConvert2LabelVO(answer, labelTotalList);

        labelTotal.setQuestionId(answer.getQuestionBaseId());
        labelTotal.setLabeledContent(labelListVO.getContent());
        labelTotal.setTitleContent(labelListVO.getTitle());
        labelTotal.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        labelTotal.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());

        labelTotal = essayLabelTotalRepository.save(labelTotal);
        if (labelTotal.getId() <= 0) {
            log.error("插入批注数据异常");
            throw new BizException(EssayLabelErrors.LABEL_INSERT_ERROR);
        }

        map.put("totalId", labelTotal.getId());
        return map;
    }


    private EssayLabelTotal checkUnfinishedLabel(String admin) {
        List<EssayLabelTotal> totalList = essayLabelTotalRepository.findByCreatorAndStatusAndBizStatus
                (admin, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(), EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());
        if (CollectionUtils.isNotEmpty(totalList)) {
            return totalList.get(0);
        } else {
            return null;
        }

    }

    /**
     * 检查标题批注和结构批注是否已经存在
     *
     * @param totalId
     * @return
     */
    @Override
    public EssayLabelCheckVO checkFlag(long totalId) {

        EssayLabelCheckVO vo = new EssayLabelCheckVO();
        //根据totalId查询；标题批注和结构批注是否已经存在
        List<EssayLabelDetail> labelDetailList = essayLabelDetailRepository.findByTotalIdAndStatus
                (totalId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());

        if (CollectionUtils.isNotEmpty(labelDetailList)) {
            for (EssayLabelDetail detail : labelDetailList) {
                if (StringUtils.isNotEmpty(detail.getTitleScore())) {
                    vo.setTitleScore(detail.getTitleScore());
                }

                if (StringUtils.isNotEmpty(detail.getStructScore())) {
                    vo.setStructScore(detail.getStructScore());
                }
            }
        }
        return vo;
    }

    /**
     * 保存总体批注
     * 1.保存总体批注
     * 2.将所有批注更新为上线状态
     *
     * @param total
     * @return
     */
    @Override
    public EssayLabelTotal saveTotalLabel(EssayLabelTotal total, String admin) {

        Long answerId = total.getAnswerId();
        EssayQuestionAnswer answer = essayQuestionAnswerRepository.findOne(answerId);
        //如果保存时超过了两个小时
        EssayLabelTotal labelTotal = essayLabelTotalRepository.findOne(total.getId());
        if (labelTotal.getStatus() == EssayLabelStatusConstant.EssayLabelStatusEnum.DELETED.getStatus()) {
            throw new BizException(EssayLabelErrors.LABEL_DELETED);
        }

        boolean isThanLimitTime = ChronoUnit.SECONDS.between(labelTotal.getGmtCreate().toInstant(), new Date().toInstant()) > 2 * 60 * 60;
        //1.保存总体批注
        total.setQuestionId(answer.getQuestionBaseId());
        total.setGmtModify(new Date());
        total.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        total.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus());
        if (isThanLimitTime && total.getLabelFlag() != LabelFlagEnum.STUDENT_LOOK.getCode()) {
            //删除已经创出建的数据
            total.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.DELETED.getStatus());
        }
        total = essayLabelTotalRepository.save(total);
        if (isThanLimitTime) {
            throw new BizException(EssayLabelErrors.ERROR_THAN_LIMIT_TIME);
        }
        //2.更新答题卡批注状态,分差
        List<EssayLabelTotal> totalList = essayLabelTotalRepository.findByAnswerIdAndStatusAndBizStatusIsNotOrderByGmtModifyAsc
                (answerId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(), EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());

        if (CollectionUtils.isNotEmpty(totalList)) {

            int labelTimes = totalList.size();
            answer.setLabelStatus(labelTimes);

            //如果是VIP批注
            if (isVip(admin)) {
                answer.setLabelStatus(4);
            }
            if (labelTimes == 2) {
                //计算分差
                Double firstScore = totalList.get(0).getTotalScore();
                Double secondScore = totalList.get(1).getTotalScore();

                Double subScore = firstScore - secondScore;
                if (subScore < 0) {
                    subScore = 0 - subScore;
                }
                double score = answer.getScore();
                double subScoreRatio = 0D;

                if (subScore != 0 && score != 0) {
                    subScoreRatio = (double) subScore / score;
                }
                answer.setSubScore(subScore);
                answer.setSubScoreRatio(subScoreRatio);
                if (subScoreRatio > 0.1) {
                    answer.setSubScoreFlag(2);
                } else {
                    answer.setSubScoreFlag(1);
                }
            }
            answer = essayQuestionAnswerRepository.save(answer);
        }

        return total;
    }

    @Override
    public long saveDetailLabel(EssayLabelDetail detail) {
        detail.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        detail.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus());
        List<EssayLabelDetail> detailList = essayLabelDetailRepository.findByTotalIdAndStatus(detail.getTotalId(), EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());

        //保存论据批注
        if (StringUtils.isNotEmpty(detail.getEvidenceScore())) {
            List<EssayLabelDetail> thesisList = getThesisListByList(detailList);
            if (CollectionUtils.isEmpty(thesisList)) {
                log.error("暂无论点批注，论据批注不可保存");
                throw new BizException(EssayLabelErrors.NO_THESIS_YET);
            }
            if (StringUtils.isNotEmpty(detail.getThesisScore())) {
                log.error("论点批注，论据批注不可同时出现，请修改后保存");
                throw new BizException(EssayLabelErrors.THESIS_EVIDENCE_NOT_EXIST_TOGETHER);
            }
            if (!detail.getEvidenceScore().contains("-")) {
                log.error("论据得分必须关联相应论点");
                throw new BizException(EssayLabelErrors.EVIDENCE_SCORE_ERROR);
            }

            //结构批注
        } else if (StringUtils.isNotEmpty(detail.getStructScore())) {
            List<Long> structureListByList = getStructureListByList(detailList);
            if (CollectionUtils.isNotEmpty(structureListByList) && !structureListByList.contains(detail.getId())) {

                log.error("结构批注已存在，不可重复批注");
                throw new BizException(EssayLabelErrors.STRUCTURE_LABEL_EXIST);
            }
            //标题批注
        } else if (StringUtils.isNotEmpty(detail.getTitleScore())) {
            List<Long> titleListByList = getTitleListByList(detailList);
            if (CollectionUtils.isNotEmpty(titleListByList) && !titleListByList.contains(detail.getId())) {
                log.error("标题批注已存在，不可重复批注");
                throw new BizException(EssayLabelErrors.TITLE_LABEL_EXIST);
            }
        }

        detail = essayLabelDetailRepository.save(detail);

        if (detail.getId() <= 0) {
            log.error("插入详细批注数据异常");
            throw new BizException(EssayLabelErrors.LABEL_INSERT_ERROR);
        } else {
            //更新labeledContent
            int updateLabeledContentById = essayLabelTotalRepository.updateLabeledContentById(detail.getLabeledContent(), detail.getTotalId());
            if (updateLabeledContentById != 1) {
                log.error("更新批注后的内容失败");
                throw new BizException(EssayLabelErrors.UPDATE_LABELED_CONTENT_ERROR);
            }
        }
        return detail.getId();
    }


    /**
     * 获取下一篇（返回的数据和列表页一样）
     *
     * @param admin
     * @return
     */
    @Override
    public QuestionAnswerLabelListVO getNext(String admin, long areaId, String year, double examScoreMin, int wordNumMin, double subScoreRatioMin, double examScoreMax, int wordNumMax, double subScoreRatioMax, int labelStatus, long questionId, String stem, long questionAnswerId) {
        QuestionAnswerLabelListVO vo = new QuestionAnswerLabelListVO();
        if (-1 != questionAnswerId) {
            log.error("没有拿到下一题");
            throw new BizException(EssayLabelErrors.NOT_GET_NEXT);
        }
        Long answerId = 0L;
        Long totalId = 0L;
        /**
         * 校验是否存在未完成的批注
         */
        EssayLabelTotal unfinishedLabel = checkUnfinishedLabel(admin);
        if (null != unfinishedLabel) {
            log.error("您还有未完成批注，请先保存或取消未完成的批注，unfinishedId：{}", unfinishedLabel.getId());
            answerId = unfinishedLabel.getAnswerId();
            totalId = unfinishedLabel.getId();
        }

        //1.获取下一篇批注的答题卡id
        if (answerId == 0L) {
            answerId = getNextLabelTask.getNextLabel(admin, areaId, year, examScoreMin, wordNumMin, subScoreRatioMin, examScoreMax, wordNumMax, subScoreRatioMax, labelStatus, questionId, stem);
        }
        if (answerId != null && answerId > 0) {
        } else {
            log.error("没有拿到下一题");
            throw new BizException(EssayLabelErrors.NOT_GET_NEXT);
        }
        Map map = startLabel(answerId, admin, NOT_FINAL);
        totalId = (Long) map.get("totalId");
        //2.根据答题卡ID，包装数据
        EssayQuestionAnswer questionAnswer = essayQuestionAnswerRepository.findOne(answerId);
        List<EssayLabelTotal> labelTotalList = essayLabelTotalRepository.findByAnswerIdAndStatusAndBizStatusIsNotOrderByGmtModifyAsc
                (questionAnswer.getId(), EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(), EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());

        vo = answerConvert2LabelVO(questionAnswer, labelTotalList);
        vo.setTotalId(totalId);

        return vo;

    }

    /**
     * 关闭超时未完成的批注
     */
    @Override
    public void closeUnfinishedLabel() {
        //total表数据处理(关闭两小时前创建的，且未完成的批注)
        Date date = getTwoHourBefore();
        int updateToClose = essayLabelTotalRepository.updateToClose(date);

    }

    /**
     * 获取下一篇批注的答题卡id
     *
     * @param admin
     */
    @Override
    public long findNext(String admin, long areaId, String year, double examScoreMin, int wordNumMin, double subScoreRatioMin,
                         double examScoreMax, int wordNumMax, double subScoreRatioMax,
                         int labelStatus, long questionId, String stem) {
        /*
         * 1.先统计有批注记录的答题卡 （包含正在批改的）
         *    1.1 有数据，取出一个只批改过一次的，并且自己没有批改过的
         *    1.2 没有数据，取最近的一个答题卡
         */
        Boolean isVip = isVip(admin);
        //如果是VIP只批注没人操作的
        if (isVip) {
            labelStatus = 0;
        }
        // 根据用户查询当前用户过滤的批注
        List<Long> userGiveUpList = getUserGiveUpList(admin);

        Page<EssayQuestionAnswer> page = findQuestionAnswerByConditions(userGiveUpList, areaId, year, examScoreMin, wordNumMin, subScoreRatioMin, examScoreMax, wordNumMax, subScoreRatioMax, labelStatus, questionId, stem, -1, -1, 20);
        if (page != null && CollectionUtils.isNotEmpty(page.getContent())) {
            List<EssayQuestionAnswer> answerList = page.getContent();
            //按照批注次数降序排列
            List<EssayQuestionAnswer> arrList = new ArrayList(answerList);
            arrList.sort(Comparator.comparingInt(i -> -i.getLabelStatus()));

            for (EssayQuestionAnswer answer : arrList) {
                //查询批注状态
                List<EssayLabelTotal> labelTotalList = essayLabelTotalRepository.findByAnswerIdAndStatusOrderByGmtModifyAsc
                        (answer.getId(), EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());

                LinkedList<String> labelCreator = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(labelTotalList)) {
                    labelTotalList.forEach(i -> {
                        labelCreator.add(i.getCreator());
                    });
                }

                //vip只能批注完全没有人批注的
                if (isVip) {
                    if (answer.getLabelStatus() == 0 && CollectionUtils.isEmpty(labelTotalList)) {
                        return answer.getId();
                    }
                } else {
                    //批注0次的
                    if (answer.getLabelStatus() == 0) {
                        if (CollectionUtils.isEmpty(labelTotalList)) {
                            return answer.getId();
                        } else if (labelTotalList.size() < 2 && !checkContainsVIP(labelCreator)) {
                            return answer.getId();
                        }
                        //批注1次的(自己没批注的)
                    } else if (answer.getLabelStatus() == 1) {
                        if (CollectionUtils.isNotEmpty(labelTotalList) && labelTotalList.size() == 1) {
                            if (!labelCreator.contains(admin) && !checkContainsVIP(labelCreator)) {
                                return answer.getId();
                            }
                        }
                    }
                }

            }
        }

        return 0L;
    }

    /**
     * 校验当前批注用户中是否包含VIP
     * true：包含
     * false：不包含
     *
     * @param labelCreator
     * @return
     */
    private boolean checkContainsVIP(List<String> labelCreator) {
        if (CollectionUtils.isEmpty(labelCreator)) {
            return false;

        }
        String vipLabelTeacherList = LabelRedisKeyConstant.getVIPLabelTeacherList();
        Set<Object> vipList = redisTemplate.opsForSet().members(vipLabelTeacherList);
        Collection intersection = CollectionUtils.intersection(new ArrayList(vipList), labelCreator);
        return CollectionUtils.isNotEmpty(intersection);

    }

    private List<Long> getUserGiveUpList(String admin) {
        String teacherGiveUpListKey = LabelRedisKeyConstant.getTeacherGiveUpListKey(admin);
        Set<Object> teacherGiveUpList = redisTemplate.opsForSet().members(teacherGiveUpListKey);

        String giveUpListKey = LabelRedisKeyConstant.getGiveUpListKey();
        Set<Object> giveUpList = redisTemplate.opsForSet().members(giveUpListKey);

        teacherGiveUpList.addAll(giveUpList);

        log.info("============================================" + new ArrayList(teacherGiveUpList).toString());

        return new ArrayList(teacherGiveUpList);
    }

    /**
     * 抄袭率持久化
     *
     * @param answerId
     * @param copyRatio
     */
    @Override
    public int saveCopyRatioToMysql(long answerId, double copyRatio) {
        int update = essayQuestionAnswerRepository.updateCopyRatioById(answerId, copyRatio);
        return update;
    }

    /**
     * 查询批注详情
     *
     * @param totalId
     * @return
     */
    @Override
    public EssayLabelInfoVO getInfo(long totalId) {
        //根据totalId查询综合批注
        EssayLabelTotal total = essayLabelTotalRepository.findOne(totalId);
        List<EssayLabelDetail> titleList = new LinkedList<>();
        List<EssayLabelDetail> detailList = new LinkedList<>();

        //根据totalID查询详细批注
        List<EssayLabelDetail> allList = essayLabelDetailRepository.findByTotalIdAndStatus(totalId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isNotEmpty(allList)) {

            for (EssayLabelDetail detail : allList) {
                if (StringUtils.isNotEmpty(detail.getTitleScore())) {
                    titleList.add(detail);
                } else {
                    detailList.add(detail);
                }
            }
        }
        return EssayLabelInfoVO.builder()
                .detailList(detailList)
                .titleList(titleList)
                .total(total)
                .build();
    }

    @Override
    public Long copy(long totalId, long finalId, String admin) {
        //校验参数
        EssayLabelTotal total = essayLabelTotalRepository.findOne(totalId);
        EssayLabelTotal finalLabel = essayLabelTotalRepository.findOne(finalId);

        if (null == total) {
            log.error("批注id错误。totalId：{}", totalId);
            throw new BizException(EssayLabelErrors.LABEL_TOTAL_ID_ERROR);
        } else if (null == finalLabel) {
            log.error("终审批注id错误。finalId:{}", finalId);
            throw new BizException(EssayLabelErrors.LABEL_FINAL_ID_ERROR);
        }

        /**
         *   将指定批注的所有细节拷贝到终审批注
         *   0.先清除之前的详细批注
         *
         *      1.详细批注的totalId 和creator 改成终审的
         *      2.综合批注同理
         */

        essayLabelDetailRepository.updateToDel(finalId);
        List<EssayLabelDetail> labelDetails = essayLabelDetailRepository.findByTotalIdAndStatus
                (totalId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        HashMap<Long, Long> changeMap = new HashMap<>();
        for (EssayLabelDetail detail : labelDetails) {
            EssayLabelDetail finalDetail = new EssayLabelDetail();

            BeanUtils.copyProperties(detail, finalDetail);
            finalDetail.setId(0L);
            finalDetail.setTotalId(finalId);
            finalDetail.setCreator(admin);
            finalDetail.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.ONLINE.getBizStatus());
            finalDetail = essayLabelDetailRepository.save(finalDetail);
            if (StringUtils.isNotEmpty(detail.getThesisScore())) {
                changeMap.put(detail.getId(), finalDetail.getId());
            }
        }

        //如果有论点批注，检查一下更新论据批注
        if (changeMap.size() != 0) {
            List<EssayLabelDetail> finalList = essayLabelDetailRepository.findByTotalIdAndStatus
                    (finalId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
            List<EssayLabelDetail> evidenceListByList = getEvidenceListByList(finalList);

            for (EssayLabelDetail evidenceLabel : evidenceListByList) {
                String evidenceScore = evidenceLabel.getEvidenceScore();
                String[] split = evidenceScore.split("-");
                long thesisId = Long.parseLong(split[1]);
                Long newThesisId = changeMap.get(thesisId);
                evidenceScore = split[0] + "-" + newThesisId;

                evidenceLabel.setEvidenceScore(evidenceScore);
                essayLabelDetailRepository.save(evidenceLabel);

            }
        }
        EssayLabelTotal essayLabelTotal = new EssayLabelTotal();
        BeanUtils.copyProperties(total, essayLabelTotal);
        essayLabelTotal.setIsFinal(IS_FINAL);
        essayLabelTotal.setId(finalId);
        essayLabelTotal.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        essayLabelTotal.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());
        essayLabelTotal.setCreator(admin);
        //用来存储来源批注
        essayLabelTotal.setModifier(totalId + "");
        essayLabelTotal = essayLabelTotalRepository.save(essayLabelTotal);

        return finalId;
    }

    @Override
    public EssayLabelDetail getDetailInfo(long detailId) {
        return essayLabelDetailRepository.findOne(detailId);
    }

    @Override
    public int delLabel(long totalId) {
        return essayLabelTotalRepository.updateToDelById(totalId);
    }

    @Override
    public int delDetailLabel(long detailId) {
        EssayLabelDetail labelDetail = essayLabelDetailRepository.findOne(detailId);
        List<EssayLabelDetail> essayLabelDetails = essayLabelDetailRepository.findByTotalIdAndStatus
                (labelDetail.getTotalId(), EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());

        long delId = 0L;
        if (labelDetail == null) {
            log.error("详细批注id错误");
            throw new BizException(EssayLabelErrors.LABEL_DETAIL_ID_ERROR);
        } else {
            /**
             * 如果被删除的批注是论点得分，修改关联的论据得分
             */
            if (StringUtils.isNotEmpty(labelDetail.getThesisScore())) {
                //1.查询所有相关的详细批注
                Long totalId = labelDetail.getTotalId();
                List<EssayLabelDetail> labelDetailList = essayLabelDetailRepository.findByTotalIdAndStatus
                        (totalId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
                for (EssayLabelDetail detail : labelDetailList) {
                    String evidenceScore = detail.getEvidenceScore();
                    //2.找到论据批注
                    if (StringUtils.isNotEmpty(evidenceScore)) {
                        //3.判断是否关联该论点
                        String[] split = evidenceScore.split("-");
                        if (split.length == 2) {
                            String thesisScore = split[1];
                            //4.关联该论点，将论点相关信息置空
                            if (thesisScore.equals(detailId + "")) {

                                detail.setEvidenceScore("");
                                boolean checkDetailIsEmpty = checkDetailIsEmpty(detail);
                                essayLabelDetailRepository.save(detail);
                                if (checkDetailIsEmpty) {
                                    delId = detail.getId();
                                }
                            }
                        }
                    }
                }
            }

        }

        labelDetail.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.DELETED.getStatus());
        essayLabelDetailRepository.save(labelDetail);
        //如果是标题批注，不处理 labeledContent
        if (StringUtils.isNotEmpty(labelDetail.getTitleScore())) {
            return 1;
        }
        //更新labeledContent(前端显示需要)
        EssayLabelTotal total = essayLabelTotalRepository.findOne(labelDetail.getTotalId());
        String labeledContent = total.getLabeledContent();
        labeledContent = labeledContent.replaceAll("\"", "\'");

        int index = -1;

        List<EssayLabelDetail> detailListWithoutTitle = new LinkedList<>();


        for (EssayLabelDetail detail : essayLabelDetails) {
            if (StringUtils.isEmpty(detail.getTitleScore())) {
                detailListWithoutTitle.add(detail);
            }
        }

        for (int i = 0; i < detailListWithoutTitle.size(); i++) {

            EssayLabelDetail detail = detailListWithoutTitle.get(i);
            String rule = "";
            String rule2 = "";
            String rule3 = "";
            String rule4 = "";

            //将此批注的标记删除
            if (detail.getId() == detailId) {
                index = i;

                rule = "△<font style='color:red;font-weight:bolder;font-size:27px'>" + (index + 1) + "</font>△";
                rule2 = "△<font style=\"color:red;font-weight:bolder;font-size:27px\">" + (index + 1) + "</font>△";
                rule3 = "△<font style='color:red;font-weight:bolder;font-size:27pxx'>" + (index + 1) + "</font>△";
                rule4 = "△<font style=\"color:red;font-weight:bolder;font-size:27px\">" + (index + 1) + "</font>△";
                int indexOf = labeledContent.indexOf(rule);
                int indexOf2 = labeledContent.indexOf(rule2);
                int indexOf3 = labeledContent.indexOf(rule3);
                int indexOf4 = labeledContent.indexOf(rule4);
                labeledContent = labeledContent.replaceAll(rule, "")
                        .replaceAll(rule2, "")
                        .replaceAll(rule3, "")
                        .replaceAll(rule4, "");
                log.info(rule);
            }
            //之后的批注，序号往前移动1位
            if (index != -1 && i >= index) {
                labeledContent = labeledContent.replaceAll("△<font style='color:red;font-weight:bolder;font-size:27px'>" + (i + 1) + "</font>△", "△<font style='color:red;font-weight:bolder;font-size:27px'>" + i + "</font>△")
                        .replaceAll("△<font style=\"color:red;font-weight:bolder;font-size:27px\">" + (i + 1) + "</font>△", "△<font style=\"color:red;font-weight:bolder;font-size:27px\">" + i + "</font>△")
                        .replaceAll("△<font style='color:red;font-weight:bolder;font-size:27px'>" + (i + 1) + "</font>△", "△<font style='color:red;font-weight:bolder;font-size:27px'>" + i + "</font>△")
                        .replaceAll("△<font style=\"color:red;font-weight:bolder;font-size:27px\">" + (i + 1) + "</font>△", "△<font style=\"color:red;font-weight:bolder;font-size:27px\">" + i + "</font>△")
                ;
            }
        }
        total.setLabeledContent(labeledContent);
        essayLabelTotalRepository.save(total);
        if (delId > 0L) {
            delDetailLabel(delId);
        }
        return 1;
    }

    /**
     * 校验详细批注是否为空（没有任何批注得分）
     *
     * @param detail
     * @return
     */
    private boolean checkDetailIsEmpty(EssayLabelDetail detail) {
        if (StringUtils.isEmpty(detail.getThesisScore()) && StringUtils.isEmpty(detail.getTitleScore())
                && StringUtils.isEmpty(detail.getEvidenceScore()) && StringUtils.isEmpty(detail.getStructScore())
                && StringUtils.isEmpty(detail.getSentenceScore()) && StringUtils.isEmpty(detail.getLiteraryScore())
                && StringUtils.isEmpty(detail.getThoughtScore()) && StringUtils.isEmpty(detail.getElseRemark())) {
            return true;
        }
        return false;
    }

    @Override
    public List<EssayLabelDetail> getThesisList(long totalId) {
        //根据totalID查询详细批注
        List<EssayLabelDetail> detailList = essayLabelDetailRepository.findByTotalIdAndStatus(totalId, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        List<EssayLabelDetail> thesisList = getThesisListByList(detailList);
        return thesisList;
    }

    /**
     * 重新批注
     *
     * @param finalId
     * @return
     */
    @Override
    public int restart(long finalId, String admin) {
        EssayLabelTotal finalLabel = essayLabelTotalRepository.findOne(finalId);
        if (finalLabel != null) {
            //综合批注信息清空EssayLabelServiceImpl
            EssayLabelTotal newFinalLabel = new EssayLabelTotal();
            newFinalLabel.setId(finalId);
            newFinalLabel.setCreator(admin);
            newFinalLabel.setAnswerId(finalLabel.getAnswerId());
            newFinalLabel.setIsFinal(IS_FINAL);
            EssayQuestionAnswer answer = essayQuestionAnswerRepository.findOne(finalLabel.getAnswerId());
            List<EssayLabelTotal> labelTotalList = essayLabelTotalRepository.findByAnswerIdAndStatusAndBizStatusIsNotOrderByGmtModifyAsc
                    (answer.getId(), EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(), EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());

            QuestionAnswerLabelListVO labelListVO = answerConvert2LabelVO(answer, labelTotalList);

            newFinalLabel.setQuestionId(answer.getQuestionBaseId());
            newFinalLabel.setLabeledContent(labelListVO.getContent());
            newFinalLabel.setTitleContent(labelListVO.getTitle());
            newFinalLabel.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
            newFinalLabel.setBizStatus(EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus());
            newFinalLabel.setModifier("");
            newFinalLabel = essayLabelTotalRepository.save(newFinalLabel);

            //详细批注全部删除
            essayLabelDetailRepository.updateToDel(finalId);
            return 1;
        }
        return 0;
    }

    @Override
    public void addFinalLabelTeacher(String teacher) {
        String finalLabelTeacherList = LabelRedisKeyConstant.getFinalLabelTeacherList();
        Long add = redisTemplate.opsForSet().add(finalLabelTeacherList, teacher);
    }

    @Override
    public Object getFinalLabelTeacher() {
        String finalLabelTeacherList = LabelRedisKeyConstant.getFinalLabelTeacherList();
        return redisTemplate.opsForSet().members(finalLabelTeacherList);
    }


    public List<EssayLabelDetail> getThesisListByList(List<EssayLabelDetail> detailList) {
        List<EssayLabelDetail> thesisList = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(detailList)) {
            for (EssayLabelDetail detail : detailList) {
                if (StringUtils.isNotEmpty(detail.getThesisScore())) {
                    thesisList.add(detail);
                }
            }
        }
        return thesisList;
    }


    public List<EssayLabelDetail> getEvidenceListByList(List<EssayLabelDetail> detailList) {
        List<EssayLabelDetail> thesisList = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(detailList)) {
            for (EssayLabelDetail detail : detailList) {
                if (StringUtils.isNotEmpty(detail.getEvidenceScore())) {
                    thesisList.add(detail);
                }
            }
        }
        return thesisList;
    }


    public List<Long> getStructureListByList(List<EssayLabelDetail> detailList) {
        List<Long> structureList = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(detailList)) {
            for (EssayLabelDetail detail : detailList) {
                if (StringUtils.isNotEmpty(detail.getStructScore())) {
                    structureList.add(detail.getId());
                }
            }
        }
        return structureList;
    }

    public List<Long> getTitleListByList(List<EssayLabelDetail> detailList) {
        List<Long> titleList = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(detailList)) {
            for (EssayLabelDetail detail : detailList) {
                if (StringUtils.isNotEmpty(detail.getTitleScore())) {
                    titleList.add(detail.getId());
                }
            }
        }
        return titleList;
    }


    /**
     * 获得当前时间的前两小时
     *
     * @param date
     * @return
     * @throws Exception
     */
    public static Date getTwoHourBefore() {
        Date date = new Date();
        Calendar c = Calendar.getInstance();

        c.setTime(date);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        c.set(Calendar.HOUR_OF_DAY, hour - 2);

        return c.getTime();
    }

    /**
     * 将答题卡对象转换成批注列表页对象
     *
     * @param answer
     * @return
     */
    QuestionAnswerLabelListVO answerConvert2LabelVO(EssayQuestionAnswer answer, List<EssayLabelTotal> labelTotalList) {
        if (null == answer) {
            return null;
        }
        long questionBaseId = answer.getQuestionBaseId();
        long questionDetailId = answer.getQuestionDetailId();
        QuestionAnswerLabelListVO labelListVO = QuestionAnswerLabelListVO.builder()
                .answerId(answer.getId())
                .questionBaseId(questionBaseId)
                .questionDetailId(questionDetailId)
                .stem("")
                .year(answer.getQuestionYear())
                .areaId(answer.getAreaId())
                .areaName(answer.getAreaName())
                .userId(answer.getUserId())
                .examScore(answer.getExamScore())
                .spendTime(answer.getSpendTime())
                .inputWordNum(answer.getInputWordNum())
                .correctDate(answer.getCorrectDate())
                .labelStatus(answer.getLabelStatus())
                .subScore(answer.getSubScore())
                .subScoreFlag(answer.getSubScoreFlag())
                .score(answer.getScore())
                .build();

        //查询标题
        //查询字数限制
//        EssayQuestionDetail questionDetail = (EssayQuestionDetail)redisTemplate.opsForValue().get("label_question_group_"+questionDetailId);
//        if(questionDetail == null){
//            questionDetail = essayQuestionDetailRepository.findById(questionDetailId);
//            redisTemplate.opsForValue().set("label_question_group_"+questionDetailId,questionDetail);
//        }
//        List<EssaySimilarQuestion> similarQuestionList = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(questionBaseId, EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
//        if (CollectionUtils.isNotEmpty(similarQuestionList)) {
//            long similarId = similarQuestionList.get(0).getSimilarId();
//            Map<Long, EssaySimilarQuestionGroupInfo> questionGroupMap = QuestionManager.getQuestionGroupMap(essaySimilarQuestionGroupInfoRepository, redisTemplate);
//            EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = questionGroupMap.get(similarId);
//            if (similarQuestionGroupInfo != null) {
//                labelListVO.setStem(similarQuestionGroupInfo.getShowMsg());
//            }
//        }
        //切分正文和标题

        StringBuilder title = new StringBuilder();
        StringBuilder content = new StringBuilder();
        if (StringUtils.isNotEmpty(answer.getContent())) {
            List<String> paragraphList = cutParagraph(answer.getContent());
            if (CollectionUtils.isNotEmpty(paragraphList)) {
                for (int i = 0; i < paragraphList.size(); i++) {
                    if (paragraphList.get(i).length() <= 30 && i < 2) {
                        title.append(paragraphList.get(i) + "<br/>");
                    } else {
                        content.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + paragraphList.get(i) + "<br/>");
                    }
                }

            }
        }

        labelListVO.setTitle(changeToChineseSym(delBrLabel(title.toString())));
        labelListVO.setContent(changeToChineseSym(delBrLabel(content.toString())));
        labelListVO.setCopyRatio(getCopyRatio().apply(answer));

        //查询字数限制
        EssayQuestionDetail questionDetail = (EssayQuestionDetail) redisTemplate.opsForValue().get("label_question_detail_" + questionDetailId);
        if (questionDetail == null) {
            questionDetail = essayQuestionDetailRepository.findById(questionDetailId);
            redisTemplate.opsForValue().set("label_question_detail_" + questionDetailId, questionDetail);
        }
        labelListVO.setMaxInputWordNum(questionDetail.getInputWordNumMax());
        labelListVO.setMinInputWordNum(questionDetail.getInputWordNumMin());
        labelListVO.setIsAssigned(questionDetail.getIsAssigned());
        labelListVO.setStem(questionDetail.getStem());

        //查询批注状态
        labelTotalList = labelTotalList
                .stream()
                .filter(total -> total.getAnswerId() == answer.getId())
                .collect(Collectors.toList());

        LinkedList<LabelSmallVO> labelSmallVOS = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(labelTotalList)) {
            for (EssayLabelTotal total : labelTotalList) {
                LabelSmallVO smallVO = LabelSmallVO.builder()
                        .labelId(total.getId())
                        .labelScore(total.getTotalScore())
                        .labelTeacher(total.getCreator())
                        .labelTime(total.getGmtCreate())
                        .build();
                labelSmallVOS.add(smallVO);
            }
        }
        labelListVO.setLabelList(labelSmallVOS);


        return labelListVO;

    }


    public Function<EssayQuestionAnswer, Double> getCopyRatio() {

        return (answer -> {
            //判断答题卡里是否计算了抄袭率，没有的话计算抄袭率，并保存（异步）
            if (answer.getCopyRatio() != -1 && answer.getCopyRatio() != 0) {
                return keepTwoDecimal(answer.getCopyRatio());
            } else {
                //查询试题的材料
                List<String> materialStrList = new LinkedList<>();
                List<EssayMaterial> materialList = findPaperMaterialList(answer.getQuestionBaseId());

                if (CollectionUtils.isNotEmpty(materialList)) {
                    for (EssayMaterial materialVO : materialList) {
                        materialStrList.add(materialVO.getContent());
                    }
                }

                double copyRatio = getCopyRatioV2(answer.getContent(), materialStrList);

                //异步更新，抄袭率
                asyncCopyRatioServiceImpl.saveCopyRatioToMysql(answer.getId(), copyRatio);
                return keepTwoDecimal(copyRatio);
            }
        });
    }

    private double getCopyRatioV2(String userAnswer, List<String> materials) {
        double copyRatio = 0D;

        List<String> materailPhrases = new ArrayList<>();
        List<String> userPhrases = new ArrayList<>();

        if (org.apache.commons.lang3.StringUtils.isNoneBlank(userAnswer)) {
            List<String> paragraphs = cutParagraph(userAnswer);
            for (String paragraph : paragraphs) {
                String content = paragraph;
//                List<String> userPhrasesTemp = cutSentences(content,2);
                List<Character> punctuations = new ArrayList<>();//断句标点
                punctuations.addAll(Arrays.asList('。', '.', ';', '；', '…', '!', '?', '？', ',', '，', '！', ':', '：', '、', '"', '“', '”'));
                List<String> userPhrasesTemp = cutSentencesNextLowerNoQuotation(content, punctuations);

                userPhrases.addAll(userPhrasesTemp);
            }
        }

        for (String material : materials) {
            if (org.apache.commons.lang3.StringUtils.isNoneBlank(material)) {
                List<String> paragraphs = cutParagraph(material);
                for (String paragraph : paragraphs) {
                    String content = paragraph;
//                    List<String> materialPhrasesTemp = cutSentences(content,2);
                    List<Character> punctuations = new ArrayList<>();//断句标点
                    punctuations.addAll(Arrays.asList('。', '.', ';', '；', '…', '!', '?', '？', ',', '，', '！', ':', '：', '、', '"', '“', '”'));
                    List<String> materialPhrasesTemp = cutSentencesNextLowerNoQuotation(content, punctuations);

                    materailPhrases.addAll(materialPhrasesTemp);
                }
            }
        }

        List<String> similarPhrases = new ArrayList<>();
        List<String> similarNotPhrases = new ArrayList<>();
        int similarLen = 0;

        for (String userPhrase : userPhrases) {
            int phraseLen = userPhrase.length();
            int temp = 0;
            for (String materialPhrase : materailPhrases) {
                String commonPhrase = Label2AppUtil.longestCommonSubstring(userPhrase, materialPhrase);
                double percent = 1.0 * commonPhrase.length() / phraseLen;
                if (percent >= 0.8) {
                    similarPhrases.add(userPhrase);
//                    log.info("相同的句子：   {}",userPhrase);
                    similarLen += userPhrase.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
                    temp = 1;
                    break;
                }
            }
            if (temp == 0) {
                similarNotPhrases.add(userPhrase);
            }
        }
		int allLen = 0;
		if (org.apache.commons.lang3.StringUtils.isNoneBlank(userAnswer)) {
			allLen = userAnswer.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
		}
        if (similarLen != 0 && allLen != 0) {
            copyRatio = 1.0 * similarLen / allLen;
        }


        return copyRatio;

    }

//    private double getCopyRatio(EssayQuestionAnswer answer) {
//        double copyRatio = 0D;
//
//        if(StringUtils.isNotEmpty(answer.getContent())){
//            List<String> paragraphList = cutParagraph(answer.getContent());
//            String contentString = list2String(paragraphList);
//            //查询试题的材料
//            StringBuilder material = new StringBuilder();
//            List<EssayMaterial> materialList = findPaperMaterialList(answer.getQuestionBaseId());
//
//            if(CollectionUtils.isNotEmpty(materialList)){
//                for(EssayMaterial materialVO:materialList){
//                    material.append(materialVO.getContent()).append("。");
//                }
//            }
//
//            //对学员答案和材料进行子句拆分
//            List<Character> punctuations = new ArrayList<>();//断句标点
//            punctuations.addAll(Arrays.asList('。','.',';','；','…','!','?','？',',','，','！',':','：','、','"','“','”'));
//            List<String> contentSentenceList = cutSentencesNextLowerNoQuotation(contentString, punctuations);
//            List<String> materialSentenceList = cutSentencesNextLowerNoQuotation(material.toString(), punctuations);
//
//            //比较学员答案和材料的相似度
//            StringBuilder copySentence = new StringBuilder();
//            for(String contentSentence:contentSentenceList){
//                for(String materialSentence:materialSentenceList){
//                    //计算学员答案子句和材料的抄袭率
//                    double sentenceCopyRatio = getSentenceCopyRatio(contentSentence,materialSentence);
//                    if(sentenceCopyRatio > 0.8){
//                        copySentence.append(contentSentence);
//                        break;
//                    }
//                }
//            }
//            log.info("====================="+copySentence+"=============="+list2String(contentSentenceList).replace("。",""));
//            if(StringUtils.isNotEmpty(list2String(contentSentenceList).replace("。","")) && StringUtils.isNotEmpty(copySentence)){
//                int contentLength = list2String(contentSentenceList).replace("。", "").length();
//                int copyLength = copySentence.length();
//                copyRatio = (double)copyLength / contentLength;
//            }
//        }
//        return copyRatio;
//
//    }


    public double getSentenceCopyRatio(String content, String material) {
        double copyRatio = 0D;
        String longestCommonSubstring = Label2AppUtil.longestCommonSubstring(content, material.toString());

        if (StringUtils.isNotEmpty(content)) {
            if (longestCommonSubstring.replaceAll("[^\\u4e00-\\u9fa5]", "").length() != 0) {
                copyRatio = (double) longestCommonSubstring.replaceAll("[^\\u4e00-\\u9fa5]", "").length() / content.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
            } else {
                copyRatio = 0;
            }
        }
        return copyRatio;


    }

    public String list2String(List<String> contentList) {
        StringBuilder contentStr = new StringBuilder();
        contentList.forEach(content -> {
            contentStr.append(content).append("。");
        });
        return contentStr.toString();
    }

    public boolean isEnd(String sentence) {
        String input = "[0-9a-zA-Z一二三四五六七八九][）)\\s]{0,1}";
        Pattern p = Pattern.compile(input);
        Matcher m = p.matcher(sentence);
        while (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 切分短句
     *
     * @param userAnswer
     * @param punctuations
     * @return
     */
    public List<String> cutSentencesNextLowerNoQuotation(String userAnswer, List<Character> punctuations) {
        List<String> sentences = new ArrayList<>();
        StringBuffer sentence = new StringBuffer();
        for (int i = 0, len = userAnswer.length(); i < len; i++) {
            char ch = userAnswer.charAt(i);
            sentence.append(ch);
            if (punctuations.contains(ch)) {
                if (ch == '.') {//出现.但是前面出现1,2等分条标记，不做断句
                    String senStr = String.valueOf(sentence).trim();
                    if (senStr.length() < 2) {
                        sentence = new StringBuffer();
                        continue;
                    } else {
                        String lastChar = senStr.substring(senStr.length() - 2, senStr.length() - 1);
                        if (isEnd(lastChar)) {
                            continue;
                        }
                    }
                }
                String senStr = String.valueOf(sentence.substring(0, sentence.length() - 1)).trim();
                if (senStr.length() > 0) {
                    sentences.add(senStr);
                }
                sentence = new StringBuffer();//重新定义句子
            }
            if (i == len - 1) {
                String senStr = String.valueOf(sentence).trim();
                if (senStr.length() > 0) {
                    sentences.add(senStr);
                }
            }
        }
        return sentences;
    }


    private List<EssayMaterial> findPaperMaterialList(long questionBaseId) {
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        if (questionBase != null) {
            List<EssayMaterial> materials = essayMaterialRepository.findByPaperIdAndStatusOrderBySortAsc(questionBase.getPaperId(), EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());
            return materials;
        }
        return null;
    }

    /**
     * 截取掉结尾的br标签
     */
    public String delBrLabel(String content) {
        if (content.endsWith("<br/>")) {
            int i = content.lastIndexOf("<br/>");
            content = content.substring(0, i);
        }

        return content;
    }

    /**
     * 小数点后保留两位小数
     *
     * @param aDouble
     * @return
     */
    public static Double keepTwoDecimal(Double aDouble) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (aDouble == null) {
            return 0x0.0p0;
        } else {
            return Double.valueOf(decimalFormat.format(aDouble));
        }
    }



    /**
     * 切段落
     *
     * @param userAnswer
     * @return
     */
    public List<String> cutParagraph(String userAnswer) {
        int[] charAscii = {160, 8232, 12288};
        char ch = (char) charAscii[0];
        String regex = ch + "";
        for (int i = 1, len = charAscii.length; i < len; i++) {
            regex += ("|" + charAscii[i]);
        }
        List<String> strings = new LinkedList<>();
        String content = userAnswer.replaceAll(regex, " ");
        String[] paragraphStrs = content.split("\\n{1,}|\\s{2,}");
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        for (int i = 0, len = paragraphStrs.length; i < len; i++) {
            if (paragraphStrs[i].trim().length() >= 1) {
                int start = findStart(0, content, paragraphStrs[i].trim(), starts, ends);
                int end = start + paragraphStrs[i].trim().length();
                starts.add(start);
                ends.add(end);
//                log.info("第{}段，内容为：{}",i,content.substring(start,end));
                strings.add(content.substring(start, end));

            }
        }
        return strings;
    }

    /**
     * @param firstStart 初始位置
     * @param firstStr
     * @param secondStr
     * @param starts
     * @param ends
     * @return
     */
    public int findStart(int firstStart, String firstStr, String secondStr, List<Integer> starts, List<Integer> ends) {
        int len = secondStr.length();
        int len1 = firstStr.length();
        int start = firstStr.indexOf(secondStr) + firstStart;
        int end = start + len;
        while (isContainStart(start, end, starts, ends) && (start - firstStart) < len1) {
            start = firstStr.indexOf(secondStr, end - firstStart) + firstStart;
            end = start + len;
        }
        return start;
    }

    /**
     * 是否该起始终止位置已经被用
     *
     * @param start
     * @param starts
     * @param ends
     * @return
     */
    private boolean isContainStart(int start, int end, List<Integer> starts, List<Integer> ends) {
        boolean result = false;
        for (int i = 0, size = starts.size(); i < size; i++) {
            int start1 = starts.get(i);
            int end1 = ends.get(i);
            if ((start1 <= start && end1 > start) || (start1 < end && end1 >= end)) {
                result = true;
            }
        }
        return result;
    }


    /**
     * 拼接批注的查询条件(左闭右包)
     *
     * @param questionIdList
     * @param areaId
     * @param year
     * @param examScore
     * @param wordNum
     * @param questionId
     * @param subScoreFlag
     * @param labelStatus
     * @return
     */
    private Specification querySpecific(List<Long> questionIdList, long areaId, String year,
                                        double examScoreMin, int wordNumMin, double subScoreRatioMin,
                                        double examScoreMax, int wordNumMax, double subScoreRatioMax,
                                        long questionId, int labelStatus, long answerId, boolean flag, List<Long> giveUpList) {
        Specification querySpecific = new Specification<EssayQuestionAnswer>() {
            @Override
            public Predicate toPredicate(Root<EssayQuestionAnswer> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {


                List<Predicate> predicates = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(giveUpList)) {
                    predicates.add((root.get("id").in(giveUpList)).not());

                }
                if (flag) {
                    predicates.add(criteriaBuilder.notEqual(root.get("labelStatus"), 2));
                    predicates.add(criteriaBuilder.notEqual(root.get("labelStatus"), 3));

                }
                //不查询已放弃题目
//                predicates.add(criteriaBuilder.not(root.get("id"), giveUpList));

                //只查询议论文题目
                predicates.add(criteriaBuilder.equal(root.get("questionType"), 5));
                //只查询字数大于0字的数据
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("inputWordNum"), 50));

                if (answerId > 0) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), answerId));
                }
                if (CollectionUtils.isNotEmpty(questionIdList) && questionId <= 0) {
                    predicates.add((root.get("questionBaseId").in(questionIdList)));
                }
                if (questionId > 0) {
                    predicates.add(criteriaBuilder.equal(root.get("questionBaseId"), questionId));
                }
                if (areaId > 0) {
                    predicates.add(criteriaBuilder.equal(root.get("areaId"), areaId));
                }
                if (StringUtils.isNotEmpty(year)) {
                    predicates.add(criteriaBuilder.equal(root.get("questionYear"), year));
                }
                //分数自定义范围
                if (examScoreMin > 0) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("examScore"), examScoreMin));
                }
                if (examScoreMax > 0) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("examScore"), examScoreMax));
                }

                //字数自定义范围

                if (wordNumMin > 0) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("inputWordNum"), wordNumMin));
                }
                if (wordNumMax > 0) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("inputWordNum"), wordNumMax));
                }


                //分差自定义范围
                if (subScoreRatioMin > 0) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("subScoreRatio"), (double) (subScoreRatioMin / 100)));
                }
                if (subScoreRatioMax > 0) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("subScoreRatio"), (double) (subScoreRatioMax / 100)));
                }

                //批注状态
                if (labelStatus >= 0) {
                    predicates.add(criteriaBuilder.equal(root.get("labelStatus"), labelStatus));
                }
                predicates.add(criteriaBuilder.equal(root.get("status"), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus()));
                predicates.add(criteriaBuilder.equal(root.get("bizStatus"), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()));


                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }


    public String changeToChineseSym(String content) {
        content = content.replaceAll("\\(", "（").replaceAll("\\)", "）");

        return content;
    }


    @Override
    public ModelAndView getFinalExcel(long questionId, long start, long end) {

        ExcelView excelView = new FinalLabelExcelView();
        Map finalMap = getFinalMap(questionId, start, end);
        return new ModelAndView(excelView, finalMap);
    }

    public Map getFinalMap(long questionId, long start, long end) {

        Map<String, Object> map = new HashMap<String, Object>();

        //开始结束时间转换成00:00-24:00
        start = DateUtil.getZeroPointTimestamps(start);
        end = DateUtil.getEndPointTimestamps(end);

        //分数区间
        LinkedList<String> scoreList = new LinkedList<>();
        //0起步   17.5%   37.5%   47.5%  62.5%   75%   85%   92.5%   100%
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionId);
        if (null == questionBase) {
            log.info("参数异常，试题Id错误，questionId：{}", questionId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }

        EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionBase.getDetailId());
        double score = questionDetail.getScore();
        double firstScore = keepTwoDecimal(score * 0.175);
        double secondScore = keepTwoDecimal(score * 0.375);
        double thirdScore = keepTwoDecimal(score * 0.475);
        double forthScore = keepTwoDecimal(score * 0.625);
        double fifthScore = keepTwoDecimal(score * 0.75);
        double sixthScore = keepTwoDecimal(score * 0.85);
        double seventhScore = keepTwoDecimal(score * 0.925);

        String firstLevel = Joiner.on("~").join("0", firstScore);
        String secondLevel = Joiner.on("~").join(firstScore, secondScore);
        String thirdLevel = Joiner.on("~").join(secondScore, thirdScore);
        String forthLevel = Joiner.on("~").join(thirdScore, forthScore);
        String fifthLevel = Joiner.on("~").join(forthScore, fifthScore);
        String sixthLevel = Joiner.on("~").join(fifthScore, sixthScore);
        String seventhLevel = Joiner.on("~").join(sixthScore, seventhScore);
        String eighthLevel = Joiner.on("~").join(seventhScore, score);

        scoreList.add(firstLevel);
        scoreList.add(secondLevel);
        scoreList.add(thirdLevel);
        scoreList.add(forthLevel);
        scoreList.add(fifthLevel);
        scoreList.add(sixthLevel);
        scoreList.add(seventhLevel);
        scoreList.add(eighthLevel);

        List<EssayLabelTotal> essayLabels = essayLabelTotalRepository.findByQuestionIdAndIsFinalAndStatusIsNotAndBizStatusAndGmtCreateBetween
                (questionId, 1, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(), EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus(), new Date(start), new Date(end));
        Map<String, Integer> scoreMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(essayLabels)) {
            for (EssayLabelTotal label : essayLabels) {
                //判断批注分数所属区间
                Double totalScore = label.getTotalScore();
                if (totalScore > seventhScore) {
                    scoreMap.put(eighthLevel, scoreMap.get(eighthLevel) == null ? 1 : scoreMap.get(eighthLevel) + 1);
                } else if (totalScore > sixthScore) {
                    scoreMap.put(seventhLevel, scoreMap.get(seventhLevel) == null ? 1 : scoreMap.get(seventhLevel) + 1);
                } else if (totalScore > fifthScore) {
                    scoreMap.put(sixthLevel, scoreMap.get(sixthLevel) == null ? 1 : scoreMap.get(sixthLevel) + 1);
                } else if (totalScore > forthScore) {
                    scoreMap.put(fifthLevel, scoreMap.get(fifthLevel) == null ? 1 : scoreMap.get(fifthLevel) + 1);
                } else if (totalScore > thirdScore) {
                    scoreMap.put(forthLevel, scoreMap.get(forthLevel) == null ? 1 : scoreMap.get(forthLevel) + 1);
                } else if (totalScore > secondScore) {
                    scoreMap.put(thirdLevel, scoreMap.get(thirdLevel) == null ? 1 : scoreMap.get(thirdLevel) + 1);
                } else if (totalScore > firstScore) {
                    scoreMap.put(secondLevel, scoreMap.get(secondLevel) == null ? 1 : scoreMap.get(secondLevel) + 1);
                } else {
                    scoreMap.put(firstLevel, scoreMap.get(firstLevel) == null ? 1 : scoreMap.get(firstLevel) + 1);
                }
            }
        }

        map.put("scoreList", scoreList);
        map.put("members", scoreMap);
        map.put("name", questionId + "终审批注分数统计" + System.currentTimeMillis());
        return map;
    }

    @Override
    public ModelAndView getTeacherExcel(long start, long end) {
        List<TeacherLabelExcelVO> teacherLabelExcelVOS = new LinkedList<>();

        //开始结束时间转换成00:00:01-23:59:59
        start = DateUtil.getZeroPointTimestamps(start);
        end = DateUtil.getEndPointTimestamps(end);

        double days = DateUtil.getDays(start, end);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<String> dateList = new LinkedList<>();
        for (int i = 0; i < days; i++) {
            Date date = DateUtil.getDaysBefore(new Date(end), i);
            dateList.add(sdf.format(date).substring(0, 10));
        }

        List<EssayLabelTotal> essayLabels = essayLabelTotalRepository.findByStatusAndBizStatusIsNotAndGmtCreateBetween
                (EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus(), EssayLabelStatusConstant.EssayLabelBizStatusEnum.INIT.getBizStatus(), new Date(start), new Date(end));
        Map<String, Map<String, Integer>> countMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(essayLabels) && CollectionUtils.isNotEmpty(dateList)) {
            //总批注数量
            for (EssayLabelTotal label : essayLabels) {
                String teacher = label.getCreator();
                String date = sdf.format(label.getGmtCreate()).substring(0, 10);
                Map<String, Integer> userCountMap = countMap.get(teacher);
                if (userCountMap == null || userCountMap.isEmpty()) {
                    userCountMap = new HashMap<>();
                }
                userCountMap.put(date, userCountMap.get(date) == null ? 1 : userCountMap.get(date) + 1);
                userCountMap.put("total", userCountMap.get("total") == null ? 1 : userCountMap.get("total") + 1);

                countMap.put(teacher, userCountMap);
            }
        }


        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dateList", dateList);
        map.put("members", countMap);
        map.put("name", "工作量统计" + System.currentTimeMillis());
        ExcelView excelView = new TeacherLabelExcelView();
        return new ModelAndView(excelView, map);
    }

    @Override
    public int giveUpLabel(long totalId, String teacher, Integer type) {

        EssayLabelTotal essayLabelTotal = essayLabelTotalRepository.findOne(totalId);
        Long answerId = essayLabelTotal.getAnswerId();
        EssayQuestionAnswer essayQuestionAnswer = essayQuestionAnswerRepository.findOne(answerId);

        //校验放弃类型
        if (type == null) {
            log.info("请明确放弃或过滤当前批注的原因。totalId:{},teacher:{}", totalId, teacher);
            throw new BizException(EssayLabelErrors.EMPTY_GIVEUP_TYPE);
            //当前用户放弃批注
        } else if (type == -1) {
            //批注过1次的不可以放弃，没有批注过的可以放弃
            if (essayQuestionAnswer.getLabelStatus() == 1) {
                log.info("当前答题卡已完成一审，暂不可放弃批注。请继续操作^_^,answerId:{}", answerId);
                throw new BizException(EssayLabelErrors.CANOT_GIVE_UP_LABEL);
            } else {
                //批注0次可以放弃，一天之内不再抽到该答题卡
                essayLabelTotalRepository.updateToDelById(totalId);
                String giveUpListKey = LabelRedisKeyConstant.getTeacherGiveUpListKey(teacher);
                redisTemplate.opsForSet().add(giveUpListKey, answerId);
                redisTemplate.expire(giveUpListKey, 1, TimeUnit.DAYS);
                return 1;
            }
        } else if (type >= 0) {
            essayLabelTotalRepository.updateToDelById(totalId);
            //过滤文章
            EssayDelAnswer delAnswer = EssayDelAnswer.builder()
                    .answerId(answerId)
                    .type(type)
                    .build();
            delAnswer.setCreator(teacher);

            delAnswer = essayDelAnswerRepository.save(delAnswer);
            String giveUpListKey = LabelRedisKeyConstant.getGiveUpListKey();
            redisTemplate.opsForSet().add(giveUpListKey, answerId);


            return 1;
        }
        return 0;
    }

    @Override
    public ModelAndView getFinalXmlExcel(Long labelId) {
        List<EssayLabelTotal> totalList = essayLabelTotalRepository.findLabelXml(labelId);
        LinkedList<FinalLabelXmlExcelVO> labelXmlExcelVOS = new LinkedList<>();
        int i = 0;
        for (EssayLabelTotal total : totalList) {
            log.info(total.getId() + "=======" + (i++));
            List<EssayLabelDetail> essayLabelDetails = essayLabelDetailRepository.findByTotalIdAndStatus(total.getId(), EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
            List<EssayLabelDetail> detailList = new LinkedList<>();
            EssayLabelDetail titleLabel = new EssayLabelDetail();
            if (CollectionUtils.isNotEmpty(essayLabelDetails)) {
                for (EssayLabelDetail detail : essayLabelDetails) {
                    if (StringUtils.isNotEmpty(detail.getTitleScore())) {
                        titleLabel = detail;
                    } else {
                        detailList.add(detail);
                    }
                }
            }
            String produceXml = labelXmlUtil.produceXml(total, detailList, titleLabel);

            FinalLabelXmlExcelVO vo = FinalLabelXmlExcelVO.builder()
                    .id(total.getId() + "")
                    .content(produceXml)
                    .build();
            labelXmlExcelVOS.add(vo);
        }

//        double page = Math.ceil((double) labelXmlExcelVOS.size() / (double) 3000);
//
//        for (int currentPage = 0; currentPage < page; currentPage++) {
//            writeExcel(labelXmlExcelVOS.subList(currentPage, Math.min((currentPage + 1) * 3000, labelXmlExcelVOS.size() - 1)), currentPage + ".xls");
//        }
//        return null;
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("members", labelXmlExcelVOS);
        map.put("name", "终审批注数据");
        ExcelView excelView = new FinalLabelXmlExcelView();
        return new ModelAndView(excelView, map);
    }


    /**
     * 判断Excel的版本,获取Workbook
     *
     * @return
     * @throws IOException
     */
    public Workbook getWorkbok(File file) throws IOException {
        Workbook wb = null;
        FileInputStream in = new FileInputStream(file);
        if (file.getName().endsWith(EXCEL_XLS)) {     //Excel&nbsp;2003
            wb = new HSSFWorkbook(in);
        } else if (file.getName().endsWith(EXCEL_XLSX)) {    // Excel 2007/2010
            wb = new XSSFWorkbook(in);
        }
        return wb;
    }

    private void writeExcel(List<FinalLabelXmlExcelVO> dataList, String finalXlsxPath) {
        OutputStream out = null;
        try {
            // 读取Excel文档
            File finalXlsxFile = new File(finalXlsxPath);
            if (!finalXlsxFile.exists()) {
                finalXlsxFile.createNewFile();
            }
            Workbook workBook = getWorkbok(finalXlsxFile);
            // sheet 对应一个工作页
            Sheet sheet = workBook.getSheetAt(0);
            /**
             * 删除原有数据，除了属性列
             */
            int rowNumber = sheet.getLastRowNum();    // 第一行从0开始算
            System.out.println("原始数据总行数，除属性列：" + rowNumber);
            for (int i = 1; i <= rowNumber; i++) {
                Row row = sheet.getRow(i);
                sheet.removeRow(row);
            }
            // 创建文件输出流，输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
            out = new FileOutputStream(finalXlsxPath);
            workBook.write(out);
            /**
             * 往Excel中写新数据
             */
            for (int j = 0; j < dataList.size(); j++) {
                // 创建一行：从第二行开始，跳过属性列
                Row row = sheet.createRow(j + 1);
                // 得到要插入的每一条记录
                FinalLabelXmlExcelVO dataCell = dataList.get(j);

                String id = dataCell.getId() + "";
                String content = dataCell.getContent();

                Cell first = row.createCell(0);
                first.setCellValue(id);

                Cell second = row.createCell(1);
                second.setCellValue(content);

            }
            // 创建文件输出流，准备输出电子表格：这个必须有，否则你在sheet上做的任何操作都不会有效
            out = new FileOutputStream(finalXlsxPath);
            workBook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("数据导出成功");
    }


    @Override
    public void fixTitle() {
        List<EssayLabelTotal> totalList = essayLabelTotalRepository.findLabelXml(11464L);

        for (EssayLabelTotal total : totalList) {
            EssayQuestionAnswer answer = essayQuestionAnswerRepository.findOne(total.getAnswerId());

            StringBuilder title = new StringBuilder();
            if (StringUtils.isNotEmpty(answer.getContent())) {
                List<String> paragraphList = cutParagraph(answer.getContent());
                if (CollectionUtils.isNotEmpty(paragraphList)) {
                    for (int i = 0; i < paragraphList.size(); i++) {
                        if (paragraphList.get(i).length() <= 30 && i < 2) {
                            if (title.length() > 0) {
                                title.append("<br/>");
                            }
                            title.append(paragraphList.get(i));
                        }
                    }

                }
            }

            if (StringUtils.isNotEmpty(title.toString())) {
                total.setTitleContent(title.toString());
                essayLabelTotalRepository.save(total);
            }

        }
    }


    @Override
    public void addVIPlLabelTeacher(String teacher) {
        String vipLabelTeacherList = LabelRedisKeyConstant.getVIPLabelTeacherList();
        Long add = redisTemplate.opsForSet().add(vipLabelTeacherList, teacher);
    }

    @Override
    public Object getVIPLabelTeacher() {
        String vipLabelTeacherList = LabelRedisKeyConstant.getVIPLabelTeacherList();
        return redisTemplate.opsForSet().members(vipLabelTeacherList);
    }

    @Override
    public Object getFinalChart(long questionId, long start, long end) {
        Map finalMap = getFinalMap(questionId, start, end);
        return finalMap;
    }

    @Override
    public Boolean checkEdit(long totalId) {
        //TODO 判断是否可编辑
        return true;
    }

    @Override
    public long delVIPlLabelTeacher(String teacher) {
        String vipLabelTeacherListKey = LabelRedisKeyConstant.getVIPLabelTeacherList();
        Long remove = redisTemplate.opsForSet().remove(vipLabelTeacherListKey, teacher);

        return remove;
    }


    public Boolean isVip(String admin) {
        String vipLabelTeacherList = LabelRedisKeyConstant.getVIPLabelTeacherList();
        return redisTemplate.opsForSet().isMember(vipLabelTeacherList, admin);
    }

}
