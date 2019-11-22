package com.huatu.ztk.backend.paperUpload.dao;

import java.io.*;
import java.util.*;


import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.springframework.stereotype.Repository;

/**
 * Created by lenovo on 2017/4/22.
 */
@Repository
public class ReadImgDocDao {
    private static final String filePath = "uploadword-web-server\\src\\main\\webapp\\wordUpload\\";
    public void readPicture(File file) throws Exception {
        FileInputStream in = new FileInputStream(file);
        HWPFDocument doc = new HWPFDocument(in);
        int length = doc.characterLength();
        PicturesTable pTable = doc.getPicturesTable();
        for (int i = 0; i < length; i++) {
            Range range = new Range(i, i + 1, doc);

            CharacterRun cr = range.getCharacterRun(0);
            if (pTable.hasPicture(cr)) {
                Picture pic = pTable.extractPicture(cr, false);
                String afileName = pic.suggestFullFileName();
                OutputStream out = new FileOutputStream(new File("filePath" + UUID.randomUUID() + afileName));
                pic.writeImageContent(out);


            }
        }
    }
}