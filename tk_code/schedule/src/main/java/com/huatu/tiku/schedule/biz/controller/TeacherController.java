package com.huatu.tiku.schedule.biz.controller;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.dto.*;
import com.huatu.tiku.schedule.biz.dto.php.PHPResponse;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.service.*;
import com.huatu.tiku.schedule.biz.util.*;
import com.huatu.tiku.schedule.biz.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.Course;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.LiveRoom;
import com.huatu.tiku.schedule.biz.domain.Menu;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.domain.TeacherSubject;
import com.huatu.tiku.schedule.biz.vo.LeftMenuVo.SubMenu;
import org.springframework.web.multipart.MultipartFile;

/**
 * 教师Controller
 * @author wangjian
 **/
@RestController
@RequestMapping("teacher")
public class TeacherController {

    private final TeacherService teacherService;

    private final CourseLiveService courseLiveService;

    private final CourseLiveTeacherService courseLiveTeacherService;

    private final CourseService courseService;

    private final PasswordEncoder passwordEncoder;

    private final MenuService menuService;


    @Autowired
	public TeacherController(TeacherService teacherService, CourseLiveService courseLiveService,
                             CourseLiveTeacherService courseLiveTeacherService, CourseService courseService,
                             PasswordEncoder passwordEncoder, MenuService menuService) {
		this.teacherService = teacherService;
		this.courseLiveService = courseLiveService;
		this.courseLiveTeacherService = courseLiveTeacherService;
		this.courseService = courseService;
		this.passwordEncoder = passwordEncoder;
		this.menuService = menuService;
    }

    /**
     * 创建教师
     * @param createTeacherDto 创建教师dto
     * @param bindingResult 验证
     * @return 教师属性
     */
    @PostMapping("createTeacher")
    public TeacherVo createTeacher(@Valid @RequestBody CreateTeacherDto createTeacherDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        // 转成domain并入库
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(createTeacherDto, teacher);
        List<TeacherSubject> teacherSubjects = createTeacherDto.getTeacherSubjects();
        // 设置默认密码为手机号
        teacher.setPassword(passwordEncoder.encode(createTeacherDto.getPhone()));
        // 默认待审核
        teacher.setStatus(TeacherStatus.DSH);
        //取出授课目录 手动创建判断空值防止空值插入
        teacher.setTeacherSubjects(null);
        teacher = teacherService.saveX(teacher, teacherSubjects);
        //PHPUtil.post(teacher);//TODO 添加同步到php代码
        return new TeacherVo(teacher);
    }

    /**
     * 条件查询教师
     * @param examType  考试类型
     * @param name  教师名字
     * @param id 教师id
     * @param subjectId 课程id
     * @param leaderFlag 是否组长
     * @param status 审核状态
     * @param teacherType 教师类型
     * @param page 分页参数
     * @return 教师列表
     */
    @GetMapping("findTeachers")
    public Page<TeacherVo> findTeachers(ExamType examType,
                                      String name,
                                      Long id,
                                      Long subjectId,
                                      Boolean leaderFlag,
                                      TeacherStatus status,
                                      TeacherType teacherType,
                                      Pageable page){
        return teacherService.getTeacherList(examType, name, id, subjectId, leaderFlag, status, teacherType, page);
    }

    /**
     * 更改审核状态
     * @param updateTeacherStatusDto 更改状态
     * @param bindingResult 绑定
     * @return 是否成功
     */
    @PostMapping("updateTeacherStatus")
    public Boolean updateTeacherStatus(@Valid @RequestBody UpdateTeacherStatusDto updateTeacherStatusDto,
                                       @AuthenticationPrincipal CustomUser user,
                                       BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<Long> ids = updateTeacherStatusDto.getIds();
        TeacherStatus status = updateTeacherStatusDto.getStatus();
        int i = teacherService.updateTeacherStatus(ids,status,user.getId());
//        List<Long> pids=teacherService.findByIds(ids);
//        PHPUtil.postStatus(pids,status.ordinal());//TODO 添加同步到php代码
        return 0!=i;
    }

    /**
     *  根据科目模块授课级别查找指定时间段可用教师
     * @param date 日期
     * @param timeBegin 开始时间
     * @param timeEnd 结束时间
     * @param subjectId 科目id
     * @param teacherCourseLevel 授课级别
     * @param courseId 课程id (面试用)
     * @param moduleId 模块id
     * @return 教师集合
     */
    @GetMapping("getAvailableTeacher")
    public List<TeacherScoreBean> getAvailableTeacher(@DateTimeFormat(pattern="yyyyMMdd") Date date,
                                               ExamType examType,
                                              Integer timeBegin,
                                              Integer timeEnd,
                                              Long subjectId,
                                              @RequestParam(defaultValue = "COMMON") TeacherCourseLevel teacherCourseLevel,
                                                      Long courseId,
                                                      Long moduleId){
        return teacherService.getAvailableTeachers(date, timeBegin, timeEnd, examType, subjectId, teacherCourseLevel, courseId,moduleId);
    }

    /**
     * 教师任务(查找未确认直播)
     * @param user 教师
     * @return 教师待确认直播列表
     */
    @GetMapping("taskTeacher")
    public List<TaskLiveVo> taskTeacher(@AuthenticationPrincipal CustomUser user){
        Teacher teacher = teacherService.findOne(user.getId());
        return courseLiveService.findTaskTeacher(teacher);
    }

    /**
     * 教师任务待沟通
     * @param user 用户
     * @param updateTaskTeacherDto 参数
     * @param bindingResult 验证
     * @return 结果
     */
    @PostMapping("updateTaskTeacherDGT")
    public Boolean updateTaskTeacherDGT(@AuthenticationPrincipal CustomUser user,@Valid @RequestBody UpdateTaskTeacherDto updateTaskTeacherDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Long teacherId = user.getId();//教师id
        List<Long> liveIds = updateTaskTeacherDto.getLiveIds();//直播ids
        Teacher teacher = teacherService.findOne(teacherId);//教师
        int result;//返回值
        TeacherType teacherType = teacher.getTeacherType();//教师类型
        if(teacherType.equals(TeacherType.JS)){//教师类型
            result=courseLiveTeacherService.updateTaskTeacher(teacherId,liveIds, CourseConfirmStatus.DGT);//根据直播id确认任务
        }else{//助教类型
            result=courseLiveService.updateTaskTeacher(teacher,liveIds, CourseConfirmStatus.DGT);//根据教师类型 直播id确认任务
        }
        return result!=0;
    }

    /**
     * 教师任务确认
     * @param updateTaskTeacherDto 教师确认实体类
     * @param bindingResult  绑定
     * @return 是否成功
     */
    @PostMapping("updateTaskTeacher")
    public Boolean updateTaskTeacher(@AuthenticationPrincipal CustomUser user,@Valid @RequestBody UpdateTaskTeacherDto updateTaskTeacherDto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Long teacherId = user.getId();//教师id
        List<Long> liveIds = updateTaskTeacherDto.getLiveIds();//直播ids
        Teacher teacher = teacherService.findOne(teacherId);//教师
        int result;//返回值
        TeacherType teacherType = teacher.getTeacherType();//教师类型
        if(teacherType.equals(TeacherType.JS)){//教师类型
            result=courseLiveTeacherService.updateTaskTeacher(teacherId,liveIds, CourseConfirmStatus.QR);//根据直播id确认任务
        }else{//助教类型
            result=courseLiveService.updateTaskTeacher(teacher,liveIds, CourseConfirmStatus.QR);//根据教师类型 直播id确认任务
        }
        List<Course> courses = courseService.findAllByLives(liveIds);//提交的直播id查找出课程列表
        for(Course course:courses) {
            courseService.updateStatus(course, teacherType);//将课程状态更改
        }
        return result!=0;
    }

    /**
     * 获取讲师列表
     */
    @GetMapping("type/{type}")
	public List<Map<String, Object>> findByType(@PathVariable TeacherType type) {
		List<Map<String, Object>> teachers = Lists.newArrayList();

		teachers.add(ImmutableMap.of("id", "", "name", "全部"));

		teacherService.findByTeacherTypeAndStatus(type, TeacherStatus.ZC).forEach(teacher -> {
			teachers.add(ImmutableMap.of("id", teacher.getId(), "name", teacher.getName()));
		});

		return teachers;
	}

    /**
     * 查找可用助教(学习师 场控等)
     * @param date 日期
     * @param timeBegin 开始时间
     * @param timeEnd 结束时间
     * @param teacherType 教师类型
     * @return 可用助教列表
     */
    @GetMapping("getAvailableAssistant")
    public List<TeacherScoreBean> getAvailableAssistant(@DateTimeFormat(pattern="yyyyMMdd")Date date,Integer timeBegin,
                                                 Integer timeEnd,
                                                 TeacherType teacherType){

        return teacherService.getAvailableAssistant(date,timeBegin,timeEnd,teacherType);
    }

    /**
     * 查找面试推荐教师 (讲授类型有面试,类型为讲师,状态为正常)
     * @return 教师vo
     */
    @GetMapping("findInterviewTeacher")
    public Page<TeacherVo> findInterviewTeacher(Pageable page){
        return teacherService.findInterviewTeacher(page);
    }

	/**
	 * 获取当前用户的信息
	 * 
	 * @param user
	 *            当前用户
	 * @return 用户信息
	 */
	@GetMapping("info")
	public Map<String, Object> info(@AuthenticationPrincipal CustomUser user) {
		Map<String, Object> info = Maps.newHashMap();

		// 教师姓名
		info.put("name", user.getName());

		Set<Menu> menus = menuService.getMenus(user.getId());

		// 拼装菜单
		List<LeftMenuVo> leftMenuVos = Lists.newArrayList();

		// 一级菜单
		Map<Long, LeftMenuVo> parentMenus = Maps.newHashMap();

		Iterator<Menu> menuIterator = menus.iterator();
		while (menuIterator.hasNext()) {
			Menu temp = menuIterator.next();

			if (temp.getParentId() == null) {
				LeftMenuVo leftMenuVo = new LeftMenuVo();
				leftMenuVo.setTitle(temp.getName());
				leftMenuVo.setList(Lists.newArrayList());

				leftMenuVos.add(leftMenuVo);
				parentMenus.put(temp.getId(), leftMenuVo);

				menuIterator.remove();
			}
		}

		menus.forEach(menu -> {
			if (menu.getParentId() != null) {
				SubMenu subMenu = new SubMenu();
				subMenu.setBt(menu.getName());
				subMenu.setLink(menu.getRoute());

				parentMenus.get(menu.getParentId()).getList().add(subMenu);
			}
		});

		info.put("leftMenus", leftMenuVos);

		// 角色
		List<String> roles = user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList());

		info.put("roles", roles);

		return info;
	}

	/**
	 * 获取用户角色
	 */
	@GetMapping("{id}/role")
	public List<OptionVo> getRolesById(@PathVariable Long id) {
		return teacherService.getRolesById(id);
	}

	/**
	 * 获取用户权限
	 */
	@GetMapping("{id}/permissions")
	public Map<String, List<OptionVo>> getPermissionsById(@PathVariable Long id) {
		Map<String, List<OptionVo>> permissions = Maps.newHashMap();

		permissions.put("function", teacherService.getRolesByIdExclude(id));

		permissions.put("data", teacherService.getDataPermissionsById(id));

		return permissions;
	}

	/**
	 * 更新用户角色
	 */
	@PostMapping("{id}/role")
	public Boolean updateRolesById(@PathVariable Long id, @RequestBody List<Long> roleIds) {

		teacherService.updateRolesById(id, roleIds);

		return true;
	}

	/**
	 * 更新用户角色&数据权限
	 */
	@PostMapping("{id}/permissions")
	public Boolean updatePermissionsById(@PathVariable Long id, @RequestBody List<List<Long>> permissionIds) {

		teacherService.updatePermissionsById(id, permissionIds);

		return true;
	}

    /**
     * 更改教师信息
     */
    @PostMapping("updateTeacher")
	public Boolean updateTeacher(@Valid @RequestBody UpdateTeacherDto updateTeacherDto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        // 转成domain并入库
        Teacher teacher = teacherService.findOne(updateTeacherDto.getId());//取出原数据
        List<TeacherSubject> Subjects = teacher.getTeacherSubjects();//取出原有的授课目录
        BeanUtils.copyProperties(updateTeacherDto, teacher);//更改原数据
        Boolean aBoolean = teacherService.updateTeacher(teacher, Subjects);
        //PHPUtil.post(teacher);//TODO 添加同步到php代码
        return aBoolean;
    }

    /**
     * 更改助教信息
     */
    @PostMapping("updateAssistant")
	public Boolean updateAssistant(@Valid @RequestBody UpdateAssistantDto updateAssistantDto, BindingResult bindingResult){
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        // 转成domain并入库
        Teacher teacher = teacherService.findOne(updateAssistantDto.getId());
        BeanUtils.copyProperties(updateAssistantDto, teacher);

        teacherService.save(teacher);
        //PHPUtil.post(teacher); //TODO 添加同步到php代码
	    return true;
    }

    /**
     * 教师id查找教师
     * @param teacherId 教师权限
     * @return 教师vo
     */
    @GetMapping("findTeacherById/{teacherId}")
    public TeacherVo findTeacherById(@PathVariable Long teacherId){
        Teacher teacher = teacherService.findOne(teacherId);
        return new TeacherVo(teacher);
    }

    /**
     * @description: 导入普通课程
     * @author duanxiangchao
     * @date 2018/5/16 下午4:15
     */
    @PostMapping("import")
    @ResponseBody
    public String importCourse(@RequestParam("file") MultipartFile file, @RequestParam Long courseId) throws IOException {
        boolean isExcel2003 = false;
        if(ExcelUtil.isExcel2003(file.getOriginalFilename())){
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        teacherService.importTeacherCourse(list, courseId);
        return "导入成功";
    }

    /**
     * @description: 导入滚动排课
     * @author duanxiangchao
     * @date 2018/5/16 下午4:15
     */
    @PostMapping("importRoll")
    public String importRollCourse(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isExcel2003 = false;
        if(ExcelUtil.isExcel2003(file.getOriginalFilename())){
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        teacherService.importTeacherRollCourse(list);
        return "导入成功";
    }

    @PostMapping("importTeacher")
    public String importTeacher(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isExcel2003 = false;
        if(ExcelUtil.isExcel2003(file.getOriginalFilename())){
            isExcel2003 = true;
        }
        ImportTeacherExcelUtil poi = new ImportTeacherExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        teacherService.importTeachers(list);
        return "导入成功";
    }

    @GetMapping("importTeacherByPHP")
    public String importTeacherByPHP() throws IOException {
        String resultString = PHPUtil.get();
        ObjectMapper mapper = new ObjectMapper();
        PHPResponse response = mapper.readValue(resultString, PHPResponse.class);
        if(response.getCode()==10000&&response.getMsg().equals("success")){
            teacherService.importTeacherByPHP(response.getData());
        }
        return "导入成功";
    }

    /**
     * 获取删除课程验证码
     */
    @GetMapping("getRemoveCourseCode")
    public Map<String, String> getRemoveCourseCode(HttpServletRequest request,@AuthenticationPrincipal CustomUser user){
        //TODO 校验权限
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
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<6;i++){//六位随机数
            sb.append(r.nextInt(10));
        }
        HttpSession session = request.getSession();
        session.setAttribute(phone,sb.toString());
        SmsUtil.sendSms(phone,"删除课程验证码:"+sb.toString());
        return ImmutableMap.of("phone", phone);
    }

    @GetMapping("getLiveByDGT")
    public List<TaskLiveDGTVo> getLiveByDGT(){
        return courseLiveService.getLiveByDGT();
    }

    /**
     *  待沟通界面重新筛选教师
     * @param teacherId 当前选中教师
     * @return 教师集合
     */
    @GetMapping("getAvailableTeacherByDGT")
    public List<TeacherScoreBean> getAvailableTeacherByDGT(@DateTimeFormat(pattern="yyyyMMdd") Date date,
                                                      ExamType examType,
                                                      Integer timeBegin,
                                                      Integer timeEnd,
                                                      Long subjectId,
                                                      @RequestParam(defaultValue = "COMMON") TeacherCourseLevel teacherCourseLevel,
                                                      Long courseId,
                                                      Long moduleId,
                                                           Long teacherId){
        List<TeacherScoreBean> availableTeachers = teacherService.getAvailableTeachers(date, timeBegin, timeEnd, examType, subjectId, teacherCourseLevel, courseId, moduleId);
        Iterator<TeacherScoreBean> iterator = availableTeachers.iterator();
        while(iterator.hasNext()){
            TeacherScoreBean next = iterator.next();
            if(next.getId().equals(teacherId)){
                iterator.remove();//剔除指定教师
            }
        }
        availableTeachers.remove(teacherId);
        return availableTeachers;
    }

    /**
     * 待沟通界面重新筛选助教
     * @param teacherId 当前选中教师
     * @return 可用助教列表
     */
    @GetMapping("getAvailableAssistantByDGT")
    public List<TeacherScoreBean> getAvailableAssistantByDGT(@DateTimeFormat(pattern="yyyyMMdd")Date date,Integer timeBegin,
                                                        Integer timeEnd,
                                                        TeacherType teacherType,Long teacherId){
        List<TeacherScoreBean> availableAssistant = teacherService.getAvailableAssistant(date, timeBegin, timeEnd, teacherType);
        Iterator<TeacherScoreBean> iterator = availableAssistant.iterator();
        while(iterator.hasNext()){
            TeacherScoreBean next = iterator.next();
            if(next.getId().equals(teacherId)){
                iterator.remove();
            }
        }
        availableAssistant.remove(teacherId);
        return availableAssistant;
    }
}
