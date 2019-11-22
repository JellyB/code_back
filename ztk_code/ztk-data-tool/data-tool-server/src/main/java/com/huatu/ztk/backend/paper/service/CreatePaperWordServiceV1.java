package com.huatu.ztk.backend.paper.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.huatu.ztk.backend.paper.bean.AnswerBean;
import com.huatu.ztk.backend.paper.dao.PaperQuestionDao;
import com.huatu.ztk.backend.paperModule.service.PaperModuleService;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;
import org.apache.fop.fo.properties.AlignmentBaseline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\2 0002.
 */
@Service
public class CreatePaperWordServiceV1 {
    private static final Logger logger = LoggerFactory.getLogger(CreatePaperWordServiceV1.class);

    @Autowired
    private UploadFileUtil uploadFileUtil;

    @Autowired
    private PaperQuestionDao paperQuestionDao;

    @Autowired
    private CreatePaperWordService createPaperWordService;
    public boolean createUploadFileWord(Paper paper,int type,String name,File file) throws Exception {
        //生成临时文件
        if(file==null){
            file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH+name);
        }
        //向文件中写数据
        boolean bln=createFileWord(file,paper.getQuestions(),type);
        if(bln){
            //上传文件
            uploadFileUtil.ftpUploadFile(file, new String(file.getName().getBytes("UTF-8"),"iso-8859-1"), FunFileUtils.WORD_FILE_SAVE_PATH);
        }
        return bln;
    }

    /**
     * 纯文本的数据拼接
     * @param file
     * @param ids
     * @param type
     * @return
     */
    public boolean createFileWord(File file, List<Integer> ids ,int type){
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

            Font contentFont = new Font(bfChinese, 14, Font.NORMAL);
            document.open();
            Map<Integer, Question> questionMap = paperQuestionDao.findBath(ids).stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
            if(questionMap!=null&&questionMap.size()>0){
                int parent = 0;
                for(int i = 0 ; i<ids.size() ; i++){
                    Question question = questionMap.get(ids.get(i));
                    if(question==null){
                        continue;
                    }
                    if(question instanceof GenericQuestion){
                        parent = createFileObject(document,contentFont,i+1,type,(GenericQuestion)question,parent);
                    }else if(question instanceof GenericSubjectiveQuestion){
                        //TODO 主观题的拼接先放下
//                        createFileSubject(document,contentFont,i+1,type,(GenericSubjectiveQuestion)question);
                    }
                }
            }

        }catch (Exception e){
            result=false;
            e.printStackTrace();
        }finally {
            //关闭书写器
            writer.close();
            //关闭文档
            document.close();
        }
        return result;
    }

    private int createFileObject(Document document, Font font, int i, int type, GenericQuestion question, int parent) throws Exception {
        if(StringUtils.isNotEmpty(question.getMaterial())&&question.getParent()!=parent){
            Font font1 = new Font(font.getBaseFont(),font.getSize(),Font.BOLD);
            createPaperWordService.fillBaseContent("【资料】" ,font1,document);
            createPaperWordService.fileExistsImg("" , FuncStr.htmlManage(question.getMaterial()),font,document);
            parent = question.getParent();
        }
        if(ExportType.PAPER_WORD_TYPE_ALL==type || ExportType.PAPER_WORD_TYPE_SIDE_STEM==type){
            //题序、题干
            if (!StringUtils.isBlank(question.getStem())){
                createPaperWordService.fileExistsImg(i+". ",question.getStem(),font,document);
            }
            //选项
            if (CollectionUtils.isNotEmpty(question.getChoices())){
                for(int j=0;j<question.getChoices().size();j++){
                    createPaperWordService.fileExistsImg(AnswerBean.answerMap.get(j+1)+". ",question.getChoices().get(j),font,document);
                }
            }
        }
        if(ExportType.PAPER_WORD_TYPE_ALL==type||ExportType.PAPER_WORD_TYPE_SIDE_ANSWER==type){
            //答案
            createPaperWordService.fillBaseContent("【答案】" + " "+createPaperWordService.convertAnswer(question.getAnswer()+""),font,document);
            //知识点
            createPaperWordService.fillBaseContent("【三级知识点】" + " "+ FuncStr.htmlManage(question.getPointsName().get(2)),font,document);
            //解析
            createPaperWordService.fillBaseContent("【解析】",font,document);
            createPaperWordService.fileExistsImg("",FuncStr.htmlManage(question.getAnalysis()),font,document);
        }
        // 换行
        document.add(new Paragraph(" "));
        return parent;
    }
}
