package com.huatu.tiku.essay.service.impl;

import com.alibaba.fastjson.JSON;
import com.huatu.tiku.essay.entity.EssayQuestionBelongPaperArea;
import com.huatu.tiku.essay.repository.EssayAreaRepository;
import com.huatu.tiku.essay.vo.file.ReadFileQuestionVO;
import com.huatu.tiku.essay.service.EssayReadFileService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by x6 on 2017/12/16.
 */
//@Transactional
@Service
@Slf4j
public class EssayReadFileServiceImpl implements EssayReadFileService {

    @Autowired
    private EssayAreaRepository areaRepository;
    @PersistenceContext
    private EntityManager entityManager;
    String txtPath="/Users/urnotmine/fb/";
    String docPath="/Users/urnotmine/fb/doc/";
    @Override
    public boolean area() {
        String url = "/Users/urnotmine/Desktop/area.txt";
        try {
            readArea(url);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }

    private void readArea(String url) throws Exception {
        InputStreamReader input = new InputStreamReader(new FileInputStream(url));

        BufferedReader bufferedReader = new BufferedReader(input);
        String line = bufferedReader.readLine();
        List<String> errorProvinceList = new LinkedList<String>();
        List<String> errorCityList = new LinkedList<String>();
        List<String> errorDistrictList = new LinkedList<String>();

        for(int lineIndex = 1;lineIndex <2900;lineIndex++){
            log.info("processing……{}",lineIndex);
            String[] split = line.split(",");
            if(split.length != 0 ){
                log.info("=========="+split);
                String provinceName = split[0];
                String cityName = split[1];
                String districtName = split[2];

                provinceName = provinceName.replaceAll("省","").replaceAll("市","");
                List<EssayQuestionBelongPaperArea> provinceList = areaRepository.findByNameLikeAndPId(provinceName,0L);
                if(CollectionUtils.isNotEmpty(provinceList)){
                    EssayQuestionBelongPaperArea province = provinceList.get(0);
                    long provinceId = province.getId();
                    List<EssayQuestionBelongPaperArea> cityList = areaRepository.findByNameLikeAndPId(cityName,provinceId);
                    EssayQuestionBelongPaperArea city = null;
                    if(CollectionUtils.isNotEmpty(cityList)){
                        city  = cityList.get(0);
                    }else{
                        log.info("二级地区处理失败,名称：{},行号：{}",cityName,lineIndex);
                        city = EssayQuestionBelongPaperArea.builder()
                                .pId(province.getId())
                                .name(cityName)
                                .status(1)
                                .bizStatus(1)
                                .build();
                        city = areaRepository.save(city);
                        entityManager.clear();
                    }
                    long cityId = city.getId();
                    EssayQuestionBelongPaperArea area = EssayQuestionBelongPaperArea.builder()
                            .pId(cityId)
                            .name(districtName)
                            .status(1)
                            .bizStatus(1)
                            .build();
                    area = areaRepository.save(area);
                    entityManager.clear();
                }else{
                    log.info("一级地区处理失败,名称：{},行号：{}",provinceName,lineIndex);
                    errorProvinceList.add(provinceName);

                }

            }
            line = bufferedReader.readLine();
        }

        log.info(errorProvinceList.toString());
        log.info(errorCityList.toString());

    }


    @Override
    public boolean createFile(String fileName) {
//        fileName = "教师资格证-中学-数学";

        File file=new File(txtPath);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                System.out.println("文     件："+tempList[i]);
                String name = tempList[i].getName();
                int index = name.lastIndexOf(".");
                name = name.substring(0, index);
//                readFile(name);
            }else if(tempList[i].isDirectory()){
                String dictoryName = tempList[i].getName();
                File[] files = tempList[i].listFiles();
                for (int j = 0; j < files.length; j++) {
                    System.out.println("文     件："+files[j]);
                    String name = files[j].getName();
                    int index = name.lastIndexOf(".");
                    name = name.substring(0, index);
                    readFile(dictoryName,name);
                }
            }
        }
        return true;
    }


    @Override
    public boolean createFileV2(String fileName) {


//        readFile("教师招聘-数学");
        return true;
    }



    public boolean readFile(String dictoryName,String fileName){
        String txtPath="/Users/urnotmine/fb/";
        String docPath="/Users/urnotmine/fb/doc/";
        docPath = docPath+dictoryName+"/";

        File docFile = new File(docPath);
        if(!docFile.exists()){
            docFile.mkdirs();
        }
        txtPath = txtPath+dictoryName+"/";
        boolean flag = false;
        // 设置纸张大小
        Document document =null;
        RtfWriter2 writer=null;
        try{
            // 设置纸张大小
            document = new Document(PageSize.A4);
            File file = new File(docPath+fileName+1+".doc");

            writer= RtfWriter2.getInstance(document, new FileOutputStream(file));
            // 设置中文字体
            BaseFont bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            // 标题字体风格
            Font titleFont = new Font(bfChinese, 16, Font.BOLD);
            // 正文字体风格
            Font contentFont = new Font(bfChinese, 10, Font.NORMAL);
            document.open();

            //录入标题
            addTitle( fileName,  document, titleFont);

            //从txt中读取试题，追加到doc文档中
            InputStreamReader input = new InputStreamReader(new FileInputStream(txtPath+fileName+".txt"));

            BufferedReader bufferedReader = new BufferedReader(input);

            int questionCount = 0;

            String line = bufferedReader.readLine();
            while (StringUtils.isNotEmpty(line) ) {
                if(null != line && StringUtils.isNotEmpty(line.toString())){
                    //将每行对应的数据转成Object对象
                    ReadFileQuestionVO readFileQuestionVO = readQuestion(line.toString());
                    try {
                        questionCount = questionCount + 1;
                        addQuestion(questionCount,readFileQuestionVO, document, contentFont);
                        log.info("录入结束，题目序号:{}",questionCount);

                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                }

                line = bufferedReader.readLine(); // 读空行
                line = bufferedReader.readLine();//读下一行文字
                if(questionCount % 300 == 0 && StringUtils.isNotEmpty(line)){
                    //关闭文档
                    document.close();
                    //关闭书写器
                    writer.close();
                    //创建一个新文件
                    int fileCount = questionCount/300+1;
                    log.info("文件生成成功，文件名称：{}",fileName+fileCount);

                    // 设置纸张大小
                    document = new Document(PageSize.A4);
                    File file2 = new File(docPath+fileName+fileCount+".doc");
                    writer= RtfWriter2.getInstance(document, new FileOutputStream(file2));
                    document.open();

                    //录入标题
                    addTitle( fileName+fileCount,  document, titleFont);
                }

            }

            //关闭文档
            document.close();
            //关闭书写器
            writer.close();
        }catch (Exception e){
            //关闭文档
            document.close();
            //关闭书写器
            if(writer!=null){
                writer.close();
            }
            e.printStackTrace();
        }
        return flag;
    }

    private void addTitle(String fileName, Document document,Font titleFont) throws DocumentException {
        //标题
        Paragraph title = new Paragraph(fileName);
        // 设置标题格式对齐方式
        title.setAlignment(Element.ALIGN_CENTER);
        title.setFont(titleFont);
        document.add(title);
    }


    private void addQuestion(int questionCount,ReadFileQuestionVO vo, Document document,Font contentFont) throws Exception {
        //来源source
        String source = vo.getSource();

        addContent(questionCount+"【题目来源】"+source ,  document, contentFont);

        //题干信息
        String content = vo.getContent();
        content = content.replaceAll("\\[/p\\]\\[p\\]", "\n")
                .replaceAll("\\[p\\]", "【题干信息】\n    ")
                .replaceAll("\\[/p\\]", "");
      //  .replace("\\[tex=\\^\\]","http://fb.fbstatic.cn/api/xingce/accessories/formulas?latex=SLh4qR+8Xu4vM3D6defkrMeKFflxHJNmm0zUUoVJmXA")
      //  .replace("\\[/tex=\\]","");


       deal(document,content,contentFont);

        //选项
        List<String> options = vo.getOptions();
        if(CollectionUtils.isNotEmpty(options)){
            for(int index= 0;index < options.size();index++){
                String option = options.get(index);
                deal(document,option,contentFont);
            }
        }

        //标答
        String correctChoice = vo.getCorrectChoice();
        if(StringUtils.isNotEmpty(correctChoice)){
            correctChoice = correctChoice.replaceAll("\\[/p\\]\\[p\\]", "\n")
                    .replaceAll("\\[p\\]", "\n    ")
                    .replaceAll("\\[/p\\]", "");
            deal(document,"【正确答案】"+correctChoice,contentFont);
        }


        //正确率
        String correctRatio = vo.getCorrectRatio();
        addContent("【正确率】"+correctRatio ,  document, contentFont);

        //难度级别
        double difficulty = vo.getDifficulty();
        addContent("【难度级别】"+difficulty ,  document, contentFont);

        //易错选项
        String mostWrongAnswerChoice = vo.getMostWrongAnswerChoice();
        if(StringUtils.isNotEmpty(mostWrongAnswerChoice)){
            addContent("【易错选项】"+mostWrongAnswerChoice ,  document, contentFont);
        }


        //知识点
        String point = vo.getPoint();
        if(StringUtils.isNotEmpty(point)){
            addContent("【知识点】"+point ,  document, contentFont);

        }

        //解析
        String solution = vo.getSolution();
        solution = solution.replaceAll("\\[/p\\]\\[p\\]", "\n    ")
                .replaceAll("\\[p\\]", "\n    ")
                .replaceAll("\\[/p\\]", "");
        deal(document,"【答案解析】"+solution,contentFont);

        addContent("\n\n" ,  document, contentFont);

    }

    private void deal(Document document, String content,Font contentFont) throws Exception{
        int a =  content.indexOf("[tex=");
        int b = content.indexOf(".png");
//        log.info("公式index = {}",a);
//        log.info("图片index = {}",b);
        while(a>=0 ||b >= 0) {
            if((a<b && a >= 0) || ( -1 == b && a >= 0)){
//                log.info("公式前的文字："+content.substring(0, a));
                addContent(content.substring(0,a) ,  document, contentFont);
                content = content.substring(a);
//                log.info("公式+剩余的文字"+content);
                content = dealTex(0, document,content);
            }else if((b < a && b >= 0 ) || (a ==-1 && b>=0)){
//                log.info("图片+之前的文字："+content.substring(0, b+4));
                //取出图片部分
                dealImg(document,content.substring(0, b+4),contentFont);
                content = content.substring(b+4);

            }

            a = content.indexOf("[tex=");
            b = content.indexOf(".png");
        }

        addContent(content ,  document, contentFont);
    }

    private  String  dealTex(int a,Document document,String content) throws Exception {
        //  String str ="[tex=7.643x1.286]PM9FgLeNAQ6MeDIhdbni6Ke6wwoHTjPYfiC32xK99/fa2kMHaQF3GI7sxYEXRQRQk79uNNHUU2cUbajmN6Cx/g==[/tex]。";
        //=后面的值
        String temp = content.substring(a + 5);
        //取出x的位置
        int b = temp.indexOf("x");
        String weight = temp.substring(0, b);

        String height_t2 = temp.substring(b + 1);
        int c = height_t2.indexOf("]");
        String height = height_t2.substring(0, c);
        int m = height_t2.indexOf("[/tex]");

        String address = "http://fb.fbstatic.cn/api/xingce/accessories/formulas?latex=" + height_t2.substring(c + 1, m);
//        System.out.println(weight + " : " + height + " :  " + address);
        content = content.substring(0,a)+content.substring(content.indexOf("[/tex]")+"[/tex]".length());
//        System.out.println(content);
        try{
            Image img = Image.getInstance(address);
            document.add(img);
        }
        catch (Exception e){
            log.error("image instance failure , address = {}",address);
        }

//        img.setAlignment(Image.MIDDLE);
        //    img.setAbsolutePosition(0, 0);
//        img.scaleAbsolute(Float.parseFloat(weight), Float.parseFloat(height));
//        img.setAlignment(Image.LEFT);// 设置图片显示位置
        return content;

    }
    private void dealImg(Document document, String content,Font contentFont) throws  Exception{
        //判断文字重视否包含img标签[img=198x48]1570d1ec5b7b096.png[/img]
        // http://fb.fbstatic.cn/api/jszgsxz/images/1570d0be06e029d.png
        int i = content.indexOf("[img=");
       if(i < 0){
           //http地址直接上传图片
           int h = content.indexOf("http:");
           if(0 != h && -1!= h){
               addContent(content.substring(0,h),document,contentFont);
           }
//           log.info("http图片地址"+content.substring(h));
           if(-1 != h){
//               Image img = Image.getInstance(content.substring(h));
//               document.add(img);
               try{
                   Image img = Image.getInstance(content.substring(h));
                   document.add(img);
               }
               catch (Exception e){
                   log.error("image instance failure , address = {}",content.substring(h));
               }
           }
       }else{
           //img标签 ，拼接图片地址再上传
           addContent(content.substring(0,i),document,contentFont);
           String temp = content.substring(i + 5);
           //取出x的位置
           int b = temp.indexOf("x");
           String weight = temp.substring(0, b);

           String height_t2 = temp.substring(b + 1);
           int c = height_t2.indexOf("]");
           String height = height_t2.substring(0, c);
           int m = height_t2.indexOf("[/img]");
           String address = "http://fb.fbstatic.cn/api/jszgsxz/images/" + height_t2.substring(c + 1);

//           System.out.println(weight + " : " + height + " :  " + address);
           try{
               Image img = Image.getInstance(address);
               document.add(img);
           }
           catch (Exception e){
               log.error("image instance failure , address = {}",address);
           }
       }
    }


    private void addContent(String content, Document document,Font contentFont) throws DocumentException {
        //试题信息
        Paragraph contentParagraph = new Paragraph(content);
        // 设置格式对齐方式
        contentParagraph.setAlignment(Element.ALIGN_LEFT);
        contentParagraph.setFont(contentFont);
        document.add(contentParagraph);
    }


    //将数字转换成ABCD
    private String changeSort(String sort){
        switch (sort){
            case "0":
                sort = "A";
                break;
            case "1":
                sort = "B";
                break;
            case "2":
                sort = "C";
                break;
            case "3":
                sort = "D";
                break;
            case "4":
                sort = "E";
                break;
            case "5":
                sort = "F";
                break;
            case "6":
                sort = "G";
                break;
            default:
                break;
        }
        return sort;
    }


    private ReadFileQuestionVO readQuestion(String question){
        Map map = JSON.parseObject(question);
        ReadFileQuestionVO vo = new ReadFileQuestionVO();
        //选项
        List<Map> accessories = (List<Map>) map.get("accessories");
        if(CollectionUtils.isNotEmpty(accessories)){
            List<String> options = (List<String>)accessories.get(0).get("options");
            if(CollectionUtils.isNotEmpty(options)){
                for(int i=0;i<options.size();i++){
                    String option = options.get(i);
                    String sort = changeSort((i ) + "");
                    option = sort+":"+option;
                    options.set(i,option);
                }
            }
            vo.setOptions(options);
        }


        //题干
        String content = (String)map.get("content");
        vo.setContent(content);

        //标答
        Map correctAnswer = (Map)map.get("correctAnswer");
        if(null !=correctAnswer.get("choice")){
            String correctChoice = correctAnswer.get("choice").toString();
            String[] choice = correctChoice.split(",");
            String correctChoiceNew = "";
            for(int i = 0; i<choice.length;i++){
                correctChoiceNew = correctChoiceNew + changeSort(choice[i]);
            }
            vo.setCorrectChoice(correctChoiceNew);
        }
        if(null !=correctAnswer.get("answer")){
            String correctChoice = correctAnswer.get("answer").toString();
            vo.setCorrectChoice(correctChoice);
        }

        //正确率
        String correctRatio = (String)map.get("correctRatio");
        vo.setCorrectRatio(correctRatio);

        //难度级别
        int difficulty = (int) map.get("difficulty");
        vo.setDifficulty(difficulty);

        //易错选项
        Map mostWrongAnswer = (Map)map.get("mostWrongAnswer");
        if(null != mostWrongAnswer){
            String mostWrongAnswerChoice = (String)mostWrongAnswer.get("choice");
            mostWrongAnswerChoice = changeSort(mostWrongAnswerChoice);
            vo.setMostWrongAnswerChoice(mostWrongAnswerChoice);
        }


        //知识点
        List<Map> pointList = (List<Map>)map.get("point");
        StringBuilder pointName = new StringBuilder();
        if(CollectionUtils.isNotEmpty(pointList)){
            for(Map point:pointList){
                pointName.append(point.get("name")).append(",");
            }
        }
        vo.setPoint(pointName.toString());

        //解析
        String solution = (String)map.get("solution");
        vo.setSolution(solution);
        //来源source
        String source = (String)map.get("source");
        vo.setSource(source);

        return vo;
    }



}
