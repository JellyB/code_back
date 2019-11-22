package com.huatu.ztk.backend.advert.service;

import com.huatu.ztk.backend.advert.bean.Advert;
import com.huatu.ztk.backend.advert.dao.AdvertDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.util.ImageUtil;
import ij.ImagePlus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by renwenlong on 2016/11/16.
 */
@Service
public class AdvertService {
    private static final Logger logger = LoggerFactory.getLogger(AdvertService.class);
    public static final int APP_HUATU_ONLINE = 2;

    @Autowired
    private AdvertDao advertDao;
    @Autowired
    private UploadFileUtil uploadFileUtil;

    //广告状态参数
    private static final Integer ENABLED = 1;
    private static final Integer CLOSED = 0;

    @Autowired
    private PaperDao paperDao;


    //app轮播图
    private static final int BANNER_TYPE = 1;

    //app启动页图片
    private static final int LAUCH_TYPE = 2;

    //app首页弹出图
    private static final int POPUP_TYPE = 3;

    //网站首页广告
    private static final int PC_TYPE = 4;

    private static final int NOTICE = 5;


    /**
     * 获取所有广告列表
     *
     * @return
     */
    public List<Advert> getAllAds() {
        List<Advert> adverts = advertDao.getAllAds();
        return adverts;
    }

    /**
     * 根据科目获取广告列表
     *
     * @param catgory
     * @param type
     * @return
     */
    public List<Advert> getAdsByCatgoryAndType(int catgory, int type) {
        List<Advert> adverts = advertDao.getAdsByCatgoryAndType(catgory, type);
        return adverts;
    }

    /**
     * 上传广告图片
     *
     * @param target
     * @param title
     * @param params
     * @param category
     * @param type
     * @param dest       @throws BizException
     * @param newVersion
     * @param position
     * @param appType
     */
    public void upload(String target, String title, String params, int category, int type,
                       File dest, int newVersion, int position, int appType,
                       long onlineTime, long offlineTime, int index,long courseCollectionId) throws BizException, UnsupportedEncodingException {
        //通过ftp上传图片并返回url
        final String imageUrl = uploadFileUtil.ftpUploadPic(dest);

        target = StringUtils.trimToEmpty(target);

        if (target.equals("ztk://course/seckill") && appType != APP_HUATU_ONLINE) {
            throw new BizException(ErrorResult.create(9001, "砖题库不支持秒杀课程"));
        }


        //multipart/form-data格式表单提交数据要进行编码格式转换
        title = StringUtils.trimToEmpty(new String(title.getBytes("iso-8859-1"), "utf-8"));
        params = StringUtils.trimToEmpty(new String(params.getBytes("iso-8859-1"), "utf-8"));
        //组装param对应的json
        Map paramMap = getParamMap(target, params);
        /**
         * update by lijun 2018-06-03 合集课程新增参数 课程ID
         */
        if (target.equals("ztk://course/collection")){
            paramMap.put("rid",courseCollectionId);
        }

        if (type == PC_TYPE) {
            paramMap.put("position", position);
        } else if (type == POPUP_TYPE) {
            final ImagePlus imagePlus = ImageUtil.parse(imageUrl);
            final int height = imagePlus.getHeight();
            final int width = imagePlus.getWidth();
            paramMap.put("height", height);
            paramMap.put("width", width);
        }


        final Advert advert = Advert.builder()
                .target(target)
                .title(title)
                .params(JsonUtil.toJson(paramMap))
                .catgory(category)
                .status(0)
                .type(type)
                .image(imageUrl)
                .newVersion(newVersion)
                .appType(appType)
                .onlineTime(onlineTime)
                .index(index)
                .offlineTime(offlineTime)
                .build();
        if (type == 3) {
            List<Advert> allByType = advertDao.getAdsByCatgoryAndType(category, type);
            for (Advert adertByType : allByType) {
                if (!excludeIntersection(adertByType.getOnlineTime(), adertByType.getOfflineTime(), advert.getOnlineTime(), advert.getOfflineTime())) {
                    throw new BizException(ErrorResult.create(233333, "时间不可以重叠"));
                }
            }
        }
        advertDao.upload(advert);

    }

    public static boolean excludeIntersection(long onlineTimeOld, long offlineTimeOld, long onlineTimeNew, long offlineTimeNew) {
        return offlineTimeOld < onlineTimeNew || onlineTimeOld > offlineTimeNew;
    }

    //组装param对应的json
    private Map getParamMap(String target, String param) throws BizException {
        Map map = new HashMap<>();
        if (StringUtils.isNoneBlank(param)) {
            switch (target) {
                case "ztk://course/detail":
                case "ztk://course/seckill":
                    map.put("rid", Long.valueOf(param));
                    break;

                case "ztk://h5/active":
                case "ztk://h5/simulate":
                    map.put("url", param);
                    break;


                case "ztk://course/collection":
                    map.put("shortTitle", param);
                    break;

                case "ztk://pastPaper": {
                    Integer paperId = Integer.valueOf(param);
                    map.put("paperId", paperId);

                    Paper paper = paperDao.findById(paperId);
                    if (paper == null) {
                        throw new BizException(ErrorResult.create(8000, "该试卷不存在"));
                    }

                    map.put("subject", paper.getCatgory());
                    map.put("type", paper.getType());
                }
                break;
            }
        }
        return map;
    }

    /**
     * 更新广告信息
     *
     * @param target
     * @param title
     * @param params
     * @param file
     * @param id
     * @param newVersion
     * @param position
     * @param appType
     * @throws BizException
     */
    public void update(String target, String title, int category, int type, String params,
                       MultipartFile file, long id, int newVersion, int position, int appType,
                       long onlineTime, long offlineTime, int index,long courseCollectionId) throws BizException, IOException {
        Assert.notNull(target,"请详细检查你的填写,有没有填的");
        Assert.notNull(title,"请详细检查你的填写,有没有填的");
        Assert.notNull(type,"请详细检查你的填写,有没有填的");
        Assert.notNull(category,"请详细检查你的填写,有没有填的");
        Assert.notNull(courseCollectionId,"请详细检查你的填写,有没有填的");
        target = StringUtils.trimToEmpty(target);

        if (target.equals("ztk://course/seckill") && appType != APP_HUATU_ONLINE) {
            throw new BizException(ErrorResult.create(9001, "砖题库不支持秒杀课程"));
        }

        //图片地址
        String imageUrl = "";
        //multipart/form-data格式表单提交数据要进行编码格式转换
        title = StringUtils.trimToEmpty(new String(title.getBytes("iso-8859-1"), "utf-8"));
        params = StringUtils.trimToEmpty(new String(params.getBytes("iso-8859-1"), "utf-8"));
        //组装param对应的json
        Map paramMap = getParamMap(target, params);
        /**
         * update by lijun 2018-06-03 合集课程新增参数 课程ID
         */
        if (target.equals("ztk://course/collection")){
            paramMap.put("rid",courseCollectionId);
        }
        if (file != null) {//图片有更新
            //获取文件数据
            final File dest = new File(file.getOriginalFilename());
            file.transferTo(dest);
            //通过ftp上传图片并返回url
            imageUrl = uploadFileUtil.ftpUploadPic(dest);
        } else {//图片没有更新
            imageUrl = getAdvertById(id).getImage();//获取原有的图片url
        }

        if (type == PC_TYPE) {
            paramMap.put("position", position);
        } else if (type == POPUP_TYPE) {
            final ImagePlus imagePlus = ImageUtil.parse(imageUrl);
            final int height = imagePlus.getHeight();
            final int width = imagePlus.getWidth();
//            paramMap.put("hide", hide);
//            paramMap.put("text", text);

            paramMap.put("height", height);
            paramMap.put("width", width);
        }

        final Advert advert = Advert.builder()
                .id(id)
                .target(target)
                .title(title)
                .catgory(category)
                .type(type)
                .params(JsonUtil.toJson(paramMap))
                .image(imageUrl)
                .newVersion(newVersion)
                .appType(appType)
                .onlineTime(onlineTime)
                .offlineTime(offlineTime)
                .index(index)
                .build();
        if (type == 3) {
            List<Advert> allByType = advertDao.getAdsByCatgoryAndType(category, type);
            for (Advert adertByType : allByType) {
                if (adertByType.getId() != advert.getId()) {
                    if (!excludeIntersection(adertByType.getOnlineTime(), adertByType.getOfflineTime(), advert.getOnlineTime(), advert.getOfflineTime())) {
                        throw new BizException(ErrorResult.create(233333, "时间不可以重叠"));
                    }
                }
            }
        }
        advertDao.update(advert);
    }

    /**
     * 修改广告的展示状态
     *
     * @param id
     * @return
     */
    public void modStatusById(long id) {
        Advert advert = advertDao.findById(id);
        int status = advert.getStatus();
        if (status == CLOSED) {
            advertDao.modStatusById(ENABLED, id);
        } else {
            advertDao.modStatusById(CLOSED, id);
        }
    }

    /**
     * 删除广告
     *
     * @param id
     * @return
     */
    public void deleteById(long id) {
        advertDao.deleteById(id);
    }


    /**
     * 根据id获取广告相关信息
     *
     * @param id
     * @return
     */
    public Advert getAdvertById(long id) {
        Advert advert = advertDao.findById(id);
        return advert;
    }


    public void gogogoById(long id) {
        Advert advert = advertDao.findById(id);
        advert.setStatus(1);
        advertDao.update(advert);
    }
}
