package com.huatu.tiku.schedule.biz.controller;

import com.google.common.collect.ImmutableMap;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.constant.SessionKey;
import com.huatu.tiku.schedule.biz.domain.ClassHourFeedback;
import com.huatu.tiku.schedule.biz.domain.ClassHourInfo;
import com.huatu.tiku.schedule.biz.dto.CreatFeedBackDto;
import com.huatu.tiku.schedule.biz.dto.FeedbackUpdateDto;
import com.huatu.tiku.schedule.biz.dto.UpdateFeedBackDto;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import com.huatu.tiku.schedule.biz.service.ClassHourFeedbackService;
import com.huatu.tiku.schedule.biz.service.ClassHourInfoService;
import com.huatu.tiku.schedule.biz.util.ExcelUtil;
import com.huatu.tiku.schedule.biz.util.ImportExcelUtil;
import com.huatu.tiku.schedule.biz.util.SmsUtil;
import com.huatu.tiku.schedule.biz.vo.ClassHourFeedBackVo;
import com.huatu.tiku.schedule.biz.vo.ClassHourInforVo;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**教研课时反馈
 * @author wangjian
 **/
@RestController
@RequestMapping("feedback")
public class FeedbackController {

    @Autowired
    private ClassHourFeedbackService classHourFeedbackService;

    @Autowired
    private ClassHourInfoService classHourInfoService;

    @PostMapping("creat")
    public Boolean creat(@Valid @RequestBody CreatFeedBackDto dto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<ClassHourFeedback> result = classHourFeedbackService.check(dto.getExamType(), dto.getSubjectId(), dto.getYear(), dto.getMonth());
        if(!(null==result||result.isEmpty())){
            throw new BadRequestException("该组本月份已经反馈，请勿重复反馈");
        }
        ClassHourFeedback feedback = new ClassHourFeedback();
        BeanUtils.copyProperties(dto, feedback);
        classHourFeedbackService.saveX(dto);
        return true;
    }

    @GetMapping("getList")
    public Page getList(ExamType examType, Long subjectId, Integer year, Integer month, FeedbackStatus status, Pageable page) {
        Page<ClassHourFeedback> classHourFeedback = classHourFeedbackService.findClassHourFeedback(examType, subjectId, year, month, status, page);
        return new PageImpl(classHourFeedback.getContent().stream().map(ClassHourFeedBackVo::new).collect(Collectors.toList()), page,classHourFeedback.getTotalElements());
    }

    @GetMapping("info")
    public ClassHourFeedBackVo info(Long id){
        ClassHourFeedback feedback = classHourFeedbackService.findOne(id);
        ClassHourFeedBackVo vo=new ClassHourFeedBackVo(feedback);
        List<ClassHourInfo> infos = feedback.getInfos();
        for (ClassHourInfo info : infos) {
            ClassHourInforVo infoVo=new ClassHourInforVo();
            BeanUtils.copyProperties(info, infoVo);
            Long teacherId = infoVo.getTeacherId();
            if(null!=teacherId){
                infoVo.setTeacherName(info.getTeacher().getName());
            }
            vo.getInfo().add(infoVo);
        }
        return vo;
    }

    @PostMapping("updateStatus")
    public Object updateStatus(@Valid @RequestBody UpdateFeedBackDto dto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        ClassHourFeedback feedback = classHourFeedbackService.findOne(dto.getId());
        if(dto.getStatus()){
            feedback.setStatus(FeedbackStatus.YSH);
        }else{
            feedback.setStatus(FeedbackStatus.WTG);
        }
        classHourFeedbackService.save(feedback);
        return true;
    }

    @PostMapping("importExcel")
    public List<Map> importExcel(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        return classHourFeedbackService.importExcel(list);
    }

    /**
     * 修改课时获取验证码
     */
    @GetMapping("getCode")
    public Map<String, String> getCode(@AuthenticationPrincipal CustomUser user,
                                       HttpSession session) {
        String phone = user.getPhone();
        if (StringUtils.isBlank(phone)) {
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
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {//4位随机数
            sb.append(r.nextInt(10));
        }

        session.setAttribute(SessionKey.FEEDBACK_MODIFY_CODE, sb.toString());
        SmsUtil.sendSms(phone, "修改教研反馈课时验证码:" + sb.toString());

        return ImmutableMap.of("phone", phone, "scucces", "true");
    }

    /**
     * 验证验证码
     *
     * @param code          验证码
     * @param codeInSession 发送的验证码
     * @return ture/false
     */
    @PostMapping("verifyCode")
    public Boolean verifyCode(@NotBlank(message = "验证码不能为空") String code,
                              @SessionAttribute(SessionKey.FEEDBACK_MODIFY_CODE) String codeInSession,
                              HttpSession session) {
        boolean flag = code.equals(codeInSession);
        if (flag) {
            session.setAttribute(SessionKey.FEEDBACK_MODIFY_AUTH, true);
        }

        return flag;
    }

    /**
     * 是否验证过
     *
     * @param codeInSession 发送的验证码
     * @return ture/false
     */
    @GetMapping("isAuth")
    public Boolean isAuth(@SessionAttribute(name = SessionKey.FEEDBACK_MODIFY_CODE, required = false) String codeInSession) {

        return codeInSession != null;
    }

    /**
     * 修改教研信息
     *
     * @param feedbackUpdateDto 修改信息
     * @param authFlag          是否认证
     * @throws Exception
     */
    @PutMapping("updateClassHour")
    public void updateClassHour(@RequestBody @Valid FeedbackUpdateDto feedbackUpdateDto,
                                @SessionAttribute(SessionKey.FEEDBACK_MODIFY_AUTH) Boolean authFlag) throws Exception {
        classHourInfoService.updateClassHour(feedbackUpdateDto);
    }
}
