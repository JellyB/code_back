package com.huatu.tiku.course.dao.provider;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.huatu.tiku.course.common.YesOrNoStatus;
import com.huatu.ztk.paper.common.AnswerCardStatus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-06 5:48 PM
 **/
public class CourseExercisesProcessLogProvider {

    public String getCoursePageInfo(long userId, int page, int size){
        String tempTable = tempTable(userId);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT * FROM (")
                .append(tempTable)
                .append(" ) AS temp_table")
                .append(" LIMIT ")
                .append((page -1) * size).append(",").append(size);
        return stringBuilder.toString();
    }

    private String tempTable(long userId){
        List<Integer> list = Lists.newArrayList(AnswerCardStatus.CREATE, AnswerCardStatus.UNDONE);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" course_id as courseId,");
        stringBuilder.append(" GROUP_CONCAT( distinct syllabus_id) AS syllabusIds");
        stringBuilder.append(" FROM");
        stringBuilder.append(" course_exercises_process_log");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" biz_status in (");
        stringBuilder.append(Joiner.on(",").join(list));
        stringBuilder.append(" )");
        stringBuilder.append(" AND user_id = ").append(userId);
        stringBuilder.append(" AND status = ").append(YesOrNoStatus.YES.getCode());
        stringBuilder.append(" GROUP BY");
        stringBuilder.append(" course_id");
        stringBuilder.append(" ORDER BY");
        stringBuilder.append(" gmt_modify DESC");
        return stringBuilder.toString();
    }


    /**
     * 查询库中的错误数据
     * @return
     */
    public String getDuplicateDate(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT");
        stringBuilder.append(" user_id,");
        stringBuilder.append(" GROUP_CONCAT( syllabus_id ) AS ids ");
        stringBuilder.append(" FROM");
        stringBuilder.append(" course_exercises_process_log t ");
        stringBuilder.append(" WHERE");
        stringBuilder.append(" t.`status` = 1 ");
        stringBuilder.append(" GROUP BY");
        stringBuilder.append(" user_id,syllabus_id ");
        stringBuilder.append(" HAVING");
        stringBuilder.append(" COUNT( syllabus_id ) > 1");
        return stringBuilder.toString();
    }

    public String distinctCourseId(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT DISTINCT course_id FROM course_exercises_process_log ");
        stringBuilder.append(" WHERE course_id is not null and course_id > 0 ");
        return stringBuilder.toString();
    }

    public String summaryData(@Param(value = "courseId") Integer courseId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" SELECT lesson_id, count(lesson_id) as cnt FROM course_exercises_process_log t");
        stringBuilder.append("  WHERE t.course_id = ");
        stringBuilder.append(courseId);
        stringBuilder.append(" AND t.card_id is not null and t.`status` = '1' GROUP BY lesson_id ORDER BY cnt asc ");
        return stringBuilder.toString();
    }

}
