package com.huatu.tiku.banckend.controller;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.banckend.service.AppVersionService;
import com.huatu.tiku.dto.request.AppVersionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-09-05 下午2:36
 **/
@Slf4j
@RestController
@RequestMapping(value = "versions")
public class AppVersionController {

    @Autowired
    private AppVersionService appVersionService;


    /**
     * app version 列表
     * @param page
     * @param size
     * @return
     */
    @GetMapping
    public Object list(@RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "size", defaultValue = "20") int size) throws BizException {
        return appVersionService.list(page, size);
    }

    /**
     * 新增app 版本
     * @param appName
     * @param terminal
     * @param appVersion
     * @param message
     * @param updateType
     * @param updateMode
     * @param releaseType
     * @param file
     * @param updateChannel
     * @return
     * @throws BizException
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object save(@RequestParam(value = "appName") Integer appName,
                       @RequestParam(value = "terminal") Integer terminal,
                       @RequestParam(value = "appVersion") String appVersion,
                       @RequestParam(value = "message") String message,
                       @RequestParam(value = "updateType") int updateType,
                       @RequestParam(value = "updateMode", required = false, defaultValue = "0") int updateMode,
                       @RequestParam(value = "releaseType") int releaseType,
                       @RequestParam(value = "file", required = false) MultipartFile file,
                       @RequestParam(value = "updateChannel", required = false, defaultValue = "0") int updateChannel) throws BizException{
        return appVersionService.saveVersion(appName, terminal, appVersion, message, updateType, updateMode, file, releaseType, updateChannel);
    }


    /**
     * 修改app 版本信息
     * @param appVersion
     * @return
     * @throws BizException
     */
    @PutMapping
    public Object update(@RequestBody HashMap<String,Object> appVersion) throws BizException{
        AppVersionDto appVersionDto = AppVersionDto.builder().build();
        transMap2Bean(appVersion, appVersionDto);
        if(null == appVersionDto.getUpdateMode()){
            appVersionDto.setUpdateMode(0);
        }
        return appVersionService.updateVersion(appVersionDto.getId(), appVersionDto.getReleaseType(), appVersionDto.getUpdateMode());
    }

    /**
     *
     *
     * 根据id获取信息
     * @param id
     * @return
     */
    @GetMapping(value = "/{id}")
    public Object getById(@PathVariable("id") long id) throws BizException {
        return appVersionService.getById(id);
    }


    /**
     * 根据id逻辑删除
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Object deleteByLogic(@PathVariable("id") long id) throws BizException {
        return appVersionService.deleteByLogic(id);
    }

    /**
     * 转换map 到bean
     * @param map
     * @param obj
     */
    public static void transMap2Bean(Map<String,Object> map, Object obj) {

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                if (map.containsKey(key)) {
                    Object value = map.get(key);
                    // 得到property对应的setter方法
                    Method setter = property.getWriteMethod();
                    setter.invoke(obj, value);
                }
            }
        } catch (Exception e) {
            log.error("transfer error!");
        }
    }
}
