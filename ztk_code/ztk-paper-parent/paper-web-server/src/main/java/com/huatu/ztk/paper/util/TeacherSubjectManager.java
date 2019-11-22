package com.huatu.ztk.paper.util;

import com.google.common.collect.Lists;
import com.huatu.ztk.paper.bean.Paper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author huangqingpeng
 * @title: TeacherSubjectManager
 * @description: TODO
 * @date 2019-10-2816:15
 */
public class TeacherSubjectManager {

    public static final List<Integer> TEACHER_SUBJECT_IDS = Lists.newArrayList(200100049, 200100051, 200100050, 200100052);

    public static final List<Integer> JIAO_ZHI_SORT_IDS = Lists.newArrayList(200100051, 200100052, 200100049, 200100050);

    /**
     * 小学教知<中学教知<小学综素<中学综素
     */
    public static final Comparator<Paper> jiao_zhi_comparator = Comparator.comparing(i -> JIAO_ZHI_SORT_IDS.indexOf(i.getCatgory()));
    /**
     * 小学综素<中学综素<小学教知<中学教知
     */
    public static final Comparator<Paper> zong_su_comparator = Comparator.comparing(Paper::getCatgory);

    /**
     * 幼儿<小学<中学
     */
    public static final Comparator<Paper> gradeComparator = Comparator.comparing(i->{
        String name = i.getName();
        return name.indexOf("幼儿") > -1 ? 1 : (name.indexOf("小学") > -1 ? 2 : name.indexOf("中学") > -1 ? 3 : 4);
    });

    public static void fillTeacherSubject(List<Integer> ids){
        boolean b = ids.stream().anyMatch(i -> TEACHER_SUBJECT_IDS.contains(i));
        if(b){
            ids.addAll(TEACHER_SUBJECT_IDS);
        }
    }
}
