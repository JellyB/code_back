package com.huatu.tiku.schedule.biz.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.dto.php.PHPBatchStatus;
import com.huatu.tiku.schedule.biz.dto.php.PHPResponse;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.repository.TeacherRepository;
import com.huatu.tiku.schedule.biz.service.TeacherService;
import com.huatu.tiku.schedule.biz.util.PHPUtil;
import com.huatu.tiku.schedule.biz.vo.php.PHPUpdateTeacherVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.dto.php.PHPUpdateTeacherDto;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 蓝色后台数据同步
 * 
 * @author Geek-S
 *
 */
@RestController
@RequestMapping("api")
public class BlueDataSyncApi {

	@Value("${api.token}")
	private String token;

	private final TeacherService teacherService;

	private final PasswordEncoder passwordEncoder;

    private final TeacherRepository teacherRepository;


    @Autowired
    public BlueDataSyncApi(TeacherService teacherService, PasswordEncoder passwordEncoder, TeacherRepository teacherRepository) {
        this.teacherService = teacherService;
        this.passwordEncoder = passwordEncoder;
        this.teacherRepository = teacherRepository;
    }

    /**
	 * 教师同步
	 *
	 * @param token
	 *            token
	 * @param phpUpdateTeacherDto
	 *            教师信息
	 */
	@PostMapping("teacher/sync")
	public PHPUpdateTeacherVo teacher(String token,@Valid @RequestBody PHPUpdateTeacherDto phpUpdateTeacherDto,
                                      BindingResult bindingResult) {
		if (this.token.equals(token)) {
            // 校验参数是否合法
            if (bindingResult.hasErrors()) {
                throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
            }
            Long pid = phpUpdateTeacherDto.getPid();
            Teacher teacher = teacherService.findByPid(pid);
            if(teacher==null){  //新建教师情况
                teacher=new Teacher();
                teacher.setPid(phpUpdateTeacherDto.getPid());
                teacher.setName(phpUpdateTeacherDto.getName());
                String phone = phpUpdateTeacherDto.getPhone();
                if(StringUtils.isNotBlank(phone)){
                    if(phone.length()==11&&phone.startsWith("1")){
                        Teacher checkPhone = teacherService.findByPhone(phone);
                        if(checkPhone!=null){
                            throw new BadRequestException("phone already exist"); //该电话已经存在
                        }else{
                            teacher.setPhone(phone);
                        }
                    }else{
                        throw new BadRequestException("phone format exception"); //电话格式错误
                    }
                    teacher.setPassword(passwordEncoder.encode(phone));//默认密码
                } else{
                    teacher.setPassword(passwordEncoder.encode("12345678"));//默认密码
                }
                Integer status = phpUpdateTeacherDto.getStatus();
                if (status != null) { //修改教师状态
                    if (status == 0) {
                        teacher.setStatus(TeacherStatus.DSH);
                    }
                    if (status == 1) {
                        teacher.setStatus(TeacherStatus.ZC);
                    } else {
                        teacher.setStatus(TeacherStatus.JY);
                    }
                }else{
                    teacher.setStatus(TeacherStatus.DSH);//默认状态
                }

                teacher.setTeacherType(TeacherType.JS);//默认讲师
                teacher.setLeaderFlag(false);//非组长
                teacher=teacherService.save(teacher);
                teacherRepository.saveRolesById(teacher.getId(), 2L);//默认非组长讲师
            }
//            else {  //修改教师情况
//                String phone = phpUpdateTeacherDto.getPhone();
//                if(StringUtils.isNotBlank(phone)) {
//                    if(!phone.equals(teacher.getPhone())){   //电话不同
//                        if (phone.length() == 11 && phone.startsWith("1")) {  //符合格式
//                            Teacher checkPhone = teacherService.findByPhone(phone);
//                            if (checkPhone != null && !phpUpdateTeacherDto.getPid().equals(checkPhone.getPid())) {//该电话已经存在
//                                throw new BadRequestException("phone already exist");
//                            }else{ //不存在修改电话
//                                teacher.setPhone(phone);
//                            }
//                        } else {
//                            throw new BadRequestException("phone format exception"); //电话格式错误
//                        }
//                    }
//                }else{  //电话为null
//                    teacher.setPhone(null);
//                }
//                Integer status = phpUpdateTeacherDto.getStatus();
//                if (status != null) { //修改教师状态
//                    if (status == 0) {
//                        teacher.setStatus(TeacherStatus.DSH);
//                    }
//                    if (status == 1) {
//                        teacher.setStatus(TeacherStatus.ZC);
//                    } else {
//                        teacher.setStatus(TeacherStatus.JY);
//                    }
//                }
//                teacher.setName(phpUpdateTeacherDto.getName());//修改名字
//                teacher=teacherService.save(teacher);
//            }
            return new PHPUpdateTeacherVo(teacher);
		} else {
			throw new BadRequestException("Token错误");
		}
	}


    /**
     * 批量修改状态
     * @param token  token
     * @param batchStatus 批量数据
     */
    @PostMapping("teacher/batchStatus")
    public Boolean batchStatus(String token, @RequestBody PHPBatchStatus batchStatus) {
        if (this.token.equals(token)) {
            List<Long> pids = batchStatus.getPids();
            if(pids==null||pids.isEmpty()){
                throw new BadRequestException("pids不能为null");
            }
            Integer status = batchStatus.getStatus();
            if(status==null){//修改教师状态
                throw new BadRequestException("status不能为null");
            }
            if(status!=0&&status!=1){
                status=2;
            }

            return 0!=teacherService.updateStatusByPids(pids,status);
        } else {
        throw new BadRequestException("Token错误");
        }

    }

//    /**
//     * 当前教师与php同步pid   一次性 临时使用
//     */
//    @RequestMapping("syncPid")
//    public String syncPid(String token) throws IOException {
//        if(this.token.equals(token)){
//            String resultString = PHPUtil.get();
//            ObjectMapper mapper = new ObjectMapper();
//            PHPResponse response = mapper.readValue(resultString, PHPResponse.class);
//            if (response.getCode() == 10000 && response.getMsg().equals("success")) {
//                List<PHPUpdateTeacherDto> datas = response.getData();
//                List<Teacher> all = teacherService.findAll();
//                for(PHPUpdateTeacherDto data: datas){
//                    String name = data.getName();
//                    if(StringUtils.isNotBlank(name)&&1==data.getStatus()){  //有值情况 状态正常
//                        Optional<Teacher> optionalTeacher= all.stream().filter(bean->bean.getName().trim().equals(name.trim())).findFirst();//名称相等
//                        if(optionalTeacher.isPresent()){
//                            Teacher teacher = optionalTeacher.get();
//                            Long pid = data.getPid();
//                            teacher.setPid(pid);
//                            teacherService.save(teacher);
//                        }
//                    }
//                }
//            }
//            return "同步成功";
//        }else{
//            return "token错误";
//        }
//
//    }
}
