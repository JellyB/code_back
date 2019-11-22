package com.huatu.tiku.essay.service.task;

import com.huatu.tiku.essay.entity.EssayPhotoAnswer;
import com.huatu.tiku.essay.entity.EssayRewardRecord;
import com.huatu.tiku.essay.repository.EssayPhotoAnswerRepository;
import com.huatu.tiku.essay.repository.EssayRewardRecordRepository;
import com.huatu.tiku.essay.util.file.FunFileUtils;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import com.huatu.tiku.essay.vo.admin.UserCorrectGoodsRewardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author zhaoxi
 * @Description: 异步任务保存拍照答题图片和识别结果
 * @create 2017-12-27 下午1:22
 **/
@Slf4j
@Component
public class AsyncFileSaveServiceImpl {

    @Autowired
    private UploadFileUtil uploadFileUtil;
    @Autowired
    private EssayPhotoAnswerRepository essayPhotoAnswerRepository;
    @Autowired
    EssayRewardRecordRepository essayRewardRecordRepository;



    /**
     * 异步任务保存拍照答题图片
     * @param file
     * @param content
     * @param userId
     */
    @Async
    public void savePhotoAndAnswerToMysql(MultipartFile file, String content,int userId,int terminal) {

        String url = uploadFile(file);
        EssayPhotoAnswer photoAnswer = EssayPhotoAnswer.builder()
                .url(url)
                .userId(userId)
                .terminal(terminal)
                .content(content)
                .build();
        essayPhotoAnswerRepository.save(photoAnswer);
    }


    private String uploadFile(MultipartFile file){
        String originalFilename = file.getOriginalFilename();
        //读取文件后缀
        int indexOf = originalFilename.lastIndexOf(".");
        String suffix = originalFilename.substring(indexOf, originalFilename.length());
        String fileName = UUID.randomUUID().toString().replaceAll("-", "")+suffix;

        try {
            InputStream inputStream = file.getInputStream();
            uploadFileUtil.ftpUploadFileInputStream(inputStream,fileName, FunFileUtils.PICTURE_SAVE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
