package com.huatu.tiku.util.file;

import com.huatu.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * TODO: notice by lijun 此处不建议使用spring组件方式，建议修改成静态类实现
 * Created by x6 on 2017/12/18.
 */
@Slf4j
@Component
public class HtmlFileUtil {

    /**
     * 根据字符串，查看是否包括图片信息，若包括将图片信息提取出来，生成本地图片，再上传ftp，并将url替换原图片sc
     *
     * @param str
     * @return
     */
    public String imgManage(String str, String account, int type) throws BizException, IOException {//加入account参数是为了防止临时生成的图片有重名

        List<ImgInfoVO> result = findSrcs(str);
        String newStr = str;
        for (int i = 0; i < result.size(); i++) {
            String imgMessge = result.get(i).getImgsrc();
            String baseAll = result.get(i).getBase();
            String[] sourceStrArray = baseAll.split(";base64,");
            String imgType = sourceStrArray[0].replaceAll("data:image/", "");
            String imgBase = sourceStrArray[1];
            //获得当前毫秒级
            long time = System.currentTimeMillis();
            String imgPath = account + time + "." + imgType;
            //生成图片成功
            if (GenerateImage(imgBase, imgPath)) {
                File file = new File(imgPath);
                log.info("file的绝对地址：", file.getCanonicalPath());
                int width = result.get(i).getWidth();
                int height = result.get(i).getHeight();
                if (width == -1 && height == -1) {
                    BufferedImage bufferedImage = ImageIO.read(new File(imgPath));
                    width = bufferedImage.getWidth();
                    height = bufferedImage.getHeight();
                }
//                final String imageUrl = "";
                final String imageUrl = UploadFileUtil.getInstance().ftpUploadPic(file).replaceAll("\\\\\"", "");
//                ;//上传ftp服务器
                //文件上传成功后，删除本地文件
                file.delete();
                String lastSrc = "<img src=\"" + imageUrl + "\" width=\"" + width + "\" height=\"" + height + "\"/>";
                if (type == 1) {
                    lastSrc = "src=\\\"" + imageUrl + "\\\" width=\\\"" + width + "\\\" height=\\\"" + height + "\\\"/>";
                }
                log.info("imageUrl={},lastSrc={},imgMessge={}", imageUrl, lastSrc, imgMessge);
                //未处理之前的img 标签
                String oldImg = "";
                String regExImg = "<img?(.*?)(\"?>|\"?/>)";
                Pattern p = compile(regExImg);
                Matcher m = p.matcher(str);
                if (m.find()) {
                    int start = m.start();
                    int end = m.end();
                    oldImg = str.substring(start, end);
                    str = str.substring(end, str.length());
                }
                //替换原图片img标签 src为新ftp地址,以及添加图片高度和宽度
                newStr = newStr.replace(oldImg, lastSrc);
            }
        }
        return newStr;
    }

    /**
     * base64字符串转化成图片
     * 对字节数组字符串进行Base64解码并生成图片
     *
     * @param imgStr
     * @param imgFilePath
     * @return
     */
    public static boolean GenerateImage(String imgStr, String imgFilePath) {
        //图像数据为空
        if (StringUtils.isBlank(imgFilePath)) {
            return false;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                //调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            //生成图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 删除图片
     *
     * @param sPath
     * @return
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }


    /**
     * 根据输入字符串，提取图片src
     *
     * @param str
     * @return
     */
    public List<ImgInfoVO> findSrcs(String str) {
        List<ImgInfoVO> result = new ArrayList<>();
        String regExImg = "src=\"?(.*?)(\"?>|\"?/>)";
//                "src=\"?(.*?)\"/>";
        Pattern p = compile(regExImg);
        Matcher m = p.matcher(str);
        int i = 0;

        while (m.find()) {
            i++;
            ImgInfoVO imgInfo = new ImgInfoVO();
            //若为base64形式，加入返回结果
            if (m.group(1).contains("data:image")) {
                imgInfo.setImgsrc(m.group(0));
                imgInfo.setBase(m.group(1));
                //若包含宽、高数据，也提取出来
                if (m.group(0).contains("height:")) {
                    String regExNew = "style=\"height: ([0-9]+)px;width: ([0-9]+)px;\"";
                    Pattern pNew = compile(regExNew);
                    Matcher mNew = pNew.matcher(m.group(0));
                    while (mNew.find()) {
                        imgInfo.setHeight(Integer.parseInt(mNew.group(1)));
                        imgInfo.setWidth(Integer.parseInt(mNew.group(2)));
                    }
                }
                result.add(imgInfo);
            }
        }
        log.info("=========解析html获得图片个数=======" + i);
        return result;
    }

    /**
     * 对html字符串进行处理，只有一个段落，即只包含一个<p></p>，同时去除所有非<br><u><img>的标签，并识别其中换行
     * updateBy lijun 2018-11-07 去掉 <p><br/></br> 兼容自动添加 6个&nbsp 处理。
     *
     * @param str
     * @return
     */
    public String htmlManage(String str) {
        //不为空进行处理
        if (StringUtils.isNotBlank(str)) {
            int charAt = 0;
            str = str.substring(charAt);
            log.info("过滤完前面html标签后的str={}", str);
            //需要保留的标签br,u,ul,span,img,strong以及p，li的结尾标签
            String regEx1 = "<(?!br|u(?!l)|span|img|strong|/(p|u(?!l)|li|span|strong))(.*?)>";
            str = str.replaceAll(regEx1, "");
            //将不是u,ul,span,strong的结尾标签变为换行标签br(即将</p></li>换成<br/>，左边标签在上一步已经去掉)
            String regEx2 = "</(?!u(?!l)|span|strong)(.*?)>";
            str = str.replaceAll(regEx2, "<br/>");
            //将无效的ul标签删除（ul中间无内容或者内容只有br标签）
            String regExNem = "<u>(<br>|<br/>)*</u>";
            str = str.replaceAll("<br />", "<br/>");
            str = str.replaceAll(regExNem, "");
            //识别连续多个<br>出现情况
            String regEx3 = "(<br>|<br/>)((\\s)*(<br>|<br/>|(\\s))*)*";
            str = str.replaceAll(regEx3, "<br/>");
            //str = str.replaceAll("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "<br/>");
            if (str.endsWith("<br/>")) {
                str = str.substring(0, str.length() - "<br/>".length());
            }
//            str = str.replaceAll("<br/>", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            /**
             * @update huangqp 20180927
             * 原有注解：重复编辑的时候，把前端把空格变成了asc码为160的空格，手动去除，再次拼接
             * 20是随便写的，为了去掉这些空格
             * 现在注解：申论前端适用，但是pandora项目不适用
             */
//            for (int i = 0; i < 20; i++) {
//                str = str.replaceFirst("\\u00A0", "");
//            }
            Pattern p = compile("\\s*|\t|\r|\n");
            //20是随便写的，为了去掉这些空格
            for (int i = 0; i < 20; i++) {
                Matcher m = p.matcher(str);
                str = m.replaceFirst("");
            }
//            String blank = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
//            //属于段落，但是已有空格，不做处理
//            if(isParagraph && str.indexOf("&nbsp;") >=0 &&str.indexOf("&nbsp;") < 5){
//                blank = "";
//            }else if(!isParagraph){     //不属于段落，不做处理
//                blank = "";
//            }
            str = "<p>" + str + "</p>";
            log.info("html标签全部处理完毕之后的str={}", str);
        }
        return str;
    }

    private boolean isEmptyContent(String str) {
        if (StringUtils.isBlank(str)) {
            return true;
        }
        String content = str.replaceAll("<[^>]*>", "");
        content = content.replace("&nbsp;", "");
        if ("".equals(content.trim())) {
            return true;
        }
        log.error("remain content ={}", content.length() > 10 ? content.substring(0, 10) : content);
        return false;
    }

    /**
     * 将pandora项目富文本内容转化为存储格式
     *
     * @param target      需要处理的富文本内容
     * @param isParagraph 是否是段落格式
     * @return
     */
    public String html2DB(String target, boolean isParagraph) {
        if (StringUtils.isBlank(target)) {
            return "";
        }
        try {
            //主要处理base64图片数据转换为链接数据
            target = imgManage(target, "admin", 0);
        } catch (IOException e) {
            log.error("{}", e);
            e.printStackTrace();
        }
        return htmlManage(target);
    }


}
