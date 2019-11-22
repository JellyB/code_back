package com.huatu.tiku.schedule.biz.controller;

import com.google.common.collect.Lists;
import com.huatu.tiku.schedule.base.config.CustomUser;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.biz.domain.*;
import com.huatu.tiku.schedule.biz.enums.*;
import com.huatu.tiku.schedule.biz.service.*;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import com.huatu.tiku.schedule.biz.util.ExportExcelUtil;
import com.huatu.tiku.schedule.biz.vo.Schedule.PageVo;
import com.huatu.tiku.schedule.biz.vo.Statistics.StatisticsBodyVo;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author wangjian
 **/
@RestController
@RequestMapping("statistics")
public class StatisticsController {

    private TeacherService teacherService;

    private StatisticsService statisticsService;

    @Autowired
    public StatisticsController( TeacherService teacherService,StatisticsService statisticsService) {
        this.teacherService = teacherService;
        this.statisticsService = statisticsService;
    }

    /**
     * 教师课程统计
     *
     * @param dateBegin 开始时间
     * @param dateEnd   结束时间
     * @param teacherId 教师id
     * @return 统计结果
     */
    @GetMapping("statistics")
    public PageVo statistics(@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                             @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                             Long teacherId, Pageable page, @AuthenticationPrincipal CustomUser user) {
        if (dateBegin == null) {
            throw new BadRequestException("请设置时间范围");
        }
        if (dateEnd == null) {
            dateEnd = new Date();
        }
        if (dateBegin.after(dateEnd)) {
            throw new BadRequestException("开始时间不能晚于结束时间");
        }
        if (teacherId == null)
            teacherId = user.getId();
        Teacher teacher = teacherService.findOne(teacherId);
        ExamType examType = teacher.getExamType();
        TeacherType teacherType = teacher.getTeacherType();//教师类型
        Long userId = user.getId();
        if (!userId.equals(teacherId)) {
            // 可以查看全部的角色
            List<String> roleNames = Lists.newArrayList("超级管理员", "教学管理组", "人力");
            // 当前用户角色
            Set<Role> roles = user.getRoles();
            // 管理员
            Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
            if (!adminFlag.isPresent()) {//如果不是管理员
                Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务")).findFirst();
                if (jwFlag.isPresent()) {  // 教务
                    // 数据权限
                    Set<ExamType> dataPermissioins = user.getDataPermissions();
                    if (!dataPermissioins.contains(examType)) { // 判断教师是否在权限内
                        throw new BadRequestException("无【" + examType.getText() + "】的教师查看权限");
                    }
                } else { //不是教务判断是否是组长
                    Optional<Role> zzFlag = roles.stream().filter(role -> role.getName().equals("组长")).findFirst();
                    Boolean leaderFlag = user.getLeaderFlag();
                    if (leaderFlag && zzFlag.isPresent()) { //组长

                    } else { //不是教务 不是组长报错
                        throw new BadRequestException("无该老师课表的查看权限");
                    }
                }
            }
        }
        StatisticsBodyVo map = statisticsService.getStatistics(dateBegin, dateEnd, teacherType, teacherId,null, true);
        PageVo pageVo = new PageVo();//返回值
        int start = page.getOffset();
        int end = page.getPageSize() + start;
        List<Object> bodyList = map.getBody();
        if (null != bodyList) {
            if (end > bodyList.size()) {
                end = bodyList.size();
            }
            map.setBody(bodyList.subList(start, end));
        }
        pageVo.setContent(map);
        int contentSize = bodyList == null ? 0 : bodyList.size();
        int size = contentSize % page.getPageSize() == 0 ? contentSize / page.getPageSize() : contentSize / page.getPageSize() + 1;
        pageVo.setFirst(page.getPageNumber() == 0 );//第一页
        pageVo.setLast(page.getPageNumber() == size - 1 );//最后一页
        pageVo.setNumber(page.getPageNumber());//页数
        pageVo.setNumberOfElements(end - start);//本页条数
        pageVo.setSize(page.getPageSize());//条数
        pageVo.setTotalElements(bodyList == null ? 0L : (long) bodyList.size());//总数
        pageVo.setTotalPages(size);//总页数
        return pageVo;
    }

    /**
     * 课时统计详情导出excel
     *
     * @param dateBegin 开始查询日期
     * @param dateEnd   结束查询日期
     * @param teacherId 教师id
     */
    @GetMapping("downLoadStatisticsExcel")
    public void downLoadExcel(@DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                              @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                              Long teacherId,
                              Boolean exportClassHour,
                              @AuthenticationPrincipal CustomUser user,
                              HttpServletResponse response, HttpServletRequest request) throws IOException {
        Teacher teacher;
        Subject subject;
        ExamType examType;
        if (null == teacherId) {
            teacher = teacherService.findOne(user.getId());
            examType = teacher.getExamType();
            subject = teacher.getSubject();
        } else {
            teacher = teacherService.findOne(teacherId);
            examType=teacher.getExamType();
            subject=teacher.getSubject();
        }
        HSSFWorkbook downLoadExcel=statisticsService.getStatisticsExcel(dateBegin,dateEnd, teacher,examType, subject, exportClassHour);


        String agent = request.getHeader("User-Agent");
        String fileName = "课时统计明细表-" + teacher.getName() + " " + DateformatUtil.format0(dateBegin) + "~" + DateformatUtil.format0(dateEnd) + "";
        if (agent.contains("Firefox")) {
            fileName = new String(fileName.getBytes(), "ISO-8859-1");
        } else {
            fileName = URLEncoder.encode(fileName, "utf-8");
            fileName = fileName.replace("+", " ");
        }
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        OutputStream os = response.getOutputStream();
        downLoadExcel.write(os);
        os.flush();
        os.close();
    }

    @GetMapping("rank")
    public Object getrank(@RequestParam(required = false)List<String> parameters,
                          @RequestParam(required = false) List<Boolean> isPartTimes,
                          @RequestParam(required = false) List<TeacherType> types,
                          ExamType examType, Long subjectId,
                          @RequestParam(required = false)List<Long> subjectIds,
                          @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                          @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                          @PageableDefault(size = 50, sort = {"count"}, direction = Sort.Direction.DESC) Pageable page,
                          @AuthenticationPrincipal CustomUser user){
        List<StatisticsBodyVo> vos = statisticsService.getRankStatisticsBodyVos(parameters,isPartTimes, types, examType, subjectId, dateBegin, dateEnd, page, user,subjectIds);
        int length = vos.size();
        PageVo pageVo = new PageVo();//返回值
        int start = page.getOffset();
        int end = page.getPageSize() + start;
        if (null != vos) {
            if (start > vos.size()) {
                start = vos.size();
            }
            if (end > vos.size()) {
                end = vos.size();
            }
            vos = vos.subList(start, end);
        }
        pageVo.setContent(vos);
        int contentSize = vos == null ? 0 : length;
        int size = contentSize % page.getPageSize() == 0 ? contentSize / page.getPageSize() : contentSize / page.getPageSize() + 1;
        pageVo.setFirst(page.getPageNumber() == 0 );//第一页
        pageVo.setLast(page.getPageNumber() == size - 1 );//最后一页
        pageVo.setNumber(page.getPageNumber());//页数
        pageVo.setNumberOfElements(end - start);//本页条数
        pageVo.setSize(page.getPageSize());//条数
        pageVo.setTotalElements((long) length);//总数
        pageVo.setTotalPages(size);//总页数
        return pageVo;
    }



    @GetMapping("downLoadRankExcel")
    public void downLoadRankExcel(@RequestParam(required = false)List<String> parameters,
                                  @RequestParam(required = false) List<Boolean> isPartTimes,
                                  @RequestParam(required = false) List<TeacherType> types,
                                  ExamType examType, Long subjectId,
                                  @RequestParam(required = false)List<Long> subjectIds,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                                  @PageableDefault( sort = {"count"}, direction = Sort.Direction.DESC) Pageable page,
                                  @AuthenticationPrincipal CustomUser user,
                                  HttpServletResponse response, HttpServletRequest request) throws IOException {
        List<StatisticsBodyVo> vos = statisticsService.getRankStatisticsBodyVos(parameters,isPartTimes, types, examType, subjectId, dateBegin, dateEnd, page, user, subjectIds);
        if(!(page.getPageSize()==10&&page.getPageNumber()==0)){
            int start = page.getOffset();
            int end = page.getPageSize() + start;
            if (null != vos) {
                if (start > vos.size()) {
                    start = vos.size();
                }
                if (end > vos.size()) {
                    end = vos.size();
                }
                vos = vos.subList(start, end);
            }
        }

        List<List<String>> rowlist=Lists.newArrayList();
        int index=0;
        for (StatisticsBodyVo vo : vos) {
            List<String> bean=Lists.newArrayList();
            bean.add(String.valueOf(++index));
            bean.add(vo.getTeacherName());
            bean.add(vo.getCount());
            bean.add(vo.getCountLiveSK());
            bean.add(vo.getCountVideo());
            bean.add(vo.getCountXXKSK());
            bean.add(vo.getCountXXKSchoolSK());//线下分校授课
            bean.add(new DecimalFormat("0.00").format(new BigDecimal(Double.valueOf(vo.getCountLiveLX())).add(new BigDecimal(Double.valueOf(vo.getCountXXKLX())))));//直播线下练习
            bean.add(vo.getCountOnline());//线上助教
            bean.add(vo.getCountOffline());//线下助教
            bean.add(vo.getCountSimulation());
            bean.add(vo.getCountReally());
            bean.add(vo.getCountArticle());
            bean.add(vo.getCountAudio());
            bean.add(vo.getCountSSKSK());
            bean.add(vo.getCountDMJZSK());
            rowlist.add(bean);
        }
        HSSFWorkbook downLoadExcel =ExportExcelUtil.downLoadRankExcel("师资排名明细表", parameters, rowlist);
        String agent = request.getHeader("User-Agent");
        String fileName = "师资排名明细表-" + DateformatUtil.format0(dateBegin) + "~" + DateformatUtil.format0(dateEnd) + "";
        if (agent.contains("Firefox")) {
            fileName = new String(fileName.getBytes(), "ISO-8859-1");
        } else {
            fileName = URLEncoder.encode(fileName, "utf-8");
            fileName = fileName.replace("+", " ");
        }
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xls");
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            downLoadExcel.write(os);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            os.close();
        }
    }

    @GetMapping("downLoadBatchStatisticsExcel")
    public void downLoadBatchStatisticsExcel(@RequestParam(required = false) List<Boolean> isPartTimes,
                                  @RequestParam(required = false) List<TeacherType> types,
                                  ExamType examType, Long subjectId,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateBegin,
                                  @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateEnd,
                                  Boolean exportClassHour,
                                  @AuthenticationPrincipal CustomUser user,
                                  HttpServletResponse response, HttpServletRequest request) throws IOException {

        if (dateBegin == null) {
            throw new BadRequestException("请设置时间范围");
        }
        if (dateEnd == null) {
            dateEnd = new Date();
        }
        if (dateBegin.after(dateEnd)) {
            throw new BadRequestException("开始时间不能晚于结束时间");
        }
        List<ExamType> examTypes = Lists.newArrayList();
        // 可以查看全部的角色
        List<String> roleNames = Lists.newArrayList("超级管理员", "人力", "教学管理组");
        // 当前用户角色
        Set<Role> roles = user.getRoles();
        // 管理员
        Optional<Role> adminFlag = roles.stream().filter(role -> roleNames.contains(role.getName())).findFirst();
        if (!adminFlag.isPresent()) { //不是指定角色 判断权限
            Optional<Role> jwFlag = roles.stream().filter(role -> role.getName().equals("教务") || role.getName().equals("运营") || role.getName().equals("录播教务") || role.getName().equals("录播产品")).findFirst();
            if (jwFlag.isPresent()) { // 教务
                // 数据权限
                Set<ExamType> dataPermissioins = user.getDataPermissions();
                if (examType == null) {
                    examTypes.addAll(dataPermissioins);
                } else {
                    examTypes.add(examType);
                }
            } else { //不是教务判断是否是组长
                Optional<Role> zzFlag = roles.stream().filter(role -> role.getName().equals("组长")).findFirst();
                Boolean leaderFlag = user.getLeaderFlag();
                if (leaderFlag && zzFlag.isPresent()) { //组长
                    if (examType == null) {
                        examTypes.add(user.getExamType());
                    } else {
                        examTypes.add(examType);
                    }
                    if (subjectId == null) {
                        subjectId = user.getSubjectId();
                    }
                } else { //不是教务 不是组长报错
                    throw new BadRequestException("无讲师课表的查看权限");
                }
            }
        } else {  //指定角色
            if (null != examType) { //指定类型添加类型 不指定类型添加全部
                examTypes.add(examType);
            } else {  //不指定类型添加全部
                examTypes = null;  //设置为null 即可查询全部数据
            }
        }
        List<Teacher> rankTeachers = teacherService.findRankTeachers(isPartTimes, types, examTypes, subjectId, null);
//        if(null==rankTeachers||rankTeachers.isEmpty()){
//            return ;
//        }
        String agent = request.getHeader("User-Agent");
        String zipName = "课时统计明细表";
        if (agent.contains("Firefox")) {
            zipName = new String(zipName.getBytes(), "ISO-8859-1");
        } else {
            zipName = URLEncoder.encode(zipName, "utf-8");
            zipName = zipName.replace("+", " ");
        }
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + zipName + ".zip");
        response.addHeader("Pargam", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        OutputStream os = null;
        ZipOutputStream out = null;
        try {
            os = response.getOutputStream();
            out = new ZipOutputStream(os);
            String dateBeginString = DateformatUtil.format0(dateBegin);
            String dateEndnString = DateformatUtil.format0(dateEnd);
            for (Teacher rankTeacher : rankTeachers) {
                HSSFWorkbook statisticsExcel = statisticsService.getStatisticsExcel(dateBegin, dateEnd, rankTeacher, rankTeacher.getExamType(), rankTeacher.getSubject(), exportClassHour);
                if(statisticsExcel!=null){
                    String fileName = "课时统计明细表-" + rankTeacher.getName() + " " +dateBeginString + "~" + dateEndnString + ".xls";
                    ZipEntry entry = new ZipEntry(fileName);
                    out.putNextEntry(entry);
                    statisticsExcel.write(out);
                    out.flush();
                    out.closeEntry();
                }
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
            os.close();
        }

    }
}
