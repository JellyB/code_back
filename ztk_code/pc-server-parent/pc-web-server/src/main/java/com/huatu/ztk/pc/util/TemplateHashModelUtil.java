package com.huatu.ztk.pc.util;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue
 * Created time 2016-09-18 19:47
 */
public class TemplateHashModelUtil {
    private static final Logger logger = LoggerFactory.getLogger(TemplateHashModelUtil.class);

    /**
     * freemark 使用静态包
     * @param packageName
     * @return
     */
    public static TemplateHashModel useStaticPackage(String packageName) {
        try {
            BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
            TemplateHashModel staticModels = wrapper.getStaticModels();
            TemplateHashModel fileStatics = (TemplateHashModel) staticModels.get(packageName);
            return fileStatics;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
