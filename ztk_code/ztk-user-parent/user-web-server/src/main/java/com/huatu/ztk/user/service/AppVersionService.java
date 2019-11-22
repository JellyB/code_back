package com.huatu.ztk.user.service;

import com.huatu.tiku.common.AppVersionEnum;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.user.bean.AppVersionBean;
import com.huatu.ztk.user.daoPandora.AppVersionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-09-06 下午7:31
 **/
@Service
@Slf4j
public class AppVersionService {

    @Autowired
    private AppVersionMapper appVersionMapper;

    @Autowired
    private UserServerConfig userServerConfig;

//    @Value("${app.version.update.white.list}")
//    private String whiteList;

    public AppVersionBean findNewVersion(long userId, int terminal, String currentVersion) {
        // log.info(" white list => {}",userServerConfig.getUpdateWhiteList());

        //log.info("userId:{},terminal:{},currentVersion:{}", userId, terminal, currentVersion);
        //log.info("terminal:{}", terminal);
        //log.info("currentVersion:{}", currentVersion);
        //版本号的数字 版本号格式为x.x.x 如果不够,则用 0补齐
        int versionCount = Integer.valueOf(org.apache.commons.lang.StringUtils.rightPad(currentVersion.replaceAll("\\.", ""), 3, '0'));
        /**
         * 数据库最新版本
         */
        final com.huatu.tiku.entity.AppVersion latestVersion = getLatestVersion(terminal);
//        log.info("latestVersion最新版本是:{}", JsonUtil.toJson(latestVersion));
        if (null == latestVersion ||
                latestVersion.getMessage() == null
                || latestVersion.getUpdateType() == null
                || latestVersion.getAppVersion() == null) {
            return AppVersionBean.builder()
                    .message("不知道的终端")
                    .level(1)
                    .full("")
                    .latestVersion(currentVersion)
                    .build();
        }
        AppVersionBean versionBean = AppVersionBean.builder()
                .message(latestVersion.getMessage())
                .level(latestVersion.getUpdateType())
                .full(latestVersion.getFileOrUrl())
                .latestVersion(latestVersion.getAppVersion())
                .build();

        if (latestVersion.getTerminal() == AppVersionEnum.TerminalTypeEnum.ANDROID.getValue()) {
            if (latestVersion.getUpdateChannel() > 0) {
                versionBean.setUpdateChannel(latestVersion.getUpdateChannel());
            }
            if (latestVersion.getUpdateType() == AppVersionEnum.UpdateTypeEnum.PATCH.getValue()) {
                versionBean.setBulk(latestVersion.getFileOrUrl());
                versionBean.setBulkMd5(latestVersion.getFileMd5());
            }
        }
        boolean update = false;
        //用户当前版本不存在或者是当前版本,就当做最新版本,不需要更新
        if (versionCount < latestVersion.getVersionCount()) {
            /**
             * 全部更新
             */
            if (latestVersion.getReleaseType() == AppVersionEnum.ReleaseTypeEnum.ALL.getValue()) {
                update = true;
            } else if (latestVersion.getReleaseType() == AppVersionEnum.ReleaseTypeEnum.MODE.getValue() && latestVersion.getUpdateMode() > 0) {
                /**
                 * 如果为取模更新
                 * update 取模更新 暂时不使用,统一返回update=true
                 */
                update = true;

               /* if (latestVersion.getUpdateMode() == AppVersionEnum.UpdateMode.PERCENT10.getValue() && userId % 100 < 10) {
                    update = true;
                } else if (latestVersion.getUpdateMode() == AppVersionEnum.UpdateMode.PERCENT20.getValue() && userId % 100 < 20) {
                    update = true;
                } else if (latestVersion.getUpdateMode() == AppVersionEnum.UpdateMode.PERCENT25.getValue() && userId % 100 < 25) {
                    update = true;
                } else if (latestVersion.getUpdateMode() == AppVersionEnum.UpdateMode.PERCENT50.getValue() && userId % 100 < 50) {
                    update = true;
                } else {
                    update = false;
                }*/
            } else if (latestVersion.getReleaseType() == AppVersionEnum.ReleaseTypeEnum.WHILT_LIST.getValue()) {
                if (userId != UserService.DEFAULT_USER_ID) {
                    /*
                     * 白名单帐号
                     * update 将白名单独放在外面判断
                     */
                    if (isAudit(userId)) {
                        update = true;
                    }
                }
            } else {
                update = false;
            }
        } else {
            update = false;
        }
        /**
         * 如果为ios版本，并且版本低于 7.0.0 不支持更新
         */
        if (terminal == AppVersionEnum.TerminalTypeEnum.IOS.getValue() && versionCount < 700) {
            update = false;
        }
        //6版本一下,禁止强制更新
        if (versionCount < 600) {
            update = false;
        }
        versionBean.setUpdate(update);
        return versionBean;

    }

    private int getClient(int terminal) {
        int client = -1;
        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            client = AppVersionEnum.TerminalTypeEnum.ANDROID.getValue();
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            client = AppVersionEnum.TerminalTypeEnum.IOS.getValue();
        }
        return client;
    }

    /**
     * 当前终端的最新版本
     *
     * @param terminal
     * @return com.huatu.tiku.entity.AppVersion
     */
    public com.huatu.tiku.entity.AppVersion getLatestVersion(int terminal) {
        int appNme = AppVersionEnum.AppNameEnum.HUATU_ONLINE.getValue();
        HashMap<String, Object> result = appVersionMapper.findLatestVersion(terminal, appNme);
        com.huatu.tiku.entity.AppVersion appVersion = com.huatu.tiku.entity.AppVersion.builder().build();
        if (null == result) {
            return appVersion;
        } else {
            transMap2Bean(result, appVersion);
            return appVersion;
        }
    }

    /**
     * 校验用户id是否存在白名单中
     *
     * @param userId
     * @return
     */
    public boolean isAudit(long userId) {
        String whiteList = userServerConfig.getUpdateWhiteList();
        if (StringUtils.isEmpty(userServerConfig.getUpdateWhiteList())) {
            return false;
        } else {
            //log.info("更新白名单用户:: => {},校验ID = {}", whiteList, userId);
            return Arrays.stream(whiteList.split(","))
                    .anyMatch(whiteId -> whiteId.equals(String.valueOf(userId)));
        }
    }


    /**
     * map 转化为bean对象
     *
     * @param map
     * @param obj
     */
    public static void transMap2Bean(Map<String, Object> map, Object obj) {

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
