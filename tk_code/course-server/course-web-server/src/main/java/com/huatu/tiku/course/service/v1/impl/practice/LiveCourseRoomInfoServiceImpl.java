package com.huatu.tiku.course.service.v1.impl.practice;

import com.google.common.collect.Lists;
import com.huatu.tiku.course.bean.NetSchoolResponse;
import com.huatu.tiku.course.netschool.api.v6.practice.LiveCourseServiceV6;
import com.huatu.tiku.course.service.v1.practice.LiveCourseRoomInfoService;
import com.huatu.tiku.course.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lijun on 2019/2/21
 */
@Service
@RequiredArgsConstructor
public class LiveCourseRoomInfoServiceImpl implements LiveCourseRoomInfoService {

    private final LiveCourseServiceV6 liveCourseService;

    @Override
    public List<Integer> getLiveCourseIdListByRoomId(Long roomId) {
        NetSchoolResponse liveCourseIdListByRoomIdList = liveCourseService.getLiveCourseIdListByRoomId(roomId);
        ArrayList<Integer> courseIdList = ResponseUtil.build(liveCourseIdListByRoomIdList, ArrayList.class, false);
        if (CollectionUtils.isEmpty(courseIdList)) {
            return Lists.newArrayList();
        }
        return courseIdList;
    }

}
