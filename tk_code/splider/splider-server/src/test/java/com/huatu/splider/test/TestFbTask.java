package com.huatu.splider.test;

import com.huatu.common.test.BaseWebTest;
import com.huatu.splider.task.FbCourseTask;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hanchao
 * @date 2018/3/12 16:24
 */
public class TestFbTask extends BaseWebTest{
    @Autowired
    private FbCourseTask fbCourseTask;
    @Test
    public void test(){
        fbCourseTask.execute();
    }
}
