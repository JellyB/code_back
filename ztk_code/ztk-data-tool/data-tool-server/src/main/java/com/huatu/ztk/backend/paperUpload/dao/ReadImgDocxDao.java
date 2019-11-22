package com.huatu.ztk.backend.paperUpload.dao;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by lenovo on 2017/4/22.
 */
@Repository
public class ReadImgDocxDao {
    private static final String filePath = "uploadword-web-server\\src\\main\\webapp\\wordUpload\\";
    public void readPicture(List<XWPFPictureData> picList,String str){
        try {
            System.out.println("start....");
            for (XWPFPictureData pic : picList) {
                byte[] bytev = pic.getData();
                FileOutputStream fos = new FileOutputStream(filePath+pic.getFileName());
                fos.write(bytev);
                fos.close();
            }
        } catch (Exception e) {
            System.out.println("exception1....");
            e.printStackTrace();
        }
    }
}
