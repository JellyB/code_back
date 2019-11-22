package com.huatu.tiku.teacher.controller.admin.question;

import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/1/24
 * @描述 为前端公式编辑提供转换接口
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping(value = "formula")
class FormulaFormatController {

    @Value("${spring.profiles}")
    private String env;


    @RequestMapping(method = RequestMethod.GET,produces = "text/javascript;charset=utf-8")
    public Object GetFormulaFormat(@RequestParam(required = false, defaultValue = "-1") String callback) {
        // 需配置
        String serverUrl = "http://123.103.86.52:11145/pand";

        HashMap<String, String> formulaMap = new HashMap<>();
        formulaMap.put("imageActionName", "/file/upload");
        formulaMap.put("imageFieldName", "file");
        formulaMap.put("serverUrl", serverUrl);
        formulaMap.put("imageUrlPrefix", "");

        String dataResult = JsonUtil.toJson(formulaMap);
        if (callback.equals("-1")) {
            return dataResult;
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(callback).append("(").append(dataResult).append(")");
        return stringBuffer.toString();
    }

}
