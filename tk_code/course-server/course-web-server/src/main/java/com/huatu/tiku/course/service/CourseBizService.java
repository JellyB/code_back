package com.huatu.tiku.course.service;

import com.google.common.primitives.Ints;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.concurrent.ConcurrentBizLock;
import com.huatu.common.utils.date.DateFormatUtil;
import com.huatu.common.utils.date.DateUtil;
import com.huatu.common.utils.date.TimeMark;
import com.huatu.common.utils.date.TimestampUtil;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.bean.*;
import com.huatu.tiku.course.netschool.api.fall.CourseServiceV3Fallback;
import com.huatu.tiku.course.netschool.api.v3.CourseServiceV3;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author hanchao
 * @date 2017/9/12 21:42
 */
@Service
@Slf4j
public class CourseBizService {

    @Autowired
    private CourseAsyncService courseAsyncService;
    @Autowired
    private CourseListService courseListService;
    @Autowired
    private CourseServiceV3 courseServiceV3;
    @Autowired
    private CourseServiceV3Fallback courseServiceV3Fallback;
    @Autowired
    private CourseCollectionBizService courseCollectionBizService;
    @Autowired
    private PromoteBizService promoteBizService;


    /**
     * 获取直播合集列表，类似直播外层列表
     * @param shorttitle
     * @param page
     * @return
     */
    public CourseListV3DTO getCollectionList(String shorttitle,int page){

        CourseListV3DTO courseList = courseCollectionBizService.getCollectionCourse(shorttitle,page);

        decorateLiveList(courseList);

        return courseList;
    }


    /**
     * 获取直播列表 v3
     *
     * @param params
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public CourseListV3DTO getCourseListV3(Map<String, Object> params) throws ExecutionException, InterruptedException, BizException {
        TimeMark timeMark = TimeMark.newInstance();

        CourseListV3DTO courseList = courseListService.getCourseListV3(params);
        log.info(">>>>>>>>> courseListV3: concurent request complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());

        decorateLiveList(courseList);

        log.info(">>>>>>>>> courseListV3: build response data complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());
        return courseList;
    }


    /**
     * 获取课程详情 v3
     *
     * @param courseId
     * @param username
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public CourseDetailV3DTO getCourseDetailV3(int courseId, String username) throws InterruptedException, ExecutionException, BizException {
        TimeMark timeMark = TimeMark.newInstance();

        ListenableFuture<CourseDetailV3DTO> courseDetailFuture = courseAsyncService.getCourseDetailV3(courseId);
        ListenableFuture<Set<Integer>> userBuyFuture = courseAsyncService.hasBuy(courseId,username);
        ListenableFuture<Map<Integer, Integer>> courseLimitFuture = courseAsyncService.getCourseLimit(courseId);

        CountDownLatch countDownLatch = new CountDownLatch(3);
        courseDetailFuture.addCallback(new RequestCountDownFutureCallback(countDownLatch));
        userBuyFuture.addCallback(new RequestCountDownFutureCallback(countDownLatch));
        courseLimitFuture.addCallback(new RequestCountDownFutureCallback(countDownLatch));

        countDownLatch.await(10, TimeUnit.SECONDS);//最多10秒等待

        log.info(">>>>>>>>> courseDetail: concurent request complete,used {} mills...", timeMark.millsWithMark());

        CourseDetailV3DTO courseDetail = courseDetailFuture.get();
        if (courseDetail == null) {
            throw new BizException(ResponseUtil.ERROR_PAGE_RESPONSE);
        }
        CourseDetailV3DTO.ClassInfo classInfo = courseDetail.getClassInfo();

        int current = TimestampUtil.currentUnixTimeStamp();
        int totalNum = Integer.MAX_VALUE;//限制人数
        if (classInfo.getLimitUserCount() != null && classInfo.getLimitUserCount() != 0) {
            totalNum = classInfo.getLimitUserCount();
        }

        int buyNum = 0;
        Map<Integer, Integer> courseLimit = courseLimitFuture.get();
        if (courseLimit.containsKey(courseId)) {
            buyNum = courseLimit.get(courseId);
        }

        classInfo.setTotal(buyNum);

        //获取开始时间和结束时间
        int startTime = Optional.ofNullable(Ints.tryParse(classInfo.getStartTime())).orElse((int) (DateUtil.addDay(-1).getTime() / 1000));
        int stopTime = Optional.ofNullable(Ints.tryParse(classInfo.getStopTime())).orElse((int) (DateUtil.addDay(1).getTime() / 1000));

        int limitStatus = 0;

        //获取课程销售状态
        do {
            if (startTime == stopTime && startTime == 0) {
                //不限时，并且不限量
                if(classInfo.getLimitUserCount() != null && classInfo.getLimitUserCount() == 0){
                    limitStatus = 7; //不限时不限量
                    break;
                }
                if(totalNum > buyNum){
                    limitStatus = 7; //不限时限量，未售罄
                }else{
                    limitStatus = 8; //不限时限量，已售罄
                }
                break;
            }
            if (startTime > current) {
                classInfo.setLimitTimes(startTime - current);
                limitStatus = 2;//未开始
            } else if (current >= startTime && current < stopTime) {
                classInfo.setLimitTimes(stopTime - current);
                if (totalNum > buyNum) {
                    limitStatus = 3;//抢购中，未售罄
                } else {
                    limitStatus = 4;//抢购中，已售罄
                }
            } else {
                classInfo.setLimitTimes(0);
                if (totalNum <= buyNum) {
                    limitStatus = 5;//抢购结束
                } else {
                    limitStatus = 6;
                }
            }

        } while (false);

        Set<Integer> userBuy = userBuyFuture.get();
        classInfo.setIsBuy(userBuy.contains(courseId) ? 1 : 0);

        classInfo.setLimitStatus(limitStatus);
        classInfo.setStartTime(DateFormatUtil.DEFAULT_FORMAT.format(startTime * 1000L));
        classInfo.setStopTime(DateFormatUtil.DEFAULT_FORMAT.format(stopTime * 1000L));

        log.info(">>>>>>>>> courseDetail: build response data complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());

        return courseDetail;
    }


    /**
     * 老版本获取课程列表
     *
     * @param username
     * @param params
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public CourseListV2DTO getCourseListV2(String username, Map<String, Object> params) throws ExecutionException, InterruptedException, BizException {
        TimeMark timeMark = TimeMark.newInstance();
        ListenableFuture<CourseListV2DTO> courseListFuture = courseAsyncService.getCourseListV2(params);
        ListenableFuture<Set<Integer>> userBuyFuture = courseAsyncService.getUserBuy(username);

        CourseListV2DTO courseList = courseListFuture.get();
        Set<Integer> userBuy = userBuyFuture.get();
        log.info(">>>>>>>>> courseList: concurent request complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());
        if (courseList == null) {
            throw new BizException(ResponseUtil.ERROR_PAGE_RESPONSE);
        }
        if (CollectionUtils.isNotEmpty(courseList.getResult())) {
            for (Map course : courseList.getResult()) {
                if ("0".equals(String.valueOf(course.get("isCollect"))) && course.containsKey("rid")) {
                    Integer courseId = Integer.parseInt(String.valueOf(course.get("rid")));
                    course.put("isBuy", userBuy.contains(String.valueOf(courseId)) ? 1 : 0);
                }//设置是否购买的状态
            }
        }
        log.info(">>>>>>>>> courseList: build response data complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());
        return courseList;
    }

    /**
     * 老版本获取课程详情
     *
     * @param courseId
     * @param username
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public CourseDetailV2DTO getCourseDetailV2(int courseId, String username) throws InterruptedException, ExecutionException, BizException {
        TimeMark timeMark = TimeMark.newInstance();

        ListenableFuture<CourseDetailV2DTO> courseDetailFuture = courseAsyncService.getCourseDetailV2(courseId);
        ListenableFuture<Set<Integer>> userBuyFuture = courseAsyncService.getUserBuy(username);
        ListenableFuture<Map<Integer, Integer>> courseLimitFuture = courseAsyncService.getCourseLimit(courseId);

        CountDownLatch countDownLatch = new CountDownLatch(3);
        courseDetailFuture.addCallback(new RequestCountDownFutureCallback(countDownLatch));
        userBuyFuture.addCallback(new RequestCountDownFutureCallback(countDownLatch));
        courseLimitFuture.addCallback(new RequestCountDownFutureCallback(countDownLatch));

        countDownLatch.await(10, TimeUnit.SECONDS);//最多10秒等待

        log.info(">>>>>>>>> courseDetail: concurent request complete,used {} mills...", timeMark.millsWithMark());

        CourseDetailV2DTO courseDetail = courseDetailFuture.get();
        if (courseDetail == null) {
            throw new BizException(ResponseUtil.ERROR_PAGE_RESPONSE);
        }
        CourseDetailV2DTO.ClassInfo classInfo = courseDetail.getClassInfo();

        int current = TimestampUtil.currentUnixTimeStamp();
        int totalNum = Integer.MAX_VALUE;//限制人数
        if (classInfo.getLimitUserCount() != null && classInfo.getLimitUserCount() != 0) {
            totalNum = classInfo.getLimitUserCount();
        }

        int buyNum = 0;
        Map<Integer, Integer> courseLimit = courseLimitFuture.get();
        if (courseLimit.containsKey(courseId)) {
            buyNum = courseLimit.get(courseId);
        }

        classInfo.setTotal(buyNum);

        int startTime = Optional.ofNullable(Ints.tryParse(classInfo.getStartTime())).orElse((int) (DateUtil.addDay(-1).getTime() / 1000));
        int stopTime = Optional.ofNullable(Ints.tryParse(classInfo.getStopTime())).orElse((int) (DateUtil.addDay(1).getTime() / 1000));

        int limitStatus = 0;
        //获取课程销售状态
        do {
            if (startTime == stopTime && startTime == 0) {
                //不限时，并且不限量
                if(classInfo.getLimitUserCount() != null && classInfo.getLimitUserCount() == 0){
                    limitStatus = 7; //不限时不限量
                    break;
                }
                if(totalNum > buyNum){
                    limitStatus = 7; //不限时限量，未售罄
                }else{
                    limitStatus = 8; //不限时限量，已售罄
                }
                break;
            }
            if (startTime > current) {
                classInfo.setLimitTimes(startTime - current);
                limitStatus = 2;//未开始
            } else if (current >= startTime && current < stopTime) {
                classInfo.setLimitTimes(stopTime - current);
                if (totalNum > buyNum) {
                    limitStatus = 3;//抢购中，未售罄
                } else {
                    limitStatus = 4;//抢购中，已售罄
                }
            } else {
                classInfo.setLimitTimes(0);
                if (totalNum <= buyNum) {
                    limitStatus = 5;//抢购结束
                } else {
                    limitStatus = 6;
                }
            }

        } while (false);
        classInfo.setLimitStatus(limitStatus);
        classInfo.setStartTime(DateFormatUtil.DEFAULT_FORMAT.format(startTime * 1000L));
        classInfo.setStopTime(DateFormatUtil.DEFAULT_FORMAT.format(stopTime * 1000L));
        courseDetail.setTeacher_informatioin(classInfo);
        courseDetail.setClassInfo(null);

        log.info(">>>>>>>>> courseDetail: build response data complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());

        return courseDetail;
    }


    @Degrade(key = "courseHtml",name="课程详情HTML")
    public String getCourseHtml(int rid){
        String data = courseServiceV3.getCourseHtml(rid);
        courseServiceV3Fallback.setCourseH5(rid,data);
        return data;
    }

    public String getCourseHtmlDegrade(int rid){
        String data = courseServiceV3Fallback.getCourseHtml(rid);
        if(StringUtils.isBlank(data)){
            String key = "_mock_course_h5$"+ rid;
            if (ConcurrentBizLock.tryLock(key)) {
                try {
                    data = courseServiceV3.getCourseHtml(rid);
                    courseServiceV3Fallback.setCourseH5(rid, data);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ConcurrentBizLock.releaseLock(key);
                }
            }
        }
        return data;
    }

    @Degrade(key = "courseTimetable",name="课程大纲")
    public NetSchoolResponse getCourseTimetable(int rid){
        NetSchoolResponse timetable = courseServiceV3.findTimetable(rid);
        courseServiceV3Fallback.setTimetable(rid,timetable);
        return timetable;
    }

    public NetSchoolResponse getCourseTimetableDegrade(int rid){
        NetSchoolResponse response = courseServiceV3Fallback.findTimetable(rid);
        if(response.getCode() == NetSchoolResponse.DEFAULT_ERROR.getCode()){
            String key = "_mock_course_timetable$"+ rid;
            if (ConcurrentBizLock.tryLock(key)) {
                try {
                    response = courseServiceV3.findTimetable(rid);
                    courseServiceV3Fallback.setCourseDetail(rid, response);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ConcurrentBizLock.releaseLock(key);
                }
            }
        }
        return response;
    }


    @Slf4j
    private static class RequestCountDownFutureCallback implements ListenableFutureCallback {
        private CountDownLatch countDownLatch;

        public RequestCountDownFutureCallback(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onFailure(Throwable ex) {
            log.error("request async error...", ex);
            countDownLatch.countDown();
        }

        @Override
        public void onSuccess(Object result) {
            countDownLatch.countDown();
        }
    }


    private void decorateLiveList(CourseListV3DTO courseList) {
        if (courseList == null) {
            throw new BizException(ResponseUtil.ERROR_PAGE_RESPONSE);
        }

        //包装数据
        long timestamp = courseList.getCacheTimestamp();
        if (CollectionUtils.isNotEmpty(courseList.getResult())) {
            long current = System.currentTimeMillis();
            for (Map item : courseList.getResult()) {
                if (courseList.isCache()) {
                    try {
                        int passed = (int) ((current - timestamp) / 1000);
                        do {
                            if (!item.containsKey(CourseListV3DTO.KEY_SALE_START)) {
                                break;
                            }
                            String tmpStr = String.valueOf(item.get(CourseListV3DTO.KEY_SALE_START));
                            if (StringUtils.isBlank(tmpStr)) {
                                break;
                            }
                            Integer saleStart = Ints.tryParse(tmpStr);
                            if (saleStart == null || saleStart <= 0) {
                                break;
                            }
                            saleStart = (saleStart.compareTo(passed) > 0) ? (saleStart - passed) : 0;
                            item.put(CourseListV3DTO.KEY_SALE_START, String.valueOf(saleStart));
                        } while (false);

                        do {
                            if (!item.containsKey(CourseListV3DTO.KEY_SALE_END)) {
                                break;
                            }
                            String tmpStr = String.valueOf(item.get(CourseListV3DTO.KEY_SALE_END));
                            if (StringUtils.isBlank(tmpStr)) {
                                break;
                            }
                            Integer saleEnd = Ints.tryParse(tmpStr);
                            if (saleEnd == null || saleEnd <= 0) {
                                break;
                            }
                            saleEnd = (saleEnd.compareTo(passed) > 0) ? (saleEnd - passed) : 0;
                            item.put(CourseListV3DTO.KEY_SALE_END, String.valueOf(saleEnd));
                        } while (false);

                    } catch (Exception e) {
                        log.error("try to decorate course list error...", e);
                    }
                }

                if ("0".equals(String.valueOf(item.get("isCollect"))) && item.containsKey("rid")) {
                    item.put("isBuy", 0);
                }
            }
        }
    }


    /**
     * 构建 课程的 购买状态信息
     *
     * @param classInfo 课程信息对象
     * @param buyNum    已购 数量
     */
    public static void buildCourseLimitStatus(CourseDetailV3DTO.ClassInfo classInfo, int buyNum) {
        int totalNum = Integer.MAX_VALUE;//限制人数
        if (classInfo.getLimitUserCount() != null && classInfo.getLimitUserCount() != 0) {
            totalNum = classInfo.getLimitUserCount();
        }
        int current = TimestampUtil.currentUnixTimeStamp();
        //获取开始时间和结束时间
        int startTime = Optional.ofNullable(Ints.tryParse(classInfo.getStartTime())).orElse((int) (DateUtil.addDay(-1).getTime() / 1000));
        int stopTime = Optional.ofNullable(Ints.tryParse(classInfo.getStopTime())).orElse((int) (DateUtil.addDay(1).getTime() / 1000));

        int limitStatus = 0;

        //获取课程销售状态
        do {
            if (startTime == stopTime && startTime == 0) {
                //不限时，并且不限量
                if (classInfo.getLimitUserCount() != null && classInfo.getLimitUserCount() == 0) {
                    limitStatus = 7; //不限时不限量
                    break;
                }
                if (totalNum > buyNum) {
                    limitStatus = 7; //不限时限量，未售罄
                } else {
                    limitStatus = 8; //不限时限量，已售罄
                }
                break;
            }
            if (startTime > current) {
                classInfo.setLimitTimes(startTime - current);
                limitStatus = 2;//未开始
            } else if (current >= startTime && current < stopTime) {
                classInfo.setLimitTimes(stopTime - current);
                if (totalNum > buyNum) {
                    limitStatus = 3;//抢购中，未售罄
                } else {
                    limitStatus = 4;//抢购中，已售罄
                }
            } else {
                classInfo.setLimitTimes(0);
                if (totalNum <= buyNum) {
                    limitStatus = 5;//抢购结束
                } else {
                    limitStatus = 6;
                }
            }

        } while (false);
        classInfo.setTotal(buyNum);
        classInfo.setLimitStatus(limitStatus);
        classInfo.setStartTime(DateFormatUtil.DEFAULT_FORMAT.format(startTime * 1000L));
        classInfo.setStopTime(DateFormatUtil.DEFAULT_FORMAT.format(stopTime * 1000L));
    }

}
