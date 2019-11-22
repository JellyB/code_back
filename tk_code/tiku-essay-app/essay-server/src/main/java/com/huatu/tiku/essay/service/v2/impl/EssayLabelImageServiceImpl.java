package com.huatu.tiku.essay.service.v2.impl;

import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.dto.ImageRollDto;
import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.repository.v2.EssayLabelImageRepository;
import com.huatu.tiku.essay.service.v2.EssayLabelImageService;
import com.huatu.tiku.essay.util.file.UploadFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.huatu.tiku.essay.util.file.FunFileUtils.MANUAL_CORRECT_SAVE_PATH;
import static com.huatu.tiku.essay.util.file.FunFileUtils.MANUAL_CORRECT_SAVE_URL;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 2:35 PM
 **/

@Slf4j
@Service
public class EssayLabelImageServiceImpl implements EssayLabelImageService {


    @Autowired
    private EssayLabelImageRepository essayLabelImageRepository;


    @Override
    public Object modifyImageRoll(List<ImageRollDto> list) {
        if(CollectionUtils.isEmpty(list)){
            throw new BizException(ErrorResult.create(1000010, "数据为空！"));
        }
        for(ImageRollDto imageRollDto : list){
            CorrectImage correctImage = essayLabelImageRepository.findOne(imageRollDto.getImageId());
            if (null == correctImage) {
                throw new BizException(ErrorResult.create(1000010, "图片不存在！"));
            }
            essayLabelImageRepository.modifyImageRollById(imageRollDto.getRoll(), new Date(), imageRollDto.getImageId());
        }
        return SuccessMessage.create();
    }


    @Override
    public Object saveFinalUrl(long imageId, String imgUrl) {
        CorrectImage correctImage = essayLabelImageRepository.findOne(imageId);
        if (null == correctImage) {
            throw new BizException(ErrorResult.create(1000010, "图片不存在！"));
        }
        essayLabelImageRepository.updateImageFinalUrl(imgUrl, new Date(), imageId);
        return SuccessMessage.create();
    }


}
