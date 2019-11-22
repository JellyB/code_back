package com.huatu.tiku.teacher.controller.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.enums.SubjectInfoEnum;
import com.huatu.tiku.teacher.dao.mongo.MatchDao;
import com.huatu.tiku.teacher.enums.ActivityTagEnum;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Match;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工具类维护模考大赛标签的接口
 * Created by huangqingpeng on 2019/1/7.
 */
@Slf4j
@RestController
@RequestMapping("match/tag")
public class MatchTagController {

    @Autowired
    TeacherSubjectService subjectService;
    @Autowired
    MatchDao matchDao;

    /**
     * 获得模考大赛标签配置信息
     */
    @GetMapping("")
    public Object getTagProperties() {
        ActivityTagEnum.Subject[] subjects = ActivityTagEnum.Subject.values();
        List<Map> result = Lists.newArrayList();
        for (ActivityTagEnum.Subject subject : subjects) {
            int essayFlag = 0;
            Integer subjectId = subject.getKey();
            if (subject.getKey() == ActivityTagEnum.Subject.ShenlunSubject.getKey()) {
//                subjectId = ActivityTagEnum.Subject.CivilServantSubject.getKey();
                essayFlag = 1;
            }
            List<ActivityTagEnum.TagEnum> tags = subject.getValues();
            Example example = new Example(Subject.class);
            example.and().andEqualTo("level", SubjectInfoEnum.SubjectTypeEnum.SUBJECT.getCode());
            List<Subject> subjectList = subjectService.selectByExample(example);
            for (ActivityTagEnum.TagEnum tag : tags) {
                if (tag.isWork()) {
                    HashMap<Object, Object> map = Maps.newHashMap();
                    map.put("subject", subjectId);
                    map.put("id", tag.getCode());
                    map.put("name", tag.getTagName());
                    map.put("channel", essayFlag);
                    int finalSubject = subjectId;
                    Optional<Subject> first = subjectList.stream().filter(i -> i.getId().intValue() == finalSubject).findFirst();
                    map.put("category",first.isPresent()?first.get().getParent():-1);
                    map.put("subjectName",first.isPresent()?first.get().getName():"");
                    result.add(map);
                }
            }
        }
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("tags", JsonUtil.toJson(result));
        return map;
    }

    /**
     * 对行测的模考大赛的标签做矫正（2018国考和省考标签全部替换一下）
     */
    @PostMapping("")
    public void handlerTag() {
        List<Match> matches = matchDao.findBySubject(ActivityTagEnum.Subject.CivilServantSubject.getKey());
        if (CollectionUtils.isEmpty(matches)) {
            return;
        }
        //2018国考
        List<Match> gk2008 = matches.stream().filter(i -> i.getName().indexOf("2018") > -1)
                .filter(i -> i.getTag() == 1)
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(gk2008)){
            System.out.println("gk2008 = " + gk2008.stream().map(Match::getName).collect(Collectors.joining("\n")));
            gk2008.forEach(i->{
                i.setTag(ActivityTagEnum.TagEnum.XINGCEGUOKAO2018.getCode());
                matchDao.save(i);
            });
        }
        //2018省考
        List<Match> sk2008 = matches.stream().filter(i -> i.getName().indexOf("2018") > -1)
                .filter(i -> i.getTag() == 2)
                .collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(sk2008)){
            System.out.println("sk2008 = " + sk2008.stream().map(Match::getName).collect(Collectors.joining("\n")));
            sk2008.forEach(i->{
                i.setTag(ActivityTagEnum.TagEnum.XINGCESHENGKAO2018.getCode());
                matchDao.save(i);
            });
        }
    }
}
