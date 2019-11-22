package com.huatu.tiku.essay.vo.resp.correct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: UserCorrectTimesVO
 * @description: 用户各种类型批改次数返回值
 * @date 2019-07-0821:51
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCorrectTimesVO {

    /**
     * 智能批改次数
     */
    private List<CorrectTimesSimpleVO> machineCorrect;

    /**
     * 人工批改次数
     */
    private List<CorrectTimesSimpleVO> manualCorrect;


    private int totalNum;
    
    /**
     * 老师工作量是否饱和
     */
    private boolean canCorrect;
    /**
     * 饱和时的客户端描述
     */
    private String correctDesc;
}
