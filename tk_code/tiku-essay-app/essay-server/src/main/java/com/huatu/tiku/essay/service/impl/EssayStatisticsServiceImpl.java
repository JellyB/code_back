package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssaySimilarQuestionGroupVO;
import com.huatu.tiku.essay.vo.resp.EssaySimilarQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayStatisticsPaperVO;
import com.huatu.tiku.essay.vo.admin.PaperAnswerStatisVO;
import com.huatu.tiku.essay.vo.excel.*;
import com.huatu.tiku.essay.vo.statistics.StatisticsSingerleVO;
import com.huatu.tiku.essay.vo.statistics.StatisticsSingleResultVO;
import com.huatu.tiku.essay.vo.statistics.StatisticsUserVO;
import com.huatu.tiku.essay.manager.PaperManager;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayMockExamService;
import com.huatu.tiku.essay.service.EssayStatisticsService;
import com.huatu.tiku.essay.util.GetAllParameter;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.ResponseMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author create by jbzm on 2018年1月4日17:57:11
 */
@Service
@Slf4j
public class EssayStatisticsServiceImpl implements EssayStatisticsService {
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;
    @Autowired
    EssaySimilarQuestionRepository essaySimilarQuestionRepository;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayMockExamRepository essayMockExamRepository;

    @Autowired
    EssayPaperAnswerRepository paperAnswerRepository;

    @Autowired
    EssayQuestionAnswerRepository questionAnswerRepository;

    @Autowired
    EssayQuestionBaseRepository questionBaseRepository;
    @Autowired
    EssayMockExamService essayMockExamService;
    @Autowired
    EssaySimilarQuestionRepository similarQuestionRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    EssayAreaRepository essayAreaRepository;

    DecimalFormat decimalFormat = new DecimalFormat("#.00");

    @Value("${user-web-server}")
    private String url;

    /**
     * 单题查询
     */
    @Override
    public PageUtil<List<EssaySimilarQuestionGroupVO>> findAllGroup(Pageable pageable, String title, int type) {
        Specification<EssaySimilarQuestionGroupInfo> specification = selectRules(type, title);
        //组装数据
        Page<EssaySimilarQuestionGroupInfo> essaySimilarQuestionGroupInfos = essaySimilarQuestionGroupInfoRepository.findAll(specification, pageable);
        List<EssaySimilarQuestionGroupVO> essaySimilarQuestionGroupVOS = GetAllParameter.test(essaySimilarQuestionGroupInfos.getContent(), EssaySimilarQuestionGroupVO.class);
        //为单题组装数据
        for (EssaySimilarQuestionGroupVO essaySimilarQuestionGroupVO : essaySimilarQuestionGroupVOS) {
            //创建单题列表
            List<EssaySimilarQuestionVO> essaySimilarQuestionVOS = Lists.newLinkedList();;
            List<EssaySimilarQuestion> essaySimilarQuestions = essaySimilarQuestionRepository.findBySimilarIdAndStatus(essaySimilarQuestionGroupVO.getId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            //组装单题
            for (EssaySimilarQuestion essaySimilarQuestion : essaySimilarQuestions) {
                EssaySimilarQuestionVO essaySimilarQuestionVO = new EssaySimilarQuestionVO();
                //日期和地区
                EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(essaySimilarQuestion.getQuestionBaseId());
                //分数和题干
                EssayQuestionDetail essayQuestionDetail = essayQuestionDetailRepository.findOne(essayQuestionBase.getDetailId());
                //开始映射
                BeanUtils.copyProperties(essayQuestionDetail, essaySimilarQuestionVO);
                BeanUtils.copyProperties(essayQuestionBase, essaySimilarQuestionVO);
                //存入集合
                essaySimilarQuestionVOS.add(essaySimilarQuestionVO);
            }
            essaySimilarQuestionGroupVO.setEssaySimilarQuestionVOList(essaySimilarQuestionVOS);
        }
        /*
         *
         * getTotalElements     得到元素总数
         * getPageNumber        得到要返回的页数
         * getSize              得到每页总共有多少信息
         */
        long totalElements = essaySimilarQuestionGroupInfos.getTotalElements();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        //对分页结果进行封装
        PageUtil resultPageUtil = PageUtil.builder().result(essaySimilarQuestionGroupVOS)
                .total(totalElements)
                .totalPage(0 == totalElements % pageSize ? totalElements / pageSize : totalElements / pageSize + 1)
                .next(totalElements > pageSize * (pageNumber + 1) ? 1 : 0)
                .build();
        return resultPageUtil;
    }

    private <T> Specification<T> selectRules(int type, String title) {
        Specification specification = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = Lists.newArrayList();
                if (0 != type) {
                    predicates.add(cb.equal(root.get("type"), type));
                }
                if (StringUtils.isNotEmpty(title)) {
                    predicates.add(cb.like(root.get("showMsg"), "%" + title + "%"));
                }
                predicates.add(cb.equal(root.get("status"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()));
                predicates.add(cb.equal(root.get("bizStatus"), EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return specification;
    }


    /**
     * 统计总数
     *
     * @param type
     * @return
     */
    @Override
    public long countCorrectSum(int type, int paperType) {
        long sum = 0;
        if (type == 0) {
            //单题交卷次数
            sum = essayQuestionAnswerRepository.countByPaperIdAndBizStatus(0L, EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        } else if (type == 1) {
            //先查询结束的模考
            List<EssayMockExam> mockList = PaperManager.getAllFinishedMockExamList(redisTemplate,essayMockExamRepository);
            LinkedList<Long> mockIdList = new LinkedList<>();
            for(EssayMockExam mockExam:mockList){
                mockIdList.add(mockExam.getId());
            }

            //套题交卷次数
            Specification<EssayPaperAnswer> specification = new Specification<EssayPaperAnswer>() {
                @Override
                public Predicate toPredicate(Root<EssayPaperAnswer> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                    List<Predicate> predicates = Lists.newArrayList();

                    //往期模考
                    if (CollectionUtils.isNotEmpty(mockList) && paperType == AdminPaperConstant.MOCK_PAPER) {
                        predicates.add((root.get("paperBaseId").in(mockIdList)));
                    }

                    predicates.add(cb.equal(root.get("status"), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus()));
                    predicates.add(cb.equal(root.get("bizStatus"), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()));
                    return cb.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            };
            sum = essayPaperAnswerRepository.count(specification);
        }
        return sum;
    }

    /**
     * 查找套题
     *
     * @param pageable
     * @param title
     * @param year
     * @param areaId
     * @return
     */
    @Override
    public Object findAllPaper(Pageable pageable, String title, String year, int areaId, int type) {
        Specification<EssayPaperBase> specification = new Specification<EssayPaperBase>() {
            @Override
            public Predicate toPredicate(Root<EssayPaperBase> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = Lists.newArrayList();
                if (!year.equals("")) {
                    predicates.add(cb.equal(root.get("paperYear"), year));
                }
                if (StringUtils.isNotEmpty(title)) {
                    predicates.add(cb.like(root.get("name"), "%" + title + "%"));
                }
                if (-1 != areaId) {
                    predicates.add(cb.equal(root.get("areaId"), areaId));
                }
                predicates.add(cb.equal(root.get("status"), EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus()));
                predicates.add(cb.equal(root.get("type"), type));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        Page<EssayPaperBase> essayPaperBases = essayPaperBaseRepository.findAll(specification, pageable);
        long totalElements = essayPaperBases.getTotalElements();
        List<EssayStatisticsPaperVO> essayStatisticsPaperVOS = GetAllParameter.test(essayPaperBases.getContent(), EssayStatisticsPaperVO.class);

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        //对分页结果进行封装
        PageUtil resultPageUtil = PageUtil.builder().result(essayStatisticsPaperVOS)
                .total(totalElements)
                .totalPage(0 == totalElements % pageSize ? totalElements / pageSize : totalElements / pageSize + 1)
                .next(totalElements > pageSize * (pageNumber + 1) ? 1 : 0)
                .build();
        return resultPageUtil;
    }

    /**
     * 添加单体组细节查询
     *
     * @param id
     * @param modeTypeEnum
     * @return
     */
    @Override
    public Object findBySingleGroupId(Long id, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        List<EssaySimilarQuestion> essaySimilarQuestions = essaySimilarQuestionRepository.findBySimilarIdAndStatus(id, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        StatisticsSingerleVO statisticsSingerleVO = new StatisticsSingerleVO();
        statisticsSingerleVO.setId(id);
        List<StatisticsSingleResultVO> statisticsSingleResultVOS = Lists.newLinkedList();
        //封装单题结果信息
        for (EssaySimilarQuestion essaySimilarQuestion : essaySimilarQuestions) {
            StatisticsSingleResultVO statisticsSingleResultVO = new StatisticsSingleResultVO();
            //下载次数
            EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(essaySimilarQuestion.getQuestionBaseId());
            statisticsSingleResultVO.setDownloadNum(essayQuestionBase.getDownloadCount());
            //添加作答次数
            statisticsSingleResultVO.setAnswerNum(essayQuestionAnswerRepository.countByPaperAnswerIdAndQuestionBaseIdAndStatusAndAnswerCardType(0, essaySimilarQuestion.getQuestionBaseId(),
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    modeTypeEnum.getType()));
            List<PaperAnswerStatisVO> data = essayQuestionAnswerRepository.findStatisData(essaySimilarQuestion.getQuestionBaseId(),0L);
            PaperAnswerStatisVO paperAnswerStatisVO = data.get(0);
            //最高分
            statisticsSingleResultVO.setMaxScore(paperAnswerStatisVO.getMax());
            //最低分
            statisticsSingleResultVO.setMinScore(paperAnswerStatisVO.getMin());
            //平均分
            statisticsSingleResultVO.setAverageScore(KeepTwoDecimal(paperAnswerStatisVO.getAvg()));
            statisticsSingleResultVO.setCorrectNum(paperAnswerStatisVO.getCount());
            //添加地区
            statisticsSingleResultVO.setAreaName(essayQuestionBase.getAreaName());
            //添加地区id
            statisticsSingleResultVO.setAreaId(essayQuestionBase.getAreaId());
            //单题id
            statisticsSingleResultVO.setQuestionId(essayQuestionBase.getId());
            statisticsSingleResultVOS.add(statisticsSingleResultVO);

        }
        statisticsSingerleVO.setStatisticsSingleResultVOS(statisticsSingleResultVOS);
        return statisticsSingerleVO;
    }

    @Override
    public Object findBySingleGroupIdAndPage(Pageable pageable, Long areaId, Long questionId) {
        //组装分页查询条件
        Specification<EssayQuestionAnswer> specification = new Specification<EssayQuestionAnswer>() {
            @Override
            public Predicate toPredicate(Root<EssayQuestionAnswer> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = Lists.newArrayList();
                //添加条件
                predicates.add(cb.equal(root.get("areaId"), areaId));
                predicates.add(cb.equal(root.get("questionBaseId"), questionId));
                predicates.add(cb.equal(root.get("status"), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus()));
                predicates.add(cb.equal(root.get("bizStatus"), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()));
                predicates.add(cb.equal(root.get("paperId"), 0L));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        //查找答题卡
        Page<EssayQuestionAnswer> essayQuestionAnswers = essayQuestionAnswerRepository.findAll(specification, pageable);
        List<UserDto> userDtos = Lists.newLinkedList();
        List<StatisticsUserVO> statisticsUserVOS = GetAllParameter.test(essayQuestionAnswers.getContent(), StatisticsUserVO.class);
        for (int i = 0; i < essayQuestionAnswers.getContent().size(); i++) {
            UserDto userDto = new UserDto();
            userDto.setId(essayQuestionAnswers.getContent().get(i).getUserId());
            userDtos.add(userDto);
        }
        //封装UserWebServer中的对象
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        //将得到的结果封装到VO
        for (int i = 0; i < userDtoList.getData().size(); i++) {
            statisticsUserVOS.get(i).setId(userDtoList.getData().get(i).get("id"));
            statisticsUserVOS.get(i).setNick(userDtoList.getData().get(i).get("nick"));
            statisticsUserVOS.get(i).setArea(essayAreaRepository.findOne(Long.valueOf(String.valueOf(userDtoList.getData().get(i).get("area")))).getName());
            statisticsUserVOS.get(i).setMobile(userDtoList.getData().get(i).get("mobile"));
            statisticsUserVOS.get(i).setSpendTime(statisticsUserVOS.get(i).getSpendTime() / 60);
        }
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        long totalElements = essayQuestionAnswers.getTotalElements();
        //封装查分页结果
        PageUtil resultPageUtil = PageUtil.builder().result(statisticsUserVOS)
                .total(totalElements)
                .totalPage(0 == totalElements % pageSize ? totalElements / pageSize : totalElements / pageSize + 1)
                .next(totalElements > pageSize * (pageNumber + 1) ? 1 : 0)
                .build();
        return resultPageUtil;
    }


    /**
     * 生成一个excel表格通过poi
     */
    @Override
    public ModelAndView getPageExcel(Long pageId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        List<ExcelUserInfoScoreByAreaVO> list = new ArrayList<ExcelUserInfoScoreByAreaVO>();
        //查询所有已经批改试卷
        List<EssayPaperAnswer> essayPaperAnswers = paperAnswerRepository.findByPaperBaseIdAndStatusAndAnswerCardTypeAndBizStatus
                (pageId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        modeTypeEnum.getType(),
                        EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        List<UserDto> userDtos = Lists.newLinkedList();
        for (int j = 0; j < essayPaperAnswers.size(); j++) {
            UserDto userDto = new UserDto();
            userDto.setId(essayPaperAnswers.get(j).getUserId());
            userDtos.add(userDto);
        }
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        for (int i = 0; i < essayPaperAnswers.size(); i++) {
            //填充用户信息
            ExcelUserInfoScoreByAreaVO vo = new ExcelUserInfoScoreByAreaVO();
            vo.setUserId(essayPaperAnswers.get(i).getUserId());
            vo.setAreaName(essayPaperAnswers.get(i).getAreaName());
            vo.setScore(essayPaperAnswers.get(i).getExamScore());
            vo.setNick((null == userDtoList.getData().get(i).get("nick")? "":userDtoList.getData().get(i).get("nick").toString()));
            vo.setMobile((null == userDtoList.getData().get(i).get("mobile")? "":userDtoList.getData().get(i).get("mobile").toString()));
//            List<EssayQuestionAnswer> questionAnswers = questionAnswerRepository.findByPaperAnswerId(essayPaperAnswers.get(i).getId(), new Sort(Sort.Direction.ASC, "id"));
            List<EssayQuestionAnswer> questionAnswers = questionAnswerRepository.findByPaperAnswerIdAndStatusAndBizStatus
                    (essayPaperAnswers.get(i).getId(), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                            EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                            new Sort(Sort.Direction.ASC, "id"));

            for (EssayQuestionAnswer eqa : questionAnswers) {
                EssayQuestionBase eqb = questionBaseRepository.findOne(eqa.getQuestionBaseId());
                switch (eqb.getSort()) {
                    case 1:
                        vo.setFirstScore(eqa.getExamScore());
                        break;
                    case 2:
                        vo.setSecondScore(eqa.getExamScore());
                        break;
                    case 3:
                        vo.setThirdScore(eqa.getExamScore());
                        break;
                    case 4:
                        vo.setForthScore(eqa.getExamScore());
                        break;
                    case 5:
                        vo.setFifthScore(eqa.getExamScore());
                        break;
                }
            }
//            vo.set

            list.add(vo);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("members", list);
        map.put("name", "用户数据");
        ExcelView excelView = new UserInfoScoreByAreaExcelView();
        return new ModelAndView(excelView, map);
//        return null;
    }

    @Override
    public ModelAndView getQuestionExcel(Long groupId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        List<EssaySimilarQuestion> similarQuestions = essaySimilarQuestionRepository.findBySimilarIdAndStatus
                (groupId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        List<Long> questions = new ArrayList<>();
        for (EssaySimilarQuestion esq : similarQuestions) {
            questions.add(esq.getQuestionBaseId());
        }
        //现在得到的是所有单题的id，我们要通过这些id获取所有的单题答题卡
        List<EssayQuestionAnswer> all = new ArrayList<>();
        for (Long questionId : questions) {
            List<EssayQuestionAnswer> temp = essayQuestionAnswerRepository.findByQuestionBaseIdAndStatusAndBizStatusAndAnswerCardType(
                    questionId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                    modeTypeEnum.getType());
            all.addAll(temp);
        }

        List<ExcelQuestionUserInfoVO> vos = new ArrayList<>();
        for (int i = 0; i < all.size(); i++) {
            ExcelQuestionUserInfoVO vo = new ExcelQuestionUserInfoVO();
            vo.setUserId(all.get(i).getUserId());
            vo.getScores().add(all.get(i).getAreaName() + "卷" + all.get(i).getExamScore() + "分");
            for (int j = i + 1; j < all.size(); j++) {

                if (all.get(j).getUserId() == all.get(i).getUserId()) {
                    vo.getScores().add(all.get(j).getAreaName() + "卷" + all.get(j).getExamScore() + "分");
                    all.remove(j--);

                }
            }
            vos.add(vo);
        }
        //  我要把其他服務的信息拿來
        List<UserDto> userDtos = Lists.newLinkedList();
        for (int j = 0; j < all.size(); j++) {
            UserDto userDto = new UserDto();
            userDto.setId(all.get(j).getUserId());
            userDtos.add(userDto);
        }
        ResponseMsg<List<LinkedHashMap<String, Object>>> userDtoList = restTemplate.postForObject(url, userDtos, ResponseMsg.class);
        for (int i = 0; i < vos.size(); i++) {
            if (userDtoList.getData().isEmpty() || userDtoList.getData().get(i) == null) {
                continue;
            }
            vos.get(i).setMobile(userDtoList.getData().get(i).get("mobile"));
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("members", vos);
        map.put("name", "用户数据");
        ExcelView excelView = new UserQuestionExcelView();
        return new ModelAndView(excelView, map);
    }


    @Override
    public Object findByPaperId(Long paperId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        StatisticsSingleResultVO statisticsSingleResultVO = new StatisticsSingleResultVO();
        //查询作答次数
        statisticsSingleResultVO.setAnswerNum(essayPaperAnswerRepository.countByPaperBaseIdAndStatusAndAnswerCardType(paperId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                modeTypeEnum.getType()));
        //查询下载次数
        EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(paperId);
        statisticsSingleResultVO.setDownloadNum(essayPaperBase.getDownloadCount());

        List<PaperAnswerStatisVO> statisData = essayPaperAnswerRepository.findStatisData(paperId);
        //最低分
        statisticsSingleResultVO.setMinScore(statisData.get(0).getMin());
        //最高分
        statisticsSingleResultVO.setMaxScore(statisData.get(0).getMax());
        //总批改次数
        statisticsSingleResultVO.setCorrectNum(statisData.get(0).getCount());
        //平均分
        statisticsSingleResultVO.setAverageScore(KeepTwoDecimal(statisData.get(0).getAvg()));

        List<EssayQuestionBase> essayQuestionBases = essayQuestionBaseRepository.findByPaperIdAndStatus(paperId, new Sort(Sort.Direction.ASC, "sort"),1);
        //循环组装单题数据
        List<StatisticsSingleResultVO> statisticsSingleResultVOS = Lists.newLinkedList();
        for (EssayQuestionBase essayQuestionBase : essayQuestionBases) {
            //创建单题容器
            StatisticsSingleResultVO statisticsSingleResultVO1 = new StatisticsSingleResultVO();
            List<PaperAnswerStatisVO> data = essayQuestionAnswerRepository.findStatisData(essayQuestionBase.getId(),paperId);
            PaperAnswerStatisVO paperAnswerStatisVO = data.get(0);
            //最高分
            statisticsSingleResultVO1.setMaxScore(paperAnswerStatisVO.getMax());
            //最低分
            statisticsSingleResultVO1.setMinScore(paperAnswerStatisVO.getMin());
            //平均分
            statisticsSingleResultVO1.setAverageScore(KeepTwoDecimal(paperAnswerStatisVO.getAvg()));
            //装入容器
            statisticsSingleResultVOS.add(statisticsSingleResultVO1);
        }
        //装入容器statisticsSingleResultVOS
        statisticsSingleResultVO.setStatisticsSingleResultVOS(statisticsSingleResultVOS);
        return statisticsSingleResultVO;
    }

    public static Double KeepTwoDecimal(Double aDouble) {
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        if (aDouble == null) {
            return 0x0.0p0;
        } else {
            return Double.valueOf(decimalFormat.format(aDouble));
        }
    }

//    public static void main(String[] args) {
//        double d = 0;
//        StatisticsSingleResultVO statisticsSingleResultVO1 = new StatisticsSingleResultVO();
//        DecimalFormat decimalFormat = new DecimalFormat("#.00");
//        statisticsSingleResultVO1.setAverageScore(Double.parseDouble(decimalFormat.format(d)));
//        System.out.println(statisticsSingleResultVO1.getAverageScore());
//    }


}
