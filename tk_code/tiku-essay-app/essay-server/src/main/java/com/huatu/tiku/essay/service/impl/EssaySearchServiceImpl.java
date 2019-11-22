package com.huatu.tiku.essay.service.impl;

import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.ARGUMENTATION;
import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.PAPER;
import static com.huatu.tiku.essay.constant.status.QuestionTypeConstant.SINGLE_QUESTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.criteria.Predicate;

import com.alibaba.fastjson.JSONObject;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.consts.SensorsEventEnum;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.status.AdminPaperConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssaySimilarQuestionGroupInfo;
import com.huatu.tiku.essay.manager.QuestionManager;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayPaperBaseRepository;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.repository.EssaySimilarQuestionGroupInfoRepository;
import com.huatu.tiku.essay.repository.EssaySimilarQuestionRepository;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.EssaySearchService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.question.SingleQuestionSearch;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.Result;
import com.huatu.tiku.essay.util.sensors.SensorsUtils;
import com.huatu.tiku.essay.vo.admin.AdminQuestionTypeVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.SearchPostRequestVO;
import com.huatu.tiku.essay.vo.resp.SearchRespVO;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaoxi
 * @date Create on 2018/2/5 11:34
 */
@Service
@Slf4j
public class EssaySearchServiceImpl implements EssaySearchService {
    @Autowired
    private EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;
    @Autowired
    private EssaySimilarQuestionRepository essaySimilarQuestionRepository;
    @Autowired
    private EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    private EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;
    @Autowired
    EssayQuestionService essayQuestionService;
    @Autowired
    private SingleQuestionSearch singleQuestionSearch;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    EssayPaperService essayPaperService;
    @Value("${paper_search_url}")
    private String paperSearchUrl;
    @Value("${question_search_url}")
    private String questionSearchUrl;
    @Autowired
    EssaySimilarQuestionService essaySimilarQuestionService;
    @Autowired
    RedisTemplate redisTemplate;
    
    @Autowired
    private SensorsAnalytics sensorsAnalytics;

    private static final String REPLACE_STR = "<font color='#e9304e'>%s</font>";

    @Override
    public Object searchQuestion(UserSession userSession, String content, int page, int pageSize, String cv, int terminal) {

        PageRequest pageable = new PageRequest(page - 1, pageSize);
        //查询
        Specification<EssaySimilarQuestionGroupInfo> specification = (root, query, cb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            if (StringUtils.isNotEmpty(content)) {
                predicates.add(cb.like(root.get("showMsg"), "%" + content.replaceAll("\"", "") + "%"));
            }
            predicates.add(cb.equal(root.get("bizStatus"), EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()));
            predicates.add(cb.equal(root.get("status"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()));
            query.orderBy(cb.asc(root.get("pType")), cb.desc(root.get("showMsg")));
            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            return query.getRestriction();
        };
        //查词查询
        Specification<EssaySimilarQuestionGroupInfo> specificationSkip = (root, query, cb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            if (StringUtils.isNotEmpty(content)) {
                char[] contentSkip = content.replaceAll("\"", "").toCharArray();
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < contentSkip.length; i++) {
                    result.append("%").append(contentSkip[i]).append("%");
                }
                predicates.add(cb.like(root.get("showMsg"), result.toString()));
            }
            if (StringUtils.isNotEmpty(content)) {
                predicates.add(cb.notLike(root.get("showMsg"), "%" + content.replaceAll("\"", "") + "%"));
            }
            predicates.add(cb.equal(root.get("status"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()));
            predicates.add(cb.equal(root.get("bizStatus"), EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()));
            query.orderBy(cb.asc(root.get("pType")), cb.desc(root.get("showMsg")));
            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            return query.getRestriction();
        };
        Page<EssaySimilarQuestionGroupInfo> essaySimilarQuestionGroupInfos = essaySimilarQuestionGroupInfoRepository.findAll(specification, pageable);
        //少于20条继续拆词查询
        List<EssaySimilarQuestionGroupInfo> similarList = essaySimilarQuestionGroupInfos.getContent();
        //存放精确查询的结果防止排序混乱
        List<EssaySimilarQuestionGroupInfo> arrayList = new ArrayList<>(similarList);
        sortSearchResult(arrayList);
        //存放模糊查询的结果防止排序混乱
        List<EssaySimilarQuestionGroupInfo> arrayListSortFuzzy = new ArrayList<>();
        if (essaySimilarQuestionGroupInfos.getTotalElements() < 20) {
            Page<EssaySimilarQuestionGroupInfo> essaySimilarQuestionGroupInfoSkip = essaySimilarQuestionGroupInfoRepository.findAll(specificationSkip, pageable);

            List<EssaySimilarQuestionGroupInfo> skipList = essaySimilarQuestionGroupInfoSkip.getContent();
            if (CollectionUtils.isNotEmpty(skipList)) {
                long size = Math.min(20 - essaySimilarQuestionGroupInfos.getTotalElements(), skipList.size());
                for (int i = 0; i < size; i++) {
                    arrayListSortFuzzy.add(skipList.get(i));
                }
            }
        }
        sortSearchResult(arrayListSortFuzzy);
        //添加结果进入集合
        arrayList.addAll(arrayListSortFuzzy);


        //使用stopwatch停表工具
        Stopwatch stopwatch = Stopwatch.createStarted();
        //封装结果
        List<EssayQuestionVO> essayQuestionVOS = Lists.newLinkedList();
        //循环拼装
        arrayList.forEach(essaySimilarQuestionGroupInfo -> {
            EssayQuestionVO essayQuestionVO = new EssayQuestionVO();
            String showMsg = essaySimilarQuestionGroupInfo.getShowMsg();

            showMsg = replaceString(showMsg, content);

            essaySimilarQuestionGroupInfo.setShowMsg(showMsg);
            BeanUtils.copyProperties(essaySimilarQuestionGroupInfo, essayQuestionVO);

            //根据similarId查询题组所有试题
            List<Long> questionIds = essaySimilarQuestionRepository.findQuestionBaseIdBySimilarIdAndStatus(essaySimilarQuestionGroupInfo.getId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

            //根据相似题目的id查询题目base信息
            LinkedList<EssayQuestionBase> baseList = new LinkedList<>();
            LinkedList<EssayQuestionAreaVO> essayQuestionAreaVOList = new LinkedList<EssayQuestionAreaVO>();
            if (CollectionUtils.isNotEmpty(questionIds)) {
                baseList = essayQuestionBaseRepository.findList(questionIds);
                essayQuestionAreaVOList = QuestionManager.changeEssayQuestionBaseToEssayQuestionAreaVO(baseList);
            }
            //对结果进行封装
            essayQuestionVO.setEssayQuestionBelongPaperVOList(essayQuestionAreaVOList);
            essayQuestionVO.setSimilarId(essaySimilarQuestionGroupInfo.getId());
            AdminQuestionTypeVO questionTypeVO = essayQuestionService.getQuestionType(essayQuestionVO.getType());
            String questionTypeName = questionTypeVO.getQuestionTypeName();
            essayQuestionVO.setType(questionTypeVO.getQuestionType().get(0).intValue());
            if (questionTypeName.contains("-")) {
                questionTypeName = questionTypeName.substring(0, questionTypeName.indexOf("-"));
            }
            essayQuestionVO.setQuestionTypeName(questionTypeName);
            essayQuestionVOS.add(essayQuestionVO);
        });
        //获取总元素
        long totalElements = essaySimilarQuestionGroupInfos.getTotalElements();

        log.info(String.valueOf(stopwatch.stop()));

        PageUtil resultPageUtil = PageUtil.builder().result(essayQuestionVOS)
                .next(totalElements > pageSize * page ? 1 : 0)
                .build();
        return resultPageUtil;
    }


    private void sortSearchResult(List<EssaySimilarQuestionGroupInfo> arrayList) {
        arrayList.stream().sorted((o1, o2) -> {
            String showMsg1 = o1.getShowMsg();
            String showMsg2 = o2.getShowMsg();
            if (showMsg1.length() > 5 && showMsg2.length() > 5) {
                if (showMsg1.substring(0, 2).equals(20) && showMsg2.substring(0, 2).equals(20)) {
                    String substring1 = showMsg1.substring(0, 3);
                    String substring2 = showMsg2.substring(0, 3);
                    return Integer.valueOf(substring1) - Integer.valueOf(substring2);
                }
            }
            return 1;
        }).collect(Collectors.toList()).stream().sorted((o1, o2) -> {
            int type1 = o1.getType();
            int type2 = o2.getType();
            return type1 - type2;
        });
    }

    private static String replaceString(String baseStr, String replaceRule) {

        if ("".equals(replaceRule) || "".equals(baseStr) || baseStr.contains("</font>")) {
            return baseStr;
        }
        List<String> rule = Arrays.asList(replaceRule.split(""));
        StringBuilder result = new StringBuilder();
        for (String charStr : baseStr.split("")) {
            if (rule.indexOf(charStr) > -1) {
                result.append(String.format(REPLACE_STR, charStr));
            } else {
                result.append(charStr);
            }
        }
        return result.toString();
    }

//    public Object searchPaper(int userId, String content, PageRequest pageable) {
//        //查询
//        Specification<EssayPaperBase> specification = (root, query, cb) -> {
//            List<Predicate> predicates = Lists.newArrayList();
//            if (StringUtils.isNotEmpty(content)) {
//                predicates.add(cb.like(root.get("name"), "%" + content.replaceAll("\"", "") + "%"));
//            }
//            predicates.add(cb.equal(root.get("bizStatus"), EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()));
//            predicates.add(cb.equal(root.get("status"), EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus()));
//            query.orderBy(cb.desc(root.get("paperYear")), cb.desc(root.get("paperDate")), cb.desc(root.get("areaId")), cb.desc(root.get("subAreaId")));
//            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
//            return query.getRestriction();
//        };
//        //查词查询
//        Specification<EssayPaperBase> specificationSkip = (root, query, cb) -> {
//            List<Predicate> predicates = Lists.newArrayList();
//            if (StringUtils.isNotEmpty(content)) {
//                char[] contentSkip = content.replaceAll("\"", "").toCharArray();
//                StringBuilder result = new StringBuilder();
//                for (int i = 0; i < contentSkip.length; i++) {
//                    result.append("%").append(contentSkip[i]).append("%");
//                }
//                predicates.add(cb.like(root.get("name"), result.toString()));
//            }
//            predicates.add(cb.equal(root.get("bizStatus"), EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()));
//            predicates.add(cb.equal(root.get("status"), EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus()));
//            query.orderBy(cb.desc(root.get("paperYear")), cb.desc(root.get("paperDate")), cb.desc(root.get("areaId")), cb.desc(root.get("subAreaId")));
//            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
//            return query.getRestriction();
//        };
//        Page<EssayPaperBase> paperList = essayPaperBaseRepository.findAll(specificationSkip, pageable);
//        //使用stopwatch停表工具
//        Stopwatch stopwatch = Stopwatch.createStarted();
//        //封装结果
//        List<EssayPaperVO> paperVOList = new LinkedList<>();
//        //循环拼装
//        paperList.forEach(paperBase -> {
//
//            String name = paperBase.getName();
//
//            char[] chars = content.replaceAll("\"", "").toCharArray();
//            for (int i = 0; i < chars.length; i++) {
//                name = name.replaceAll(chars[i] + "", "<font color='#e9304e'>" + chars[i] + "</font>");
//            }
//
//            EssayPaperVO paperVO = EssayPaperVO.builder()
//                    .paperName(name)
//                    .limitTime(paperBase.getLimitTime())
//                    .score(paperBase.getScore())
//                    .build();
//            paperVO.setPaperId(paperBase.getId());
//            //根据试卷id查询用户作答记录
//            List<EssayPaperAnswer> answerList = essayPaperAnswerRepository.findByUserIdAndPaperBaseIdAndTypeAndStatusAndAnswerCardTypeOrderByGmtCreateDesc
//                    (userId, paperBase.getId(), AdminPaperConstant.TRUE_PAPER, EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus());
//            if (CollectionUtils.isNotEmpty(answerList)) {
//
//                if (answerList.size() != 1) {
//                    paperVO.setCorrectNum(answerList.size());
//                } else {
//                    EssayPaperAnswer essayPaperAnswer = answerList.get(0);
//                    paperVO.setRecentStatus(essayPaperAnswer.getBizStatus());
//                    if (EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus() == essayPaperAnswer.getBizStatus()) {
//                        paperVO.setCorrectNum(1);
//                    }
//                }
//            }
//            paperVOList.add(paperVO);
//
//
//        });
//        log.info(String.valueOf(stopwatch.stop()));
//        //获取总元素
//        long totalElements = paperList.getTotalElements();
//
//        //对分页结果进行封装
//        int pageNumber = pageable.getPageNumber();
//        int pageSize = pageable.getPageSize();
//        PageUtil resultPageUtil = PageUtil.builder().result(paperVOList)
//                .total(totalElements)
//                .totalPage(0 == totalElements % pageSize ? totalElements / pageSize : totalElements / pageSize + 1)
//                .next(totalElements > pageSize * (pageNumber + 1) ? 1 : 0)
//                .build();
//        return resultPageUtil;
//    }


    @Override
    public Object searchPaper(String content, int page, int pageSize) {

        if (StringUtils.isNotEmpty(content)) {
            String url = String.format(paperSearchUrl, page, pageSize, content);
            Result result = restTemplate.getForObject(url, Result.class);
            return result.getData();
        } else {
            return null;
        }

    }

    @Override
    public void importPaper2Search(long paperId) {
//        List<EssayPaperBase> paperBaseList = essayPaperBaseRepository.findByStatusAndBizStatusAndType(EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus(), EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(),AdminPaperConstant.TRUE_PAPER);
//        if (CollectionUtils.isNotEmpty(paperBaseList)) {
//            paperBaseList.forEach(i -> essayPaperService.sendPaper2Search(i.getId(), AdminPaperConstant.UP_TO_ONLINE));
//        }

        essayPaperService.sendPaper2Search(paperId, AdminPaperConstant.UP_TO_OFFLINE);
    }

    @Override
    public Object searchQuestionV3(int userId,String content, int page, int pageSize, int type) {
        List<SearchRespVO> list = new LinkedList<>();
        //查询所有
        if (type == -1) {
            pageSize = 3;
        } else {
            pageSize = 20;
        }

        SearchPostRequestVO requestVO = SearchPostRequestVO.builder()
                .keyword(content)
                .page(page)
                .size(pageSize)
                .build();
        //单题搜索
        if (type == -1 || type == SINGLE_QUESTION) {
            requestVO.setType(SINGLE_QUESTION);
            Map result = restTemplate.postForObject(questionSearchUrl,requestVO, Map.class);
            Boolean flag = checkResultExist(result);
            Map data = (Map)result.get("data");
            if(flag){
                Map map = convertGroupDataToMap( data,userId);
                SearchRespVO respVO = SearchRespVO.builder()
                        .data(map)
                        .type(SINGLE_QUESTION)
                        .typeName("标准答案")
                        .build();
                list.add(respVO);
            }
        }

        //套题搜索
        if (type == -1 || type == PAPER) {
            requestVO.setType(PAPER);
            Map result = restTemplate.postForObject(paperSearchUrl, requestVO,Map.class);
            Map data = (Map)result.get("data");
            Boolean flag = checkResultExist(result);
            if(flag){
                checkPaperStatus(data);
                SearchRespVO respVO = SearchRespVO.builder()
                        .data(data)
                        .type(PAPER)
                        .typeName("套题")
                        .build();
                list.add(respVO);
            }
        }

        //议论文搜索
        if (type == -1 || type == ARGUMENTATION) {
            requestVO.setType(ARGUMENTATION);
            Map result = restTemplate.postForObject(questionSearchUrl,requestVO, Map.class);
            Map data = (Map)result.get("data");
            Boolean flag = checkResultExist(result);
            if(flag){
                Map map = convertGroupDataToMap(data,userId);
                SearchRespVO respVO = SearchRespVO.builder()
                        .data(map)
                        .type(ARGUMENTATION)
//                        .typeName("议论文")
                        .typeName("文章写作")
                        .build();
                list.add(respVO);
            }
        }

        //按照题型排序（标答-套题-议论文）
        list.sort((a, b) -> (a.getType() - b.getType()));
        return list;
    }


    @Override
    public Object searchQuestionV4(int userId,String content, int page, int pageSize, int type) {
        List<SearchRespVO> list = new LinkedList<>();
        //查询所有
        if (type == -1) {
            pageSize = 3;
        } else {
            pageSize = 20;
        }

        SearchPostRequestVO requestVO = SearchPostRequestVO.builder()
                .keyword(content)
                .page(page)
                .size(pageSize)
                .build();
        //单题搜索
        if (type == -1 || type == SINGLE_QUESTION) {
            requestVO.setType(SINGLE_QUESTION);
            Map result = restTemplate.postForObject(questionSearchUrl,requestVO, Map.class);
            Boolean flag = checkResultExist(result);
            Map data = (Map)result.get("data");
            if(flag){
                Map map = convertGroupDataToMap( data,userId);
                SearchRespVO respVO = SearchRespVO.builder()
                        .data(map)
                        .type(SINGLE_QUESTION)
                        .typeName("标准答案")
                        .build();
                list.add(respVO);
            }
        }

        //套题搜索
        if (type == -1 || type == PAPER) {
            requestVO.setType(PAPER);
            Map result = restTemplate.postForObject(paperSearchUrl, requestVO,Map.class);
            Map data = (Map)result.get("data");
            Boolean flag = checkResultExist(result);
            if(flag){
                SearchRespVO respVO = SearchRespVO.builder()
                        .data(data)
                        .type(PAPER)
                        .typeName("套题")
                        .build();
                list.add(respVO);
            }
        }

        //议论文搜索
        if (type == -1 || type == ARGUMENTATION) {
            requestVO.setType(ARGUMENTATION);
            Map result = restTemplate.postForObject(questionSearchUrl,requestVO, Map.class);
            Map data = (Map)result.get("data");
            Boolean flag = checkResultExist(result);
            if(flag){
                Map map = convertGroupDataToMap(data,userId);
                SearchRespVO respVO = SearchRespVO.builder()
                        .data(map)
                        .type(ARGUMENTATION)
//                        .typeName("议论文")
                        .typeName("文章写作")
                        .build();
                list.add(respVO);
            }
        }

        //按照题型排序（标答-套题-议论文）
        list.sort((a, b) -> (a.getType() - b.getType()));
        return list;
    }

    private Map convertGroupDataToMap(Map data,int userId){
        boolean searchSwitch = getSearchShowMaterialAndStemSwitch();
        if(data.isEmpty()){
            return data;
        }
        List<Map<String,Object>> content = (List<Map<String,Object>>)data.get("content");
        if(CollectionUtils.isNotEmpty(content)){
            content.forEach(group ->{
                Long groupId = Long.parseLong(group.get("groupId").toString());
                if(null != groupId && groupId > 0){
                    List<EssayQuestionAreaVO> areaList = singleQuestionSearch.findSimilarQuestionAreaVOInfoList(groupId, userId);
                    boolean videoAnalyzeFlag = areaList.stream().anyMatch(question -> null != question.getVideoId() && question.getVideoId() > 0);
                    group.put("videoAnalyzeFlag",videoAnalyzeFlag);
                    group.put("areaList",areaList);

                    if(!searchSwitch){
                        //题干内容置空
                        List<Map<String,Object>> questionList = (List<Map<String,Object>>)group.get("questionList");
                        if(CollectionUtils.isNotEmpty(questionList)){
                            questionList.forEach(question -> {
                                question.put("stem",null);
                            });
                        }
                        //材料内容置空
                        List<Map<String,Object>> materialList = (List<Map<String,Object>>)group.get("materialList");
                        if(CollectionUtils.isNotEmpty(materialList)){
                            materialList.forEach(material -> {
                                material.put("content",null);
                            });
                        }

                    }
                }
            });
        }
        return data;
    }

    /**
     * 判断是模考卷还是真题卷
     * 真题 1  模考题 0
     */
    private void checkPaperStatus(Map data){
        List<Map<String,Object>> content = (List<Map<String,Object>>)data.get("content");
        if(CollectionUtils.isNotEmpty(content)){
            List<Long> paperIds = content.stream().map(item -> MapUtils.getLong(item, "paperId")).collect(Collectors.toList());
            log.info("essay search paperIds:{}", paperIds);
            List<EssayPaperBase> paperBases = essayPaperBaseRepository.findByIdIn(paperIds);
            log.info("essay search papers info:{}", JSONObject.toJSONString(paperBases));
            Map<Long, Integer> paperTypeMap = paperBases.stream().collect(Collectors.toMap(i -> i.getId(), i -> i.getType()));
            log.info("essay search paper map:{}", paperTypeMap);
            content.forEach(group -> {
                Long paperId = MapUtils.getLong(group, "paperId");
                Integer type = MapUtils.getInteger(paperTypeMap, paperId);
                group.put("paperType", type == null ? 0 : type) ;
            });
        }
    }

    @Override
    public void importQuestion2Search() {
        List<EssaySimilarQuestionGroupInfo> groupInfos = essaySimilarQuestionGroupInfoRepository.findByBizStatusAndStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(),
                EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isNotEmpty(groupInfos)) {
            groupInfos.forEach(i -> essaySimilarQuestionService.sendSimilarQuestion2Search(i.getId(), AdminPaperConstant.UP_TO_ONLINE));
        }
    }


    /**
     * 搜索是命中材料和题干是否展示
     * @return
     */
    private boolean getSearchShowMaterialAndStemSwitch(){
        String essaySearchSwitchKey = RedisKeyConstant.getEssaySearchSwitchKey();

        Boolean essaySearchSwitch = (Boolean)redisTemplate.opsForValue().get(essaySearchSwitchKey);
        if(null == essaySearchSwitch){
            essaySearchSwitch = false;
        }
        return essaySearchSwitch;
    }

    /**
     * 检验返回是否有数据
     */
	private Boolean checkResultExist(Map result) {
		if (result != null) {
			Map data = (Map) result.get("data");
			int code = (int) result.get("code");
			if (code == 1000000 && null != data) {
				int totalElements = (int) data.get("totalElements");
				if (totalElements > 0) {
					return true;
				}
			}
			return false;
		}
		return false;
	}


	@Override
	@Async
	public void report2Sensors(String ucId, Integer searchCount, String keywords, int terminal) {

		try {
			log.info("report2Sensors is start");
			Map<String, Object> properties = Maps.newHashMap();

			properties.put("first_module", "题库");
			properties.put("second_module", "申论");
			properties.put("search_keyword", keywords);
			properties.put("is_having_result", searchCount > 0 ? true : false);
			properties.put("platform", SensorsUtils.getPlatform(terminal));
			log.info("report2Sensors ucId :{},properties：{}", ucId, properties.toString());
			sensorsAnalytics.track(ucId, true, SensorsEventEnum.SEARCH.getCode(), properties);
			sensorsAnalytics.flush();
		} catch (Exception e) {
			log.error("sa track error:{}", e);
		}

	}

}
