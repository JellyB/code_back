package com.huatu.tiku.interview.service.impl;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.*;
import com.huatu.tiku.interview.entity.vo.request.PaperCommitVO;
import com.huatu.tiku.interview.entity.vo.request.QuestionCommitVO;
import com.huatu.tiku.interview.entity.vo.response.*;
import com.huatu.tiku.interview.manager.AdviceManager;
import com.huatu.tiku.interview.repository.*;
import com.huatu.tiku.interview.service.LearningSituationMaterialService;
import com.huatu.tiku.interview.service.LearningSituationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.huatu.tiku.interview.constant.InterviewErrors.ANSWER_DATE_NOT_EXIST;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 15:13
 * @Modefied By:
 */
@Service
@Slf4j
public class LearningSituationServiceImpl implements LearningSituationService {

    @Autowired
    private PaperPracticeRepository paperPracticeRepository;
    @Autowired
    private ModulePracticeRepository modulePracticeRepository;
    @Autowired
    private MockPracticeRepository mockPracticeRepository;
    @Autowired
    QuestionAnswerRepository questionAnswerRepository;
    @Autowired
    LearningAdviceRepository learningAdviceRepository;
    @Autowired
    PracticeContentTypeRepository practiceContentTypeRepository;
    @Autowired
    QuestionInfoRepository questionInfoRepository;
    @Autowired
    LearningSituationMaterialService learningSituationMaterialService;
    @Autowired
    LearningReportServiceImpl learningReportServiceImpl;

    /**
     * 模块练习
     * @param modulePractice
     * @return
     */
    @Override
    public boolean saveModulePractice(ModulePractice modulePractice) {
        if(StringUtils.isEmpty(modulePractice.getAnswerDate())){
            log.error(ANSWER_DATE_NOT_EXIST.getMessage());
            throw new BizException(ANSWER_DATE_NOT_EXIST);
        }

        modulePractice.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
        modulePractice.setBizStatus(WXStatusEnum.BizStatus.ONLINE.getBizSatus());

        ModulePractice save = modulePracticeRepository.save(modulePractice);
        return  save == null ?false:true;
    }

    /**
     * 套题演练
     * @param data
     * @return
     */
    @Override
    public Boolean savePaperPractice(PaperPractice data) {
        if(StringUtils.isEmpty(data.getAnswerDate())){
            log.error(ANSWER_DATE_NOT_EXIST.getMessage());
            throw new BizException(ANSWER_DATE_NOT_EXIST);
        }
        data.setCreator("admin");
        data.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
        data.setBizStatus(WXStatusEnum.BizStatus.ONLINE.getBizSatus());

        PaperPractice save = paperPracticeRepository.save(data);
        return  save == null ?false:true;
    }

    /**
     * 全真模考
     * @param vo
     * @return
     */
    @Override
    public boolean saveMockPractice(PaperCommitVO vo) {
        if(StringUtils.isEmpty(vo.getAnswerDate())){
            log.error(ANSWER_DATE_NOT_EXIST.getMessage());
            throw new BizException(ANSWER_DATE_NOT_EXIST);
        }

        MockPractice mockPractice = new MockPractice();
        if(vo.getId() != 0 ){
            questionAnswerRepository.updateToDeleteByMockId(vo.getId());
        }
        BeanUtils.copyProperties(vo,mockPractice);
        mockPractice.setCreator("admin");
        mockPractice.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
        mockPractice.setBizStatus(WXStatusEnum.BizStatus.ONLINE.getBizSatus());
        MockPractice save = mockPracticeRepository.save(mockPractice);
        String openId = vo.getOpenId();
        List<QuestionCommitVO> questionCommitVOList = vo.getQuestionCommitVOList();
        questionCommitVOList.forEach(i -> {
            QuestionAnswer answer =  QuestionAnswer.builder()
                    .mockId(save.getId())
                    .content(i.getAnswer())
                    .openId(openId)
                    .questionId(i.getQuestionId())
                    .build();
            answer.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
            answer.setBizStatus(WXStatusEnum.BizStatus.ONLINE.getBizSatus());
            answer.setCreator(openId);
            answer.setModifier(openId);
            questionAnswerRepository.save(answer);
        });


        return  save == null ?false:true;
    }

    @Override
    public Object detail(String openId, String date) {
        if(StringUtils.isEmpty(date)){

            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.format(d);

        }

        //根据学员当天所有的学习情况
        List<PaperPractice> paperPracticeList = paperPracticeRepository.findByOpenIdAndAnswerDateAndStatus
                (openId, date, WXStatusEnum.Status.NORMAL.getStatus());

        List<ModulePractice> modulePracticeList = modulePracticeRepository.findByOpenIdAndAnswerDateAndStatus
                (openId, date, WXStatusEnum.Status.NORMAL.getStatus());


        // 将当天练习内容放入map中
        Map<Object, List<Object>> map = new HashMap<>();
        if(CollectionUtils.isNotEmpty(modulePracticeList)){
            modulePracticeList.forEach(i -> {
                PracticeContentType practiceType = practiceContentTypeRepository.findOne(i.getPracticeContent());
                String key =  practiceType.getId()+"";
                if(-1 != practiceType.getPid()){
                    key = practiceType.getPid()  + "-"+ practiceType.getId();
                }
                List<Object> practiceList = map.get(key);

                if(CollectionUtils.isEmpty(practiceList)){
                    practiceList = new LinkedList<Object>();
                }
                practiceList.add(i);
                map.put(key,practiceList);
            });

        }
        if(CollectionUtils.isNotEmpty(paperPracticeList)){
            List<Object> practiceList = map.get("13");
            for(PaperPractice i:paperPracticeList){
                if(CollectionUtils.isEmpty(practiceList)){
                    practiceList = new LinkedList<Object>();
                }
                PaperPracticeVO vo = new PaperPracticeVO();
                BeanUtils.copyProperties(i, vo);

                //建议
                vo.setBehaviorAdvice(AdviceManager.getAdvice(i.getBehavior(), 1,learningAdviceRepository));
                vo.setLanguageExpressionAdvice(AdviceManager.getAdvice(i.getLanguageExpression(), 2,learningAdviceRepository));
                vo.setFocusTopicAdvice(AdviceManager.getAdvice(i.getFocusTopic(), 3,learningAdviceRepository));
                vo.setIsOrganizedAdvice(AdviceManager.getAdvice(i.getIsOrganized(), 4,learningAdviceRepository));
                vo.setHaveSubstanceAdvice(AdviceManager.getAdvice(i.getHaveSubstance(), 5,learningAdviceRepository));
                practiceList.add(vo);
            }
            map.put("13",practiceList);

        }
        upMockList(map,openId,date);
        return map;
    }
    @Override
    public Map upMockList(Map<Object, List<Object>> map, String openId,String date) {
        List<MockPractice> mockPracticeList = mockPracticeRepository.findByOpenIdAndAnswerDateAndStatus
                (openId, date, WXStatusEnum.Status.NORMAL.getStatus());

        if(CollectionUtils.isNotEmpty(mockPracticeList)){
            List<PaperDetailVO> paperDetailList = (List<PaperDetailVO>) learningSituationMaterialService.getPaperDetail();

            List<Object> practiceList = map.get("14");
            if(CollectionUtils.isEmpty(practiceList)){
                practiceList = new LinkedList<Object>();
            }

            for(MockPractice i:mockPracticeList){
                MockPracticeVO vo = new MockPracticeVO();
                BeanUtils.copyProperties(i, vo);
                // 表现
                ExpressionVO expressionVO = learningReportServiceImpl.getExpression(vo.getPronunciation(), vo.getFluencyDegree(), vo.getDeportment());
                BeanUtils.copyProperties(expressionVO,vo);

                List<QuestionAnswer> questionAnswerList = questionAnswerRepository.findByMockIdAndStatus(i.getId(), WXStatusEnum.Status.NORMAL.getStatus());
                //查询模考对应试卷信息
                PaperDetailVO paper = learningSituationMaterialService.getPaperDetailById(i.getPaperId());
                //找到试卷对应的答题卡
                List<QuestionDetailVO> questionList = paper.getQuestionList();
                if(CollectionUtils.isNotEmpty(questionList)){
                    for(QuestionDetailVO question:questionList){
                        for(QuestionAnswer answer:questionAnswerList){
                            if(answer.getQuestionId() == question.getId()){
                                String content = answer.getContent();
                                //根据学员作答情况对选项进行标识
                                if(StringUtils.isNotEmpty(content)){
                                    String[] split = content.split(",");
                                    List<ChoiceDetailVO> choiceList = question.getChoiceList();
                                    List<String> userAnswers = Arrays.asList(split);
                                    for(ChoiceDetailVO choice:choiceList){
                                        if(userAnswers.contains(choice.getId()+"")){
                                            choice.setFlag(true);
                                        }else{
                                            choice.setFlag(false);
                                        }
                                    }
                                    question.setChoiceList(choiceList);
                                }
                            }
                        }
                    }
                    vo.setPaper(paper);
                }
                practiceList.add(vo);
            }
            map.put("14",practiceList);

        }

        return map;
    }


//    @Override
//    public PaperPractice findOne(Long id) {
//
//        return learningSituationRepository.findOne(id);
//    }
//    @Override
//    public int  del(Long id) {
//        return learningSituationRepository.updateToDel(id);
//    }
//
//    @Override
//    public List<PaperPractice> findList(String name,Pageable pageRequest) {
//
//        List<PaperPractice> list = learningSituationRepository.findByStatusAndNameLike(WXStatusEnum.Status.NORMAL.getStatus(),"%"+name+"%", pageRequest);
//
//        return list;
//    }
//
//    @Override
//    public long countByNameLikeStatus(String name) {
//        return learningSituationRepository.countByStatusAndNameLike(WXStatusEnum.Status.NORMAL.getStatus(),"%"+name+"%");
//    }


}
