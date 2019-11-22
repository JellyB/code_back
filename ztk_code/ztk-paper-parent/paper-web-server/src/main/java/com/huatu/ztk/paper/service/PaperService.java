package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.utils.cache.NullHolder;
import com.huatu.ztk.commons.Area;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.spring.serializer.StringRedisKeySerializer;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.paper.bean.*;
import com.huatu.ztk.paper.common.EstimateStatus;
import com.huatu.ztk.paper.common.LookParseStatus;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.dao.MatchDao;
import com.huatu.ztk.paper.dao.PaperDao;
import com.huatu.ztk.paper.service.v4.EssayPaperService;
import com.huatu.ztk.paper.service.v4.PaperServiceV4;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 试卷业务层
 * Created by shaojieyue
 * Created time 2016-04-28 19:11
 */

@Service
public class PaperService {
    private static final Logger logger = LoggerFactory.getLogger(PaperService.class);
    @Autowired
    private PaperDao paperDao;

    @Resource
    private RedisTemplate redisTemplate;
    @Resource(name = "coreRedisTemplate")
    private ValueOperations valueOperations;

    @Resource(name = "coreRedisTemplate")
    private RedisTemplate template;

    @Autowired
    private PaperUserMetaService paperUserMetaService;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private PaperAnswerCardUtilComponent utilComponent;

    @Autowired
    private BigBagUsedSubjectConfig bigBagUsedSubjectConfig;

    @Autowired
    private SubjectDubboService subjectDubboService;

    @Autowired
    private PaperServiceV4 paperServiceV4;

    @Autowired
    private MatchDao matchDao;

    @Autowired
    private EssayPaperService essayPaperService;

    @Autowired
    private PaperAnswerCardService paperAnswerCardService;

    private static final String PAPER_REDIS_KEY = "paper_info_by_id";

    /**
     * 插入试卷
     *
     * @param paper
     * @return
     */
    public boolean insert(Paper paper) {
        boolean success = true;
        try {
            paperDao.insert(paper);
        } catch (Throwable e) {
            logger.error("insert paper fail. paper:{}", paper, e);
            success = false;
        }
        return success;
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    public Paper findById(int id) {
        String key = PaperRedisKeys.getPaperKey(id);
        Object object = valueOperations.get(key);
        if (object instanceof NullHolder) {
            return null;
        }
        if (object == null) {
            final Paper paper = paperDao.findById(id);
            valueOperations.set(key, paper == null ? NullHolder.DEFAULT : paper, 1, TimeUnit.HOURS);
            return paper;
        } else {
            return (Paper) object;
        }
    }

    /**
     * 根据给定条件查询对应的试卷列表
     *
     * @param areaId    区域ID
     * @param year      年费
     * @param startPage 起始页
     * @param pageSize  每页记录数
     * @return
     */
    public List<Paper> findByConditions(int areaId, int year, int startPage, int pageSize) {
        return paperDao.findByConditions(areaId, year, startPage, pageSize);
    }

    /**
     * 根据用户查询试题列表
     *
     * @param subjects 科目
     * @param area     用户考试区域
     * @param type
     * @return
     */
    public List<PaperSummary> summary(List<Integer> subjects, int area, int type) {
        final List<PaperSummary> paperSummaries = paperDao.summaryPageCount(subjects, type);
        PaperSummary quanGuo = null;
        PaperSummary myArea = null;
        reSortByAreaOrder(paperSummaries);
        List<PaperSummary> finalSummaries = new ArrayList<>();
        for (PaperSummary paperSummary : paperSummaries) {//遍历设置区域名字
            Area areaObj = AreaConstants.getArea(paperSummary.getArea());
            if (areaObj == null) {
                continue;
            }

            String areaName = areaObj.getName();

            paperSummary.setAreaName(areaName);
            if (paperSummary.getArea() == AreaConstants.QUAN_GUO_ID) {//全国区域
                quanGuo = paperSummary;
            } else if (paperSummary.getArea() == area) {//用户选择的考试区域
                myArea = paperSummary;
            }

            //添加到新的里面
            finalSummaries.add(paperSummary);
        }


        if (quanGuo != null) {//全国不为空，则将全国排到最前边
            finalSummaries.remove(quanGuo);
            finalSummaries.add(0, quanGuo);
        }

        if (myArea != null) {//用户选择不为空，则将用户的区域排到最靠前
            finalSummaries.remove(myArea);
            finalSummaries.add(0, myArea);
        }
        //最终汇总顺序： 用户自己区域，全国区域，其他
        //logger.info("summaries:1:2:{}", finalSummaries);
        return finalSummaries;
    }

    private void reSortByAreaOrder(List<PaperSummary> paperSummaries) {
        paperSummaries.sort(((a, b) -> (compareToArea(a.getArea(), b.getArea()))));

    }

    private int compareToArea(int a, int b) {
        List<Integer> areaList = AreaConstants.AREA_ORDER;
        if (areaList.indexOf(a) != -1 && areaList.indexOf(b) != -1) {
            return areaList.indexOf(a) - areaList.indexOf(b);
        } else if (areaList.indexOf(a) != -1 && areaList.indexOf(b) == -1) {
            return -1;
        } else if (areaList.indexOf(b) != -1 && areaList.indexOf(a) == -1) {
            return 1;
        } else {
            return a - b;
        }

    }


    public List<PaperSummary> summaryNew(List<Integer> subjects, int area, int type) {
        List<PaperSummary> summaries = summary(subjects, area, type);
        logger.info("summaries:2:1:{}", summaries);
        //areaid-->paper count
        Map<Integer, Integer> areaPaperCountMap = new LinkedHashMap<>();
        for (PaperSummary summary : summaries) {
            int area1 = summary.getArea();
            int topAreaId = getTopAreaId(area1);
            areaPaperCountMap.put(topAreaId, areaPaperCountMap.getOrDefault(topAreaId, 0) + summary.getPaperCount());
        }

        List<PaperSummary> resultList = areaPaperCountMap.keySet().stream()
                .map(areaId -> PaperSummary.builder()
                        .area(areaId)
                        .areaName(AreaConstants.getArea(areaId).getName())
                        .paperCount(areaPaperCountMap.get(areaId))
                        .build()).collect(Collectors.toList());
        logger.info("summaries:2:2:{}", resultList);
        return resultList;
    }

    /**
     * 顶级area id
     *
     * @param areaId
     * @return
     */
    private int getTopAreaId(int areaId) {
        Area area = AreaConstants.getArea(areaId);

        while (area.getParentId() != 0) {
            area = AreaConstants.getArea(area.getParentId());
        }
        return area.getId();
    }

    private String getAreaName(List<Area> areas, int areaId) {
        for (Area area1 : areas) {
            if (area1.getId() == areaId) {
                return area1.getName();
            }
        }

        return "";
    }

    /**
     * 分页查询试卷列表
     *
     * @param subjects 科目
     * @param area     区域
     * @param page     页码
     * @param size     分页大小
     * @return
     */
    public PageBean findForPage(List<Integer> subjects, int area, int year, int paperType, int page, int size, long uid) {
        final List<Paper> papers = paperDao.findForPage(subjects, area, year, paperType, page, size);
        PageBean pageBean = new PageBean(papers, 0, 0);
        //非游客模式
        if (uid > 0) {
            fillPaperUserMeta(pageBean.getResutls(), uid);
        }
        return pageBean;
    }

    /**
     * 分页查询试卷列表
     *
     * @param subjects 科目
     * @param area     区域
     * @param page     页码
     * @param size     分页大小
     * @return
     */
    public PageBean findForPageNoUser(List<Integer> subjects, int area, int year, int paperType, int page, int size) {
        final List<Paper> papers = paperDao.findForPage(subjects, area, year, paperType, page, size);
        PageBean pageBean = new PageBean(papers, 0, 0);
        fillPaperUserMeta(pageBean.getResutls(), -1);
        return pageBean;
    }

    public List<Paper> findBathByIds(List<Integer> paperIds) {
        return paperDao.findBathByIds(paperIds);
    }

    /**
     * 模考估分卷
     *
     * @param subject 科目
     * @param area
     * @param page
     * @return
     */
    public List<EstimatePaper> getEstimatePapers(int subject, int area, int page, int size, long userId) {
        List<EstimatePaper> papers = paperDao.findEstimatePaper(subject, page, size);


        return filterEstimatePaper(papers, userId);
    }

    public List<EstimatePaper> filterEstimatePaper(List<EstimatePaper> papers, long userId) {

        if (CollectionUtils.isEmpty(papers)) {
            return Collections.emptyList();
        }

        //papers为空，返回空表
        List<EstimatePaper> ret = Lists.newLinkedList();

        //填充做题信息
        fillPaperUserMeta(papers, userId);


        for (EstimatePaper paper : papers) {
            long startTime = paper.getStartTime();
            long endTime = paper.getEndTime();
            long offlineTime = paper.getOfflineTime();
            long currentTime = System.currentTimeMillis();

            if (currentTime < startTime) {
                paper.setStatus(EstimateStatus.NOT_START);
            } else if (currentTime < endTime) {
                paper.setStatus(EstimateStatus.ONLINE);
            } else if (currentTime < offlineTime) {
                paper.setStatus(EstimateStatus.END);
            }

            PaperUserMeta userMeta = paper.getUserMeta();
            if (userMeta != null) {
                //用户已经交卷
                if (userMeta.getCurrentPracticeId() == -1) {
                    if (paper.getLookParseTime() == LookParseStatus.IMMEDIATELY
                            || (paper.getLookParseTime() == LookParseStatus.AFTER_END
                            && paper.getEndTime() < currentTime)) {
                        //可以立即查看或者（结束后查看，当前时间大于结束时间）
                        paper.setStatus(EstimateStatus.REPORT_AVAILABLE);

                    } else if (paper.getLookParseTime() == LookParseStatus.AFTER_END
                            && paper.getEndTime() > currentTime) {

                        //状态是结束后查看，模考结束时间大于当前时间,将状态设置为未出报告
                        paper.setStatus(EstimateStatus.REPORT_UNAVILABLE);
                    }
                } else {
                    //可以继续做题
                    paper.setStatus(EstimateStatus.CONTINUE_AVAILABLE);
                }
            }

            //设置交卷人数,兼容旧版本，保留答题人数
            final ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            String paperPracticeIdSore = PaperRedisKeys.getPaperPracticeIdSore(paper.getId());
            long cardCounts = zSetOperations.zCard(paperPracticeIdSore);
            //转int
            PaperMeta paperMeta = PaperMeta.builder()
                    .cardCounts(Integer.valueOf(cardCounts + "")).build();
            paper.setPaperMeta(paperMeta);
            //游客模式,用户信息为空
            if (userId < 0) {
                PaperUserMeta paperUserMeta = new PaperUserMeta();
                paper.setUserMeta(paperUserMeta);
            }
            ret.add(paper);
        }
        return ret;
    }


    /**
     * 试卷列表填充用户做题信息
     *
     * @param papers
     * @param uid
     */
    private void fillPaperUserMeta(List<? extends Paper> papers, long uid) {
        List<Integer> paperIds = new ArrayList(papers.size());
        for (Paper paper : papers) {
            paperIds.add(paper.getId());
        }
        if (uid == -1) {
            return;
        }
        //批量查询meta信息
        final List<PaperUserMeta> paperUserMetas = paperUserMetaService.findBatch(uid, paperIds);
        for (Paper paper : papers) {//遍历试卷，填充用户做卷信息
            for (PaperUserMeta paperUserMeta : paperUserMetas) {
                if (paperUserMeta.getPaperId() == paper.getId()) {//met信息匹配则设置
                    //设置meta信息
                    //小程序需要支持字符串类型
                    String currentPracticeIdStr = String.valueOf(paperUserMeta.getCurrentPracticeId());
                    paperUserMeta.setCurrentPracticeIdStr(currentPracticeIdStr);

                    List<Long> practiceIds = paperUserMeta.getPracticeIds();
                    List<String> practiceIdsStr = Lists.newArrayList();
                    if (CollectionUtils.isNotEmpty(practiceIds)) {
                        practiceIdsStr = practiceIds.stream().map(practiceId -> String.valueOf(practiceId)).collect(Collectors.toList());
                    }
                    paperUserMeta.setPracticeIdsStr(practiceIdsStr);
                    paper.setUserMeta(paperUserMeta);
                }
            }
        }
    }

    /**
     * 查询试卷列表,此处不加载用户个人信息
     *
     * @param category
     * @param area
     * @param year
     * @param paperType
     * @param page
     * @param size
     * @return
     */
    public PageBean<Paper> findForPage(int category, int area, int year, int paperType, int page, int size) {
        final List<Paper> papers = paperDao.findForPage(Arrays.asList(category), area, year, paperType, page, size);
        PageBean pageBean = new PageBean(papers, 0, 0);
        return pageBean;
    }

    /**
     * 模考估分
     * v3
     *
     * @param subject
     * @param page
     * @param size
     * @param userId
     * @return
     */
    public List<EstimatePaper> getNewEstimatePapers(int subject, int page, int size, List<Integer> types, long userId, int terminal) throws BizException {
        List<EstimatePaper> papers = cacheEstimatePapers(subject, page, size, types,
                () -> paperDao.findNewEstimatePaper(subject, page, size, types));
        logger.info("subject is:{},types is :{}", subject, types);
        /**
         * update by lizhenjuan  增加事业单位公基科目估分活动
         */
        if (bigBagUsedSubjectConfig.isEnabledUserSubject(subject)) {
            if (types.contains(PaperType.ESTIMATE_PAPER)) {
                papers = utilComponent.addGiftInfoForEstimateSearchList(papers);
                logger.info("估分活动");
            }
        }
        if (userId != 0) {
            List<EstimatePaper> estimatePapers = filterEstimatePaper(papers, userId);
            //小程序估分列表,添加学员分数
            if (terminal == TerminalType.WEI_XIN_APPLET) {
                estimatePapers.stream().forEach(paper -> {
                    if (null != paper.getUserMeta()) {
                        if (paper.getStatus() == EstimateStatus.REPORT_AVAILABLE) {
                            List<Long> practiceIds = paper.getUserMeta().getPracticeIds();
                            if (CollectionUtils.isNotEmpty(practiceIds)) {
                                try {
                                    AnswerCard answerCard = paperAnswerCardService.findById(practiceIds.get(0), userId);
                                    if (null != answerCard) {
                                        paper.getUserMeta().setScore(answerCard.getScore());
                                    }
                                } catch (BizException e) {
                                    e.printStackTrace();
                                    logger.info("小程序估分列表查询答题卡信息错误");
                                }
                            }
                        }
                    }
                });
            }
            return estimatePapers;
        }
        return papers;
    }

    public List<EstimatePaper> getNewEstimatePapers(int subject, int page, int size, List<Integer> types) {
        List<EstimatePaper> papers = cacheEstimatePapers(subject, page, size, types,
                () -> paperDao.findNewEstimatePaper(subject, page, size, types));
        //只有行测&&精准估分处理
        if (bigBagUsedSubjectConfig.isEnabledUserSubject(subject) && types.contains(PaperType.ESTIMATE_PAPER)) {
            papers = utilComponent.addGiftInfoForEstimateSearchList(papers);
        }
        return papers;
    }

    /**
     * 模考估分数据缓存策略
     *
     * @param supplier
     * @return
     */
    private List<EstimatePaper> cacheEstimatePapers(
            int subject, int page, int size, List<Integer> types,
            Supplier<List<EstimatePaper>> supplier) {
        if (page > 1) {
            return supplier.get();
        }
        final String estimatePaperCacheKey = getEstimatePaperCacheKey(subject, page, size, types);
        List<EstimatePaper> estimatePapers = (List<EstimatePaper>) valueOperations.get(estimatePaperCacheKey);
        logger.info("精分估分查询信息,subject = {},page = {},size = {},types = {},缓存信息(paperIds) = {}",
                subject, page, size, types.stream().map(String::valueOf).collect(Collectors.joining(",")),
                estimatePapers == null ? "" : estimatePapers.stream().map(EstimatePaper::getId).map(String::valueOf).collect(Collectors.joining(","))
        );
        logger.info("精分估分缓存key:{}", estimatePaperCacheKey);
        if (null == estimatePapers || estimatePapers.size() == 0) {
            List<EstimatePaper> estimatePaperList = supplier.get();
            valueOperations.set(estimatePaperCacheKey, estimatePaperList, 15, TimeUnit.MINUTES);
            //存储当前的key值,方便查找
            SetOperations<String, String> setOperations = redisTemplate.opsForSet();
            setOperations.add(getEstimatePaperCacheAllKeyInfo(), getSerializerKey(estimatePaperCacheKey));
            return estimatePaperList;
        }
        return new ArrayList<>(estimatePapers);
    }

    /**
     * 清除当前的缓存信息
     */
    public List<String> clearEstimatePaperCache(String subjectId) {
        final String prefixKey = new StringBuffer(128)
                .append("estimatePapers")
                .append(":")
                .append(subjectId).append(":").toString();
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        Set<String> members = setOperations.members(getEstimatePaperCacheAllKeyInfo());
        if (members == null) {
            return new ArrayList<>();
        }
        members.stream()
                .filter(key -> key.indexOf(prefixKey) > 0)

                .forEach(key -> {
                    //1.删除缓存key
                    redisTemplate.delete(key);
                    //2.删除总记录中的key值
                    setOperations.remove(getEstimatePaperCacheAllKeyInfo(), key);
                });
        return members.stream().filter(key -> key.indexOf(prefixKey) > 0).collect(Collectors.toList());
    }

    private static String getEstimatePaperCacheKey(int subject, int page, int size, List<Integer> types) {
        StringBuilder key = new StringBuilder(128);
        String type = types.stream().map(String::valueOf).collect(Collectors.joining("_"));
        key.append("estimatePapers").append(":")
                .append(subject).append(":")
                .append(page).append(":")
                .append(size).append(":")
                .append(type);
        return key.toString();
    }

    private String getSerializerKey(String key) {
        StringRedisKeySerializer bean = (StringRedisKeySerializer) context.getBean("stringRedisKeySerializer");
        byte[] serialize = bean.serialize(key);
        return new String(serialize);
    }

    private static String getEstimatePaperCacheAllKeyInfo() {
        return "estimatePapers:cache:keys";
    }

    public List<EstimatePaper> findByUidAndType(int type, int subjectId) {
        List<EstimatePaper> papers = paperDao.findEstimatePaperByType(subjectId, type);
        if (CollectionUtils.isEmpty(papers)) {
            return Lists.newArrayList();
        }
        long l = System.currentTimeMillis();
        return papers.stream().filter(i -> i.getStartTime() > l)
                .collect(Collectors.toList());
    }

    public Object getPaperIdsGroupBySubjectAndType() {
        List<SubjectTree> subjectTree = subjectDubboService.getSubjectTree();
        List<Map> result = Lists.newArrayList();
        for (SubjectTree tree : subjectTree) {
            List<SubjectTree> children = tree.getChildrens();
            if (CollectionUtils.isNotEmpty(children)) {
                for (SubjectTree child : children) {
                    BiFunction<Integer, Integer, Map> pandora = ((category, subject) -> {        //pandora相关科目逻辑处理
                        HashMap<Object, Object> map = Maps.newHashMap();
                        map.put("category", category);
                        map.put("subject", subject);
                        List<Match> usefulMatch = matchDao.findUsefulMatch(subject);
                        map.put("match", CollectionUtils.isEmpty(usefulMatch) ?
                                Lists.newArrayList() :
                                usefulMatch.stream().map(Match::getPaperId).collect(Collectors.toList()));
                        List<EstimatePaper> todaySmallEstimatePaper = paperServiceV4.getTodaySmallEstimatePaper(subject);
                        map.put("small", CollectionUtils.isEmpty(todaySmallEstimatePaper) ?
                                Lists.newArrayList() :
                                todaySmallEstimatePaper.stream().map(Paper::getId).collect(Collectors.toList()));
                        return map;
                    });
                    BiFunction<Integer, Integer, Map> essay = ((category, subject) -> {        //essay相关科目逻辑处理
                        HashMap<Object, Object> map = Maps.newHashMap();
                        map.put("category", category);
                        map.put("subject", subject);
                        List<EstimateEssayPaper> essayPapers = essayPaperService.findUserFulMatch();
                        map.put("match", CollectionUtils.isEmpty(essayPapers) ?
                                Lists.newArrayList() :
                                essayPapers.stream().map(EssayPaper::getId).collect(Collectors.toList()));
                        map.put("small", Lists.newArrayList());
                        return map;
                    });
                    Boolean isEssay = child.getId() == 14;
                    if (isEssay) {
                        result.add(essay.apply(tree.getId(), child.getId()));
                    } else {
                        result.add(pandora.apply(tree.getId(), child.getId()));
                    }

                }


            }
        }

        return result;
    }
}

