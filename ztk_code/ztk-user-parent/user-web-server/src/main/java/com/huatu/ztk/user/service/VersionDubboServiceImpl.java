package com.huatu.ztk.user.service;

import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.user.bean.AppVersion;
import com.huatu.ztk.user.bean.AppVersionBean;
import com.huatu.ztk.user.common.VersionRedisKey;
import com.huatu.ztk.user.dao.AppVersionDao;
import com.huatu.ztk.user.dubbo.VersionDubboService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by linkang on 9/28/16.
 */
public class VersionDubboServiceImpl implements VersionDubboService {
    private static final Logger logger = LoggerFactory.getLogger(VersionDubboServiceImpl.class);

    @Autowired
    private AppVersionDao appVersionDao;

    @Resource(name = "coreRedisTemplate")
    private ValueOperations valueOperations;

    //测试uid列表
    private static final List<Long> testUids =
            Collections.unmodifiableList(Arrays.asList(233939122L, 233883562L, 2233997281L, 7741045L, 11563817L, 233098784L));


    /**
     * 获得版本信息
     *
     * @param terminal       终端
     * @param currentVersion 当前客户端版本
     * @param catgory        科目  1：公务员
     * @return
     */
    @Override
    public AppVersionBean checkVersion(int terminal, String currentVersion, long userId, int catgory) {
        //版本号的数字 版本号格式为x.x.x 如果不够,则用 0补齐
        int versionCount = Integer.valueOf(StringUtils.rightPad(currentVersion.replaceAll("\\.", ""), 3, '0'));
        int client = getClient(terminal);
        //用户当前版本
        final AppVersion current = appVersionDao.findVersion(client, versionCount, catgory);
        //砖题库不再更新，华图在线最低版本是6.0.0
        if (versionCount < 600) {
            if (current != null) {
                AppVersionBean versionBean = AppVersionBean.builder()
                        .message(current.getMessage())
                        .level(current.getLevel())
                        .full(current.getFull())
                        .latestVersion(current.getVersion())
                        .build();
                versionBean.setUpdate(false);
                return versionBean;
            } else {
                return AppVersionBean.builder()
                        .message("不知道的终端")
                        .level(1)
                        .full("")
                        .latestVersion(currentVersion)
                        .build();
            }

        }


        //最新版本
        final AppVersion latest = getLatestVersion(terminal, catgory);
        if (latest == null) {//没有查到最新版本
            logger.warn("unknown terminal={},category={}", terminal, catgory);
            return AppVersionBean.builder()
                    .message("不知道的终端")
                    .level(1)
                    .full("")
                    .latestVersion(currentVersion)
                    .build();
        }

        AppVersionBean versionBean = AppVersionBean.builder()
                .message(latest.getMessage())
                .level(latest.getLevel())
                .full(latest.getFull())
                .latestVersion(latest.getVersion())
                .build();
        boolean update = false;

        //用户当前版本不存在或者是当前版本,就当做最新版本,不需要更新
        if (versionCount < latest.getVersionCount()) {
            update = true;
        }

        //取模更新,批量更新用户
        if (latest.getUpdateMode() > 0 && update) {
            if (userId % latest.getUpdateMode() != 0) {
                update = false;
            }

            if (testUids.contains(userId)) {//特殊账户,设置为更新,方便线上测试
                update = true;
            }
        }
        //当前版本不为null,则设置当前版本的增量信息

         if (current != null && current.getVersionCount() == latest.getVersionCount()) {
        //只对测试用户设置增量升级
      //  if (current != null && current.getVersionCount() == latest.getVersionCount() && testUids.contains(userId)) {
            versionBean.setBulk(current.getBulk());
            versionBean.setBulkMd5(current.getBulkMd5());
        }
        versionBean.setUpdate(update);
        return versionBean;
    }

    /**
     * 获取指定终端下的最新版本
     *
     * @param terminal
     * @param category
     * @return
     */
    @Override
    public AppVersion getLatestVersion(int terminal, int category) {
     //   StringBuilder sb = new StringBuilder(VersionRedisKey.LATEST_VERSION_OBJ_PREFIX).append(terminal).append("_").append(category);
//        Object object = valueOperations.get(sb.toString());
//        if (object != null) {
//            return (AppVersion) object;
//        }
        int client = getClient(terminal);
        if (client < 0) {
            return null;
        }
        final AppVersion latest = appVersionDao.findLatestVersion(client, category);
//        if (latest != null) {
//            valueOperations.set(sb.toString(), latest, 1, TimeUnit.DAYS);
//        }
        return latest;
    }

    private int getClient(int terminal) {
        int client = -1;
        if (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD) {
            client = 1;
        } else if (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD) {
            client = 2;
        }
        return client;
    }
}
