package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.entity.po.*;
import com.huatu.tiku.interview.entity.vo.response.*;
import com.huatu.tiku.interview.entity.vo.response.excel.ExcelView;
import com.huatu.tiku.interview.entity.vo.response.excel.MockPracticeExcelView;
import com.huatu.tiku.interview.entity.vo.response.excel.ModulePracticeExcelView;
import com.huatu.tiku.interview.entity.vo.response.excel.PaperPracticeExcelView;
import com.huatu.tiku.interview.manager.AdviceManager;
import com.huatu.tiku.interview.repository.*;
import com.huatu.tiku.interview.repository.impl.UserRepositoryImpl;
import com.huatu.tiku.interview.service.ImportDataService;
import com.huatu.tiku.interview.service.LearningSituationMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

/**
 * @author zhaoxi
 * @Description: 导出学员每日练习之后的测评报告
 *      一次性代码  看到不要喷
 * @date 2018/7/25下午7:56
 */
@Service
@Slf4j
public class ImportDataServiceImpl implements ImportDataService {

    @Autowired
    LearningSituationMaterialService learningSituationMaterialService;
    @Autowired
    private UserRepositoryImpl userRepositoryImpl;
    @Autowired
    private ModulePracticeRepository modulePracticeRepository;
    @Autowired
    private MockPracticeRepository mockPracticeRepository;
    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;
    @Autowired
    private LearningAdviceRepository learningAdviceRepository;
    @Autowired
    private PaperPracticeRepository paperPracticeRepository;
    @Autowired
    private ChoiceInfoRepository choiceInfoRepository;
    

    @Override
    public ModelAndView importData(Long type) {

        Map<String, Object> map = new HashMap<String, Object>();

        List list = new LinkedList<>();

        //1。查询所有题目类型，并处理题型名称
        Map<Long, String> allType = getAllType();
        //2。查询所有可用学员信息
        Map<String, UserExcelVO> allUser = getAllUser();
        //3。遍历所有题型，查询答题记录
        String typeName = allType.get(type);
        map.put("name", typeName+"测评报告整理");

        if(type == 13){
            //套题演练
            List<PaperPractice> paperPractices = paperPracticeRepository.findByStatusOrderByAnswerDateAsc(1);
            if(CollectionUtils.isNotEmpty(paperPractices)){
                for(PaperPractice paperPractice:paperPractices){
                    log.info("========openId:{}========",paperPractice.getOpenId());
                    UserExcelVO userExcelVO = allUser.get(paperPractice.getOpenId());
                    if(userExcelVO != null){
                        PaperPracticeExcelVO excelVO = new PaperPracticeExcelVO();
                        PaperRemarkExcelVO paperRemark = getPaperRemark(paperPractice);

                        BeanUtils.copyProperties(userExcelVO,excelVO);
                        BeanUtils.copyProperties(paperRemark,excelVO);
                        excelVO.setAnswerDate(paperPractice.getAnswerDate());
                        excelVO.setElseRemark(paperPractice.getElseRemark());
                        list.add(excelVO);
                    }
                }
            }

            
            
            map.put("members", list);
            ExcelView excelView = new PaperPracticeExcelView();
            return new ModelAndView(excelView, map);

        }else if(type == 14){
            //全真模拟

            List<MockPractice> mockPracticeList = mockPracticeRepository.findByStatusOrderByAnswerDateAsc(1);
            if(CollectionUtils.isNotEmpty(mockPracticeList)){
                for(MockPractice mockPractice:mockPracticeList){
                    log.info("========openId:{}========",mockPractice.getOpenId());
                    UserExcelVO userExcelVO = allUser.get(mockPractice.getOpenId());
                    if(userExcelVO != null){
                        MockPracticeExcelVO excelVO = new MockPracticeExcelVO();
                        List<String> answerList = new LinkedList<>();
                        List<QuestionAnswer> questionAnswers = questionAnswerRepository.findByMockIdAndStatus(mockPractice.getId(), 1);
                        for(QuestionAnswer questionAnswer:questionAnswers){
                            StringBuilder choiceStr = new StringBuilder();
                            String content = questionAnswer.getContent();
                            String[] idList = content.split(",");
                            if(idList.length > 0){
                                for(int i = 0;i< idList.length; i++){
                                    Long id = Long.parseLong(idList[i]);
                                    ChoiceInfo choice = choiceInfoRepository.findOne(id);
                                    choiceStr.append(choice.getContent());
                                }
                            }
                            answerList.add(choiceStr.toString());
                            
                        }

                        if(answerList.size() < 5){
                            answerList.add("");
                            answerList.add("");
                            answerList.add("");
                            answerList.add("");
                            answerList.add("");
                        }

                        BeanUtils.copyProperties(userExcelVO,excelVO);
                        excelVO.setAnswerList(answerList);
                        excelVO.setAnswerDate(mockPractice.getAnswerDate());
                        excelVO.setElseRemark(mockPractice.getElseRemark());
                        excelVO.setOverAllScore(mockPractice.getOverAllScore());

                        list.add(excelVO);
                    }
                }
            }
            map.put("members", list);
            ExcelView excelView = new MockPracticeExcelView();
            return new ModelAndView(excelView, map);
        }else{
            //模块练习
            List<ModulePractice> modulePracticeList = modulePracticeRepository.findByPracticeContentAndStatusOrderByAnswerDateAsc(type,1);
            if(CollectionUtils.isNotEmpty(modulePracticeList)){
                for(ModulePractice modulePractice:modulePracticeList){
                    log.info("========openId:{}========",modulePractice.getOpenId());
                    UserExcelVO userExcelVO = allUser.get(modulePractice.getOpenId());
                    if(userExcelVO != null){
                        ExpressionExcelVO expression = getExpression(modulePractice.getPronunciation(), modulePractice.getFluencyDegree(), modulePractice.getDeportment());
                        RemarkListExcelVO remarkVO = getRemarkList(modulePractice.getAdvantage(), modulePractice.getDisAdvantage(), type);
                        ModulePracticeExcelVO excelVO = new ModulePracticeExcelVO();

                        BeanUtils.copyProperties(userExcelVO,excelVO);
                        BeanUtils.copyProperties(expression,excelVO);
                        BeanUtils.copyProperties(remarkVO,excelVO);
                        excelVO.setAnswerDate(modulePractice.getAnswerDate());
                        excelVO.setElseRemark(modulePractice.getElseRemark());
                        excelVO.setTotalRemark(modulePractice.getOverAllRemark());
                        list.add(excelVO);
                    }
                }
            }
            map.put("members", list);
            ExcelView excelView = new ModulePracticeExcelView();
            return new ModelAndView(excelView, map);

        }

    }

    private PaperRemarkExcelVO getPaperRemark(PaperPractice i) {

        PaperRemarkExcelVO vo = new PaperRemarkExcelVO();
        vo.setBehavior(AdviceManager.getAdvice(i.getBehavior(), 1,learningAdviceRepository));
        vo.setLanguageExpression(AdviceManager.getAdvice(i.getLanguageExpression(), 2,learningAdviceRepository));
        vo.setFocusTopic(AdviceManager.getAdvice(i.getFocusTopic(), 3,learningAdviceRepository));
        vo.setIsOrganized(AdviceManager.getAdvice(i.getIsOrganized(), 4,learningAdviceRepository));
        vo.setHaveSubstance(AdviceManager.getAdvice(i.getHaveSubstance(), 5,learningAdviceRepository));
        return vo;
    }


    /**
     * 查询所有题型
     * key：题型id   value：题型名称
     * 直接写死吧  懒得查表了
     * @return
     */
    private Map<Long, String> getAllType() {
        Map<Long, String> map = new HashMap<>();
        map.put(5L,"人际能力");
        map.put(6L,"执行能力-突发事件处理");
        map.put(2L,"执行能力-规划事件处理");
        map.put(8L,"认知能力-现象认知");
        map.put(9L,"认知能力-观点认知");
        map.put(7L,"创新题型-情景模拟");
        map.put(10L,"创新题型-启示题");
        map.put(11L,"创新题型-漫画题");
        map.put(12L,"创新题型-演讲题");
        map.put(13L,"套题演练");
        map.put(14L,"全真模拟");
        
        return map;
    }


    /**
     * 查询所有用户
     * key:openId  value:用户名称，地区，班级
     * @return
     */
    private Map<String, UserExcelVO> getAllUser() {
        Map<String, UserExcelVO> map = new HashMap<>();

        List<Map<String, Object>> userList = userRepositoryImpl.listForLimit(1, 270, "", -1, -1);
        for(Map<String,Object> userMap:userList){
            String openId = userMap.get("openId").toString();

            UserExcelVO userExcelVO = UserExcelVO.builder()
                    .areaName(userMap.get("aname").toString())
                    .className(userMap.get("cname").toString())
                    .userName(userMap.get("uname").toString())
                    .build();
            map.put(openId,userExcelVO);

        }
        return map;
    }

    /**
     * 表现得分转文字
     * @param pronunciation
     * @param fluencyDegree
     * @param deportment
     * @return
     */
    ExpressionExcelVO getExpression(String pronunciation, String fluencyDegree, String deportment){
        ExpressionVO expressionList =(ExpressionVO) learningSituationMaterialService.getExpressionList();

        StringBuilder pronunciationStr = new StringBuilder();
        StringBuilder fluencyDegreeStr = new StringBuilder();
        StringBuilder deportmentStr = new StringBuilder();
        ExpressionExcelVO vo = new ExpressionExcelVO();
        //语音语调
        expressionList.getPronunciationList().forEach(j -> {
            if(StringUtils.isNotEmpty(pronunciation) && j.getId() == new Long(pronunciation)){
                pronunciationStr.append(j.getContent());
                vo.setPronunciationStr(pronunciationStr.toString());
            }
        });
        //流畅程度
        expressionList.getFluencyDegreeList().forEach(j -> {
            if(StringUtils.isNotEmpty(fluencyDegree) && j.getId() == new Long(fluencyDegree)){
                fluencyDegreeStr.append(j.getContent());
                vo.setFluencyDegreeStr(fluencyDegreeStr.toString());
            }
        });
        //仪态动作
        expressionList.getDeportmentList().forEach(j -> {
            if(StringUtils.isNotEmpty(deportment) && j.getId() == new Long(deportment)){
                deportmentStr.append(j.getContent());
                vo.setDeportmentStr(deportmentStr.toString());
            }
        });
        return vo;
    }


    /**
     * 优缺点得分转文字
     * @param advantage
     * @param disAdvantage
     * @param practiceContent
     * @return
     */
    private RemarkListExcelVO getRemarkList(String advantage,String disAdvantage,long practiceContent) {
        RemarkListVO remarkList =(RemarkListVO) learningSituationMaterialService.getRemarkList(practiceContent);

        StringBuilder advantageStr = new StringBuilder();
        StringBuilder disAdvantageStr = new StringBuilder();

        if(StringUtils.isNotEmpty(advantage)){
            String[] split = advantage.split(",");
            remarkList.getAdvantageList().forEach(j -> {
                for(int k= 0;k<split.length;k++){
                    if(new Long(split[k]) == j.getId() ){
                        advantageStr.append(j.getContent()).append("\n");
                    }
                }
            });
        }

        if(StringUtils.isNotEmpty(disAdvantage)){
            String[] split = disAdvantage.split(",");
            remarkList.getDisAdvantageList().forEach(j -> {
                for(int k= 0;k<split.length;k++){
                    if(new Long(split[k]) == j.getId() ){
                        disAdvantageStr.append(j.getContent()).append("\n");
                    }
                }
            });
        }
        return  RemarkListExcelVO.builder()
                .advantageStr(advantageStr.toString())
                .disAdvantageStr(disAdvantageStr.toString())
                .build();

    }
}
