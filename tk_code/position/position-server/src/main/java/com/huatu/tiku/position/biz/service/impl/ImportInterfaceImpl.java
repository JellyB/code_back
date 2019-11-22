package com.huatu.tiku.position.biz.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.position.biz.domain.*;
import com.huatu.tiku.position.biz.enums.*;
import com.huatu.tiku.position.biz.respository.*;
import com.huatu.tiku.position.biz.service.ImportInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @author wangjian
 **/
@Service
@Slf4j
public class ImportInterfaceImpl implements ImportInterface {

    private final PositionRepository positionRepository;

    private final SpecialtyRepository specialtyRepository;

    private final DepartmentRepository departmentRepository;

    private final AreaRepository areaRepository;

    private final ScoreLineRepository scoreLineRepository;

    public ImportInterfaceImpl(PositionRepository positionRepository, SpecialtyRepository specialtyRepository, DepartmentRepository departmentRepository, AreaRepository areaRepository, ScoreLineRepository scoreLineRepository) {
        this.positionRepository = positionRepository;
        this.specialtyRepository = specialtyRepository;
        this.departmentRepository = departmentRepository;
        this.areaRepository = areaRepository;
        this.scoreLineRepository = scoreLineRepository;
    }


    @Override
    @Transactional
    public void importExcelPostition(List<List<List<String>>> list, Nature nature, Integer year, Date beginDate, Date endDate, PositionType type,Date enrolmentEndDate) {
        Map<String,Long> areaIdMap = getAreaIdMap();
        Map<String, List<Specialty>> specialtyMap = getSpecialty();//通过专业名 获取专业集合
        Calendar date = Calendar.getInstance();
        int count=0;
        for (List<List<String>> sheetList : list) {
            for (int i = 1; i < sheetList.size(); i++) {  //跳过第一行表头
                List<String> lineList = sheetList.get(i);
                Position position = new Position();

                Department department;
                String departmentCode = lineList.get(0);//部门代码
                String departmentName = lineList.get(1);//部门名称
                List<Department> departments = departmentRepository.findByNatureAndCodeAndName(nature, departmentCode, departmentName);
                if(null!=departments&&!departments.isEmpty()){
                    department = departments.get(0);
                }else{
                    department=new Department();
                    department.setName(lineList.get(1));
                    department.setCode(departmentCode);
                    department.setUrl(lineList.get(23));
                    department.setAttribute(lineList.get(3));
                    department.setType(DepartmentType.findByString(lineList.get(9)));
                    department.setNature(nature);
                    StringBuilder sb=new StringBuilder();
                    String phone1 = lineList.get(24);//电话1
                    String phone2 = lineList.get(25);
                    String phone3 = lineList.get(26);//AA
                    if(StringUtils.isNotBlank(phone1)){
                        sb.append(phone1);
                        sb.append(",");
                    }if(StringUtils.isNotBlank(phone2)){
                        sb.append(phone2);
                        sb.append(",");
                    }if(StringUtils.isNotBlank(phone3)){
                        sb.append(phone3);
                        sb.append(",");
                    }
                    if (sb.length() != 0) {
                        String phone = sb.delete(sb.length() - 1, sb.length()).toString();
                        department.setPhone(phone);
                    }
                    department=departmentRepository.save(department);
                }
                position.setDepartmentId(department.getId());
                String company=lineList.get(2);//司局名称
                String departmentAttribute=lineList.get(3);//机构性质
                position.setCompany(company);//司局
                position.setDepartmentAttribute(departmentAttribute);//机构性质
                position.setYear(year);//开始时间
                position.setBeginDate(beginDate);//开始时间
                position.setEndDate(endDate);//结束时间
                position.setType(type);//类型
                position.setNature(nature);
                position.setOrganization(OrganizationType.INSIDE);
                position.setNameStr(lineList.get(4));//招考职位名
                if(company.equals(department.getName())){  //部门司局名称相同
                    company=company+lineList.get(4).replace("及以下","");
                }else{
                    company=department.getName()+company+lineList.get(4).replace("及以下","");
                }
                position.setName(company);//招考职位

                position.setAttribute(lineList.get(5));//职位属性
                position.setDistribution(lineList.get(6));//职位分布
                position.setIntroduce(lineList.get(7));//职位简介
                position.setCode(lineList.get(8));//职位代码
                position.setExamType(lineList.get(10));//考试类别
                if (!Strings.isNullOrEmpty(lineList.get(11))) {
                    position.setNumber(Integer.valueOf(lineList.get(11)));//招考人数
                }

                String educationString = lineList.get(13);
                if(StringUtils.isNotBlank(educationString)){
                    position.setEducation(educationString);//学历
                    if(educationString.contains("仅限")){
                        educationString=educationString.replace("仅限","");
                        Education byName = Education.findByName(educationString);//学历
                        position.setMinEducation(byName);
                        position.setMaxEducation(byName);
                    } else if(educationString.contains("及以上")){
                        educationString=educationString.replace("及以上","");
                        Education byName = Education.findByName(educationString);
                        position.setMinEducation(byName);
                        position.setMaxEducation(Education.BS);
                    }else if (educationString.contains("或")){
                        String[] educations = educationString.split("或");
                        Education minEducations = Education.findByName(educations[0]);
                        Education maxEducations = Education.findByName(educations[1]);
                        position.setMinEducation(minEducations);
                        position.setMaxEducation(maxEducations);
                    } else {
                        Education education = Education.findByName(educationString);
                        if (education == null) {
                            log.error("学历为空：{}", educationString);
                        }

                        Assert.notNull(education, "学历为空");

                        position.setMinEducation(education);
                        position.setMaxEducation(education);
                    }
                }

                String degreeString = lineList.get(14);//学位
                if(StringUtils.isNotBlank(degreeString)){
                    position.setDegreeString(degreeString);//学位字符串
                    if("与最高学历相对应的学位".equals(degreeString)){  //设置成最低要求学历对应的学位
                        try {
                            Education minEducation = position.getMinEducation();
                            position.setDegree(Degree.findByIndex(minEducation.ordinal()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        position.setDegree(Degree.findByName(degreeString));//学位
                    }
                }else {
                    position.setDegree(null);//学位
                    position.setDegreeString(null);//学位字符串
                }

                String specialtyStrings = lineList.get(12);//专业
                if(StringUtils.isNotBlank(specialtyStrings)) {
                    position.setSpecialtyString(specialtyStrings);
                    if(specialtyStrings.contains("不限") && specialtyStrings.length() < 6){
                    }else{
                        List<Specialty> specialtysList = position.getSpecialtys();
                        if(null==specialtysList){
                            specialtysList= Lists.newArrayList();
                        }
                        Education minEducation = position.getMinEducation();
                        Education maxEducation = position.getMaxEducation();
                        specialtyStrings=specialtyStrings.replaceAll("所学专业要求为：","");
                        specialtyStrings=specialtyStrings.replaceAll("二级专业目录","");
                        specialtyStrings=specialtyStrings.replaceAll(" 或者 ","、");
                        specialtyStrings=specialtyStrings.replaceAll("招收","、");
                        specialtyStrings=specialtyStrings.replaceAll("为","、");
                        specialtyStrings=specialtyStrings.replaceAll("限","、");
                        specialtyStrings=specialtyStrings.replaceAll(",","、");
                        specialtyStrings=specialtyStrings.replaceAll(",","、");
                        specialtyStrings=specialtyStrings.replaceAll("（","、");
                        specialtyStrings=specialtyStrings.replaceAll("\\(","、");
                        specialtyStrings=specialtyStrings.replaceAll("）","、");
                        specialtyStrings=specialtyStrings.replaceAll("\\)","、");
                        specialtyStrings=specialtyStrings.replaceAll("：","、");
                        specialtyStrings=specialtyStrings.replaceAll("；","、");
                        specialtyStrings=specialtyStrings.replaceAll("。","、");
                        specialtyStrings=specialtyStrings.replaceAll("（二级学科）","类");
                        specialtyStrings=specialtyStrings.replaceAll("等学科门类","类");
                        specialtyStrings=specialtyStrings.replaceAll("包括","类");
                        specialtyStrings=specialtyStrings.replaceAll("等方向","类");
                        specialtyStrings=specialtyStrings.replaceAll("相关专业","");
                        String[] specialtys = specialtyStrings.split("、");
                        for (String specialtyString : specialtys) {
//                            List<Specialty> byNameLike = specialtyRepository.findByNameAndEducationIsGreaterThanEqual(specialtyString,position.getMinEducation());//通过名称和最小学历寻找专业
                            List<Specialty> byNameLike = specialtyMap.get(specialtyString);
                            if(null!=byNameLike&&!byNameLike.isEmpty()) {
                                byNameLike.sort((o1,o2)->o2.getType()-o1.getType());//按类型排序 优先选择小类型专业
                                for (Specialty specialty : byNameLike) {
                                    if(specialty.getEducation().ordinal()>=minEducation.ordinal()&&
                                            specialty.getEducation().ordinal()<=maxEducation.ordinal()){
                                        specialtysList.add(specialty);  //符合条件
                                    }
                                };
                            }
                        }
                        position.setSpecialtys(specialtysList);
                    }
                }

                position.setPolitical(lineList.get(15));//政治面貌
                position.setExp(Exp.findByName(lineList.get(16)));//工作经验
                position.setBaseExp(lineList.get(17));//基层经验
                position.setExtraFlag("是".equals(lineList.get(18)));//是否专业能力测试
                position.setProportion(lineList.get(19));//面试人员比例


                String areaString = lineList.get(20);//工作地区
                if(StringUtils.isNotBlank(areaString)){
                    position.setAreaId(areaIdMap.get(areaString));
                }
                String registerAreaString = lineList.get(21);//落户地区
                if(StringUtils.isNotBlank(registerAreaString)){
                    position.setRegisterAreaId(areaIdMap.get(areaString));
                }
                String requirementMark = lineList.get(22);
                if(StringUtils.isNotBlank(requirementMark)){
                    position.setRequirementMark(requirementMark);//备注条件
                }
                position.setEnrolment(0);//报名人数  定时获取 TODO
                position.setInterviewScope(0);//分数线
                position.setEnrolmentEndDate(enrolmentEndDate);

                if(lineList.size()>=41) {

                    String englishType = lineList.get(31);
                    if (StringUtils.isNotBlank(englishType)) {
                        position.setEnglishType(englishType);//英语水平
                    }
                    String certificate = lineList.get(32);
                    if (StringUtils.isNotBlank(certificate)) {
                        position.setCertificate(certificate);//所持证书
                    }

                    String areaString1 = lineList.get(33);//户籍
                    if (StringUtils.isNotBlank(areaString1)) {
                        String[] split = areaString1.split("，");
                        StringBuffer sb = new StringBuffer();
                        for (String s : split) {
                            s = getAreaString(s);
                            Long aLong = areaIdMap.get(s);
                            if (null != aLong) {
                                sb.append(String.valueOf(aLong));
                                sb.append(",");
                            }
                        }
                        if (sb.length() >= 2) {
                            sb.deleteCharAt(sb.length() - 1);
                            position.setDomicileAreaIds(sb.toString());
                        }
                    }
                    String areaString2 = lineList.get(34);//生源地
                    if (StringUtils.isNotBlank(areaString2)) {
                        String[] split = areaString2.split("，");
                        StringBuffer sb = new StringBuffer();
                        for (String s : split) {
                            s = getAreaString(s);
                            Long aLong = areaIdMap.get(s);
                            if (null != aLong) {
                                sb.append(String.valueOf(aLong));
                                sb.append(",");
                            }
                        }
                        if (sb.length() >= 2) {
                            sb.deleteCharAt(sb.length() - 1);
                            position.setBirthAreaIds(sb.toString());
                        }
                    }
                    String recent = lineList.get(35);//是否应届毕业生
                    if (StringUtils.isNotBlank(recent) && "应届生".equals(recent)) {
                        position.setRecent(true);
                    } else if (StringUtils.isNotBlank(recent) && "非应届生".equals(recent)) {
                        position.setRecent(false);
                    }
                    String graduationYear = lineList.get(36);//毕业年份
                    if (StringUtils.isNotBlank(graduationYear)) {
                        position.setGraduationYear(Integer.valueOf(graduationYear));
                    }

                    String sex = lineList.get(37);//性别
                    if (StringUtils.isNotBlank(sex)) {
                        if ("男".equals(sex)) {
                            position.setSex(Sex.MAN);
                        }
                        if ("女".equals(sex)) {
                            position.setSex(Sex.WOMAN);
                        }
                    }
                    String birthYear = lineList.get(38);//出生年份有值 按出生年份 没有值按年龄
                    if (StringUtils.isNotBlank(birthYear)) {
                        position.setBirthYear(Integer.valueOf(birthYear));
                    } else {
                        String age = lineList.get(39);//年龄
                        if (StringUtils.isNotBlank(age)) {
                            Integer ageInteger = Integer.valueOf(age);
                            position.setBirthYear(date.get(Calendar.YEAR) - ageInteger);//当前年份减去年龄
                        }
                    }
                    String nation = lineList.get(40);//民族
                    if (StringUtils.isNotBlank(nation)) {
                        position.setNation(nation);
                    }
                    String isMark = lineList.get(41);//其他要求
                    if (StringUtils.isNotBlank(isMark) && "是".equals(isMark)) {
                        position.setIsMark(true);
                    } else {
                        position.setIsMark(false);
                    }
                }
                // AT
                position.setRequireTarget(lineList.get(45));
                position.setAgeStr(lineList.get(46));
                position.setSexStr(lineList.get(47));
                position.setExamTypeStr(lineList.get(48));
                position.setCommonExamTypeStr(lineList.get(49));
                position.setSpecialtyExamTypeStr(lineList.get(50));

                positionRepository.save(position);
                log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                log.info(count+++"条数据插入成功");
                log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        }
    }

    @Override
    @Transactional
    public void updateEnrolmentEndDateString(List<List<List<String>>> list,String date) {
        int count=0;
        Date data=new Date();
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(data);
        rightNow.add(Calendar.DATE,-1);
        for (List<List<String>> sheetList : list) {
            for (int i = 1; i < sheetList.size(); i++) {  //跳过第一行表头
                List<String> lineList = sheetList.get(i);
                String department = lineList.get(0);//部门名称
                String departmentCode = lineList.get(1);//部门代码
                String company= lineList.get(2);//司局
                String name = lineList.get(3);//职位
                String code = lineList.get(4);//code
                String examCount = lineList.get(5);//招考人数
                String num = lineList.get(6);//人数
                List<Position> byCode = positionRepository.findByCompanyAndNameStrAndYearAndCodeAndNumberAndDepartmentId(company,name,2019,
                        code,Integer.valueOf(examCount),Long.valueOf(departmentCode));
                if(byCode==null||byCode.isEmpty()){
                    log.info("没有对应数据");
                }
                if(byCode.size()>1){
                    log.info("多条数据");
                }
                for (Position position : byCode) {
                    position.setEnrolment(Integer.valueOf(num));
                    position.setEnrolmentEndDate(rightNow.getTime());
                    position.setEnrolmentEndDateString(date);
                    positionRepository.save(position);
                    log.info(++count+" 次update");
                }
            }
        }
    }

    @Override
    @Transactional
    public void updatePostition(List<List<List<String>>> list) {
        Calendar date = Calendar.getInstance();
        Map<String,Long> areaIdMap = getAreaIdMap();
        int count=0;
        for (List<List<String>> sheetList : list) {
            for (int i = 1; i < sheetList.size(); i++) {  //跳过第一行表头
                List<String> lineList = sheetList.get(i);
                String department = lineList.get(0);//司局
                String company = lineList.get(2);//司局
                String nameStr = lineList.get(4);//职位
                String introduce = lineList.get(7);//简介
                String code = lineList.get(8);//简介
                String specialtyStrings = lineList.get(12);//专业
                String educationString = lineList.get(13);//学历
                List<Position> byCode = positionRepository.findByCompanyAndNameStrAndIntroduceAndYearAndSpecialtyStringAndEducationAndCode(company,nameStr,introduce,2019,
                        specialtyStrings,educationString,code,department);
                if(byCode==null||byCode.isEmpty()){
                    log.info("没有对应数据");
                }
                if(byCode.size()>1){
                    log.info("多条数据");
                }
                for (Position position : byCode) {
                    if(position.getYear()!=2019){
                        continue;
                    }
                    String englishType = lineList.get(31);
                    if(StringUtils.isNotBlank(englishType)){
                        position.setEnglishType(englishType);//英语水平
                    }
                    String certificate = lineList.get(32);
                    if(StringUtils.isNotBlank(certificate)){
                        position.setCertificate(certificate);//所持证书
                    }

                    String areaString1 = lineList.get(33);//户籍
                    if(StringUtils.isNotBlank(areaString1)){
                        String[] split = areaString1.split("，");
                        StringBuffer sb=new StringBuffer();
                        for (String s : split) {
                            s = getAreaString(s);
                            Long aLong = areaIdMap.get(s);
                            if(null!=aLong){
                                sb.append(String.valueOf(aLong));
                                sb.append(",");
                            }
                        }
                        if(sb.length()>=2) {
                            sb.deleteCharAt(sb.length()-1);
                            position.setDomicileAreaIds(sb.toString());
                        }
                    }
                    String areaString2 = lineList.get(34);//生源地
                    if(StringUtils.isNotBlank(areaString2)){
                        String[] split = areaString2.split("，");
                        StringBuffer sb=new StringBuffer();
                        for (String s : split) {
                            s=getAreaString(s);
                            Long aLong = areaIdMap.get(s);
                            if(null!=aLong) {
                                sb.append(String.valueOf(aLong));
                                sb.append(",");
                            }
                        }
                        if(sb.length()>=2) {
                            sb.deleteCharAt(sb.length()-1);
                            position.setBirthAreaIds(sb.toString());
                        }
                    }
                    String recent = lineList.get(35);//是否应届毕业生
                    if(StringUtils.isNotBlank(recent)&&"应届生".equals(recent)){
                        position.setRecent(true);
                    }else if (StringUtils.isNotBlank(recent)&&"非应届生".equals(recent)){
                        position.setRecent(false);
                    }
                    String graduationYear=lineList.get(36);//毕业年份
                    if(StringUtils.isNotBlank(graduationYear)){
                        position.setGraduationYear(Integer.valueOf(graduationYear));
                    }

                    String sex = lineList.get(37);//性别
                    if(StringUtils.isNotBlank(sex)){
                        if("男".equals(sex)){
                            position.setSex(Sex.MAN);
                        }
                        if("女".equals(sex)){
                            position.setSex(Sex.WOMAN);
                        }
                    }else {
                        position.setSex(Sex.BX);//没有限定条件为不限
                    }
                    String birthYear = lineList.get(38);//出生年份有值 按出生年份 没有值按年龄
                    if(StringUtils.isNotBlank(birthYear)){
                        position.setBirthYear(Integer.valueOf(birthYear));
                    }else {
                        String age = lineList.get(39);//年龄
                        if (StringUtils.isNotBlank(age)) {
                            Integer ageInteger = Integer.valueOf(age);
                            position.setBirthYear(date.get(Calendar.YEAR)-ageInteger);//当前年份减去年龄
                        }
                    }
                    String nation = lineList.get(40);//民族
                    if(StringUtils.isNotBlank(nation)) {
                        position.setNation(nation);
                    }
                    String isMark = lineList.get(41);//其他要求
                    if(StringUtils.isNotBlank(isMark)&&"是".equals(isMark)){
                        position.setIsMark(true);
                    }else {
                        position.setIsMark(false);
                    }
                    positionRepository.save(position);
                    log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                    log.info(count+++"data update success");
                    log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                }
            }
        }
    }

    /**
     * 导入分数线
     */
    @Override
    @Transactional
    public void importExcelScoreLine(List<List<List<String>>> list, Nature nature, Integer year) {
        for (List<List<String>> sheetList : list) {
            int count=1;
            for (int i = 1; i < sheetList.size(); i++) {  //跳过第一行表头
                List<String> lineList = sheetList.get(i);
                ScoreLine sc=new ScoreLine();
                sc.setCode(lineList.get(5));//职位代码
                sc.setNameStr(lineList.get(3));//职位名称
                sc.setCompany(lineList.get(2));//用人司局

                Department department;
                String departmentCode = lineList.get(42);//部门代码
                String departmentName = lineList.get(1);//部门名称
                List<Department> departments = departmentRepository.findByNatureAndCodeAndName(nature, departmentCode, departmentName);
                if(null!=departments&&!departments.isEmpty()){
                    department = departments.get(0);
                }else{
                    department=new Department();
                    department.setName(lineList.get(1));//部门名称
                    department.setCode(departmentCode);//部门代码
                    department.setUrl(lineList.get(45));//部门网址
                    department.setAttribute(lineList.get(31));//性质
                    department.setType(DepartmentType.findByString(lineList.get(34)));//层级
                    StringBuilder sb=new StringBuilder();
                    String phone1 = lineList.get(44);//电话1
//                    String phone2 = lineList.get(46);
//                    String phone3 = lineList.get(47);
                    if(StringUtils.isNotBlank(phone1)){
                        sb.append(phone1);
                        sb.append(",");
                    }
//                    if(StringUtils.isNotBlank(phone2)){
//                        sb.append(phone2);
//                        sb.append(",");
//                    }
//                    if(StringUtils.isNotBlank(phone3)){
//                        sb.append(phone3);
//                        sb.append(",");
//                    }
                    if (sb.length() != 0) {
                        String phone = sb.delete(sb.length() - 1, sb.length()).toString();
                        department.setPhone(phone);
                    }
                    department=departmentRepository.save(department);
                }
                sc.setDepartmentId(department.getId());

                sc.setProportion(lineList.get(18));//面试比例
                sc.setYear(Integer.valueOf(lineList.get(24)));//年份
                String interviewScope = lineList.get(21);
                if(StringUtils.isNotBlank(interviewScope)){
                    sc.setInterviewScope(Double.valueOf(interviewScope));//分数线
                }else{
                    sc.setInterviewScope(0D);//分数线
                }
                String number = lineList.get(7);
                if(StringUtils.isNotBlank(number)){
                    sc.setNumber(Integer.valueOf(number));//招考人数
                }else{
                    sc.setNumber(0);//招考人数
                }
                String enrolment = lineList.get(30);
                if(StringUtils.isNotBlank(enrolment)){
                    sc.setEnrolment(Integer.valueOf(enrolment));//报名人数
                }else{
                    sc.setEnrolment(0);//报名人数
                }
                scoreLineRepository.save(sc);
                log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                log.info(""+count++);
                log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }
        }
    }

    /**
     * 导入专业
     */
    @Override
    @Transactional
    public void importExcelSpecialty(List<List<List<String>>> list) {
        Specialty one=null;//一级专业
        Specialty two=null;//二级专业
        Education education;
        Education.values();
        for (int i = list.size(); i >0; i--) {
            List<List<String>> sheetList = list.get(i-1);
            education=Education.values()[list.size()-i];//当前学历
            for (int j = 2; j < sheetList.size(); j++) {  //跳过第一二行表头
                List<String> lineList = sheetList.get(j);
                String oneString = lineList.get(0);
                if(StringUtils.isNotBlank(oneString)){
                    Specialty specialty=new Specialty();
                    specialty.setEducation(education); //设置学历
                    specialty.setName(oneString);//名称
                    specialty.setType(1);//类型
                    specialty.setSorting(1);//排序
                    specialty.setStatus(Status.ZC);//状态
                    one=specialtyRepository.save(specialty);
                }
                String twoString = lineList.get(1);
                if(StringUtils.isNotBlank(twoString)){
                    Specialty specialty=new Specialty();
                    specialty.setEducation(education); //设置学历
                    specialty.setName(twoString);//名称
                    specialty.setType(2);//类型
                    specialty.setSorting(1);//排序
                    specialty.setStatus(Status.ZC);//状态
                    specialty.setParentId(one.getId());
                    two=specialtyRepository.save(specialty);
                }
                String threeString = lineList.get(2);
                Specialty specialty=new Specialty();
                specialty.setEducation(education); //设置学历
                specialty.setName(threeString);//名称
                specialty.setType(3);//类型
                specialty.setSorting(1);//排序
                specialty.setStatus(Status.ZC);//状态
                specialty.setParentId(two.getId());
                specialtyRepository.save(specialty);
            }
        }
    }

    @Override
    public void updatePostitionArea(List<List<List<String>>> list, Date enrolmentEndDate) {
        Map<String,Long> areaIdMap = getAreaIdMap();
        for (List<List<String>> sheetList : list) {
            for (int i = 1; i < sheetList.size(); i++) {  //跳过第一行表头
                List<String> lineList = sheetList.get(i);
                String code = lineList.get(8);//代码
                List<Position> byCode = positionRepository.findByCode(code);
                Position position = byCode.get(0);
                if(null==position.getArea()){
                    String areaString = lineList.get(20);//工作地区
                    if(StringUtils.isNotBlank(areaString)){
                        Long aLong = areaIdMap.get(areaString);
                        if(aLong!=null){
                            position.setAreaId(aLong);
                            positionRepository.save(position);
                        }
                    }
                }
            }
        }
    }

    /**
     * 对户籍生源地处理
     */
    private String getAreaString(String areaString){
        if(StringUtils.isNotBlank(areaString)) {
            String replace = areaString.replace("　", "");//去掉空格
            String[] areaArray = replace.split("x");
            int length = areaArray.length;
            switch (length){
                case 1:
                    areaString=areaArray[0];
                    break;
                case 2:
                    areaString=areaArray[0]+areaArray[1];
                    break;
                case 3:
                    areaString=areaArray[1]+areaArray[2];
                    break;
            }
        }
        return areaString;
    }
    /**
     * 地址名-id map
     */
    private  Map<String,Long> getAreaIdMap() {
        List<Area> allArea = areaRepository.findAll();
        Map<String,Long> map= Maps.newHashMap();
        allArea.forEach(area->{
            String name = area.getName().replace("　","");
            Area parentArea = area.getParentArea();
            if(null!=parentArea){
                if(null!=parentArea.getParentId()){
                    Area provinceArea = parentArea.getParentArea();
                    map.put(provinceArea.getName().replace("　","")+name,area.getId());
                }
                if(!name.equals(parentArea.getName())) {
                    name = parentArea.getName().replace("　","") + name;
                }
            }
            map.put(name,area.getId());
        });
        return map;
    }

    /**
     * 专业集合
     */
    private Map<String,List<Specialty>> getSpecialty(){
        Map<String,List<Specialty>> map=Maps.newHashMap();
        List<Specialty> all = specialtyRepository.findAll();
        all.forEach(specialty -> {
            if(specialty.getType()!=1) {
                String name = specialty.getName();
                List list = map.get(name);
                if (list == null) {
                    list = Lists.newArrayList();
                }
                list.add(specialty);
                map.put(name, list);
            }
        });
        return map;
    }
}
