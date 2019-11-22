package com.huatu.tiku.essay.essayEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 描述：大纲返回课件 videoType 枚举
 *
 * @author biguodong
 * Create time 2019-03-21 2:44 PM
 **/

public enum CourseWareTypeEnum {
    ;

    /**
     * 转换大纲列表中的 video type 为 php 课件绑定表 type
     * 只要不是 2 的统一换成直播
     * @param videoType
     * @return
     */
    public static int changeVideoType2TableCourseType(int videoType){
        VideoTypeEnum videoTypeEnum = VideoTypeEnum.create(videoType);
        if(videoTypeEnum != VideoTypeEnum.LIVE){
            return TableCourseTypeEnum.RECORD.type;
        }else{
            return TableCourseTypeEnum.LIVE.type;
        }
    }

    /**
     * 课件绑定表 type
     */
    @AllArgsConstructor
    @Getter
    public enum TableCourseTypeEnum {
        /**
         * php 录播表
         */
        RECORD("录播", 1),
        /**
         * php 直播表
         */
        LIVE("直播", 2);


        private String value;
        private int type;
    }

    /**
     * 大纲列表 videoType
     */
    @AllArgsConstructor
    @Getter
    public enum  VideoTypeEnum{
        /**
         * 点播（录播）
         */
        DOT_LIVE("点播", 1),
        /**
         * 直播
         */
        LIVE("直播", 2),
        /**
         * 直播回放
         */
        LIVE_PLAY_BACK("直播回放", 3),
        /**
         * 阶段测试
         */
        PERIOD_TEST("阶段测试", 4),

        /**
         * 音频课件
         */
        AUDIO_WARE("音频课件", 5);

        private String value;
        private int videoType;

        public static VideoTypeEnum create (int videoType){
            for (VideoTypeEnum videoTypeEnum : values()) {
                if(videoTypeEnum.getVideoType() == videoType){
                    return videoTypeEnum;
                }
            }
            return null;
        }
    }
}
