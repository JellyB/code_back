package com.huatu.tiku.essay.service.impl;

import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.repository.EssayLabelDetailRepository;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import com.huatu.tiku.essay.service.LabelXmlService;
import com.huatu.tiku.essay.util.file.LabelXmlUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-08-30  09:34 .
 */
@Service
public class LabelXmlServiceImpl implements LabelXmlService {
    @Autowired
    private EssayLabelTotalRepository totalRepository;

    @Autowired
    private EssayLabelDetailRepository detailRepository;

    @Autowired
    private LabelXmlUtil labelXmlUtil;
    @Override
    public String findTotalAndProduceXml(long id) {
        EssayLabelTotal total = totalRepository.findOne(id);
        List<EssayLabelDetail> essayLabelDetails = detailRepository.findByTotalIdAndStatus(id, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        List<EssayLabelDetail> detailList = new LinkedList<>();
        EssayLabelDetail titleLabel = new EssayLabelDetail();
        if(CollectionUtils.isNotEmpty(essayLabelDetails)){
            for(EssayLabelDetail detail:essayLabelDetails){
                if(StringUtils.isNotEmpty(detail.getTitleScore())){
                    titleLabel = detail;
                }else{
                    detailList.add(detail);
                }
            }
        }
        return labelXmlUtil.produceXml(total,detailList,titleLabel);
    }
}
