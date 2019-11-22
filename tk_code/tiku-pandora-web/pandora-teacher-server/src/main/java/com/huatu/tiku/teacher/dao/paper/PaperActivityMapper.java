package com.huatu.tiku.teacher.dao.paper;

import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.teacher.dao.provider.paper.PaperActivityProvider;
import com.huatu.tiku.teacher.enums.ActivityTypeAndStatus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by huangqp on 2018\6\23 0023.
 */
@Repository
public interface PaperActivityMapper extends Mapper<PaperActivity> {


    /**
     * 根据试题ID查询试卷名称
     *
     * @param paperIds
     * @return
     */
    List<String> findPaperNameByPaperIds(List<Long> paperIds);


    /*List<Long> getPaperEntityId();*/

    @SelectProvider(type = PaperActivityProvider.class, method = "getActivityPaperList")
    List<HashMap<String, Object>> getActivityPaperList(Integer type, Integer state, Integer year,
                                                       String areaIds, String name, List<Integer> subjectIds,
                                                       String startTime, String endTime, int searchType);


    @SelectProvider(type = PaperActivityProvider.class,method = "getActivityListForEdu")
    List<HashMap<String, Object>> getActivityListForEdu(ActivityTypeAndStatus.ActivityTypeEnum activityTypeEnum,
                                                        int status,
                                                        String name,
                                                        int subjectId, long startTime, long endTime, int tagId, String paperId);
}

