package com.huatu.tiku.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 课件关联题目批量导入-VO
 * @date 2018/12/284:13 PM
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class CourseQuestionImportVO {


    //文件地址
    private String path;
    //文件类型（1课后作业 2直播随堂练习 3录播课随堂练习）
    private int type;
    //异常用户列表
    private List<String> errorList;

    //操作人
    private String creator;

}
