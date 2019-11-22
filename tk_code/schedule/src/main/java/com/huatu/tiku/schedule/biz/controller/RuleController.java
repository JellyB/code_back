package com.huatu.tiku.schedule.biz.controller;

import com.google.common.collect.ImmutableMap;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.Rule;
import com.huatu.tiku.schedule.biz.dto.CreatRuleDto;
import com.huatu.tiku.schedule.biz.dto.DeleteRuleDto;
import com.huatu.tiku.schedule.biz.dto.UpdateRuleDto;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.service.RuleService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.SmsUtil;
import com.huatu.tiku.schedule.biz.vo.RuleVo;
import com.huatu.tiku.schedule.biz.vo.Schedule.PageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wangjian
 **/
@RestController
@RequestMapping("rule")
@Slf4j
public class RuleController {

    private final RuleService ruleService;

    @Autowired
    public RuleController( RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * 获取验证码
     */
    @GetMapping("getCode")
    public Map<String, String> getCode(HttpServletRequest request, @AuthenticationPrincipal CustomUser user){
        String phone = user.getPhone();
        String regEx = "^1[3|4|5|7|8]\\d{9}$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(phone);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
        if (!rs) {
            throw new BadRequestException("手机号异常");
        }
        Random r=new Random();
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<4;i++){//4位随机数
            sb.append(r.nextInt(10));
        }
        HttpSession session = request.getSession();
        session.setAttribute(phone,sb.toString());
        SmsUtil.sendSms(phone,"验证码:"+sb.toString());
        return ImmutableMap.of("phone", phone,"scucces","true");
    }

    /**
     * 创建新规则
     * @param creatRuleDto 参数
     * @return 结果
     */
    @PostMapping("creatRule")
    public Boolean creatRule(@Valid @RequestBody CreatRuleDto creatRuleDto, BindingResult bindingResult,
                             HttpServletRequest request,@AuthenticationPrincipal CustomUser user) throws ParseException {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        if(null==creatRuleDto.getDateEnd()){
            SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd" );
            Date endDate=sdf.parse("3000-01-01");
            creatRuleDto.setDateEnd(endDate);
        }
        if(creatRuleDto.getDateBegin().getTime()>creatRuleDto.getDateEnd().getTime()){
            throw new BadRequestException("开始时间不能晚于结束时间");
        }

        String phone = user.getPhone();//电话
        HttpSession session = request.getSession();
        String code = (String)session.getAttribute(phone);//验证码
        if(!creatRuleDto.getCode().equals(code)){//验证码不同
            throw new BadRequestException("验证码错误");
        }
        // 转成domain并入库
        Rule rule = new Rule();
        BeanUtils.copyProperties(creatRuleDto, rule);
        int beginInt=Integer.parseInt(DateformatUtil.format1(rule.getDateBegin()));
        int endInt = Integer.parseInt(DateformatUtil.format1(rule.getDateEnd()));
        List<Rule> rules = ruleService.checkDate(beginInt, endInt);//时间重合的规则
        ExamType examType = rule.getExamType();
        CourseCategory courseCategory = rule.getCourseCategory();
        CourseLiveCategory liveCategory = rule.getLiveCategory();
        if(examType.equals(ExamType.MS)) {//面试类型
            if (null == liveCategory) {
                throw new BadRequestException("请设置面试授课形式");
            }
        }
        for(Rule check:rules){
            if(examType.equals(check.getExamType())&&courseCategory.equals(check.getCourseCategory())){//考试类型 分类相同
                if(examType.equals(ExamType.MS)){//面试类型
                    if(liveCategory.equals(check.getLiveCategory())){//授课类型相同
                        throw new BadRequestException("该时间段已有"+examType.getText()+courseCategory.getText()+liveCategory.getText()+"规则设置");
                    }
                }else {//类型 分类相同 且不是面试 返回异常
                    throw new BadRequestException("该时间段已有"+examType.getText()+courseCategory.getText()+"规则设置");
                }
            }
        };
        rule.setDateBeginInt(beginInt);
        rule.setDateEndInt(endInt);
        ruleService.save(rule);
        log.info("新课时比例 : {} -> {}",  user.getId(),rule.toString());
        return  true;
    }

    /**
     * 删除规则
     * @param deleteRuleDto 参数
     * @param bindingResult 验证
     * @param request 请求
     * @param user 用户
     * @return 结果
     */
    @PostMapping("deleteRule")
    public Boolean deleteRule(@Valid @RequestBody DeleteRuleDto deleteRuleDto, BindingResult bindingResult,
                              HttpServletRequest request, @AuthenticationPrincipal CustomUser user){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String phone = user.getPhone();//电话
        HttpSession session = request.getSession();
        String code = (String)session.getAttribute(phone);//验证码
        if(!deleteRuleDto.getCode().equals(code)){//验证码不同
            throw new BadRequestException("验证码错误");
        }
        Rule rule = ruleService.findOne(deleteRuleDto.getId());
        log.info("删除课时比例 : {} -> {}",  user.getId(),rule.toString());
        ruleService.delete(deleteRuleDto.getId());
        return true;
    }

    /**
     * 通过id查找规则
     * @param id
     * @return
     */
    @GetMapping("findRule/{id}")
    public RuleVo findByid(@PathVariable Long id){
        Rule rule = ruleService.findOne(id);
        RuleVo vo=new RuleVo();
        vo.setId(rule.getId());
        vo.setCoefficient(rule.getCoefficient());
        vo.setCourseCategory(rule.getCourseCategory());
        CourseLiveCategory liveCategory = rule.getLiveCategory();
        if(liveCategory!=null){
            vo.setLiveCategory(liveCategory);
        }
        vo.setExamType(rule.getExamType());
        Date dateBegin = rule.getDateBegin();
        vo.setDateBegin(DateformatUtil.format0(dateBegin));
        Date dateEnd = rule.getDateEnd();
        Integer dateEndInt = rule.getDateEndInt();
        if(30000101==dateEndInt){
            vo.setDateEnd("");
        }else{
            vo.setDateEnd(DateformatUtil.format0(dateEnd));
        }
        Date nowDate=new Date();
        if(nowDate.getTime()<dateBegin.getTime()){//当前日期大于结束日期
            vo.setStatus("未生效");
        }else if(nowDate.getTime()>dateEnd.getTime()){
            vo.setStatus("已过期");
        }else{
            vo.setStatus("生效中");
        }
        return vo;
    }

    /**
     * 修改规则
     * @param updateRuleDto 参数
     * @return 修改结果
     */
    @PostMapping("updateRule")
    public Boolean updateRule(@Valid @RequestBody UpdateRuleDto updateRuleDto, BindingResult bindingResult,
                              HttpServletRequest request, @AuthenticationPrincipal CustomUser user) throws ParseException {
        if(null==updateRuleDto.getDateEnd()){
            SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd" );
            Date endDate=sdf.parse("3000-01-01");
            updateRuleDto.setDateEnd(endDate);
        }
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String phone = user.getPhone();//电话
        HttpSession session = request.getSession();
        String code = (String)session.getAttribute(phone);//验证码
        if(!updateRuleDto.getCode().equals(code)){//验证码不同
            throw new BadRequestException("验证码错误");
        }
        // 转成domain并入库
        Rule rule = new Rule();
        BeanUtils.copyProperties(updateRuleDto, rule);
        int beginInt=Integer.parseInt(DateformatUtil.format1(rule.getDateBegin()));
        int endInt = Integer.parseInt(DateformatUtil.format1(rule.getDateEnd()));
        List<Rule> rules = ruleService.checkDateExceptId(beginInt, endInt,rule.getId());//时间重合的规则
        ExamType examType = rule.getExamType();
        CourseCategory courseCategory = rule.getCourseCategory();
        CourseLiveCategory liveCategory = rule.getLiveCategory();
        if(examType.equals(ExamType.MS)&&CourseCategory.XXK.equals(courseCategory)) {//面试类型线下课
            if (null == liveCategory) {
                throw new BadRequestException("请设置面试授课形式");
            }
        }
        for(Rule check:rules){
            if(examType.equals(check.getExamType())&&courseCategory.equals(check.getCourseCategory())){//考试类型 分类相同
                if(examType.equals(ExamType.MS)){//面试类型
                    if(liveCategory.equals(check.getLiveCategory())){//授课类型相同
                        throw new BadRequestException("该时间段已有"+examType.getText()+courseCategory.getText()+liveCategory.getText()+"规则设置");
                    }
                }else {//类型 分类相同 且不是面试 返回异常
                    throw new BadRequestException("该时间段已有"+examType.getText()+courseCategory.getText()+"规则设置");
                }
            }
        };
        rule.setDateBeginInt(beginInt);
        rule.setDateEndInt(endInt);
        ruleService.save(rule);
        log.info("修改课时比例 : {} -> {}",  user.getId(),rule.toString());
        return true;
    }


    /**
     * 分页查询规则
     * @param page 分页参数
     * @return 数据
     */
    @GetMapping("findRule")
    public PageVo findRule(Pageable page) {
        Page<Rule> pages = ruleService.getRuleList(page);
        List<RuleVo> ruleVoList=new ArrayList();
        pages.getContent().forEach(rule->{
            RuleVo vo=new RuleVo();
            vo.setId(rule.getId());
            vo.setCoefficient(rule.getCoefficient());
            vo.setCourseCategory(rule.getCourseCategory());
            CourseLiveCategory liveCategory = rule.getLiveCategory();
            if(liveCategory!=null){
                vo.setLiveCategory(liveCategory);
            }
            vo.setExamType(rule.getExamType());
            Date dateBegin = rule.getDateBegin();
            vo.setDateBegin(DateformatUtil.format0(dateBegin));
            Date dateEnd = rule.getDateEnd();
            Integer dateEndInt = rule.getDateEndInt();
            if(30000101==dateEndInt){
                vo.setDateEnd("");
            }else{
                vo.setDateEnd(DateformatUtil.format0(dateEnd));
            }
            Date nowDate=new Date();
            if(nowDate.getTime()<dateBegin.getTime()){//当前日期大于结束日期
                vo.setStatus("未生效");
            }else if(nowDate.getTime()>dateEnd.getTime()){
                vo.setStatus("已过期");
            }else{
                vo.setStatus("生效中");
            }
            ruleVoList.add(vo);
        });
        PageVo pageVo=new PageVo();//返回值
        pageVo.setFirst(pages.isFirst());
        pageVo.setLast(pages.isLast());
        pageVo.setNumber(pages.getNumber());
        pageVo.setNumberOfElements(pages.getNumberOfElements());
        pageVo.setSize(pages.getSize());
        pageVo.setTotalElements(pages.getTotalElements());
        pageVo.setTotalPages(pages.getTotalPages());
        pageVo.setContent(ruleVoList);
        return pageVo;
    }
}
