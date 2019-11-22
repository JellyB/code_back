package com.huatu.tiku.interview.controller.admin.v1;

import com.huatu.tiku.interview.constant.BasicParameters;
import com.huatu.tiku.interview.constant.WeChatUrlConstant;
import com.huatu.tiku.interview.entity.dto.CourseArr;
import com.huatu.tiku.interview.entity.po.NotificationType;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.vo.ImageVo;
import com.huatu.tiku.interview.service.OnlineCourseArrangementService;
import com.huatu.tiku.interview.util.LogPrint;
import com.huatu.tiku.interview.util.file.FileUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.util.fs.FileUtils;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialUploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 线上课程安排
 * @Author jbzm
 * @Date Create on 2018/1/17 17:21
 */
@RestController
@Slf4j
@RequestMapping(value = "/end/oca", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class OnlineCourseArrangementController {

    @Autowired
    private OnlineCourseArrangementService arrangementService;

    @Autowired
    private FileUtil fileUtil;
    @Autowired
    private RedisTemplate redisTemplate;

    @LogPrint
    @PostMapping("/CourseArrangement")
    public Result add(@RequestBody CourseArr courseArr) throws Exception {
        NotificationType notificationType = new NotificationType();
        notificationType.setCreator("admin");

        notificationType.setWxImageId(courseArr.getMediaId());
        notificationType.setImageUrl(courseArr.getFileUrl());
        notificationType.setTitle(courseArr.getTitle());
        notificationType.setId(courseArr.getId());
        notificationType.setClassId(courseArr.getClassId());
        arrangementService.add(notificationType);
        return Result.ok(notificationType);
    }
    @LogPrint
    @PostMapping("/uploadImage")
    public Result uploadImage(@RequestParam("file") MultipartFile file) throws IOException, WxErrorException {
        WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();

        // 设置微信公众号的appid
        config.setAppId(BasicParameters.appID);
        // 设置微信公众号的app corpSecret
        config.setSecret(BasicParameters.appsecret);
        config.setAccessToken((String) redisTemplate.opsForValue().get(WeChatUrlConstant.ACCESS_TOKEN));
        log.debug("获取accesstoken:" + config.getAccessToken());
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(config);
        String fileUrl = fileUtil.ftpUploadArrangement(file);
        log.debug("文件地址:" + fileUrl);

        //转换为文件
        File tempFile = FileUtils.createTmpFile(file.getInputStream(), UUID.randomUUID().toString(), file.getContentType().split("/")[1]);
        WxMpMaterial wxMpMaterial = new WxMpMaterial();
        wxMpMaterial.setFile(tempFile);
        wxMpMaterial.setName(WxConsts.MediaFileType.IMAGE);
        //通过微信服务器获取图片官方id
        WxMpMaterialUploadResult res = wxMpService.getMaterialService().materialFileUpload(WxConsts.MediaFileType.IMAGE, wxMpMaterial);
        log.debug("获取官方imageId:" + res.getMediaId());
        ImageVo imageVo = new ImageVo();
        imageVo.setFileUrl(fileUrl);
        imageVo.setMediaId(res.getMediaId());
        return Result.ok(imageVo);
    }

    @LogPrint
    @GetMapping("/CourseArrangement")
    public Result findById(@RequestParam Long id) {
        return Result.ok(arrangementService.findById(id));
    }
}
