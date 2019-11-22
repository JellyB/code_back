package com.huatu.tiku.teacher.service.impl.paper;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.enums.PaperInfoEnum;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 处理questionSort
 * Created by lijun on 2018/8/13
 */
public class QuestionSortHandler {

    /**
     * 缓存各个试卷 已存在的sort
     */
    private static final Cache<String, Set<Integer>> CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    /**
     * 构建key
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @return
     */
    protected static String buildKey(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        return typeInfo.getCode() + ":" + paperId;
    }

    /**
     * 验证 sort 是否可用
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @param sort     排列号
     * @param supplier 数据补充
     * @return 是否可用
     */
    protected static boolean validateSort(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Integer sort, Supplier<Set<Integer>> supplier) {
        String key = buildKey(paperId, typeInfo);
        Set<Integer> set = CACHE.getIfPresent(key);
        if (CollectionUtils.isEmpty(set)) {
            Set<Integer> cache = supplier.get();
            CACHE.put(key, cache);
        }
        Set<Integer> finalData = CACHE.getIfPresent(key);
        return finalData.contains(sort);
    }

    /**
     * 获取缓存的
     *
     * @param paperId
     * @param typeInfo
     * @return
     */
    private static Set<Integer> getCache(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        String key = buildKey(paperId, typeInfo);
        Set<Integer> set = CACHE.getIfPresent(key);
        return set;
    }

    /**
     * 移除 部分被占用的sort
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @param sortList 排列序号信息
     */
    protected static void deleteSort(Long paperId, PaperInfoEnum.TypeInfo typeInfo, List<Integer> sortList) {
        String key = buildKey(paperId, typeInfo);
        Set<Integer> set = CACHE.getIfPresent(key);
        if (CollectionUtils.isNotEmpty(set)) {
            set.removeAll(sortList);
            CACHE.put(key, set);
        }
    }

    /**
     * 移除 部分被占用的sort
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @param sort     排列序号信息
     */
    protected static void deleteSort(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Integer sort) {
        String key = buildKey(paperId, typeInfo);
        Set<Integer> set = CACHE.getIfPresent(key);
        if (CollectionUtils.isNotEmpty(set)) {
            set.remove(sort);
            CACHE.put(key, set);
        }
    }

    /**
     * 完全移除 缓存细腻系
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     */
    protected static void deleteAllSort(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        String key = buildKey(paperId, typeInfo);
        CACHE.invalidate(key);
    }

    /**
     * 添加已经被占用的 sort
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @param sort     排列序号
     */
    protected static void addSort(Long paperId, PaperInfoEnum.TypeInfo typeInfo, Integer sort) {
        String key = buildKey(paperId, typeInfo);
        Set<Integer> set = CACHE.getIfPresent(key);
        if (CollectionUtils.isNotEmpty(set)) {
            set.add(sort);
            CACHE.put(key, set);
        } else {
            Set<Integer> hashSet = new HashSet<>();
            hashSet.add(sort);
            CACHE.put(key, hashSet);
        }
    }

    /**
     * 添加已经被占用的 sort
     *
     * @param paperId  试卷ID
     * @param typeInfo 试卷类型
     * @param sortList 排列序号
     */
    protected static void addSort(Long paperId, PaperInfoEnum.TypeInfo typeInfo, List<Integer> sortList) {
        String key = buildKey(paperId, typeInfo);
        Set<Integer> set = CACHE.getIfPresent(key);
        if (CollectionUtils.isNotEmpty(sortList)) {
            set.addAll(sortList);
            CACHE.put(key, set);
        } else {
            CACHE.put(key, new HashSet<>(sortList));
        }
    }

}
