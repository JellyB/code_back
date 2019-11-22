package com.huatu.tiku.essay.vo.statistics;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author zhangchong
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockUserVO {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户昵称
     */
    private String nick;
    
    private String userName;
    /**
     * 用户手机
     */
    private String mobile;
    
    /**
     * 报名时间
     */
    private Date enrollTime;
    
    /**
     * 开始考试时间
     */
    private Date startTime;
   
}
