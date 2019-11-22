package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.essay.entity.EssayMaterial;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.admin.AdminMaterialListVO;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by huangqp on 2017\12\5 0005.
 */
@RestController
@Slf4j
@RequestMapping("/end/material")
public class EssayMaterialController {
    @Autowired
    EssayMaterialService essayMaterialService;


    /**
     * 查询试卷材料
     * @param paperId
     * @return
     */
    @LogPrint
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayMaterialVO> findMaterialsByPaperId(@RequestParam long paperId){
        return essayMaterialService.findMaterialsByPaperId(paperId);
    }

//    /**
//     * 插入单个材料
//     * @throws InvocationTargetException
//     * @throws IllegalAccessException
//     */
//    @LogPrint
//    @PostMapping(value="single",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public EssayMaterialVO addMaterial(@RequestBody  EssayMaterialVO essayMaterial) throws InvocationTargetException, IllegalAccessException {
//        String userId = "";
//        try{
//            return essayMaterialService.insertMaterial(essayMaterial,userId);
//        }catch (Exception e){
//            log.error(e.getMessage());
//            e.printStackTrace();
//            throw new BizException(EssayErrors.UPDATE_MATERIAL_ERROR);
//        }
//
//    }

    /**
     *修改材料（删除材料）
     */
    @LogPrint
    @PostMapping(value="materialList",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<EssayMaterial> updateMaterial(@RequestBody AdminMaterialListVO adminMaterialVO) throws InvocationTargetException, IllegalAccessException {
        String userId = "admin";
        return essayMaterialService.saveMaterial(adminMaterialVO,userId);
    }

}
