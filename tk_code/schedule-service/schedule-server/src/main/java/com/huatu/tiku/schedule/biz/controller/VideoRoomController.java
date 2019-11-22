package com.huatu.tiku.schedule.biz.controller;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.VideoRoom;
import com.huatu.tiku.schedule.biz.dto.CreateVideoRoomDto;
import com.huatu.tiku.schedule.biz.dto.DeleteVideoRoomDto;
import com.huatu.tiku.schedule.biz.dto.UpdateVideoRoomDto;
import com.huatu.tiku.schedule.biz.service.VideoRoomService;
import com.huatu.tiku.schedule.biz.util.SmsUtil;
import com.huatu.tiku.schedule.biz.vo.VideoRoomVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**录影棚相关接口
 * @author wangjian
 **/
@Slf4j
@RestController
@RequestMapping("videoRoom")
public class VideoRoomController {

    private VideoRoomService videoRoomService;

    @Autowired
    public VideoRoomController(VideoRoomService videoRoomService) {
        this.videoRoomService = videoRoomService;
    }

    @PostMapping("createVideoRoom")
    public VideoRoomVo createVideoRoom(@Valid @RequestBody CreateVideoRoomDto createVideoRoomDto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        VideoRoom videoRoom = new VideoRoom();
        BeanUtils.copyProperties(createVideoRoomDto, videoRoom);
        if(null==videoRoom.getShowFlag()){
            videoRoom.setShowFlag(true);//默认展示
        }
        videoRoom=videoRoomService.save(videoRoom);
        return VideoRoomVo.builder().id(videoRoom.getId()).name(videoRoom.getName()).mark(videoRoom.getMark()).showFlag(videoRoom.getShowFlag()).build();
    }

    @PostMapping("updateVideoRoom")
    public VideoRoomVo updateVideoRoom(@Valid @RequestBody UpdateVideoRoomDto updateVideoRoomDto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        VideoRoom videoRoom = videoRoomService.findOne(updateVideoRoomDto.getId());
        BeanUtils.copyProperties(updateVideoRoomDto, videoRoom);
        videoRoom=videoRoomService.save(videoRoom);
        return VideoRoomVo.builder().id(videoRoom.getId()).name(videoRoom.getName()).mark(videoRoom.getMark()).showFlag(videoRoom.getShowFlag()).build();
    }

    @GetMapping("getVideoRoomList")
    public List<VideoRoomVo> getVideoRoomList(){
        List<VideoRoom> videoRoomList = videoRoomService.getVideoRoomList();
        List<VideoRoomVo> list= Lists.newArrayList();
        videoRoomList.stream().filter(VideoRoom::getShowFlag).forEach(room-> list.add(VideoRoomVo.builder().id(room.getId()).name(room.getName()).mark(room.getMark()).showFlag(room.getShowFlag()).build()));
        return list;
    }

    /**
     * 获取验证码
     */
    @GetMapping("getCode")
    public Map<String, String> getCode(HttpServletRequest request, @AuthenticationPrincipal CustomUser user){
        String phone = user.getPhone();
        if(StringUtils.isBlank(phone)){
            throw new BadRequestException("loginUser phone Exception");
        }
        String regEx = "^1[2|3|4|5|6|7|8]\\d{9}$";
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
        SmsUtil.sendSms(phone,"删除录影棚验证码:"+sb.toString());
        return ImmutableMap.of("phone", phone,"scucces","true");
    }

    @PostMapping("deleteVideoRoom")
    public Object delete(@Valid @RequestBody DeleteVideoRoomDto dto, BindingResult bindingResult,
                         HttpServletRequest request, @AuthenticationPrincipal CustomUser user){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String userPhone = user.getPhone();//电话
        if(StringUtils.isBlank(userPhone)){
            throw new BadRequestException("loginUser phone Exception");
        }
        HttpSession session = request.getSession();
        String code = (String)session.getAttribute(userPhone);//验证码
        if(!dto.getCode().equals(code)){//验证码不同
            throw new BadRequestException("验证码错误");
        }
        session.removeAttribute(userPhone);
        videoRoomService.deleteX(dto.getRoomId(),dto.getReason());
        return true;
    }
}
