package jbzm.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TestPdf {
    public static void main(String args[]) throws Exception {
        String path = "D:/1.pdf";
        String test ="<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1.中国是文明古国，为世界文明的发展做出了卓越贡献。根据有关专家的说法，从公元6世纪到17世纪初的世界重大科技成果中，中国人创造的成果一直占比在50%以上。然而，到了17世纪中叶之后，中国的科学技术发展速度慢了下来。这种局面直到新中国成立后才出现巨大改变。上世纪50年代以来，尤其是80年代后，我国科技发展迅速。2016年7月中国科学技术发展战略研究发布的《国家创新指数报告2015》显示，在世界40个主要国家中，中国创新能力排名第18位，与排名靠前国家的差距进一步缩小。中国国家创新指数远远超过处于同一经济发展水平的国家（如印度和南非），得分接近人均国内生产总值（GDP）在5万美元左右的欧洲国家。报告显示，中国主要指标已达到世界领先水平。2014年，中国在被列入美国《科学论文索引》（SCI）中的学术刊物上发表的论文数量达到25.0万篇，居全球第2位，占到全球总量的13.3%。2014年，中国国内发明专利申请量达到80.1万件，占世界总量的47.5%，连续5年居世界首位；国内发明专利授权量达到16.3万件，占世界总量的24.8%。2014年，中国高技术产业出口占制造业出口的比重达到27.0%，占比居世界第3位。知识密集型服务业增加值占世界比重由2000年的2.8%提高到10.4%。<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;另外，由世界知识产权组织、康奈尔大学、英士国际商学院共同发布的2016年全球创新指数显示，中国位列世界最具创新力经济体第25位，较前一年上升4位。世界知识产权组织表示，在过去9年的调查中，高度发达经济体在全球创新指数中一直占据主导地位，此次中国进入25强，标志着中等收入国家的创新能力首次达到了高度发达经济水平。</p>";
        long startTime = new Date().getTime();
        Document document = new Document(PageSize.A4, 45, 45, 100, 34);
        PdfUtil pdf = new PdfUtil(path, document);
        document.open();
        pdf.addTitle("2017年408广东省考：分析说明B镇推动菠萝产业发展的成功经验。（广东乡镇）", document);
        pdf.addBlank(10, document);
        pdf.addLittleTitle("考试时间：30分钟", document);
        pdf.addBlank(20, document);
        pdf.addBaseTitle("【给定资料】", document);
        pdf.addBaseTitle(" 资料一", document);
        pdf.addBaseContent(changeString(test), document);
        pdf.addBlank(20, document);
        pdf.addBlank(10, document);
        pdf.addForm(300, document);
        document.newPage();

        pdf.addBaseContent(changeString(test), document);
        pdf.addBlank(20, document);
        document.newPage();
        pdf.addBaseContent("她投资500多万建成了菠萝罐头加工厂，另外承包了3000多亩土地种植菠萝保证原料供应，成为当地有名的菠萝深加工巨头。目前，她的菠萝罐头厂年产罐头3000多吨，年收入达到700多万。\n" +
                "为鼓励当地推行菠萝加工业的发展模式，B镇特地组织了种植大户到国外考察学习，引导种植大户引进先进设备提高农产品加工转化率和附加值。“菠萝罐头厂的成功就是延长了菠萝的产业链，克服了菠萝保质期短，不利于长途运输的弱点。引进加工技术设备，有利于大家分工更明确，做到标准化、规模化经营，形成产业链。这样才能推进菠萝深加工产业发展。”B镇一负责人说。\n", document);
        String img = "“城中村”环境卫生脏乱差一直是制约城市化进程的严重问题。据一份《×市×区×村环境卫生管理情况调查》的报告显示，该村“垃圾随处可见，各种广告贴满墙壁”。记者看到，村里的确张贴着不少落款是该村村委会治安办的标语。“多少年了，乱扔垃圾问题越来越严重，但一直解决不了。”一名村干部说，由于该村属于城中村，流动<img src=\"http://img02.tooopen.com/images/20141231/sy_78327074576.jpg\" width=\"500\" height=\"140\"/>人口多，关于乱扔垃圾的问题，村委会想了诸多办法都无法解决。他介绍说：“村里曾派保洁员现场盯守，但经常一去吃饭，垃圾又遍地都是，罚款有时候都不管用，我们反而还总被人冤枉。”由此看来，简单罚款有时并不能解决根本问题。</p><p></p>";
        String[] imgSpl = img.split("<img[^>]+>");
        System.out.println(imgSpl[0].replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""));
        System.out.println("===========");
        Pattern p = Pattern.compile("<img[^>]+>");
        Matcher m = p.matcher(img);
        pdf.addBlank(5, document);
        pdf.addBaseContent(imgSpl[0].replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""), document);
        int i = 1;
        while (m.find()) {
            String lol = m.group();
            Pattern p1 = Pattern.compile("\\\"([^\\\"]*)+\\\"");
            Matcher m1 = p1.matcher(lol);
            if (m1.find()) {
                String imageName = "D:/xzx.png";
                String url = m1.group().substring(1, m1.group().length() - 1);
                pdf.downloadPicture(url, imageName);
                //读取一个图片
                String toImagePath = "D:/test.jpg";
                //String toImagePath = ESSAY_PDF_PICTURE_DATA + UUID.randomUUID().toString().replaceAll("-", "") + ".jpg";
                Thumbnails.of(imageName).size(510, 800).toFile(toImagePath);
                Image image = Image.getInstance(toImagePath);
                System.out.println(image.getWidth());
                //插入一个图片
                document.add(image);
                pdf.addBaseContent(imgSpl[i].replaceAll("<[^>]+>", "").replaceAll("&nbsp;", ""), document);
            }
            System.out.println(lol);
            i++;
        }
        String str = "1.[转变(1)]管理[观念(1)]，采取管理与<服务(2)>并举2.变单一罚款方式为各种<管理方式(3)>有机结合，要体现管理方式多样性，执行的[灵活(4)]性3.强化管理和[执法(4)]的<规范性(5)>";
        pdf.correct(str, document);
        pdf.addBlank(20, document);
        document.close();
        //pdf.addWaterMark(pdfFile,"华图教育",200,200);
        long endTime = new Date().getTime();
       // pdf.addWaterImage(64, 100, path, "D:/logo.png");
        System.out.println(endTime - startTime);
    }
    private static String changeString(String str) {
        //切割</br>和</p>
        String text = str.replaceAll("<br/>", "\n").replaceAll("</p>", "\n").replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ");
        return text;
    }
}
