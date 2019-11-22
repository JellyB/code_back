package com.huatu.ztk.backend;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.util.ExcelManageUtil;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.MatchRedisKeys;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.common.PaperType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/11/12.
 */
public class MockTest extends BaseTestW {
    @Autowired
    PaperDao paperDao;
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void test() {
        List<Paper> papers = paperDao.findByType(Lists.newArrayList(PaperType.CUSTOM_PAPER, PaperType.MATCH));
        List<List> result = Lists.newArrayList();
        insertCountryInfo(papers, result);
        insertCityInfo(papers, result);
        String[] rows = new String[]{"试卷名称", "报名人数", "参考人数"};
        try {
            ExcelManageUtil.writer("C:\\Users\\x6\\Desktop\\pandora\\","模考数据","xls",result,rows);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertCityInfo(List<Paper> papers, List<List> result) {
        List<Paper> paperList = papers.stream()
                .filter(i -> i instanceof EstimatePaper)
                .filter(i -> i.getName().indexOf("2018") >= 0)
                .filter(i -> i.getName().indexOf("省考") >= 0)
                .collect(Collectors.toList());
        result.add(Lists.newArrayList("省考模考从第一季开始的报名人数"));
        Optional<Paper> option = paperList.stream().filter(i -> i.getName().indexOf("十二") >= 0)
                .filter(i -> i.getType() == PaperType.MATCH)
                .findAny();
        if (option.isPresent()) {
            EstimatePaper paper = (EstimatePaper) option.get();
            paperList.removeIf(i -> ((EstimatePaper) i).getStartTime() < paper.getStartTime());
        }
        ArrayList<Object> title = Lists.newArrayList("试卷名称", "报名人数", "参考人数");
        result.add(title);
        for (Paper paper : paperList) {
            System.out.println("paper.getName() = " + paper.getName());
            String totalEnrollCountKey = MatchRedisKeys.getTotalEnrollCountKey(paper.getId());
            String matchPracticeIdSetKey = PaperRedisKeys.getPaperPracticeIdSore(paper.getId());
            Object total = redisTemplate.opsForValue().get(totalEnrollCountKey);
            Long size = redisTemplate.opsForZSet().size(matchPracticeIdSetKey);
            if (Objects.isNull(total)) {
                total = size;
            }
            if (Objects.isNull(size) && size.intValue() ==  0) {
                continue;
            }
            ArrayList<String> line = Lists.newArrayList(paper.getName(), String.valueOf(total), size.toString());
            result.add(line);
        }
    }

    private void insertCountryInfo(List<Paper> papers, List<List> result) {
        List<Paper> paperList = papers.stream()
                .filter(i -> i instanceof EstimatePaper)
                .filter(i -> i.getName().indexOf("2019") >= 0)
                .filter(i -> i.getName().indexOf("国考") >= 0)
                .collect(Collectors.toList());
        result.add(Lists.newArrayList("国考模考从第12季开始的报名人数+实际参考人数"));
        Optional<Paper> option = paperList.stream().filter(i -> i.getName().indexOf("十二") >= 0)
                .filter(i -> i.getType() == PaperType.MATCH)
                .findAny();
        if (option.isPresent()) {
            EstimatePaper paper = (EstimatePaper) option.get();
            paperList.removeIf(i -> ((EstimatePaper) i).getStartTime() < paper.getStartTime());
        }
        ArrayList<Object> title = Lists.newArrayList("试卷名称", "报名人数", "参考人数");
        result.add(title);
        for (Paper paper : paperList) {
            System.out.println("paper.getName() = " + paper.getName());
            String totalEnrollCountKey = MatchRedisKeys.getTotalEnrollCountKey(paper.getId());
            String matchPracticeIdSetKey = PaperRedisKeys.getPaperPracticeIdSore(paper.getId());
            Object total = redisTemplate.opsForValue().get(totalEnrollCountKey);
            Long size = redisTemplate.opsForZSet().size(matchPracticeIdSetKey);
            if (Objects.isNull(total)) {
                total = size;
            }
            if (Objects.isNull(size) && size.intValue() ==  0) {
                continue;
            }
            ArrayList<String> line = Lists.newArrayList(paper.getName(), String.valueOf(total), size.toString());
            result.add(line);
        }
    }


}
