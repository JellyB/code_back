package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/6 14:00
 * @Description
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackDto {
    private Long total;
    private Long totalPage;
    private Long next;
    private List<Feedback> feedbacks;
}

