package com.huatu.hadoop.service;

import com.huatu.common.exception.BizException;
import com.huatu.hadoop.bean.CourseProcessDTO;
import com.huatu.hadoop.constant.HBaseErrors;
import com.huatu.hadoop.util.HBaseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.util.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by x6 on 2018/5/14.
 */
@Service
@Slf4j
public class VideoProcessService {

    /**
     * 保存播放进度
     *
     * @param courseProcessDTO
     * @param terminal
     * @param uname            @return
     */
    public boolean saveProcess(CourseProcessDTO courseProcessDTO, String uname, int terminal, String cv) {
        boolean put = false;
        String rowKey = getRowKey(courseProcessDTO, uname);

        try {
            //填充播放时间，客户端，APP版本，信息
            Map<String, Object> map = new HashMap<>();
            map.put("terminal", terminal);
            map.put("cv", cv);
//            map.put("playTime", courseProcessDTO.getPlayTime());
//            map.put("wholeTime", courseProcessDTO.getWholeTime());
//            map.put("userPlayTime", courseProcessDTO.getUserPlayTime());
            map.put("playTime", courseProcessDTO.getPlayTime() == null ? 0 : courseProcessDTO.getPlayTime());
            map.put("wholeTime", courseProcessDTO.getWholeTime() == null ? 0 : courseProcessDTO.getWholeTime());
            map.put("userPlayTime", courseProcessDTO.getUserPlayTime() == null ? 0 : courseProcessDTO.getUserPlayTime());
            put = HBaseUtil.putMulti("videoplay", rowKey, "playinfo", map);
        } catch (Exception e) {
            log.warn("hbase添加数据异常");
            e.printStackTrace();
        }
        return put;
    }

    /**
     * 查询播放进度
     *
     * @param uname
     * @return
     */
    public List<CourseProcessDTO> queryProcess(List<CourseProcessDTO> list, String uname) {
        if (!CollectionUtils.isEmpty(list)) {
            for (CourseProcessDTO courseProcessDTO : list) {
                String rowKey = getRowKey(courseProcessDTO, uname);
                //查询该用户所有的视频播放信息
                HBaseUtil hbase = new HBaseUtil();
                Map videoplay = new HashMap();
                try {
                    videoplay = HBaseUtil.get("videoplay", rowKey);
                } catch (Exception e) {
                    log.warn("hbase查询数据异常");
                    e.printStackTrace();
                    throw new BizException(HBaseErrors.QUERY_ERROR);
                }
                Map playinfo = (Map<String, String>) videoplay.get("playinfo");
                if (playinfo == null) {
                    courseProcessDTO.setPlayTime(0L);
                    courseProcessDTO.setWholeTime(0L);
                } else {
                    String playTime = (String) playinfo.get("playTime");
                    String wholeTime = (String) playinfo.get("wholeTime");
                    courseProcessDTO.setPlayTime(Long.parseLong(playTime));
                    courseProcessDTO.setWholeTime(Long.parseLong(wholeTime));
                }

            }
        }
        return list;
    }

    private String getRowKey(CourseProcessDTO courseProcessDTO, String uname) {
        String videoIdWithoutTeacher = courseProcessDTO.getVideoIdWithoutTeacher();
        String videoIdWithTeacher = courseProcessDTO.getVideoIdWithTeacher();
        String roomId = courseProcessDTO.getRoomId();
        String sessionId = courseProcessDTO.getSessionId();
        String joinCode = courseProcessDTO.getJoinCode();

        Long classId = courseProcessDTO.getClassId();
        Long coursewareId = courseProcessDTO.getCoursewareId();


        StringBuilder rowKey = new StringBuilder();
        rowKey.append(uname);

        // 录播视频
        if (StringUtils.isNotEmpty(videoIdWithoutTeacher) || StringUtils.isNotEmpty(videoIdWithTeacher)) {
            if (StringUtils.isNotEmpty(videoIdWithoutTeacher)) {
                rowKey.append("-videoIdWithoutTeacher-").append(videoIdWithoutTeacher);
            } else {
                rowKey.append("-videoIdWithTeacher-").append(videoIdWithTeacher);
            }
            //直播回放
        } else if (StringUtils.isNotEmpty(roomId)) {
            rowKey.append("-roomId-").append(roomId);
            if (StringUtils.isNotEmpty(sessionId)) {
                rowKey.append("-sessionId-").append(sessionId);
            }
            //展示视频
        } else if (StringUtils.isNotEmpty(joinCode)) {
            rowKey.append("-gensee-").append(joinCode);
        } else if (classId != null && coursewareId != null) {

            rowKey.append("-classId-").append(classId).append("-coursewareId-").append(coursewareId);
        } else {
            log.warn("参数错误");
        }
        return rowKey.toString();
    }
}
