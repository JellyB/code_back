package com.huatu.tiku.essay.vo.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/7 15:06
 * @Modefied By:
 */
@Data
@NoArgsConstructor
public class EssayMockStatisticsVO {
    private EssayResultInfoVO essayResultInfoVO;
    private List<Integer> scoreRange;
//    private List<UserDto> users;

}
