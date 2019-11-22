package com.huatu.tiku.position.biz.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.position.biz.enums.*;
import com.huatu.tiku.position.biz.util.MapUtil;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wangjian
 **/
@RequestMapping("dic")
@RestController
public class DicController {


    /**
     * 学位数据
     */
    @GetMapping("degree")
    public List<Map<String, String>> degree() {
        List<Map<String, String>> degrees = Lists.newArrayList();

        for (Degree degree : Degree.values()) {
            degrees.add(ImmutableMap.of("value", degree.name(), "text", degree.getText()));
        }

        return degrees;
    }

    /**
     * 学历数据
     */
    @GetMapping("education")
    public List<Map<String, String>> education() {
        List<Map<String, String>> educations = Lists.newArrayList();

        for (Education education : Education.values()) {
            educations.add(ImmutableMap.of("value", education.name(), "text", education.getText().substring(0,2)));
        }

        return educations;
    }

    /**
     * 最高英语水平
     */
    @GetMapping("english")
    public List<String> english() {
        List<String> englishs = Lists.newArrayList();
        englishs.add("英语四级");
        englishs.add("英语六级");
        englishs.add("专业四级");
        englishs.add("专业八级");
        englishs.add("托福");
        englishs.add("雅思");
        englishs.add("托业");
        return englishs;
    }

    /**
     * 工作经验
     */
    @GetMapping("exp")
    public List<Map<String, String>> exp() {
        List<Map<String, String>> exps = Lists.newArrayList();

        for (Exp exp : Exp.values()) {
            exps.add(ImmutableMap.of("value", exp.name(), "text", exp.getText()));
        }
        return exps;
    }

    /**
     * 政治面貌
     */
    @GetMapping("political")
    public List<Map<String, String>> political() {
        List<Map<String, String>> politicals = Lists.newArrayList();

        for (Political political : Political.values()) {
            politicals.add(ImmutableMap.of("value", political.name(), "text", political.getText()));
        }

        return politicals;
    }

    /**
     * 课程状态
     */
    @GetMapping("positionStatus")
    public List<Map<String, String>> positionStatus() {
        List<Map<String, String>> positionStatuss = Lists.newArrayList();
        for (PositionStatus positionStatus : PositionStatus.values()) {
            positionStatuss.add(ImmutableMap.of("value", positionStatus.name(), "text", positionStatus.getText()));
        }

        return positionStatuss;
    }

    /**
     * 职位类型
     */
    @GetMapping("positionType")
    public List<Map<String, String>> positionType() {
        List<Map<String, String>> positionTypes = Lists.newArrayList();

        for (PositionType positionType : PositionType.values()) {
            positionTypes.add(ImmutableMap.of("value", positionType.name(), "text", positionType.getText()));
        }

        return positionTypes;
    }

    /**
     * 状态
     */
    @GetMapping("status")
    public List<Map<String, String>> status() {
        List<Map<String, String>> statusList = Lists.newArrayList();

        for (Status status : Status.values()) {
            statusList.add(ImmutableMap.of("value", status.name(), "text", status.getText()));
        }

        return statusList;
    }

    /**
     * 机构层级
     */
    @GetMapping("departmentType")
    public List<Map<String, String>> departmentType() {
        List<Map<String, String>> departmentTypes = Lists.newArrayList();

        for (DepartmentType status : DepartmentType.values()) {
            departmentTypes.add(ImmutableMap.of("value", status.name(), "text", status.getText()));
        }

        return departmentTypes;
    }

    /**
     * 基层工作经验
     */
    @GetMapping("baseExp")
    public List<Map<String, String>> baseExp() {
        List<Map<String, String>> baseExps = Lists.newArrayList();

        for (BaseExp baseExp : BaseExp.values()) {
            baseExps.add(ImmutableMap.of("value", baseExp.name(), "text", baseExp.getText()));
        }

        return baseExps;
    }

    /**
     * 性别
     */
    @GetMapping("sex")
    public List<Map<String, String>> sex() {
        List<Map<String, String>> sexs = Lists.newArrayList();

        for (Sex sex : Sex.values()) {
            sexs.add(ImmutableMap.of("value", sex.name(), "text", sex.getText()));
        }

        return sexs;
    }

    private static final List<String> nations=new ArrayList<>();
    static{
        nations.add("汉族");nations.add("壮族");nations.add("满族");nations.add("回族");nations.add("苗族");
        nations.add("维吾尔族");nations.add("土家族");nations.add("彝族");nations.add("蒙古族");nations.add("藏族");
        nations.add("布依族");nations.add("侗族");nations.add("瑶族");nations.add("朝鲜族");nations.add("白族");nations.add("哈尼族");
        nations.add("哈萨克族");nations.add("黎族");nations.add("傣族");nations.add("畲族");nations.add("傈僳族");nations.add("仡佬族");
        nations.add("东乡族");nations.add("高山族");nations.add("拉祜族");nations.add("水族");nations.add("佤族");nations.add("纳西族");
        nations.add("羌族");nations.add("土族");nations.add("仫佬族");nations.add("锡伯族");nations.add("柯尔克孜族");nations.add("达斡尔族");
        nations.add("景颇族");nations.add("毛南族");nations.add("撒拉族");nations.add("塔吉克族");nations.add("阿昌族");nations.add("普米族");
        nations.add("鄂温克族");nations.add("怒族");nations.add("京族");nations.add("基诺族");nations.add("德昂族");nations.add("保安族");nations.add("俄罗斯族");nations.add("裕固族");
        nations.add("乌兹别克族");nations.add("门巴族");nations.add("鄂伦春族");nations.add("独龙族");nations.add("塔塔尔族");nations.add("赫哲族");nations.add("珞巴族");nations.add("布朗族");
    }
    /**
     * 民族
     */
    @GetMapping("nation")
    public List<String> nation() {
        return nations;
    }

    /**
     * 民族查找
     */
    @GetMapping("findNation")
    public List<String> findNation(String nation){
        return nations.stream().filter(str-> str.contains(nation)).collect(Collectors.toList());
    }

    /**
     * 证书
     */
    @GetMapping("certificate")
    public List<String> findCertificate(){
        List<String> list = Lists.newArrayList();
        list.add("司法类证书");
        list.add("计算机等级证书");
        list.add("会计类证书");
        return list;
    }
    /**
     * 取得全部参数
     */
    @GetMapping("getParameters")
    public List<List> getParameters() {
        List<List> result=Lists.newArrayList();
        result.add(degree());
        result.add(education());
        result.add(english());
        result.add(exp());
        result.add(political());
        result.add(positionStatus());
        result.add(positionType());
        result.add(status());
        result.add(departmentType());
        result.add(baseExp());
        result.add(sex());
        result.add(findCertificate());
        return result;
    }
}
