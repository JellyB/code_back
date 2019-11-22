package com.huatu.tiku.schedule.biz.service;

import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.vo.Statistics.StatisticsBodyVo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
public interface StatisticsService {

    StatisticsBodyVo getStatistics(Date dateBegin, Date dateEnd, TeacherType teacherType, Long teacherId,List<String> parameters, Boolean exportClassHour);

    /**
     * 指定范围教师排名数据
     */
    List<StatisticsBodyVo> getRankStatisticsBodyVos(List<String> parameters,
                                                    List<Boolean> isPartTimes,
                                                    List<TeacherType> types,
                                                    ExamType examType, Long subjectId,
                                                    Date dateBegin,
                                                    Date dateEnd,
                                                    Pageable page,
                                                    CustomUser user, List<Long> subjectIds);

    /**
     *  指定教师指定日期课时excel
     * @return excel
     */
    HSSFWorkbook getStatisticsExcel(Date dateBegin, Date dateEnd, Teacher teacher, ExamType examType, Subject subject, Boolean exportClassHour);

}
