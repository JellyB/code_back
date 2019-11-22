package com.huatu.ztk.backend.paper.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.bean.PaperCheck;
import com.huatu.ztk.backend.paper.bean.PaperTemp;
import com.huatu.ztk.commons.Area;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.paper.common.PaperType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ht on 2016/12/21.
 */
@Repository
public class PaperDao {

    private static final Logger logger = LoggerFactory.getLogger(PaperDao.class);

    /**
     * 存储试题的集合名字
     */
    public static final String collection = "ztk_paper";
    public static final String collectionCopy = "ztk_paper_copy";
    public static final String collectionId = "paper_id_base";//存放试卷最大id的表
    public static final int PAPER_LIST_LIMIT = 20000;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取试卷列表
     *
     * @param catgorys
     * @param areas
     * @param year
     * @param name
     * @param types           试卷类型
     * @param notInStatusList 需要过滤的状态列表
     * @return
     */
    public List<Paper> list(List<Integer> catgorys, List<Integer> areas, int year, String name, List<Integer> types, List<Integer> notInStatusList, List<Integer> ids, int creator) {
        final Criteria criteria = Criteria.where("type").in(types);

        if (CollectionUtils.isNotEmpty(notInStatusList)) {
            criteria.and("status").nin(notInStatusList);
        }

        if (CollectionUtils.isNotEmpty(catgorys)) {
            criteria.and("catgory").in(catgorys);
        }

        if (creator > 0) {
            criteria.and("createdBy").is(creator);
        }

        if (CollectionUtils.isNotEmpty(areas)) {
            Set<Integer> totalAreas = new LinkedHashSet<>();

            for (Integer areaId : areas) {
                final Area area = AreaConstants.getArea(areaId);

                totalAreas.add(areaId);

                //将省下面的市也包含进去
                if (CollectionUtils.isNotEmpty(area.getChildren())) {
                    List<Integer> childIds = area.getChildren().stream().map(i -> i.getId()).collect(Collectors.toList());
                    totalAreas.addAll(childIds);
                }
            }
            criteria.and("area").in(totalAreas);
        }
        if (year > 0) {
            criteria.and("year").is(year);
        }

        if (StringUtils.isNoneBlank(name)) {
            criteria.and("name").regex(".*" + name + ".*");
        }

        if (CollectionUtils.isNotEmpty(ids)) {
            criteria.and("id").in(ids);
        }

        Query query = new Query(criteria);
        query.limit(PAPER_LIST_LIMIT);

        //按创建时间倒序
        query.with(new Sort(Sort.Direction.DESC, "createTime"));

        final List<Paper> papers = mongoTemplate.find(query, Paper.class);
        return papers;
    }

    public List<Paper> allDownList(List<Integer> catgorys, List<Integer> areas, int sYear, int eYear, List<Integer> ids) {
        Query query = assertQueryStatement(catgorys, areas, sYear, eYear, ids, PaperType.TRUE_PAPER, "");
        final List<Paper> papers = mongoTemplate.find(query, Paper.class);
        return papers;
    }

    private Query assertQueryStatement(List<Integer> catgorys, List<Integer> areas, int sYear, int eYear, List<Integer> ids, int paperType, String name) {
        final Criteria criteria = Criteria.where("type").is(paperType)
                .and("status").is(PaperStatus.AUDIT_SUCCESS);
//        final Criteria criteria = Criteria.where("status").is(PaperStatus.AUDIT_SUCCESS);

        if (CollectionUtils.isNotEmpty(catgorys)) {
            criteria.and("catgory").in(catgorys);
        }
        if (CollectionUtils.isNotEmpty(areas)) {
            criteria.and("area").in(areas);
        }
        if (sYear > 0 && eYear > 0) {
            criteria.and("year").gte(sYear).lt(eYear);
        } else {
            if (sYear > 0) {
                criteria.and("year").gte(sYear);

            } else if (eYear > 0) {
                criteria.and("year").lte(eYear);
            }
        }

        if (CollectionUtils.isNotEmpty(ids)) {
            criteria.and("id").in(ids);
        }
        if (!"".equals(name)) {
            criteria.and("name").regex(".*" + name + ".*");
        }
        Query query = new Query(criteria);
        query.limit(PAPER_LIST_LIMIT);

        //按年份排序
        query.with(new Sort(Sort.Direction.DESC, "createTime"));
        return query;
    }

    /**
     * 保存试卷
     *
     * @param paper
     */

    public void createPaper(Paper paper) {
        logger.info("create paper={}", JsonUtil.toJson(paper));
        mongoTemplate.insert(paper);
    }

    /**
     * 获取试卷详情
     *
     * @param id
     * @return
     */
    public Paper findById(int id) {
        return mongoTemplate.findById(id, Paper.class);
    }

    public List<Paper> findByIds(List<Integer> ids) {
        Criteria criteria = Criteria.where("id").in(ids);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, "id"));
        return mongoTemplate.find(query, Paper.class, "ztk_paper");
    }

    public Match findMatchById(int id) {
        return mongoTemplate.findById(id, Match.class);
    }

    public Paper findByIdCopy(int id) {
        return mongoTemplate.findById(id, Paper.class, collectionCopy);
    }

    /**
     * 修改保存
     *
     * @param paper
     */
    public void update(Paper paper) {
        logger.info("update paper:{}", JsonUtil.toJson(paper));
        mongoTemplate.save(paper);

    }

    /**
     * 查询试卷最大id,并自加1
     *
     * @return
     */
    public int findMaxId() {
        return findMaxId(1);
    }

    public int findMaxId(int num) {
        Criteria criteria = Criteria.where("id").is(1);
        Query query = new Query(criteria);
        Update update = new Update().inc("paperId", num);
        int id = -1;
        PaperTemp paperTemp = mongoTemplate.findAndModify(query, update, PaperTemp.class, collectionId);
        id = paperTemp.getPaperId();
        return id;
    }

    /**
     * 根据试卷名字和类型查询
     *
     * @param name
     * @return
     */
    public Paper findByNameAndType(String name, int type) {
        Criteria criteria = Criteria.where("name").is(name).and("type").is(type).and("status").ne(BackendPaperStatus.DELETED);
        Query query = new Query(criteria);
        Paper paper = mongoTemplate.findOne(query, Paper.class);
        return paper;
    }

    /**
     * 根据试卷类型查询试试卷列表
     *
     * @param types
     * @return
     */
    public List<Paper> findByType( List<Integer> types) {
        Criteria criteria = Criteria.where("type").in(types).and("status").ne(BackendPaperStatus.DELETED);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC,"startTime"));
        List<Paper> papers = mongoTemplate.find(query, Paper.class);
        return papers;
    }
    // 获取指定试卷pid的最新的审核意见
    public List<PaperCheck> getPaperCheckByPids(List<Integer> pids) {
        if (CollectionUtils.isNotEmpty(pids)) {
            StringBuilder sbr = new StringBuilder();
            for (Integer pid : pids) {
                sbr.append(pid + ",");
            }
            String param = sbr.toString().substring(0, sbr.toString().lastIndexOf(","));
            String sql = "select * from (" +
                    "select * from v_paper_check_log " +
                    "where paper_id in ( " + param + " ) " +
                    "ORDER BY check_time DESC,pukey DESC " +
                    ") s " +
                    "GROUP BY paper_id";
//            Object[] params = {
//                    param
//            };
            logger.info("执行查询，获取每个试卷最新审核信息：{}", sql);
            final List<PaperCheck> paperChecks = jdbcTemplate.query(sql, new PaperCheckRow());
            return paperChecks;
        }
        return Lists.newArrayList();
    }

    //查找指定试卷指定状态的试卷
    public List<PaperCheck> getPaperCheckByStatus(int pid, int status) {
        String sql = "select * from v_paper_check_log where paper_id = ? and check_status=?";
        Object[] param = {pid, status};
        final List<PaperCheck> paperChecks = jdbcTemplate.query(sql, param, new PaperCheckRow());
        return paperChecks;
    }

    //提交审核执行
    public boolean insertPaperCheck(PaperCheck paperCheck) {
        String sql = "INSERT  INTO v_paper_check_log (check_status,paper_id,applier_id,apply_time) VALUES (?,?,?,?) ";
        Object[] param = {
                paperCheck.getCheckStatus(),
                paperCheck.getPaperId(),
                paperCheck.getApplierId(),
                paperCheck.getApplyTime()
        };
        return jdbcTemplate.update(sql, param) > 0 ? true : false;
    }

    //审核通过或者拒绝执行
    public boolean updatePaperCheck(PaperCheck paperCheck) {
        String sql = "update v_paper_check_log set check_time = ? ,checker_id = ? ,check_status =?, suggestion=? where PUKEY = ?  ";
        Object[] param = {
                paperCheck.getCheckTime(),
                paperCheck.getCheckId(),
                paperCheck.getCheckStatus(),
                paperCheck.getSuggestion(),
                paperCheck.getId()
        };
        return jdbcTemplate.update(sql, param) > 0 ? true : false;
    }

    //更新试卷中状态
    public void updatePaperStatus(int pid, int status) {
        Query query = new Query(Criteria.where("id").is(pid));
        Update update = new Update().set("status", status);
        mongoTemplate.updateFirst(query, update, Paper.class, collection);
    }

    //批量更新试卷中状态
    public void updateBatchPaperStatus(List pids, int status) {
        Query query = new Query(Criteria.where("id").in(pids.toArray()));
        Update update = new Update().set("status", status);
        mongoTemplate.updateFirst(query, update, Paper.class, collection);
    }

    /**
     * 查询试卷的总分
     *
     * @param paperId
     * @return
     */
    public int findPaperScore(int paperId) {
        Paper paper = mongoTemplate.findById(paperId, Paper.class);
        int score = 100;
        if (paper.getScore() != 0) {
            score = paper.getScore();
        }
        logger.info("试卷分数为{}", score);
        return score;
    }


    public List<Paper> findPaperByRange(int startId, int endId) {
        Criteria criteria = Criteria.where("id").gte(startId).lte(endId).and("status").ne(BackendPaperStatus.DELETED);
        Query query = new Query(criteria);
        List<Paper> papers = mongoTemplate.find(query, Paper.class);
        papers.removeIf(i -> CollectionUtils.isEmpty(i.getQuestions()));
        logger.info("papers's size is = {}", papers.size());
        return papers;
    }

    public List<Paper> allDownListV1(List<Integer> catgoryIds, List<Integer> areas, int sYear, int eYear, int paperType, String name) {
        Query query = assertQueryStatement(catgoryIds, areas, sYear, eYear, null, paperType, name);
        final List<Paper> papers = mongoTemplate.find(query, Paper.class);
        return papers;
    }

    public Long countBySubject(int subjectId) {
        Query query = new Query(Criteria.where("catgory").is(subjectId));
        return mongoTemplate.count(query, Paper.class);
    }

    public List<Paper> findQuestionsForPage(int cursor, int size, int subjectId) {
        Query query = new Query(Criteria.where("id").gt(cursor).and("catgory").is(subjectId));
        query.with(new Sort(Sort.Direction.ASC, "id")).limit(size);
        return mongoTemplate.find(query, Paper.class);
    }

    /**
     * 根据科目和类型查询某个区段的考试试卷
     *
     * @param subject
     * @param startTime
     * @param endTime
     * @param paperTypes
     * @return
     */
    public List<Paper> findPaperList(Integer subject, Long startTime, Long endTime, List<Integer> paperTypes) {
        Criteria criteria = Criteria.where("status").is(2);
        if (subject != -1) {
            criteria.and("catgory").is(subject);
        }
        criteria.and("startTime").gte(startTime).lte(endTime);
        criteria.and("type").in(paperTypes);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Paper.class);
    }

    /**
     * 根据科目和试卷类型查询试卷
     *
     * @param subjectId
     * @return
     */
    public List<Paper> findBySubject(int subjectId, List<Integer> types) {
        Criteria criteria = Criteria.where("status").is(2);
        criteria.and("catgory").is(subjectId);
        criteria.and("type").in(types);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Paper.class);
    }


    class PaperCheckRow implements RowMapper<PaperCheck> {

        @Override
        public PaperCheck mapRow(ResultSet rs, int rowNum) throws SQLException {
            final PaperCheck paperCheck = PaperCheck.builder()
                    .id(rs.getInt("pukey"))
                    .checkTime(rs.getLong("check_time"))
                    .applyTime(rs.getLong("apply_time"))
                    .checkStatus(rs.getInt("check_status"))
                    .paperId(rs.getInt("paper_id"))
                    .applierId(rs.getInt("applier_id"))
                    .checkId(rs.getInt("checker_id"))
                    .suggestion(rs.getString("suggestion"))
                    .build();
            return paperCheck;
        }
    }
}
