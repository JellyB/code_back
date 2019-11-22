package com.huatu.tiku.essay.test;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.vo.admin.AdminQuestionKeyRuleVO;
import com.huatu.tiku.essay.vo.export.KeyWithDescVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author zhaoxi
 * @Description: excel 读取测试
 * @date 2018/12/275:40 PM
 */
@Slf4j
public class FileTest {

    public static void main(String args[]) throws Exception {
//        MultipartFile file = new File("Macintosh HD/用户/urnotmine/关键词测试.docx");
//        //文件预处理
//        List<String> contentList = filePreHandle(file);
//
//        //将数据转换成VO
//        AdminQuestionKeyRuleVO ruleVO = convertContent2VO(contentList);
        String path = "app/xixixi.xls";
        File file = new File(path);
        if(!file.exists()){
            boolean newFile = file.createNewFile();
            log.info(newFile+"");
        }

    }



    public static AdminQuestionKeyRuleVO convertContent2VO(List<String> contentList) {

        AdminQuestionKeyRuleVO ruleVO = new AdminQuestionKeyRuleVO();

        String wordReg = "(\"|“)(.*?)(\"|”)";
        Pattern wordPattern = Pattern.compile(wordReg);

        String scoreReg = "(\"|”)(.*?)分";
        Pattern scorePattern = Pattern.compile(scoreReg);
        //2.解析数据
        LinkedList<KeyWithDescVO> keyWithDescVOList = new LinkedList<>();
        KeyWithDescVO keyWithDescVO = new KeyWithDescVO();
        for (String content : contentList) {
            //判断是描述还是关键词
            if (content.contains(".")) {
                if(null != keyWithDescVO){
                    keyWithDescVOList.add(keyWithDescVO);
                }
                keyWithDescVO = new KeyWithDescVO();
                keyWithDescVO.setDesc(content);
            }else{
//                List<String> keyWordParaList = keyWithDescVO.getKeyWordParaList();
//                keyWordParaList.add(content);
            }



            //按句子切分
//            String[] sentenceArray = content.split("。");
//            List<String> sentenceList = Arrays.asList(sentenceArray);
//            if (CollectionUtils.isNotEmpty(sentenceList)) {
//
//                //切分词语和分数
//                for (String keyWordSentence : sentenceList) {
//                    //关键词
//                    List<String> keyWordList = new LinkedList<>();
//                    Matcher keyWordMatcher = wordPattern.matcher(keyWordSentence);
//                    while (keyWordMatcher.find()) {
//                        keyWordList.add(keyWordMatcher.group(2));
//                    }
//
//                    //分数
//                    List<Double> scoreList = new LinkedList<>();
//                    Matcher scoreMatcher = scorePattern.matcher(keyWordSentence);
//                    while (scoreMatcher.find()) {
//                        scoreList.add(Double.parseDouble(scoreMatcher.group(2)));
//                    }
//                    System.out.println("关键词列表"+keyWordList+"分数列表"+scoreList);
//
//
//                }
//
//            }
        }

        System.out.println(keyWithDescVOList.toString());
        return null;
    }


    public static List<String> filePreHandle(MultipartFile file) {
        //获取文件名
        String fileName = file.getOriginalFilename();
        System.out.println(file.getContentType());
        ;
        if (!fileName.endsWith("docx")) {
//            log.warn("文件类型错误，仅支持docx文件导入。文件名称：{}", fileName);
            throw new BizException(EssayErrors.ERROR_FILE_TYPE);
        }
        List<String> contentList = new ArrayList<>();

        try {
            //读取文件内容
            XWPFDocument xwpfDocument = new XWPFDocument(file.getInputStream());
            Iterator<XWPFParagraph> iterator = xwpfDocument.getParagraphsIterator();
            while (iterator.hasNext()) {
                XWPFParagraph next = iterator.next();
                if (null != next && StringUtils.isNotEmpty(next.getText())) {
                    contentList.add(next.getText());
                }
            }

        } catch (IOException e) {
            throw new BizException(EssayErrors.PDF_OBJ_NULL);
        }
        return contentList;
    }
}
