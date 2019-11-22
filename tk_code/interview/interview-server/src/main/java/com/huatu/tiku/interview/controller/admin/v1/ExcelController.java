package com.huatu.tiku.interview.controller.admin.v1;

import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.service.ExcelImport;
import com.huatu.tiku.interview.util.ExcelImportUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 表格导入数据（学员分班）
 * @Author ZhenYang
 * @Date Created in 2018/1/29 16:23
 * @Description
 */

@RestController
@RequestMapping("/end/excel")
public class ExcelController {

    @Autowired
    ExcelImport excelImport;

    @RequestMapping(value = "/test",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result up(){
        return Result.ok("test");
    }

    @PostMapping(value = "/upClazzInfo",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result upClazz(@RequestParam(value="file") MultipartFile file) throws IOException {
        //判断文件是否为空
        if(file==null){
            return Result.build(ResultEnum.FileEmptyError);
        }

        //获取文件名
        String fileName=file.getOriginalFilename();

        //验证文件名是否合格
        if(!ExcelImportUtils.validateExcel(fileName)){
            return Result.build(ResultEnum.ExcelTypeError);
        }

        //进一步判断文件内容是否为空（即判断其大小是否为0或其名称是否为null）
        long size=file.getSize();
        if(StringUtils.isEmpty(fileName) || size==0){
            return Result.build(ResultEnum.FileEmptyError);
        }

        return Result.ok(excelImport.batchImport(file.getInputStream()));
    }
}
