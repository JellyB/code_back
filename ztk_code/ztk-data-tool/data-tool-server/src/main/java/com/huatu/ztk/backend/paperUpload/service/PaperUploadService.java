package com.huatu.ztk.backend.paperUpload.service;

import com.huatu.ztk.backend.paperUpload.bean.PaperUploadError;
import com.huatu.ztk.backend.paperUpload.bean.PaperUploadRedisKeys;
import com.huatu.ztk.backend.paperUpload.dao.LogIterator;
import com.huatu.ztk.backend.paperUpload.dao.WmfToPng;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by hqp on 2017/5/5.
 */
@Service
public class PaperUploadService extends LogIterator {
    private static final Logger logger  = LoggerFactory.getLogger(PaperUploadService.class);
    @Autowired
    private UploadFileUtil uploadFileUtil;
    private static final String targetFileName = "disconf\\download\\";


    public String docToHtml(File dest) throws Exception {
        int index =dest.getName().lastIndexOf(".");
        if(!"doc".equals(dest.getName().substring(index+1))){
            dest.deleteOnExit();
            throw new BizException(ErrorResult.create(10001,"文件格式错误"));
        }
        FileInputStream fis = new FileInputStream(dest);
        HWPFDocument wordDocument = new HWPFDocument(fis);
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(document);
        // 保存图片，并返回图片的相对路径
        wordToHtmlConverter.setPicturesManager((content, pictureType, name, width, height) -> {
            //创建临时图片文件
            File tempFile = null;
            File wmfFile = null;
            if(name.lastIndexOf(".")!=-1){
                name = name + ".png";
            }
            String type = name.substring(name.lastIndexOf(".")).toLowerCase();
            String tempPath = targetFileName+name;
            try {
                FileOutputStream out = new FileOutputStream(tempPath);
                out.write(content);
                out.close();
                if(type.equals(".wmf")){
                    wmfFile = new File(tempPath);
                    tempPath = WmfToPng.convert(tempPath);
                    name = name.replace("wmf","png");
                }
                tempFile = new File(tempPath);
                String path = "";
                path = uploadFileUtil.ftpUploadPicByThread(tempFile);
                logger.info("preImageName={},preImageType={},toFtpClient={}",name,pictureType,path);
                return path;
            }catch (IOException e) {
                e.printStackTrace();
            }catch (BizException e1){
                e1.printStackTrace();
            }finally{
//                tempFile.deleteOnExit();
//                if(wmfFile!=null){
//                    wmfFile.deleteOnExit();
//                }
            }
            return "";
        });
        wordToHtmlConverter.processDocument(wordDocument);
        Document htmlDocument = wordToHtmlConverter.getDocument();
        DOMSource domSource = new DOMSource(htmlDocument);
        long time = System.currentTimeMillis();
        StreamResult streamResult = new StreamResult(new File(targetFileName+time+".html").toURI().getPath());

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.METHOD, "html");
        serializer.transform(domSource, streamResult);
        if(fis!=null){
            fis.close();
            dest.delete();
        }
        return targetFileName+time+".html";
    }

    /**
     * 解析HTML文档
     * @throws Exception
     * @param  htmlPath
     */
    public LinkedList<String> anayleHtml(String htmlPath) throws Exception{
        //读取文件数据
        File file = new File(htmlPath);
        InputStreamReader in = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)));
        StringBuilder input = new StringBuilder();
        int ch;
        while ((ch = in.read()) != -1) input.append((char) ch);
        if(in!=null){
            in.close();
            file.delete();
        }
        //得到段落列表
        LinkedList<String> eleList = getTagInnerHtml(input);
        return eleList;
    }

    /**
     * 通过解析数据得到所有的有用数据<img>和<p></p>标签
     * @param builder
     * @return
     */
    public LinkedList getTagInnerHtml(StringBuilder builder) throws Exception{
        //去除文档中的无用标签
        builder = doTagOperatePre(builder);
        Pattern pattern = Pattern.compile("<([/]?p)>");
        Matcher matcher = pattern.matcher(builder.toString());
        int i = 0;
        int start = 0;
        LinkedList<String> contentList = new LinkedList<>();
        while(matcher.find(i)) {
            i = matcher.end();
            if(matcher.group(1).equals("p")){
                start = matcher.end();
            }else{
                String eleStr = builder.substring(start,matcher.start());
                contentList.addLast(imgManage(eleStr));
            }
        }
        return contentList;
    }
    /**
     * 过滤掉img标签中的所有属性
     * @param eleStr
     * @return
     */
    private String imgManage(String eleStr) {
        Pattern pattern = Pattern.compile("<img[^>]+>");
        Matcher matcher = pattern.matcher(eleStr);
        int i = 0;
        while(matcher.find(i)){
            String tmp = matcher.group().replaceAll("style=\"[^\"]\"","");
            eleStr = eleStr.substring(0,matcher.start())+tmp+eleStr.substring(matcher.end());
            i=matcher.start()+tmp.length();
        }
        return eleStr;
    }

    /**
     * 对builder中的标签做处理
     * 1、判断是否存在table标签，如果存在直接抛错
     * 2、判断文本中是否存在“text-decoration:underline”，如果存在证明有下划线，将对应的标签全部转化为<u></u>
     * 3、去掉所有非<p></p><u></u><span></span><br>标签及其中的内容
     * @param builder
     * @return
     */
    private StringBuilder doTagOperatePre(StringBuilder builder) throws Exception{
        Pattern pattern = Pattern.compile("<table[^>]*>[^(table)]*</table>");
        Matcher matcher =pattern.matcher(builder);
        int l =0;
        while(matcher.find(l)){
            l =matcher.end();
            int start = matcher.start();
            int end = matcher.end();
            String error = "文档中存在表格格式的数据，请将其转化为图片格式;";
            String error1 = "";
            Pattern pattern1 = Pattern.compile("<span[^>]*>([^<]+)</span>");
            Matcher matcher1 =pattern1.matcher(builder);
            if(matcher1.find(l)){
                error1=matcher1.group(1);
            }
            if(!"".equals(error1)){
                logger.error(error+"表信息在文档内容：\""+error1+"\"之前");
                this.setLoggerList(PaperUploadError.builder()
                        .errorMsg(error)
                        .location(error+"表信息在\"第"+error1+"题\"附近")
                        .floor("doTagOperatePre").build());
            }else{
                logger.error(error+"表信息在试卷结尾处");
                this.setLoggerList(PaperUploadError.builder()
                        .errorMsg(error)
                        .location(error+"表信息在试卷结尾处")
                        .floor("doTagOperatePre").build());
            }
            builder.delete(start,end);
            l = start;
        }
        pattern = Pattern.compile("<style[^>]*>[^<]*</style>");
        matcher = pattern.matcher(builder);
        int s = 0;
        Set<String> uSet = new HashSet();
        while(matcher.find(s)){
            String style = matcher.group();
            getUTagCss(style,uSet);
            builder.delete(matcher.start(),matcher.end());
            s = matcher.start();
        }
        pattern = Pattern.compile("<(?!p|span|br|u|/p|/span|/u|img)[^>]*>");
        matcher = pattern.matcher(builder);
        int i=0;
        Map<String,Integer> map = new HashMap();
        while(matcher.find(i)){
            String str =  matcher.group();
            if(map.get(str)==null){
                map.put(str,1);
            }else{
                map.put(str,map.get(str)+1);
            }
            builder.delete(matcher.start(),matcher.end());
            i = matcher.start();
        }
        for(String key:uSet){
            pattern = Pattern.compile("<span class=\""+key+"\">([^<]*)</span>");
            matcher = pattern.matcher(builder);
            int m = 0 ;
            while(matcher.find(m)){
                String tmp = "<u>"+matcher.group(1)+"</u>";
                builder.replace(matcher.start(),matcher.end(),tmp);
                m = matcher.start();
            }
        }
        pattern = Pattern.compile("^([\\s]+)[^//s]");
        matcher = pattern.matcher(builder);
        int s1  = 0;
        while(matcher.find(s1)){
            builder.delete(matcher.start(1),matcher.end(1));
            s1 = matcher.start();
        }
        pattern = Pattern.compile("<[^>]+>([\\s]+)<[^>]+>");
        matcher = pattern.matcher(builder);
        while(matcher.find(s1)){
            builder.delete(matcher.start(1),matcher.end(1));
            s1 = matcher.start();
        }
        pattern = Pattern.compile("[^//s]([\\s]+)$");
        matcher = pattern.matcher(builder);
        while(matcher.find(s1)){
            builder.delete(matcher.start(1),matcher.end(1));
            s1 = matcher.start();
        }
        pattern = Pattern.compile("<p[^>]+>");
        matcher = pattern.matcher(builder);
        int d = 0;
        while(matcher.find(d)){
            builder.replace(matcher.start(),matcher.end(),"<p>");
            d = matcher.start();
        }
        pattern = Pattern.compile("<[/]?span[^>]*>");
        matcher = pattern.matcher(builder);
        int ds = 0;
        while(matcher.find(ds)){
            builder.delete(matcher.start(),matcher.end());
            ds = matcher.start();
        }
        pattern = Pattern.compile("<br>");
        matcher = pattern.matcher(builder);
        int br = 0;
        while(matcher.find(br)){
            builder.replace(matcher.start(),matcher.end(),"</p><p>");
            br = matcher.start();
        }
        return builder;
    }

    private void getUTagCss(String style, Set<String> uSet) {
        //找到所有含有下划线定义的css格式
        Pattern pattern = Pattern.compile("\\.([^\\{]+)\\{[^\\}]*(text-decoration:underline;)[^\\}]*\\}");
        Matcher matcher = pattern.matcher(style);

        int u=0;
        while(matcher.find(u)){
            u = matcher.end();
            uSet.add(matcher.group(1));
        }
    }

}
