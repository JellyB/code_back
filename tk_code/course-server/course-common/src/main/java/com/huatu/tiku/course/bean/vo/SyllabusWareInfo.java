package com.huatu.tiku.course.bean.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-05 5:35 PM
 **/


@NoArgsConstructor
@Getter
@Setter
public class SyllabusWareInfo {
    private String className;
    private Long classId;
    private Long syllabusId;
    private String coursewareName;
    private Long coursewareId;
    private Integer serialNumber;
    private Integer videoType;
    private String length;
    private String roomId;
    private Integer afterCoreseNum;
    private Integer classExercisesNum;
    // 1 行测 2 申论
    private Integer subjectType;
    // 0 单题 1 套题
    private Integer buildType;

    @Builder
    public SyllabusWareInfo(String className, Long classId, Long syllabusId, String coursewareName, Long coursewareId, Integer serialNumber, Integer videoType, String length, String roomId, Integer afterCoreseNum, Integer classExercisesNum, Integer subjectType, Integer buildType) {
        this.className = className;
        this.classId = classId;
        this.syllabusId = syllabusId;
        this.coursewareName = coursewareName;
        this.coursewareId = coursewareId;
        this.serialNumber = serialNumber;
        this.videoType = videoType;
        this.length = length;
        this.roomId = roomId;
        this.afterCoreseNum = afterCoreseNum;
        this.classExercisesNum = classExercisesNum;
        this.subjectType = subjectType;
        this.buildType = buildType;
    }

    /**
     * 新版大纲内容检查 - 课后作业提供
     * @param syllabusWareInfo
     * @return
     */
    public static boolean cacheCheck(SyllabusWareInfo syllabusWareInfo){
        if(null == syllabusWareInfo.getSubjectType() || null == syllabusWareInfo.getBuildType() || StringUtils.isEmpty(syllabusWareInfo.getRoomId())){
            return false;
        }else{
            return true;
        }
    }
}
