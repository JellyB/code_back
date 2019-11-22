package com.huatu.tiku.teacher;

import com.google.zxing.WriterException;
import com.huatu.tiku.teacher.service.impl.download.v1.PdfWriteServiceImplV1;
import com.huatu.tiku.teacher.util.file.CommonFileUtil;
import com.huatu.tiku.teacher.util.file.PDFDocument;
import com.huatu.tiku.teacher.util.file.PdfUtil;
import com.huatu.tiku.teacher.util.image.NewImageUtils;
import com.huatu.tiku.util.file.CourseQCode;
import com.huatu.tiku.util.file.UploadFileUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import lombok.Cleanup;
import org.apache.logging.log4j.util.Strings;
import org.assertj.core.util.Lists;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static com.huatu.tiku.teacher.util.file.PdfUtil.writeTitle;

/**
 * Created by huangqingpeng on 2018/11/14.
 */
public class PdfTest {

    public static PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool;


    @Test
    public void test() {
        PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool = PdfUtil.initCreateTool();
        Document document = pdfWriteTool.getDocument();
        writeTitle(pdfWriteTool, "2017年新疆公务员《行测》真题（网友回忆）");
        pdfWriteTool.setSubject(1);
        document.newPage();
        try {
            Paragraph paragraph = new Paragraph("常识判断", pdfWriteTool.getModuleFont());
            paragraph.setLeading(pdfWriteTool.getQuestionFont().getSize());
            paragraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(paragraph);
            document.add(Chunk.NEWLINE);
            /**
             * 试题添加
             */
//            PdfUtil.testQuestion(pdfWriteTool);
            testStem(pdfWriteTool);
//            testChoice(pdfWriteTool);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        pdfWriteTool.close();

    }

    private void testChoice(PdfWriteServiceImplV1.PdfWriteTool baseTool) {
        List<String> choices = Lists.newArrayList(
                "<p><img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/s/sen6pyGV8MNhIvYrvz76jlZ9MQh.png\" width=\"45\" height=\"45\"></p>",
                "<p><img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/s/sOxHNXS46gIPbPfeSj44mWKGySI.png\" width=\"66\" height=\"45\"></p>",
                "<p><img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/k/kt36ydTrGM0tfVszVEKTMzSIUT8.png\" width=\"28\" height=\"46\"></p>",
                "<p><img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/n/n8SB4VZWHBU6O3lT1qbPCX7cYsd.png\" width=\"66\" height=\"53\"></p>"

        );
//        ArrayList<String> choices = Lists.newArrayList(
//                "<p>人均GDP超过3000美元</p>",
//                "<p>外汇储备首次超过日本居世界第一</p>",
//                "<p>城镇化率超过50%</p>",
//                "<p>粮食总产量首次超过10万亿斤</p>"
//        );
        for (int i = 0; i < choices.size(); i++) {
            char perChar = (char) ('A' + i);
            try {
                CommonFileUtil.fileExistsImg(perChar + ".", choices.get(i), baseTool, PdfWriteServiceImplV1.addText());
            } catch (BizException e) {
                e.printStackTrace();
            }
        }
        try {
            PDFDocument.handleChoice(baseTool.getElementList(), choices, baseTool.getDocument());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        baseTool.getElementList().clear();
    }

    private void testStem(PdfWriteServiceImplV1.PdfWriteTool pdfWriteTool) {
        String value = "据此，哈丁教授得出的悲观结论：共享是释放社会贪婪与毁灭的祸根。<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>这里，我们不是否认“共享”在中国的价值，但缺乏净土的根源与如哈丁教授所言极为相似！</u>";
        try {
            CommonFileUtil.fileExistsImg(1 + ".", value, pdfWriteTool, PdfWriteServiceImplV1.addText());
        } catch (BizException e) {
            e.printStackTrace();
        }
        try {
            PdfUtil.addElements(pdfWriteTool.getElementList(), pdfWriteTool);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String url = "http://tiku.huatu.com/cdn/pandora/img/answer-qcode-back-ground-0.png";
        try {
            String tailCode = handlerQcodeImage(url, CourseQCode.getInstance().uploadQCodeImgAndReturnPath(new Long(1)));
            System.out.println("tailCode = " + tailCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    public static String handlerQcodeImage(String groundUrl, String waterUrl) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new URL(groundUrl));
        BufferedImage waterImage = ImageIO.read(new URL(waterUrl));
        // 构建叠加层
        BufferedImage buffImg = NewImageUtils.watermark(bufferedImage, waterImage, 100, 55, 1.0f, 250, 250);;
        String url = uploadImage(buffImg, "png");
        return url;
    }

    private static String uploadImage(BufferedImage image, String format) throws IOException {
        @Cleanup
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        String fileName = UUID.randomUUID().toString() + "." + format;
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            UploadFileUtil.getInstance().ftpUploadFileInputStream(inputStream, fileName, UploadFileUtil.IMG_PATH_QUESTION);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
        return UploadFileUtil.IMG_URL_QUESTION + fileName;
    }
}
