package com.huatu.tiku.essay.web.controller.admin.v1;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.service.upload.UploadImageService;
import com.huatu.tiku.essay.util.video.BjyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

import static com.huatu.tiku.essay.util.file.FunFileUtils.*;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/4
 * @描述
 */
@RestController
@Slf4j
@RequestMapping("/end/v1/upload")
public class EssayUploadController {


    @Autowired
    private UploadImageService uploadImageService;
    @Autowired
    private BjyHandler bjyHandler;

    /**
     * 上传批注图片
     *
     * @return 生成的图片url
     * @throws BizException
     */
    @PostMapping("image")
    @ResponseBody
    public Object uploadImage(@RequestParam("file") MultipartFile file) throws BizException {
        String url = uploadImageService.upLoad(file, MANUAL_CORRECT_SAVE_PATH, MANUAL_CORRECT_SAVE_URL);
        HashMap map = new HashMap();
        map.put("url", url);
        return map;
    }


    /**
     * 上传音频(名师之声)
     *
     * @param answerId 批注ID
     * @param type     0 单题 1 套题
     * @return 生成的url
     * @throws BizException
     */
    @PostMapping("audio")
    @ResponseBody
    public Object uploadAudio(@RequestParam("file") MultipartFile file, long answerId, int type) throws BizException {
        int audioId = uploadImageService.uploadVideo(file, String.valueOf(answerId), String.valueOf(type));
        HashMap map = new HashMap();
        map.put("audioId", audioId);
        return map;
    }

    /**
     * 根据音频ID获取视频url
     *
     * @param audioId 音频Id
     */
    @GetMapping("audioInfo")
    public Object getAudioInfo(@RequestParam Integer audioId) throws BizException {
        String audioUrl = bjyHandler.getVideoUrl(audioId);
        HashMap map = new HashMap();
        map.put("audioUrl", audioUrl);
        return map;
    }


}
