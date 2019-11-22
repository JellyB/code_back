package com.huatu.tiku.essay.vo.excel;

import com.huatu.tiku.essay.constant.status.EssayFeedBackConstant;
import com.huatu.tiku.essay.vo.resp.Feedback;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author: zhaoxi
 * @Date: Created in 2018/1/8 21:54
 * 意见反馈导出
 */
public class


FeedBackExcelView extends ExcelView {
    static HashMap<Integer, String> feedbackMap = new HashMap<>();
    static HashMap<Integer, String> feedbackStatusMap = new HashMap<>();

    static {
        feedbackMap.put(1, "其他");
        feedbackMap.put(4, "程序BUG");
        feedbackMap.put(5, "功能建议");
        feedbackMap.put(6, "内容意见");
        feedbackMap.put(7, "申论");


        feedbackStatusMap.put(0, "未回复");
        feedbackStatusMap.put(1, "已回复");
        feedbackStatusMap.put(2, "已关闭");
    }

    @Override
    public void setRow(Sheet sheet, Map<String, Object> map) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("id");
        header.createCell(1).setCellValue("反馈内容");
        header.createCell(2).setCellValue("用户id");
        header.createCell(3).setCellValue("联系方式");
        header.createCell(4).setCellValue("图片");
        header.createCell(5).setCellValue("反馈类型");
        header.createCell(6).setCellValue("反馈时间");
        header.createCell(7).setCellValue("用户昵称");
        header.createCell(8).setCellValue("设备类型");
        header.createCell(9).setCellValue("APP版本");
        header.createCell(10).setCellValue("系统版本");
        header.createCell(11).setCellValue("状态");
        header.createCell(12).setCellValue("操作人");
        header.createCell(13).setCellValue("回复内容");

        List<Feedback> list = (List<Feedback>) map.get("members");
        List<HashMap> replyContentList = (List<HashMap>) map.get("replyContents");

        int rowCount = 1;
        for (Feedback vo : list) {
            String type = feedbackMap.get(vo.getType());
//            String status = feedbackStatusMap.get(vo.getStatus());
//            if(StringUtils.isEmpty(status)){
//                status = "未回复";
//            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            String createDate = dateFormat.format(vo.getCreateTime() * 1000);

            Row feedBackRow = sheet.createRow(rowCount++);
            feedBackRow.createCell(0).setCellValue(vo.getId());
            feedBackRow.createCell(1).setCellValue(vo.getContent());
            feedBackRow.createCell(2).setCellValue(vo.getUid());
            feedBackRow.createCell(3).setCellValue(vo.getContacts());
            feedBackRow.createCell(4).setCellValue(vo.getImgs());
            feedBackRow.createCell(5).setCellValue(type);
            feedBackRow.createCell(6).setCellValue(createDate);
            feedBackRow.createCell(7).setCellValue(vo.getUname());
            feedBackRow.createCell(8).setCellValue(vo.getFacility());
            feedBackRow.createCell(9).setCellValue(vo.getAppVersion());
            feedBackRow.createCell(10).setCellValue(vo.getSysVersion());
            //获取
            EssayFeedBackConstant.EssayFeedBackQueryStatusEnum statusEnum = EssayFeedBackConstant.getStatusEnum(vo.getStatus());
            feedBackRow.createCell(11).setCellValue(statusEnum.getDescription());
            feedBackRow.createCell(12).setCellValue(vo.getModifier());
            //调用接口,返回内容
            String content = "";
            if (CollectionUtils.isNotEmpty(replyContentList)) {
                Optional<HashMap> replyContent = replyContentList.stream().filter(reply -> reply.get("feedBackId").equals(vo.getId())).findFirst();
                if (replyContent.isPresent()) {
                    List<String> replyContents = (List<String>) replyContent.get().get("replyContent");
                    if (CollectionUtils.isNotEmpty(replyContents)) {
                        List<String> collect = replyContents.stream().map(targetContent -> {
                            return filterContent(targetContent);
                        }).collect(Collectors.toList());

                        if (CollectionUtils.isNotEmpty(collect)) {
                            content = collect.stream().collect(Collectors.joining(";"));
                        }
                    }
                }
            }
            feedBackRow.createCell(13).setCellValue(content);

        }
    }


    public String filterContent(String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        String regex = "<p>(.*?)</p>";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            return m.group(1);
        }
        return "";
    }


    protected void setStyle(Workbook workbook) {
    }

}
