package com.huatu.ztk.backend.version.controller;

import com.huatu.ztk.backend.version.bean.AppVersion;
import com.huatu.ztk.backend.version.service.AppVersionService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by linkang on 11/22/16.
 */

@RestController
@RequestMapping(value = "versions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AppVersionController {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionController.class);

    @Autowired
    private AppVersionService appVersionService;

    /**
     * 列表
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public Object getList() {
        return appVersionService.findAll();
    }

    /**
     * 删除
     *
     * @param id
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public void del(@RequestParam int id) {
        logger.info("del version id={}", id);
        appVersionService.delete(id);
    }


    /**
     * 新增ios版本
     *
     * @param appVersion
     */
    @RequestMapping(value = "/ios", method = RequestMethod.POST)
    public void add(@RequestBody AppVersion appVersion) throws BizException {
        logger.info("add ios version json={}", JsonUtil.toJson(appVersion));
        appVersionService.addIosVersion(appVersion);
    }

    /**
     * 修改
     *
     * @param appVersion
     */
    @RequestMapping(value = "/ios/modify", method = RequestMethod.PUT)
    public void modify(@RequestBody AppVersion appVersion) throws BizException {
        logger.info("modify version json={}", JsonUtil.toJson(appVersion));
        appVersionService.modify(appVersion);
    }


    /**
     * 查询
     *
     * @param id
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public AppVersion find(@PathVariable int id) {
        return appVersionService.findById(id);
    }


    /**
     * 新增安卓版本
     *
     * @return
     */
    @RequestMapping(value = "/android", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void upload(@RequestParam(value = "fullFile") MultipartFile fullFile,
                       @RequestParam(value = "bulkFile", required = false) MultipartFile bulkFile,
                       @RequestParam(defaultValue = "") String version,
                       @RequestParam(defaultValue = "") String message,
                       @RequestParam(defaultValue = "1") int level,
                       @RequestParam(defaultValue = "1") int updateMode,
                       @RequestParam int catgory) throws Exception {
        //转码
        message = new String(message.getBytes("iso-8859-1"), "utf-8");
        appVersionService.addAndroidVersion(fullFile, bulkFile, version, message, updateMode, level, catgory);
    }


    /**
     * 修改安卓版本
     *
     * @return
     */
    @RequestMapping(value = "/android/modify", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void upload2(@RequestParam(value = "fullFile", required = false) MultipartFile fullFile,
                        @RequestParam(value = "bulkFile", required = false) MultipartFile bulkFile,
                        @RequestParam(defaultValue = "") String version,
                        @RequestParam(defaultValue = "") String message,
                        @RequestParam(defaultValue = "1") int level,
                        @RequestParam(defaultValue = "1") int updateMode,
                        @RequestParam int id,
                        @RequestParam int catgory) throws Exception {
        //转码
        message = new String(message.getBytes("iso-8859-1"), "utf-8");
        appVersionService.updateAndroidVersion(fullFile, bulkFile, version, message, updateMode, level,id,catgory);
    }
}
