package com.huatu.tiku.schedule.biz.service.imple;

import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.CourseLive;
import com.huatu.tiku.schedule.biz.domain.CourseLiveTeacher;
import com.huatu.tiku.schedule.biz.domain.VideoRoom;
import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.repository.CourseLiveRepository;
import com.huatu.tiku.schedule.biz.repository.VideoRoomRepository;
import com.huatu.tiku.schedule.biz.service.VideoRoomService;
import com.huatu.tiku.schedule.biz.util.SmsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author wangjian
 **/
@Service
@Slf4j
public class VideoRoomServiceImpl extends BaseServiceImpl<VideoRoom, Long>  implements VideoRoomService {

    private VideoRoomRepository videoRoomRepository;

    private CourseLiveRepository courseLiveRepository;

    @Autowired
    public VideoRoomServiceImpl(VideoRoomRepository videoRoomRepository, CourseLiveRepository courseLiveRepository) {
        this.videoRoomRepository = videoRoomRepository;
        this.courseLiveRepository = courseLiveRepository;
    }

    @Override
    public List<VideoRoom> getVideoRoomList() {
        return videoRoomRepository.findByShowFlagIsTrue();
    }

    @Override
    @Transactional
    public void deleteX(Long roomId, String reason) {
        Map<String, String> map = Maps.newHashMap();
        List<CourseLive> courseLives = courseLiveRepository.findByVideoRoomId(roomId);
        Iterator<CourseLive> iterator = courseLives.iterator();
        while (iterator.hasNext()) {
            CourseLive next = iterator.next();
            if (CourseCategory.VIDEO.equals(next.getCourse().getCourseCategory())) { //录播课
                List<CourseLiveTeacher> courseLiveTeachers = next.getCourseLiveTeachers();
                if (null != courseLiveTeachers && !courseLiveTeachers.isEmpty()) {//没有子集直接删除
                    for (CourseLiveTeacher clt : courseLiveTeachers) {
                        String name = clt.getTeacher().getName();
                        String phone = clt.getTeacher().getPhone();
                        map.put(phone, name);
                    }
                }
                iterator.remove();
                courseLiveRepository.delete(next);
            }
        }
        VideoRoom room = videoRoomRepository.findOne(roomId);
        for (String phone : map.keySet()) {
            StringBuffer sb = new StringBuffer();
            sb.append(map.get(phone));
            sb.append("老师,\"");
            sb.append(room.getName());
            sb.append("\"");
            if (StringUtils.isNotBlank(reason)) {
                sb.append("因\"" + reason + "\"");
            }
            sb.append("已删除");
            SmsUtil.sendSms(phone, sb.toString());
            log.info("发送取消排课短信 : {} -> {}", phone, sb.toString());
        }
        videoRoomRepository.delete(roomId);
    }
}

