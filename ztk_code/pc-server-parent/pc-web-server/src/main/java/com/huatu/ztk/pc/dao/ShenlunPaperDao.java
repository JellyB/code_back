package com.huatu.ztk.pc.dao;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.pc.bean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ht on 2016/9/23.
 */
@Repository
public class ShenlunPaperDao {
    private static final Logger logger = LoggerFactory.getLogger(ShenlunPaperDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ShenlunQuestionDao shenlunQuestionDao;

    public List<ShenlunSummary> querySummary() {

        String sql = "SELECT a.PAST_AREA,a.NUM,(CASE WHEN b.NAME IS NULL THEN 'AA国家' ELSE b.NAME END ) PASTNAME FROM (SELECT PAST_AREA,COUNT(*) NUM " +
                " FROM v_pastpaper_info WHERE bl_exam_sub=2 AND BB102=1 AND BB1B1=1 AND past_year>=2013 GROUP BY past_area) a LEFT JOIN  v_common_area b ON a.past_area=b.pukey";

        List<ShenlunSummary> summaryList = new ArrayList<>();
        final SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            Integer areaId = rs.getInt("PAST_AREA");
            String areaName = rs.getString("PASTNAME");
            Integer count = rs.getInt("NUM");
            //组装申论首页展示数据
            ShenlunSummary summary = ShenlunSummary.builder()
                    .areaId(areaId)
                    .areaName(areaName + "公务员申论真题")
                    .count(count).build();
            summaryList.add(summary);
        }
        return summaryList;
    }

    /**
     * 按地区获取申论列表
     *
     * @param areaId
     * @return
     */
    public List<ShenlunPaper> findByAreaId(int areaId) {

        String sql = "SELECT p.pukey,p.pastpaper_name,p.past_area,(case when v.`name` is null then '国家' else v.`name` end) areaname  from v_pastpaper_info p" +
                " left join v_common_area v on p.past_area=v.pukey " +
                " where p.bl_exam_sub=2 and p.bb102=1 and p.bb1b1=1 and  p.past_year >=2013 " +
                " and past_area  = ? order by p.past_year desc,p.pukey desc";

        Object[] param = {
                areaId
        };
        List<ShenlunPaper> paperList = jdbcTemplate.query(sql, param, new ShenlunPaperMapper());
        //假如查询为空，返回空list
        if (CollectionUtils.isEmpty(paperList)) {
            return new ArrayList<>();
        }
        return paperList;
    }

    /**
     * 根据试题id真题试卷
     *
     * @return
     */
    public ShenlunPaper findById(int id) throws IOException {

        ShenlunPaper paper = new ShenlunPaper();

        //根据id查询对应试卷信息
        String sql = "select * from v_pastpaper_info where pukey=?";
        Object[] param = {id};
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, param);
        if (rs.first()) {
            paper.setId(rs.getInt("PUKEY"));
            paper.setName(rs.getString("pastpaper_name"));
            paper.setYear(rs.getInt("past_year"));
            paper.setArea(rs.getInt("past_area"));
            String tractics = rs.getString("tactics");
            //tractics不为空，对试卷信息进行处理
            if (tractics != null && !tractics.equals("[]")) {
                //将Json格式转为JsonNode对象处理
                final JsonNode jsonNode = Jackson2ObjectMapperBuilder.json().build().readTree(tractics);
                //遍历模块
                if (jsonNode.isArray()) {
                    final ArrayNode nodes = (ArrayNode) jsonNode;
                    for (JsonNode node : nodes) {
                        //获取模块id(22/23/24)
                        final int moduleId = node.get("moduleId").asInt();
                        //此处先获取node中的itemsList，下面进一步获取题目信息
                        final ArrayNode itemsList = (ArrayNode) node.get("itemsList");

                        //当moduleId=22时，可以从中获取注意事项
                        if (moduleId > 0 && moduleId == 22) {
                            String restrict = node.get("moduleDescrp").asText();
                            paper.setRestrict(restrict);
                            // logger.info("restrict={}", restrict);//测试完删除
                        }

                        //当moduleId=23时，可以从中获取材料列表,其存储在moduleDescrps模块
                        if (moduleId > 0 && moduleId == 23) {
                            List<String> materials = new ArrayList<>();
                            //获取材料内容
                            String moduleDescrps = node.get("moduleDescrp").asText();
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root = mapper.readTree(moduleDescrps);
                            if (root.isArray()) {
                                for (JsonNode contentNode : root) {
                                    final String content = contentNode.get("content").asText();
                                    materials.add(content);
                                }
                            }
                            paper.setMaterials(materials);
                            //logger.info("materials={}", materials);//测试完删除
                        }

                        //只有当moduleId=24时，itemsList不为空，可以从中获取题目信息
                        List<ShenlunQuestion> shenlunQuestionList = new ArrayList<>();
                        if (moduleId == 24 && !itemsList.equals("[]")) {
                            for (JsonNode questionNode : itemsList) {
                                final String qId = questionNode.get("qId").asText();
                                int questionId = 0;
                                if (qId.startsWith("o")) {
                                    questionId = Integer.valueOf(qId.substring(1));
                                    //根据试题id查询单个题目信息
                                    ShenlunSingleQuestion singleQuestion = shenlunQuestionDao.findSingleQuestionById(questionId);
                                    shenlunQuestionList.add(singleQuestion);
                                } else if (qId.startsWith("m")) {//复合题
                                    int multiQuestionId = 0;
                                    //取出复合题id
                                    multiQuestionId = Integer.valueOf(qId.substring(1));
                                    //根据试题id查询单个复合题题目信息
                                    ShenlunMultiQuestion multiQuestion = shenlunQuestionDao.findMultiQuestionById(multiQuestionId);

                                    final ArrayNode qSubList = (ArrayNode) questionNode.get("qSubList");
                                    List<ShenlunSingleQuestion> subQuestions = new ArrayList<>();
                                    for (JsonNode multiQuestionNode : qSubList) {//遍历所有子试题
                                        //取出子试题的id并查询(子试题以"o44756"形式存储在qSubList中)
                                        questionId = Integer.valueOf(multiQuestionNode.asText().substring(1));
                                        ShenlunSingleQuestion singleQuestion = shenlunQuestionDao.findSingleQuestionById(questionId);
                                        subQuestions.add(singleQuestion);
                                    }
                                    multiQuestion.setSubQuestions(subQuestions);
                                    shenlunQuestionList.add(multiQuestion);
                                } else {
                                    logger.info("未知的qId,qId={}", qId);
                                }
                            }
                            //将题目存入返回bean中
                            paper.setQuestions(shenlunQuestionList);
                            //logger.info("shenlunQuestionList={}", JsonUtil.toJson(shenlunQuestionList));//测试完后删除
                        }
                    }
                }
            }
        }

        return paper;
    }


    private class ShenlunPaperMapper implements RowMapper<ShenlunPaper> {
        public ShenlunPaper mapRow(ResultSet rs, int rowNum) throws SQLException {
            final ShenlunPaper paper = ShenlunPaper.builder()
                    .id(rs.getInt("PUKEY"))
                    .name(rs.getString("PASTPAPER_NAME"))
                    .area(rs.getInt("PAST_AREA"))
                    .build();
            return paper;
        }
    }

}
