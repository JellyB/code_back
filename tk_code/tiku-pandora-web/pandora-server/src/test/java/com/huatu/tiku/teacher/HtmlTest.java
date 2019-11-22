package com.huatu.tiku.teacher;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.constant.QuestionTailConstant;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.util.file.HtmlFileUtil;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/1/28
 * @描述 html tool class
 */
public class HtmlTest extends TikuBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileUtil.class);

    @Autowired
    private HtmlFileUtil HtmlFileUtil;

    @Autowired
    private CommonQuestionServiceV1 commonQuestionServiceV1;


}
