package com.huatu.tiku.course.consts;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-21 3:11 PM
 **/

public class SyllabusInfo {

    /**
     * 大纲id
     */
    public static String SyllabusId = "id";

    /**
     * 课程id
     */
    public static String CourseId = "courseId";
    /**
     * 课件id
     */
    public static String CourseWareId = "coursewareId";

    /**
     * 课件类型
     * CourseWareTypeEnum
     */
    public static String VideoType = "videoType";

    /**
     * 科目类型
     * {@link com.huatu.tiku.course.common.SubjectEnum}
     */
    public static String SubjectType = "subjectType";

    /**
     * 查询paper服务所映射的 videoType 字段
     */
    public static String PaperCourseType = "courseType";

    /**
     * 查询paper服务所映射的 courseId 字段
     */
    public static String PaperCourseId = "courseId";

    /**
     * 百家云房间id
     */
    public static String BjyRoomId = "bjyRoomId";

    /**
     * 类型
     */
    public static String Type = "type";

    /**
     * 是否展示学习报告 0 不展示 1 展示
     * {@link com.huatu.tiku.course.common.YesOrNoStatus}
     */
    public static String StudyReport = "studyReport";

    /**
     * 直播是否结束{@link com.huatu.tiku.course.common.LiveStatusEnum}
     */
    public static final String LiveStatus = "liveStatus";

    /**
     * 大纲学习报告生成状态
     */
    public static final String ReportStatus = "reportStatus";

    /**
     * 课后练习数量
     */
    public static final String AfterCourseNum = "afterCoreseNum";

    public static final String NetClassId = "netClassId";

}
