package com.huatu.ztk.question.util;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by shaojieyue
 * Created time 2016-06-07 16:35
 */
public class ImageUtil {

    public static void main(String[] args) throws IOException {
//        BufferedImage thumbnail = Scalr.resize(image, 150);

//        ImagePlus imagePlus = new ImagePlus("http://i1.mhimg.com/M01/22/C6/CgAAhldWNoGAKDmBAAFEa8b5obQ431.jpg");
        ImagePlus imagePlus = new ImagePlus("http://tiku.huatu.com/cdn/images/vhuatu/tiku/5/5770faa27d1b8bdc59d9c07b173c978c4c65ffa5.png");
        System.out.println(imagePlus.getWidth());
    }

    public static final ImagePlus parse(String url){
        ImagePlus imagePlus = null;
        for (int i = 0; i < 3; i++) {
            imagePlus = new ImagePlus(url);
            //保证能获取到正常的图片，否则继续尝试
            if (imagePlus.getWidth() > 0 && imagePlus.getHeight() > 0) {
                break;
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
            }
        }

        return imagePlus;
    }
}
