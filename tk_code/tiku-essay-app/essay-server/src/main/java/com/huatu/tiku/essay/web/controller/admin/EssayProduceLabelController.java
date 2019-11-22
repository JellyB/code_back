package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.essay.service.LabelXmlService;
import com.huatu.tiku.essay.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaoxi
 * @Description: 批注测试工具类
 * @date 2018/10/14下午8:34
 */
@RestController
@Slf4j
@RequestMapping("/end/produce/label")
public class EssayProduceLabelController {

    @Autowired
    private LabelXmlService labelXmlService;
    /**
     * 根据id查询批注xml
     */
    @LogPrint
    @PostMapping(value = "",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public  Object findTotalAndProduceXml(@RequestParam long id){
        return labelXmlService.findTotalAndProduceXml(id);
    }
}
