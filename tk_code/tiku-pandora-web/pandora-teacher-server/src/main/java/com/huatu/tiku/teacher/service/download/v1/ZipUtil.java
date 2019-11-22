package com.huatu.tiku.teacher.service.download.v1;

import com.huatu.common.utils.date.DateFormatUtil;
import com.huatu.tiku.teacher.service.impl.download.v1.PdfWriteServiceImplV1;
import com.huatu.tiku.teacher.service.impl.download.v1.WordWriteServiceImplV1;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

/**
 * Created by huangqingpeng on 2019/3/19.
 */
public class ZipUtil {

    public static String zipFile(List<String> fileNames, String tailName, String dirPath) throws BizException {

        //生成压缩包
        String zipName = DateFormatUtil.NUMBER_FORMAT.format(new Date());
        boolean bln = false;
        try {
            bln = FunFileUtils.zipFile(zipName, fileNames, tailName, dirPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("获取压缩包：" + dirPath + zipName + ".zip");
        File fileZip = new File(dirPath + zipName + ".zip");
        if (bln) {
            //ftp上传文件  ?  windows 系统 ，上次服务器放开注释
            String savePath = "";
            String urlPath = "";
            if(WordWriteServiceImplV1.WORD_TAIL_NAME.equals(tailName)){
                savePath = FunFileUtils.WORD_FILE_SAVE_PATH;
                urlPath = FunFileUtils.WORD_FILE_SAVE_URL;
            }else if(ExcelHandleServiceImpl.EXCEL_TAIL_NAME.equals(tailName)){
                savePath = FunFileUtils.EXCEL_FILE_SAVE_URL;
                urlPath = FunFileUtils.EXCEL_FILE_SAVE_URL;
            }else if(PdfWriteServiceImplV1.PDF_TAIL_NAME.equals(tailName)){
                savePath = FunFileUtils.PDF_FILE_SAVE_PATH;
                urlPath = FunFileUtils.PDF_FILE_SAVE_URL;
            }else{
                throw new BizException(ErrorResult.create(1000012,"不支持文件格式:"+ tailName));
            }
            try {
                UploadFileUtil.getInstance().ftpUploadFile(fileZip, new String(fileZip.getName().getBytes("UTF-8"), "iso-8859-1"), savePath);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            //下载地址
            return urlPath + zipName + ".zip";
        } else {
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            throw new BizException(ErrorResult.create(1000107, "下载试卷失败"));
        }
//        return dirPath + zipName + ".zip";
    }

}
