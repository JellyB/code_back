package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.entity.po.User;
import com.huatu.tiku.interview.entity.po.UserClassRelation;
import com.huatu.tiku.interview.exception.ReqException;
import com.huatu.tiku.interview.repository.UserClassRelationRepository;
import com.huatu.tiku.interview.service.ClassInfoService;
import com.huatu.tiku.interview.service.ExcelImport;
import com.huatu.tiku.interview.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/29 16:34
 * @Description
 */
@Service
@Slf4j
public class ExcelImportImpl implements ExcelImport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserClassRelationRepository userClassRelationRepository;

    @Autowired
    private ClassInfoService classInfoService;

    @Override
    public Integer batchImport(InputStream inputStream) throws IOException {
//根据版本选择创建Workbook的方式
        Workbook wb = null;
        //根据文件名判断文件是2003版本还是2007版本
        try {
//            if (ExcelImportUtils.isExcel2007(fileName)) {
////                wb = new XSSFWorkbook(inputStream);
//                throw new ReqException(ResultEnum.ExcelTypeError);
//            } else {
//                wb = new HSSFWorkbook(inputStream);
                wb = new XSSFWorkbook(inputStream);
//            }
            return readExcelValue(wb);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    inputStream = null;
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    public Integer readExcelValue(Workbook wb) {
        Integer count = 0;
        //得到第一个shell
        Sheet sheet = wb.getSheetAt(0);
        //得到Excel的行数
        int totalRows = sheet.getPhysicalNumberOfRows();
        //总列数
        int totalCells = 0;
        //得到Excel的列数(前提是有行数)，从第二行算起
        if (totalRows >= 2 && sheet.getRow(1) != null) {
            totalCells = sheet.getRow(1).getPhysicalNumberOfCells();
        }
//        List<UserClassRelation> list = new ArrayList<>();
        //循环Excel行数,从第二行开始。标题不入库
        for (int r = 1; r < totalRows; r++) {
//            System.out.println("行数：" + totalRows);
//            String rowMessage = "";
            Row row = sheet.getRow(r);
            if (row == null) {
//                errorMsg += br + "第" + (r + 1) + "行数据有问题，请仔细检查！";
                continue;
            }
            UserClassRelation userClassRelation = new UserClassRelation();
            if (totalCells != 2) {
                throw new ReqException(ResultEnum.ExcelCellError);
            }
            Boolean check = true;
            for (int c = 0; c < totalCells; c++) {

//                System.out.println("列数：" + totalCells);
                Cell cell = row.getCell(c);
                if (null==cell){
                    check = false;
                }
                if (check) {
                    if (c == 0) {
                        if (cell.getStringCellValue().equals("国考")) {
                            userClassRelation.setClassId(1);
                        } else if (cell.getStringCellValue().equals("京考")) {
                            userClassRelation.setClassId(2);
                        } else {
                            check = false;
                        }
                    } else if (c == 1) {
                        DecimalFormat df = new DecimalFormat("#");
                        List<User> users = new ArrayList<>();
                        String phone = "";
                        int cellType = cell.getCellType();
                        if (cellType == Cell.CELL_TYPE_STRING) {
                            phone = df.format(cell.getStringCellValue());
                        } else if (cellType == Cell.CELL_TYPE_NUMERIC) {
                            phone = df.format(cell.getNumericCellValue()) ;
                        }
                        System.out.println(phone);
                        users = userService.findByPhone(phone);
                        if (users.isEmpty()) {
                            log.error(phone + "不存在这个手机号！！！！！！！！！！！！！！！！！！");
                            check = false;
                        }else{
                            if (users.size() > 1) {
                                log.error(phone + "重复！！！！！！！！！！！！！！！！！！");
                                check = false;
                            }else{
                                userClassRelation.setOpenId(users.get(0).getOpenId());
                            }
                        }
                    }
                }
            }
            if (check) {

//                list.add(userClassRelation);
                List<UserClassRelation> byOpenIdAndStatus = userClassRelationRepository.findByOpenIdAndStatus(userClassRelation.getOpenId(), 1);
                userClassRelation.setStatus(1);
                userClassRelation.setBoundType(1);
                userClassRelation.setBizStatus(1);
                userClassRelation.setStartTime(classInfoService.getOne(userClassRelation.getClassId()).getStartTime().toString());
                userClassRelation.setEndTime(classInfoService.getOne(userClassRelation.getClassId()).getEndTime().toString());

                if(byOpenIdAndStatus.isEmpty()){

                    userClassRelationRepository.save(userClassRelation);
                    count++;
                }else if(byOpenIdAndStatus.size() == 1){
                    userClassRelation.setId(byOpenIdAndStatus.get(0).getId());
                    userClassRelationRepository.save(userClassRelation);
                    count++;
                }else{
                    log.error("数据库出现重复数据");
                }
            }
        }
        return count;
    }
}
