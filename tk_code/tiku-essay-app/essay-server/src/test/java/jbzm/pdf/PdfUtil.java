package jbzm.pdf;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * Create by jbzm on 171212
 */
public class PdfUtil {

    public static BaseFont BASEFONT = getBaseFont();
    public static Font ANSWER_SHEET=new Font(BASEFONT,8, Font.NORMAL,new BaseColor(241,158,194));
    public static Font TITLE_FONT = new Font(BASEFONT, 13, Font.NORMAL);
    public static Font LITTLE_TITLE_FONT = new Font(BASEFONT, 12, Font.NORMAL);
    public static Font CONTENT_FONT = new Font(BASEFONT, 10, Font.NORMAL);
    public static Font CONTENT_FONT_RED = new Font(BASEFONT, 10, Font.NORMAL, BaseColor.RED);
    public static Font CONTENT_FONT_GRAY = new Font(BASEFONT, 10, Font.NORMAL, BaseColor.GRAY);
    public static Font CONTENT_FONT_YELLOW = new Font(BASEFONT, 10, Font.NORMAL, BaseColor.YELLOW);
    public static Font CONTENT_FONT_ORANGE = new Font(BASEFONT, 10, Font.NORMAL, BaseColor.ORANGE);

    /**
     * 创建文件
     *
     * @param file
     * @param document
     * @throws Exception
     */
    public PdfUtil(String file, Document document) throws Exception {
        // 创建PdfWriter, 类似还有HtmlWriter、RtfWriter、XmlWriter
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(new File(file)));
    }

    /**
     * 获取中文字体
     *
     * @return
     */
    private static BaseFont getBaseFont() {
        try {
            return BaseFont.createFont("STSongStd-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建空格
     *
     * @param size
     * @param document
     * @throws DocumentException
     */
    public void addBlank(int size, Document document) throws DocumentException {
        Paragraph paragraph = new Paragraph(" ", new Font((BASEFONT), size));
        document.add(paragraph);
    }

    /**
     * 创建标题头
     *
     * @param document
     * @param content  标题头内容
     * @throws DocumentException
     */
    public void addTitle(String content, Document document) throws DocumentException {
        Paragraph paragraph = new Paragraph(content, TITLE_FONT);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }

    /**
     * 创建内容头
     *
     * @param content
     * @param document
     * @throws DocumentException
     */
    public void addBaseTitle(String content, Document document) throws DocumentException {
        Phrase phrase = new Phrase();
        Paragraph paragraph = new Paragraph(content, CONTENT_FONT);
        paragraph.setIndentationLeft(5);
        document.add(paragraph);
    }

    /**
     * 添加基本内容
     *
     * @param content
     * @param document
     * @throws Exception
     */
    public void addBaseContent(String content, Document document) throws Exception {
        Paragraph paragraph = new Paragraph(content, CONTENT_FONT);
        paragraph.setIndentationLeft(5);
        document.add(paragraph);
    }

    public void addBaseContentRed(String content, Document document) throws Exception {
        Paragraph paragraph = new Paragraph(content, CONTENT_FONT);
        paragraph.setIndentationLeft(5);
        document.add(paragraph);
    }

    /**
     * 添加小标题
     *
     * @param content
     * @param document
     * @throws Exception
     */
    public void addLittleTitle(String content, Document document) throws Exception {
        Paragraph paragraph = new Paragraph(content, LITTLE_TITLE_FONT);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        document.add(paragraph);
    }

    /**
     * 添加答题格
     *
     * @param document
     * @param sum
     * @throws DocumentException
     */
    public void addForm(int sum, Document document) throws DocumentException {
        int line = 25;
        int row = sum / 25 + 1;
        int num = 1;
        for (int i = 0; i < row; i++) {
            //创建PdfTable对象
            PdfPTable table = new PdfPTable(line);
            PdfPCell blankCell = new PdfPCell();
            blankCell.setMinimumHeight(19);
            blankCell.setBorderColor(new BaseColor(241,158,194));
            table.addCell(blankCell);
            //设置各列的列宽
            table.setTotalWidth(495);
            table.setLockedWidth(true);
            for (int j = 0; j < line; j++) {

                table.addCell(" ");
            }
            if ((i + 1) * line % 100 == 0) {
                //创建数字
                Paragraph paragraph = new Paragraph("" + num * 100, new Font(BASEFONT, 8));
                paragraph.setIndentationLeft(400);
                num++;
                document.add(paragraph);
            } else {
                //创建空格
                Paragraph paragraph = new Paragraph(" ", new Font(BASEFONT, 6));
                document.add(paragraph);
            }
            document.add(table);
        }
    }

    /**
     * 添加水印
     *
     * @param text       加水印的文本内容
     * @param textWidth  文字横坐标
     * @param textHeight 文字纵坐标
     * @throws Exception
     */
    public void addWaterMark(String inputPath, String text, int textWidth, int textHeight) throws Exception {
        //创建临时文件
        String outputPath = "D:/2.pdf";
        // 待加水印的文件
        PdfReader reader = new PdfReader(inputPath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
        //定义页数
        int total = reader.getNumberOfPages() + 1;
        // 加完水印的文件
        waterMark(text, textWidth, textHeight, stamper, total);
        stamper.close();
    }

    /**
     * @param text
     * @param textWidth
     * @param textHeight
     * @param stamper
     * @param total
     */
    private void waterMark(String text, int textWidth, int textHeight, PdfStamper stamper, int total) {
        // 循环对每页插入水印
        for (int i = 1; i < total; i++) {
            // 水印的起始
            PdfContentByte content = stamper.getUnderContent(i);
            // 开始
            content.beginText();
            // 设置颜色
            content.setColorFill(BaseColor.GRAY);
            // 设置字体及字号
            content.setFontAndSize(BASEFONT, 10);
            // 设置起始位置
            content.setTextMatrix(textWidth, textHeight);
            // 开始写入水印
            //旋转度数默认45
            content.showTextAligned(Element.ALIGN_LEFT, text, textWidth,
                    textHeight, 0);
            // 结束
            content.endText();
        }
    }

    /**
     * 添加页码
     *
     * @param stamper
     * @param total
     */
    private void pageMark(PdfStamper stamper, int total) {
        // 循环对每页插入水印
        for (int i = 1; i < total; i++) {
            // 水印的起始
            PdfContentByte content = stamper.getUnderContent(i);
            // 开始
            content.beginText();
            // 设置颜色
            content.setColorFill(BaseColor.GRAY);
            // 设置字体及字号
            content.setFontAndSize(BASEFONT, 10);
            // 设置起始位置
            content.setTextMatrix(350, 20);
            // 开始写入水印
            //旋转度数默认45
            content.showTextAligned(Element.ALIGN_LEFT, "第" + i + "页", 287,
                    20, 0);
            // 结束
            content.endText();
        }
    }


    /**
     * 添加图片水印
     *
     * @param x         x轴
     * @param y         y轴
     * @param inputPath 写入文件
     * @throws IOException
     * @throws DocumentException
     */
    public String addWaterImage(int x, int y, String inputPath, String imgPath) throws Exception {
        String outputPath = "D:/" + UUID.randomUUID().toString() + ".pdf";
        PdfReader reader = new PdfReader(inputPath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
        //定义页数
        int total = reader.getNumberOfPages() + 1;
        //加载水印
        waterImage(x, y, imgPath, total, stamper);
        pageMark(stamper, total);
        stamper.close();
        reader.close();
        return outputPath;
    }

    /**
     * @param x
     * @param y
     * @param imgPath
     * @param total
     * @param stamper
     * @throws Exception
     */
    public void waterImage(int x, int y, String imgPath, int total, PdfStamper stamper) throws Exception {
        //设置清晰度1f是完全清晰
        PdfGState gs1 = new PdfGState();
        gs1.setFillOpacity(1f);

        Image image = Image.getInstance(IOUtils.toByteArray(new FileInputStream(imgPath)));
        for (int i = 1; i < total; i++) {
            PdfContentByte pdfContentByte = stamper.getOverContent(i);
            //加入图片格式
            pdfContentByte.setGState(gs1);
            //设置图片位置
            image.setAbsolutePosition(x, y);
            //生成图片
            pdfContentByte.addImage(image);
        }
    }

    /**
     * 添加图片和文字水印
     *
     * @param x
     * @param y
     * @param inputPath
     * @param imgPath
     * @param text
     * @param textWidth
     * @param textHeight
     * @return
     * @throws Exception
     */
    public String addwaterImgAndWord(int x, int y, String inputPath, String imgPath, String text, int textWidth, int textHeight) throws Exception {
        String outputPath = "D:/" + UUID.randomUUID().toString() + ".pdf";
        PdfReader reader = new PdfReader(inputPath);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
        //定义页数
        int total = reader.getNumberOfPages() + 1;
        //加载水印
        waterImage(x, y, imgPath, total, stamper);
        waterMark(text, textWidth, textHeight, stamper, total);
        stamper.close();
        reader.close();
        return outputPath;
    }

    public void correct(String str, Document document) throws Exception {
        long startTime = new Date().getTime();
        char[] charList = str.toCharArray();
        Paragraph paragraph = new Paragraph();
        for (int i = 0; i < charList.length; i++) {
            if (charList[i] == '[' || charList[i] == ']' || charList[i] == '<' || charList[i] == '>') {
                continue;
            } else if (i > 1 && charList[i - 1] == '[') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j] == ']') {
                        i = j;
                        break;
                    }
                    Phrase phrase = new Phrase(String.valueOf(charList[j]), CONTENT_FONT_RED);
                    paragraph.add(phrase);
                }
            } else if (i > 1 && charList[i - 1] == '<') {
                for (int j = i; j < charList.length; j++) {
                    if (charList[j] == '>') {
                        i = j;
                        break;
                    }
                    Phrase phrase = new Phrase(String.valueOf(charList[j]), CONTENT_FONT_ORANGE);
                    paragraph.add(phrase);
                }
            } else {
                Phrase phrase = new Phrase(String.valueOf(charList[i]), CONTENT_FONT);
                paragraph.add(phrase);
            }
        }
        document.add(paragraph);
        long endTime = new Date().getTime();
        System.out.println(endTime - startTime);
    }

    public void downloadPicture(String urlString,String imageName) {
        URL url = null;

        try {
            url = new URL(urlString);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            // String imageName = ESSAY_PDF_PICTURE_DATA + ".jpg";
            FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));

            byte[] buffer = new byte[1024];
            int length;

            while ((length = dataInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }

            dataInputStream.close();
            fileOutputStream.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * 按照一定的比例来缩小图片
//     *
//     * @param srcImagePath 读取图片路径
//     * @param width   宽度缩小比例
//     * @param height  高度缩小比例
//     * @throws IOException
//     */
//    public String reduceImageByRatio(String srcImagePath, int width, int height) throws IOException {
//        String toImagePath = "D:/test.jpg";
//        FileOutputStream out = null;
//        try {
//            //读入文件
//            File file = new File(srcImagePath);
//            // 构造Image对象
//            BufferedImage src = javax.imageio.ImageIO.read(file);
//            // 缩小边长
//            BufferedImage tag = new BufferedImage(width , height , BufferedImage.TYPE_INT_RGB);
//            // 绘制 缩小  后的图片
//            tag.getGraphics().drawImage(src, 0, 0, width , height , null);
//            out = new FileOutputStream(toImagePath);
//            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//            encoder.encode(tag);
//        return toImagePath;
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (out != null) {
//                out.close();
//            }
//        }
//        return null;
//    }
}
