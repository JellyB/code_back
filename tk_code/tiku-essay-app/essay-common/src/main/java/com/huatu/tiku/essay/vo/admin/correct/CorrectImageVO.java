package com.huatu.tiku.essay.vo.admin.correct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangqingpeng
 * @title: CorrectImageVO
 * @description: 图片封装类
 * @date 2019-07-0914:01
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CorrectImageVO {

    /**
     * 图片ID
     */
    private long id;
    /**
     * 源图片地址
     */
    private String imageUrl;

    /**
     * 答题卡ID
     */
    private long questionAnswerId;

    /**
     * 图片顺序
     */
    private int sort;

    /**
     * 反转角度（0正常、1.90度、2.180度、3.270度）
     */
    private int roll;

    /**
     * 批注完成后带批注截图的url
     */
    private String finalUrl;

    /**
     * 图片坐标问题
     */
    private String imageAllAxis;

    /**
     * 图片内容
     */
    private String content;

}
