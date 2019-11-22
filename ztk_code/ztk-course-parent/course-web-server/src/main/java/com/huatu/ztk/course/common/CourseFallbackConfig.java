package com.huatu.ztk.course.common;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 临时使用，新框架上使用hystrix来熔断
 * @author hanchao
 * @date 2017/9/28 13:54
 */
@Component
@DisconfFile(filename = "fall.properties")
@Slf4j
public class CourseFallbackConfig {
    ///boolean类型不能用is MethodUtils里面的bug，通过is取set方法///


    /**
     * 商品购买量
     */
    private int productLimit = 0; // 0不fallback,1业务fallback走缓存,2直接返回默认假数据
    /**
     * 用户购买的数据
     */
    private int userBuy = 0;

    /**
     * 课程列表
     */
    private int courseList = 0; //0走默认的缓存策略，1全部走默认刷新的数据

    private int specialInfo = 0;//特殊指定的合集或者课程，0按照之前的规则走,1开启定时任务中的缓存
    /**
     * 需要定时刷新fallback的合集名称
     */
    private String titles = "";

    /**
     * 需要定时刷新的课程id列表
     */
    private String courseIds = "";



    private Set<String> _titles = Sets.newConcurrentHashSet();
    private Set<Integer> _courseIds = Sets.newConcurrentHashSet();

    @DisconfFileItem(name = "productLimit", associateField = "productLimit")
    public int getProductLimit() {
        return productLimit;
    }

    public void setProductLimit(int productLimit) {
        log.info(">>disconf update: productLimit->{}",productLimit);
        this.productLimit = productLimit;
    }

    @DisconfFileItem(name = "userBuy", associateField = "userBuy")
    public int getUserBuy() {
        return userBuy;
    }

    public void setUserBuy(int userBuy) {
        log.info(">>disconf update: userBuy->{}",userBuy);
        this.userBuy = userBuy;
    }

    @DisconfFileItem(name = "courseList", associateField = "courseList")
    public int getCourseList() {
        return courseList;
    }


    public void setCourseList(int courseList) {
        log.info(">>disconf update: courseList->{}",courseList);
        this.courseList = courseList;
    }

    @DisconfFileItem(name = "titles", associateField = "titles")
    public String getTitles() {
        return titles;
    }

    public void setTitles(String titles) {
        log.info(">>disconf update: titles->{}",titles);
        this.titles = titles;
        if(StringUtils.isNotBlank(titles)){

            _titles.addAll(Splitter.on(",").splitToList(titles));
        }
    }

    @DisconfFileItem(name = "courseIds", associateField = "courseIds")
    public String getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(String courseIds) {
        log.info(">>disconf update: courseIds->{}",courseIds);
        this.courseIds = courseIds;
        if(StringUtils.isNotBlank(courseIds)){
            List<String> list = Splitter.on(",").splitToList(courseIds);
            for (String o : list) {
                _courseIds.add(Ints.tryParse(o));
            }
        }
    }

    @DisconfFileItem(name = "specialInfo", associateField = "specialInfo")
    public int getSpecialInfo() {
        return specialInfo;
    }

    public void setSpecialInfo(int specialInfo) {
        log.info(">>disconf update: specialInfo->{}",specialInfo);
        this.specialInfo = specialInfo;
    }

    public boolean containsCourseId(int courseId){
        if(CollectionUtils.isNotEmpty(_courseIds)){
            return _courseIds.contains(courseId);
        }
        return false;
    }

    public boolean containsCollectionTitle(String shortTitle){
        if(CollectionUtils.isNotEmpty(_titles)){
            return _titles.contains(shortTitle);
        }
        return false;
    }

    public Set<String> get_titles() {
        return _titles;
    }

    public Set<Integer> get_courseIds() {
        return _courseIds;
    }
}
