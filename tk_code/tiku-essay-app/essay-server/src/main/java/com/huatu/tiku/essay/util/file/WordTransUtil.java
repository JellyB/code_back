//package com.huatu.tiku.essay.util.file;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.lowagie.text.*;
//import com.lowagie.text.pdf.BaseFont;
//import com.lowagie.text.rtf.RtfWriter2;
//import org.apache.poi.POIXMLDocument;
//import org.apache.poi.POIXMLTextExtractor;
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.extractor.WordExtractor;
//import org.apache.poi.openxml4j.opc.OPCPackage;
//import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//
//import java.io.*;
//import java.util.List;
//import java.util.Map;
//
//public class WordTransUtil {
//
//    public static void main(String[] args) {
//        String fileName = "/Users/zhouwei/Documents/题库测试脚本测试02.doc";
//        readWord_2003(new File(fileName));
//    }
//
//    public static String readWord_2003(File file) {
//        String text = "";
//        try {
//            InputStream stream = new FileInputStream(file);
//            HWPFDocument document = new HWPFDocument(stream);
//            WordExtractor word = new WordExtractor(document);
//            text = word.getText();
//            //去掉word文档中的多个换行
//            text = text.replaceAll("(\\r\\n){2,}", "\r\n");
//            text = text.replaceAll("(\\n){2,}", "\n");
//            stream.close();
//
//
//            String t[] = text.split("\n");
//            Map<String, List<String>> map = Maps.newTreeMap();
//            int start = -1;
//            int end = -1;
//            List list = Lists.newArrayList();
//            for (int i = 0; i < t.length; i++) {
//                t[i] = t[i].trim();
//                //记录每道题题干的索引
//                if ((t[i].startsWith("(") || t[i].startsWith("（")) && (t[i].contains("题)") || t[i].contains("题）"))) {
//                    list.add(i);
//                }
//            }
//            //把最后一行的值作为最后一道题的结束索引
//            list.add(t.length - 1);
//            //   System.out.println(list.size());
//
//            //   list.forEach(x-> System.out.println(x));
//
//            int m = 0;
//            for (int i = 0; i < t.length; i++) {
//                if (t[i].contains("标签")) {
//                    //  找到标签的位置，找到标签匹配的试题开头和结尾  结尾是下道题的开头-1
//                    start = (Integer) list.get(m);
//                    m = m + 1;
//                    //防止溢出
//                    m = m >= list.size() ? m - 1 : m;
//                    end = (Integer) list.get(m);
//                    end = end - 1;
//                    String s = t[i];
//                    //截取出标签的年份地区考试科目
//                    String temp = s.substring(0, s.lastIndexOf("，"));
//
//
//                    String value = t[start];
//                    for (int j = start + 1; j <= end; j++) {
//                        value += "\n";
//                        value += t[j];
//                    }
//                    List<String> co = map.get(temp);
//                    //处理后的标签作为key，存入map中，value是该标签对应所有的题
//                    if (co == null) {
//                        co = Lists.newArrayList();
//                        co.add(value);
//                        map.put(temp, co);
//                    } else {
//                        co.add(value);
//                        map.put(temp, co);
//                    }
//                    // map.put(m+""+temp+"i:"+i+"s:"+start+"end:"+end,value);
//
//                }
//                // System.out.println(i+":"+t[i]);
//            }
//            map.forEach((k, v) -> {
//                try {
//                    //每个标签生成一个文档
//                    createDoc(k, v);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
////            for(int i=0;i<t.length;i++){
////                System.out.println(i+""+t[i]);
////            }
//
//
//            return text;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    public static String readWord_2007(String fileName) {
//        String text = "";
//        try {
//            OPCPackage oPCPackage = POIXMLDocument.openPackage(fileName);
//            XWPFDocument xwpf = new XWPFDocument(oPCPackage);
//            POIXMLTextExtractor ex = new XWPFWordExtractor(xwpf);
//            text = ex.getText();
//            //去掉word文档中的多个换行
//            text = text.replaceAll("(\\r\\n){2,}", "\r\n");
//            text = text.replaceAll("(\\n){2,}", "\n");
//            System.out.println("读取Word文档成功！");
//            return text;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    public static String createDoc(String name, List<String> co) throws Exception {
//        String content = "";
//        for (String c : co) {
//            content = content + "\n" + c;
//        }
//        Document document = new Document(PageSize.A4);
//        File file2 = new File("/Users/zhouwei/Documents/doc/" + name + ".doc");
//        RtfWriter2 writer = RtfWriter2.getInstance(document, new FileOutputStream(file2));
//        document.open();
//        // 设置中文字体
//        BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
//        Font titleFont = new Font(bfChinese, 16, Font.BOLD);
//        //标题
//        Paragraph title = new Paragraph(name);
//        // 设置标题格式对齐方式
//        title.setAlignment(Element.ALIGN_CENTER);
//        title.setFont(titleFont);
//        document.add(title);
//        //试题信息
//        Paragraph contentParagraph = new Paragraph(content);
//        // 设置格式对齐方式
//        contentParagraph.setAlignment(Element.ALIGN_LEFT);
//        // 正文字体风格
//        Font contentFont = new Font(bfChinese, 10, Font.NORMAL);
//        contentParagraph.setFont(contentFont);
//        document.add(contentParagraph);
//        //关闭文档
//        document.close();
//        //关闭书写器
//        writer.close();
//        return null;
//    }
//
//
//}
