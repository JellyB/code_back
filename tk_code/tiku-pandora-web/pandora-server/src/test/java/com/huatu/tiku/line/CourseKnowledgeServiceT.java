package com.huatu.tiku.line;

import com.google.zxing.WriterException;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.banckend.service.CourseKnowledgeService;
import com.huatu.tiku.entity.CourseKnowledge;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.teacher.service.match.MatchUserMetaService;
import com.huatu.tiku.util.file.CourseQCode;
import com.huatu.tiku.util.file.FormulaConvert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * Created by huangqp on 2018\6\12 0012.
 */
public class CourseKnowledgeServiceT extends TikuBaseTest {

    @Autowired
    CourseKnowledgeService courseKnowledgeService;

    @Autowired
    private MatchUserMetaService matchUserMetaService;


    @Test
    public void test() {
        CourseKnowledge courseKnowledge = CourseKnowledge.builder().courseId(100L).knowledgeId(100L).build();
        courseKnowledgeService.save(courseKnowledge);
    }


    @Test
    public void fileUpload() throws Exception {
        FormulaConvert.dealTheImage("http://latex.codecogs.com/gif.latex?\\sqrt{5}");
    }

    @Test
    public void courseQCode() throws IOException, WriterException {
        String returnPath = CourseQCode.getInstance().uploadQCodeImgAndReturnPath(123L);
        System.out.println("returnPath = " + returnPath);
    }

    @Test
    public void Tags() {
//        List<HashMap<String, Object>> tags = ActivityTagEnum.Tags(3);
//        System.out.println("标签是：{}" + tags);
        //题干是2
        //选项是4
        //材料是8
        //解析是16
        int a = 6; /* 60 = 0011 1100 */
        int b = 2; /* 13 = 0000 1101 */
        int c = 0;
        c = a & b;       /* 12 = 0000 1100 */
        System.out.println("c是：{}" + c);

        if ((4 & 8) > 0) {
            System.out.println("结果是：{}");
        }

    }

    @Test
    public void getTest() {
        List<MatchUserMeta> byMatchId = matchUserMetaService.findByMatchId(3528671);
        System.out.print("结果是:{}" + byMatchId);

    }

}

