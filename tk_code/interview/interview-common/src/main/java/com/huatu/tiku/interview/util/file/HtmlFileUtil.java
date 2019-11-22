package com.huatu.tiku.interview.util.file;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.interview.entity.vo.ImgInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Created by x6 on 2017/12/18.
 */
@Slf4j
@Component
public class HtmlFileUtil {

    @Autowired
    private FileUtil uploadFileUtil;

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
            long time = System.currentTimeMillis();//获得当前毫秒级
            String imgPath = account + time + "." + imgType;

            if (GenerateImage(imgBase, imgPath)) {//生成图片成功
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
                final String imageUrl = uploadFileUtil.ftpUploadPic(file).replaceAll("\\\\\"", "");
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
                Pattern p = Pattern.compile(regExImg);
                Matcher m = p.matcher(str);
                if (m.find()) {
                    int start = m.start();
                    int end = m.end();
                    oldImg = str.substring(start, end);
                    str = str.substring(end,str.length());
                }
                newStr = newStr.replace(oldImg, lastSrc);//替换原图片img标签 src为新ftp地址,以及添加图片高度和宽度

            }
        }
        return newStr;
    }

    //base64字符串转化成图片
    public boolean GenerateImage(String imgStr, String imgFilePath) {   //对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) //图像数据为空
            return false;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {//调整异常数据
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

    //删除图片
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
        Pattern p = Pattern.compile(regExImg);
        Matcher m = p.matcher(str);
        int i = 0;

        while (m.find()) {
            i++;
            ImgInfoVO imgInfo = new ImgInfoVO();
            if (m.group(1).contains("data:image")) {//若为base64形式，加入返回结果
                imgInfo.setImgsrc(m.group(0));
                imgInfo.setBase(m.group(1));
                if (m.group(0).contains("height:")) {//若包含宽、高数据，也提取出来
                    String regExNew = "style=\"height: ([0-9]+)px;width: ([0-9]+)px;\"";
                    Pattern pNew = Pattern.compile(regExNew);
                    Matcher mNew = pNew.matcher(m.group(0));
                    while (mNew.find()) {
                        imgInfo.setHeight(Integer.parseInt(mNew.group(1)));
                        imgInfo.setWidth(Integer.parseInt(mNew.group(2)));
                    }
                }
                result.add(imgInfo);
            }
        }
        log.info("=========解析html获得图片个数======="+i);
        return result;
    }

    /**
     * 对html字符串进行处理，只有一个段落，即只包含一个<p></p>，同时去除所有非<br><u><img>的标签，并识别其中换行
     *
     * @param str
     * @return
     */
    public  String htmlManage(String str) {
        if(isEmptyContent(str)){
            return "";
        }
        if (str.length() > 0) {//不为空进行处理
            int flag = 0;
            int charAt = 0;
//            for(int i=0;i<str.length();i++){
//                char ch = str.charAt(i);
//                if(ch==' '||ch=='\f'||ch=='\r'||ch=='\t'||ch=='\b'){
//                    continue;
//                }else{
//                    if(flag==0&&ch!='<'){
//                        charAt = i;
//                        break;
//                    }else if(flag==0&&ch=='<'){
//                        flag = 1;
//                        if(str.length()>i+4){
//                            log.info("前面部分={}",str.substring(i+1,i+4));
//                            if(str.substring(i+1,i+4).equals("img")){
//                                log.info("用于测试是否已经成功");
//                                break;
//                            }
//                        }
//                    }else if(flag==1&&ch=='>'){
//                        flag = 0;
//                    }
//                }
//            }
            str = str.substring(charAt);
            //  log.info("过滤完前面html标签后的str={}",str);
            String regEx1 = "<(?!br|u(?!l)|img|/(p|u(?!l)|li))(.*?)>";//识别所有非<br><u><img></p><u></li>的标签
            str = str.replaceAll(regEx1, "");
            String regEx2 = "</(?!u(?!l))(.*?)>";//识别所有非</u>的</……>标签
            str = str.replaceAll(regEx2, "<br/>");
            String regExNem = "<u>(<br>|<br/>)*</u>";//识别所有非</u>的</……>标签
            str = str.replaceAll(regExNem, "");
            str = str.replaceAll("<br />", "<br/>");
            String regEx3 = "(<br>|<br/>)((\\s)*(<br>|<br/>|(\\s))*)*";//识别连续多个<br>出现情况
            str = str.replaceAll(regEx3, "<br/>");
            str = str.replaceAll("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "<br/>");
            if(str.endsWith("<br/>")){
               str = str.substring(0,str.length()-"<br/>".length());
            }
            str = str.replaceAll("<br/>", "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            //重复编辑的时候，把前端把空格变成了asc码为160的空格，手动去除，再次拼接
            for (int i = 0; i < 20; i++) { //20是随便写的，为了去掉这些空格
                str = str.replaceFirst("\\u00A0", "");
            }
            System.out.println("java:"+str);
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            for (int i = 0; i < 20; i++) { //20是随便写的，为了去掉这些空格
                Matcher m = p.matcher(str);
                str = m.replaceFirst("");
            }
            //  str.replace("&nbsp;"," ");
            str = "<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + str + "</p>";
            log.info("html标签全部处理完毕之后的str={}", str);
        }
        return str;
    }

    private boolean isEmptyContent(String str) {
        if(StringUtils.isBlank(str)){
            return true;
        }
        String content = str.replaceAll("<[^>]*>","");
        content = content.replace("&nbsp;","");
        if("".equals(content.trim())){
            return true;
        }
        log.error("remain content ={}",content.length()>10?content.substring(0,10):content);
        return false;
    }

//    public static void main(String[] args) {
//        String str ="              合作社员工小林说：“自合作社创办以来，我就到社里工作了，不用到外地打工，在这里，每月工资3000左右，解决了家里的生活问题，比到外面打工好多了。”<br />        吴如意是当地的菠萝大户，她刚参加了镇里组织的菠萝大户国外考察活动。一从国外考察回来，她就琢磨着怎<br />";
//        for (int i = 0; i < 20; i++) { //10是随便写的，为了去掉这些空格
//            str = str.replaceFirst("\\u00A0", "");
//        }
//        HtmlFileUtil fileUtil  = new HtmlFileUtil();
//        fileUtil.htmlManage(str);
//      //  System.out.println(str);
//      //  System.out.println( htmlManage(str1));
//    }


}
