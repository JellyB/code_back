package com.huatu.tiku.essay.service.impl;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.constant.status.EssayUserCollectionConstant.EssayUserCollectionBizStatusEnum;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayPaperVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.UserCollectionService;
import com.huatu.tiku.essay.util.PageUtil;
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

import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.*;

/**
 * Created by x6 on 2018/1/30.
 */
@Service
@Slf4j
public class UserCollectionServiceImpl implements UserCollectionService {

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
    /**
     * 收藏题目
     *
     * @param userId
     * @param type
     * @param baseId
     * @return
     */
    @Override
    public Object saveCollection(int userId, int type, long baseId, long similarId) {

        //收藏单题
        if (SINGLE_QUESTION == type || ARGUMENTATION == type){

            //校验题目可用性
            EssaySimilarQuestionGroupInfo essaySimilarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(similarId);
            List<EssaySimilarQuestion> similarQuestions = similarQuestionRepository.findByQuestionBaseIdAndSimilarIdAndStatus
                    (baseId, similarId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(similarQuestions) || null == essaySimilarQuestionGroupInfo) {
                log.warn("试题已删除或已下线，请刷新试题列表后再进行操作,userId：{}，questionBaseId：{}，similarId：{}",userId,baseId,similarId);
                throw new BizException(EssayUserCollectionErrors.QUESTION_NOT_EXIST);
            } else if (similarQuestions.size() == 1 && null != essaySimilarQuestionGroupInfo) {
                //查询 校验是否已经收藏过
                List<EssayUserQuestionCollection> userQuestionCollections = userQuestionCollectionRepository.findByUserIdAndSimilarIdAndQuestionBaseId(
                        userId, similarId, baseId);
                if (CollectionUtils.isEmpty(userQuestionCollections)) {
                    EssayUserQuestionCollection userCollection = EssayUserQuestionCollection.builder()
                            .userId(userId)
                            .questionBaseId(similarQuestions.get(0).getQuestionBaseId())
                            .similarId(similarQuestions.get(0).getSimilarId())
                            .questionType(essaySimilarQuestionGroupInfo.getPType())
                            .build();
                    userCollection.setBizStatus(EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus());
                    userCollection.setStatus(EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
                    userCollection = userQuestionCollectionRepository.save(userCollection);

                    if (null == userCollection) {
                        log.warn("收藏单题失败。userId：{}，questionBaseId：{}，similarId：{}",userId,baseId,similarId);
                        throw new BizException(EssayUserCollectionErrors.COLLECT_FAIL);
                    }else{
                        return userCollection;
                    }
                } else {
                    userQuestionCollections.get(0).setBizStatus(EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus());
                    userQuestionCollections.get(0).setStatus(EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
                    EssayUserQuestionCollection userCollection = userQuestionCollectionRepository.save(userQuestionCollections.get(0));
                    if (null == userCollection) {
                        log.warn("题目收藏，修改状态失败，userId:{},questionBaseId:{},similarId:{}",userId,baseId,similarId);
                        throw new BizException(EssayUserCollectionErrors.COLLECT_UPDATE_FAIL);
                    }else{
                        return userCollection;
                    }
                }
            } else  {
                log.warn("题目数据异常，共查询到题目数：{}。questionBaseId：{}，similarId：{}，",similarQuestions.size(),baseId,similarId);
                throw new BizException(EssayUserCollectionErrors.SIMILAR_QUESTION_REPETITION_ERROR);
            }
        }

        //收藏套题
        else if (PAPER == type) {
            //校验试卷可用性
            EssayPaperBase paper = essayPaperBaseRepository.findOne(baseId);
            if(9999 == paper.getAreaId()){
                log.warn("估分试卷暂不支持收藏，paperId：{}",baseId);
                throw new BizException(EssayUserCollectionErrors.GUFEN_CANOT_COLLECT);
            }
            if (null == paper || paper.getStatus() != EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus()
                    || paper.getBizStatus() != EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()) {
                log.warn("试卷已删除或已下线，请刷新试卷列表后再进行操作。userId：{}，paperId：{}",userId,baseId);
                throw new BizException(EssayUserCollectionErrors.PAPER_NOT_EXIST);
            }

            List<EssayUserPaperCollection> paperCollections = userPaperCollectionRepository.findByUserIdAndPaperBaseId(userId, baseId);
            if (CollectionUtils.isEmpty(paperCollections)) {
                EssayUserPaperCollection userCollection = EssayUserPaperCollection.builder()
                        .paperBaseId(baseId)
                        .userId(userId)
                        .build();
                userCollection.setStatus(EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
                userCollection.setBizStatus(EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus());
                userCollection = userPaperCollectionRepository.save(userCollection);
                if (null == userCollection) {
                    log.warn("试卷收藏失败。userId：{}，paperBaseId：{}",userId,baseId);
                    throw new BizException(EssayUserCollectionErrors.COLLECT_FAIL);
                }else{
                    return userCollection;
                }
            } else {
                paperCollections.get(0).setBizStatus(EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus());
                paperCollections.get(0).setStatus(EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
                EssayUserPaperCollection userCollection = userPaperCollectionRepository.save(paperCollections.get(0));
                if (null == userCollection) {
                    log.info("试卷收藏，修改状态失败，userId:{},paperBaseId:{}",userId,baseId);
                    throw new BizException(EssayUserCollectionErrors.COLLECT_FAIL);
                }else{
                    return userCollection;
                }
            }
        }else {
            log.warn("操作类型异常。userId:{},type:{}",userId,type);
            throw new BizException(EssayUserCollectionErrors.ERROR_SAVE_TYPE);
        }
    }


    /**
     *
     * @param userId
     * @param baseId
     * @param type
     * @return
     */
    @Override
    public Object delCollection(int userId, long baseId,int type, long similarId) {
        //单题
        if (SINGLE_QUESTION == type || ARGUMENTATION == type) {
            //查询是否存在可用的收藏记录
            List<EssayUserQuestionCollection> userQuestionCollections = userQuestionCollectionRepository.findByUserIdAndSimilarIdAndQuestionBaseIdAndBizStatusAndStatus(
                    userId, similarId, baseId,EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(),EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());

            if(CollectionUtils.isNotEmpty(userQuestionCollections)){
                for(EssayUserQuestionCollection questionCollection:userQuestionCollections){
                    int i = userQuestionCollectionRepository.upToDelete(questionCollection.getId());
                }
            }
        }
        //套题
        else if (PAPER == type) {
            //查询是否存在可用的收藏记录
            List<EssayUserPaperCollection> essayUserPaperCollectionList = userPaperCollectionRepository.findByUserIdAndPaperBaseIdAndBizStatusAndStatus
                    (userId, baseId,EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(),EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());

            if(CollectionUtils.isNotEmpty(essayUserPaperCollectionList)){
                for(EssayUserPaperCollection paperCollection :essayUserPaperCollectionList){
                    int i = userPaperCollectionRepository.upToDelete(paperCollection.getId());
                }
            }
        }else {
            log.warn("操作类型异常。userId:{},type:{}",userId,type);
            throw new BizException(EssayUserCollectionErrors.ERROR_SAVE_TYPE);
        }
        return  EssayUpdateVO.builder().flag(true).build();
    }



    @Override
    public Object listV2(UserSession userSession, int type, int page, int pageSize, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        int userId = userSession.getId();
        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtModify");
        PageUtil pageUtil ;

        if(PAPER == type){
            //根据用户查询收藏的paperBaseId
             List<EssayUserPaperCollection> paperCollections = userPaperCollectionRepository.findByUserIdAndBizStatusAndStatus
                     (userId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(),EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus(),pageRequest);
             long count = userPaperCollectionRepository.countByUserIdAndBizStatusAndStatus
                     (userId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
             LinkedList<EssayPaperVO> paperVOList = new LinkedList<>();

             for(EssayUserPaperCollection paperCollection:paperCollections){
                 EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperCollection.getPaperBaseId());
                 boolean isOnline = true;
                 if(EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus() != paperBase.getStatus()
                    || EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() != paperBase.getBizStatus()){
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
                         .build();
                 //根据试卷id查询用户作答记录
                 List<EssayPaperAnswer> answerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndBizStatusAndStatusAndTypeAndAnswerCardTypeOrderByGmtCreateDesc
                         (userId,
                                 paperBase.getId(),
                                 EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                                 EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                                 AdminPaperConstant.TRUE_PAPER,
                                 modeTypeEnum.getType());
                 if(CollectionUtils.isNotEmpty(answerList)){
                     paperVO.setCorrectNum(answerList.size());
                 }else{
                     List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndStatusAndTypeAndAnswerCardTypeOrderByGmtCreateDesc
                             (userId, paperBase.getId(), EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),AdminPaperConstant.TRUE_PAPER,
                                     modeTypeEnum.getType());
                     if (CollectionUtils.isNotEmpty(paperAnswerList) && null != paperAnswerList.get(0)) {
                         paperVO.setRecentStatus(paperAnswerList.get(0).getBizStatus());
                     } else {
                         paperVO.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
                     }
                 }

                 paperVO.setCorrectSum(essayPaperAnswerRepository.countByPaperBaseIdAndStatusAndBizStatusAndAnswerCardType(paperBase.getId(),
                         EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                         EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                         modeTypeEnum.getType()).intValue());

                 //校验是否是未结束的模考
                 if(paperBase.getType() == AdminPaperConstant.MOCK_PAPER){
                     EssayMockExam mockExam = essayMockExamRepository.findOne(paperCollection.getPaperBaseId());
                     if(mockExam != null && mockExam.getEndTime().getTime() >= System.currentTimeMillis()){
                        paperVO.setIsAvailable(false);
                     }

                 }
                 paperVOList.add(paperVO);
             }
              pageUtil = PageUtil.builder().result(paperVOList).next(((int)count) > page*pageSize ? 1 : 0).build();
        }else if(ARGUMENTATION == type || SINGLE_QUESTION == type){
            List<EssayUserQuestionCollection> questionCollectionList = new LinkedList<>();
            long count = 0L;
            if(ARGUMENTATION == type ){
                //查询议论文题目信息
                 //根据用户查询收藏的questionBaseId
                 questionCollectionList = userQuestionCollectionRepository.findByUserIdAndQuestionTypeAndBizStatusAndStatus
                         (userId,5, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus(), pageRequest);
                 //收藏数量
                 count = userQuestionCollectionRepository.countByUserIdAndQuestionTypeAndBizStatusAndStatus
                         (userId, 5,EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
            }else{
                //查询非议论文题目信息
                 //根据用户查询收藏的questionBaseId
                 questionCollectionList = userQuestionCollectionRepository.findByUserIdAndQuestionTypeNotAndBizStatusAndStatus
                         (userId,5, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus(), pageRequest);
                 //收藏数量
                 count = userQuestionCollectionRepository.countByUserIdAndQuestionTypeNotAndBizStatusAndStatus
                         (userId, 5,EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());

            }

             List<EssayQuestionVO> questionVOList = new LinkedList<EssayQuestionVO>();

             for(EssayUserQuestionCollection questionCollection:questionCollectionList){
                 boolean isOnline = true;
                 EssaySimilarQuestionGroupInfo questionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(questionCollection.getSimilarId());
                 //校验题组状态
                 if(questionGroupInfo.getBizStatus() != EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()
                         || questionGroupInfo.getStatus() != EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()){
                     isOnline = false;
                 }

                 LinkedList<EssayQuestionAreaVO> essayQuestionAreaVOList = new LinkedList<EssayQuestionAreaVO>();

                 //查询试题地区
                 EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(questionCollection.getQuestionBaseId());
                 if(essayQuestionBase.getBizStatus() != EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()
                         || essayQuestionBase.getStatus() != EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()){
                     isOnline = false;
                 }
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
                                 EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                                 modeTypeEnum.getType()));

                 //查询批改次数
                 int correctCount = essayQuestionAnswerRepository.countByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType
                         (userId, essayQuestionBase.getId(),
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

                 //组装试卷信息
                 EssayQuestionVO questionVO = EssayQuestionVO.builder()
                         .showMsg(questionGroupInfo.getShowMsg())
                         .similarId(questionGroupInfo.getId())
                         .essayQuestionBelongPaperVOList(essayQuestionAreaVOList)
                         .type(questionGroupInfo.getType())
                         .isOnline(isOnline)
                         .videoAnalyzeFlag(videoAnalyzeFlag)
                         .build();

                 questionVOList.add(questionVO);
             }

              pageUtil = PageUtil.builder().result(questionVOList).next(((int)count) > page*pageSize ? 1 : 0).build();
        }else{
             log.warn("操作类型异常。userId:{},type:{}",userId,type);
             throw new BizException(EssayUserCollectionErrors.ERROR_SAVE_TYPE);
        }
        return pageUtil;
    }


    @Override
    public EssayUpdateVO check(UserSession userSession, long baseId, int type, long similarId) {
        int userId = userSession.getId();
        boolean flag = false;
        if(PAPER == type){
            List<EssayUserPaperCollection> paperCollections = userPaperCollectionRepository.findByUserIdAndPaperBaseIdAndBizStatusAndStatus
                    (userId, baseId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
            if(CollectionUtils.isNotEmpty(paperCollections)){
                flag = true;
            }

        }else if(SINGLE_QUESTION == type || ARGUMENTATION == type){
            List<EssayUserQuestionCollection> questionCollections = userQuestionCollectionRepository.findByUserIdAndSimilarIdAndQuestionBaseIdAndBizStatusAndStatus
                    (userId, similarId, baseId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
            if(CollectionUtils.isNotEmpty(questionCollections)){
                flag = true;
            }
        }else{
            log.warn("操作类型异常。userId:{},type:{}",userId,type);
            throw new BizException(EssayUserCollectionErrors.ERROR_SAVE_TYPE);
        }
        return EssayUpdateVO.builder()
                .flag(flag)
                .build();
    }

    @Override
    public EssayUpdateVO status(UserSession userSession, long baseId, int type, long similarId) {
        int userId = userSession.getId();
        boolean flag = false;

        if(PAPER == type){
            EssayPaperBase paperBase = essayPaperBaseRepository.findOne(baseId);
             if(null != paperBase
                     && paperBase.getBizStatus() != EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()
                     && paperBase.getStatus() != EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus()){
                flag = true;
            }else{
                 log.warn("试卷已经删除或下线，paperId：{}",baseId);
                 throw new BizException(EssayErrors.ESSAY_PAPER_OFFLINE);
             }

        }else if(SINGLE_QUESTION == type){
            //校验题组状态
            EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(similarId);
            if(null != similarQuestionGroupInfo
                    && similarQuestionGroupInfo.getBizStatus() != EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()
                    && similarQuestionGroupInfo.getStatus() != EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus()){
                List<EssaySimilarQuestion> similarQuestions = essaySimilarQuestionRepository.findByQuestionBaseIdAndSimilarIdAndStatus
                        (baseId, similarId,EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
                if(CollectionUtils.isNotEmpty(similarQuestions) && similarQuestions.size() == 1){
                    EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(baseId);
                    if(null != questionBase
                            && questionBase.getBizStatus() != EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()
                            && questionBase.getStatus() != EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()){
                        flag = true;
                    }else{
                        log.warn("试题已经删除或下线，similarId：{},baseId:{}",similarId,baseId);
                        throw new BizException(EssayErrors.ESSAY_QUESTION_OFFLINE);
                    }

                }else{
                    log.warn("题目已从题组中删除或下线，similarId：{},baseId:{}",similarId,baseId);
                    throw new BizException(EssayErrors.ESSAY_QUESTION_GROUP_UNCONNECT);
                }
            }else{
                log.warn("题组已经删除或下线，similarId：{}",similarId);
                throw new BizException(EssayErrors.ESSAY_QUESTION_GROUP_OFFLINE);
            }
        }else{
            log.warn("操作类型异常。userId:{},type:{}",userId,type);
            throw new BizException(EssayUserCollectionErrors.ERROR_SAVE_TYPE);
        }
        return EssayUpdateVO.builder()
                .flag(flag)
                .build();
    }


    public Object listV1(UserSession userSession, int type, int page, int pageSize, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        int userId = userSession.getId();
        PageRequest pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtModify");
        PageUtil pageUtil ;

        if(PAPER == type){
            //根据用户查询收藏的paperBaseId
            List<EssayUserPaperCollection> paperCollections = userPaperCollectionRepository.findByUserIdAndBizStatusAndStatus
                    (userId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(),EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus(),pageRequest);
            long count = userPaperCollectionRepository.countByUserIdAndBizStatusAndStatus
                    (userId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
            LinkedList<EssayPaperVO> paperVOList = new LinkedList<>();

            for(EssayUserPaperCollection paperCollection:paperCollections){
                EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperCollection.getPaperBaseId());
                boolean isOnline = true;
                if(EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus() != paperBase.getStatus()
                        || EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus() != paperBase.getBizStatus()){
                    isOnline = false;
                }

                //根据试卷id查询试卷下题目数
                EssayPaperVO paperVO = EssayPaperVO.builder().paperName(paperBase.getName())
                        .limitTime(paperBase.getLimitTime())
                        .paperId(paperBase.getId())
                        .isOnline(isOnline)
                        .build();
                //根据试卷id查询用户作答记录
                List<EssayPaperAnswer> answerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndBizStatusAndStatusAndTypeAndAnswerCardTypeOrderByGmtCreateDesc
                        (userId,
                                paperBase.getId(),
                                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                                EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                                AdminPaperConstant.TRUE_PAPER,
                                modeTypeEnum.getType());
                if(CollectionUtils.isNotEmpty(answerList)){
                    paperVO.setCorrectNum(answerList.size());
                }else{
                    List<EssayPaperAnswer> paperAnswerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndStatusAndTypeAndAnswerCardTypeOrderByGmtCreateDesc
                            (userId, paperBase.getId(), EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),AdminPaperConstant.TRUE_PAPER,
                                    modeTypeEnum.getType());
                    if (CollectionUtils.isNotEmpty(paperAnswerList) && null != paperAnswerList.get(0)) {
                        paperVO.setRecentStatus(paperAnswerList.get(0).getBizStatus());
                    } else {
                        paperVO.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
                    }
                }

                paperVO.setCorrectSum(essayPaperAnswerRepository.countByPaperBaseIdAndStatusAndAnswerCardType(paperBase.getId(), EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus(),
                        modeTypeEnum.getType()).intValue());

                paperVOList.add(paperVO);
            }
            pageUtil = PageUtil.builder().result(paperVOList).next(((int)count) > page*pageSize ? 1 : 0).build();
        }else if(SINGLE_QUESTION == type){
            //根据用户查询收藏的questionBaseId
            List<EssayUserQuestionCollection> questionCollectionList = userQuestionCollectionRepository.findByUserIdAndBizStatusAndStatus
                    (userId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus(), pageRequest);
            //收藏数量
            long count = userQuestionCollectionRepository.countByUserIdAndBizStatusAndStatus
                    (userId, EssayUserCollectionBizStatusEnum.ONLINE.getBizStatus(), EssayUserCollectionConstant.EssayUserCollectionStatusEnum.NORMAL.getStatus());
            List<EssayQuestionVO> questionVOList = new LinkedList<EssayQuestionVO>();

            for(EssayUserQuestionCollection questionCollection:questionCollectionList){
                boolean isOnline = true;
                EssaySimilarQuestionGroupInfo questionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(questionCollection.getSimilarId());
                //校验题组状态
                if(questionGroupInfo.getBizStatus() != EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()
                        || questionGroupInfo.getStatus() != EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()){
                    isOnline = false;
                }

                LinkedList<EssayQuestionAreaVO> essayQuestionAreaVOList = new LinkedList<EssayQuestionAreaVO>();

                //查询试题地区
                EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(questionCollection.getQuestionBaseId());
                if(essayQuestionBase.getBizStatus() != EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()
                        || essayQuestionBase.getStatus() != EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()){
                    isOnline = false;
                }
                EssayQuestionAreaVO essayQuestionAreaVO = new EssayQuestionAreaVO();
                BeanUtils.copyProperties(essayQuestionBase, essayQuestionAreaVO);
                if (StringUtils.isNotEmpty(essayQuestionBase.getSubAreaName())) {
                    essayQuestionAreaVO.setAreaId(essayQuestionBase.getSubAreaId());
                    essayQuestionAreaVO.setAreaName(essayQuestionBase.getSubAreaName());
                }
                essayQuestionAreaVO.setQuestionBaseId(essayQuestionBase.getId());
                essayQuestionAreaVO.setQuestionDetailId(essayQuestionBase.getDetailId());
                essayQuestionAreaVO.setCorrectSum(essayQuestionAnswerRepository.countByQuestionBaseIdAndStatusAndAnswerCardType
                        (essayQuestionBase.getId(), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),modeTypeEnum.getType()));

                //查询批改次数
                int correctCount = essayQuestionAnswerRepository.countByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType
                        (userId, essayQuestionBase.getId(), 0, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
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

                //组装试卷信息
                EssayQuestionVO questionVO = EssayQuestionVO.builder()
                        .showMsg(questionGroupInfo.getShowMsg())
                        .similarId(questionGroupInfo.getId())
                        .essayQuestionBelongPaperVOList(essayQuestionAreaVOList)
                        .isOnline(isOnline)
                        .type(questionGroupInfo.getType())
                        .build();

                questionVOList.add(questionVO);
            }

            pageUtil = PageUtil.builder().result(questionVOList).next(((int)count) > page*pageSize ? 1 : 0).build();
        }else{
            log.warn("操作类型异常。userId:{},type:{}",userId,type);
            throw new BizException(EssayUserCollectionErrors.ERROR_SAVE_TYPE);
        }
        return pageUtil;
    }





}
