package com.huatu.tiku.teacher.service;

import com.google.common.collect.Maps;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.teacher.dao.mongo.MatchDao;
import com.huatu.tiku.teacher.service.match.MatchUserMetaService;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author huangqingpeng
 * @title: MatchCountTest
 * @description: TODO
 * @date 2019-09-0510:05
 */
public class MatchCountTest extends TikuBaseTest {

    @Autowired
    MatchDao matchDao;

    @Autowired
    MatchUserMetaService matchUserMetaService;


    private static List<Integer> xingce = Lists.newArrayList(4001836,4001849,4001857,4001896,4001899,4001909,4001915,4001929,4001939,4001943,4001955,4001989);

    private static List<Integer> gongji = Lists.newArrayList(4001148,4001175,4001185,4001207,4001280,4001291,4001407,4001433,4001484,4001515,4001636,4001694,4001735,4001824,4001848,4001892,4001905,4001919,4001940,4001980);


    @Test
    public void test() {

        Map<Integer, Set<Integer>> enrollMap = Maps.newHashMap();
        Map<Integer, Set<Integer>> joinMap = Maps.newHashMap();
        for (Integer integer : xingce) {
            getMatchUserId(integer, enrollMap, joinMap);
        }
        for (Integer integer : gongji) {
            getMatchUserId(integer, enrollMap, joinMap);
        }
        xingce.stream().forEach(a ->
                        System.out.println("第" + (a) + "季"  +"\t" +
                                enrollMap.getOrDefault(a, Sets.newHashSet()).size() + "\t" +
                                joinMap.getOrDefault(a, Sets.newHashSet()).size() + "\t" +
                                joinMap.getOrDefault(a, Sets.newHashSet()).size() * 100 / Math.max(1,enrollMap.getOrDefault(a, Sets.newHashSet()).size())));
        Set<Integer> enroll = xingce.parallelStream().map(a -> enrollMap.getOrDefault(a, Sets.newHashSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        long count = xingce.parallelStream().map(a -> joinMap.getOrDefault(a, Sets.newHashSet()))
                .flatMap(Collection::stream).distinct().count();
        System.out.println("enroll = " + enroll.size());
        System.out.println("count = " + count);


        gongji.stream().forEach(a ->
                System.out.println("第" + (a+1) + "季"  +"\t" +
                        enrollMap.getOrDefault(a, Sets.newHashSet()).size() + "\t" +
                        joinMap.getOrDefault(a, Sets.newHashSet()).size() + "\t" +
                        joinMap.getOrDefault(a, Sets.newHashSet()).size() * 100 / Math.max(1,enrollMap.getOrDefault(a, Sets.newHashSet()).size())));
        Set<Integer> enroll1 = gongji.parallelStream().map(a -> enrollMap.getOrDefault(a, Sets.newHashSet()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        long count1 = gongji.parallelStream().map(a -> joinMap.getOrDefault(a, Sets.newHashSet()))
                .flatMap(Collection::stream).distinct().count();
        System.out.println("enroll1 = " + enroll1.size());
        System.out.println("count1 = " + count1);

        Collection intersection = CollectionUtils.intersection(enroll, enroll1);
        System.out.println("intersection = " + intersection.size());
    }

    private void getMatchUserId(int paperId, Map<Integer, Set<Integer>> enrollMap, Map<Integer, Set<Integer>> joinMap) {
        List<MatchUserMeta> matchUserMetas = matchUserMetaService.findByMatchId(paperId);
        enrollMap.put(paperId, matchUserMetas.parallelStream().map(MatchUserMeta::getUserId).collect(Collectors.toSet()));
        joinMap.put(paperId, matchUserMetas.parallelStream()
                .filter(i -> null != i.getPracticeId() && i.getPracticeId() > 0)
                .map(MatchUserMeta::getUserId).collect(Collectors.toSet()));
    }
}
