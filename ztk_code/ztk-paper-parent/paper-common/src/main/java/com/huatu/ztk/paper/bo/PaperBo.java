package com.huatu.ztk.paper.bo;

import com.huatu.ztk.knowledge.bean.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author shanjigang
 * @date 2019/3/1 15:35
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaperBo {
    /**
     * 知识点模块列表
     */
    private List<Module> modules;

    /**
     * 试题Id
     */
    private List<Integer> questions;

}
