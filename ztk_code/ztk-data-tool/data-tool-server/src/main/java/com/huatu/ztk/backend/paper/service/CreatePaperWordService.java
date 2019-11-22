package com.huatu.ztk.backend.paper.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.google.common.collect.Lists;
import com.huatu.ztk.backend.paper.bean.AnswerBean;
import com.huatu.ztk.backend.paper.bean.PaperQuestionBean;
import com.huatu.ztk.backend.paperModule.service.PaperModuleService;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.huatu.ztk.backend.util.UploadFileUtil.IMG_BASE_URL;


/**
 * Created by ht on 2017/3/9.
 */
@Service
public class CreatePaperWordService {

    private static final Logger logger = LoggerFactory.getLogger(CreatePaperWordService.class);

    @Autowired
    private UploadFileUtil uploadFileUtil;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperModuleService paperModuleService;


    /**
     * 获取下载文件的url
     * @param paper
     * @param type
     * @return
     * @throws Exception
     */
    public String getDownFileUrl(Paper paper,int type)throws Exception{
        if (CollectionUtils.isEmpty(paper.getQuestions())) {
            throw new BizException(ErrorResult.create(1000106, "该试卷中不存在试题"));
        }
        String name=paper.getName();
        if(type==ExportType.PAPER_WORD_TYPE_SIDE_STEM){
            name=name+"试题";
        }else if(type==ExportType.PAPER_WORD_TYPE_SIDE_ANSWER){
            name=name+"答案";
        }
        String fileName=FuncStr.replaceDiagonal(name)+".doc";
        File file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH+ fileName);
        String fileUrl=FunFileUtils.WORD_FILE_SAVE_URL+fileName;
        //String fileUrl=FunFileUtils.TMP_WORD_SOURCE_FILEPATH+fileName;
        if(!FunFileUtils.fileExists(file)){
            boolean bln=createUploadFileWord(paper,type,fileName,file);
            if(!bln){
                FunFileUtils.deleteFile(file);
                throw new BizException(ErrorResult.create(1000107, "下载试卷失败"));
            }
        }
        return fileUrl;
    }

    public boolean createUploadFileWord(Paper paper,int type,String name,File file) throws Exception {
        //生成临时文件
        if(file==null){
            file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH+name);
        }
        //向文件中写数据
        boolean bln=createFileWord(file,paper,type);
         if(bln){
             //上传文件
              uploadFileUtil.ftpUploadFile(file, new String(file.getName().getBytes("UTF-8"),"iso-8859-1"), FunFileUtils.WORD_FILE_SAVE_PATH);
         }
        return bln;
    }

    public boolean createFileWord(File file, Paper paper,int type){
        boolean result=true;
        // 设置纸张大小
        Document document =null;
        RtfWriter2 writer=null;
        try{
            // 设置纸张大小
             document = new Document(PageSize.A4);

             writer= RtfWriter2.getInstance(document, new FileOutputStream(file));
            // 设置中文字体
            BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            // 标题字体风格
            Font titleFont = new Font(bfChinese, 16, Font.BOLD);

            Font moduleFont = new Font(bfChinese, 12, Font.BOLD);
            // // 正文字体风格
            Font contentFont = new Font(bfChinese, 10, Font.NORMAL);
            document.open();
            //标题
            Paragraph title = new Paragraph(paper.getName());
            // 设置标题格式对齐方式
            title.setAlignment(Element.ALIGN_CENTER);
            title.setFont(titleFont);
            document.add(title);
            //获取试题信息
            List<PaperQuestionBean> questionList=(List<PaperQuestionBean>) paperService.getQuestionByPaper(paper);
            if(CollectionUtils.isNotEmpty(questionList)&&questionList.size()>0){
                int k=0;
                for (PaperQuestionBean paperQuestionBean:questionList){
                    Question question=paperQuestionBean.getQuestion();
                    QuestionExtend questionExtend=paperQuestionBean.getExtend();
                    List<PaperQuestionBean> questionBeanList= paperQuestionBean.getChildrens();
                    if(question!=null){
                        k++;
                        if(question instanceof GenericQuestion){
                            createFileObject(document,contentFont,k,type,(GenericQuestion)question,questionExtend);
                        }else if(question instanceof GenericSubjectiveQuestion){
                            createFileSubject(document,contentFont,k,type,(GenericSubjectiveQuestion)question,questionExtend);
                        }else if(question instanceof CompositeQuestion){
                            createFileCompositeObject(document,contentFont,k,type,(CompositeQuestion)question,questionBeanList);
                            k=k+((CompositeQuestion) question).getQuestions().size()-1;
                        }else if(question instanceof CompositeSubjectiveQuestion){
                            createFileCompositeSubject(document,contentFont,k,type,(CompositeSubjectiveQuestion)question,questionBeanList);
                            k=k+((CompositeSubjectiveQuestion) question).getQuestions().size()-1;
                        }
                    }
                }
            }
        }catch (Exception e){
            result=false;
           e.printStackTrace();
        }finally {
            document.close();
            //关闭书写器
            writer.close();
        }
       return result;
    }

    /**
     * 创建客观题文件
     * @param document
     * @param font
     * @param question
     */
    private void createFileObject(Document document,Font font,int k,int type,GenericQuestion question,QuestionExtend questionExtend)throws Exception{
        //试题id
        fillBaseContent(k+"、【试题ID】" + "   "+question.getId()+"",font,document);
        if(ExportType.PAPER_WORD_TYPE_ALL==type || ExportType.PAPER_WORD_TYPE_SIDE_STEM==type){
            //题序、题干
            if (!StringUtils.isBlank(question.getStem())){
                fileExistsImg("",question.getStem(),font,document);
            }
            //选项
            if (CollectionUtils.isNotEmpty(question.getChoices())){
                for(int j=0;j<question.getChoices().size();j++){
                    fileExistsImg(AnswerBean.answerMap.get(j+1)+"、",question.getChoices().get(j),font,document);
                }
            }
        }
        if(ExportType.PAPER_WORD_TYPE_ALL==type||ExportType.PAPER_WORD_TYPE_SIDE_ANSWER==type){
            //答案
            fillBaseContent("【答案】" + "   "+convertAnswer(question.getAnswer()+""),font,document);
            //知识点
            fillBaseContent("【知识点】" + "   "+FuncStr.htmlManage(StringUtils.join(question.getPointsName(),",")),font,document);
            //模块
            String moduleName=questionExtend!=null?paperModuleService.findById(questionExtend.getModuleId()).getName():"";
            fillBaseContent("【模块】" + "   "+moduleName,font,document);
            //解析
            fileExistsImg("【解析】" + "   ",FuncStr.htmlManage(question.getAnalysis()),font,document);
            if(StringUtils.isNotEmpty(question.getMaterial())&&question.getParent()<=0){
              fileExistsImg("【资料】" + "   ",FuncStr.htmlManage(question.getMaterial()),font,document);
            }
        }
        // 换行
        document.add(new Paragraph(" "));
    }

    /**
     * 创建主观题文件
     * @param document
     * @param font
     * @param question
     * @param questionExtend
     */
    private void createFileSubject(Document document,Font font,int k,int type,GenericSubjectiveQuestion question,QuestionExtend questionExtend)throws Exception{
        //试题id
        fillBaseContent(k+"、【试题ID】" + "   "+question.getId()+"",font,document);
        if(ExportType.PAPER_WORD_TYPE_ALL==type || ExportType.PAPER_WORD_TYPE_SIDE_STEM==type){
            //题序、注意事项
            if (!StringUtils.isBlank(question.getRequire())){
                fileExistsImg("",question.getStem(),font,document);
            }
            // 注意事项
            fileExistsImg("【注意事项（题干要求）】" + "   ",FuncStr.htmlManage(question.getRequire()),font,document);
            //选项
            if (CollectionUtils.isNotEmpty(question.getMaterials())){
                for(int j=0;j<question.getMaterials().size();j++){
                    fileExistsImg("材料"+(j+1)+"、",question.getMaterials().get(j),font,document);
                }
            }else if(StringUtils.isNotEmpty(question.getMaterial())){
                fileExistsImg("材料1、",question.getMaterial(),font,document);
            }
            //题干
            fileExistsImg("【题干】" + "   ",FuncStr.htmlManage(question.getStem()),font,document);
        }
        if(ExportType.PAPER_WORD_TYPE_ALL==type||ExportType.PAPER_WORD_TYPE_SIDE_ANSWER==type){
            //解析
            fileExistsImg("【参考解析】" + "   ",FuncStr.htmlManage(question.getReferAnalysis()),font,document);
        }
        // 换行
        document.add(new Paragraph(" "));
    }

    /**
     * 创建复合客观题文件
     * @param document
     * @param font
     * @param question
     * @param questionBeanList
     */
    private void createFileCompositeObject(Document document, Font font,int k,int type, CompositeQuestion question, List<PaperQuestionBean> questionBeanList)throws Exception{
        //题序
        fillBaseContent("复合客观题", font, document);
        //试题id
        fillBaseContent("【试题ID】" + "   "+question.getId()+"",font,document);
        if(ExportType.PAPER_WORD_TYPE_ALL==type || ExportType.PAPER_WORD_TYPE_SIDE_STEM==type) {
            //材料
            if (CollectionUtils.isNotEmpty(question.getMaterials())) {
                for (int j = 0; j < question.getMaterials().size(); j++) {
                    fileExistsImg("材料" + (j + 1) + "、", question.getMaterials().get(j), font, document);
                }
            }else if(StringUtils.isNotEmpty(question.getMaterial())){
                fileExistsImg("材料1 、", question.getMaterial(), font, document);

            }
        }
        //小题列表
        if(CollectionUtils.isNotEmpty(questionBeanList)&&questionBeanList.size()>0){
            int c=0;
            for (PaperQuestionBean paperQuestionBean:questionBeanList){
                Question subQuestion= paperQuestionBean.getQuestion();
                QuestionExtend subQuestionExtend=paperQuestionBean.getExtend();
                if(subQuestion!=null){
                    if(subQuestion instanceof  GenericQuestion){
                        createFileObject(document,font,k+c,type,(GenericQuestion)subQuestion,subQuestionExtend);
                    }else if(subQuestion instanceof GenericSubjectiveQuestion){
                        //试题id
                        fillBaseContent(k+c+"、【试题ID】" + "   "+subQuestion.getId()+"",font,document);
                        if(ExportType.PAPER_WORD_TYPE_ALL==type || ExportType.PAPER_WORD_TYPE_SIDE_STEM==type){
                            //题干
                            fileExistsImg("【题干】" + "   ",FuncStr.htmlManage(((GenericSubjectiveQuestion)subQuestion).getStem()),font,document);
                        }
                        if(ExportType.PAPER_WORD_TYPE_ALL==type||ExportType.PAPER_WORD_TYPE_SIDE_ANSWER==type){
                            //解析
                            fileExistsImg("【参考解析】" + "   ",FuncStr.htmlManage(((GenericSubjectiveQuestion)subQuestion).getReferAnalysis()),font,document);
                        }
                    }
                    c++;
                }
            }
        }
        // 换行
        document.add(new Paragraph(" "));

    }

    /**
     * 创建复合主观题文件
     * @param document
     * @param font
     * @param question
     * @param questionBeanList
     */
    private void createFileCompositeSubject(Document document, Font font,int k,int type, CompositeSubjectiveQuestion question,List<PaperQuestionBean> questionBeanList)throws Exception{
        //题序
        fillBaseContent("复合主观题", font, document);
        fillBaseContent("【试题ID】" + "   "+question.getId()+"",font,document);
        if(ExportType.PAPER_WORD_TYPE_ALL==type || ExportType.PAPER_WORD_TYPE_SIDE_STEM==type) {
            // 注意事项
            fileExistsImg("【注意事项（题干要求）】" + "   ",FuncStr.htmlManage(question.getRequire()),font,document);
            //选项
            if (CollectionUtils.isNotEmpty(question.getMaterials())){
                for(int j=0;j<question.getMaterials().size();j++){
                    fileExistsImg("材料"+(j+1)+"、",question.getMaterials().get(j),font,document);
                }
            }else if(StringUtils.isNotEmpty(question.getMaterial())){
                fileExistsImg("材料1、",question.getMaterial(),font,document);

            }
        }
        //小题列表
        if(CollectionUtils.isNotEmpty(questionBeanList)&&questionBeanList.size()>0){
            int c=0;
            for (PaperQuestionBean paperQuestionBean:questionBeanList){
                Question subQuestion= paperQuestionBean.getQuestion();
                QuestionExtend subQuestionExtend=paperQuestionBean.getExtend();
                if(subQuestion!=null){
                    if(subQuestion instanceof  GenericQuestion){
                        createFileObject(document,font,k+c,type,(GenericQuestion)subQuestion,subQuestionExtend);
                    }else if(subQuestion instanceof GenericSubjectiveQuestion){
                        fillBaseContent(k+c+"、【试题ID】" + "   "+subQuestion.getId()+"",font,document);
                        if(ExportType.PAPER_WORD_TYPE_ALL==type || ExportType.PAPER_WORD_TYPE_SIDE_STEM==type){
                            //题干
                            fileExistsImg("【题干】" + "   ",FuncStr.htmlManage(((GenericSubjectiveQuestion)subQuestion).getStem()),font,document);
                        }
                        if(ExportType.PAPER_WORD_TYPE_ALL==type||ExportType.PAPER_WORD_TYPE_SIDE_ANSWER==type){
                            //解析
                            fileExistsImg("【参考解析】" + "   ",FuncStr.htmlManage(((GenericSubjectiveQuestion)subQuestion).getReferAnalysis()),font,document);
                        }
                    }
                    c++;
                }
            }
        }
        // 换行
        document.add(new Paragraph(" "));
    }

    public String convertAnswer(String answer){
        String answers=answer.replace("1","A")
                .replace("2","B")
                .replace("3","C")
                .replace("4","D")
                .replace("5","E")
                .replace("6","F")
                .replace("7","G")
                .replace("8","H");
        return answers;
    }
    /**
     * 填充基本内容
     * @param value
     * @param font
     * @param document
     * @throws Exception
     */
    public void fillBaseContent(String value,Font font,Document document) throws Exception{
        Paragraph paragraph = new Paragraph(value,font);
        paragraph.setIndentationLeft(20);
        paragraph.setFirstLineIndent(20);
        document.add(paragraph);
    }

    /**
     * 填充包含图片的内容
     * @param value
     * @param font
     * @param document
     */
    public void fileExistsImg(String index, String value, Font font, Document document){
        String strTemp = FuncStr.htmlManage(value); //处理基本标签《br/》
        List<Object> parts = Lists.newArrayList();
        try {
//            splitContentAndAddToList(index,strTemp,parts);
            splitContentAndAddToList1(index+strTemp,parts);
            Paragraph paragraph = new Paragraph("", font);
            paragraph.setAlignment(Element.ALIGN_MIDDLE);
            for (Object part : parts) {
                try{
                    paragraph.add(part);
                }catch (Exception e){
                    logger.error("part={}",part);
                    e.printStackTrace();
                }

            }
            paragraph.setIndentationLeft(20);
            paragraph.setFirstLineIndent(20);
            document.add(paragraph);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     *
     * @param index
     * @param strTemp  index+strTmep 是数据处理对象
     * @param parts     存储字段和图片对象
     * @throws IOException
     * @throws BadElementException
     */
    private void splitContentAndAddToList(String index, String strTemp, List<Object> parts) throws IOException, BadElementException {
        if (StringUtils.isNotEmpty(strTemp)&&strTemp.indexOf("<img") != -1){
            //图片前的文字处理
            parts.add(index + strTemp.substring(0, strTemp.indexOf("<img")));
            //图片处理
            String imgUrl = "";
            Map<String,Integer> sortedMap=FuncStr.sortMap(strTemp);
            for(String key:sortedMap.keySet()){
                Integer v=sortedMap.get(key);
                if(v>-1){
                    imgUrl = strTemp.substring(strTemp.indexOf("src") + 5, strTemp.indexOf("."+key) + 4);
                    break;
                }
            }
            logger.info("fileExistsImg=url={}",imgUrl);
            if(StringUtils.isNotEmpty(imgUrl)&&imgUrl.length()>0){
                Image tempImage;
                try{
                    tempImage = Image.getInstance(new URL(imgUrl));
                }catch (Exception e){
                    tempImage = Image.getInstance(new URL("http://120.76.154.176/400/"+imgUrl.substring(imgUrl.lastIndexOf(File.separator))));
                }
                tempImage.scalePercent(80f);
                parts.add(tempImage);
            }
            //图片后
            // document.add(new Paragraph(strTemp.substring(strTemp.indexOf(">") + 1), font));
            if(StringUtils.isNotEmpty(strTemp)&&strTemp.indexOf("</img>") != -1){
                String endContent=strTemp.substring(strTemp.indexOf("</img>") + 6);
                splitContentAndAddToList("",endContent,parts);
            }else{
                splitContentAndAddToList("",strTemp.substring(strTemp.indexOf(">") + 1),parts);
            }

        }else{
            parts.add(index+strTemp);
        }
    }
    private void splitContentAndAddToList1(String strTemp, List<Object> parts) throws IOException, BadElementException, BizException {
        Pattern pattern = Pattern.compile("<[/]?img[^>]*>");
        StringBuilder sb = new StringBuilder(strTemp);
        Matcher matcher = pattern.matcher(sb);
        int i = 0;
        while(matcher.find(i)){
            if(matcher.start()>0){
                //处理图片前的文本内容
                parts.add(sb.substring(0,matcher.start()));
            }
            //start~end 是图片信息组成Image信息
            parts.add(assertImg(matcher.group()));
            sb.delete(0,matcher.end());
        }
        //图片之后的数据处理
        parts.add(sb.toString());
    }

    private Object assertImg(String content) throws BizException, IOException, BadElementException {
        String imgUrl = subAttrString(content,"src");
        if("".equals(imgUrl)){
            return "【图片暂缺】("+content+")";
        }
        String widthString = subAttrString(content,"width");
        String heightString = subAttrString(content,"height");
        Image tempImage = null;
        if(StringUtils.isNotEmpty(imgUrl)&&imgUrl.length()>0){
            try{
                tempImage = Image.getInstance(new URL(imgUrl));
            }catch (Exception e){
                tempImage = Image.getInstance(new URL("http://120.76.154.176/400/"+imgUrl.substring(imgUrl.lastIndexOf(File.separator))));
            }
        }
        if(tempImage!=null){
            if(!"".equals(widthString)){
                tempImage.scalePercent(Float.parseFloat(widthString.replace("px",""))/tempImage.getWidth()*80f);
                logger.info("content={},widthString={},width={}",content,widthString,tempImage.getWidth());
            }
            if(!"".equals(heightString)){
                tempImage.scalePercent(Float.parseFloat(heightString.replace("px",""))/tempImage.getHeight()*80f);
                logger.info("content={},heightString={},height={}",content,heightString,tempImage.getHeight());
            }
            tempImage.setAlignment(Image.MIDDLE);
        }

        return tempImage;
    }

    private String subAttrString(String content, String attr) throws BizException {
        int index = content.indexOf(attr);
        if(index!=-1){
            String url = FunFileUtils.getQuoteContent(content.substring(index+1));
            return url;
        }else{
            logger.error("没有{}属性，content={}",attr,content);
//            throw new BizException(ErrorResult.create(12101021,"src属性不匹配"));
        }
        return "";
    }


}
