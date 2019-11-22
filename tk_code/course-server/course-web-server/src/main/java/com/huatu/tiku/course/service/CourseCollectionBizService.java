package com.huatu.tiku.course.service;

import com.huatu.common.utils.concurrent.ConcurrentBizLock;
import com.huatu.springboot.degrade.core.Degrade;
import com.huatu.tiku.course.bean.CourseListV3DTO;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.CourseServiceV3Fallback;
import com.huatu.tiku.course.netschool.api.v3.CourseServiceV3;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hanchao
 * @date 2017/10/18 10:38
 */
@Service
@Slf4j
public class CourseCollectionBizService {
    @Autowired
    private CourseServiceV3 courseServiceV3;
    @Autowired
    private CourseServiceV3Fallback courseServiceV3Fallback;
    @Autowired
    private PromoteBizService promoteBizService;
    private static final String MOCK_UNAME = "app_ztk768618662";

    /**
     * 用来将降级过程中的请求缓存在此处，然后10s主动重建一次,WeakKeyConcurrentHashMap
     */
    private Map<String,CollectionParam> requestQueue = new ConcurrentHashMap<>();

    @Degrade(key = "collectionCourse",name = "课程合集")
    public CourseListV3DTO getCollectionCourse(String shorttitle,int page){
        //如果促销状态开启，默认直接降级
        NetSchoolResponse collectionDetail = courseServiceV3.getCollectionDetail(shorttitle, page);
        courseServiceV3Fallback.setCollectionDetail(shorttitle,page,collectionDetail);
        return ResponseUtil.build(collectionDetail,CourseListV3DTO.class,false);
    }

    /**
     * 课程合集降级方法
     * @param shorttitle
     * @param page
     * @return
     */
    public CourseListV3DTO getCollectionCourseDegrade(String shorttitle,int page){

        //begin 为了补偿
        String key = "_mock_collection_detail$"+ shorttitle+"$"+page;
        requestQueue.put(key,CollectionParam.builder()
                .shorttitle(shorttitle)
                .page(page)
                .build());
        // end


        NetSchoolResponse collectionDetail = courseServiceV3Fallback.getCollectionDetail(shorttitle, page);
        if(collectionDetail.getCode() == NetSchoolResponse.DEFAULT_ERROR.getCode()){ //说明数据是mock过来的，放单个线程过去构建此数据
            if(ConcurrentBizLock.tryLock(key)){
                try {
                    collectionDetail = courseServiceV3.getCollectionDetail(shorttitle,page);
                    courseServiceV3Fallback.setCollectionDetail(shorttitle,page,collectionDetail);
                } catch(Exception e){
                    e.printStackTrace();
                } finally {
                    ConcurrentBizLock.releaseLock(key);
                }
            }
        }
        CourseListV3DTO result = ResponseUtil.build(collectionDetail, CourseListV3DTO.class, false);
        if (result != null) {
            result.setCache(true);
        }
        return result;
    }


    /**
     * 降级过程中的合集列表补偿
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void collectionListDegradeCompensate(){
        Set<String> keys = requestQueue.keySet();
        for (String key : keys) {
            try {
                CollectionParam params = requestQueue.get(key);
                this.getCollectionCourse(params.getShorttitle(),params.getPage());
            } catch(Exception e){
                log.error("定时补偿合集列表缓存失败");
            } finally {
                requestQueue.remove(key);
            }
        }
        keys = null;
        //防止任务堆积出现问题
        requestQueue.clear();
    }

    @Data
    @Builder
    private static class CollectionParam {
        private String shorttitle;
        private int page;
    }
}
