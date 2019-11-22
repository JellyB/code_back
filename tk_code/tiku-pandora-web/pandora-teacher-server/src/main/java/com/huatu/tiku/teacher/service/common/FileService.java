package com.huatu.tiku.teacher.service.common;

import com.huatu.tiku.request.FileRequest;
import com.huatu.tiku.response.file.FileResp;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件相关
 * Created by x6 on 2018/5/9.
 */
public interface FileService {

    /**
     * 图片上传
     * TODO 待验证
     * @param file
     * @return
     */
    FileResp upload(MultipartFile file);

    /**
     * 文件内容解析
     * TODO 待验证
     * @param file
     * @return
     */
    Object save(FileRequest file);

    String latex2ImgLabel(String formula,float height,float width);
}
