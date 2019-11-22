package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.interview.entity.po.PracticeRemark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/4/16.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RemarkListVO {
    //优点
    private List<PracticeRemark> advantageList;
    //问题
    private List<PracticeRemark> disAdvantageList;

}
