package com.huatu.ztk.paper.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.Area;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperSummary;
import com.huatu.ztk.paper.common.EstimateConstants;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.util.TeacherSubjectManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 试题dao层
 * Created by shaojieyue
 * Created time 2016-04-28 19:07
 */

@Repository
public class PaperDao {
    private static final Logger logger = LoggerFactory.getLogger(PaperDao.class);
    public static final int ID_BASE = 2000000;//id基数，防止跟以前的id冲突
    /**
     * 存储试题的集合名字
     */
    public static final String collection = "ztk_paper";

    @Autowired
    private MongoTemplate mongoTemplate;

    /*   缓存模考估分列表数据 */
    private static final Cache<String, List<EstimatePaper>> estimatePaperListCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(2).build();

    /**
     * 插入试卷
     *
     * @param paper
     */
    public void insert(Paper paper) {
        logger.info("insert paper:{}", paper);
        long id = -1;
        for (int i = 0; i < 5; i++) {//此处循环是未来保证key冲突的情况下也能插入到mongo里
            try {
                if (paper.getId() < 1) {//没有设置id，则生成id此处主要是数据初始化时用到
                    id = mongoTemplate.count(new Query(), collection) + ID_BASE;
                }
                mongoTemplate.insert(paper, collection);
                id = paper.getId();//获取id
                break;//插入成功，则跳出循环
            } catch (Exception e) {
            }
        }
    }

    public void save(Paper paper) {
        logger.info("save paper:{}", paper);
        mongoTemplate.save(paper);
    }

    /**
     * 根据id查询试卷
     *
     * @param id
     * @return
     */
    public Paper findById(int id) {
        return mongoTemplate.findById(id, Paper.class);
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

        Criteria criteria = new Criteria();

        //TODO 有没有好点的办法判断
        if (areaId != -1 && year != 0) {
            criteria.andOperator(Criteria.where("year").is(year), Criteria.where("area").is(areaId));
        } else if (areaId == -1 && year != 0) {
            criteria.andOperator(Criteria.where("year").is(year));
        } else if (areaId != -1 && year == 0) {
            criteria.andOperator(Criteria.where("area").is(areaId));
        }

        Query query = new Query(criteria);
        query.skip(startPage * pageSize);
        query.limit(pageSize);
        query.with(new Sort(Sort.Direction.DESC, "year"));
        return mongoTemplate.find(query, Paper.class);
    }

    /**
     * 根据科目和试卷类型来汇总试卷个数
     *
     * @param type
     * @return
     */
    public List<PaperSummary> summaryPageCount(List<Integer> subjects, int type) {
        final Criteria criteria = Criteria.where("catgory").in(subjects)
                .and("type").is(type)
                .and("status").is(PaperStatus.AUDIT_SUCCESS);
        GroupByResults<PaperSummary> results = mongoTemplate.group(
                criteria,
                collection,
                GroupBy.key("area").initialDocument("{ paperCount: 0 }").reduceFunction("function(doc, prev) { prev.paperCount += 1 }"),
                PaperSummary.class);
        return Lists.newArrayList(results.iterator());
    }

    /**
     * 分页查询Paper
     * 试卷列表做倒序排序
     *
     * @param subjects
     * @param areaId
     * @param paperType
     * @param size
     * @return
     */
    public List<Paper> findForPage(List<Integer> subjects, int areaId, int year, int paperType, int page, int size) {
        final Criteria criteria = Criteria.where("catgory").in(subjects)//指定科目
                .and("type").is(paperType)//指定类型
                .and("status").is(PaperStatus.AUDIT_SUCCESS);

        if (year > 0) {//加入年的限制
            criteria.and("year").is(year);
        }


        if (areaId > 0 || areaId == AreaConstants.QUAN_GUO_ID) {
            final Area area = AreaConstants.getArea(areaId);
            if (area.getParentId() > 0) {//说明是根据市区id查询的
                criteria.and("area").is(areaId);//指定区域
            } else {//根据省份查询,这个地方要注意,根据省份时,也要把市的给查询出来
                List<Integer> areaIds = Lists.newArrayList();
                areaIds.add(area.getId());
                for (Area area1 : area.getChildren()) {
                    areaIds.add(area1.getId());
                }
                criteria.and("area").in(areaIds);
            }
        }

        Query query = new Query(criteria);

        //按年份排序
        query.skip((page - 1) * size).limit(size).with(new Sort(Sort.Direction.DESC, "year"))
                .with(new Sort(Sort.Direction.DESC, "createTime"));

        final List<Paper> papers = mongoTemplate.find(query, Paper.class);
        return papers;
    }

    /**
     * 模考卷列表
     *
     * @param page
     * @param size
     * @return
     */
    public List<EstimatePaper> findEstimatePaper(int subject, int page, int size) {
//        String key = new StringBuilder(subject).append("_").append(page).append("_").append(size).toString();
//        List<EstimatePaper> estimatePapers = estimatePaperListCache.getIfPresent(key);
//        if(CollectionUtils.isNotEmpty(estimatePapers)){
//            logger.info("  huang  estimatePaperListCache   ");
//            return  estimatePapers;
//        }

        //只返回估分
        final Criteria criteria = Criteria
                .where("status").is(PaperStatus.AUDIT_SUCCESS)
                .and("catgory").is(subject)   //添加科目查询条件(默认是1:公务员考试)
                .and("offlineTime").gt(System.currentTimeMillis())  //表示未下线的试卷
                .and("hideFlag").is(EstimateConstants.NOT_HIDE)
                .and("type").in(PaperType.CUSTOM_PAPER, PaperType.ESTIMATE_PAPER);

        Query query = new Query(criteria);

        //按上线时间排序
        query.skip((page - 1) * size).limit(size)
                .with(new Sort(Sort.Direction.DESC, "onlineTime"));

        List<EstimatePaper> estimatePapers = mongoTemplate.find(query, EstimatePaper.class);
        //  estimatePaperListCache.put(key,estimatePapers);
        return estimatePapers;
    }

    //TODO 是否慢查询？mongo端检测
    public List<EstimatePaper> findNewEstimatePaper(int subject, int page, int size, List<Integer> types) {
        Supplier<Query> commonQuery = ()->{
            final Criteria criteria = Criteria
                    .where("status").is(PaperStatus.AUDIT_SUCCESS)
                    .and("catgory").is(subject)   //添加科目查询条件(默认是1:公务员考试)
                    .and("offlineTime").gt(System.currentTimeMillis())  //表示未下线的试卷
                    .and("hideFlag").is(EstimateConstants.NOT_HIDE);

            if (CollectionUtils.isNotEmpty(types)) {
                criteria.and("type").in(types);
            }

            Query query = new Query(criteria);

            //按上线时间排序
            query.skip((page - 1) * size).limit(size)
                    .with(new Sort(Sort.Direction.DESC, "type"))
                    .with(new Sort(Sort.Direction.DESC, "onlineTime"));
            return query;
        };
        List<Integer> teacherIds = TeacherSubjectManager.TEACHER_SUBJECT_IDS;
        Supplier<Query> teacherQuery = ()->{
            final Criteria criteria = Criteria
                    .where("status").is(PaperStatus.AUDIT_SUCCESS)
                    .and("catgory").in(teacherIds)   //添加科目查询条件(默认是1:公务员考试)
                    .and("offlineTime").gt(System.currentTimeMillis())  //表示未下线的试卷
                    .and("hideFlag").is(EstimateConstants.NOT_HIDE);

            if (CollectionUtils.isNotEmpty(types)) {
                criteria.and("type").in(types);
            }

            Query query = new Query(criteria);

            return query;
        };
        if(!teacherIds.contains(subject)){
            List<EstimatePaper> estimatePapers = mongoTemplate.find(commonQuery.get(), EstimatePaper.class);
            return estimatePapers;
        }else{
            List<EstimatePaper> estimatePapers = mongoTemplate.find(teacherQuery.get(), EstimatePaper.class);
            if(CollectionUtils.isEmpty(estimatePapers)){
                return estimatePapers;
            }
            switch (subject){
                case 200100049:
                case 200100050:
                    return estimatePapers.stream().sorted(TeacherSubjectManager.zong_su_comparator.thenComparing(TeacherSubjectManager.gradeComparator)).skip((page - 1) * size).limit(size).collect(Collectors.toList());
                case 200100051:
                case 200100052:
                    return estimatePapers.stream().sorted(TeacherSubjectManager.jiao_zhi_comparator.thenComparing(TeacherSubjectManager.gradeComparator)).skip((page - 1) * size).limit(size).collect(Collectors.toList());
                default:
                    return estimatePapers;
            }
        }
    }

    public List<Paper> findBathByIds(List<Integer> paperIds) {
        final Criteria criteria = Criteria
                .where("id").in(paperIds);
        Query query = new Query(criteria);
        List<Paper> papers = mongoTemplate.find(query, Paper.class);
        if (CollectionUtils.isEmpty(paperIds)) {
            return Lists.newArrayList();
        }
        return papers;
    }

    public List<EstimatePaper> findEstimatePaperList(int subject, Integer type) {
        final Criteria criteria = Criteria
                .where("status").is(PaperStatus.AUDIT_SUCCESS)
                .and("catgory").is(subject)   //添加科目查询条件(默认是1:公务员考试)
                .and("offlineTime").gt(System.currentTimeMillis())  //表示未下线的试卷
                .and("hideFlag").is(EstimateConstants.NOT_HIDE)
                .and("type").is(type);

        Query query = new Query(criteria);
        //按上线时间排序
        query.with(new Sort(Sort.Direction.DESC, "type"))
                .with(new Sort(Sort.Direction.DESC, "onlineTime"));
        List<EstimatePaper> estimatePapers = mongoTemplate.find(query, EstimatePaper.class);
        return estimatePapers;
    }

    /**
     * 根据试卷开始结束时间和科目，类型查询符合的试卷
     * @param subject
     * @param paperType
     * @param current
     * @return
     */
    public List<EstimatePaper> findEstimatePaperByTypeAndTime(int subject, int paperType, long current) {
        final Criteria criteria = Criteria
                .where("status").is(PaperStatus.AUDIT_SUCCESS)
                .and("catgory").is(subject)   //添加科目查询条件(默认是1:公务员考试)
                .and("startTime").lte(current)  //表示未下线的试卷
                .and("endTime").gt(current)
                .and("type").is(paperType);

        Query query = new Query(criteria);
        //按上线时间排序
        query.with(new Sort(Sort.Direction.DESC, "startTime"));
        List<EstimatePaper> estimatePapers = mongoTemplate.find(query, EstimatePaper.class);
        return estimatePapers;
    }

    /**
     * 查询所有的考试科目
     * @param subject
     * @param paperType
     * @return
     */
    public List<EstimatePaper> findEstimatePaperByType(int subject, int paperType) {
        final Criteria criteria = Criteria
                .where("status").is(PaperStatus.AUDIT_SUCCESS)
                .and("catgory").is(subject)   //添加科目查询条件(默认是1:公务员考试)
                .and("type").is(paperType);

        Query query = new Query(criteria);
        //按上线时间排序
        query.with(new Sort(Sort.Direction.DESC, "startTime"));
        List<EstimatePaper> estimatePapers = mongoTemplate.find(query, EstimatePaper.class);
        return estimatePapers;
    }
}
