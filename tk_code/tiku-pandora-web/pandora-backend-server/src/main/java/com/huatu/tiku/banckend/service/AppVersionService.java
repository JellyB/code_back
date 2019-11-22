package com.huatu.tiku.banckend.service;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.AppVersion;
import org.springframework.web.multipart.MultipartFile;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-09-05 下午2:37
 **/
public interface AppVersionService {

    /**保存版本号
     * @param appName
     * @param terminal
     * @param appVersion
     * @param message
     * @param updateType
     * @param updateMode
     * @param file
     * @param releaseType
     * @param updateChannel
     * @return
     */
    Object saveVersion(Integer appName, Integer terminal, String appVersion, String message, Integer updateType, Integer updateMode, MultipartFile file, Integer releaseType, Integer updateChannel) throws BizException;

    /**
     * 修改版本号灰度发布
     * @param id
     * @param releaseType
     * @param updateMode
     * @return
     * @throws BizException
     */
    Object updateVersion(long id, int releaseType, int updateMode) throws BizException;

    /**
     * 根据主键获取
     * @param id
     * @return
     */
    AppVersion getById(Long id);

    /**
     * 逻辑删除
     * @param id
     * @return
     */
    Object deleteByLogic(Long id);

    /**
     * 查询列表
     * @return
     */
    Object list(int page, int size);

}
