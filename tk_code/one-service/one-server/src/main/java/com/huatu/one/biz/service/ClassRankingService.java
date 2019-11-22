package com.huatu.one.biz.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.one.biz.feign.PHPBlueClient;
import com.huatu.one.biz.mapper.UserCourseRankingMapper;
import com.huatu.one.biz.model.UserCourseRanking;
import com.huatu.one.biz.vo.OptionVo;
import com.huatu.one.biz.vo.PHPBlueClassRankingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClassRankingService {

    @Autowired
    private PHPBlueClient phpBlueClient;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCourseRankingMapper userCourseRankingMapper;

    private final Map<Long, String> EXAM_TYPE_DIC;

    public ClassRankingService() {
        EXAM_TYPE_DIC = Maps.newHashMap();
        EXAM_TYPE_DIC.put(0L, "不限");
        EXAM_TYPE_DIC.put(1L, "公务员");
        EXAM_TYPE_DIC.put(3L, "社工");
        EXAM_TYPE_DIC.put(4L, "招警");
        EXAM_TYPE_DIC.put(5L, "法检");
        EXAM_TYPE_DIC.put(7L, "选调生");
        EXAM_TYPE_DIC.put(8L, "事业单位");
        EXAM_TYPE_DIC.put(9L, "军转干");
        EXAM_TYPE_DIC.put(10L, "村官");
        EXAM_TYPE_DIC.put(12L, "教师招聘");
        EXAM_TYPE_DIC.put(19L, "教师资格证");
        EXAM_TYPE_DIC.put(20L, "遴选");
        EXAM_TYPE_DIC.put(21L, "特岗教师");
        EXAM_TYPE_DIC.put(22L, "农商行/农信社");
        EXAM_TYPE_DIC.put(25L, "银行招聘");
        EXAM_TYPE_DIC.put(26L, "三支一扶");
        EXAM_TYPE_DIC.put(28L, "军队文职");
        EXAM_TYPE_DIC.put(30L, "国家电网");
        EXAM_TYPE_DIC.put(31L, "医疗卫生");
        EXAM_TYPE_DIC.put(33L, "医疗资格证");
        EXAM_TYPE_DIC.put(37L, "政法干警");
        EXAM_TYPE_DIC.put(38L, "金融资格类");
        EXAM_TYPE_DIC.put(39L, "公选");
        EXAM_TYPE_DIC.put(40L, "部队其他");
        EXAM_TYPE_DIC.put(41L, "其他");
        EXAM_TYPE_DIC.put(42L, "考研");
        EXAM_TYPE_DIC.put(43L, "事业单位D类");
        EXAM_TYPE_DIC.put(44L, "财会");
        EXAM_TYPE_DIC.put(45L, "建筑");
        EXAM_TYPE_DIC.put(46L, "金融资格证");
        EXAM_TYPE_DIC.put(47L, "中小学");
    }

    private final String TOKEN = "69666667-20cb-4358-a012-1689d9aa2177";

    public List<OptionVo> getExamTypes() {
        List<OptionVo> optionVos = Lists.newArrayListWithExpectedSize(EXAM_TYPE_DIC.size());

        EXAM_TYPE_DIC.forEach((value, text) -> {
            optionVos.add(OptionVo.builder()
                    .value(value.toString())
                    .text(text)
                    .build());
        });

        return optionVos;
    }

    public List<OptionVo> getExamTypeOptions(String openid) {
        List<Long> examTypeIds = userService.selectCourseRankingByOpenid(openid);

        List<OptionVo> optionVos = Lists.newArrayList();

        EXAM_TYPE_DIC.forEach((value, text) -> {
            if (!examTypeIds.contains(value)) {
                optionVos.add(OptionVo.builder()
                        .value(value.toString())
                        .text(text)
                        .build());
            }
        });

        return optionVos;
    }

    public List<PHPBlueClassRankingResponse> getClassRanking(Long examType, Integer orderBy, String date, Integer rowcount, String showzeroPrice) {
        return phpBlueClient.classRanking(examType, orderBy, date, rowcount, showzeroPrice, TOKEN).getData();
    }

    public List<OptionVo> getCustomExamTypes(String openid) {
        List<Long> examTypeIds = userService.selectCourseRankingByOpenid(openid);

        SortedSet<Long> examTypeIdsSorted = new TreeSet<>();
        examTypeIdsSorted.addAll(examTypeIds);

        List<OptionVo> examTypes = Lists.newArrayListWithExpectedSize(examTypeIds.size());

        examTypeIdsSorted.forEach(examTypeId -> examTypes.add(OptionVo.builder()
                .value(examTypeId.toString())
                .text(EXAM_TYPE_DIC.get(examTypeId))
                .build()));

        return examTypes;
    }

    public void addExamTypes(Long examTypeId, String openid) {
        if (!checkExamType(openid, examTypeId)) {
            UserCourseRanking userCourseRanking = new UserCourseRanking();
            userCourseRanking.setOpenid(openid);
            userCourseRanking.setExamTypeId(examTypeId);
            userCourseRanking.setGmtCreate(new Date());

            userCourseRankingMapper.insertSelective(userCourseRanking);
        }
    }

    public void delExamTypes(Long examTypeId, String openid) {
        userCourseRankingMapper.deleteByExamTypeIdAndOpenid(examTypeId, openid);
    }

    public Boolean checkExamType(String openid, Long examTypeId) {
        return userCourseRankingMapper.selectCountByOpenidAndExamTypeId(openid, examTypeId) == 1;
    }

    public OptionVo getDefaultExamType() {
        return OptionVo.builder()
                .value("0")
                .text(EXAM_TYPE_DIC.get(0L))
                .build();
    }
}
