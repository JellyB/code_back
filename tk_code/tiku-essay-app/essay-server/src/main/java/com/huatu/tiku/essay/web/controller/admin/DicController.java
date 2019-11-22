package com.huatu.tiku.essay.web.controller.admin;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.vo.resp.OptionVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/15
 */
@RestController
@RequestMapping("/end/dic")
public class DicController {

    @GetMapping("/{name}")
    public Object getTikuDic(@PathVariable String name) {
        List<OptionVo> optionVos = Lists.newArrayList();

        try {
            // 反射获取对应枚举
            Class<?> temp = Class.forName(String.format("com.huatu.tiku.essay.essayEnum.%sEnum", name));

            // 通过values获取值
            Method values = temp.getMethod("values");

            Object[] enums = (Object[]) values.invoke(null, new Object[] {});

            // 封装成选项值
            for (Object tmp : enums) {
                OptionVo optionVo = new OptionVo();

                optionVo.setValue(temp.getMethod("getValue").invoke(tmp, new Object[] {}));
                optionVo.setText(temp.getMethod("getTitle").invoke(tmp, new Object[] {}).toString());
                try {
                    optionVo.setChecked((Boolean) temp.getMethod("getSelected").invoke(tmp, new Object[] {}));
                } catch (Exception e){

                }
                optionVos.add(optionVo);
            }
        } catch (Exception e) {
            throw new BizException(ErrorResult.create(1001, "未找到字典"));
        }

        return optionVos;
    }

}
