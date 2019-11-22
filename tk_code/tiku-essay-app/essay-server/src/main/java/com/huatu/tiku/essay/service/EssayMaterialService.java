package com.huatu.tiku.essay.service;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.entity.EssayMaterial;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.admin.AdminMaterialListVO;
import com.itextpdf.text.BadElementException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by huangqp on 2017\12\5 0005.
 */
public interface EssayMaterialService {
    //获取所有单题信息
    List<EssayMaterialVO> findMaterialsByPaperId(long paperId);
    //新增资料
    EssayMaterialVO insertMaterial(EssayMaterialVO essayMaterial, String userId) throws InvocationTargetException, IllegalAccessException, BadElementException, BizException, IOException;

    List<EssayMaterial> saveMaterial(AdminMaterialListVO adminMaterialVO, String userId);
}
