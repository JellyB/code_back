package com.huatu.tiku.teacher.dao.provider;

import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.PaperInfoEnum;

/**
 * Created by lijun on 2018/8/16
 */
public class PaperAssemblyProvider {

    /**
     * 列表查询
     */
    public String list(String name, String beginTime, String endTime, Long subjectId, PaperInfoEnum.PaperAssemblyType type) {
        StringBuilder sql = new StringBuilder(256);
        sql.append("SELECT ");
        sql.append("paper_assembly.id,paper_assembly.`name`,paper_assembly.type,")
                .append("paper_assembly.gmt_create AS 'gmtCreate',paper_assembly.gmt_modify AS 'gmtModify',")
                .append("paper_assembly.creator_id AS creatorId,paper_assembly.modifier_id As modifierId, ")
                .append("COUNT(paq.id) AS 'questionCount'");
        sql.append(" FROM ");
        sql.append(" paper_assembly LEFT JOIN paper_assembly_question paq ON paper_assembly.id = paq.paper_id AND paq.status = 1");
        sql.append(" WHERE");
        sql.append(" paper_assembly.status = 1");
        if (BaseInfo.SEARCH_DEFAULT_INT_VALUE!=subjectId) {
            sql.append(" AND paper_assembly.subject_id=").append("'").append(subjectId).append("'");
        }
        if (BaseInfo.isNotDefaultSearchValue(beginTime)) {
            beginTime = beginTime + " 00:00:00";
            sql.append(" AND paper_assembly.gmt_create >= '").append(beginTime).append("'");
        }
        if (BaseInfo.isNotDefaultSearchValue(endTime)) {
            endTime = endTime + " 23:59:59";
            sql.append(" AND paper_assembly.gmt_create <= '").append(endTime).append("'");
        }
        if (BaseInfo.isNotDefaultSearchValue(name)) {
            sql.append(" AND paper_assembly.name like '%").append(name).append("%'");
        }
        sql.append(" AND paper_assembly.type = ").append(type.getCode());
        sql.append(" GROUP BY paper_assembly.id ");
        sql.append(" ORDER BY paper_assembly.gmt_create DESC");
        return sql.toString();
    }

}
