package com.huatu.tiku.course.service;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.huatu.common.consts.ApolloConfigConsts;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.fall.CourseServiceV3Fallback;
import com.huatu.tiku.course.netschool.api.v3.CourseServiceV3;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.springboot.basic.support.ConfigSubscriber;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hanchao
 * @date 2017/10/17 17:44
 */
@Service
@Slf4j
public class PromoteBizService implements ConfigSubscriber{
    private static volatile String configSign = "";
    //促销的产品id
    private Set<Integer> promoteCourseIds = Sets.newHashSet();
    private Set<String> promoteCollections = Sets.newHashSet();

    private volatile static boolean initializing = false;
    private static final String MOCK_UNAME = "app_ztk768618662";

    @Autowired
    private CourseServiceV3 courseServiceV3;
    @Autowired
    private CourseServiceV3Fallback courseServiceV3Fallback;




    @Override
    public void update(ConfigChange configChange) {
        log.debug(">>> promote config changed,oldValue:{} -> newValue:{}",configChange.getOldValue(),configChange.getNewValue());
        String sign = DigestUtils.md5Hex(configChange.getNewValue());
        if(Objects.equals(sign, configSign)){
            log.debug(">>> promote config not really changed!!!");
            return;
        }
        try {
            load(configChange.getNewValue());
        } catch (IOException e) {
            log.error("",e);
        }
    }

    /**
     * 初始化配置
     * @param config
     * @throws IOException
     */
    private void load(String config) throws IOException {
        log.debug("load promote config ...");
        ObjectMapper objectMapper = new ObjectMapper();
        PromoteConfig promoteConfig = objectMapper.readValue(config,PromoteConfig.class);
        if(StringUtils.isNotBlank(promoteConfig.getPromoteCourseIds())){
            Set<Integer> promoteCourseIdsTemp = Splitter.on(",").splitToList(promoteConfig.getPromoteCourseIds())
                    .stream()
                    .map(id -> Ints.tryParse(id))
                    .filter(id -> id != null)
                    .collect(Collectors.toSet());
            Set<String> promoteCollectionsTemp = Splitter.on(",").splitToList(promoteConfig.getPromoteCourseIds())
                    .stream()
                    .filter(name -> StringUtils.isNotBlank(name))
                    .collect(Collectors.toSet());

            synchronized (this){
                promoteCourseIds = promoteCourseIdsTemp;
                promoteCollections = promoteCollectionsTemp;
            }
        }
        initHotData();//预热
    }

    private void initHotData(){
        initializing = true;
        log.info("初始化促销热点数据...");
        try {
            //预热所有的课程数据
            for (Integer promoteCourseId : promoteCourseIds) {
                try {
                    NetSchoolResponse courseDetail = courseServiceV3.getCourseDetail(promoteCourseId);
                    courseServiceV3Fallback.setCourseDetail(promoteCourseId,courseDetail);

                    String courseHtml = courseServiceV3.getCourseHtml(promoteCourseId);
                    courseServiceV3Fallback.setCourseH5(promoteCourseId,courseHtml);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }

            //预热所有定义的合集
            for (String title : promoteCollections) {
                try {
                    int page = 1;
                    for (;;){
                        NetSchoolResponse collectionCourse = courseServiceV3.getCollectionDetail(title, page++);
                        courseServiceV3Fallback.setCollectionDetail(title,page,collectionCourse);
                        if(! ResponseUtil.isSuccess(collectionCourse)){
                            break;
                        }
                        if(collectionCourse.getData() instanceof Map && "0".equals(String.valueOf(((Map) collectionCourse.getData()).get("next")))){
                            break;
                        }
                    }

                } catch(Exception e){
                    e.printStackTrace();
                }
            }

        } catch(Exception e){
            e.printStackTrace();
        }
        log.info("初始化促销热点数据完毕...");
        initializing = false;
    }

    @Override
    public String key() {
        return "promotion";
    }

    @Override
    public String namespace() {
        return ApolloConfigConsts.NAMESPACE_DEFAULT;
    }

    @Override
    public boolean notifyOnReady() {
        return true;
    }

    /**
     * 判断是否在促销中
     * @return
     */
    public boolean isPromoteOn(){
        //存在促销产品和促销套餐课，则认为促销开关开启，平时请将此项配置清空
        return (CollectionUtils.isNotEmpty(promoteCourseIds) || CollectionUtils.isNotEmpty(promoteCollections)) && !initializing;
    }

    @Data
    private static class PromoteConfig {
        private String promoteCourseIds;
        private String promoteCollections;
    }

}
