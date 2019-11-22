package com.huatu.tiku.essay.constant.course;


import java.io.Serializable;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-11-05 6:24 PM
 **/

public class CallBack implements Serializable{

    private Long roomId;

    private List<Meta> metaList;

    public CallBack() {
    }

    public CallBack(Long roomId, List<Meta> metaList) {
        this.roomId = roomId;
        this.metaList = metaList;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public List<Meta> getMetaList() {
        return metaList;
    }

    public void setMetaList(List<Meta> metaList) {
        this.metaList = metaList;
    }

    public static class Meta{
        /**
         * 直播ID
         */
        private Long liveCourseId;

        /**
         * 回放ID
         */
        private Long recordCourseId;

        public Long getLiveCourseId() {
            return liveCourseId;
        }

        public void setLiveCourseId(Long liveCourseId) {
            this.liveCourseId = liveCourseId;
        }

        public Long getRecordCourseId() {
            return recordCourseId;
        }

        public void setRecordCourseId(Long recordCourseId) {
            this.recordCourseId = recordCourseId;
        }

        public Meta() {
        }

        public Meta(Long liveCourseId, Long recordCourseId) {
            this.liveCourseId = liveCourseId;
            this.recordCourseId = recordCourseId;
        }
    }
}
