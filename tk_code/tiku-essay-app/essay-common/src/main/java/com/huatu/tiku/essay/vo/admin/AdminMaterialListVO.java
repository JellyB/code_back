package com.huatu.tiku.essay.vo.admin;

import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2017/12/13.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMaterialListVO {

    //材料列表
    private List<EssayMaterialVO> materialList;
//    private List<Long> materialIdList;

    //试卷id、
    private long paperId;
    //试卷id、
    private long questionBaseId;

    //用户id
    private int userId;




}
