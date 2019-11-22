package com.huatu.tiku.essay.web.controller.admin.v1;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.dto.ImageRollDto;
import com.huatu.tiku.essay.service.v2.EssayLabelImageService;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 1:36 PM
 **/

@RequestMapping(value = "end/v1/label/img")
@Controller
public class EssayLabelImageControllerV1 {


    @Autowired
    private EssayLabelImageService essayLabelImageService;

    /**
     * 图片旋转保存
     * @param list
     * @return
     * @throws BizException
     */
    @PostMapping("/roll")
    @ResponseBody
    public Object roll(@RequestBody List<ImageRollDto> list) throws BizException{

        return essayLabelImageService.modifyImageRoll(list);
    }


    /**
     * 保存最终批注图片url
     * @param imageVO
     * @return
     * @throws BizException
     */
    @PostMapping
    @ResponseBody
    public Object saveFinalUrl(@RequestBody Map imageVO) throws BizException{
        Long imageId = MapUtils.getLong(imageVO, "imageId");
        String imgUrl = MapUtils.getString(imageVO, "imgUrl");
        return essayLabelImageService.saveFinalUrl(imageId, imgUrl);
    }
}
