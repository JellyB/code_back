package com.huatu.ztk.search.dao;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.search.bean.KeywordSearchBean;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by renwenlong on 2016/9/8.
 */
@Repository("CourseKeyWordDao")
public class CourseKeyWordDao {
    private static final Logger logger = LoggerFactory.getLogger(CourseKeyWordDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;


    /**
     * 保存搜索记录
     *
     * @param searchBean
     */
    public int insert(KeywordSearchBean searchBean) {
        Object[] params = {
                searchBean.getUid(),
                searchBean.getCatgory(),
                searchBean.getKeyword(),
                searchBean.getCount(),
                searchBean.getUpdateTime()
        };
        String savesql = "INSERT INTO v_search_keywords(uid,catgory,keyword,count,update_time) VALUES (?,?,?,?,?)";
        final int count = jdbcTemplate.update(savesql, params);
        logger.info("succeed number={}, insert searchRecord={}", count, JsonUtil.toJson(searchBean));
        return count;
    }

    /**
     * 更新搜索记录
     *
     * @param searchBean
     */
    public void update(KeywordSearchBean searchBean) {
        Object[] params = {
                searchBean.getCount(),
                searchBean.getUpdateTime(),
                searchBean.getUid(),
                searchBean.getCatgory(),
                searchBean.getKeyword()
        };
        String updatesql = "UPDATE v_search_keywords set count=?,update_time =? where uid=? and catgory =? and keyword =?";
        jdbcTemplate.update(updatesql, params);
        logger.info("update keyword search record succeed,keyword={}", searchBean.getKeyword());
    }

    /**
     * 查询某关键词历史搜索
     *
     * @param userId
     * @param catgory
     * @param q
     * @return
     */
    public KeywordSearchBean query(long userId, int catgory, String q) {
        Object[] params = {
                q,
                userId,
                catgory
        };
        String sql = "SELECT * FROM v_search_keywords where keyword=? and uid =? and catgory=?";
        final KeywordSearchBean result = findObject(sql, params, new KeywordSearchBeanMapper());
        return result;
    }

    /**
     * 查询我的历史搜索
     *
     * @param userId
     * @param catgory
     */
    public List<KeywordSearchBean> queryMyWords(long userId, int catgory) {
        Object[] params = {
                userId,
                catgory
        };
        String sql = "SELECT * FROM v_search_keywords sk where sk.uid =? and sk.catgory=? order by update_time DESC limit 10";
        List<KeywordSearchBean> results = jdbcTemplate.query(sql, params, new KeywordSearchBeanMapper());
        //假如查询历史为空，返回空list
        if (CollectionUtils.isEmpty(results)) {
            results = new ArrayList<>();
        }
        results.sort((a, b) -> (int) (b.getUpdateTime() - a.getUpdateTime()));
        return results;
    }

    /**
     * 删除关键字搜索记录
     *
     * @param userId
     * @param catgory
     * @param q
     * @return
     */
    public int delete(long userId, int catgory, String q) {
        Object[] params = {
                q,
                userId,
                catgory
        };
        String sql = "DELETE FROM v_search_keywords where keyword=? and uid =? and catgory=?";
        final int count = jdbcTemplate.update(sql, params);
        return count;
    }

    private <T> T findObject(String sql, Object[] params, RowMapper<T> mapper) {
        final java.util.List<T> results = jdbcTemplate.query(sql, params, mapper);
        T ret = null;
        if (results != null && results.size() > 0) {
            ret = results.get(0);
        }
        return ret;
    }

    /**
     * 清空某用户搜索记录
     *
     * @param userId
     * @param catgory
     */
    public int clearAllKeywords(long userId, int catgory) {
        Object[] params = {
                userId,
                catgory
        };
        String sql = "DELETE FROM v_search_keywords where uid =? and catgory=?";
        final int count = jdbcTemplate.update(sql, params);
        logger.info("clear success,uid={},the number deleted={}", userId, count);
        return count;

    }

    class KeywordSearchBeanMapper implements RowMapper<KeywordSearchBean> {
        public KeywordSearchBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            final KeywordSearchBean keywordSearchBean = KeywordSearchBean.builder()
                    .uid(rs.getLong("uid"))
                    .catgory(rs.getInt("catgory"))
                    .keyword(rs.getString("keyword"))
                    .count(rs.getInt("count"))
                    .updateTime(rs.getLong("update_time"))
                    .build();
            return keywordSearchBean;
        }
    }

}
