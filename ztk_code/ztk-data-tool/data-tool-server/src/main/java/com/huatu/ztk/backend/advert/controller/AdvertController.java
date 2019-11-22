package com.huatu.ztk.backend.advert.controller;

import com.huatu.ztk.backend.advert.bean.Advert;
import com.huatu.ztk.backend.advert.common.error.AdvertErrors;
import com.huatu.ztk.backend.advert.service.AdvertService;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by renwenlong on 2016/11/16.
 */
@RestController
@RequestMapping(value = "/adverts")
public class AdvertController {
    private static final Logger logger = LoggerFactory.getLogger(AdvertController.class);

    @Autowired
    private AdvertService advertService;

    /**
     * 获取所有广告列表
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Object getAllAds() {
        List<Advert> adverts = advertService.getAllAds();
        return adverts;
    }

    /**
     * 根据科目和广告类型获取广告列表
     *
     * @param catgory
     * @return
     */
    @RequestMapping(value = "/list/", method = RequestMethod.GET)
    public Object getAdsBycatgory(@RequestParam int catgory,
                                  @RequestParam int type) {
        List<Advert> adverts = advertService.getAdsByCatgoryAndType(catgory, type);
        if (CollectionUtils.isEmpty(adverts)) {
            logger.info("no any advert exist for catgory={} and type={}", catgory, type);
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        return adverts;
    }

    /**
     * 保存数据
     *
     * @return
     */
    @RequestMapping(value = "/advert", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object upload(@RequestParam(value = "file", required = false) MultipartFile file,
                         @RequestParam String target,
                         @RequestParam String title,
                         @RequestParam(defaultValue = "") String params,
                         @RequestParam int catgory,
                         @RequestParam int type,
                         @RequestParam(defaultValue = "0") int index,
                         @RequestParam(defaultValue = "0",required = false)Long onlineTime,
                         @RequestParam(defaultValue = "0",required = false)Long offlineTime,
                         @RequestParam(defaultValue = "0") int newVersion,
                         @RequestParam(defaultValue = "0") int appType,
                         @RequestParam(defaultValue = "0") int position,
                         @RequestParam(defaultValue = "0") long courseCollectionId) throws BizException, IOException {
        if (file != null) {
            //获取文件数据
            final File dest = new File(file.getOriginalFilename());
            file.transferTo(dest);
            advertService.upload(target, title, params, catgory, type, dest, newVersion, position, appType, onlineTime, offlineTime,index,courseCollectionId);
        } else {
            logger.info("未添加图片");
            return AdvertErrors.NO_IMAGE_ADDED;
        }
        return SuccessMessage.create("保存成功");
    }

    /**
     * 更新广告信息
     *
     * @param file
     * @param target
     * @param title
     * @param params
     * @return
     * @throws BizException
     * @throws IOException
     */
    @RequestMapping(value = "/advert/", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object update(@RequestParam(value = "file", required = false) MultipartFile file,
                         @RequestParam long id,
                         @RequestParam String target,
                         @RequestParam String title,
                         @RequestParam int catgory,
                         @RequestParam int type,
                         @RequestParam(defaultValue = "0") int index,
                         @RequestParam(defaultValue = "0") long onlineTime,
                         @RequestParam(defaultValue = "0") long offlineTime,
                         @RequestParam(defaultValue = "") String params,
                         @RequestParam(defaultValue = "0") int appType,
                         @RequestParam(defaultValue = "0") int newVersion,
                         @RequestParam(defaultValue = "0") int position,
                         @RequestParam(defaultValue = "0") long courseCollectionId) throws BizException, IOException {
        advertService.update(target, title, catgory, type,
                params, file, id, newVersion, position, appType, onlineTime, offlineTime,index,courseCollectionId);
        return SuccessMessage.create("修改成功");
    }

    /**
     * 修改广告的展示状态
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/advert/status", method = RequestMethod.GET)
    public Object modStatus(@RequestParam long id) {
        advertService.modStatusById(id);
        return SuccessMessage.create("状态修改成功");
    }

    /**
     * 删除广告
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/advert/{id}", method = RequestMethod.DELETE)
    public Object delete(@PathVariable long id) {
        advertService.deleteById(id);
        return SuccessMessage.create("删除成功");
    }

    /**
     * 上线广告
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/advert/gogogo/{id}", method = RequestMethod.GET)
    public Object gogogo(@PathVariable long id) {
        advertService.gogogoById(id);
        return SuccessMessage.create("上线成功");
    }

    /**
     * 根据id获取对应广告的信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/advert/{id}", method = RequestMethod.GET)
    public Object getAdvertById(@PathVariable long id) {
        Advert advert = advertService.getAdvertById(id);
        return advert;
    }

    private String getFormatString(String str) throws UnsupportedEncodingException {
        return new String(str.getBytes("iso-8859-1"), "utf-8");
    }

}
