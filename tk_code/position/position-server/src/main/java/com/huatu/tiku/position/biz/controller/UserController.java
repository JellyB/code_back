package com.huatu.tiku.position.biz.controller;

import com.google.common.collect.ImmutableMap;
import com.huatu.tiku.position.base.exception.BadRequestException;
import com.huatu.tiku.position.biz.domain.Area;
import com.huatu.tiku.position.biz.domain.User;
import com.huatu.tiku.position.biz.dto.*;
import com.huatu.tiku.position.biz.service.UserService;
import com.huatu.tiku.position.biz.vo.UserVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**用户
 * @author wangjian
 **/
@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 返回用户信息
     */
    @GetMapping("getUserInfo")
    public UserVo getUserInfo(@RequestHeader String openId){
        User user = userService.findByOpenId(openId);
        if(null!=user) {
            UserVo vo=new UserVo();
            BeanUtils.copyProperties(user, vo);
            String certificate = user.getCertificate();
            if(StringUtils.isNotBlank(certificate)) {
                List<String> certificates = new ArrayList<>();
                String[] split = certificate.split(",");
                Collections.addAll(certificates, split);
                vo.setCertificates(certificates);
            }
            String englishType = user.getEnglishType();
            if(StringUtils.isNotBlank(englishType)){
                List<String> englishTypes = new ArrayList<>();
                String[] split = englishType.split(",");
                Collections.addAll(englishTypes, split);
                vo.setEnglishTypes(englishTypes);
            }
            vo.setSpecialty(null==user.getSpecialty()?null:user.getSpecialty().getName());
            Set<Area> areas = user.getAreas();
            vo.setAreas(null==areas?null:areas.stream().sorted((o1,o2)->(int)(o1.getId()-o2.getId())).map(area ->
                    ImmutableMap.of("id", area.getId(),"name",area.getName())).collect(Collectors.toList()));
            vo.setRegisterArea(null==user.getRegisterArea()?null:user.getRegisterArea().getName());
            vo.setBirthArea(null==user.getBirthArea()?null:user.getBirthArea().getName());
            return vo;
        }else{
            throw new BadRequestException("请登录");
        }
    }

    /**
     * 更改用户信息(职位筛选属性)
     */
    @PostMapping("updateUserInfo")
    public Object updateUserInfo(@Valid @RequestBody UpdateUserDto dto, BindingResult bindingResult,
                                 @RequestHeader String openId){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        User user = userService.findByOpenId(openId);
        userService.updateUserInfo(dto,user);
        return true;
    }

    /**
     * 更改用户所选地区
     */
    @PostMapping("updateUserAreas")
    public Object updateUserAreas(@Valid @RequestBody UpdateAreaDto dto, BindingResult bindingResult,
                                  @RequestHeader String openId){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        User user = userService.findByOpenId(openId);
        userService.updateUserAreas(dto,user);
        return true;
    }

    /**
     * 更改用户所选专业
     */
    @PostMapping("updateUserSpecialty")
    public Object updateUserSpecialty(@Valid @RequestBody UpdateSpecialtyDto dto, BindingResult bindingResult,
                                      @RequestHeader String openId){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        User user = userService.findByOpenId(openId);
        userService.updateUserSpecialty(dto,user);
        return true;
    }

    /**
     * 更改用户所选学历
     */
    @PostMapping("updateUserEducation")
    public Object updateUserEducation(@Valid @RequestBody UpdateEducationDto dto, BindingResult bindingResult,
                                      @RequestHeader String openId){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        User user = userService.findByOpenId(openId);
        userService.updateUserEducation(dto,user);
        return true;
    }

    /**
     * 更改用户所选工作经验
     */
    @PostMapping("updateUserExp")
    public Object updateUserExp(@Valid @RequestBody UpdateExpDto dto, BindingResult bindingResult,
                                @RequestHeader String openId){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        User user = userService.findByOpenId(openId);
        userService.updateUserExp(dto,user);
        return true;
    }
}
