package com.huatu.tiku.essay.service.upload.impl;

import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.correct.EssayPaperLabelTotal;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.repository.v2.EssayPaperLabelTotalRepository;
import com.huatu.tiku.essay.service.EssayLabelService;
import com.huatu.tiku.essay.service.upload.UploadImageService;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.util.video.pojo.VideoUploadUrl;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;


/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/4
 * @描述 上传图片
 */

@Service
public class UploadImageServiceImpl implements UploadImageService {

    private static final Logger logger = LoggerFactory.getLogger(UploadImageServiceImpl.class);

    @Autowired
    UploadFileUtil uploadFileUtil;
    @Autowired
    BjyHandler bjyHandler;
    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;
    @Autowired
    EssayPaperLabelTotalRepository essayPaperLabelTotalRepository;


    /**
     * 上传文件
     *
     * @param file
     * @param savePath
     * @param saveUrl
     * @return
     */
    @Override
    public String upLoad(MultipartFile file, String savePath, String saveUrl) {
        if (file.isEmpty()) {
            return "上传的文件不存在!";
        }
        String[] split = file.getContentType().split("/");
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") +"."+ split[1];
        try {
            //上传
            InputStream inputStream = file.getInputStream();
            uploadFileUtil.ftpUploadFileInputStream(inputStream, fileName, savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saveUrl + fileName;
    }

    /**
     * 上传名师之声音频
     *
     * @param multipartFile
     * @return
     */
    @Override
    public int uploadVideo(MultipartFile multipartFile, String answerId, String type) {
        String fileName = multipartFile.getOriginalFilename();
        VideoUploadUrl uploadUrl = bjyHandler.getVideoUploadUrl(answerId + type);
        File file = null;
        try {
            file = File.createTempFile("shenlun", fileName);
            IOUtils.copy(multipartFile.getInputStream(), new FileOutputStream(file));
            bjyHandler.uploadVideo(uploadUrl.getUpload_url(), new FileSystemResource(file));
            file.deleteOnExit();
            return uploadUrl.getVideo_id();
        } catch (IOException e) {
            logger.info("上传失败,totalId是:{},type是:{}", answerId);
            e.printStackTrace();
        }
        return 0;
    }


}
