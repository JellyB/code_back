package com.huatu.ztk.search;

import com.google.common.collect.Lists;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.common.QuestionType;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by shaojieyue
 * Created time 2016-05-04 14:40
 */
public class EsClientTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        GenericQuestion question = new GenericQuestion();
        question.setParent(1);
        question.setAnswer(12);
        question.setFrom("2012 太原");
        question.setStatus(1);
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setStem("英国著名学术刊物《自然》6日发布声明，就其网站刊载的一篇将中国游泳运动员叶诗文和兴奋剂联系在一起的文章道歉，这篇文章认为叶诗文夺得伦敦奥运会女子400米个人混合泳金牌的卓越表现是“异常”的，即使当前的药检结果清白，也不能完全排除服用兴奋剂的可能，道歉声明中提到：“我们注意到相关讨论中指出的一些错误，以及相关统计中缺乏更多的细节，造成了我们支持指责叶诗文的印象，而需要强调的是________。”填入横线的语句与上文衔接最顺畅的一句是（&nbsp;&nbsp;&nbsp;&nbsp;）。");
        question.setAnalysis("<p class=\"item-p\">文段指出由于道歉声明中提及“我们注意到相关讨论中指出的一些错误，以及相关统计中缺乏更多的细节”这使得学术刊物《自然》给他人造成支持指责叶诗文的印象，接下来出现转折词“而”，说明填入语句是表示对于造成的这种印象并不是该刊物的初衷，观察选项，只有B项最为合适</p>");
        question.setChoices(Lists.newArrayList("我们对此表示遗憾","这不是我们的意图","我们向叶诗文道歉","这确实是一个问题"));
        question.setDifficult(3);
        question.setArea(12);
        question.setScore(1);
        question.setId(132);
        question.setYear(2012);
//        question.setKnowledgeId(11);
//        QuestionSearchService.index(question);
//        QuestionSearchService.search("英国著名学",0,1);
//        QuestionSearchService.search("我们对此表",0,1);
//        QuestionSearchService.search("文段指出由于道歉声明",0,1);
    }
}
