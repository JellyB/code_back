package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 批注详情接口
 * @date 2018/7/13下午5:23
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayLabelInfoVO {
    /*
     * 综合批注
     */
    private EssayLabelTotal total;

    /*
     * 详细批注的列表
     */
    private List<EssayLabelDetail> detailList;

    /*
     * 标题批注
     */
    private List<EssayLabelDetail> titleList;

}
