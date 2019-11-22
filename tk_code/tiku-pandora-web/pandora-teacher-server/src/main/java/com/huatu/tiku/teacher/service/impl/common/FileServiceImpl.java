package com.huatu.tiku.teacher.service.impl.common;

import com.huatu.tiku.request.FileRequest;
import com.huatu.tiku.response.file.FileResp;
import com.huatu.tiku.teacher.service.common.FileService;
import com.huatu.tiku.util.file.FormulaUtil;
import com.huatu.tiku.util.file.HtmlFileUtil;
import com.huatu.tiku.util.file.ImgInfoVO;
import com.huatu.tiku.util.file.UploadFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.huatu.tiku.constant.BaseConstant.IMG_PATH;
import static com.huatu.tiku.constant.BaseConstant.IMG_URL;

/**
 * Created by x6 on 2018/5/9.
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService{

    @Autowired
    HtmlFileUtil htmlFileUtil;

    private String imgLabel = "<img src=\"%s\" width=\"%s\" height=\"%s\" style=\"width:%s;height:%s\">";

    @Override
    public FileResp upload(MultipartFile file) {
        InputStream inputStream = null;
        try {
            if(null == file){
                log.warn("图片读取异常");
            }
            inputStream = file.getInputStream();
            //获取文件后缀名
            String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            //生成新文件名称
            String fileName = UUID.randomUUID().toString() + "."+suffix;
            UploadFileUtil.getInstance().ftpUploadFileInputStream(inputStream,fileName,IMG_PATH);
            FileResp fileResp = new FileResp();
            fileResp.setUrl(IMG_URL+fileName);
            return fileResp ;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=inputStream){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public Object save(FileRequest file) {
        String content = file.getContent();
        try {
            content = htmlFileUtil.imgManage(content,"2333",0);
            content = htmlFileUtil.htmlManage(content);
            content = content.replace("&nbsp;","");
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileResp fileResp = FileResp.builder()
                .content(content)
                .build();
        return fileResp;
    }

    @Override
    public String latex2ImgLabel(String latex,float height ,float width){
        String imgPath = UUID.randomUUID().toString().replace("-","") + ".png";
        String imageUrl = IMG_URL+imgPath;
        System.out.println("imgPath:"+imgPath);
        ImgInfoVO formulaImgInfoVO = FormulaUtil.latex2Png(latex,200);
        if(null != formulaImgInfoVO){
            String base = formulaImgInfoVO.getBase();

            if (htmlFileUtil.GenerateImage(base, imgPath)) {
                FileInputStream fileInputStream = null;
                File file = new File(imgPath);
                //图片上传
                try {
                    fileInputStream = new FileInputStream(file);
                    UploadFileUtil.getInstance().ftpUploadFileInputStream(fileInputStream,imgPath,IMG_PATH);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }finally {
                    if(null != fileInputStream){
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //删除本地文件
                    if(null != file){
                        file.delete();
                    }
                }
            }

            return String.format(imgLabel, imageUrl, width, height, width, height);
        }
        return "";
    }


}
