package com.huatu.tiku.essay.service.upload;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/4
 * @描述
 */
public interface UploadImageService {

    String upLoad(MultipartFile file, String savePath, String saveUrl);

    int uploadVideo(MultipartFile multipartFile, String totalId, String type);

}
