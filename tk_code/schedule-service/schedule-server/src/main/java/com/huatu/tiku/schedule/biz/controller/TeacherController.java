package com.huatu.tiku.schedule.biz.controller;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import com.huatu.tiku.schedule.biz.bean.TeacherScoreBean;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.dto.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.repository.TeacherSubjectRepository;
import com.huatu.tiku.schedule.biz.service.*;
import com.huatu.tiku.schedule.biz.util.*;
import com.huatu.tiku.schedule.biz.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.vo.LeftMenuVo.SubMenu;

/**
 * 教师Controller
 * @author wangjian
 */
@RestController
@RequestMapping("teacher")
@Slf4j
public class TeacherController {

    private final TeacherService teacherService;

    private final CourseLiveService courseLiveService;

    private final CourseLiveTeacherService courseLiveTeacherService;

    private final CourseService courseService;

    private final PasswordEncoder passwordEncoder;

    private final MenuService menuService;

    private final TeacherSubjectRepository subjectRepository;

    @Value("${api.post_url}")
    private String POST_URL;

    @Value("${api.post_status_url}")
    private String POST_STATUS_URL;

    @Autowired
    public TeacherController(TeacherService teacherService, CourseLiveService courseLiveService,
                             CourseLiveTeacherService courseLiveTeacherService, CourseService courseService,
                             PasswordEncoder passwordEncoder, MenuService menuService, TeacherSubjectRepository subjectRepository) {
        this.teacherService = teacherService;
        this.courseLiveService = courseLiveService;
        this.courseLiveTeacherService = courseLiveTeacherService;
        this.courseService = courseService;
        this.passwordEncoder = passwordEncoder;
        this.menuService = menuService;
        this.subjectRepository = subjectRepository;
    }

    /**
     * 创建教师
     * @param createTeacherDto 创建教师dto
     * @param bindingResult    验证
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
        if(null==createTeacherDto.getLeaderFlag()){
            createTeacherDto.setLeaderFlag(false);
        }
        BeanUtils.copyProperties(createTeacherDto, teacher);
        if(null==teacher.getIsPartTime()){
            teacher.setIsPartTime(false);
        }
        List<TeacherSubject> teacherSubjects = createTeacherDto.getTeacherSubjects();
        // 设置默认密码为手机号
        teacher.setPassword(passwordEncoder.encode(createTeacherDto.getPhone()));
        // 默认待审核
        teacher.setStatus(TeacherStatus.DSH);
        //取出授课目录 手动创建判断空值防止空值插入
        teacher.setTeacherSubjects(null);
        teacher = teacherService.saveX(teacher, teacherSubjects);
//        try {
//            Long pid = PHPUtil.post(POST_URL, teacher);//TODO 添加同步到php代码
//            teacher.setPid(pid);
//            teacher=teacherService.save(teacher);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
        return new TeacherVo(teacher);
    }

    /**
     * 条件查询教师
     * @param leaderFlag  是否组长
     * @param status      审核状态
     * @param teacherType 教师类型
     */
    @GetMapping("findTeachers")
    public Page<TeacherVo> findTeachers(ExamType examType,
                                        String name,
                                        Long id,
                                        Long subjectId,
                                        Boolean leaderFlag,
                                        TeacherStatus status,
                                        TeacherType teacherType,
                                        Pageable page) {
        return teacherService.getTeacherList(examType, name, id, subjectId, leaderFlag, status, teacherType, page);
    }

    /**
     * 更改审核状态
     *  不能更改自己
     * @param updateTeacherStatusDto 更改状态
     * @param bindingResult          绑定
     * @return 是否成功
     */
    @PostMapping("updateTeacherStatus")
    public Boolean updateTeacherStatus(@Valid @RequestBody UpdateTeacherStatusDto updateTeacherStatusDto,
                                       @AuthenticationPrincipal CustomUser user,
                                       BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        List<Long> ids = updateTeacherStatusDto.getIds();
        TeacherStatus status = updateTeacherStatusDto.getStatus();
        int i = teacherService.updateTeacherStatus(ids, status, user.getId());
        List<Teacher> teachers = teacherService.findByIdIn(ids);
        Set<Long> pids=teachers.stream().map(bean->{  //pid集合
            if(null!=bean.getPid()){
                return bean.getPid();
            }else{
                return null;
            }
        }).collect(Collectors.toSet());
        if(pids!=null){ //有数据时删除null值 向php发送同步请求
            pids.remove(null);
            PHPUtil.postStatus(POST_STATUS_URL,pids,status.ordinal());//TODO 添加同步到php代码
        }
        return 0 != i;
    }

    /**
     * 根据科目模块授课级别查找指定时间段可用教师
     *
     * @param date               日期
     * @param timeBegin          开始时间
     * @param timeEnd            结束时间
     * @param subjectId          科目id
     * @param teacherCourseLevel 授课级别
     * @param courseId           课程id (面试用)
     * @return 教师集合
     */
    @GetMapping("getAvailableTeacher")
    public List<TeacherScoreBean> getAvailableTeacher(@DateTimeFormat(pattern = "yyyyMMdd") Date date,
                                                      ExamType examType,
                                                      Integer timeBegin,
                                                      Integer timeEnd,
                                                      Long subjectId,
                                                      TeacherCourseLevel teacherCourseLevel,
                                                      Long courseId) {
        return teacherService.getAvailableTeachers(date, timeBegin, timeEnd, examType, subjectId, teacherCourseLevel, courseId);
    }

    //查询可用的场控和主持人
    @GetMapping("getAvailableCtrl")
    public List<TeacherScoreBean> getAvailableCtrl(@DateTimeFormat(pattern = "yyyyMMdd") Date date,
                                                      Integer timeBegin,
                                                      Integer timeEnd,
                                                   TeacherType teacherType) {
        return teacherService.getAvailableCtrl(date, timeBegin, timeEnd,teacherType);
    }

    /**
     * 教师任务(查找未确认直播)
     * @param user 教师
     * @return 教师待确认直播列表
     */
    @GetMapping("taskTeacher")
    public List<TaskLiveVo> taskTeacher(@AuthenticationPrincipal CustomUser user) {
        Teacher teacher = teacherService.findOne(user.getId());
        return courseLiveService.findTaskTeacher(teacher);
    }

    /**
     * 教师任务待沟通
     * @param user                 用户
     * @param updateTaskTeacherDto 参数
     * @param bindingResult        验证
     * @return 结果
     */
    @PostMapping("updateTaskTeacherDGT")
    public Boolean updateTaskTeacherDGT(@AuthenticationPrincipal CustomUser user,
                                        @Valid @RequestBody UpdateTaskTeacherDto updateTaskTeacherDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Long teacherId = user.getId();//教师id
        List<Long> liveIds = updateTaskTeacherDto.getLiveIds();//直播ids
        int result;//返回值
        result = courseLiveTeacherService.updateTaskTeacher(teacherId, liveIds, CourseConfirmStatus.DGT);//根据直播id确认任务
        return result != 0;
    }

    /**
     * 教师任务确认
     *
     * @param updateTaskTeacherDto 教师确认实体类
     * @param bindingResult        绑定
     * @return 是否成功
     */
    @PostMapping("updateTaskTeacher")
    public Boolean updateTaskTeacher(@AuthenticationPrincipal CustomUser user, @Valid @RequestBody UpdateTaskTeacherDto updateTaskTeacherDto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Long teacherId = user.getId();//教师id
        List<Long> liveIds = updateTaskTeacherDto.getLiveIds();//直播ids
        int result;//返回值
        result = courseLiveTeacherService.updateTaskTeacher(teacherId, liveIds, CourseConfirmStatus.QR);//根据直播id确认任务
        if(result != 0&&null!=liveIds){  //如果是重新安排给相关教师发送短信
            liveIds.forEach(liveId->
            courseLiveTeacherService.sendSmsToAbout(liveId,teacherId)
            );
        }
        courseLiveTeacherService.updateLastTeacher(teacherId, liveIds);//修改原教师id 为修改教师时给原教师发送短信使用
        List<Course> courses = courseService.findAllByLives(liveIds);//提交的直播id查找出课程列表
        for (Course course : courses) {
            courseService.updateStatus(course);//将课程状态更改 并初始化助教数据
        }
        return result != 0;
    }

    /**
     * 获取讲师列表
     */
    @GetMapping("type/{type}")
    public List<Map<String, Object>> findByType(@PathVariable TeacherType type) {
        List<Map<String, Object>> teachers = Lists.newArrayList();

        teachers.add(ImmutableMap.of("id", "", "name", "全部"));

        teacherService.findByTeacherTypeAndStatus(type, TeacherStatus.ZC).forEach(teacher ->
            teachers.add(ImmutableMap.of("id", teacher.getId(), "name", teacher.getName()))
        );

        return teachers;
    }

    /**
     * 查找面试推荐教师 (讲授类型有面试,类型为讲师,状态为正常)
     *
     * @return 教师vo
     */
    @GetMapping("findInterviewTeacher")
    public Page<TeacherVo> findInterviewTeacher(Pageable page) {
        return teacherService.findInterviewTeacher(page);
    }

    /**
     * 获取当前用户的信息
     *
     * @param user 当前用户
     * @return 用户信息
     */
    @GetMapping("info")
    public Map<String, Object> info(@AuthenticationPrincipal CustomUser user) {
        Map<String, Object> info = Maps.newHashMap();

        // ID
        info.put("id", user.getId());
        // 教师姓名
        info.put("name", user.getName());

        Set<Menu> set = menuService.getMenus(user.getId());
        List<Menu> menus= Lists.newArrayList();
        menus.addAll(set);
        menus.sort(Comparator.comparingInt(Menu::getSort));//排序
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
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

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
    public Boolean updateTeacher(@Valid @RequestBody UpdateTeacherDto dto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        // 转成domain并入库
        Teacher teacher = teacherService.findOne(dto.getId());//取出原数据
        TeacherType teacherType = teacher.getTeacherType();
        if(!TeacherType.JS.equals(teacherType)){//不是教师类型 为其他类型教师修改为助教
            teacher.setTeacherType(TeacherType.ZJ);
        }
        List<TeacherSubject> Subjects = teacher.getTeacherSubjects();//取出原有的授课目录
        Boolean flag=false;
        if(null!=teacher.getPid()){  //有蓝色后台id
            if(!teacher.getName().equals(dto.getName())){ //名字不同时更改   电话不可改 pid不可改 状态另有接口修改
                flag=true;
            }
        }
        if(null==dto.getIsPartTime()){
            dto.setIsPartTime(false);
        }
        BeanUtils.copyProperties(dto, teacher);//更改原数据
        Boolean aBoolean = teacherService.updateTeacher(teacher, Subjects);
        if(flag&&aBoolean){
//            PHPUtil.post(POST_URL,teacher);//TODO 添加同步到php代码
        }
        return aBoolean;
    }

    /**
     * 更改助教信息
     */
    @PostMapping("updateAssistant")
    public Boolean updateAssistant(@Valid @RequestBody UpdateAssistantDto dto, BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        // 转成domain并入库
        Teacher teacher = teacherService.findOne(dto.getId());
        Boolean flag=false;
        if(null!=teacher.getPid()){  //有蓝色后台id
            if(!teacher.getName().equals(dto.getName())){ //名字不同时更改
                flag=true;
            }
        }
        if(null==dto.getIsPartTime()){
            dto.setIsPartTime(false);
        }
        BeanUtils.copyProperties(dto, teacher);
        TeacherType teacherType = teacher.getTeacherType();
        if(!TeacherType.ZJ.equals(teacherType)&&!TeacherType.JS.equals(teacherType)){
            teacher.setExamType(null);
            teacher.setSubjectId(null);
            List<TeacherSubject> teacherSubjects = teacher.getTeacherSubjects();
            teacher.setTeacherSubjects(null);
            subjectRepository.delete(teacherSubjects);
        }
        teacherService.save(teacher);
        if(flag){
//            PHPUtil.post(POST_URL,teacher);//TODO 添加同步到php代码
        }
        return true;
    }

    /**
     * 教师id查找教师
     *
     * @param teacherId 教师权限
     * @return 教师vo
     */
    @GetMapping("findTeacherById/{teacherId}")
    public TeacherVo findTeacherById(@PathVariable Long teacherId) {
        Teacher teacher = teacherService.findOne(teacherId);
        return new TeacherVo(teacher);
    }

    /**
     * 获取删除课程验证码
     */
    @GetMapping("getRemoveCourseCode")
    public Map<String, String> getRemoveCourseCode(HttpServletRequest request, @AuthenticationPrincipal CustomUser user) {
        String phone = user.getPhone();
        String regEx = "^1[3|4|5|7|8]\\d{9}$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(phone);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
        if (!rs) {
            throw new BadRequestException("手机号异常");
        }
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {//六位随机数
            sb.append(r.nextInt(10));
        }
        HttpSession session = request.getSession();
        session.setAttribute(phone, sb.toString());
        SmsUtil.sendSms(phone, "删除课程验证码:" + sb.toString());
        return ImmutableMap.of("phone", phone);
    }

    /**
     * 待沟通任务
     */
    @GetMapping("getLiveByDGT")
    public List<TaskLiveDGTVo> getLiveByDGT() {
        return courseLiveService.getLiveByDGT();
    }

    /**
     * 待沟通界面重新筛选教师
     *
     * @param teacherId 当前选中教师
     * @return 教师集合
     */
    @GetMapping("getAvailableTeacherByDGT")
    public List<TeacherScoreBean> getAvailableTeacherByDGT(@DateTimeFormat(pattern = "yyyyMMdd") Date date,
                                                           ExamType examType,
                                                           Integer timeBegin,
                                                           Integer timeEnd,
                                                           Long subjectId,
                                                           TeacherCourseLevel teacherCourseLevel,
                                                           Long courseId,
                                                           Long teacherId) {
        List<TeacherScoreBean> availableTeachers = teacherService.getAvailableTeachers(date, timeBegin, timeEnd, examType, subjectId, teacherCourseLevel, courseId);
        Iterator<TeacherScoreBean> iterator = availableTeachers.iterator();
        while (iterator.hasNext()) {
            TeacherScoreBean next = iterator.next();
            if (next.getId().equals(teacherId)) {
                iterator.remove();//剔除指定教师
                break;
            }
        }
        return availableTeachers;
    }

    /**
     * 待沟通界面重新筛选助教
     *
     * @param teacherId 当前选中教师
     * @return 可用助教列表
     */
    @GetMapping("getAvailableAssistantByDGT")
    public List<TeacherScoreBean> getAvailableAssistantByDGT(@DateTimeFormat(pattern = "yyyyMMdd") Date date, Integer timeBegin,
                                                             Integer timeEnd,
                                                             TeacherType teacherType, Long teacherId) {
        List<TeacherScoreBean> availableAssistant = teacherService.getAvailableCtrl(date, timeBegin, timeEnd, teacherType);
        Iterator<TeacherScoreBean> iterator = availableAssistant.iterator();
        while (iterator.hasNext()) {
            TeacherScoreBean next = iterator.next();
            if (next.getId().equals(teacherId)) {
                iterator.remove();
                break;
            }
        }
        return availableAssistant;
    }

    /**
     * 修改密码
     */
    @PostMapping("updatePassword")
    public Boolean updatePassword(@AuthenticationPrincipal CustomUser user,
                                 @Valid @RequestBody UpdatePasswordDto dto,
                                 BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        Teacher teacher = teacherService.findOne(user.getId());
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        if(!encoder.matches(dto.getOldPassword(),teacher.getPassword())){
            throw new BadRequestException("原密码错误");
        }
        teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        teacherService.save(teacher);
        return true;
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
        SmsUtil.sendSms(phone,"更换手机号验证码:"+sb.toString());
        return ImmutableMap.of("phone", phone,"scucces","true");
    }

    //修改电话号码
    @PostMapping("updatePhone")
    public Map updatePhone(@AuthenticationPrincipal CustomUser user,
                                 @Valid @RequestBody UpdatePhoneDto dto,
                                 BindingResult bindingResult,
                               HttpServletRequest request) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        String newPhone = dto.getPhone();//用户新手机号
        String regEx = "^1[2|3|4|5|6|7|8]\\d{9}$";
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(newPhone);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
        if (!rs) {
            throw new BadRequestException("手机号错误");
        }

        if (dto.getTeacherId() != null) {
            Set<Role> roles = user.getRoles();
            List<String> roleNames = Lists.newArrayList("超级管理员","教务","教学管理组","人力");
            Optional<Role> adminFlag=roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
            if(!adminFlag.isPresent()){  //非权限角色
                if(!user.getId().equals(dto.getTeacherId())){  //登录用户和修改的用户id不同 不是本人修改手机号
                    throw new BadRequestException("无对应权限");
                }
            }
        }

        if(null==teacherService.findByPhone(newPhone)) {
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
            Long teacherId = dto.getTeacherId();
            if(null==teacherId){
                teacherId=user.getId();
            }
            Teacher teacher = teacherService.findOne(teacherId);  //登录用户
            teacher.setPhone(newPhone);
            teacherService.save(teacher);
            return ImmutableMap.of("phone", newPhone,"scucces","true");
        }else {
            throw new BadRequestException("该手机号已注册");
        }
    }

}