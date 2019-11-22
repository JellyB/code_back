package com.huatu.tiku.essay.service.impl;


import com.alibaba.druid.support.json.JSONUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayMockErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayMockStatisticsVO;
import com.huatu.tiku.essay.vo.resp.EssayResultInfoVO;
import com.huatu.tiku.essay.vo.resp.EssayUserVO;
import com.huatu.tiku.essay.vo.excel.ExcelUserInfoScoreByAreaVO;
import com.huatu.tiku.essay.vo.excel.ExcelView;
import com.huatu.tiku.essay.vo.excel.UserInfoScoreByAreaExcelView;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayMockStatisticsService;
import com.huatu.tiku.essay.util.GetAllParameter;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.ResponseMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/7 13:43
 * @Modefied By:
 */

@Service
@Slf4j
public class EssayMockStatisticsServiceImpl implements EssayMockStatisticsService {

    @Autowired
    private EssayAreaRepository areaRepository;

    @Autowired
    private EssayMockExamRepository mockExamRepository;

    @Autowired
    private EssayPaperAnswerRepository paperAnswerRepository;

    @Autowired
    private EssayQuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private EssayQuestionBaseRepository questionBaseRepository;


    @Autowired
    RestTemplate restTemplate;

    @Value("${user-web-server}")
    private String url;

    @Override
    public EssayMockStatisticsVO firstGet(Long mockExamId) {
        //结果信息 和分数分布区间
        EssayMockStatisticsVO mockStatisticsVO = new EssayMockStatisticsVO();
        EssayResultInfoVO resultInfoVO = new EssayResultInfoVO();
//        EssayQuestionBelongPaperArea area = areaRepository.findOne(areaId);
        //获取模考
        EssayMockExam exam = mockExamRepository.findOne(mockExamId);
        //判断是否为空
        if (exam == null) {

            throw new BizException(EssayMockErrors.MOCK_ID_NOT_EXIST);
        }
        // 获取最高分所在地区
        List<String> maxScoreArea = paperAnswerRepository.getMaxScoreArea(mockExamId);

        // 获取平均分所在地区
        List<String> avgMaxAreas = paperAnswerRepository.getAvgMaxAreas(mockExamId);
        //留下三个
        for (int i = avgMaxAreas.size() - 1; i > 2; i--) {
            avgMaxAreas.remove(i);
        }
        // 获取考试人数最大的地区
        List<String> countMaxAreas = paperAnswerRepository.getCountMaxAreas(mockExamId);
        for (int i = countMaxAreas.size() - 1; i > 2; i--) {
            countMaxAreas.remove(i);
        }
        resultInfoVO.setExamCount(exam.getExamCount());
        resultInfoVO.setMaxScore(exam.getMaxScore());
        resultInfoVO.setAvgScore(exam.getAvgScore());
        resultInfoVO.setMaxScoreArea(maxScoreArea);
        resultInfoVO.setAvgScoreArea(avgMaxAreas);
        resultInfoVO.setExamCountMaxArea(countMaxAreas);
        mockStatisticsVO.setEssayResultInfoVO(resultInfoVO);

        //获取区间
        List<Integer> scoreRange = new ArrayList<>();
        for (Double i = -0.0001; i < 90; i += 10.0000) {
            System.out.println("当前区间为" + i + "--" + (i + 10.00000));
            if (i + 10 > 90) {
                scoreRange.add(paperAnswerRepository.getScoreRange(i, i + 1000, mockExamId));
                System.out.println("这里判断了高于100分的情况");
            } else {
                scoreRange.add(paperAnswerRepository.getScoreRange(i, i + 10, mockExamId));
            }
        }

        mockStatisticsVO.setScoreRange(scoreRange);

//        Specification<EssayPaperAnswer> specification = selectRules(mockExamId);


        return mockStatisticsVO;
    }

    @Override
    public PageUtil<Object> getPage(Pageable pageable, Long mockExamId) {

        //生成条件
        Specification<EssayPaperAnswer> specification = selectRules(mockExamId);
        //查询分页
        Page<EssayPaperAnswer> paperAnswers = paperAnswerRepository.findAll(specification, pageable);
        if (paperAnswers.getContent().size() == 0) {
            return PageUtil.builder().build();
//            throw new BizException(EssayMockErrors.MOCK_ID_NOT_EXIST);
        }
        List<UserDto> userDtos = Lists.newLinkedList();
        System.out.println(paperAnswers.getContent().get(0).getSpendTime());
        //获取对象
        List<EssayUserVO> EssayUserVOs = GetAllParameter.test(paperAnswers.getContent(), EssayUserVO.class);
        for (int i = 0; i < paperAnswers.getContent().size(); i++) {
            UserDto userDto = new UserDto();
            userDto.setId(paperAnswers.getContent().get(i).getUserId());
            userDtos.add(userDto);
        }
        //封装UserWebServer中的对象
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        System.out.println(userDtoList.getData().size() + ": size");
        // 获取手机
        for (int i = 0; i < userDtoList.getData().size(); i++) {
            if (null != userDtoList.getData().get(i)) {
                EssayUserVOs.get(i).setMobile(userDtoList.getData().get(i).get("mobile"));
            } else {
                EssayUserVOs.get(i).setMobile(" ");
//                EssayUserVOs.get(i).setAreaName(" ");
            }
//            System.out.println(userDtoList.getData().get(i).get("area"));

            EssayUserVOs.get(i).setSpendTime(EssayUserVOs.get(i).getSpendTime() / 60);
        }
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        long totalElements = paperAnswers.getTotalElements();
        // 获取前端分页
        PageUtil resultPageUtil = PageUtil.builder()
                .result(EssayUserVOs)
                .total(totalElements)
                .totalPage(0 == totalElements % pageSize ? totalElements / pageSize : totalElements / pageSize + 1)
                .next(totalElements > pageSize * pageNumber ? 1 : 0)
                .build();
//        System.out.println(EssayUserVOs);
        return resultPageUtil;
    }

    /**
     * '
     * 生成一个excel表格通过poi
     */
    @Override
    public ModelAndView getExcel(Long mockExamId, ArrayList<Long> areaIds) {
        List<ExcelUserInfoScoreByAreaVO> list = new ArrayList<ExcelUserInfoScoreByAreaVO>();
        StopWatch stopWatch = new StopWatch("下载模考大赛excel");
        //  这里就应该通过地区id和模考id，获得相应的用户信息
        List<EssayPaperAnswer> essayPaperAnswers = new ArrayList<>();
        stopWatch.start("查询试卷答题卡" + mockExamId);
        if (areaIds.isEmpty()) {
            essayPaperAnswers.addAll(paperAnswerRepository.findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatus(mockExamId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()));
        } else {
            essayPaperAnswers.addAll(paperAnswerRepository.findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatusAndAreaIdIn(mockExamId,
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                    areaIds));
        }
        stopWatch.stop();
        //  我要把user服务中的info拿过来
        stopWatch.start("查询用户数据");
        List<UserDto> userDtos = essayPaperAnswers.stream().map(EssayPaperAnswer::getUserId).map(i -> UserDto.builder().id(i).build()).collect(Collectors.toList());
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        List<LinkedHashMap<String, Object>> data = userDtoList.getData();
        stopWatch.stop();
        if (CollectionUtils.isNotEmpty(data)) {
            System.out.println("data = " + JSONUtils.toJSONString(data));
            stopWatch.start("area find all");
            List<EssayQuestionBelongPaperArea> all = areaRepository.findAll();
            Map<Long, String> areaMap = all.stream().collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));
            List<EssayQuestionBase> questionBases = questionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(mockExamId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            final Map<Long, Integer> sortMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(questionBases)) {
                sortMap.putAll(questionBases.stream().collect(Collectors.toMap(i -> i.getId(), i -> i.getSort())));
            }
            stopWatch.stop();
            Function<Long, Integer> getSort = (id -> {
                Integer sort = sortMap.getOrDefault(id, -1);
                return sort;
            });
            stopWatch.start("查询试题答题卡");
            String idStr = questionBases.stream().map(EssayQuestionBase::getId).map(String::valueOf).collect(Collectors.joining(","));
            List<Object[]> scoreList = questionAnswerRepository.findScores(questionBases.stream().map(EssayQuestionBase::getId).collect(Collectors.toList()));
            stopWatch.stop();
            System.out.println(scoreList);
            for (int i = 0; i < essayPaperAnswers.size(); i++) {
                EssayPaperAnswer essayPaperAnswer = essayPaperAnswers.get(i);

                stopWatch.start("组装答题卡分数：" + essayPaperAnswer.getId());
                //填充用户信息
                ExcelUserInfoScoreByAreaVO vo = new ExcelUserInfoScoreByAreaVO();
                vo.setUserId(essayPaperAnswer.getUserId());
                vo.setAreaName(essayPaperAnswer.getAreaName());
                vo.setScore(essayPaperAnswer.getExamScore());
                vo.setMobile((null == data.get(i).get("mobile") ? "" : data.get(i).get("mobile").toString()));
                vo.setAreaName(areaMap.getOrDefault(essayPaperAnswer.getAreaId(), ""));

//                //  根据答题卡id ，从单题中搜索若干题目及其得分，再通过其中的paperBaseid，获得其题号，保留题号为123的题目并存储到vo里
//                List<EssayQuestionAnswer> questionAnswers = questionAnswerRepository.findByPaperAnswerIdAndStatusAndBizStatus
//                        (essayPaperAnswer.getId(), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), EssayQuestionAnswerConstant.EssayQuestionAnswerBizStatusEnum.CORRECT.getBizStatus(), new Sort(Sort.Direction.ASC, "id"));
                scoreList.stream().filter(temp -> essayPaperAnswer.getId() == Long.parseLong(String.valueOf(temp[2])))
                        .forEach(temp -> {
                            Long id = Long.parseLong(String.valueOf(temp[1]));
                            Double examScore = Double.parseDouble(String.valueOf(temp[0]));
                            switch (getSort.apply(id)) {
                                case 1:
                                    vo.setFirstScore(examScore);
                                    break;
                                case 2:
                                    vo.setSecondScore(examScore);
                                    break;
                                case 3:
                                    vo.setThirdScore(examScore);
                                    break;
                                case 4:
                                    vo.setForthScore(examScore);
                                    break;
                                case 5:
                                    vo.setFifthScore(examScore);
                                    break;
                            }
                        });

//                for (EssayQuestionAnswer eqa : questionAnswers) {
//
//                    switch (getSort.apply(eqa.getQuestionBaseId())) {
//                        case 1:
//                            vo.setFirstScore(eqa.getExamScore());
//                            break;
//                        case 2:
//                            vo.setSecondScore(eqa.getExamScore());
//                            break;
//                        case 3:
//                            vo.setThirdScore(eqa.getExamScore());
//                            break;
//                        case 4:
//                            vo.setForthScore(eqa.getExamScore());
//                            break;
//                        case 5:
//                            vo.setFifthScore(eqa.getExamScore());
//                            break;
//                    }
//                }
                stopWatch.stop();
                list.add(vo);
            }
        }
        stopWatch.start("生成excel数据");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("members", list);
        map.put("name", "用户数据");
        ExcelView excelView = new UserInfoScoreByAreaExcelView();
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
        return new ModelAndView(excelView, map);
    }

    private <T> Specification<T> selectRules(Long mockExamId) {
        Specification specification = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = Lists.newArrayList();
                if (mockExamId != null) {
                    predicates.add(cb.equal(root.get("paperBaseId"), mockExamId));
                }
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }
}
