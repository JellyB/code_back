package jbzm.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class test {
    public static void main(String args[]) throws Exception {
//        long startTime = new Date().getTime();
//        String path = "D:/1.pdf";
//        Document document = new Document(PageSize.A4, 45, 45, 100, 34);
//        PdfUtil pdf = new PdfUtil(path, document);
//        document.open();
//        String str = "                              远离网络\"三俗\"\n" +
//                "广大青年朋友们：\n" +
//                "        <如今互联网已与年轻人生活深度融合(1)>，<但庸俗、低俗、媚俗的\"三俗\"之风也随之产生(2)>，<这不仅影响你们的健康成长(3)>，<更有可能诱发违法犯罪行为(4)>。为此，<文化部发出通知(5)>，要求严格整治网络乱象，[严查网络表演市场(6)]，[严惩相关平台(6)]。在此我们民警大队恳切地向全区年轻人发出如下倡议：\n" +
//                "        首先，<我们应当树立正确价值观(7)>，<增强法律意识(8)>。文明上网，恪守道德底线，遵守法律法规，不借网络平台乘\"三俗\"之风谋利。其次，<加强自律(9)>，<以身作则传递正能量(10)>。向网络中正面形象学习，争做正面典型。再次，<增强责任意识(11)>，<自觉践行监督责任(12)>。积极配合政府部门管理，几时监督举报网络乱象违法行为。\n" +
//                "        广大青年朋友们，健康和谐的网络环境连接你我他，让我们远离\"三俗\"，携手共建网络文明，还网络一片净土。\n" +
//                "                                                               某民警大队\n" +
//                "                                                             某年某月某日";
//        char[] charList = str.toCharArray();
//        Paragraph paragraph = new Paragraph();
//        for (int i = 0; i < charList.length; i++) {
//            if (charList[i] == '[' || charList[i] == ']' || charList[i] == '<' || charList[i] == '>') {
//                continue;
//            } else if (charList[i == 0 ? 0 : i - 1] == '[') {
//                for (int j = i; j < charList.length; j++) {
//                    if (charList[j] == ']') {
//                        i = j;
//                        break;
//                    }
//                    Phrase phrase = new Phrase(String.valueOf(charList[j]), PdfUtil.CONTENT_FONT_RED);
//                    paragraph.add(phrase);
//                }
//            } else if (charList[i == 0 ? 0 : i - 1] == '<') {
//                for (int j = i; j < charList.length; j++) {
//                    if (charList[j] == '>') {
//                        i = j;
//                        break;
//                    }
//                    Phrase phrase = new Phrase(String.valueOf(charList[j]), PdfUtil.CONTENT_FONT_ORANGE);
//                    paragraph.add(phrase);
//                }
//            } else {
//                Phrase phrase = new Phrase(String.valueOf(charList[i]), PdfUtil.CONTENT_FONT);
//                paragraph.add(phrase);
//            }
//        }
//        document.add(paragraph);
//        long endTime = System.currentTimeMillis();
//        System.out.println(endTime - startTime);
//        document.close();

        ArrayList<Integer> list = null;
        ArrayList<Integer> list2 = null;

//        list.add(1);
//        list.add(2);
//        list2.add(1);
//        list2.add(2);
//        list2.add(3);
        Collection disjunction = CollectionUtils.intersection(list, list2);

        System.out.println(list+"==="+list2+"==="+disjunction);
    }


    /**
     * 添加正则
     */
    @Test
    public void test() {
        String str = "<p>请根据给定资料3，用简洁的语言概括当前社会上“求快”风气形成的原因（15分）要求：全面准确\n" +
                "、分条陈述，不超过200字。</p>";
        System.out.println(str.replaceAll("<[^>]+>", " "));
    }

}
