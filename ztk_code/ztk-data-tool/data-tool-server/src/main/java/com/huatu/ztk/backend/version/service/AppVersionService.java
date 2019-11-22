package com.huatu.ztk.backend.version.service;

import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.backend.version.bean.AppVersion;
import com.huatu.ztk.backend.version.bean.ClientType;
import com.huatu.ztk.backend.version.dao.AppVersionDao;
import com.huatu.ztk.backend.version.error.VersionErrors;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by linkang on 11/22/16.
 */

@Service
public class AppVersionService {
    private static final Logger logger = LoggerFactory.getLogger(AppVersionService.class);

    //app升级文件url前缀
    private static final String ANDROID_FILE_BASE_URL = "http://tiku.huatu.com/cdn/apk/";

    //app升级文件的基本保存路径
    private static final String ANDROID_FILE_BASE_BATH = "/var/www/cdn/apk/";

    //全量升级文件后缀
    private static final String FULL_FILE_SUFFIX = ".apk";

    //增量升级文件后缀
    private static final String BULK_FILE_SUFFIX = ".patch";

    @Autowired
    private AppVersionDao appVersionDao;

    @Autowired
    private UploadFileUtil uploadFileUtil;

    /**
     * 查询所有
     *
     * @return
     */
    public List<AppVersion> findAll() {
        return appVersionDao.findAll();
    }

    /**
     * 删除
     *
     * @param id
     */
    public void delete(int id) {
        appVersionDao.deleteById(id);
    }

    /**
     * 新增ios版本
     *
     * @param appVersion
     * @throws BizException
     */
    public void addIosVersion(AppVersion appVersion) throws BizException {
        checkVersion(appVersion.getVersion());

        int versionCount = Integer.valueOf(org.apache.commons.lang.StringUtils.rightPad(appVersion.getVersion().replaceAll("\\.", ""), 3, '0'));
        appVersion.setVersionCount(versionCount);
        appVersion.setCreateTime(new Date());
        appVersionDao.insert(appVersion);
    }

    /**
     * 修改
     *
     * @param appVersion
     * @throws BizException
     */
    public void modify(AppVersion appVersion) throws BizException {
        checkVersion(appVersion.getVersion());
        int versionCount = Integer.valueOf(org.apache.commons.lang.StringUtils.rightPad(appVersion.getVersion().replaceAll("\\.", ""), 3, '0'));
        appVersion.setVersionCount(versionCount);
        appVersionDao.update(appVersion);
    }

    /**
     * 查询某个
     *
     * @param id
     * @return
     */
    public AppVersion findById(int id) {
        return appVersionDao.findById(id);
    }


    /**
     * 版本号检查
     *
     * @param version
     * @throws BizException
     */
    private void checkVersion(String version) throws BizException {
        if (StringUtils.isBlank(version)) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        //不允许2.14这样的版本号
        if (!version.matches("[0-9]{1,2}\\.[0-9](\\.[0-9]){0,1}")) {
            throw new BizException(VersionErrors.VERSION_TYPE_ERROR);
        }
    }

    /**
     * 文件扩展名检查
     * @param file
     * @throws BizException
     */
    private void checkFile(MultipartFile file,String correctSuffix) throws BizException {
        if (file != null) {
            String fullName = file.getOriginalFilename();
            int index = fullName.lastIndexOf(".");
            if (index < 0 || (index >= 0 && !fullName.substring(index).equals(correctSuffix))) {
                throw new BizException(VersionErrors.WRONG_FILE_TYPE);
            }
        }
    }


    /**
     * MD5
     * @param inputStream
     * @return
     * @throws BizException
     */
    private String getMd5(InputStream inputStream) throws BizException {
        String md5 = "";
        try {
            md5 = DigestUtils.md5Hex(inputStream);
        } catch (Exception e) {
            logger.error("ex", e);
            throw new BizException(VersionErrors.MD5_FAIL);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return md5;
    }

    /**
     * 新增安卓版本
     * @param fullFile
     * @param bulkFile
     * @param version
     * @param message
     * @param updateMode
     * @throws Exception
     */
    public void addAndroidVersion(MultipartFile fullFile, MultipartFile bulkFile, String version, String message, int updateMode,int level,int catgory) throws Exception{
        checkVersion(version);
        if (fullFile == null) {
            throw new BizException(VersionErrors.MISSING_FILE);
        }

        checkFile(fullFile, FULL_FILE_SUFFIX);
        checkFile(bulkFile, BULK_FILE_SUFFIX);

        File fullTmp = new File(fullFile.getOriginalFilename());
        fullFile.transferTo(fullTmp);
        String fullUrl = ftpUploadAppFile(fullTmp, version);

        logger.info("full url ={}", fullUrl);

        String bulkUrl = "";
        String md5 = "";
        if (bulkFile != null) {
            File bulkTmp = new File(bulkFile.getOriginalFilename());
            bulkFile.transferTo(bulkTmp);

            File tmpFile = new File("tmp");
            //复制到临时文件
            FileUtils.copyFile(bulkTmp,tmpFile);

            bulkUrl = ftpUploadAppFile(bulkTmp, version);

            //用临时文件计算md5
            md5 = getMd5(new FileInputStream(tmpFile));
            tmpFile.delete();
            logger.info("bulk url={},md5={}",bulkUrl,md5);
        }

        int versionCount;
        versionCount = Integer.valueOf(org.apache.commons.lang.StringUtils.rightPad(version.replaceAll("\\.", ""), 3, '0'));
        AppVersion appVersion = AppVersion.builder()
                .client(ClientType.CLIENT_ANDROID)
                .version(version)
                .message(message)
                .level(level)
                .updateMode(updateMode)
                .full(fullUrl)
                .bulk(bulkUrl)
                .bulkMd5(md5)
                .createTime(new Date())
                .versionCount(versionCount)
                .catgory(catgory)
                .build();
        appVersionDao.insert(appVersion);
    }


    /**
     * 修改安卓版本
     * @param fullFile
     * @param bulkFile
     * @param version
     * @param message
     * @param updateMode
     * @throws Exception
     */
    public void updateAndroidVersion(MultipartFile fullFile, MultipartFile bulkFile, String version, String message, int updateMode,int level,int id,int catgory) throws Exception{
        checkVersion(version);
        checkFile(fullFile, FULL_FILE_SUFFIX);
        checkFile(bulkFile, BULK_FILE_SUFFIX);

        AppVersion old = appVersionDao.findById(id);

        //修改版本号，但是没有上传APK文件
        if (!old.getVersion().equals(version) && fullFile == null) {
            throw new BizException(VersionErrors.VERSION_FILE_NOT_MATCH);
        }

        String fullUrl = old.getFull();
        if (fullFile != null) {
            File fullTmp = new File(fullFile.getOriginalFilename());
            fullFile.transferTo(fullTmp);
            fullUrl = ftpUploadAppFile(fullTmp, version);
        }

        String bulkUrl = old.getBulk();
        String md5 = old.getBulkMd5();
        if (bulkFile != null) {
            File tmpFile = new File("tmp");
            File bulkTmp = new File(bulkFile.getOriginalFilename());
            bulkFile.transferTo(bulkTmp);

            FileUtils.copyFile(bulkTmp,tmpFile);

            bulkUrl = ftpUploadAppFile(bulkTmp, version);

            md5 = getMd5(new FileInputStream(tmpFile));
            tmpFile.delete();
            logger.info("bulk url={},md5={}",bulkUrl,md5);
        }

        int versionCount = Integer.valueOf(org.apache.commons.lang.StringUtils.rightPad(version.replaceAll("\\.", ""), 3, '0'));
        AppVersion appVersion = AppVersion.builder()
                .id(id)
                .client(ClientType.CLIENT_ANDROID)
                .version(version)
                .message(message)
                .level(level)
                .updateMode(updateMode)
                .full(fullUrl)
                .bulk(bulkUrl)
                .bulkMd5(md5)
                .createTime(new Date())
                .versionCount(versionCount)
                .catgory(catgory)
                .build();
        appVersionDao.update(appVersion);
    }


    /**
     * ftp 上传app升级文件
     * @param appFile
     * @return
     * @throws BizException
     */
    public String ftpUploadAppFile(File appFile,String version) throws BizException{
        String fileName = appFile.getName();
        String savePath = ANDROID_FILE_BASE_BATH + version;
        uploadFileUtil.ftpUpload(appFile, fileName, savePath);
        String url = ANDROID_FILE_BASE_URL + version + "/" + fileName;
        return url;
    }
}
