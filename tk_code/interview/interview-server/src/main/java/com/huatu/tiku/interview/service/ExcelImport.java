package com.huatu.tiku.interview.service;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/29 16:34
 * @Description
 */
public interface ExcelImport {
    Integer batchImport(InputStream inputStream) throws IOException;
    Integer readExcelValue(Workbook wb);
}
