package com.huatu.ztk.backend.paper.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.huatu.ztk.backend.paper.bean.AnswerBean;
import com.huatu.ztk.backend.paper.bean.ModuleBean;
import com.huatu.ztk.backend.paper.bean.PaperBean;
import com.huatu.ztk.backend.paper.bean.PaperQuestionBean;
import com.huatu.ztk.backend.paperModule.service.PaperModuleService;
import com.huatu.ztk.backend.question.service.QuestionService;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.backend.util.PDFHeaderFooter;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by ht on 2017/3/1.
 */
@Service
public class CreatePaperPdfService {
    private static final Logger logger = LoggerFactory.getLogger(CreatePaperPdfService.class);

    @Autowired
    private UploadFileUtil uploadFileUtil;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private PaperModuleService paperModuleService;

    @Autowired
    private PaperService paperService;
    /**
     * 下载文件url
     * @param paper
     */
    public String downFileUrl(Paper paper,int type)throws Exception{
        if (CollectionUtils.isEmpty(paper.getQuestions())) {
            throw new BizException(ErrorResult.create(1000106, "该试卷中不存在试题"));
        }
        String name=paper.getName();
        if(type==ExportType.PAPER_PDF_TYPE_STEM){
            name=name+"试题";
        }else if(type==ExportType.PAPER_PDF_TYPE_ANSER){
            name=name+"答案";
        }
        String fileName=FuncStr.replaceDiagonal(name)+".pdf";

        File file = new File(FunFileUtils.TMP_PDF_SOURCE_FILEPATH+ fileName);
        String fileUrl=FunFileUtils.PDF_FILE_SAVE_URL+fileName;
        //String fileUrl=FunFileUtils.TMP_PDF_SOURCE_FILEPATH+fileName;
        if(!FunFileUtils.fileExists(file)){
            boolean bln= createUploadFilePdf(paper,type,fileName,file);
            if(!bln){
                FunFileUtils.deleteFile(file);
                throw new BizException(ErrorResult.create(1000107, "下载试卷失败"));
            }
        }
        return fileUrl;
    }

    public boolean createUploadFilePdf(Paper paper,int type,String name,File file)throws Exception{
        //生成临时文件
        if(file==null){
            file = new File(FunFileUtils.TMP_PDF_SOURCE_FILEPATH+ name);
        }
        //向文件中写数据
        boolean bln= createFilePdf(file,paper,type);
        if(bln){
            //上传文件
            uploadFileUtil.ftpUploadFile(file, new String(file.getName().getBytes("UTF-8"),"iso-8859-1"), FunFileUtils.PDF_FILE_SAVE_PATH);
        }
       return bln;
    }
    public boolean createFilePdf(File file, Paper paper, int type)throws Exception{
        boolean result=true;
        Document document =null;
        PdfWriter writer =null;
        try {
            //创建文件
            document = new Document(PageSize.A4, 50, 50, 50, 50);
            // 汉字处理
            BaseFont bfChinese = BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font common_fontChinese = new Font(bfChinese, 12, Font.BOLD); //大栏目字体
            Font content_fontChinese = new Font(bfChinese, 10, Font.NORMAL); //一般数据字体
            //建立一个书写器，想file中写数据
            writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            // 添加页眉和页脚
            Rectangle rect = new Rectangle(36, 54, 559, 788);
            rect.setBorderColor(BaseColor.GRAY);
            writer.setBoxSize("art", rect);
            PDFHeaderFooter header = new PDFHeaderFooter(paper.getName(),FunFileUtils.PDF_FILE_SAVE_URL+"huatuzhuantikulogo.png", FunFileUtils.PDF_FILE_SAVE_URL+"zhuantikuwatermark.jpg",
                common_fontChinese);
            /*PDFHeaderFooter header = new PDFHeaderFooter(paper.getName(),FunFileUtils.TMP_PDF_SOURCE_FILEPATH+"huatuzhuantikulogo.png", FunFileUtils.TMP_PDF_SOURCE_FILEPATH+"zhuantikuwatermark.jpg",
                    common_fontChinese);*/
            writer.setPageEvent(header);
            //打开文件
            document.open();
            // 首页设置
            Font cover_fontChinese = new Font(bfChinese, 20, Font.BOLD);
            for (int i = 0; i < 8; i++) {
                document.add(new Paragraph(" "));
            }
            Paragraph coverContent = new Paragraph(paper.getName(), cover_fontChinese);
            coverContent.setAlignment(Element.ALIGN_CENTER);
            document.add(coverContent);
            //增加空白行
            for (int i = 0; i < 5; i++) {
                document.add(new Paragraph(" "));
            }
            //获取试题信息
            List<PaperQuestionBean> questionList=(List<PaperQuestionBean>) paperService.getQuestionByPaper(paper);
            if(CollectionUtils.isNotEmpty(questionList)&&questionList.size()>0){
                int k=0;//计数器
                for (PaperQuestionBean paperQuestionBean:questionList){
                    Question question=paperQuestionBean.getQuestion();
                    QuestionExtend questionExtend=paperQuestionBean.getExtend();
                    List<PaperQuestionBean> questionBeanList= paperQuestionBean.getChildrens();
                    if(question!=null){
                        k++;
                        if(question instanceof GenericQuestion){
                            createFileObject(document,content_fontChinese,k,type,(GenericQuestion)question,questionExtend);
                        }else if(question instanceof GenericSubjectiveQuestion){
                            createFileSubject(document,content_fontChinese,k,type,(GenericSubjectiveQuestion)question,questionExtend);
                        }else if(question instanceof CompositeQuestion){
                            createFileCompositeObject(document,content_fontChinese,k,type,(CompositeQuestion)question,questionBeanList);
                            k+=(((CompositeQuestion) question).getQuestions().size())-1;
                        }else if(question instanceof CompositeSubjectiveQuestion){
                            createFileCompositeSubject(document,content_fontChinese,k,type,(CompositeSubjectiveQuestion)question,questionBeanList);
                            k+=(((CompositeSubjectiveQuestion) question).getQuestions().size())-1;
                        }
                    }
                }
            }
            //关闭文档
            document.close();
            //关闭书写器
            writer.close();
        }catch (Exception e){
             result=false;
            //关闭文档
            document.close();
            //关闭书写器
            writer.close();
             e.printStackTrace();
        }
        return result;
    }

    private void createFileObject(Document document,Font font,int k,int type ,GenericQuestion question,QuestionExtend questionExtend)throws Exception{
        //试题id
        fillBaseContent(k+"、【试题ID】" + "   "+question.getId()+"",font,document);
        //题干
        if(ExportType.PAPER_PDF_TYPE_All==type || ExportType.PAPER_PDF_TYPE_STEM==type){
            //题序、题干
            if (!StringUtils.isBlank(question.getStem())){
                fileExistsImg("",document,font,question.getStem());
            }
            //选项
            if (CollectionUtils.isNotEmpty(question.getChoices())){
                for(int j=0;j<question.getChoices().size();j++){
                    fileExistsImg(AnswerBean.answerMap.get(j+1)+"、",document,font,question.getChoices().get(j));
                }
            }
        }
        if(ExportType.PAPER_PDF_TYPE_All==type||ExportType.PAPER_PDF_TYPE_ANSER==type){
            //答案
            fillBaseContent("【答案】" + "   "+convertAnswer(question.getAnswer()+""),font,document);
            //知识点
            fillBaseContent("【知识点】" + "   "+FuncStr.htmlManage(StringUtils.join(question.getPointsName(),",")),font,document);
            //模块
            String moduleName=questionExtend!=null?paperModuleService.findById(questionExtend.getModuleId()).getName():"";
            fillBaseContent("【模块】" + "   "+moduleName,font,document);
            //解析
            fileExistsImg("【解析】" + "   ",document,font,FuncStr.htmlManage(question.getAnalysis()));
            if(StringUtils.isNotEmpty(question.getMaterial())&&question.getParent()<=0){
                fileExistsImg("【资料】" + "   ",document,font,FuncStr.htmlManage(question.getMaterial()));
            }
        }
        // 换行
        document.add(new Paragraph(" "));
    }
    private void createFileSubject(Document document,Font font,int k,int type ,GenericSubjectiveQuestion question,QuestionExtend questionExtend)throws Exception{
        //试题id
        fillBaseContent(k+"、【试题ID】" + "   "+question.getId()+"",font,document);
        if(ExportType.PAPER_PDF_TYPE_All==type || ExportType.PAPER_PDF_TYPE_STEM==type){
            //题序、注意事项
            if (!StringUtils.isBlank(question.getRequire())){
                fileExistsImg("",document,font,question.getStem());
            }
            // 注意事项
            fileExistsImg("【注意事项（题干要求）】" + "   ",document,font,FuncStr.htmlManage(question.getRequire()));
            //选项
            if (CollectionUtils.isNotEmpty(question.getMaterials())){
                for(int j=0;j<question.getMaterials().size();j++){
                    fileExistsImg("材料"+(j+1)+"、",document,font,question.getMaterials().get(j));
                }
            }else if(StringUtils.isNotEmpty(question.getMaterial())){
                fileExistsImg("材料1、",document,font,question.getMaterial());
            }
            //题干
            fileExistsImg("【题干】" + "   ",document,font,FuncStr.htmlManage(question.getStem()));
        }
        if(ExportType.PAPER_PDF_TYPE_All==type||ExportType.PAPER_PDF_TYPE_ANSER==type){
            //解析
            fileExistsImg("【参考解析】" + "   ",document,font,FuncStr.htmlManage(question.getReferAnalysis()));
        }
        // 换行
        document.add(new Paragraph(" "));
    }
    private void createFileCompositeObject(Document document,Font font,int k,int type ,CompositeQuestion question,List<PaperQuestionBean> questionBeanList)throws Exception{
        //题序
        fillBaseContent("复合客观题", font, document);
        //试题id
        fillBaseContent("【试题ID】" + "   "+question.getId()+"",font,document);
        if (ExportType.PAPER_PDF_TYPE_All == type || ExportType.PAPER_PDF_TYPE_STEM == type) {
            //材料
            if (CollectionUtils.isNotEmpty(question.getMaterials())) {
                for (int j = 0; j < question.getMaterials().size(); j++) {
                    fileExistsImg("材料" + (j + 1) + "、", document, font, question.getMaterials().get(j));
                }
            }else if(StringUtils.isNotEmpty(question.getMaterial())){
                fileExistsImg("材料1 、", document, font, question.getMaterial());
            }
        }
        //小题列表
        if (CollectionUtils.isNotEmpty(questionBeanList) && questionBeanList.size() > 0) {
            int c = 0;
            for (PaperQuestionBean paperQuestionBean : questionBeanList) {
                Question subQuestion = paperQuestionBean.getQuestion();
                QuestionExtend subQuestionExtend = paperQuestionBean.getExtend();
                if (subQuestion != null) {
                    if (subQuestion instanceof GenericQuestion) {
                        createFileObject(document, font,k+c, type, (GenericQuestion) subQuestion, subQuestionExtend);
                    } else if (subQuestion instanceof GenericSubjectiveQuestion) {
                        //试题id
                        fillBaseContent(k+c+"、【试题ID】" + "   "+subQuestion.getId()+"",font,document);
                        if(ExportType.PAPER_PDF_TYPE_All==type || ExportType.PAPER_PDF_TYPE_STEM==type){
                            //题干
                            fileExistsImg(k+c+"【题干】" + "   ",document,font,FuncStr.htmlManage(((GenericSubjectiveQuestion)subQuestion).getStem()));
                        }
                        if(ExportType.PAPER_PDF_TYPE_All==type||ExportType.PAPER_PDF_TYPE_ANSER==type){
                            //解析
                            fileExistsImg("【参考解析】" + "   ",document,font,FuncStr.htmlManage(((GenericSubjectiveQuestion)subQuestion).getReferAnalysis()));
                        }
                    }
                    c++;
                }

            }
        }
        // 换行
        document.add(new Paragraph(" "));
    }
    private void createFileCompositeSubject(Document document,Font font,int k,int type ,CompositeSubjectiveQuestion question,List<PaperQuestionBean> questionBeanList)throws Exception{
        //题序
        fillBaseContent("复合主观题", font, document);
        fillBaseContent("【试题ID】" + "   "+question.getId()+"",font,document);
        if (ExportType.PAPER_PDF_TYPE_All == type || ExportType.PAPER_PDF_TYPE_STEM == type) {
            // 注意事项
            fileExistsImg("【注意事项（题干要求）】" + "   ", document, font, FuncStr.htmlManage(question.getRequire()));
            //选项
            if (CollectionUtils.isNotEmpty(question.getMaterials())) {
                for (int j = 0; j < question.getMaterials().size(); j++) {
                    fileExistsImg("材料" + (j + 1) + "、", document, font, question.getMaterials().get(j));
                }
            }else if(StringUtils.isNotEmpty(question.getMaterial())){
                fileExistsImg("材料1、",document,font,question.getMaterial());
            }
        }
        //小题列表
        if (CollectionUtils.isNotEmpty(questionBeanList) && questionBeanList.size() > 0) {
            int c = 0;
            for (PaperQuestionBean paperQuestionBean : questionBeanList) {
                Question subQuestion = paperQuestionBean.getQuestion();
                QuestionExtend subQuestionExtend = paperQuestionBean.getExtend();
                if (subQuestion != null) {
                    if (subQuestion instanceof GenericQuestion) {
                        createFileObject(document, font,k+ c, type, (GenericQuestion) subQuestion, subQuestionExtend);
                    } else if (subQuestion instanceof GenericSubjectiveQuestion) {
                        fillBaseContent("【试题ID】" + "   "+question.getId()+"",font,document);
                        if(ExportType.PAPER_PDF_TYPE_All==type || ExportType.PAPER_PDF_TYPE_STEM==type){
                            //题干
                            fileExistsImg(k+c+"【题干】" + "   ",document,font,FuncStr.htmlManage(((GenericSubjectiveQuestion)subQuestion).getStem()));
                        }
                        if(ExportType.PAPER_PDF_TYPE_All==type||ExportType.PAPER_PDF_TYPE_ANSER==type){
                            //解析
                            fileExistsImg("【参考解析】" + "   ",document,font,FuncStr.htmlManage(((GenericSubjectiveQuestion)subQuestion).getReferAnalysis()));
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
     *
     * @param index
     * @param document
     * @param font
     * @param Str
     * @throws Exception
     */
    private void fileExistsImg(String index,Document document,Font font,String Str) throws Exception {
        String strTemp = FuncStr.htmlManage(Str);
        try {
            if (StringUtils.isNotEmpty(strTemp)&&strTemp.indexOf("<img") != -1) {
                //图片前的文字处理
                Paragraph paragraph1 = new Paragraph(index + strTemp.substring(0, strTemp.indexOf("<img")), font);
                paragraph1.setIndentationLeft(20);
                document.add(paragraph1);
                //图片处理
                String imgUrl = "";
                Map<String,Integer> sortedMap=FuncStr.sortMap(strTemp);
                for(String key:sortedMap.keySet()){
                    Integer v=sortedMap.get(key);
                    if(v!=-1){
                        imgUrl = strTemp.substring(strTemp.indexOf("src") + 5, strTemp.indexOf("."+key) + 4);
                        break;
                    }
                }
                logger.info("pdf==图片url={}",imgUrl);
                if(StringUtils.isNotEmpty(imgUrl)&&imgUrl.length()>0){
                    Image tempImage;
                    try{
                        tempImage = Image.getInstance(new URL(imgUrl));
                    }catch (Exception e){
                        tempImage = Image.getInstance(new URL("http://120.76.154.176/400/"+imgUrl.substring(imgUrl.lastIndexOf(File.separator))));
                    }
                    tempImage.scalePercent(80f);
                    document.add(tempImage);
                }

                //图片后
                //document.add(new Paragraph(strTemp.substring(strTemp.indexOf(">") + 1), font));
                if(StringUtils.isNotEmpty(strTemp)&&strTemp.indexOf("</img>") != -1){
                    fileExistsImg("",document,font,strTemp.substring(strTemp.indexOf("</img>") + 6));
                }else{
                    fileExistsImg("",document,font,strTemp.substring(strTemp.indexOf(">") + 1));
                }
            } else {
                fillBaseContent(index+strTemp,font,document);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
  }

    /**
     * 答案转换
     * @param answer
     * @return
     */
    private String convertAnswer(String answer){
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
     * 画pdf内容
     * @param value
     * @param font
     * @param document
     * @throws Exception
     */
    private void fillBaseContent(String value,Font font,Document document) throws Exception{
        Paragraph paragraph1 = new Paragraph(value,font);
        paragraph1.setIndentationLeft(20);
        paragraph1.setFirstLineIndent(-12);
        document.add(paragraph1);
    }

    /**
     * 下载文件
     * @param name
     * @param fileUrl
     * @param response
     * @throws Exception
     */
    public void downLoadFile(String name,String fileUrl,HttpServletResponse response)throws Exception{
        FunFileUtils.downLoadNetFile(name+".pdf",fileUrl,response);
    //    FunFileUtils.downLoadlocalFile(name+".pdf",fileUrl,response);

    }
}
