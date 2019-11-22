package com.huatu.tiku.teacher.dao.provider.paper;

import com.huatu.tiku.enums.BaseInfo;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import com.huatu.tiku.teacher.enums.StatusEnum;
import com.huatu.ztk.paper.common.PaperType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @创建人 lizhenjuan
 * @创建时间 2018/12/24
 * @描述
 */

@Slf4j
public class PaperActivityProvider {


    public String getActivityPaperList(Integer type, Integer state, Integer year,
                                       String areaIds, String content, List<Integer> subjectIds,
                                       String startTime, String endTime, int searchType) {

        StringBuffer sql = new StringBuffer();
        sql.append("select activity.id ,activity.name,");
        sql.append(" activity.creator_id as creatorId,");
        sql.append(" activity.biz_status as bizStatus ,");
        sql.append(" IFNULL(activity.online_time,null) AS onlineTime,");
        sql.append(" IFNULL(activity.offline_time,null) AS offlineTime,");
        sql.append(" activity.start_time AS startTime,");
        sql.append(" activity.end_time AS endTime,");
        sql.append(" activity.start_time_is_effective as startTimeIsEffective,");
        sql.append(" GROUP_CONCAT(area.`name`) as areaNames");
        sql.append("  from paper_activity activity");
        sql.append(" left join paper_activity_subject pSubject on activity.id = pSubject.paper_id and activity.status = ");
        sql.append(StatusEnum.NORMAL.getValue());
        sql.append(" left join paper_area paperArea on paperArea.paper_id= activity.id and paperArea.status = 1 and paperArea.paper_type=");
        sql.append(PaperInfoEnum.TypeInfo.SIMULATION.getCode());
        sql.append(" left join area area on paperArea.area_id=area.id and area.`status`=");
        sql.append(StatusEnum.NORMAL.getValue());
        if (CollectionUtils.isNotEmpty(subjectIds)) {
            String subjectStr = subjectIds.stream().map(subjectId -> subjectId.toString()).collect(Collectors.joining(","));
            sql.append(" where pSubject.subject_id in (");
            sql.append(subjectStr);
            sql.append(")");
        }

        if (BaseInfo.isNotDefaultSearchValue(type)) {
            sql.append(" and activity.type=").append(type);
        }
        if (BaseInfo.isNotDefaultSearchValue(year)) {
            sql.append(" and activity.`year`=").append(year);
        }
        if (BaseInfo.isNotDefaultSearchValue(areaIds)) {
            sql.append(" and paperArea.area_id in (").append(areaIds).append(")");
        }
        if (BaseInfo.isNotDefaultSearchValue(content)) {
            if (type == PaperType.FORMATIVE_TEST_ESTIMATE && searchType == PaperInfoEnum.SearchTypeEnum.SEARCH_NAME.getKey()) {
                //searchType=2,ID搜索;1名称搜索,默认是按照名称搜索
                sql.append(" and activity.id=").append(Integer.valueOf(content.trim()));
            } else {
                sql.append(" and activity.name like ").append("'%").append(content).append("%'");
            }
        }

        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = format.format(date);
        if (BaseInfo.isNotDefaultSearchValue(state)) {
            //阶段测试
            if (type == PaperType.TRUE_PAPER || type == PaperType.APPLETS_PAPER) {
                sql.append(" and activity.biz_status=").append(state);
            } else {
                if (state != ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_END.getKey()) {
                    sql.append(" and activity.biz_status=").append(state);
                    if (type != PaperType.FORMATIVE_TEST_ESTIMATE) {
                        sql.append(" and activity.offline_time>='").append(dateStr).append("'");
                    }
                } else {
                    sql.append(" and activity.offline_time<='").append(dateStr).append("'");
                }
            }
        }


        /**
         *阶段测试需根据开始时间和结束时间来查询（php阶段策测试列表查询）
         */
        if (type == PaperType.FORMATIVE_TEST_ESTIMATE) {
            if (BaseInfo.isNotDefaultSearchValue(startTime)) {
                sql.append(" and activity.online_time>='").append(startTime).append("'");
            }
            if (BaseInfo.isNotDefaultSearchValue(endTime)) {
                sql.append(" and activity.offline_time<='").append(endTime).append("'");
            }
        }


        sql.append(" group by activity.id order by activity.gmt_create desc");
        //log.info("活动列表查询sql是:{}", sql.toString());
        return sql.toString();
    }


    /**
     * 教育小程序数据查询
     *
     * @param activityTypeEnum
     * @param status
     * @param name
     * @param subjectId
     * @return
     */
    public String getActivityListForEdu(ActivityTypeAndStatus.ActivityTypeEnum activityTypeEnum,
                                        int status,
                                        String name,
                                        int subjectId, long startTime, long endTime, int tagId, String paperId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select activity.id ,activity.name,");
        sql.append(" activity.creator_id as creatorId,");
        sql.append(" activity.biz_status as bizStatus ,");
        sql.append(" activity.gmt_create as gmtCreate,");
        sql.append(" activity.gmt_modify as gmtModify,");
        sql.append(" activity.score AS score,");
        sql.append(" IFNULL(activity.online_time,null) AS onlineTime,");
        sql.append(" IFNULL(activity.offline_time,null) AS offlineTime,");
        sql.append(" activity.start_time AS startTime,");
        sql.append(" activity.end_time AS endTime,");
        sql.append(" activity.tag AS tag,");
        sql.append(" activity.course_id AS courseId,");
        sql.append(" activity.source_flag AS sourceFlag,");
        sql.append(" activity.start_time_is_effective as startTimeIsEffective,");
        sql.append(" GROUP_CONCAT(area.`name`) as areaNames");
        sql.append("  from paper_activity activity");
        sql.append(" left join paper_activity_subject pSubject on activity.id = pSubject.paper_id and activity.status = ");
        sql.append(StatusEnum.NORMAL.getValue());
        sql.append(" left join paper_area paperArea on paperArea.paper_id= activity.id and paperArea.status = 1 and paperArea.paper_type=");
        sql.append(PaperInfoEnum.TypeInfo.SIMULATION.getCode());
        sql.append(" left join area area on paperArea.area_id=area.id and area.`status`=");
        sql.append(StatusEnum.NORMAL.getValue());
        if (subjectId > 0) {
            sql.append(" where pSubject.subject_id = ");
            sql.append(subjectId);
            sql.append(" ");
        }
        if (StringUtils.isNotBlank(paperId)) {
            sql.append(" and activity.id in (").append(paperId).append(")");
        }
        if (BaseInfo.isNotDefaultSearchValue(tagId)) {
            sql.append(" and activity.tag=").append(tagId);
        }

        if (BaseInfo.isNotDefaultSearchValue(activityTypeEnum.getKey())) {
            sql.append(" and activity.type=").append(activityTypeEnum.getKey());
        }
        if (BaseInfo.isNotDefaultSearchValue(name)) {
            sql.append(" and activity.name like ").append("'%").append(name).append("%'");
        }

        if (BaseInfo.isNotDefaultSearchValue(status)) {
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = format.format(date);
            if (status != ActivityTypeAndStatus.ActivityStatusEnum.ACTIVITY_END.getKey()) {
                sql.append(" and activity.biz_status=").append(status);
                sql.append(" and activity.offline_time>='").append(dateStr).append("'");
            } else {
                sql.append(" and activity.offline_time<='").append(dateStr).append("'");
            }
        }


        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (BaseInfo.isNotDefaultSearchValue(startTime)) {
            sql.append(" and activity.start_time >= '").append(format.format(new Date(startTime))).append("' ");
        }
        if (BaseInfo.isNotDefaultSearchValue(endTime)) {
            sql.append(" and activity.start_time <= '").append(format.format(new Date(endTime))).append("' ");
        }
        sql.append(" group by activity.id order by activity.gmt_create desc");
        log.info("小程序模考大赛列表查询sql是:{}", sql.toString());
        return sql.toString();
    }
}




