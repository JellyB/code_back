package com.huatu.ztk;

import com.huatu.ztk.question.service.QuestionDubboServiceImpl;
import com.huatu.ztk.question.util.ImageUtil;
import com.sun.media.sound.SoftTuning;
import ij.ImagePlus;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.*;
import org.jsoup.parser.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shaojieyue
 * Created time 2016-06-07 11:52
 */
public class TsoupTest {
    private static final Logger logger = LoggerFactory.getLogger(TsoupTest.class);
    private static final String BR = "<br/>";
    private static String imgprefix = "http://tiku.huatu.com/cdn/images/vhuatu/tiku/";
    public static final Set includes = new HashSet<>();
    public static final Set excludes = new HashSet<>();
    public static final Set supports = new HashSet<>();
    static {
        excludes.add("table");
        excludes.add("tr");
        supports.add("br");
        supports.add("p");
    }
    private static String format(String source) {
        source = StringUtils.trimToNull(source);
        if (source == null) {
            return "";
        }
        source = source.replaceAll("<br />", BR)
                .replaceAll("<br>", BR)
                .replaceAll("<br></br>", BR)
                .replaceAll("<p></p>", "")
                .replaceAll("<p />", "")
                .replaceAll("<p/>", "");
        while (source.endsWith(BR)){
            source = StringUtils.removeEndIgnoreCase(source, BR);
        }
        return source;
    }

    public static void main(String[] args) {
//        final String s = "afaefasdf<br><br><br></br><br /></br><br><br><br><br>";
//        System.out.println(format(s));
//        String content = "<p><!--[img]34a88f0a1509b896e38a8e68e603a9765310bf10.png[/img]-->”旅行者1号“，空间探测器历经36年的人类星际探索，沿途探索①木星、②土星、③、土卫二、④土卫六是否存在生命痕迹，并发回资料。阅读表格，依据探测资料判断将来最有可能孕育生命的天体是：</p><table><tbody><tr class=\"firstRow\"><td width=\"83\" valign=\"top\"><p>天体</p></td><td width=\"83\" align=\"center\" valign=\"middle\">① </td><td width=\"83\" align=\"center\" valign=\"middle\">② </td><td width=\"83\" align=\"center\" valign=\"middle\">③</td><td width=\"83\" align=\"center\" valign=\"middle\">④</td></tr><tr><td width=\"83\" valign=\"top\"><p>探测资料</p></td><td width=\"83\" valign=\"top\"><p>有浓密的大气层，表面由沸腾的氢组成。</p></td><td width=\"83\" valign=\"top\"><p>有较为平静和单纯的氢和氦组成的大气。</p></td><td width=\"83\" valign=\"top\"><p>大量水汽从地表缝隙喷出，存在地下海洋。</p></td><td width=\"83\" valign=\"top\"><p>存在氮为主以及碳氢化合物的稳定大气。</p><span style=\"line-height:0px;\">\u200D</span></td></tr><tr><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td></tr><tr><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td></tr><tr><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td><td width=\"83\" valign=\"top\"></td></tr></tbody></table><p> </p><br/><br/></br><br>";
        final String content = "<p style=\"display:inline\">依次填入下面横线处的语句，与上下文衔接最恰当的一组是（&nbsp;&nbsp;&nbsp;&nbsp;）</p><p>这便是黄州赤壁，陡峭的石坡直逼着浩荡东去的大江，坡上有险道可以攀登俯瞰，江面有小船可供荡漾浆仰望，地方不大，<span style=\"text-decoration: underline; \">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;&nbsp;</span>，有了视角与空间的变异，有了伟大与渺小的比照，有了视觉空间的变异和倒错，<span style=\"text-decoration: underline; \"> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;&nbsp;</span>。<span style=\"text-decoration: underline; \"> &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;&nbsp;</span>，而不同的游人才使这种可能获得不同程度的实现，产生各具特色的审美愉悦。</p><p>①但一仰一俯之间就有了气势②但一俯一仰之间就有了气势③因此也就有了冥思和游观的价值④因此也就有了游观和冥思的价值⑤主观感情只提供一种审美可能⑥客观物象景物只提供一种审美可能</p>";
        final String s = content.replaceAll("<span style=\"text-decoration: underline; \">*</span>", "_____");
        System.out.println(content);
        System.out.println(s);
        final String s1 = QuestionDubboServiceImpl.convert2MobileLayout(content);
        System.out.println(s1);
        System.out.println(QuestionDubboServiceImpl.convert2MobileLayout(s1));
//        final String s1 = QuestionDubboServiceImpl.convert2MobileLayout("<!--[img]fa951f87a293944302103fc3311ba02fcfef4cd4.png[/img]-->");
    }


}
