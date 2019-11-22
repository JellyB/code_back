package com.huatu.tiku.essay.service.v2;


import com.huatu.tiku.essay.dto.ImageRollDto;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 2:29 PM
 **/
public interface EssayLabelImageService {


    Object modifyImageRoll(List<ImageRollDto> list);


    Object saveFinalUrl(long imageId, String imgUrl);
}
