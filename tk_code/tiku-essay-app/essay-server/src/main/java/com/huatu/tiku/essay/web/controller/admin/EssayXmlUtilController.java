package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.service.impl.LabelXmlServiceImpl;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.file.LabelXmlUtil;
import com.itextpdf.text.DocumentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-08-15  15:51 .
 */
@RestController
@Slf4j
@RequestMapping("pku/xml")
public class EssayXmlUtilController {
    @Autowired
    LabelXmlUtil labelXmlUtil;
    @Autowired
    LabelXmlServiceImpl labelXmlService;

    /**
     *  数据统计 - 单题列表
     * @return 分页之后的结果
     */
    @LogPrint
    @GetMapping(value = "produceXml", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object testXml(@RequestParam long id) throws DocumentException {
        EssayLabelTotal total = new EssayLabelTotal();
        List<EssayLabelDetail> detailList = new LinkedList<>();
        EssayLabelDetail titleLabel = new EssayLabelDetail();
//        labelXmlService.findTotalAndProduceXml(id);
        return labelXmlService.findTotalAndProduceXml(id);
        //labelXmlUtil.produceXml(total,detailList,titleLabel,id);
    }
}
