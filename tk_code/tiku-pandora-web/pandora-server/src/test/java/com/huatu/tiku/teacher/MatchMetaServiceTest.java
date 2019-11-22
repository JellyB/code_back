package com.huatu.tiku.teacher;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.teacher.service.match.MatchMetaService;
import com.huatu.tiku.teacher.service.match.MatchUserMetaService;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MatchMetaServiceTest extends TikuBaseTest {

    @Autowired
    MatchMetaService matchMetaService;

    @Autowired
    MatchUserMetaService matchUserMetaService;
//
//    @Test
//    public void test() {
//        Object o = matchMetaService.metaEnroll(4001909);
//        System.out.println(JsonUtil.toJson(o));
//    }
//
//    @Test
//    public void test(){
//        Object o = matchMetaService.metaEnroll(4001899);
//        System.out.println(JsonUtil.toJson(o));
//    }
//
//    @Test
//    public void test1(){
//        Object o = matchMetaService.metaResult(4001899);
//        System.out.println("o = " + JsonUtil.toJson(o));
//    }

    @Test
    public void test12() throws IOException {
        int paperId = 4001943;
        Object o1 = matchMetaService.metaAllTime(paperId);
        System.out.println("o1 = " + o1);
        File o = matchMetaService.metaResult(paperId);
        System.out.println("o = " + FileUtils.readFileToString(o));
    }
    @Test
    public void test2(){
        List<MatchUserMeta> orderByScore = matchUserMetaService.findOrderByScore(4001899, 20);
        System.out.println("JsonUtil.toJson(orderByScore) = " + JsonUtil.toJson(orderByScore));
    }

    @Test
    public void test3() throws IOException {
        File o = matchMetaService.metaResult(4001899);
        System.out.println("o = " +  FileUtils.readFileToString(o));
    }
}
