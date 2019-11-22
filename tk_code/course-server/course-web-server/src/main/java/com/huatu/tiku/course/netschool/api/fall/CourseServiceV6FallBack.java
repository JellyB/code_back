package com.huatu.tiku.course.netschool.api.fall;

import com.google.common.collect.Lists;
import com.huatu.common.utils.web.RequestUtil;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.CourseServiceV6;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-23 下午1:28
 **/
@Slf4j
@Component
public class CourseServiceV6FallBack implements CourseServiceV6 {


    private static final String CALENDAR_DETAIL_PRE = "_mock_calendar_detail$";


    /**
     * app课程列表
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse obtainCourseList(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    /**
     * 日历详情接口
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse calendarDetail(Map<String, Object> params) {
        log.warn("response from call back calendarDetail");
        try{
            String key = CALENDAR_DETAIL_PRE + RequestUtil.getParamSign(params);
            NetSchoolResponse response = FallbackCacheHolder.get(key);
            if(response == null){
                log.warn("obtain calendar detail not in fallbackHolder");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String defaultDate = simpleDateFormat.format(new Date());
                DefaultCalenderDetail defaultCalenderDetail = DefaultCalenderDetail.builder()
                        .msg(StringUtils.EMPTY)
                        .type(0)
                        .current_page("1")
                        .data(Lists.newArrayList())
                        .date(String.valueOf(params.getOrDefault("date", defaultDate)))
                        .month(String.valueOf(params.getOrDefault("date", defaultDate)).split("-")[1])
                        .day(String.valueOf(params.getOrDefault("date", defaultDate)).split("-")[2])
                        .per_page(String.valueOf(params.getOrDefault("pageSize", 20)))
                        .last_page(1)
                        .total(0)
                        .liveTotal(0)
                        .from(1)
                        .to(1)
                        .build();
                return NetSchoolResponse.newInstance(defaultCalenderDetail);
            }
            return response;
        }catch (Exception e){
            return NetSchoolResponse.newInstance(DefaultCalenderDetail.builder()
                    .msg(StringUtils.EMPTY).type(0).current_page("1").data(Lists.newArrayList())
                    .date(StringUtils.EMPTY).month(StringUtils.EMPTY).day(StringUtils.EMPTY).per_page(StringUtils.EMPTY)
                    .last_page(1).total(0).liveTotal(0).from(1).to(1).build());
        }
    }

    /**
     * 课程分类详情
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse courseTypeDetail(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    /**
     * 课程搜索接口
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse searchCourses(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    /**
     * 合集课程列表
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse collectDetail(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    /**
     * 获取解析课课程信息 pc 端模考大赛专用
     *
     * @param params
     * @return
     */
    @Override
    public NetSchoolResponse analysis(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    @Override
    public NetSchoolResponse analysisClassList(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    @Override
    public NetSchoolResponse userCourseStatus(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    /**
     * 缓存日历详情接口数据
     * @param params
     * @param response
     */
    public void setCalendarDetailStaticData(Map<String,Object> params, NetSchoolResponse response){
        String key = CALENDAR_DETAIL_PRE + RequestUtil.getParamSign(params);
        if(ResponseUtil.isSuccess(response)){
            FallbackCacheHolder.put(key, response);
        }
    }


    /**
     * 查询课程详情
     *
     * @param params
     */
    @Override
    public NetSchoolResponse getClassDetail(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    @Override
    public NetSchoolResponse getBaseClassInfo(Map<String, Object> params) {
        return NetSchoolResponse.DEFAULT;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DefaultCalenderDetail{
        private String msg;
        private int type;
        private String current_page;
        private Object data;
        private String date;
        private String month;
        private String day;
        private String per_page;
        private int last_page;
        private int total;
        private int liveTotal;
        private int from;
        private int to;

        @Builder
        public DefaultCalenderDetail(String msg, int type, String current_page, Object data, String date, String month, String day, String per_page, int last_page, int total, int liveTotal, int from, int to) {
            this.msg = msg;
            this.type = type;
            this.current_page = current_page;
            this.data = data;
            this.date = date;
            this.month = month;
            this.day = day;
            this.per_page = per_page;
            this.last_page = last_page;
            this.total = total;
            this.liveTotal = liveTotal;
            this.from = from;
            this.to = to;
        }
    }
}
