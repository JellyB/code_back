package com.huatu.ztk.user.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.common.RegexConfig;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.utils.FtpClientPool;
import com.huatu.ztk.user.utils.etag.Etag;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内部工具
 * Created by shaojieyue
 * Created time 2016-10-31 13:35
 */

@RestController
@RequestMapping(value = "/inner")
@Deprecated
public class InnerController {
    private static final Logger logger = LoggerFactory.getLogger(InnerController.class);

    @Autowired
    private FtpClientPool ftpClientPool;


    @Autowired
    private UserDao userDao;

    //图片文件的基本保存路径
    public static final String IMG_FILE_BASE_BATH = "/var/www/cdn/images/vhuatu/tiku/";

    //图片url前缀
    public static final String IMG_BASE_URL = "http://tiku.huatu.com/cdn/images/vhuatu/tiku/";



    /**
     * 上传图片到服务器,不压缩
     * @param request
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/tiku/img", method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object uploadAvatar(HttpServletRequest request) throws BizException {
        String remoteAddr = request.getRemoteAddr();
        String url1 = request.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url1, remoteAddr);
        String url = "";
        //对应关系，保持文件顺序
        LinkedHashMap map = new LinkedHashMap();
        try {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            if (!ServletFileUpload.isMultipartContent(request)) {
                throw new BizException(UserErrors.UPLOAD_AVATAR_FAIL);
            }
            //如果超过设置的最大值，将抛出异常
            List<FileItem> fileItems = upload.parseRequest(request);

            for (FileItem fileItem : fileItems) {
                //不是文件
                if (fileItem.isFormField()) {
                    continue;
                }

                String contentType = fileItem.getContentType();
                //使用MIME类型判断文件类型
                if (!contentType.equals(MediaType.IMAGE_JPEG_VALUE)
                        && !contentType.equals(MediaType.IMAGE_GIF_VALUE)
                        && !contentType.equals(MediaType.IMAGE_PNG_VALUE)) {
                    throw new BizException(UserErrors.IMG_TYPE_NOT_SUPPORT);
                }

                //原始文件名
                String originName = fileItem.getName();

                //ftp上传到服务器
                url = ftpUpload(fileItem,originName);
                //删除临时文件
                fileItem.delete();

                map.put(originName, url);
            }
        } catch (Exception e) {
            logger.error("ex", e);
            throw new BizException(UserErrors.UPLOAD_AVATAR_FAIL);
        }
        return map;
    }

    /**
     * ftp上传图片
     * @param fileItem
     * @param originName 原始文件名
     * @return
     * @throws BizException
     */
    private String ftpUpload(FileItem fileItem, String originName) throws BizException {
        String url = "";
        //从连接池获取ftp 客户端
        final FTPClient ftpClient = ftpClientPool.getFTPClient();
        try {
            //保持文件后缀名
            String extName = originName.substring(originName.lastIndexOf("."));
            //生成文件名
            String fileNameTmp = Etag.stream(fileItem.getInputStream(), fileItem.getInputStream().available()) + extName;
            String fileName = fileNameTmp.substring(1);

            //文件保存路径
            String savePath = makeSavePath(fileName);
            //目录不存在，创建目录
            ftpClient.makeDirectory(savePath);
            //切换工作目录
            ftpClient.changeWorkingDirectory(savePath);

            InputStream inputStream = fileItem.getInputStream();

            //ftp上传服务器
            ftpClient.storeFile(fileName,inputStream);

            url = makeUrl(fileName);
            inputStream.close();
        } catch (IOException e) {
            logger.error("ex", e);
            throw new BizException(UserErrors.UPLOAD_AVATAR_FAIL);
        } finally {
            //回收到连接池
            ftpClientPool.returnFTPClient(ftpClient);
        }
        return url;
    }

    /**
     * 简单组装图片url
     *
     * @param fileName
     * @return
     */
    private String makeUrl(String fileName) {
        return IMG_BASE_URL + fileName.charAt(0) + "/" + fileName;
    }

    /**
     * 组装文件路径
     *
     * @param fileName
     * @return
     */
    private String makeSavePath(String fileName) {
        return IMG_FILE_BASE_BATH + fileName.charAt(0);
    }


    /**
     * 内部使用-用手机号查询username
     * @param phone
     * @return
     */
    @RequestMapping(value = "username",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object queryUser(@RequestParam String phone,HttpServletRequest httpServletRequest) throws Exception{
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String url = httpServletRequest.getRequestURL().toString();
        logger.info("tool interface:----{}-------host:---{}", url, remoteAddr);
        if (!phone.matches(RegexConfig.MOBILE_PHONE_REGEX)) {
            throw new BizException(UserErrors.ILLEGAL_MOBILE);
        }

        UserDto userDto = userDao.findAny(phone);

        if (userDto == null) {
            throw new BizException(UserErrors.USER_NOT_EXIST);
        }

        Map map = new HashMap();
        map.put("username", userDto.getName());

        return map;
    }
}
