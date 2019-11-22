package com.huatu.tiku.schedule.biz.api;

import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.dto.php.PHPBatchStatus;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.TeacherLevel;
import com.huatu.tiku.schedule.biz.enums.TeacherStatus;
import com.huatu.tiku.schedule.biz.enums.TeacherType;
import com.huatu.tiku.schedule.biz.service.TeacherService;
import com.huatu.tiku.schedule.biz.vo.php.PHPUpdateTeacherVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.dto.php.PHPUpdateTeacherDto;

import java.util.List;

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

    @Autowired
    public BlueDataSyncApi(TeacherService teacherService, PasswordEncoder passwordEncoder) {
        this.teacherService = teacherService;
        this.passwordEncoder = passwordEncoder;
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
	public PHPUpdateTeacherVo teacher(String token, @RequestBody PHPUpdateTeacherDto phpUpdateTeacherDto) {
		if (this.token.equals(token)) {
            Long pid = phpUpdateTeacherDto.getPid();
            if(pid==null){
                throw new  BadRequestException("请输入id");
            }
            Teacher teacher = teacherService.findByPid(pid);
            if(teacher==null){//新建教师情况
                teacher=new Teacher();
                teacher.setPid(phpUpdateTeacherDto.getPid());
                teacher.setStatus(TeacherStatus.DSH);//默认状态
                if(phpUpdateTeacherDto.getPhone()!=null){
                    teacher.setPassword(passwordEncoder.encode(phpUpdateTeacherDto.getPhone()));//默认密码
                }
                teacher.setTeacherType(TeacherType.JS);//默认讲师
                teacher.setLeaderFlag(false);//非组长
                teacher.setTeacherLevel(TeacherLevel.H1);//默认一级
            }
            Integer status = phpUpdateTeacherDto.getStatus();
            if(status!=null){//修改教师状态
                if(status==0){
                    teacher.setStatus(TeacherStatus.DSH);
                }if(status==1){
                    teacher.setStatus(TeacherStatus.ZC);
                }else{
                    teacher.setStatus(TeacherStatus.JY);
                }
			}
            if(StringUtils.isNotBlank(phpUpdateTeacherDto.getName())){//名字有值
                teacher.setName(phpUpdateTeacherDto.getName());
            }
            Integer examTypeId = phpUpdateTeacherDto.getExamType();//考试类型id
            if(examTypeId!=null){//考试类型有值
                ExamType examTypeById = ExamType.findById(examTypeId);//id转化成类型
                teacher.setExamType(examTypeById);

            }
            Long subjectId = phpUpdateTeacherDto.getSubjectId();//科目id
            if(subjectId!=null){//科目id有值
                if(subjectId==6){//科目为面试
                    teacher.setExamType(ExamType.MS);
                }
                teacher.setSubjectId(phpUpdateTeacherDto.getSubjectId());

            }
            if(StringUtils.isNotBlank(phpUpdateTeacherDto.getPhone())){//电话有值
                teacher.setPhone(phpUpdateTeacherDto.getPhone());

            }
			teacher=teacherService.save(teacher);
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
            if(pids==null||pids.size()==0){
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
}
