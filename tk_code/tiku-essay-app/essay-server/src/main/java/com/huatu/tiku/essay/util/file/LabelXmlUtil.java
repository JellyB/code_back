package com.huatu.tiku.essay.util.file;

import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.repository.EssayLabelDetailRepository;
import com.huatu.tiku.essay.repository.EssayLabelTotalRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
/**
 * @author zhaoxi
 * @Description: 根据批注生成xml文件
 * @date 2018/7/11上午10:31
 */
@Component
public class LabelXmlUtil {

    @Autowired
    private EssayLabelTotalRepository totalRepository;

    @Autowired
    private EssayLabelDetailRepository detailRepository;
//    private static final Logger logger = LoggerFactory.getLogger(LabelXmlUtil.class);
    public String produceXml(EssayLabelTotal total, List<EssayLabelDetail> detailList,EssayLabelDetail titleLabel){

//        total = totalRepository.findOne(75L);
//        List<EssayLabelDetail> essayLabelDetails = detailRepository.findByTotalIdAndStatus(75L, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
//        detailList = new LinkedList<>();
//        if(CollectionUtils.isNotEmpty(essayLabelDetails)){
//            for(EssayLabelDetail detail:essayLabelDetails){
//                if(StringUtils.isNotEmpty(detail.getTitleScore())){
//                    titleLabel = detail;
//                }else{
//                    detailList.add(detail);
//                }
//            }
//        }

//        logger.info("得到标题的结果={}",total.getTitleContent());
//        logger.info("得到批改后的结果={}",total.getLabeledContent());

        String content =  total.getLabeledContent();
        content = content.replaceAll("27px","55px");
        String titleContent = total.getTitleContent();
        String newTitle = "";
        String newContent = "";
        String resultContent = "";

        if(StringUtils.isNotEmpty(titleContent)){
//            titleContent = titleContent.replaceAll("&nbsp;"," ").replaceAll("<br>|<br/>","\n");
//            List<EssayLabelDetail> titleLabels = new ArrayList<>();
//            titleLabels.add(titleLabel);
//            newTitle = produceNewStr(titleLabels,titleContent);
            newTitle = "<titleScore value="+total.getTitleScore()+">"+titleContent+"</titleScore>\n";
            resultContent = newTitle+"\n";
        }else{
            newTitle = "<titleScore value="+5+">"+"</titleScore>\n";
            resultContent = newTitle+"\n";
        }
        if(StringUtils.isNotEmpty(content)){
            content = content.replaceAll("&nbsp;"," ").replaceAll("<br>|<br/>","\n");
            newContent = produceNewStr(detailList,content);
            resultContent += newContent+"\n";
        }

//        logger.info("得到标题的结果={}",titleContent);
//        logger.info("得到批改后的结果={}",content);

        resultContent += "<totalTitleScore value="+total.getTitleScore()+"></totalTitleScore>\n";
        resultContent += "<totalThesisScore value="+total.getThesisScore()+"></totalThesisScore>\n";
        resultContent += "<totalEvidenceScore value="+total.getEvidenceScore()+"></totalEvidenceScore>\n";
        resultContent += "<totalStructScore value="+total.getStructScore()+"></totalStructScore>\n";
        resultContent += "<totalSentenceScore value="+total.getSentenceScore()+"></totalSentenceScore>\n";
        resultContent += "<totalLiteraryScore value="+total.getLiteraryScore()+"></totalLiteraryScore>\n";
        resultContent += "<totalThoughtScore value="+total.getThoughtScore()+"></totalThoughtScore>\n";
        resultContent += "<totalWordNumScore value="+total.getWordNumScore()+"></totalWordNumScore>\n";
        resultContent += "<totalCopyRatio value="+total.getCopyRatio()+"></totalCopyRatio>\n";
        resultContent += "<totalParagraphScore value="+total.getParagraphScore()+"></totalParagraphScore>\n";
        resultContent += "<answerCardId value="+total.getAnswerId()+"></answerCardId>\n";
        resultContent += "<totalScore value="+total.getTotalScore()+"></totalScore>\n";
        for(EssayLabelDetail detail:detailList){
//            logger.info("传输进来的答题卡id={},detail_id={},titleScore={},thesisScore={},evidenceScore={},structScore={},sentenceScore={},literaryScore={},thoughtScore={}",
//                    detail.getAnswerId(),detail.getId(),detail.getTitleScore(),detail.getThesisScore(),detail.getEvidenceScore(),
//                    detail.getLiteraryScore(),detail.getStructScore(),detail.getSentenceScore(),detail.getThoughtScore());
        }
//        logger.info("最后输出的resultContent={}",resultContent);

        resultContent = resultContent.replaceAll("</font>|<font>|<br/>|[0-9]{2,}字体|[1-9][0-9]{2,}字|100\n","");
//        logger.info("最后输出的resultContent={}",resultContent);
        return resultContent;
    }

    /**
     * @param detailList
     * @param content
     * @return
     */
    public String produceNewStr(List<EssayLabelDetail> detailList,String content){
        //若为正文，则需考虑多个标签
        for(int i=0,size=detailList.size();i<size;i++){
            EssayLabelDetail detailTemp = detailList.get(i);
            List<String> tags = produceTag(detailTemp);
            if(CollectionUtils.isNotEmpty(tags)){

                content = content.replaceAll("&amp;<font style=['\"]color:red;(font-weight:bolder;){0,1}font-size:55px['\"]>","△<font style=\"color:red;(font-weight:bolder;){0,1}font-size:55px\">")
                        .replaceAll("&<font style=['\"]color:red;(font-weight:bolder;){0,1}font-size:55px['\"]>","△<font style=\"color:red;(font-weight:bolder;){0,1}font-size:55px\">")
                        .replaceAll("</font>&amp;","</font>△")
                        .replaceAll("</font>&","</font>△");

                content = content.replaceFirst("△{0,}(amp;){0,1}<font style=['\"]color:red;(font-weight:bolder;){0,1}font-size:55px['\"]>"+(i+1)+"</font>△{0,}(amp;){0,1}",tags.get(0))
                        .replaceFirst("△{0,}(amp;){0,1}<font style=['\"]color:red;(font-weight:bolder;){0,1}font-size:55px['\"]>"+(i+1)+"</font>△{0,}(amp;){0,1}",tags.get(1));
            }
        }
        return content;
    }

    /**
     * 生成标签
     * @param detail
     * @return
     */
    public List<String> produceTag(EssayLabelDetail detail){
        List<String> tags = new ArrayList<>();
        String tagStart = "";
        String tagEnd = "";
        if(StringUtils.isNotEmpty(detail.getTitleScore())){
            tagStart += "<titleScore value="+detail.getTitleScore()+">";
            tagEnd += "</titleScore>";
        }
        if(StringUtils.isNotEmpty(detail.getThesisScore())){
            tagStart += "<thesisScore value="+detail.getThesisScore()+" id="+detail.getId()+">";
            tagEnd += "</thesisScore>";
        }
        if(StringUtils.isNotEmpty(detail.getEvidenceScore())){
            tagStart += "<evidenceScore value="+detail.getEvidenceScore()+">";
            tagEnd += "</evidenceScore>";
        }
        if(StringUtils.isNotEmpty(detail.getStructScore())){
            tagStart += "<structScore value="+detail.getStructScore()+">";
            tagEnd += "</structScore>";
        }
        if(StringUtils.isNotEmpty(detail.getSentenceScore())){
            tagStart += "<sentenceScore value="+detail.getSentenceScore()+">";
            tagEnd += "</sentenceScore>";
        }
        if(StringUtils.isNotEmpty(detail.getLiteraryScore())){
            tagStart += "<literaryScore value="+detail.getLiteraryScore()+">";
            tagEnd += "</literaryScore>";
        }
        if(StringUtils.isNotEmpty(detail.getThoughtScore())){
            tagStart += "<thoughtScore value="+detail.getThoughtScore()+">";
            tagEnd += "</thoughtScore>";
        }
        if(StringUtils.isNotEmpty(tagStart)&&StringUtils.isNotEmpty(tagEnd)){
            tags.add(tagStart);
            tags.add(tagEnd);
        }
        return tags;
    }


}
