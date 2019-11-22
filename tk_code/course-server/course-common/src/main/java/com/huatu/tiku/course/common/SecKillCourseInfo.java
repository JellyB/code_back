package com.huatu.tiku.course.common;

import java.io.Serializable;

/**
 * 描述：秒杀课
 *
 * @author biguodong
 * Create time 2019-04-25 2:16 PM
 **/
public class SecKillCourseInfo implements Serializable {


    private String classId;
    private boolean isRushOut;
    private boolean isSaleOut;
    private int limit;

    private SecKillCourseInfo(String classId, boolean isRushOut, boolean isSaleOut, int limit) {
        this.classId = classId;
        this.isRushOut = isRushOut;
        this.isSaleOut = isSaleOut;
        this.limit = limit;
    }

    private SecKillCourseInfo(String classId, int limit) {
        this.classId = classId;
        this.limit = limit;
    }

    private SecKillCourseInfo(){

    }

    private static final SecKillCourseInfo instance = new SecKillCourseInfo("0", 0);

    public static SecKillCourseInfo getInstance(){
        return instance;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
