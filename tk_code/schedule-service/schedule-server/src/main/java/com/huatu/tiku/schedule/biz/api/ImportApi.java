package com.huatu.tiku.schedule.biz.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huatu.tiku.schedule.biz.dto.php.PHPResponse;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.service.*;
import com.huatu.tiku.schedule.biz.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**导入课表
 * @author wangjian
 **/
@RestController
@RequestMapping("api/import")
public class ImportApi {
    private ImportService importService;

    @Autowired
    public ImportApi(ImportService importService) {
        this.importService = importService;
    }

    /**
     * 导入横版课程
     * @param file
     * @param courseId 课程id
     * @param flag 是否是原版(第一行数字结尾为阶段 )
     */
    @PostMapping("import")
    @ResponseBody
    public String importCourse(@RequestParam("file") MultipartFile file, @RequestParam Long courseId, Boolean flag) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        if(flag==null){
            flag=false;
        }
        importService.importTeacherCourse(list, courseId, flag);
        return "导入成功";
    }

    /**
     *  导入滚动排课
     */
    @PostMapping("importRoll")
    public String importRoll(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importService.importCourseRoll(list);
        return "导入成功";
    }

    /**
     * 导入竖版课表
     * @param file 文件
     * @param courseName 课程名
     * @param examType 考试类型
     * @param category 课程类型
     */
    @PostMapping("importCourse")
    public String importCourse(@RequestParam("file") MultipartFile file, String courseName, ExamType examType, CourseCategory category) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importService.importCourse(list,courseName, examType,category);
        return "导入成功";
    }

    /**
     * 导入竖版课表滚动排课
     * @param file 文件
     * @param examType 考试类型
     * @param category 课程类型
     */
    @PostMapping("importCourseRoll")
    public String importCourseRoll(@RequestParam("file") MultipartFile file  , ExamType examType, CourseCategory category) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importService.importCourseRoll(list, examType,category);
        return "导入成功";
    }

    @PostMapping("importTeacher")
    public String importTeacher(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportTeacherExcelUtil poi = new ImportTeacherExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importService.importTeachers(list);
        return "导入成功";
    }

    @GetMapping("importTeacherByPHP")
    public String importTeacherByPHP() throws IOException {
        String resultString = PHPUtil.get();
        ObjectMapper mapper = new ObjectMapper();
        PHPResponse response = mapper.readValue(resultString, PHPResponse.class);
        if (response.getCode() == 10000 && response.getMsg().equals("success")) {
            importService.importTeacherByPHP(response.getData());
        }
        return "导入成功";
    }
}
