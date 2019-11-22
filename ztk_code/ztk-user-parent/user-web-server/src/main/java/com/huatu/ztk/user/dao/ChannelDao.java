package com.huatu.ztk.user.dao;

import com.google.common.base.Stopwatch;
import com.huatu.ztk.user.bean.AppChannel;
import com.huatu.ztk.user.common.DeviceTokenState;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author zhouwei
 * @Description: 统计什么什么下载渠道
 * @create 2018-06-07 上午11:33
 **/
@Repository
public class ChannelDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    private org.slf4j.Logger logger = LoggerFactory.getLogger(ChannelDao.class);

    /**
     * 上报idfa值
     *
     * @param channel
     * @param ip
     * @param gmtCreate
     * @param state
     * @param source
     * @return
     */
    public void insertChannel(String channel, String ip, long gmtCreate, int state, int source, int type, String version,
                              String model, String cv, Boolean isCool, int isBreakPrison) {
        String table = judgeSystemTable(isCool);
        Stopwatch stopwatch = Stopwatch.createStarted();
        String updateTime = convertDate(new Date());
        String createTime = convertDate(new Date());

        String sql = "insert into " + table + "(device_token,ip,gmt_create,state,source,update_time,create_time,source_type,version,model,cv,is_break_prison) " +
                "values(?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new Object[]{channel, ip, gmtCreate, state, source, updateTime, createTime, type, version, model, cv,isBreakPrison});
        logger.info("stopwatch spentTime:{}", String.valueOf(stopwatch.stop()));

    }

    /**
     * 多渠道排重，查询token值是否重复
     *
     * @param
     * @return
     */
    public AppChannel selectSourceChannel(String token, Boolean isCool) {
        String table = judgeSystemTable(isCool);
        String sql = " SELECT a.device_token,a.state,a.update_time,a.call_back from " + table
                + " as a  where  a.device_token = ?";
        String[] params = {token};
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, params);

        while (sqlRowSet.next()) {
            AppChannel appChannel = new AppChannel();
            String deviceToken = sqlRowSet.getString("device_token");
            Timestamp updateTime = sqlRowSet.getTimestamp("update_time");
            int channelState = sqlRowSet.getInt("state");
            String callBack = sqlRowSet.getString("call_back");
            appChannel.setDeviceToken(deviceToken);
            appChannel.setUpdateTime(updateTime);
            appChannel.setState(channelState);
            appChannel.setCallBack(callBack);
            return appChannel;
        }
        return null;
    }


    /**
     * 将状态更新为激活
     *
     * @param token
     * @return
     */
    public int updateChannelSateByToken(String token, String version, String model, String ip, String cv) {
        String createTime = convertDate(new Date());
        String sql = "UPDATE app_channel as ch set ch.state=?,ch.create_time =?,ch.version=?,ch.model=?,ch.ip=?,ch.cv=? where ch.device_token=?";
        Object[] params = {
                DeviceTokenState.ACTIVE_STATE,
                createTime,
                version,
                model,
                ip,
                cv,
                token
        };
        return jdbcTemplate.update(sql, params);
    }


    /**
     * 根据传递不同参数,更新不同字段
     *
     * @param appChannel
     * @return
     */
    public int updateChannelByParams(AppChannel appChannel) {
        String updateTime = convertDate(new Date());
        StringBuffer sql = new StringBuffer();
        sql.append("update app_channel as ch set");
        sql.append(" ch.update_time=").append(buildParamStr(updateTime));

        if (StringUtils.isNotEmpty(appChannel.getCallBack())) {
            sql.append(" ,ch.call_back=").append(buildParamStr(appChannel.getCallBack()));
        }
        if (0 != appChannel.getSource()) {
            sql.append(" ,ch.source=").append(appChannel.getSource());
        }
        sql.append(" where ch.device_token =").append(buildParamStr(appChannel.getDeviceToken()));
        logger.info("更新sql是：{}", sql);
        return jdbcTemplate.update(sql.toString());

    }

    public String buildParamStr(String content) {
        StringBuffer stringBuffer = new StringBuffer();
        return stringBuffer.append("'").append(content)
                .append("'").toString();

    }

    public String convertDate(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }


    /**
     * 判断系统类别,查询不同的表
     *
     * @param isCool
     * @return
     */
    public String judgeSystemTable(Boolean isCool) {
        if (isCool) {
            return DeviceTokenState.MIAN_KU;
        }
        return DeviceTokenState.HUA_TU_ONLINE;
    }
}
