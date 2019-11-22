package com.huatu.tiku.essay.util.admin;

import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import org.apache.commons.lang3.StringUtils;

/**
 * 处理申论试卷分析内容的一些实现
 * Created by huangqp on 2018\4\21 0021.
 */
public class EssayAnalyzeUtil {
    /**
     * 将三个字段的内容，合并为一个字段的三个段落，格式保持不变
     * @param answerTask  <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;内容<p/>
     * @param answerRange   <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;内容<p/>
     * @param answerDetail  <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;内容<p/>
     * @return  <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;任务：内容<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;范围：内容<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;要求：内容</p>
     */
    public static String assertAnalyze(String answerTask, String answerRange, String answerDetail) {
        String result = "<p>";
        answerTask = assertContent(answerTask,"任务：");
        if(StringUtils.isNotBlank(answerTask)){
            result = result + answerTask;
        }
        answerRange = assertContent(answerRange,"范围：");
        if(StringUtils.isNotBlank(answerRange)){
            result =   result + "<br/>" + answerRange;
        }
        answerDetail = assertContent(answerDetail,"要求：");
        if(StringUtils.isNotBlank(answerDetail)){
            result =   result + "<br/>" + answerDetail;
        }
        result = result + "</p>";
        return result;
    }
    public static void main(String[] args){
//        String answerTask = "<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;以“创业者心中的山”为题，在写作的时候我们需要揭示出“山”的内涵，即“山”指的是什么。如何才能越过这座山？这就需要考生从给定资料出发，并联系自己的实践经验或生活体验。</p>";
//        String answerRange = "";
//        String answerDetail = "<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;“主旨明确，内容充实，结构完整，论述深刻，思路清晰，语言流畅”是议论文的一般性要求，在此不做详细解读。字数要求1000～1200字。 </p>";
//        String result = assertAnalyze(answerTask,answerRange,answerDetail);
//        System.out.println("result=\n"+result);
        String result = "<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;任务：归纳概括“垃圾难题”，作答任务简单明了，就是要把材料中出现的与垃圾相关的各种问题提炼出来。这道题涵盖四则材料，案例比较多，涉及的问题也比较多，因此我们在圈定关键词句后，应当对这些问题进行适当地概括总结，有意识地对答案要点进行分类整理，这样才能符合题目“归纳概括”的要求。<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;范围：给定资料2-5。<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;细节：概括全面，语言简明，表述准确，这些是归纳概括题的常规要求。条理清楚这个细节要求我们分条作答，同时作答任务中的“归纳”还要求我们在条理的基础上有分类的意识。字数要求300字，说明“垃圾难题”可不少。<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>";
        EssayQuestionDetail essayQuestionDetail = EssayQuestionDetail.builder().analyzeQuestion(result).type(1).build();
        splitAnalyze(essayQuestionDetail);
        System.out.println("task= "+essayQuestionDetail.getAnswerTask());
        System.out.println("range= "+essayQuestionDetail.getAnswerRange());
        System.out.println("detail= "+essayQuestionDetail.getAnswerDetails());
    }
    /**
     * 在字段的前面拼上name，作为段落格式不变
     * @param content   <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;内容<p/>
     * @param name      name
     * @return      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;name+内容
     */
    public static String assertContent(String content, String name) {
        if(content.indexOf("<p>")==0){
            content = content.replaceFirst("<p>","").trim();
        }
        while(content.indexOf("&nbsp;")==0){
            content = content.replaceFirst("&nbsp;","").trim();
        }
        content = content.trim();
        content = content.replaceAll("</p>$","").trim();
        if(StringUtils.isBlank(content)){
            return "";
        }
        return "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + name + content;
    }

    /**
     * 根据试题类型将试题分析内容拆分
     * type = 1、2、3  任务，范围，细节
     * type = 4   文种，范围，细节
     * type = 5  主题，细节
     * @param essayQuestionDetail
     */
    public static void splitAnalyze(EssayQuestionDetail essayQuestionDetail){
        int type = essayQuestionDetail.getType();
        switch (type){
            case 1:
            case 2:
            case 3:{
                splitAnalyzeBy(essayQuestionDetail,"任务","范围","细节");
                break;
            }
            case 4:{
                splitAnalyzeBy(essayQuestionDetail,"文种","范围","细节");
                break;
            }
            case 5:{
                splitAnalyzeBy(essayQuestionDetail,"主题","范围","细节");
                break;
            }
        }
    }

    private static void splitAnalyzeBy(EssayQuestionDetail essayQuestionDetail, String task, String range, String detail) {
        String analyzeQuestion = essayQuestionDetail.getAnalyzeQuestion();
        if(StringUtils.isBlank(analyzeQuestion)){
            return;
        }
        String[] contents = {null,null,null};
        if(analyzeQuestion.indexOf(task+"：")!=-1){
            analyzeQuestion = analyzeQuestion.split(task+"：")[1];
        }else if(analyzeQuestion.indexOf(task+":")!=-1) {
            analyzeQuestion = analyzeQuestion.split(task+":")[1];
        }else{
            contents[0] = "";
        }
        if(analyzeQuestion.indexOf(range+":")!=-1){
            String temp = dealTailTag(analyzeQuestion.split(range+":")[0]);
            contents[0] = temp;
            analyzeQuestion = analyzeQuestion.split(range+":")[1];
        }else if(analyzeQuestion.indexOf(range+"：")!=-1){
            String temp = dealTailTag(analyzeQuestion.split(range+"：")[0]);
            contents[0] = temp;
            analyzeQuestion = analyzeQuestion.split(range+"：")[1];
        }else{
            contents[1] = "";
        }
        if(analyzeQuestion.indexOf(detail+":")!=-1){
            String temp = dealTailTag(analyzeQuestion.split(detail+":")[0]);
            if(contents[0]==null){
                contents[0] = temp;
            }else if(contents[1] == null){
                contents[1] = temp;
            }
            analyzeQuestion = analyzeQuestion.split(detail+":")[1];
        }else if(analyzeQuestion.indexOf(detail+"：")!=-1){
            String temp = dealTailTag(analyzeQuestion.split(detail+"：")[0]);
            if(contents[0]==null){
                contents[0] = temp;
            }else if(contents[1] == null){
                contents[1] = temp;
            }
            analyzeQuestion = analyzeQuestion.split(detail+"：")[1];
        }else{
            contents[2] = "";
        }
        for(int i = 0 ;i<contents.length;i++){
            if(contents[i] == null){
                contents[i] = dealTailTag(analyzeQuestion);
                break;
            }
        }
        essayQuestionDetail.setAnswerTask(contents[0]);
        essayQuestionDetail.setAnswerRange(contents[1]);
        essayQuestionDetail.setAnswerDetails(contents[2]);
    }

    private static String dealTailTag(String content) {
        content = content.trim();
        content = content.replaceAll("</p>$","");
        while(content.lastIndexOf("&nbsp;")==content.length()-6){
            content = content.replaceAll("&nbsp;$","");
            if(content.lastIndexOf("&nbsp;")==-1){
                break;
            }
            System.out.println(content);
        }
        content = content.replaceAll("<br/>$","").trim();
        return "<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + content +"</p>";
    }
}
