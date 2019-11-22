package com.huatu.tiku.essay.vo.resp;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.entity.UpdateCorrectTimesOrderVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author x6
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCorrectTimesByUserVO {

    // 课程列表和修改的对应
    List<UpdateCorrectTimesOrderVO> list;
    /** 订单id **/
    private Long orderId;
    /** 用户名称 **/
    private String userName;
    /** 用户名称 **/
    private int userId;

}
