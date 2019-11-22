package com.huatu.ztk.user.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperUserMeta;
import com.huatu.ztk.user.bean.Message;
import com.huatu.ztk.user.common.AdvertType;
import com.huatu.ztk.user.daoPandora.AdvertMapper;
import com.huatu.ztk.user.service.UserServerConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 首页广告图 dao层
 * Created by shaojieyue
 * Created time 2016-10-31 15:19
 */

@Repository
public class AdvertMessageDao {
    private static final Logger logger = LoggerFactory.getLogger(AdvertMessageDao.class);
    private static final String ADVERT_PREFIX = "user_advert_";
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserServerConfig userServerConfig;

    @Autowired
    private AdvertMapper advertMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Resource(name = "coreRedisTemplate")
    private ValueOperations valueOperations;

    /*  白名单测试代码  */
    public List<HashMap<String, Object>> findAdvert(String ids) {
        if (StringUtils.isBlank(ids)) {
            return null;
        }
        List<HashMap<String, Object>> result = advertMapper.findByIds(ids);

        return result;
    }

    /**
     * 查询所有有效的广告
     *
     * @param newVersion 1:新版,0:旧版,新版返回大图
     * @param catgory    科目，1为公考
     * @return
     */
    public List<Message> findBannerList(int catgory, int newVersion) {
        //status为1为有效的广告,按序号升序
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM v_advert WHERE status = 1 AND catgory = ? AND type = ? and new_version = ? ");
        /**
         * 组装时间筛选条件
         */
        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sql.append(" AND on_line_time <= '").append(nowDate).append("' ");
        sql.append(" AND off_line_time >= '").append(nowDate).append("' ");

        sql.append(" ORDER BY `index` DESC,id DESC ");

        Object[] params = {
                catgory,
                AdvertType.APP_CAROUSEL_FIGURE,
                newVersion
        };
        List<Message> messages = jdbcTemplate.query(sql.toString(), params, new AdvertMessageMapper());
        //过滤null
        messages.removeIf(i -> i == null);
        return messages;
    }

    /**
     * 统一的查询的方法
     *
     * @param category
     * @param type
     * @param newVersion
     * @param appType
     * @return
     */
    public List<Message> findAdvert(int category, int type, int newVersion, int appType) {
        List<Message> messages = Lists.newArrayList();
        String key = new StringBuilder(ADVERT_PREFIX).append(type).append("_").append(newVersion).append("_").append(appType).append("_").append(category).append("_").toString();
        messages = (List<Message>) valueOperations.get(key);
        //测试环境不走缓存
        if (messages != null && messages.size() > 0 && !userServerConfig.getEnvironment().contains("test")) {
            return messages;
        }
        //status为1为有效的广告,按序号升序
        StringBuilder sql = new StringBuilder("SELECT * FROM v_advert WHERE status = 1 ");

        List<Object> params = new ArrayList<>();
        if (type > 0) {
            sql.append("AND type = ? ");
            params.add(type);
        }

        if (category > 0) {
            sql.append("AND catgory = ? ");
            params.add(category);
        }

        if (newVersion >= 0) {
            sql.append("AND new_version = ? ");
            params.add(newVersion);
        }

        if (appType > 0) {
            sql.append("AND app_type in (0,?) ");
            params.add(appType);
        }

        /**
         * 组装时间筛选条件
         */
        long nowDate = new Date().getTime();
        sql.append(" AND on_line_time <= '").append(nowDate).append("' ");
        sql.append(" AND off_line_time >= '").append(nowDate).append("' ");

        sql.append(" ORDER BY `index` DESC,id DESC ");
        messages = jdbcTemplate.query(sql.toString(), params.toArray(), new AdvertMessageMapper());
        //过滤null
        messages = messages.stream().filter(message -> message != null).collect(Collectors.toList());
        valueOperations.set(key, messages, 6, TimeUnit.MINUTES);
        return messages;
    }

    public static final String getPaperKey(int paperId) {
        return String.format("course_detail$%s", paperId);
    }

    public Paper findPaper(int paperId) {

        String key = getPaperKey(paperId);
        Object object = valueOperations.get(key);

        if (object == null) {
            final Paper paper = mongoTemplate.findById(paperId, Paper.class);
            valueOperations.set(key, paper, 1, TimeUnit.HOURS);
            return paper;
        } else {
            //测试不走缓存
            if (userServerConfig.getEnvironment().contains("test")) {
                return mongoTemplate.findById(paperId, Paper.class);
            }
            return (Paper) object;
        }

    }

    public PaperUserMeta findPaperUserMeta(String id) {
        return mongoTemplate.findById(id, PaperUserMeta.class);
    }

    public List<Message> findLaunchList(int catgory) {
        Object[] params = {
                catgory,
                AdvertType.APP_LAUNCH
        };
        return findMethod(params);
    }

    private List<Message> findMethod(Object[] params) {
        //status为1为有效的广告,按序号升序
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM v_advert WHERE status = 1 AND catgory = ? AND type = ? ");
        /**
         * 组装时间筛选条件
         */
        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sql.append(" AND on_line_time <= '").append(nowDate).append("' ");
        sql.append(" AND off_line_time >= '").append(nowDate).append("' ");

        sql.append(" ORDER BY `index` DESC,id DESC ");
        List<Message> messages = jdbcTemplate.query(sql.toString(), params, new AdvertMessageMapper());
        //过滤null
        messages.removeIf(i -> i == null);
        return messages;
    }

    /**
     * 根据科目查看首页弹出广告图列表
     *
     * @param catgory
     * @return
     */
    public List<Message> findPopupList(int catgory) {
        Object[] params = {
                catgory,
                AdvertType.APP_POPUP
        };
        return findMethod(params);
    }

    /**
     * 网站首页广告
     *
     * @param catgory
     * @return
     */
    public List<Message> findPcHomePageList(int catgory) {
        Object[] params = {
                catgory,
                AdvertType.PC_HOME_PAGE
        };
        return findMethod(params);
    }


    private class AdvertMessageMapper implements RowMapper<Message> {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            Integer id = rs.getInt("id");
            String image = rs.getString("image");
            String title = rs.getString("title");
            String target = rs.getString("target");
            //其他参数，该字段保存的值为json形式,需要转成map
            String params = rs.getString("params");
            Long onLineTime = rs.getLong("on_line_time");
            Long offLineTime = rs.getLong("off_line_time");
            Integer type = rs.getInt("type");
            //位置
            int position = rs.getInt("position");
            Integer cateId = rs.getInt("cate_id");
            Integer subject = rs.getInt("subject");
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
            m.put("cateId", null == cateId ? 0 : cateId);
            m.put("subject", null == subject ? 0 : subject);
            m.putAll(paramsMap);

            Message msg = Message.builder()
                    .params(m)
                    .target(target)
                    .onLineTime(onLineTime)
                    .offLineTime(offLineTime)
                    .type(type)
                    .build();
            return msg;
        }
    }

}

