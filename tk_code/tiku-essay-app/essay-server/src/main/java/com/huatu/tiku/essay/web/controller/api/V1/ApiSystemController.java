package com.huatu.tiku.essay.web.controller.api.V1;

import com.huatu.tiku.essay.vo.system.EssaySystemVO;
import com.huatu.tiku.essay.service.EssaySystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by x6 on 2018/3/29.
 *
 * 系统控制
 */
@RestController
@RequestMapping("api/v1/system")
@Slf4j
public class ApiSystemController {


    @Autowired
    private EssaySystemService essaySystemService;
    /**
     *  拍照答题相关提示
     */
    @GetMapping(value="photo/msg",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssaySystemVO photoMsg(){

        return essaySystemService.photoAnswerMessage();
    }


    /**
     * 申论首页icon管理
     */
    @GetMapping(value="icon",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object iconList(){
        return essaySystemService.iconList();
    }
}
