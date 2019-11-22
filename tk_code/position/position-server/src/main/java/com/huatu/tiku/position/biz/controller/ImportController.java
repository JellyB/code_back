package com.huatu.tiku.position.biz.controller;

import com.huatu.tiku.position.biz.enums.Nature;
import com.huatu.tiku.position.biz.enums.PositionType;
import com.huatu.tiku.position.biz.service.ImportInterface;
import com.huatu.tiku.position.biz.util.ExcelUtil;
import com.huatu.tiku.position.biz.util.ImportExcelUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
@RestController
@RequestMapping("import")
public class ImportController {


    private ImportInterface importInterface;


    public ImportController(ImportInterface importInterface ) {
        this.importInterface = importInterface;
    }


    /**
     * 导入职位excel
     */
    @PostMapping("importExcelPostition")
    public Object importExcelPostition(@RequestParam("file") MultipartFile file,
                                       Nature nature,
                                       Integer year,
                                       @DateTimeFormat(pattern = "yyyy-MM-dd")Date beginDate,
                                       @DateTimeFormat(pattern = "yyyy-MM-dd")Date endDate,
                                       PositionType type,
                                       @DateTimeFormat(pattern = "yyyy-MM-dd")Date enrolmentEndDate) throws IOException {
        Assert.notNull(file, "请选择文件上传");
        Assert.notNull(nature, "考试类型不能为空");
        Assert.notNull(year, "年份不能为空");
        Assert.notNull(beginDate, "开始时间不能为空");
        Assert.notNull(endDate, "结束时间不能为空");
        Assert.notNull(type, "职位类型不能为空");
        Assert.notNull(enrolmentEndDate, "报名结束日期不能为空");

        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importInterface.importExcelPostition(list,nature, year,beginDate,endDate,type, enrolmentEndDate);
        return true;
    }

    @PostMapping("updateEnrolmentEndDateString")
    public Object updateEnrolmentEndDateString(@RequestParam("file") MultipartFile file,String date) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importInterface.updateEnrolmentEndDateString(list,date);
        return true;
    }

    /**
     * 修改职位excel(拆分备注后调用)
     */
    @PostMapping("updatePostition")
    public Object updatePostition(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importInterface.updatePostition(list);
        return true;
    }

    /**
     * 对原有excelnull地区进行处理
     */
    @PostMapping("updatePostitionArea")
    public Object updatePostitionArea(@RequestParam("file") MultipartFile file,
                                       @DateTimeFormat(pattern = "yyyy-MM-dd")Date enrolmentEndDate) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importInterface.updatePostitionArea(list, enrolmentEndDate);
        return true;
    }

    /**
     * 导入职位分数线
     */
    @PostMapping("importExcelScoreLine")
    public Object importExcelScoreLine(@RequestParam("file") MultipartFile file, Nature nature, Integer year) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importInterface.importExcelScoreLine(list,nature,year);
        return true;
    }

    /**
     * 导入专业数据
     */
    @PostMapping("importExcelSpecialty")
    public Object importExcelSpecialty(@RequestParam("file") MultipartFile file) throws IOException {
        boolean isExcel2003 = false;
        if (ExcelUtil.isExcel2003(file.getOriginalFilename())) {
            isExcel2003 = true;
        }
        ImportExcelUtil poi = new ImportExcelUtil();
        List<List<List<String>>> list = poi.read(file.getInputStream(), isExcel2003);
        importInterface.importExcelSpecialty(list);
        return true;
    }

}
