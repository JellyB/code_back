package com.huatu.tiku.course.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author hanchao
 * @date 2017/9/14 15:19
 */
@Data
public class CourseDetailV3DTO implements Serializable{
    private static final long serialVersionUID = 1L;
    /**
     * classInfo : {"stopTime":"","limitStatus":9,"TimeLength":"136","limitUserCount":0,"rid":"53306","TypeName":"套餐方案","limitTimes":0,"Title":"2017年江苏省公务员考试红领全程套餐A","scaleimg":"http://upload.htexam.net/classimg/class/1467601327.jpg","startTime":1,"ActualPrice":3180,"total":"350","studyDate":"9月23日-1月1日","isBuy":0}
     * teacherInfo : [{"TeacherName":"李委明","roundPhoto":"http://upload.htexam.net/teacherphoto/1506300453473100.jpg","TeacherId":"2"},{"TeacherName":"蔡金龙","roundPhoto":"http://upload.htexam.net/teacherphoto/1506300458302951.jpg","TeacherId":"5"},{"TeacherName":"罗红军","roundPhoto":"http://upload.htexam.net/teacherphoto/15063005013215711.jpg","TeacherId":"81"},{"TeacherName":"郜爽","roundPhoto":"http://upload.htexam.net/teacherphoto/15063004530013647.jpg","TeacherId":"114"},{"TeacherName":"刘有珍","roundPhoto":"http://upload.htexam.net/teacherphoto/1495613716.jpg","TeacherId":"157"},{"TeacherName":"韩利亚","roundPhoto":"http://upload.htexam.net/teacherphoto/1507030901072390.jpg","TeacherId":"203"},{"TeacherName":"省丽丽","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"218"},{"TeacherName":"孔令昂","roundPhoto":"http://upload.htexam.net/teacherphoto/15063005050917949.jpg","TeacherId":"353"},{"TeacherName":"胡泊","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"428"},{"TeacherName":"赵寰宇","roundPhoto":"http://upload.htexam.net/teacherphoto/1507030912356288.jpg","TeacherId":"445"},{"TeacherName":"李梦娇","roundPhoto":"http://upload.htexam.net/teacherphoto/15063005021117508.jpg","TeacherId":"448"},{"TeacherName":"张蕊","roundPhoto":"http://upload.htexam.net/teacherphoto/15063005002111436.jpg","TeacherId":"453"},{"TeacherName":"贾文博","roundPhoto":"http://upload.htexam.net/teacherphoto/1509110316183566.jpg","TeacherId":"470"},{"TeacherName":"郝曜华","roundPhoto":"http://upload.htexam.net/teacherphoto/1458609619.jpg","TeacherId":"500"},{"TeacherName":"贺瑞锐","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"516"},{"TeacherName":"王铁红","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"618"},{"TeacherName":"丁溆","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"620"},{"TeacherName":"赵晶","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"642"},{"TeacherName":"马老师","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"655"},{"TeacherName":"徐文婷","roundPhoto":"http://upload.htexam.net/teacherphoto/1453944776.jpg","TeacherId":"668"},{"TeacherName":"朱晓丹","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"800"},{"TeacherName":"程永乐","roundPhoto":"http://upload.htexam.net/teacherphoto/1509150352152633.jpg","TeacherId":"818"},{"TeacherName":"华图老师","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"1094"},{"TeacherName":"王凌燕","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"1112"},{"TeacherName":"黄晓雯","roundPhoto":"http://v.huatu.com/images/default_teacher.jpg","TeacherId":"1234"}]
     */

    private ClassInfo classInfo;
    private List<TeacherInfo> teacherInfo;


    @Data
    public static class ClassInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String stopTime;
        private int limitStatus;
        @JsonProperty
        private String TimeLength;
        private Integer limitUserCount;
        private String rid;
        @JsonProperty
        private String TypeName;
        private int limitTimes;
        @JsonProperty
        private String Title;
        private String scaleimg;
        private String startTime;
        @JsonProperty
        private int ActualPrice;
        private String price;
        private Integer total;
        private String studyDate;
        private int isBuy;
        private int courseType;
        private String isSuit;
        private String isMianshou;
        private boolean hasTrial;
        private int purchasType;//

        private int isTermined;
        private String terminedDesc;
        private int isProvincialFaceToFace;
        
        @JsonProperty
        private int Province;
        //订单id
        private String orderId;
        private int isShiWu;
        private int isRushOut;
        private int isSaleOut;

        private List<TeacherInfo> teacherInfo;
    }

    @Data
    public static class TeacherInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * TeacherName : 李委明
         * roundPhoto : http://upload.htexam.net/teacherphoto/1506300453473100.jpg
         * TeacherId : 2
         */

        private String TeacherName;
        private String roundPhoto;
        private String TeacherId;

    }
}
