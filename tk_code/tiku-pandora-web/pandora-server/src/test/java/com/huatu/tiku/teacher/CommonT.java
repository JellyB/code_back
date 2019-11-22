package com.huatu.tiku.teacher;

import com.github.pagehelper.PageInfo;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.tiku.request.material.MaterialReq;
import com.huatu.tiku.request.question.v1.UpdateCommonQuestionReqV1;
import com.huatu.tiku.response.question.v1.*;
import com.huatu.tiku.teacher.enums.DifficultyLevelEnum;
import com.huatu.tiku.util.file.FormulaUtilTest;
import com.huatu.tiku.util.file.ImgInfoVO;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import sun.misc.BASE64Decoder;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 不启动项目测试
 * Created by huangqp on 2018\7\10 0010.
 */
public class CommonT {

    @Test
    public void tempTest(){
        List list = Lists.newArrayList(1,2,3,4,5,6,7,8,9,10);
        System.out.println(list.subList(0,3));
        System.out.println(list.subList(3,3+2));
        System.out.println(list.subList(3+2,3+2+4));
    }
    @Test
    public void test(){
//        SelectJudgeQuestionRespV1 selectJudgeQuestionResp = new SelectJudgeQuestionRespV1();
//        selectJudgeQuestionResp.setJudgeBasis("判断依据，只要判断题类的辨析题才会用到");
//        selectJudgeQuestionResp.setChoices(Lists.newArrayList("<p>选项A内容<p>","<p>选项B内容<p>","<p>选项CN内容<p>","<p>选项D内容<p>"));
//        selectJudgeQuestionResp.setAnswer("A");
//        selectJudgeQuestionResp.setStem("题干内容，所有题型都有，除了复合题主题部分");
//        selectJudgeQuestionResp.setAnalysis("试题解析，所有单一客观题的属性");
//        selectJudgeQuestionResp.setExtend("试题拓展，所有非复合题题型的属性");
//        selectJudgeQuestionResp.setAnswerComment("参考答案，主观题字段");
//        selectJudgeQuestionResp.setAnalyzeQuestion("试题分析，主观题字段");
//        selectJudgeQuestionResp.setOmnibusRequirements("总括要求，复合题主题部分属性");
//        selectJudgeQuestionResp.setMaterials(Lists.newArrayList(MaterialReq.builder().content("材料1内容").materialId(-1L).build(),MaterialReq.builder().content("材料2内容").materialId(-1L).build()));
//        selectJudgeQuestionResp.setDuplicateId(1L);
//        selectJudgeQuestionResp.setAvailFlag(1);
//        selectJudgeQuestionResp.setMissFlag(0);
//        selectJudgeQuestionResp.setQuestionType(99);
//        selectJudgeQuestionResp.setMultiId(0L);
//        selectJudgeQuestionResp.setDifficultyLevel(2);
//        selectJudgeQuestionResp.setSubject(1L);
//        selectJudgeQuestionResp.setGrades(Lists.newArrayList(2L));
//        selectJudgeQuestionResp.setKnowledgeIds(Lists.newArrayList(1L,2L));
//        selectJudgeQuestionResp.setTags(Lists.newArrayList(1L));
//        selectJudgeQuestionResp.setQuestionAreaYears(Lists.newArrayList());
//        System.out.println(JsonUtil.toJson(selectJudgeQuestionResp));
    }
    @Test
    public void test1(){
        UpdateCommonQuestionReqV1 updateCommonQuestionReqV1 = new UpdateCommonQuestionReqV1();
        updateCommonQuestionReqV1.setJudgeBasis("判断依据，只要判断题类的辨析题才会用到");
        updateCommonQuestionReqV1.setChoices(Lists.newArrayList("<p>选项A内容<p>","<p>选项B内容<p>","<p>选项CN内容<p>","<p>选项D内容<p>"));
        updateCommonQuestionReqV1.setAnswer("A");
        updateCommonQuestionReqV1.setStem("题干内容，所有题型都有，除了复合题主题部分");
        updateCommonQuestionReqV1.setAnalysis("试题解析，所有单一客观题的属性");
        updateCommonQuestionReqV1.setExtend("试题拓展，所有非复合题题型的属性");
        updateCommonQuestionReqV1.setAnswerComment("参考答案，主观题字段");
        updateCommonQuestionReqV1.setAnalyzeQuestion("试题分析，主观题字段");
        updateCommonQuestionReqV1.setOmnibusRequirements("总括要求，复合题主题部分属性");
        updateCommonQuestionReqV1.setMaterials(Lists.newArrayList(MaterialReq.builder().content("材料1内容").materialId(-1L).build(),MaterialReq.builder().content("材料2内容").materialId(-1L).build()));
        updateCommonQuestionReqV1.setDuplicateId(1L);
        updateCommonQuestionReqV1.setId(1L);
        updateCommonQuestionReqV1.setAvailFlag(1);
        updateCommonQuestionReqV1.setMissFlag(0);
        updateCommonQuestionReqV1.setQuestionType(99);
        updateCommonQuestionReqV1.setMultiId(0L);
        updateCommonQuestionReqV1.setDifficultyLevel(2);
        updateCommonQuestionReqV1.setSubject(1L);
        updateCommonQuestionReqV1.setGrades(Lists.newArrayList(2L));
        updateCommonQuestionReqV1.setKnowledgeIds(Lists.newArrayList(1L,2L));
        updateCommonQuestionReqV1.setTags(Lists.newArrayList(1L));
        updateCommonQuestionReqV1.setQuestionAreaYears(Lists.newArrayList());
        System.out.println(JsonUtil.toJson(updateCommonQuestionReqV1));
    }

    @Test
    public void queryTest(){
        //复合题
        SelectCompositeQuestionRespV1 selectCompositeQuestionRespV1 = new SelectCompositeQuestionRespV1();
        assertCompositeQuestion(selectCompositeQuestionRespV1);
        System.out.println("复合题json:  "+ JsonUtil.toJson(selectCompositeQuestionRespV1));
        //选择题
        SelectObjectiveQuestionRespV1 selectObjectiveQuestionRespV1 = new SelectObjectiveQuestionRespV1();
        assertObjectiveQuestion(selectObjectiveQuestionRespV1);
        System.out.println("客观题json:  "+ JsonUtil.toJson(selectObjectiveQuestionRespV1));
        //判断题
        SelectJudgeQuestionRespV1 selectJudgeQuestionRespV1 = new SelectJudgeQuestionRespV1();
        assertJudgeQuestion(selectJudgeQuestionRespV1);
        System.out.println("判断题json:  "+ JsonUtil.toJson(selectJudgeQuestionRespV1));
        //主观题
        SelectSubjectiveQuestionRespV1 selectSubjectiveQuestionRespV1 = new SelectSubjectiveQuestionRespV1();
        assertSubjectiveQuestion(selectSubjectiveQuestionRespV1);
        System.out.println("主观题json:  "+ JsonUtil.toJson(selectSubjectiveQuestionRespV1));
        List<SelectQuestionRespV1> questions = Lists.newArrayList(selectCompositeQuestionRespV1,selectObjectiveQuestionRespV1,selectJudgeQuestionRespV1,selectSubjectiveQuestionRespV1);
        PageInfo<SelectQuestionRespV1> result = new PageInfo<>();
        result.setList(questions);
        result.setEndRow(10);
        result.setIsFirstPage(true);
        result.setFirstPage(1);
        result.setHasNextPage(false);
        result.setHasPreviousPage(false);
        result.setIsLastPage(true);
        result.setLastPage(1);
        result.setNavigateFirstPage(1);
        result.setNavigateLastPage(1);
        result.setNavigatePages(1);
        result.setNavigatepageNums(new int[]{1});
        result.setNextPage(1);
        result.setPageNum(1);
        result.setPageSize(10);
        result.setPages(1);
        result.setPrePage(0);
        result.setSize(10);
        result.setStartRow(1);
        result.setTotal(4);
        System.out.println(JsonUtil.toJson(result));
    }

    /**
     * 组装复合题
     * @param selectCompositeQuestionRespV1
     */
    private void assertCompositeQuestion(SelectCompositeQuestionRespV1 selectCompositeQuestionRespV1) {
        selectCompositeQuestionRespV1.setOmnibusRequirements("总括要求（非必要属性，有则展示，没有忽略）");
        selectCompositeQuestionRespV1.setMaterials(Lists.newArrayList(MaterialReq.builder().content("材料1的内容").materialId(1L).build(),MaterialReq.builder().content("材料2的内容").materialId(1L).build()));
        //选择题
        SelectObjectiveQuestionRespV1 selectObjectiveQuestionRespV1 = new SelectObjectiveQuestionRespV1();
        assertObjectiveQuestion(selectObjectiveQuestionRespV1);
        selectObjectiveQuestionRespV1.setId(5L);
        selectObjectiveQuestionRespV1.setMultiId(4L);
        selectObjectiveQuestionRespV1.setMaterialIds(Lists.newArrayList(1L));
        //判断题
        SelectJudgeQuestionRespV1 selectJudgeQuestionRespV1 = new SelectJudgeQuestionRespV1();
        assertJudgeQuestion(selectJudgeQuestionRespV1);
        selectJudgeQuestionRespV1.setId(6L);
        selectJudgeQuestionRespV1.setMultiId(4L);
        selectJudgeQuestionRespV1.setMaterialIds(Lists.newArrayList(1L));
        //主观题
        SelectSubjectiveQuestionRespV1 selectSubjectiveQuestionRespV1 = new SelectSubjectiveQuestionRespV1();
        assertSubjectiveQuestion(selectSubjectiveQuestionRespV1);
        selectSubjectiveQuestionRespV1.setId(7L);
        selectSubjectiveQuestionRespV1.setMultiId(4L);
        selectSubjectiveQuestionRespV1.setMaterialIds(Lists.newArrayList(2L));
        selectCompositeQuestionRespV1.setQuestionType(QuestionInfoEnum.QuestionTypeEnum.COMPOSITE_SUBJECTIVE.getCode());
        selectCompositeQuestionRespV1.setChildren(Lists.newArrayList(selectObjectiveQuestionRespV1,selectJudgeQuestionRespV1,selectSubjectiveQuestionRespV1));
    }

    /**
     * 组装主观题
     * @param selectSubjectiveQuestionRespV1
     */
    private void assertSubjectiveQuestion(SelectSubjectiveQuestionRespV1 selectSubjectiveQuestionRespV1) {
//        selectSubjectiveQuestionRespV1.setQuestionType(QuestionInfoEnum.QuestionTypeEnum.SUBJECTIVE.getType());
        selectSubjectiveQuestionRespV1.setStem("题干");
        selectSubjectiveQuestionRespV1.setAnswerComment("正确");
        selectSubjectiveQuestionRespV1.setBizStatus(2);
        selectSubjectiveQuestionRespV1.setId(3L);
        selectSubjectiveQuestionRespV1.setDifficult("较易");
        selectSubjectiveQuestionRespV1.setKnowledgeList(Lists.newArrayList("教育学-教育与社会发展-教育与政治、经济、科技","教育学-教育与社会发展-教育与政治、经济、科技"));
        selectSubjectiveQuestionRespV1.setAnalyzeQuestion("解析内容");
        selectSubjectiveQuestionRespV1.setExtend("拓展内容");
        selectSubjectiveQuestionRespV1.setSourceList(Lists.newArrayList("2009年浙江省公务员录用考试《行测》真题  第29题","2009年山西省公务员录用考试《行测》真题  第39题"));
        assertCommonAttr(selectSubjectiveQuestionRespV1);
    }


    /**
     * 组装判断题
     * @param selectJudgeQuestionRespV1
     */
    private void assertJudgeQuestion(SelectJudgeQuestionRespV1 selectJudgeQuestionRespV1) {
//        selectJudgeQuestionRespV1.setQuestionType(QuestionInfoEnum.QuestionTypeEnum.JUDGE.getType());
        selectJudgeQuestionRespV1.setStem("题干");
        selectJudgeQuestionRespV1.setAnswerDetail("正确");
        selectJudgeQuestionRespV1.setBizStatus(2);
        selectJudgeQuestionRespV1.setId(2L);
        selectJudgeQuestionRespV1.setDifficult("较易");
        selectJudgeQuestionRespV1.setKnowledgeList(Lists.newArrayList("教育学-教育与社会发展-教育与政治、经济、科技","教育学-教育与社会发展-教育与政治、经济、科技"));
        selectJudgeQuestionRespV1.setAnalysis("解析内容");
        selectJudgeQuestionRespV1.setExtend("拓展内容");
        selectJudgeQuestionRespV1.setSourceList(Lists.newArrayList("2009年浙江省公务员录用考试《行测》真题  第29题","2009年山西省公务员录用考试《行测》真题  第39题"));
        assertCommonAttr(selectJudgeQuestionRespV1);
    }

    /**
     * 组装客观题
     * @param selectObjectiveQuestionRespV1
     */
    private void assertObjectiveQuestion(SelectObjectiveQuestionRespV1 selectObjectiveQuestionRespV1) {
//        selectObjectiveQuestionRespV1.setQuestionType(QuestionInfoEnum.QuestionTypeEnum.SINGLE.getType());
        selectObjectiveQuestionRespV1.setStem("题干");
        selectObjectiveQuestionRespV1.setChoices(Lists.newArrayList("<p>选项A内容<p>","<p>选项B内容<p>","<p>选项C内容<p>","<p>选项D内容<p>"));
        selectObjectiveQuestionRespV1.setAnalysis("解析内容");
        selectObjectiveQuestionRespV1.setExtend("拓展内容");
        selectObjectiveQuestionRespV1.setAnswer("A");
        selectObjectiveQuestionRespV1.setId(1L);
        assertCommonAttr(selectObjectiveQuestionRespV1);
    }

    private void assertCommonAttr(SelectQuestionRespV1 selectQuestionRespV1) {
        selectQuestionRespV1.setDuplicateId(1L);
        selectQuestionRespV1.setMode(1);
        selectQuestionRespV1.setBizStatus(2);
        selectQuestionRespV1.setDifficultyLevel(2);
        selectQuestionRespV1.setGrades(Lists.newArrayList(1L,2L));
        selectQuestionRespV1.setGradeList(Lists.newArrayList("中学","小学"));
        selectQuestionRespV1.setSourceList(Lists.newArrayList("2009年浙江省公务员录用考试《行测》真题  第29题","2009年山西省公务员录用考试《行测》真题  第39题"));
        selectQuestionRespV1.setKnowledgeList(Lists.newArrayList("教育学-教育与社会发展-教育与政治、经济、科技1","教育学-教育与社会发展-教育与政治、经济、科技2"));
        selectQuestionRespV1.setDifficult(DifficultyLevelEnum.create(2).getTitle());
        selectQuestionRespV1.setKnowledgeIds(Lists.newArrayList(199L,200L));
        selectQuestionRespV1.setTags(Lists.newArrayList(1L,2L));
        selectQuestionRespV1.setTagList(Lists.newArrayList("标签1","标签2"));
        selectQuestionRespV1.setAvailFlag(1);
        selectQuestionRespV1.setMissFlag(2);
    }

    @Test
    public void test2() throws UnsupportedEncodingException {
        String target = "%EF%BF%AF%EF%BE%BC%EF%BE%88%EF%BF%A5%EF%BE%A4%EF%BE%9A%EF%BF%A9%EF%BE%80%EF%BE%89%EF%BF%A9%EF%BE%A2%EF%BE%98%EF%BF%AF%EF%BE%BC%EF%BE%89%EF%BF%A5%EF%BE%A4%EF%BE%9A%EF%BF%A9%EF%BE%80%EF%BE%89%EF%BF%A9%EF%BE%A2%EF%BE%98%EF%BF%A7%EF%BE%9A%EF%BE%84%EF%BF%A9%EF%BE%A2%EF%BE%98%EF%BF%A5%EF%BE%B9%EF%BE%B2%EF%BF%A9%EF%BE%83%EF%BE%A8%EF%BF%A5%EF%BE%88%EF%BE%86%0AA.+%EF%BF%A9%EF%BE%80%EF%BE%89%EF%BF%A9%EF%BE%A1%EF%BE%B9%EF%BF%A6%EF%BE%96%EF%BE%87%EF%BF%A6%EF%BE%A1%EF%BE%881%0AB.+%EF%BF%A9%EF%BE%80%EF%BE%89%EF%BF%A9%EF%BE%A1%EF%BE%B9%EF%BF%A6%EF%BE%96%EF%BE%87%EF%BF%A6%EF%BE%A1%EF%BE%882%0AC.+%EF%BF%A9%EF%BE%80%EF%BE%89%EF%BF%A9%EF%BE%A1%EF%BE%B9%EF%BF%A6%EF%BE%96%EF%BE%87%EF%BF%A6%EF%BE%A1%EF%BE%883%0AD.+%EF%BF%A9%EF%BE%80%EF%BE%89%EF%BF%A9%EF%BE%A1%EF%BE%B9%EF%BF%A6%EF%BE%96%EF%BE%87%EF%BF%A6%EF%BE%A1%EF%BE%884%0A%EF%BF%A3%EF%BE%80%EF%BE%90%EF%BF%A7%EF%BE%AD%EF%BE%94%EF%BF%A6%EF%BE%A1%EF%BE%88%EF%BF%A3%EF%BE%80%EF%BE%91ABC%0A%EF%BF%A3%EF%BE%80%EF%BE%90%EF%BF%A8%EF%BE%A7%EF%BE%A3%EF%BF%A6%EF%BE%9E%EF%BE%90%EF%BF%A3%EF%BE%80%EF%BE%91%EF%BF%A8%EF%BE%A7%EF%BE%A3%EF%BF%A6%EF%BE%9E%EF%BE%90%EF%BF%A6%EF%BE%96%EF%BE%87%EF%BF%A6%EF%BE%A1%EF%BE%88%0A%EF%BF%A3%EF%BE%80%EF%BE%90%EF%BF%A6%EF%BE%8B%EF%BE%93%EF%BF%A5%EF%BE%B1%EF%BE%95%EF%BF%A3%EF%BE%80%EF%BE%91%EF%BF%A6%EF%BE%8B%EF%BE%93%EF%BF%A5%EF%BE%B1%EF%BE%95%EF%BF%A6%EF%BE%96%EF%BE%87%EF%BF%A6%EF%BE%A1%EF%BE%88%0A%EF%BF%A3%EF%BE%80%EF%BE%90%EF%BF%A6%EF%BE%A0%EF%BE%87%EF%BF%A7%EF%BE%AD%EF%BE%BE%EF%BF%A3%EF%BE%80%EF%BE%91%EF%BF%A6%EF%BE%A0%EF%BE%87%EF%BF%A7%EF%BE%AD%EF%BE%BE1%EF%BF%AF%EF%BE%BC%EF%BE%8C%EF%BF%A6%EF%BE%A0%EF%BE%87%EF%BF%A7%EF%BE%AD%EF%BE%BE2%EF%BF%AF%EF%BE%BC%EF%BE%8C%EF%BF%A6%EF%BE%A0%EF%BE%87%EF%BF%A7%EF%BE%AD%EF%BE%BE3%0A%EF%BF%A3%EF%BE%80%EF%BE%90%EF%BF%A7%EF%BE%9F%EF%BE%A5%EF%BF%A8%EF%BE%AF%EF%BE%86%EF%BF%A7%EF%BE%82%EF%BE%B9%EF%BF%A3%EF%BE%80%EF%BE%91%EF%BF%A4%EF%BE%B8%EF%BE%80%EF%BF%A7%EF%BE%BA%EF%BE%A7%EF%BF%A7%EF%BE%9F%EF%BE%A5%EF%BF%A8%EF%BE%AF%EF%BE%86%EF%BF%A7%EF%BE%82%EF%BE%B91*%EF%BF%A4%EF%BE%BA%EF%BE%8C%EF%BF%A7%EF%BE%BA%EF%BE%A7%EF%BF%A7%EF%BE%9F%EF%BE%A5%EF%BF%A8%EF%BE%AF%EF%BE%86%EF%BF%A7%EF%BE%82%EF%BE%B91*%EF%BF%A4%EF%BE%B8%EF%BE%89%EF%BF%A7%EF%BE%BA%EF%BE%A7%EF%BF%A7%EF%BE%9F%EF%BE%A5%EF%BF%A8%EF%BE%AF%EF%BE%86%EF%BF%A7%EF%BE%82%EF%BE%B91%EF%BF%AF%EF%BE%BC%EF%BE%8C%EF%BF%A4%EF%BE%B8%EF%BE%80%EF%BF%A7%EF%BE%BA%EF%BE%A7%EF%BF%A7%EF%BE%9F%EF%BE%A5%EF%BF%A8%EF%BE%AF%EF%BE%86%EF%BF%A7%EF%BE%82%EF%BE%B92*%EF%BF%A4%EF%BE%B8%EF%BE%80%EF%BF%A7%EF%BE%BA%EF%BE%A7%EF%BF%A7%EF%BE%9F%EF%BE%A5%EF%BF%A8%EF%BE%AF%EF%BE%86%EF%BF%A7%EF%BE%82%EF%BE%B92*%EF%BF%A4%EF%BE%B8%EF%BE%89%EF%BF%A7%EF%BE%BA%EF%BE%A7%EF%BF%A7%EF%BE%9F%EF%BE%A5%EF%BF%A8%EF%BE%AF%EF%BE%86%EF%BF%A7%EF%BE%82%EF%BE%B92%0A%EF%BF%A3%EF%BE%80%EF%BE%90%EF%BF%A9%EF%BE%9A%EF%BE%BE%EF%BF%A5%EF%BE%BA%EF%BE%A6%EF%BF%A3%EF%BE%80%EF%BE%91%EF%BF%A4%EF%BE%B8%EF%BE%AD%EF%BF%A5%EF%BE%BA%EF%BE%A6%0A=";
        System.out.println("restult= "+ utf8Togb2312(target));
        System.out.println("restult= "+ gb2312ToUtf8(target));
    }

    public static String utf8Togb2312(String str){

        StringBuffer sb = new StringBuffer();

        for ( int i=0; i<str.length(); i++) {

            char c = str.charAt(i);

            switch (c) {

                case '+' :

                    sb.append( ' ' );

                    break ;

                case '%' :

                    try {

                        sb.append(( char )Integer.parseInt (

                                str.substring(i+1,i+3),16));

                    }

                    catch (NumberFormatException e) {

                        throw new IllegalArgumentException();

                    }

                    i += 2;

                    break ;

                default :

                    sb.append(c);

                    break ;

            }

        }

        String result = sb.toString();

        String res= null ;

        try {

            byte [] inputBytes = result.getBytes( "UTF-8" );

            res= new String(inputBytes, "GB2312" );

        }

        catch (Exception e){}

        return res;

    }

// 将 GB2312 编码格式的字符串转换为 UTF-8 格式的字符串：

    public static String gb2312ToUtf8(String str) {

        String urlEncode = "" ;

        try {

            urlEncode = URLEncoder.encode (str, "GB2312" );

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();

        }

        return urlEncode;

    }

    @Test
    public void test11(){
        Long id = 1L;
        ArrayList<Long> longs = Lists.newArrayList(id);
        id ++;
        longs.add(id);
        System.out.println(longs);

    }

    @Test
    public void testLetex(){
        ImgInfoVO imag = FormulaUtilTest.latex2Png("测");
        String base = imag.getBase();
        System.out.println(base);
        GenerateImage(base, "C:\\Users\\x6\\Desktop\\1.png");
    }



    public static boolean GenerateImage(String imgStr, String imgFilePath) {
        //图像数据为空
        if (StringUtils.isBlank(imgFilePath))
        {
            return false;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                //调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            //生成图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}