package com.huatu.tiku.banckend.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.banckend.dao.manual.AppVersionMapper;
import com.huatu.tiku.banckend.service.AppVersionService;
import com.huatu.tiku.common.AppVersionEnum;
import com.huatu.tiku.dto.request.AppVersionVo;
import com.huatu.tiku.entity.AppVersion;
import com.huatu.tiku.util.file.FtpClientPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-09-05 下午2:44
 **/
@Service
@Slf4j
public class AppVersionServiceImpl implements AppVersionService{


    private static FtpClientPool ftpClientPool;

    private static ExecutorService threadPool;

    static {
        ftpClientPool = new FtpClientPool();
        threadPool = Executors.newFixedThreadPool(5);
    }

    //app升级文件url前缀
    private static final String ANDROID_FILE_BASE_URL = "http://tiku.huatu.com/cdn/apk/";

    //app升级文件的基本保存路径
    private static final String ANDROID_FILE_BASE_BATH = "/var/www/cdn/apk/";

    //全量升级文件后缀
    private static final String FULL_FILE_SUFFIX = ".apk";

    //增量升级文件后缀
    private static final String BULK_FILE_SUFFIX = ".patch";

    @Autowired
    private AppVersionMapper appVersionMapper;

    @Value("${app.version.ios.update.url}")
    private String iosUpdateUrl;

    /**
     * 新增版本信息
     * @param appName
     * @param terminal
     * @param appVersion
     * @param message
     * @param updateType
     * @param file
     * @param releaseType
     * @param updateChannel
     * @return
     * @throws BizException
     */
    @Override
    public Object saveVersion(Integer appName, Integer terminal, String appVersion, String message, Integer updateType, Integer updateMode, MultipartFile file, Integer releaseType, Integer updateChannel)throws BizException {

        //版本号格式检测
        checkVersion(appVersion);
        int versionCount;
        versionCount = Integer.valueOf(StringUtils.rightPad(appVersion.replaceAll("\\.", ""), 3, '0'));

        /**
         * 如果为安卓设备
         */
        if(terminal == AppVersionEnum.TerminalTypeEnum.ANDROID.getValue()){
            if (file == null) {
                throw new BizException(ErrorResult.create(1315003,"缺少升级文件！"));
            }
            //文件格式检查
            if(updateType == AppVersionEnum.UpdateTypeEnum.FORCE.getValue()){
                checkFile(file, FULL_FILE_SUFFIX);
            }
            if(updateType == AppVersionEnum.UpdateTypeEnum.PATCH.getValue()){
                checkFile(file, BULK_FILE_SUFFIX);
            }

            String fileOrUrl;
            String fileMd5 = "";
            FileOutputStream fileOutputStream = null;
            FileOutputStream md5fileOutputStream = null;
            File fileTemp = null;

            try{
                fileTemp = new File(file.getOriginalFilename());
                fileTemp.createNewFile();
                fileOutputStream = new FileOutputStream(fileTemp);
                fileOutputStream.write(file.getBytes());

                fileOrUrl = generateFtpUploadAppFileUrl(fileTemp, appVersion);

                if(updateType == AppVersionEnum.UpdateTypeEnum.PATCH.getValue()){
                    File md5TempFile = new File("temp");
                    md5TempFile.createNewFile();
                    md5fileOutputStream = new FileOutputStream(md5TempFile);
                    md5fileOutputStream.write(file.getBytes());
                    fileMd5 = getMd5(new FileInputStream(md5TempFile));
                    md5TempFile.delete();
                }

                AppVersion appVersion_ = AppVersion.builder()
                        .appName(appName)
                        .terminal(terminal)
                        .appVersion(appVersion)
                        .message(message)
                        .updateType(updateType)
                        .updateMode(updateMode)
                        .releaseType(releaseType)
                        .fileOrUrl(fileOrUrl)
                        .updateChannel(updateChannel)
                        .fileMd5(fileMd5)
                        .createTime(new Date())
                        .versionCount(versionCount)
                        .build();

                int execute =  appVersionMapper.insertSelective(appVersion_);
                String savePath = ANDROID_FILE_BASE_BATH + appVersion;
                ftpUploadAsync(fileTemp, fileTemp.getName(), savePath, appVersion_.getId());
                if(1 != execute){
                    throw new BizException(ErrorResult.create(1000109, "保存失败！"));
                }else{
                    return SuccessMessage.create("操作成功");
                }

            }catch (Exception e){
                log.error(e.getMessage(), e);
                throw new BizException(ErrorResult.create(1000110, "服务异常！"));
            }finally {
                try{
                    if(null != fileOutputStream){
                        fileOutputStream.close();
                    }
                    if(null != md5fileOutputStream){
                        md5fileOutputStream.close();
                    }
                    //fileTemp.delete();
                }catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }

        }
        /**
         * 如果为ios设备
         */
        else if(terminal == AppVersionEnum.TerminalTypeEnum.IOS.getValue()){
            AppVersion appVersion_ = AppVersion.builder()
                    .appName(appName)
                    .terminal(terminal)
                    .appVersion(appVersion)
                    .versionCount(versionCount)
                    .createTime(new Date())
                    .message(message)
                    .fileOrUrl(iosUpdateUrl)
                    .updateType(updateType)
                    .releaseType(releaseType)
                    .updateMode(updateMode)
                    .build();
            int execute =  appVersionMapper.insertSelective(appVersion_);
            if(1 != execute){
                throw new BizException(ErrorResult.create(1000109, "保存失败！"));
            }else{
                return SuccessMessage.create("操作成功");
            }
        }else{
            throw new BizException(ErrorResult.create(1000104, "设备类型错误"));
        }
    }

    /**
     * 更新ios 安卓灰度发布信息
     * @param id
     * @param releaseType
     * @param updateMode
     * @return
     * @throws BizException
     */
    @Override
    public Object updateVersion(long id,  int releaseType, int updateMode) throws BizException {
        AppVersion appVersion = AppVersion.builder()
                .releaseType(releaseType)
                .id(id)
                .build();
        if(releaseType == AppVersionEnum.ReleaseTypeEnum.MODE.getValue() && 0 == updateMode){
            throw new BizException(ErrorResult.create(1000101, "非法的参数！"));
        }else{
            appVersion.setUpdateMode(updateMode);
        }
        return appVersionMapper.updateByPrimaryKeySelective(appVersion);
    }

    /**
     * 根据id获取
     * @param id
     * @return
     * @throws BizException
     */
    @Override
    public AppVersion getById(Long id) throws BizException{
        return appVersionMapper.selectByPrimaryKey(id);
    }

    /**
     * 逻辑删除
     * @param id
     * @return
     * @throws BizException
     */
    @Override
    public Object deleteByLogic(Long id) throws BizException{
        AppVersion appVersion = AppVersion.builder()
                .status(0)
                .build();
        Example example = new Example(AppVersion.class);
        example.and().andEqualTo("id", id);
        appVersionMapper.updateByExampleSelective(appVersion, example);
        return SuccessMessage.create("操作成功");
    }

    /**
     * 列表信息
     * @param page
     * @param size
     * @return
     */
    @Override
    public Object list(int page, int size) throws BizException{

        Example example = new Example(AppVersion.class);
        example.orderBy("createTime").desc();

        PageInfo pageInfo = PageHelper.startPage(page, size).doSelectPageInfo(()-> appVersionMapper.selectByExample(example));
        List<AppVersionVo> list = Lists.newArrayList();
        pageInfo.getList().forEach(item->{
            AppVersion appVersion = (AppVersion) item;
            AppVersionVo vo = AppVersionVo.builder().build();
            BeanUtils.copyProperties(appVersion, vo);
            String releaseType = AppVersionEnum.ReleaseTypeEnum.getTitle(appVersion.getReleaseType());
            vo.setAppName(AppVersionEnum.AppNameEnum.getTitle(appVersion.getAppName()));
            vo.setTerminal(AppVersionEnum.TerminalTypeEnum.getTitle(appVersion.getTerminal()));
            vo.setUpdateType(AppVersionEnum.UpdateTypeEnum.getTitle(appVersion.getUpdateType()));
            vo.setUpdateChannel(AppVersionEnum.UpdateChannelEnum.getTitle(appVersion.getUpdateChannel()));
            if(appVersion.getReleaseType() == AppVersionEnum.ReleaseTypeEnum.MODE.getValue()){

                String modeValue = AppVersionEnum.UpdateMode.getTitle(appVersion.getUpdateMode());
                vo.setReleaseType(releaseType + ":" + modeValue);
            }else{
                vo.setReleaseType(releaseType);
            }
            list.add(vo);

        });
        pageInfo.setList(list);
        return pageInfo;
    }


    /**
     * 上传失败更新message
     * @param id
     */
    public void updateVersionMessage(long id){
        AppVersion appVersion = AppVersion.builder()
                .id(id)
                .message("文件上传失败，请稍后重试！")
                .status(0)
                .build();
        appVersionMapper.updateByPrimaryKeySelective(appVersion);
    }

    /**
     * 版本号检查
     *
     * @param version
     * @throws BizException
     */
    private void checkVersion(String version) throws BizException {
        if (StringUtils.isBlank(version)) {
            throw new BizException(ErrorResult.create(1000101, "非法的参数！"));
        }

        //不允许2.14这样的版本号
        if (!version.matches("^[0-9]{1,2}\\.[0-9]{1,2}(\\.[0-9]{1,3})?$")) {
            throw new BizException(ErrorResult.create(1315001, "版本号错误！"));
        }
    }


    /**
     * 文件扩展名检查
     * @param file
     */
    private void checkFile(MultipartFile file,String correctSuffix) throws BizException {
        if (file != null) {
            String fullName = file.getOriginalFilename();
            int index = fullName.lastIndexOf(".");
            if (index < 0 || (index >= 0 && !fullName.substring(index).equals(correctSuffix))) {
                throw new BizException(ErrorResult.create(1315004, "文件格式错误！"));
            }
        }
    }

    /**
     * ftp 上传app升级文件
     * @param appFile
     * @return
     * @throws BizException
     */
    public String generateFtpUploadAppFileUrl(File appFile,String version) throws BizException {
        String fileName = appFile.getName();
        String url = ANDROID_FILE_BASE_URL + version + "/" + fileName;
        return url;
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
            log.error("ex", e);
            throw new BizException(ErrorResult.create(1315002, "无法计算MD5值"));
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
     * ftp上传文件
     *
     * @param file     file对象
     * @param fileName 文件名称
     * @param savePath 保存路径
     * @return //     * @throws BizException
     */
    public void ftpUploadAsync(File file, String fileName, String savePath, long id){

        final FTPClient ftpClient = ftpClientPool.getFTPClient();

        Future<Result> future = threadPool.submit(() -> {
            FileInputStream fis = null;
            try {
                log.info("file upload starting ..");
                ftpClient.makeDirectory(savePath);
                ftpClient.changeWorkingDirectory(savePath);
                fis = new FileInputStream(file);
                ftpClient.storeFile(fileName, fis);
                log.info("file upload finished...");
            } catch (IOException e) {
                log.error("upload file failed!", e);
                updateVersionMessage(id);
                return Result.FAIL;
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                        file.delete();
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                ftpClientPool.returnFTPClient(ftpClient);
            }
            return Result.SUCCESS;
        });
    }


    public enum Result{
        SUCCESS(1, "SUCCESS"),
        FAIL(-1, "FAIL");
        private Integer value;
        private String title;

        Result(Integer value, String title) {
            this.value = value;
            this.title = title;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
