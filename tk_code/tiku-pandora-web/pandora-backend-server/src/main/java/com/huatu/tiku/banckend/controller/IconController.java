package com.huatu.tiku.banckend.controller;

import com.huatu.tiku.banckend.service.IconService;
import com.huatu.tiku.dto.request.IconDto;
import com.huatu.tiku.teacher.enums.BizStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-29 2:13 PM
 **/

@RestController
@RequestMapping("/backend/icon")
public class IconController {

    @Autowired
    private IconService iconService;


    @GetMapping(value = "/list")
    @ResponseBody
    public Object types(){
        return IconDto.IconType.values();
    }

    /**
     * 添加 icon
     * @param subject
     * @param dtoList
     * @return
     */
    @PostMapping(value = "/{subject}")
    @ResponseBody
    public Object add(@PathVariable(value = "subject") int subject, @RequestBody List<IconDto> dtoList){
        return iconService.add(subject, dtoList);
    }

    /**
     * 修改
     * @param subject
     * @param iconDto
     * @return
     */
    @PutMapping(value = "/{subject}")
    @ResponseBody
    public Object update(@PathVariable(value = "subject") int subject, @RequestBody IconDto iconDto){
        return iconService.update(subject, iconDto);
    }

    /**
     * 查询科目下 icons
     * @param subject
     * @return
     */
    @GetMapping(value = "/{subject}")
    @ResponseBody
    public Object list(@PathVariable int subject){
        return iconService.list(subject);
    }


    /**
     * 上线
     * @param id
     * @return
     */
    @PutMapping(value = "/on/{id}")
    @ResponseBody
    public Object turnOn(@PathVariable(value = "id") Long id){
        return iconService.turn(id, BizStatusEnum.PUBLISH.getValue());
    }

    /**
     * 下线
     * @param id
     * @return
     */
    @PutMapping(value = "/off/{id}")
    @ResponseBody
    public Object turnOff(@PathVariable(value = "id") Long id){
        return iconService.turn(id, BizStatusEnum.NO_PUBLISH.getValue());
    }
}
