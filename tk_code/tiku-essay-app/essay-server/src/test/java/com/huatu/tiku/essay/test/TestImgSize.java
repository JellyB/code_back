package com.huatu.tiku.essay.test;

import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;
import javax.imageio.ImageIO;
import org.mockito.stubbing.Answer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * @author zhaoxi
 * @Description: 图片读取测试类
 * @date 2018/8/3下午3:46
 */
public class TestImgSize {
    public static void main(String args[]) throws Exception {
//        File file = new File("http://tiku.huatu.com/cdn/pandora/img/question/33ced278-df91-4037-a11f-97f52f23d1b2.png");
//        int imgHeight = getImgHeight(file);
//        int imgWidth = getImgWidth(file);
//        System.out.println("imgHeight:" +imgHeight+
//                "imgWidth:"+imgWidth);

//        URL url = new URL("http://tiku.huatu.com/cdn/pandora/img/question/33ced278-df91-4037-a11f-97f52f23d1b2.png");
//        URLConnection connection = url.openConnection();
//        connection.setDoOutput(true);
//        BufferedImage image = ImageIO.read(connection.getInputStream());
//        int srcWidth = image .getWidth();      // 源图宽度
//        int srcHeight = image .getHeight();    // 源图高度
//
//        System.out.println("srcWidth = " + srcWidth);
//        System.out.println("srcHeight = " + srcHeight);

        LinkedList<EssayUserCorrectGoods> essayUserCorrectGoods = new LinkedList<>();
        EssayUserCorrectGoods goods = new EssayUserCorrectGoods();
        goods.setUsefulNum(100);
        essayUserCorrectGoods.add(goods);
        LinkedList<EssayUserCorrectGoods> newUserCorrectGoods = new LinkedList<>();

        for (EssayUserCorrectGoods answer : essayUserCorrectGoods) {//遍历提交的答案,过滤掉重复提交的答案和无效的答案，校验用时，让用时合理化
//            final int questionId = answer.getQuestionId();
//            //查询出试题所在索引位置
//            final int index = questions.indexOf(questionId);
//
//            if (index < 0) {//说明该试题不存在
//                logger.error("questionId={} not in practiceId={}",questionId,practiceId);
//                throw new BizException(PracticeErrors.SUBMIT_ANSWER_QUESTION_NO_EXIST);
//            }
//
//            //如果该答案和已有的不一样,说明是新答案,否则说明是重复提交的
//            logger.info("answer={},is equal 0:{}",answer,answer.getAnswer().equals("0"));
//            if(answer.getAnswer().equals("0")){
//                continue;
//            }
//            if (answer.getTime() > MAX_ANSWER_TIME) {//检查答题时间是否超越了最大值
//                answer.setTime(MAX_ANSWER_TIME);
//            } else if (answer.getTime() < 1) { //不合理的时间设置为50秒
//                answer.setTime(50);
//            }
//            //重复提交答案（两次答案一样）则不进行处理,未作答（答案为0）的也不进行处理
//            if (answersArray[index].equals(answer.getAnswer())) {
//                continue;
//            }
            answer.setUsefulNum(1111);
            //添加到有效答案列表
            newUserCorrectGoods.add(answer);
        }
        for(EssayUserCorrectGoods answer : essayUserCorrectGoods){
            answer.getUsefulNum();
        }

    }

    public static int getImgWidth(File file) {
        InputStream is = null;
        BufferedImage src = null;
        int ret = -1;
        try {
            is = new FileInputStream(file);
            src = javax.imageio.ImageIO.read(is);
            ret = src.getWidth(null); // 得到源图宽
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


    public static int getImgHeight(File file) {
        InputStream is = null;
        BufferedImage src = null;
        int ret = -1;
        try {
            is = new FileInputStream(file);
            src = javax.imageio.ImageIO.read(is);
            ret = src.getHeight(null); // 得到源图高
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

}
