package com.huatu.tiku.banckend.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.banckend.dao.manual.AdvertMapper;
import com.huatu.tiku.banckend.service.AdvertService;
import com.huatu.tiku.common.AdvertEnum;
import com.huatu.tiku.entity.Advert;
import com.huatu.tiku.teacher.service.common.AreaService;
import com.huatu.tiku.util.http.ResponseMsg;
import com.huatu.ztk.commons.JsonUtil;
import ij.ImagePlus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/5/31
 */
@Service
@Slf4j
public class AdvertServiceImpl implements AdvertService {

    @Value("${checkClassIdUrl}")
    private String checkClassIdUrl;

    @Autowired
    AdvertMapper advertMapper;

    @Autowired
    AreaService areaService;

    @Override
    public List<Advert> getAdvertByAllConditions(String title, String target, int type, int status, int category, int appType, long onLineTime, long offLineTime, String platForm, int subject, int cateId) {
        Example example = buildExample(title, target, type, status, category, appType, onLineTime, offLineTime, platForm, subject, cateId);
        List<Advert> mappers = advertMapper.selectByExample(example);
        return mappers.stream()
                .map(advert -> {
                    advert.setTargetName(AdvertEnum.Target.getValueByCode(advert.getTarget()));
                    advert.setCategoryName(AdvertEnum.Category.getValueByCode(advert.getCategory()));
                    advert.setTypeName(AdvertEnum.Type.getValueByCode(advert.getType()));
                    return advert;
                })
                .collect(Collectors.toList());
    }

    @Override
    public int save(int id, String target, String title, String params, int category, int type, String imageUrl, int newVersion, int position, int appType, long onlineTime, long offlineTime, int index, long courseCollectionId,String padImageUrl,String platForm, int subject, int cateId, int mId, String mTitle) {
        //通过ftp上传图片并返回url
        log.info("advert save.params:title:{}, courseCollectionId:{}, mid:{}, mTitle:{}", title, courseCollectionId, mId, mTitle);
        target = StringUtils.trimToEmpty(target);
        title = StringUtils.trimToEmpty(title);
        int a = 0;
        int m = 0;
        AdvertEnum.PlatForm platFormEnum = AdvertEnum.PlatForm.convert(platForm);
        //m 站专属参数
        if (target.equals("ztk://course/seckill") && appType != AdvertEnum.AppType.HTZX.getCode()) {
            throw new BizException(ErrorResult.create(9001, "砖题库不支持秒杀课程"));
        }
        if(null == platFormEnum){
            a = 1;
            if(type == AdvertEnum.Type.SYLBT.getCode()){
                throw new BizException(ErrorResult.create(9002, "显示平台不能为空"));
            }
        }else if(platFormEnum == AdvertEnum.PlatForm.APP_M){
            a = 1;
            m = 1;
        }else{
            if(platFormEnum == AdvertEnum.PlatForm.APP){
                a = 1;
            }
            if(platFormEnum == AdvertEnum.PlatForm.M){
                m = 1;
                if(StringUtils.isEmpty(imageUrl)){
                    throw new BizException(ErrorResult.create(9003, "M 站广告图片不能为空！"));
                }
            }
        }

        JSONObject mPrams = new JSONObject();
        //组装param对应的json
        Map paramMap = getParamMap(target, params, mPrams);
        if (target.equals(AdvertEnum.Target.KCHJ.getUrl())) {
            paramMap.put("rid", courseCollectionId);
        }

        if (AdvertEnum.Type.needPosition(type)) {
            paramMap.put("position", position);
        } else if (AdvertEnum.Type.needHW(type)) {
            final ImagePlus imagePlus = parse(imageUrl);
            final int height = imagePlus.getHeight();
            final int width = imagePlus.getWidth();
            paramMap.put("height", height);
            paramMap.put("width", width);
        }

        if (AdvertEnum.Type.needJudgmentTime(type)) {
            //查询目前该模块下一进上线的广告，避免时间重复
            List<Advert> allByType = selectByTypeAndCategory(type, category, AdvertEnum.Status.ENABLED.getCode());
            boolean match = allByType.parallelStream()
                    .filter(advert1 -> !advert1.getId().equals(id))
                    .anyMatch(advert1 -> !excludeIntersection(advert1.getOnLineTime(), advert1.getOffLineTime(), advert1.getOnLineTime(), advert1.getOffLineTime()));
            if (match) {
                throw new BizException(ErrorResult.create(500000, "时间不可以重叠"));
            }
        }

        mPrams.put("mId", mId > 0 ? mId : MapUtils.getIntValue(paramMap, "rid", 0));
        mPrams.put("mTitle", StringUtils.isEmpty(mTitle) ? title : mTitle);

        Advert advert = Advert.builder()
                .a(a)
                .m(m)
                .target(target)
                .title(title)
                .params(JsonUtil.toJson(paramMap))
                .category(category)
                .type(type)
                .image(imageUrl)
                .padImageUrl(padImageUrl)
                .newVersion(newVersion)
                .appType(appType)
                .onLineTime(String.valueOf(onlineTime))
                .index(index)
                .offLineTime(String.valueOf(offlineTime == -1 ? Long.MAX_VALUE : offlineTime))
                .createTime(new Timestamp(new Date().getTime()))
                .platForm(platForm)
                .subject(subject)
                .cateId(cateId)
                .mParams(mPrams.toJSONString())
                .build();


        if (id > 0) {
            advert.setId(id);
            //如果是首页弹出图,删除旧广告图生成新id
            if (advert.getType() == AdvertEnum.Type.SYTCT.getCode()) {
                advertMapper.deleteByPrimaryKey(id);
                advert.setId(null);
            } else {
                return advertMapper.updateByPrimaryKeySelective(advert);
            }
        }
        //新增数据默认下线状态
        advert.setStatus(AdvertEnum.Status.CLOSED.getCode());
        return advertMapper.insert(advert);
    }


    public List<Advert> selectByTypeAndCategory(int type, int category, int status) {
        WeekendSqls<Advert> weekendSql = WeekendSqls.custom();
        weekendSql.andEqualTo(Advert::getType, type);
        weekendSql.andEqualTo(Advert::getCategory, category);
        weekendSql.andEqualTo(Advert::getStatus, status);
        Example example = Example.builder(Advert.class)
                .where(weekendSql)
                .build();
        return advertMapper.selectByExample(example);
    }

    @Override
    public int delete(int id) {
        return advertMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int updateStatus(int id) {
        Advert advert = advertMapper.selectByPrimaryKey(id);
        if (null == advert) {
            return 0;
        }
        if (advert.getStatus() == AdvertEnum.Status.CLOSED.getCode()) {
            //上线某个广告 需要判断时间段是否重复
            if (AdvertEnum.Type.needJudgmentTime(advert.getType())) {
                //查询目前该模块下一进上线的广告，避免时间重复
                List<Advert> allByType = selectByTypeAndCategory(advert.getType(), advert.getCategory(), AdvertEnum.Status.ENABLED.getCode());
                for (Advert advertByType : allByType) {
                    if (!excludeIntersection(advertByType.getOnLineTime(), advertByType.getOffLineTime(), advertByType.getOnLineTime(), advertByType.getOffLineTime())) {
                        throw new BizException(ErrorResult.create(500000, "时间不可以重叠"));
                    }
                }
            }
            advert.setStatus(AdvertEnum.Status.ENABLED.getCode());
        } else {
            advert.setStatus(AdvertEnum.Status.CLOSED.getCode());
        }
        return advertMapper.updateByPrimaryKeySelective(advert);
    }

    @Override
    public Advert detail(int id) {
        Advert advert = advertMapper.selectByPrimaryKey(id);
        if(StringUtils.isNotEmpty(advert.getMParams())){
            JSONObject jsonObject = JSONObject.parseObject(advert.getMParams());
            advert.setMid(jsonObject.getInteger("mId"));
            advert.setMtitle(jsonObject.getString("mTitle"));
        }else{
            advert.setMid(0);
            advert.setMtitle("");
        }
        return advert;
    }

    /**
     * 构造查询条件
     *
     * @return
     */
    private Example buildExample(String title, String target, int type, int status, int category, int appType, long onLineTime, long offLineTime, String platForm, int subject, int cateId) {
        WeekendSqls<Advert> weekendSql = WeekendSqls.custom();
        if (StringUtils.isNotBlank(title)) {
            weekendSql.andLike(Advert::getTitle, "%" + title + "%");
        }
        if (StringUtils.isNotBlank(target)) {
            weekendSql.andEqualTo(Advert::getTarget, target);
        }
        if (0 != type) {
            weekendSql.andEqualTo(Advert::getType, type);
        }
        if (-1 != status) {
            weekendSql.andEqualTo(Advert::getStatus, status);
        }
        if (0 != category) {
            weekendSql.andEqualTo(Advert::getCategory, category);
        }
        if (0 != appType) {
            weekendSql.andEqualTo(Advert::getAppType, appType);
        }
        if (0 != onLineTime) {
            weekendSql.andGreaterThanOrEqualTo(Advert::getOnLineTime, onLineTime);
        }
        if (0 != offLineTime) {
            weekendSql.andLessThanOrEqualTo(Advert::getOffLineTime, offLineTime);
        }
        if(StringUtils.isEmpty(platForm)){
            weekendSql.andEqualTo(Advert::getPlatForm, platForm);
        }
        if(0 != subject){
            weekendSql.andEqualTo(Advert::getSubject, subject);
        }
        if(0 != cateId){
            weekendSql.andEqualTo(Advert::getCateId, cateId);
        }
        Example example = Example.builder(Advert.class)
                .where(weekendSql)
                .orderByDesc("id")
                .build();
        return example;
    }

    //组装param对应的json
    private Map getParamMap(String target, String param, JSONObject mPrams) throws BizException {
        param = StringUtils.trimToEmpty(param);
        Map map = new HashMap<>();
        if (StringUtils.isNoneBlank(param)) {
            switch (target) {
                //申论套题列表页
                case "ztk://essay/paper":
                    Integer areaId = Integer.valueOf(param);
                    map.put("areaId", areaId);
                    break;
                case "ztk://course/detail":
                    map.put("rid", Long.valueOf(param));
                    int type = getCourseType(Long.valueOf(param));
                    map.put("type", type);
                    break;
                case "ztk://course/seckill":
                    map.put("rid", Long.valueOf(param));
                    break;
                case "ztk://h5/active":
                //case "ztk://h5/simulate":
                case "ztk://match/detail":
                    mPrams.put("url", param);
                    map.put("url", param);
                    break;
                case "ztk://course/collection":
                    map.put("shortTitle", param);
                    break;
                case "ztk://pastPaper": {
                    Integer paperId = Integer.valueOf(param);
                    map.put("paperId", paperId);
                    break;
                }
                case "ztk://estimatePaper":{
                    Integer paperId = Integer.valueOf(param);
                    map.put("paperId",paperId);
                    break;
                }
                case "ztk://pastPaper/province":{
                    Long area = Long.valueOf(param);
                    List<String> names = areaService.findNameByIds(Lists.newArrayList(area));
                    if(CollectionUtils.isNotEmpty(names)){
                        map.put("area",area.intValue());
                        map.put("areaName",names.get(0));
                    }else{
                        map.put("area",-9);
                        map.put("areaName","全国");
                    }
                }
            }
        }
        return map;
    }

    /**
     * 调用php 接口校验课程id 直播 or 录播
     *
     * @param classId
     * @return
     */
    public int getCourseType(long classId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(checkClassIdUrl)
                    .queryParam("classId", classId);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            HttpEntity<ResponseMsg> exchange = restTemplate.exchange(uriComponentsBuilder.toUriString(), HttpMethod.GET, entity, ResponseMsg.class);
            ResponseMsg responseMsg = exchange.getBody();
            if (responseMsg.getCode() == 10000) {
                if (null == responseMsg.getData()) {
                    return -1;
                } else {
                    Map<String, String> data = (Map<String, String>) responseMsg.getData();
                    return Integer.valueOf(data.get("videoType"));
                }
            } else {
                return -1;
            }

        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 解析图片信息
     *
     * @param url 图片地址
     * @return
     */
    public ImagePlus parse(String url) {
        ImagePlus imagePlus = null;
        for (int i = 0; i < 3; i++) {
            imagePlus = new ImagePlus(url);
            //保证能获取到正常的图片，否则继续尝试
            if (imagePlus.getWidth() > 0 && imagePlus.getHeight() > 0) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
            }
        }
        return imagePlus;
    }

    /**
     * 判断时间
     *
     * @return
     */
    public static boolean excludeIntersection(String onlineTimeOld, String offlineTimeOld, String onlineTimeNew, String offlineTimeNew) {
        return Long.parseLong(offlineTimeOld) < Long.parseLong(onlineTimeNew)
                || Long.parseLong(onlineTimeOld) > Long.parseLong(offlineTimeNew);
    }
}
