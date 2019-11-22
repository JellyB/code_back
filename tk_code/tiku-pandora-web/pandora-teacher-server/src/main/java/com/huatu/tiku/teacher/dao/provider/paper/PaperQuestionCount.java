package com.huatu.tiku.teacher.dao.provider.paper;

import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;

import java.util.function.Supplier;

/**
 * 统计试题数量
 * Created by lijun on 2018/8/7
 */
public class PaperQuestionCount {

    /**
     * 获取所有试题数量
     *
     * @param typeInfo 试卷类型
     * @param supplier 自定义where 条件
     * @return
     */
    public static final String countAllSql(PaperInfoEnum.TypeInfo typeInfo, Supplier<String> supplier) {
        StringBuilder sql = new StringBuilder(128);
        sql.append(" SELECT COUNT(1)");
        sql.append(" FROM paper_base_question ")
                .append(" LEFT JOIN base_question ON paper_base_question.question_id = base_question.id AND base_question.`status` = 1 ");
        sql.append(" WHERE ")
                .append(" paper_base_question.`status` = 1")
                .append("  AND paper_base_question.paper_type = 1")
                .append(" AND ").append(supplier.get());
        return sql.toString();
    }

    /**
     * 获取某类试题的数量
     *
     * @param typeInfo     试卷类型
     * @param completeEnum 试题类型 对应 missFlag
     * @param supplier     自定义where 语句
     * @return
     */
    public static final String countByTypeSql(PaperInfoEnum.TypeInfo typeInfo, QuestionInfoEnum.CompleteEnum completeEnum, Supplier<String> supplier) {
        StringBuilder sql = new StringBuilder(countAllSql(typeInfo, supplier));
        sql.append(" AND base_question.miss_flag = ").append(completeEnum.getCode());
        return sql.toString();
    }
}
