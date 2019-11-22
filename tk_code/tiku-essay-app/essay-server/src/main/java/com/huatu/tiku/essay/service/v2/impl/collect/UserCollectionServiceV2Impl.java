package com.huatu.tiku.essay.service.v2.impl.collect;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.constant.status.EssayUserCollectionConstant.EssayUserCollectionBizStatusEnum;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.v2.collect.UserCollectionServiceV2;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayPaperVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.correct.ResponseExtendVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.*;

/**
 * Created by x6 on 2018/1/30.
 */
@Service
@Slf4j
public class UserCollectionServiceV2Impl implements UserCollectionServiceV2 {

    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssayQuestionDetailRepository questionDetailRepository;
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayUserQuestionCollectionRepository userQuestionCollectionRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EssaySimilarQuestionRepository similarQuestionRepository;
    @Autowired
    EssayUserPaperCollectionRepository userPaperCollectionRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;
    @Autowired
    EssaySimilarQuestionRepository essaySimilarQuestionRepository;
    @Autowired
    EssayMockExamRepository essayMockExamRepository;

    @Autowired
    private EssayPaperService essayPaperService;
    @Autowired
    private EssayQuestionService essayQuestionService;


    @Override
    public Object list(UserSession userSession, int type, int page, int pageSize, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        int userId = userSession.getId();
        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtModify");
        PageUtil pageUtil;

        if (PAPER == type) {
            //根据用户查询收藏的paperBaseId
            List<EssayUserPaperCollection> paperCollections = userPaperCollectionRepository.findByUserIdAndBizStatusAndStatus
                    (userId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus(), pageRequest);
            long count = userPaperCollectionRepository.countByUserIdAndBizStatusAndStatus
                    (userId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());

            LinkedList<EssayPaperVO> paperVOList = new LinkedList<>();
            List<Long> paperIds = paperCollections.stream().map(EssayUserPaperCollection::getPaperBaseId).collect(Collectors.toList());
            Map<Long, List<EssayPaperAnswer>> paperAnswerMap = essayPaperService.convertPaperIds2PaperAnswerMap(userId, paperIds, modeTypeEnum);

            for (EssayUserPaperCollection paperCollection : paperCollections) {
                EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperCollection.getPaperBaseId());
                boolean isOnline = true;
                if (EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus() != paperBase.getStatus()
                        || EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() != paperBase.getBizStatus()) {
                    isOnline = false;
                }
                //根据试卷id查询试卷下题目数
                EssayPaperVO paperVO = EssayPaperVO.builder().paperName(paperBase.getName())
                        .limitTime(paperBase.getLimitTime())
                        .paperId(paperBase.getId())
                        .isOnline(isOnline)
                        .videoAnalyzeFlag(paperBase.getVideoAnalyzeFlag())
                        .isAvailable(true)
                        .areaId(paperBase.getAreaId())
                        //模考卷不支持人工批改
                        .type(paperBase.getType())
                        .build();
                List<EssayPaperAnswer> list = paperAnswerMap.get(paperBase.getId());
                if (CollectionUtils.isNotEmpty(list)) {
                    essayPaperService.buildManualCorrectExtendInfo(list, paperVO);
                }
                //根据试卷id查询用户作答记录
                List<EssayPaperAnswer> answerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndStatusAndTypeAndAnswerCardTypeOrderByGmtCreateDesc
                        (userId, paperBase.getId(), EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(), AdminPaperConstant.TRUE_PAPER,
                                modeTypeEnum.getType());

                if (CollectionUtils.isNotEmpty(answerList)) {
                    essayPaperService.buildManualCorrectExtendInfo(answerList, paperVO);
                }

                paperVO.setCorrectSum(essayPaperAnswerRepository.countByPaperBaseIdAndStatusAndBizStatusAndAnswerCardType(paperBase.getId(),
                        EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                        EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                        modeTypeEnum.getType()
                ).intValue());

                //校验是否是未结束的模考
                if (paperBase.getType() == AdminPaperConstant.MOCK_PAPER) {
                    EssayMockExam mockExam = essayMockExamRepository.findOne(paperCollection.getPaperBaseId());
                    if (mockExam != null && mockExam.getEndTime().getTime() >= System.currentTimeMillis()) {
                        paperVO.setIsAvailable(false);
                    }

                }
                paperVOList.add(paperVO);
            }
            pageUtil = PageUtil.builder().result(paperVOList).next(((int) count) > page * pageSize ? 1 : 0).build();
        } else if (ARGUMENTATION == type || SINGLE_QUESTION == type) {
            List<EssayUserQuestionCollection> questionCollectionList = new LinkedList<>();
            long count = 0L;
            if (ARGUMENTATION == type) {
                //查询议论文题目信息
                //根据用户查询收藏的questionBaseId
                questionCollectionList = userQuestionCollectionRepository.findByUserIdAndQuestionTypeAndBizStatusAndStatus
                        (userId, 5, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus(), pageRequest);
                //收藏数量
                count = userQuestionCollectionRepository.countByUserIdAndQuestionTypeAndBizStatusAndStatus
                        (userId, 5, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
            } else {
                //查询非议论文题目信息
                //根据用户查询收藏的questionBaseId
                questionCollectionList = userQuestionCollectionRepository.findByUserIdAndQuestionTypeNotAndBizStatusAndStatus
                        (userId, 5, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus(), pageRequest);
                //收藏数量
                count = userQuestionCollectionRepository.countByUserIdAndQuestionTypeNotAndBizStatusAndStatus
                        (userId, 5, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());

            }

            List<Long> questionIds = questionCollectionList.stream().map(EssayUserQuestionCollection::getQuestionBaseId).collect(Collectors.toList());

            //2.统计用户所有试题答题卡信息
            List<EssayQuestionAnswer> userAllAnswerCardList = essayQuestionAnswerRepository.findByUserIdAndPaperIdAndStatusAndQuestionBaseIdInAndAnswerCardType(
                    userId, 0L, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), questionIds,
                    modeTypeEnum.getType());

            Map<Long, List<EssayQuestionAnswer>> essayMap = userAllAnswerCardList.parallelStream().collect(Collectors.groupingBy(card -> card.getQuestionBaseId(), Collectors.toList()));
            List<EssayQuestionVO> questionVOList = new LinkedList<EssayQuestionVO>();

            for (EssayUserQuestionCollection questionCollection : questionCollectionList) {
                List<EssayQuestionAnswer> detailList = essayMap.get(questionCollection.getQuestionBaseId());
                ResponseExtendVO responseExtendVO = ResponseExtendVO.builder().build();
                essayQuestionService.dealQuestionResponseExtendInfo(detailList, responseExtendVO);


                LinkedList<EssayQuestionAreaVO> essayQuestionAreaVOList = new LinkedList<EssayQuestionAreaVO>();

                //查询试题地区
                EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(questionCollection.getQuestionBaseId());
                if(null == essayQuestionBase){
                    continue;
                }
                Predicate<EssayQuestionBase> questionOnlinePredict = (i ->
                        essayQuestionBase.getBizStatus() == EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()
                                && essayQuestionBase.getStatus() == EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()
                );

                EssayQuestionAreaVO essayQuestionAreaVO = new EssayQuestionAreaVO();
                BeanUtils.copyProperties(essayQuestionBase, essayQuestionAreaVO);
                if (StringUtils.isNotEmpty(essayQuestionBase.getSubAreaName())) {
                    essayQuestionAreaVO.setAreaId(essayQuestionBase.getSubAreaId());
                    essayQuestionAreaVO.setAreaName(essayQuestionBase.getSubAreaName());
                }
                essayQuestionAreaVO.setQuestionBaseId(essayQuestionBase.getId());
                essayQuestionAreaVO.setQuestionDetailId(essayQuestionBase.getDetailId());
                essayQuestionAreaVO.setCorrectSum(essayQuestionAnswerRepository.countByQuestionBaseIdAndStatusAndBizStatusAndAnswerCardType
                        (essayQuestionBase.getId(),
                                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(), modeTypeEnum.getType()));


                //查询批改次数
                int correctCount = essayQuestionAnswerRepository.countByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType
                        (userId,
                                essayQuestionBase.getId(),
                                0,
                                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                                modeTypeEnum.getType());
                if (0 != correctCount) {
                    essayQuestionAreaVO.setCorrectTimes(correctCount);
                } else {
                    List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndAnswerCardTypeOrderByGmtModifyDesc(
                            userId,
                            essayQuestionBase.getId(),
                            0,
                            EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                            modeTypeEnum.getType());
                    if (CollectionUtils.isNotEmpty(questionAnswerList) && null != questionAnswerList.get(0)) {
                        essayQuestionAreaVO.setBizStatus(questionAnswerList.get(0).getBizStatus());
                    } else {
                        essayQuestionAreaVO.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
                    }
                }


                essayQuestionAreaVOList.add(essayQuestionAreaVO);
                boolean videoAnalyzeFlag = essayQuestionAreaVOList.stream().anyMatch(question -> null != question.getVideoId() && question.getVideoId() > 0);
                EssaySimilarQuestionGroupInfo questionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(questionCollection.getSimilarId());

                //校验题组状态
                Predicate<EssaySimilarQuestionGroupInfo> groupOnlinePredict = (i -> i.getBizStatus() == EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()
                        && i.getStatus() == EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

                EssayQuestionVO questionVO = Optional.ofNullable(questionGroupInfo)
                        .map(i -> EssayQuestionVO.builder()
                                .showMsg(i.getShowMsg())
                                .similarId(i.getId())
                                .essayQuestionBelongPaperVOList(essayQuestionAreaVOList)
                                .type(i.getType())
                                .isOnline(questionOnlinePredict.test(essayQuestionBase) && groupOnlinePredict.test(i))
                                .videoAnalyzeFlag(videoAnalyzeFlag)
                                .build())
                        .orElseGet(()->{
                            long detailId = essayQuestionBase.getDetailId();
                            EssayQuestionDetail detail = essayQuestionService.findQuestionDetailById(detailId);
                            return EssayQuestionVO.builder()
                                    .showMsg(detail.getStem())
                                    .similarId(-1L)
                                    .essayQuestionBelongPaperVOList(essayQuestionAreaVOList)
                                    .type(detail.getType())
                                    .isOnline(questionOnlinePredict.test(essayQuestionBase))
                                    .videoAnalyzeFlag(videoAnalyzeFlag)
                                    .build();
                        });


                //组装试卷信息
                BeanUtils.copyProperties(responseExtendVO, questionVO);
                questionVOList.add(questionVO);
            }

            pageUtil = PageUtil.builder().result(questionVOList).next(((int) count) > page * pageSize ? 1 : 0).build();
        } else {
            log.warn("操作类型异常。userId:{},type:{}", userId, type);
            throw new BizException(EssayUserCollectionErrors.ERROR_SAVE_TYPE);
        }
        return pageUtil;
    }

}
