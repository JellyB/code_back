package com.huatu.tiku.teacher.dao.provider.paper;

import com.huatu.tiku.baseEnum.BaseStatusEnum;
import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.util.log.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by lijun on 2018/8/7
 */
@Slf4j
public class PaperEntityProvider {

    /**
     * 列表查询
     */
    @LogPrint
    public String list(int mode, int year, int specialFlag, int missFlag, long subjectId, int bizStatus, String areaIds, String name) {
        StringBuilder sql = new StringBuilder(256);
        Supplier<String> countSql = () -> "paper_base_question.paper_id = paper_entity.id";

        sql.append(" SELECT paper_entity.id,paper_entity.name,paper_entity.mode,paper_entity.year,")
                .append(" paper_entity.biz_status AS 'bizStatus',paper_entity.miss_flag as 'missFlag',")
                .append(" paper_entity.source_flag AS 'sourceFlag',")
                .append(" GROUP_CONCAT(area.`name` ORDER BY area.sort) AS 'area',")
                .append("(")
                .append(PaperQuestionCount.countAllSql(PaperInfoEnum.TypeInfo.ENTITY, countSql))
                .append(") as 'allCount',")
                .append("(")
                .append(PaperQuestionCount.countByTypeSql(PaperInfoEnum.TypeInfo.ENTITY, QuestionInfoEnum.CompleteEnum.INCOMPLETE, countSql))
                .append(") as 'inCompLeftCount'");
        sql.append(" FROM ")
                .append(" paper_entity LEFT JOIN paper_area pa ON paper_entity.id = pa.paper_id ")
                .append(" AND pa.paper_type = ").append(PaperInfoEnum.TypeInfo.ENTITY.getCode()).append(" AND pa.`status` = ").append(BaseStatusEnum.NORMAL.getCode())
                .append(" LEFT JOIN area ON pa.area_id = area.id ")
                .append(" AND area.`status` = ").append(BaseStatusEnum.NORMAL.getCode());
        sql.append(" WHERE paper_entity.status =").append(BaseStatusEnum.NORMAL.getCode());
        //试卷属性
        if (BaseInfo.isNotDefaultSearchValue(mode)) {
            sql.append(" AND paper_entity.mode =").append(mode);
        }
        //年份
        if (BaseInfo.isNotDefaultSearchValue(year)) {
            sql.append(" AND paper_entity.year =").append(year);
        }
        //是否是 特等教师
        if (BaseInfo.isNotDefaultSearchValue(specialFlag)) {
            sql.append(" AND paper_entity.special_flag =").append(specialFlag);
        }
        //是否残缺
        if (BaseInfo.isNotDefaultSearchValue(missFlag)) {
            sql.append(" AND paper_entity.miss_flag =").append(missFlag);
        }
        //科目
        if (BaseInfo.isNotDefaultSearchValue(subjectId + "")) {
            sql.append(" AND paper_entity.subject_id =").append(subjectId);
        }
        //业务状态
        if (BaseInfo.isNotDefaultSearchValue(bizStatus)) {
            sql.append(" AND paper_entity.biz_status =").append(bizStatus);
        }
        //名称
        if (StringUtils.isNotBlank(name)) {
            sql.append(" AND paper_entity.name like '%").append(name).append("%'");
        }
        //区域ID
        if (BaseInfo.isNotDefaultSearchValue(areaIds)) {
            String[] split = areaIds.split(",");
            if (split.length > 0) {
                String areaIdsStr = Stream.of(split)
                        .map(id -> "'" + id + "'")
                        .collect(Collectors.joining(","));
                sql.append(" AND pa.area_id in (").append(areaIdsStr).append(")");
            }
        }
        sql.append(" GROUP BY paper_entity.id ");
        sql.append(" ORDER BY paper_entity.gmt_create DESC");
        return sql.toString();
    }

    /**
     * 获取所有的考试时间
     */
    public String getEntityPaperTime(){
        StringBuilder sql = new StringBuilder(128);
        sql.append(" SELECT DISTINCT (paper_time) ");
        sql.append(" FROM paper_entity ");
        sql.append(" WHERE ");
        sql.append(" paper_entity.biz_status = 1 AND paper_entity.paper_time IS NOT NULL ");
        sql.append(" ORDER BY paper_entity.paper_time DESC ");
        return sql.toString();
    }

}
