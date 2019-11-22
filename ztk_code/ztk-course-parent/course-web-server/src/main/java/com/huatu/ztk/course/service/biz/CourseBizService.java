package com.huatu.ztk.course.service.biz;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.course.common.CourseFallbackConfig;
import com.huatu.ztk.course.common.NetSchoolUrl;
import com.huatu.ztk.course.service.CourseService;
import com.huatu.ztk.course.utils.ParamsUtils;
import com.huatu.ztk.utils.date.DateFormatUtil;
import com.huatu.ztk.utils.date.DateUtil;
import com.huatu.ztk.utils.date.TimeMark;
import com.huatu.ztk.utils.date.TimestampUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author hanchao
 * @date 2017/9/7 10:20
 */
@Service
@Slf4j
public class CourseBizService {
    public static final String COURSE_KEY_PREFIX = "course.course_detail_";
    public static final String COURSELIST_KEY = "course.courses_list_";
    public static final String COLLECTION_KEY = "course.collection_";
    public static final String USER_BUY_KEY = "course.course_user_buy_";
    public static final String PRODUCT_SALES_KEY = "course.product_sales_";

    public static final int FUTURE_TIMEOUT = 3000; //默认获取future数据的超时时间 (针对这几个异步并行的请求，如果5s内读取不到数据，直接超时)

    private static final NullHolder NULL_HOLDER = new NullHolder();

    @Resource(name = "coreRedisTemplate")
    private ValueOperations<String, Object> valueOperations;

    @Autowired
    @Qualifier("coreRedisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private CourseService courseService;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private CourseFallbackConfig courseFallbackConfig;

    @Autowired
    private CourseBizServiceMock courseBizServiceMock;


    private static final Cache<String,Object> courseListCache =
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .build();

    /**
     * 获取公务员的视频列表
     *
     * @param courseId
     * @param username
     * @return
     */
    public Object getCourseDetail(int courseId, String username) throws InterruptedException, ExecutionException, BizException {
        TimeMark timeMark = TimeMark.newInstance();
        CountDownLatch latch = new CountDownLatch(3);
        ListenableFuture<Object> courseBasicFuture = threadPoolTaskExecutor.submitListenable(() -> {
            //开启了并且包含此课程
            if(courseFallbackConfig.getSpecialInfo() == 1 && courseFallbackConfig.containsCourseId(courseId)){
                return courseBizServiceMock.getCourseDetailMock(courseId);
            }else{
                Object result = null;
                String key = COURSE_KEY_PREFIX + courseId;
                Object object = valueOperations.get(key);
                if (object == null) {
                    HashMap<String, Object> parameterMap = Maps.newHashMapWithExpectedSize(2);
                    parameterMap.put("rid", courseId);
                    try {
                        result = courseService.getJsonByEncryptParams(parameterMap, NetSchoolUrl.COURSE_DATAIL_NEW_V2, true);
                        if (result != null) {
                            valueOperations.set(key, result, 300, TimeUnit.SECONDS); //统一缓存5分钟
                        } else {
                            valueOperations.set(key, NULL_HOLDER, 300, TimeUnit.SECONDS);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (object instanceof NullHolder) {
                        return null;
                    } else {
                        result = object;
                    }
                }
                return result;
            }

        });
        ListenableFuture<Object> userBuy = getUserBuy(username);
        ListenableFuture<Object> productLimit = getProductLimit(courseId);

        //courseBasicFuture.addCallback(new RequestCountDownFutureCallback(latch));
        //userBuy.addCallback(new RequestCountDownFutureCallback(latch));
        //productLimit.addCallback(new RequestCountDownFutureCallback(latch));

        log.info(">>>>>>>>> courseDetail: concurent request complete,used {} mills...", timeMark.millsWithMark());

        Map _productLimit = new HashMap();
        try {
            _productLimit = (Map) productLimit.get(FUTURE_TIMEOUT,TimeUnit.MILLISECONDS);
        } catch(Exception e){
            e.printStackTrace();
        }
        Set<Integer> _userBuy = Sets.newHashSet();
        try {
            _userBuy = Sets.newHashSet((Iterable<? extends Integer>) userBuy.get(FUTURE_TIMEOUT,TimeUnit.MILLISECONDS));
        } catch(Exception e){
            e.printStackTrace();
        }

        Object result = buildCourseDetail(courseId, (Map) courseBasicFuture.get(),_productLimit , _userBuy);

        log.info(">>>>>>>>> courseDetail: build response data complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());
        return result;
    }


    /**
     * 获取课程合集列表
     * @param username
     * @param shortTitle
     * @return
     */
    public Object getCollectionList(String username,String shortTitle,int page) throws ExecutionException, InterruptedException, BizException {
        TimeMark timeMark = TimeMark.newInstance();

        Future<Object> collectionFuture = threadPoolTaskExecutor.submit(() -> {
            if(courseFallbackConfig.getSpecialInfo() == 1 && courseFallbackConfig.containsCollectionTitle(shortTitle)){
                Object result = courseBizServiceMock.getCollectionListMock(shortTitle);
                ((Map)result).put("_cache", true);
                return result;
            }else{
                String key = COLLECTION_KEY+page+"_" + shortTitle;
                Object result = valueOperations.get(key);
                if(result == null){
                    Map<String,Object> params = Maps.newHashMapWithExpectedSize(2);
                    params.put("username",username);
                    params.put("page",page);
                    params.put("shortTitle",shortTitle);
                    try {
                        result = courseService.getJson(params, NetSchoolUrl.COLLECTION_DETAIL, false);
                        if (result != null) {
                            if(result instanceof Map){
                                ((Map)result).put("_timestamp", System.currentTimeMillis());
                            }
                            valueOperations.set(key, result, 20, TimeUnit.SECONDS);//缓存20s
                        }
                    } catch(BizException e){
                        log.warn("catch BizException,maybe the response data is null...", e);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }else{
                    ((Map)result).put("_cache", true);
                }
                return result;
            }
        });

        ListenableFuture<Object> userBuy = getUserBuy(username);

        Set<Integer> _userBuy = Sets.newHashSet();
        try {
            _userBuy = Sets.newHashSet((Iterable<? extends Integer>) userBuy.get(FUTURE_TIMEOUT,TimeUnit.MILLISECONDS));
        } catch(Exception e){
            e.printStackTrace();
        }

        Object result = buildCollectionList((Map) collectionFuture.get(), _userBuy);

        log.info(">>>>>>>>> collectionList: build response data complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());
        return result;
    }

    /**
     * 获取课程列表
     *
     * @return
     */
    public Object getCourseList(String username, Map<String, Object> params) throws ExecutionException, InterruptedException, BizException {
        params.remove("username");//生成缓存键错误的问题

        TimeMark timeMark = TimeMark.newInstance();
        Future<Object> listFuture = threadPoolTaskExecutor.submit(() -> {
            //0 的时候走默认的策略，会区分排序
            if(courseFallbackConfig.getCourseList() == 1){
                //非0则走fallback，忽略排序规则
                String key = COURSELIST_KEY + ParamsUtils.getSign(params);
                Object result = courseListCache.getIfPresent(key);
                //因为不能确定是不是真是没有数据，还是流量太高导致的
                if(result != null && result instanceof Map && ((Map) result).containsKey("result")){
                    ((Map)result).put("_cache", true);
                    return result;
                }else{
                    result = courseBizServiceMock.getCourseListMock();
                    ((Map)result).put("_cache", true);
                    return result;
                }
            }else{
                String key = COURSELIST_KEY + ParamsUtils.getSign(params);
                Object result = valueOperations.get(key);
                if (result == null) {
                    try {
                        result = courseService.getJson(params, NetSchoolUrl.ALL_COLLECTION_LIST_SP, false);
                        if (result != null) {
                            if(result instanceof Map){
                                ((Map)result).put("_timestamp", System.currentTimeMillis());
                            }
                            courseListCache.put(key,result);
                            valueOperations.set(key, result, 10, TimeUnit.SECONDS);//缓存10s
                        }
                    } catch (BizException e) {
                        log.warn("catch BizException,maybe the response data is null...", e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    if(result instanceof Map){
                        ((Map)result).put("_cache", true);
                    }
                }
                return result;
            }
        });

        ListenableFuture<Object> userBuy = getUserBuy(username);

        Set<Integer> _userBuy = Sets.newHashSet();
        try {
            _userBuy = Sets.newHashSet((Iterable<? extends Integer>) userBuy.get(FUTURE_TIMEOUT,TimeUnit.MILLISECONDS));
        } catch(Exception e){
            e.printStackTrace();
        }
        //
        Object result = buildCourseList((Map) listFuture.get(), _userBuy);

        log.info(">>>>>>>>> courseList: build response data complete,used {} mills,total cost {} mills...", timeMark.mills(), timeMark.totalMills());
        return result;
    }


    public ListenableFuture<Object> getUserBuy(String username) {
        return threadPoolTaskExecutor.submitListenable(() -> {
            //开关打开的时候，对用户购买数据业务降级
            if(courseFallbackConfig.getUserBuy() == 2){
                return Lists.newArrayListWithCapacity(0);
            }else if(courseFallbackConfig.getUserBuy() == 1){
                String key = USER_BUY_KEY+username;
                Object object = valueOperations.get(key);
                if(object == null){
                    HashMap<String, Object> parameterMap = Maps.newHashMapWithExpectedSize(2);
                    parameterMap.put("username", username);
                    object = courseService.getJsonByEncryptParams(parameterMap, NetSchoolUrl.USER_PRODUCTS, false);
                    //用户没有购买数据的时候，工具类返回了包装类，这时候也需要缓存，但是需要转换为list
                    if(object instanceof SuccessMessage){
                        object = Lists.newArrayListWithCapacity(0);
                    }
                    if(object != null){
                        valueOperations.set(key,object,30, TimeUnit.SECONDS);
                    }
                }

                if (object == null) {
                    //可能是单个接口超时的问题,fallback
                    return Lists.newArrayList();
                }
                return object;
            }else{
                HashMap<String, Object> parameterMap = Maps.newHashMapWithExpectedSize(2);
                parameterMap.put("username", username);
                Object obj = courseService.getJsonByEncryptParams(parameterMap,NetSchoolUrl.USER_PRODUCTS,false);
                if(obj == null){
                    //可能是单个接口超时的问题,fallback
                    return Lists.newArrayList();
                }
                if(obj instanceof SuccessMessage){
                    return Lists.newArrayListWithCapacity(0);
                }else{
                    return obj;
                }
            }
        });
    }

    public ListenableFuture<Object> getProductLimit(int rid) {
        return threadPoolTaskExecutor.submitListenable(() -> {
            //对产品购买数据业务降级，缓存10s
            if(courseFallbackConfig.getProductLimit() == 2){
                return new HashMap<>();
            }else if(courseFallbackConfig.getProductLimit() == 1){
                String key = PRODUCT_SALES_KEY+rid;
                Object result = valueOperations.get(key);
                if(result == null){
                    HashMap<String, Object> parameterMap = Maps.newHashMapWithExpectedSize(2);
                    parameterMap.put("rid", rid);
                    result = courseService.getJson(parameterMap, NetSchoolUrl.PRODUCT_LIMITS, false);
                    if(result != null){
                        valueOperations.set(key,result,10,TimeUnit.SECONDS);
                    }
                }
                return result;
            }else{
                HashMap<String, Object> parameterMap = Maps.newHashMapWithExpectedSize(2);
                parameterMap.put("rid", rid);
                return courseService.getJson(parameterMap, NetSchoolUrl.PRODUCT_LIMITS, false);
            }
        });
    }


    /**
     * 组装合集列表请求的数据
     * @param courseList
     * @param userBuy
     * @return
     * @throws BizException
     */
    private Object buildCollectionList(Map courseList, Set<Integer> userBuy) throws BizException {
        if (MapUtils.isEmpty(courseList) || !courseList.containsKey("result")) {
            throw new BizException(ErrorResult.create(0, "数据为空"));//为空默认不处理
        }
        long timestamp = 0L;
        boolean isCache = false;
        if(courseList.containsKey("_cache")){
            isCache = true;
            timestamp = (long) courseList.get("_timestamp");
        }else{
            //如果不是缓存，那么数据就是当前用户获得的，所以可以直接返回
            return courseList;
        }

        //如果数据来自缓存，需要重新计算购买和别的信息
        List<Map> decorateList = (List) ((HashMap) courseList).get("result");
        long current = System.currentTimeMillis();
        for (Map course : decorateList) {
            System.out.print(course.get("rid")+",");
            if (!"1".equals(String.valueOf(course.get("isCollect"))) && course.containsKey("rid")) {
                Integer courseId = Integer.parseInt(String.valueOf(course.get("rid")));
                course.put("isBuy", userBuy.contains(String.valueOf(courseId)) ? 1 : 0);
            }
            //从缓存中获取到的，需要更新倒计时
            if (isCache) {
                //产生异常直接放弃，用默认的
                try {
                    int passed = (int) ((current - timestamp) / 1000);
                    do {
                        if (!course.containsKey("saleStart")) {
                            break;
                        }
                        String tmpStr = String.valueOf(course.get("saleStart"));
                        if (StringUtils.isBlank(tmpStr)) {
                            break;
                        }
                        Integer saleStart = Ints.tryParse(tmpStr);
                        if (saleStart == null || saleStart <= 0) {
                            break;
                        }
                        saleStart = (saleStart.compareTo(passed) > 0) ? (saleStart - passed) : 0;
                        course.put("saleStart", String.valueOf(saleStart));
                    } while (false);

                    do {
                        if (!course.containsKey("saleEnd")) {
                            break;
                        }
                        String tmpStr = String.valueOf(course.get("saleEnd"));
                        if (StringUtils.isBlank(tmpStr)) {
                            break;
                        }
                        Integer saleEnd = Ints.tryParse(tmpStr);
                        if (saleEnd == null || saleEnd <= 0) {
                            break;
                        }
                        saleEnd = (saleEnd.compareTo(passed) > 0) ? (saleEnd - passed) : 0;
                        course.put("saleEnd", String.valueOf(saleEnd));
                    } while (false);

                } catch (Exception e) {
                    log.error("try to decorate course list error...", e);
                }
            }
        }
        return courseList;
    }

    /**
     * 组装课程列表异步请求的数据
     * @param courseList
     * @param userBuy
     * @return
     * @throws BizException
     */
    private Object buildCourseList(Map courseList, Set<Integer> userBuy) throws BizException {
        if (MapUtils.isEmpty(courseList) || !courseList.containsKey("result")) {
            throw new BizException(ErrorResult.create(0, "数据为空"));//为空默认不处理
        }

        long timestamp = 0L;
        boolean isCache = false;
        if(courseList.containsKey("_cache")){
            isCache = true;
            timestamp = (long) courseList.get("_timestamp");
        }

        List<Map> decorateList = (List) ((HashMap) courseList).get("result");
        long current = System.currentTimeMillis();

        for (Map course : decorateList) {
            if ("0".equals(String.valueOf(course.get("isCollect"))) && course.containsKey("rid")) {
                Integer courseId = Integer.parseInt(String.valueOf(course.get("rid")));
                course.put("isBuy", userBuy.contains(String.valueOf(courseId)) ? 1 : 0);
            }
            //从缓存中获取到的，需要更新倒计时
            if (isCache) {
                //产生异常直接放弃，用默认的
                try {
                    int passed = (int) ((current - timestamp) / 1000);
                    do {
                        if (!course.containsKey("saleStart")) {
                            break;
                        }
                        String tmpStr = String.valueOf(course.get("saleStart"));
                        if (StringUtils.isBlank(tmpStr)) {
                            break;
                        }
                        Integer saleStart = Ints.tryParse(tmpStr);
                        if (saleStart == null || saleStart <= 0) {
                            break;
                        }
                        saleStart = (saleStart.compareTo(passed) > 0) ? (saleStart - passed) : 0;
                        course.put("saleStart", String.valueOf(saleStart));
                    } while (false);

                    do {
                        if (!course.containsKey("saleEnd")) {
                            break;
                        }
                        String tmpStr = String.valueOf(course.get("saleEnd"));
                        if (StringUtils.isBlank(tmpStr)) {
                            break;
                        }
                        Integer saleEnd = Ints.tryParse(tmpStr);
                        if (saleEnd == null || saleEnd <= 0) {
                            break;
                        }
                        saleEnd = (saleEnd.compareTo(passed) > 0) ? (saleEnd - passed) : 0;
                        course.put("saleEnd", String.valueOf(saleEnd));
                    } while (false);

                } catch (Exception e) {
                    log.error("try to decorate course list error...", e);
                }
            }
        }

        return courseList;
    }

    private Object buildCourseDetail(int courseId, Map course, Map productLimit, Set<Integer> userBuy) throws BizException {
        if (MapUtils.isEmpty(course) || !course.containsKey("classInfo")) {
            throw new BizException(ErrorResult.create(0, "数据为空"));//为空默认不处理
        }
        Map result = Maps.newHashMap();
        int current = TimestampUtil.currentUnixTimeStamp();
        //teacher_informatioin
        Map<String, Object> detail = (Map<String, Object>) course.get("classInfo");
        int totalNum = Integer.MAX_VALUE;
        if (detail.containsKey("limitUserCount") && !"0".equals(String.valueOf(detail.get("limitUserCount")))) {
            totalNum = Integer.parseInt(String.valueOf(detail.get("limitUserCount")));
        }

        int buyNum = 0;
        if (productLimit.containsKey(String.valueOf(courseId))) {
            buyNum = Integer.parseInt(String.valueOf(productLimit.get(String.valueOf(courseId))));
        }


        int startTime = Optional.ofNullable((Integer) detail.get("startTime")).orElse((int) (DateUtil.addDay(-1).getTime() / 1000));
        int stopTime = Optional.ofNullable((Integer) detail.get("stopTime")).orElse((int) (DateUtil.addDay(1).getTime() / 1000));

        detail.put("isBuy", userBuy.contains(String.valueOf(courseId)) ? 1 : 0);
        detail.put("total", buyNum);
        int limitStatus = 0;


        if(startTime == stopTime && stopTime == 0){
            //不限时，并且不限量
            if("0".equals(String.valueOf(detail.get("limitUserCount")))){
                limitStatus = 7; //不限时不限量
            }else{
                if(totalNum > buyNum){
                    limitStatus = 7; //不限时限量，未售罄
                }else{
                    limitStatus = 8; //不限时限量，已售罄
                }
            }
        }else{
            if (startTime > current) {
                detail.put("limitTimes", startTime - current);
                limitStatus = 2;//未开始
            } else if (current >= startTime && current < stopTime) {
                detail.put("limitTimes", stopTime - current);
                if (totalNum > buyNum) {
                    limitStatus = 3;//抢购中，未售罄
                } else {
                    limitStatus = 4;
                }
            } else{
                detail.put("limitTimes", 0);
                if (totalNum <= buyNum) {
                    limitStatus = 5;//抢购结束已售罄
                } else {
                    limitStatus = 6;
                }
            }
        }


        detail.put("limitStatus", limitStatus);
        detail.put("startTime", DateFormatUtil.DEFAULT_FORMAT.format(startTime * 1000L));
        detail.put("stopTime", DateFormatUtil.DEFAULT_FORMAT.format(stopTime * 1000L));

        result.put("teacher_informatioin", detail);

        return result;
    }

    private static class NullHolder implements Serializable {
        private static final long serialVersionUID = 6961360764282030213L;
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

}
