package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.constant.cache.RedisKeyConstant;
import com.huatu.tiku.interview.entity.po.*;
import com.huatu.tiku.interview.entity.vo.response.*;
import com.huatu.tiku.interview.repository.*;
import com.huatu.tiku.interview.service.LearningSituationMaterialService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by x6 on 2018/4/11.
 */
@Service
public class LearningSituationMaterialServiceImpl implements LearningSituationMaterialService {
    @Autowired
    PracticeContentTypeRepository practiceContentTypeRepository;
    @Autowired
    PracticeRemarkRepository practiceRemarkRepository;
    @Autowired
    RemarkWordRepository remarkWordRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    PracticeExpressionRepository practiceExpressionRepository;
    @Autowired
    PaperInfoRepository paperInfoRepository;
    @Autowired
    QuestionInfoRepository questionInfoRepository;
    @Autowired
    ChoiceInfoRepository choiceInfoRepository;

    @Override
    public Object getPracticeContent() {
        //优先从缓存中取数据
        String practiceContentTypeKey = RedisKeyConstant.PRACTICE_CONTENT_TYPE;
        List<PracticeContentTypeVO> list = (List<PracticeContentTypeVO>)redisTemplate.opsForValue().get(practiceContentTypeKey);
        if(CollectionUtils.isEmpty(list)){
            List<PracticeContentTypeVO> practiceContentTypeVOList = new LinkedList<>();
            //查询所有的联系内容
            List<PracticeContentType> practiceContentTypes = practiceContentTypeRepository.findByStatusOrderBySortAsc(1);
            practiceContentTypes.forEach(i -> {
                //一级练习内容
                if(i.getPid() == -1){
                    PracticeContentTypeVO.PracticeContentTypeVOBuilder builder =  PracticeContentTypeVO.builder()
                            .name(i.getName())
                            .id(i.getId());
                    LinkedList<PracticeContentTypeVO> subList = new LinkedList<>();
                    practiceContentTypes.forEach(j -> {
                        //查询练习内容的子模块列表
                        if(j.getPid() == i.getId()){
                            PracticeContentTypeVO subVO =  PracticeContentTypeVO.builder()
                                    .name(j.getName())
                                    .id(j.getId())
                                    .build();
                            subList.add(subVO);
                        }
                    });
                    PracticeContentTypeVO contentTypeVO = builder.subList(subList)
                            .build();
                    practiceContentTypeVOList.add(contentTypeVO);
                }
            });
            //不为空才放缓存
            if(CollectionUtils.isNotEmpty(practiceContentTypeVOList)){
                redisTemplate.opsForValue().set(practiceContentTypeKey,practiceContentTypeVOList);
                redisTemplate.expire(practiceContentTypeKey,7, TimeUnit.DAYS);
            }
            return practiceContentTypeVOList;
        }
        return list;
    }

    @Override
    public Object getRemarkList(long typeId) {
        String practiceContentRemarkKey = RedisKeyConstant.getPracticeContentTypeKey(typeId);
        List<PracticeRemark> list = (List<PracticeRemark>)redisTemplate.opsForValue().get(practiceContentRemarkKey);
        if(CollectionUtils.isEmpty(list)){
            //查询所有的联系内容
            list = practiceRemarkRepository.findByPracticeContentId(typeId, new Sort(Sort.Direction.ASC, "id"));
            //不为空才放缓存
            if(CollectionUtils.isNotEmpty(list)){
                redisTemplate.opsForValue().set(practiceContentRemarkKey,list);
                redisTemplate.expire(practiceContentRemarkKey,7, TimeUnit.DAYS);
            }
        }

        List<PracticeRemark> advantageList = new LinkedList<PracticeRemark>();
        List<PracticeRemark> disAdvantageList = new LinkedList<PracticeRemark>();
        list.forEach(i -> {
            if(i.getType() == 1){
                advantageList.add(i);
            }else if(i.getType() == 2){
                disAdvantageList.add(i);
            }

        });
        return  RemarkListVO.builder()
                .advantageList(advantageList)
                .disAdvantageList(disAdvantageList)
                .build();
    }

    @Override
    public Object getWordList() {
        String remarkWordKey = RedisKeyConstant.REMARK_WORD;
        List<String> wordList = (List<String>)redisTemplate.opsForValue().get(remarkWordKey);
        if(CollectionUtils.isEmpty(wordList)){
            List newList = new LinkedList<>();
            List<RemarkWord> remarkWordList = remarkWordRepository.findByStatus(WXStatusEnum.Status.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "sort"));

            remarkWordList.forEach(i -> {
                newList.add(i.getContent());
            });
            //不为空才放缓存
            if(CollectionUtils.isNotEmpty(newList)){
                redisTemplate.opsForValue().set(remarkWordKey,newList);
                redisTemplate.expire(remarkWordKey,7, TimeUnit.DAYS);
            }
            return newList;
        }

        return wordList;
    }

    @Override
    public Object getExpressionList() {
        String expressionKey = RedisKeyConstant.EXPRESSION;
        List<PracticeExpression> expressionList = (List<PracticeExpression>)redisTemplate.opsForValue().get(expressionKey);
        if(CollectionUtils.isEmpty(expressionList)){
            expressionList = practiceExpressionRepository.findAll();
            if(CollectionUtils.isNotEmpty(expressionList)){
                redisTemplate.opsForValue().set(expressionKey,expressionList);
            }
        }
        List<PracticeExpression> pronunciationList = new LinkedList<PracticeExpression>();
        List<PracticeExpression> fluencyDegreeList = new LinkedList<PracticeExpression>();
        List<PracticeExpression> deportmentList = new LinkedList<PracticeExpression>();


        expressionList.forEach(i -> {
            if(i.getType() == 1){
                pronunciationList.add(i);
            }else if(i.getType() == 2){
                fluencyDegreeList.add(i);
            }else if(i.getType() == 3){
                deportmentList.add(i);
            }
        });
        return  ExpressionVO.builder()
                .pronunciationList(pronunciationList)
                .fluencyDegreeList(fluencyDegreeList)
                .deportmentList(deportmentList)
                .build();
    }

    @Override
    public Object getPaperDetail() {
        List<PaperDetailVO> paperDetailVOS = new LinkedList<>();

        String mockPaperListKey = RedisKeyConstant.MOCK_PAPER_LIST;
        paperDetailVOS = (List<PaperDetailVO>)redisTemplate.opsForValue().get(mockPaperListKey);
        if(CollectionUtils.isEmpty(paperDetailVOS)){
            List<PaperInfo> paperList = paperInfoRepository.findByExamTypeAndStatus(2, WXStatusEnum.Status.NORMAL.getStatus());
            if(CollectionUtils.isEmpty(paperList)){
                return ListUtils.EMPTY_LIST;
            }
            List<PaperDetailVO> paperDetailVOList = new LinkedList<>();
            paperList.forEach(i ->{
                PaperDetailVO paperDetailVO = getPaperDetailById(i.getId());
                paperDetailVOList.add(paperDetailVO);

            });
            if(CollectionUtils.isNotEmpty(paperDetailVOList)){
                redisTemplate.opsForValue().set(mockPaperListKey,paperDetailVOList);
            }
            return paperDetailVOList;
        }
        return paperDetailVOS;
    }

    @Override
    public PaperDetailVO getPaperDetailById(long id){
        PaperInfo i = paperInfoRepository.findOne(id);
        PaperDetailVO.PaperDetailVOBuilder paperBuilder =  PaperDetailVO.builder()
                .id(i.getId())
                .paperName(i.getPaperName())
                .type(i.getType());
        //构建试题列表
        LinkedList<QuestionDetailVO> questionDetailVOList = new LinkedList<>();
        List<QuestionInfo> questionInfoList = questionInfoRepository.findByPaperIdAndStatus
                (i.getId(), WXStatusEnum.Status.NORMAL.getStatus());
        if(CollectionUtils.isNotEmpty(questionInfoList)){
            LinkedList<Long> questionIds = new LinkedList<>();
            questionInfoList.forEach(j -> {
                questionIds.add(j.getId());
            });
            List<ChoiceInfo> choiceInfoList = choiceInfoRepository.findByQuestionIdInAndStatus(questionIds, WXStatusEnum.Status.NORMAL.getStatus());

            for(QuestionInfo questionInfo:questionInfoList){
                //构建选项列表
                LinkedList<ChoiceDetailVO> choiceDetailList = new LinkedList<>();
                choiceInfoList.forEach(k -> {

                    if(k.getQuestionId() == questionInfo.getId()){
                        ChoiceDetailVO.ChoiceDetailVOBuilder choiceVOBuilder =  ChoiceDetailVO.builder();
                        choiceVOBuilder.id(k.getId())
                                .content(k.getContent())
                                .sort(k.getSort())
                                .questionId(k.getQuestionId());
                        choiceDetailList.add(choiceVOBuilder.build());
                    }
                });

                QuestionDetailVO questionDetailVO =  QuestionDetailVO.builder()
                        .id(questionInfo.getId())
                        .choiceList(choiceDetailList)
                        .stem(questionInfo.getStem())
                        .questionType(questionInfo.getQuestionType())
                        .build();
                questionDetailVOList.add(questionDetailVO);
            }
        }
        PaperDetailVO paperDetailVO = paperBuilder.questionList(questionDetailVOList)
                .build();
        return paperDetailVO;
    }
}
