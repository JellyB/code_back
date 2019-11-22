/**
 * Sohu.com Inc.
 * Copyright (c) 2004-2015 All Rights Reserved.
 */
package com.huatu.ztk.scm.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 *
 * @author wenpingliu
 * @version v 0.1 15/9/21 09:46 wenpingliu Exp $$
 */
public class FreemarkerUtils{

    private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);

    public static Configuration cfg;

    static {
        try {
            cfg = new Configuration(Configuration.VERSION_2_3_22);
            cfg.setClassLoaderForTemplateLoading(FreemarkerUtils.class.getClassLoader(),"/templates");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);

        }
    }

    public static String getRenderResutl(String temlate,Map<String,Object> data){
        Template temp = null;
        try {
            temp = cfg.getTemplate(temlate);
            Writer out = new StringWriter(2048);
            temp.process(data, out);
            return out.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(FreemarkerUtils.cfg);
    }
}
