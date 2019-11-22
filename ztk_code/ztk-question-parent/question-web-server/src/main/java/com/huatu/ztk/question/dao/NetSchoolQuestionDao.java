package com.huatu.ztk.question.dao;

import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.question.bean.NetSchoolQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.DifficultGrade;
import com.huatu.ztk.question.common.QuestionMode;
import com.huatu.ztk.question.common.QuestionStatus;
import com.huatu.ztk.question.common.QuestionType;
import com.huatu.ztk.question.controller.InitController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 网校试题dao层
 * Created by linkang on 8/29/16.
 */
@Repository
public class NetSchoolQuestionDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InitController initController;

    private static String IMG_PREFIX = "http://tiku.huatu.com/cdn/images/vhuatu/tiku/";

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 直接查询数据库
     * 不处理文本
     *
     * @param qids 试题id
     * @return
     * @throws Exception
     */
    public List<Question> findBath(List<Integer> qids) throws Exception {
        List<Question> ret = new ArrayList<>();
        for (Integer qid : qids) {
            NetSchoolQuestion question = new NetSchoolQuestion();
            String sql = "SELECT * FROM v_obj_question where pukey=" + qid;
            SqlRowSet resultSet = jdbcTemplate.queryForRowSet(sql);
            while (resultSet.next()) {
                String stem = resultSet.getString("stem");
                String analysis = resultSet.getString("answer_comment");
                String choice1 = resultSet.getString("choice_1");
                String choice2 = resultSet.getString("choice_2");
                String choice3 = resultSet.getString("choice_3");
                String choice4 = resultSet.getString("choice_4");
                Float score = resultSet.getFloat("point");
                String stand_answer = resultSet.getString("stand_answer");
                String source = resultSet.getString("source");
                String source_year = resultSet.getString("source_year");
                String source_area = resultSet.getString("source_area");
                Integer type = resultSet.getInt("type_id");

                question.setType(type);

                List<String> choices = new ArrayList<>();
                choices.add(choice1);
                choices.add(choice2);
                choices.add(choice3);
                choices.add(choice4);

                question.setSubject(1);
                question.setStem(stem);
                question.setChoices(choices);
                question.setAnalysis(analysis);
                question.setScore(score);
                question.setFrom(source);
                question.setArea(Integer.parseInt(source_area));
                Integer year = Ints.tryParse(source_year);
                if (year == null) {
                    year = 2012;
                }
                question.setYear(year);
                question.setAnswer(initController.answerParse(stand_answer));
                Map<Integer, Integer> tmpMap = initController.getPointMap();
                question.setPoints(initController.getKnowledge(question.getId(), tmpMap, "v_question_pk_r"));

                List<String> pointsName = new ArrayList<>();
                for (Integer integer : question.getPoints()) {
                    final String pointName = initController.point_map.get(integer);
                    if (pointName != null) {
                        pointsName.add(pointName);
                    }
                }
                question.setPointsName(pointsName);

                Float difficult_grade = Float.valueOf(resultSet.getString("difficult_grade"));
                int difficult = 3;
                if (difficult_grade < -2.4) {
                    difficult = DifficultGrade.SO_EASY;
                } else if (difficult_grade >= -2.4 && difficult_grade <= -1.2) {
                    difficult = DifficultGrade.EASY;
                } else if (difficult_grade > -1.2 && difficult_grade <= 0) {
                    difficult = DifficultGrade.GENERAL;
                } else if (difficult_grade > 0 && difficult_grade <= 1.2) {
                    difficult = DifficultGrade.DIFFICULT;
                } else if (difficult_grade > 1.2) {
                    difficult = DifficultGrade.SO_DIFFICULT;
                } else {
                    difficult = DifficultGrade.GENERAL;
                }
                question.setDifficult(difficult);
                question.setId(qid);

            }
            ret.add(question);
        }

        return ret;
    }

    //转换文本中的img标签
    private static String convert(String content) {
        if (content.indexOf("img") < 0)
            return content;

        content = content.replaceAll("\\[img.*?\\]", "[img]");

        //用-->分割,分割后的字符串缺少-->
        String[] contents = content.split("-->");
        String result = "";

        for (int i = 0; i < contents.length; i++) {
            String con = contents[i];
            //如果不是最后一个，加上-->
            if (i != contents.length - 1) {
                con += "-->";
            }
            result += convertSingle(con);
        }

        return result;
    }

    //处理单个字符串
    private static String convertSingle(String content) {
        if (content.indexOf("img") < 0)
            return content;

        final int s1 = content.indexOf("<!--");
        final int e1 = content.indexOf("-->") + 2;
        final int start = content.indexOf("[img]") + 5;
        final int end = content.indexOf("[/img]");

        String imgage = content.substring(start, end);
        //组装图片地址
        final String imgUrl = IMG_PREFIX + imgage.charAt(0) + "/" + imgage;
        String imgStr = "<img src=" + imgUrl + "></img>";

        return content.substring(0, s1) + imgStr + content.substring(e1 + 1, content.length());
    }
}
