package com.huatu.tiku.schedule.biz.service.imple;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.LiveRoom;
import com.huatu.tiku.schedule.biz.repository.LiveRoomRepository;
import com.huatu.tiku.schedule.biz.service.LiveRoomService;

@Service
public class LiveRoomServiceImpl extends BaseServiceImpl<LiveRoom, Long> implements LiveRoomService {

    private final LiveRoomRepository liveRoomRepository;

    @Autowired
    public LiveRoomServiceImpl(LiveRoomRepository liveRoomRepository) {
        this.liveRoomRepository = liveRoomRepository;
    }

    @Override
    public List<LiveRoom> getAvailable(Integer date, Integer timeBegin, Integer timeEnd) {
        // 获取直播间（带有课程明细）
        List<Long> ids = liveRoomRepository.getUnavailableLiveRoomIds(date, timeBegin, timeEnd);//指定时间被使用的直播间ids
        ids.remove(null);
        List<LiveRoom> liveRooms;
        if (null != ids && !ids.isEmpty()) {//如果指定日期有占用的直播间
            liveRooms = liveRoomRepository.getAllByIdNotIn(ids);
        } else {
            liveRooms = liveRoomRepository.findAll();//如果没有查找全部
        }
        // 如果课程明细中有跟当前时间冲突的直接删除
        Iterator<LiveRoom> liveRoomIterator = liveRooms.iterator();
//        while (liveRoomIterator.hasNext()) {
//            LiveRoom liveRoom = liveRoomIterator.next();
//            if (!liveRoom.getCourseLives().isEmpty()) {
//                liveRoom.getCourseLives().forEach(courseDetail -> {
////                    if (courseDetail.getDateInt().equals(date)) {//当天的判断时间
////                        if ((timeBegin > courseDetail.getTimeBegin() && timeBegin < courseDetail.getTimeEnd())
////                                || (timeEnd > courseDetail.getTimeBegin() && timeEnd < courseDetail.getTimeEnd())
////                                || timeBegin < courseDetail.getTimeBegin() && timeEnd > courseDetail.getTimeBegin()) {
////                            liveRoomIterator.remove();
////                        }
////                    }
////                    courseDetail.setLiveRoom(null);//清空嵌套对象
//                });
//            }
//        }
        return liveRooms;
    }

}
