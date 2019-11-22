package com.huatu.ztk.user.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import com.huatu.ztk.paper.util.VersionUtil;
import com.huatu.ztk.user.bean.MMessage;
import com.huatu.ztk.user.bean.Message;
import com.huatu.ztk.user.dao.AdvertMessageDao;
import com.huatu.ztk.user.daoPandora.AdvertMapper;
import com.huatu.ztk.user.utils.MessageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 首页广告图service层
 * Created by linkang on 11/4/16.
 */

@Service
public class AdvertMessageService {

    private static final Logger logger = LoggerFactory.getLogger(AdvertMessageService.class);
    @Autowired
    private AdvertMessageDao advertMessageDao;
    @Autowired
    private AdvertMapper advertMapper;

    @Resource(name = "coreRedisTemplate")
    private ValueOperations valueOperations;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    /**
     * 直接设定砖题库的最新版本，区分砖题库和华图在线版本
     */
    public static final String GWY_IOS_NEW_VERSION = "2.4.8";
    private static final String GWY_ANDROID_NEW_VERSION = "2.4.9";
    public static final String SHIYE_IOS_NEW_VERSION = "2.4.8";
    private static final String SHIYE_ANDROID_NEW_VERSION = "2.4.9";
    private static final String BANNER_PREFIX = "user_banner_";

    @Autowired
    private UserServerConfig userServerConfig;

    //app轮播图
    private static final int BANNER_TYPE = 1;

    //app启动页图片
    private static final int LAUCH_TYPE = 2;

    //app首页弹出图
    private static final int POPUP_TYPE = 3;

    //app公告
    private static final int NOTICE_TYPE = 5;

    private final int NEW_VERSION_VALUE = 1;

    private final int OLD_VERSION_VALUE = 0;

    private final int ANY_VERSION_VALUE = -1;

    private static final String ADVERT_PREFIX = "user_advert_";
    private static final String ADVERT_PREFIX_M = "user_advert_m_";

    Cache<String, List<Message>> advertCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    Cache<String, List<MMessage>> mAdvertCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();


    /**
     * 查询首页广告图
     *
     * @param category 科目
     * @param terminal
     * @param cv       @return
     * @param appType
     */
    public List<Message> findBannerList(int category, int terminal, String cv, long userId, int appType) {
        int newVersion = isNewVersion(category, terminal, cv) ? NEW_VERSION_VALUE : OLD_VERSION_VALUE;
        boolean iosNewVersion = true;

        if (category == CatgoryType.GONG_WU_YUAN) { //公务员app
            iosNewVersion = isIosNewVersion(terminal, cv, GWY_IOS_NEW_VERSION);
        } else if (category == CatgoryType.SHI_YE_DAN_WEI) { //事业单位app
            iosNewVersion = isIosNewVersion(terminal, cv, SHIYE_IOS_NEW_VERSION);
        }
        List<Message> messages;
        //测试环境不走缓存
        if (!userServerConfig.getEnvironment().contains("test")) {
            String key = new StringBuilder(BANNER_PREFIX + newVersion).append(BANNER_TYPE).append("_").append(category).append("_").append(appType).toString();
            messages = (List<Message>) valueOperations.get(key);
            if (messages != null && messages.size() > 0) {
                if (iosNewVersion) { //如果是IOS新版本，轮播图模考大赛应该是estimatePaper/home
                    MessageUtil.dealMessage(messages);
                }
                return messages;
            }
            messages = findAdvert(category, BANNER_TYPE, newVersion, userId, appType);
            if (messages != null && messages.size() > 0) {
                valueOperations.set(key, messages, 5, TimeUnit.MINUTES);
            }
        } else {
            messages = findAdvert(category, BANNER_TYPE, newVersion, userId, appType);
        }
        if (iosNewVersion) { //如果是IOS新版本，轮播图模考大赛应该是estimatePaper/home
            MessageUtil.dealMessage(messages);
        }
        return messages;
    }


    /**
     * 查询首页广告图
     *
     * @param category 科目
     * @param terminal
     * @param cv       @return
     * @param appType
     */
    public List<Message> findBannerListV3(int category, int terminal, String cv, long userId, int appType) {
        int newVersion = isNewVersion(category, terminal, cv) ? NEW_VERSION_VALUE : OLD_VERSION_VALUE;
        List<Message> messages = findAdvert(category, BANNER_TYPE, newVersion, userId, appType);
        return messages;
    }

    /**
     * 查询 m 站轮播图
     */
    public List<MMessage> findMBannerList() {

        List<MMessage> newMessages;

        /*String advertKey = new StringBuilder(ADVERT_PREFIX_M).append(category).toString();

        List<MMessage> advertMessage = mAdvertCache.getIfPresent(advertKey);

        if (CollectionUtils.isNotEmpty(advertMessage)) {
            return advertMessage;
        }*/
        newMessages = findMAdvertFromDB();
        //mAdvertCache.put(advertKey, newMessages);

        return newMessages;
    }


    /**
     * 判断版本号是否大于指定版本
     *
     * @param catgory
     * @param terminal
     * @param cv
     * @return
     */
    private static boolean isNewVersion(int catgory, int terminal, String cv) {
        boolean iosNewVersion;
        boolean androidNewVersion;
        if (catgory == CatgoryType.GONG_WU_YUAN) { //公务员app
            iosNewVersion = isIosNewVersion(terminal, cv, GWY_IOS_NEW_VERSION);
            androidNewVersion = isAndroidNewVersion(terminal, cv, GWY_ANDROID_NEW_VERSION);
        } else if (catgory == CatgoryType.SHI_YE_DAN_WEI) { //事业单位app
            iosNewVersion = isIosNewVersion(terminal, cv, SHIYE_IOS_NEW_VERSION);
            androidNewVersion = isAndroidNewVersion(terminal, cv, SHIYE_ANDROID_NEW_VERSION);
        } else {
            return true;
        }
        return iosNewVersion || androidNewVersion;
    }

    /**
     * ios版本号是否大于指定的版本
     *
     * @param terminal
     * @param userCv
     * @param newCv
     * @return
     */
    public static boolean isIosNewVersion(int terminal, String userCv, String newCv) {
        return (terminal == TerminalType.IPHONE || terminal == TerminalType.IPHONE_IPAD)
                && VersionUtil.compare(userCv, newCv) >= 0;
    }

    /**
     * Android版本号是否大于指定的版本
     *
     * @param terminal
     * @param userCv
     * @param newCv
     * @return
     */
    private static boolean isAndroidNewVersion(int terminal, String userCv, String newCv) {
        return (terminal == TerminalType.ANDROID || terminal == TerminalType.ANDROID_IPAD)
                && VersionUtil.compare(userCv, newCv) >= 0;
    }


    /**
     * 根据科目查看启动广告图列表
     *
     * @param category
     * @param appType
     * @return
     */
    public List<Message> findLaunchList(int category, long userId, int appType) {
        return findAdvert(category, LAUCH_TYPE, ANY_VERSION_VALUE, userId, appType);
    }

    /**
     * 根据科目查看首页弹出广告图列表
     *
     * @param catgory
     * @param appType
     * @return
     */
    public List<Message> findPopupList(int catgory, int appType) {
        return findAdvert(catgory, POPUP_TYPE, OLD_VERSION_VALUE, -1, appType);
    }


    public List<Message> findNewPopupList(int category, long userId, int appType) {
        return findAdvert(category, POPUP_TYPE, NEW_VERSION_VALUE, userId, appType);
    }

    /**
     * 修改:原来广告走的是vhuatu库的旧表,后来持久层替换成pandora的advert
     * 走guawa缓存,缓存时间是5分钟
     *
     * @param category   考试类别
     * @param type       广告类型
     * @param newVersion 版本
     * @param userId     用户ID
     * @param appType
     * @return
     */
    private List<Message> findAdvert(int category, int type, int newVersion, long userId, int appType) {

        List<Message> newMessages;

       /* if (userServerConfig.getEnvironment().contains("test")) {
            logger.info("测试环境不走缓存");
            return findAdvertFromDB(category, type, newVersion, appType);
        }*/
        String advertKey = new StringBuilder(ADVERT_PREFIX).append(type).append("_").append(newVersion).append("_")
                .append(appType).append("_").append(category).toString();
        // logger.info("findAdvert缓存key:{}", advertKey);
        List<Message> advertMessage = advertCache.getIfPresent(advertKey);
        // logger.info("advertMessage缓存内容是:{}", advertMessage);
        if (CollectionUtils.isNotEmpty(advertMessage)) {
            return advertMessage;
        }
        newMessages = findAdvertFromDB(category, type, newVersion, appType);
        advertCache.put(advertKey, newMessages);

        /**
         * 原来逻辑：如果跳转的是真题做题页面（ztk://pastPaper）,需要判断用户的做题状态;并且只有type=3即首页弹出图生效
         * 现在优化：单开接口返回给客户端用户状态,此处不做处理
         */
        // return newMessages;
        return filterList(newMessages, userId, type);
    }

    /**
     * 从mysql中查询广告信息
     *
     * @return
     */
    private List<Message> findAdvertFromDB(int category, int type, int newVersion, int appType) {
        if (newVersion == 0) {
            appType = 1;
        }
        List<HashMap<String, Object>> messageMaps = advertMapper.findAdvert(category, type, newVersion, appType);
        List<Message> newMessages = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(messageMaps)) {
            messageMaps.forEach(messageMap -> {
                Message message = convertToMessage(messageMap);
                newMessages.add(message);
            });
        }
        // logger.info("findAdvertFromDB");
        return newMessages;
    }

    /**
     * 从mysql中查询 m 站 广告轮播图
     *
     * @return
     */
    private List<MMessage> findMAdvertFromDB() {
        List<HashMap<String, Object>> messageMaps = advertMapper.findMAdvert();
        List<MMessage> newMMessages = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(messageMaps)) {
            messageMaps.forEach(messageMap -> {
                MMessage message = convertToMMessage(messageMap);
                newMMessages.add(message);
            });
        }
        return newMMessages;
    }


    private List<Message> filterList(List<Message> popupList, long userId, int type) {
        List<Message> messagesReplace = Lists.newLinkedList();
        logger.debug("~~~~~~~~~~~~~~~~~message长度~~~~~~~~~~~~" + popupList.size());
        for (Message message : popupList) {
            logger.debug("~~~~~~~~~~~~~~~~广告名字" + message.getTarget());
            String target = message.getTarget();
            if (target.equals("ztk://pastPaper") || target.equals("ztk://estimatePaper")) {
                int paperId = Integer.valueOf(message.getParams().get("paperId").toString());

                Paper paper = advertMessageDao.findPaper(paperId);

                PaperUserMeta userMeta = advertMessageDao.findPaperUserMeta(getId(userId, paperId));

                if (userMeta != null) {
                    paper.setUserMeta(userMeta);
                }

                message.getParams().put("pastPaper", paper);
            }
            if (message.getType() == 3 && filterDate(message.getOnLineTime(), message.getOffLineTime())) {
                messagesReplace.add(message);
            }
        }
        logger.debug("~~~~~~~~~~~~~~~~~message长度~~~~~~~~~~~~" + messagesReplace.size());
        if (type == 3) {
            //  logger.info("messagesReplace = {}",messagesReplace);
            return messagesReplace;
        }
        //logger.info("popupList = {}",popupList);
        return popupList;
    }

    /**
     * 与当前时间进行对比
     *
     * @param onLineTime
     * @param offLineTime
     * @return 如果当前时间在上线和下线时间之间则返回
     */
    public boolean filterDate(Long onLineTime, Long offLineTime) {
        long newTime = System.currentTimeMillis();
        if (onLineTime < newTime && offLineTime > newTime) {
            logger.debug("~~~~~~~~~~~时间复合返回true~~~~~~~~~~~~当前时间:" + newTime);
            return true;
        }
        logger.debug("~~~~~~~~~~~时间不符合~~~~~~~~~~~~上闲时间{},下线时间{}:" + onLineTime, offLineTime);
        return false;
    }

    /**
     * 获取答卷统计记录id
     *
     * @param uid
     * @param paperId
     * @return
     */
    public String getId(long uid, int paperId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uid).append("_").append(paperId);
        return stringBuilder.toString();
    }

    public Object findNoticeList(int category, long uid, int appType) {
        return findAdvert(category, NOTICE_TYPE, ANY_VERSION_VALUE, uid, appType);
    }


    /**
     * 网站首页广告图
     *
     * @param catgory
     * @return
     */
    public Object findPcHomePageList(int catgory) {
        List<Message> pcHomePageList = advertMessageDao.findPcHomePageList(catgory);

        Map<Integer, List<Message>> resultMap = new TreeMap<>();

        for (Message message : pcHomePageList) {
            int position = (int) message.getParams().get("position");

            List<Message> messageList = resultMap.get(position);
            if (messageList == null) {
                resultMap.put(position, Lists.newArrayList(message));
            } else {
                messageList.add(message);
                resultMap.put(position, messageList);
            }
        }

        return resultMap;
    }


    public Message convertToMessage(HashMap<String, Object> messageMap) {
        Integer id = (Integer) messageMap.get("id");
        String image = messageMap.get("image") == null ? "" : messageMap.get("image").toString();
        String title = messageMap.get("title") == null ? "" : messageMap.get("title").toString();
        String target = messageMap.get("target") == null ? "" : messageMap.get("target").toString();
        //其他参数，该字段保存的值为json形式,需要转成map
        String params = messageMap.get("params") == null ? "" : messageMap.get("params").toString();
        String onLineTime = messageMap.get("on_line_time") == null ? "" : messageMap.get("on_line_time").toString();
        String offLineTime = messageMap.get("off_line_time") == null ? "" : messageMap.get("off_line_time").toString();
        String padImageUrl = messageMap.get("pad_image_url") == null ? "" : messageMap.get("pad_image_url").toString();
        Integer cateId = MapUtils.getInteger(messageMap, "cate_id", 0);
        Integer subject = MapUtils.getInteger(messageMap, "subject", 0);
        Integer type = (Integer) messageMap.get("type");
        //位置
        int position = (Integer) messageMap.get("position");
        Map paramsMap = new HashMap<>();
        //防止错误的json
        try {
            //如果字段值非空
            if (StringUtils.isNoneBlank(params)) {
                paramsMap = JsonUtil.toMap(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //转换失败直接返回null
            return null;
        }

        //组装参数
        Map m = new HashMap<>();
        m.put("id", id);
        m.put("title", title);
        m.put("image", image);
        m.put("cateId", cateId);
        m.put("subject", subject);
        m.put("padImageUrl", padImageUrl);
        m.putAll(paramsMap);

        Message msg = Message.builder()
                .params(m)
                .target(target)
                .onLineTime(Long.parseLong(onLineTime))
                .offLineTime(Long.parseLong(offLineTime))
                .type(type)
                .build();
        return msg;
    }

    /**
     * convertToMMessage
     *
     * @param messageMap
     * @return
     */
    public MMessage convertToMMessage(HashMap<String, Object> messageMap) {
        logger.info("convertToMMessage content:{}", messageMap);
        Integer id = (Integer) messageMap.get("id");
        String title = messageMap.get("title") == null ? "" : messageMap.get("title").toString();
        String target = messageMap.get("target") == null ? "" : messageMap.get("target").toString();
        //其他参数，该字段保存的值为json形式,需要转成map
        String params = messageMap.get("params") == null ? "" : messageMap.get("params").toString();
        String onLineTime = messageMap.get("on_line_time") == null ? "" : messageMap.get("on_line_time").toString();
        String offLineTime = messageMap.get("off_line_time") == null ? "" : messageMap.get("off_line_time").toString();
        String padImageUrl = messageMap.get("pad_image_url") == null ? "" : messageMap.get("pad_image_url").toString();
        String phoneImageUrl = messageMap.get("image") == null ? "" : messageMap.get("image").toString();
        Integer cateId = MapUtils.getInteger(messageMap, "cate_id", 0);
        Integer subject = MapUtils.getInteger(messageMap, "subject", 0);
        Integer type = (Integer) messageMap.get("type");
        Map<String, Object> defaultMap = Maps.newHashMap();
        defaultMap.put("mId", 0);
        defaultMap.put("mTitle", "");
        //位置
        int position = (Integer) messageMap.get("position");
        Map paramsMap = new HashMap<>();
        //防止错误的json
        try {
            //如果字段值非空
            if (StringUtils.isNoneBlank(params)) {
                paramsMap = JsonUtil.toMap(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //转换失败直接返回null
            return null;
        }

        //组装参数
        Map m = new HashMap<>();
        m.put("id", id);
        m.put("title", title);
        m.put("padImageUrl", padImageUrl);
        m.put("phoneImageUrl", phoneImageUrl);
        m.put("cateId", cateId);
        m.put("cateName", "");
        m.put("subject", subject);
        m.putAll(paramsMap);

        String mParams = MapUtils.getString(messageMap, "m_params", "");
        logger.info("convertToMMessage.mParams.value:{}", mParams);
        if (StringUtils.isNotEmpty(mParams)) {
            JSONObject mParamsObject = JSONObject.parseObject(mParams);
            logger.info("convertToMMessage.mParamsObject.value:{}", mParamsObject.toJSONString());
            m.putAll(mParamsObject);
        } else {
            m.putAll(defaultMap);
        }

        MMessage msg = MMessage.builder()
                .mParams(m)
                .target(target)
                .onLineTime(Long.parseLong(onLineTime))
                .offLineTime(Long.parseLong(offLineTime))
                .type(type)
                .build();
        return msg;
    }


    /**
     * 轮播图白名单
     */
    public void filterWhiteUserName(List<Message> result) {

        final ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String tempIds = valueOperations.get("tempId");
        if (StringUtils.isEmpty(tempIds)) {
            return;
        }

        List<HashMap<String, Object>> messageMaps = advertMessageDao.findAdvert(tempIds);
        if (null == result) {
            result = Lists.newArrayList();
        }
        System.out.print("白名单广告是:{}" + JsonUtil.toJson(messageMaps));
        if (CollectionUtils.isNotEmpty(messageMaps)) {
            for (HashMap<String, Object> messageMap : messageMaps) {
                Message message = convertToMessage(messageMap);
                result.add(message);
            }
        }
    }

}
