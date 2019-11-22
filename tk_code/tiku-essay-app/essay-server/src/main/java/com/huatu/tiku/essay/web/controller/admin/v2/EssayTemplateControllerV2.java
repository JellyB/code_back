package com.huatu.tiku.essay.web.controller.admin.v2;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.dto.CommentTemplateDto;
import com.huatu.tiku.essay.service.v2.EssayTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 3:03 PM
 **/
@RequestMapping(value = "/end/v2/template")
@Controller
public class EssayTemplateControllerV2 {


    @Autowired
    private EssayTemplateService essayTemplateService;


    /**
     * 查询所有的评语模板
     * @param type
     * @return
     * @throws BizException
     */
    @GetMapping
    @ResponseBody
    public Object findAllByType(@RequestParam(value = "type") int type)throws BizException{
        return essayTemplateService.findAllTemplateByType(type);
    }


    /**
     * 模板保存
     * @param dto
     * @return
     * @throws BizException
     */
    @PostMapping
    @ResponseBody
    public Object save(@RequestBody CommentTemplateDto dto) throws BizException{
        return essayTemplateService.save(dto);
    }


    /**
     * 模板 - 删除
     * @param templateId
     * @return
     * @throws BizException
     */
    @DeleteMapping("/{templateId}")
    @ResponseBody
    public Object removeLogic(@PathVariable(value = "templateId") long templateId) throws BizException {
        return essayTemplateService.removeLogic(templateId);
    }
    

}
