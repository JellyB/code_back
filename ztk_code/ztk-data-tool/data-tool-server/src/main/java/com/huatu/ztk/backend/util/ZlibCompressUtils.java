package com.huatu.ztk.backend.util;

import com.huatu.ztk.backend.system.dao.SystemDao;
import com.huatu.ztk.commons.JsonUtil;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by linkang on 10/11/16.
 */
public class ZlibCompressUtils {


    public static String compress(String content) {
        byte[] data = content.getBytes();
        byte[] output = new byte[0];
        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();

        byte[] ret = Base64.encodeBase64(output);
        return new String(ret);
    }


    public static String uncompress(String content) {
        byte[] zbytes = Base64.decodeBase64(content.getBytes());
        String unzipped = null;
        try {
            byte[] input = new byte[zbytes.length + 1];
            System.arraycopy(zbytes, 0, input, 0, zbytes.length);
            input[zbytes.length] = 0;
            ByteArrayInputStream bin = new ByteArrayInputStream(input);
            InflaterInputStream in = new InflaterInputStream(bin);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(512);
            int b;
            while ((b = in.read()) != -1) {
                bout.write(b);
            }
            bout.close();
            unzipped = bout.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return unzipped;
    }

    private static Map<String,Object> cast(String result){
        result=ZlibCompressUtils.uncompress(result);//解码
        return JsonUtil.toMap(result);

    }

    public static void main(String[] args){
/*
        System.out.println(ZlibCompressUtils.uncompress("eJyrVnIsKHje2fFszpqn/duVrJSM9IyUdJSeTe59snfOi3X7ni5pBwpmBmTk56UqmAJlnm/e/Xz3fGQdlnrGeqZKtQBAJyLe"));
    System.out.println(ZlibCompressUtils.uncompress("eNq9ULFugzAU/BXLUyKljrGxTbJVDK2UhqVILF4MPIQFrREBVUrVf+8jKM0fdDlZujvfvfumz8Ng5xhA2lnJxtAjPYer73tHdyunYl7b2RwajlwG06VyA9xJI+LEztqICknFONkU/rMOXxeS5USxaEtQ1kMB5clPdq+kYVKTzek1P7/tSO87IC9QdWFL0nYMH2D3MmKcRVpxpiV5d40b/Z8vb6EIY18TjfkYq0Wz9BP6hvg2SiRpCJ0HepzGGRZVUgL2Txxf+l/bpzS7mU2jIkSoq+WKEr/QnEcPmwKzTrLY8CgpVhsmoFRIc1/NxFw/VsOu/7qCHzBbcyYESxClpj+/58KGzw=="));
    System.out.println(ZlibCompressUtils.uncompress("eNrFUE1rwkAU/CuPnBTsut8b7amE0oLVHhTSw16S+IKLaRNiQsHS/94Xg/oTenn7YGbezsxP9NQ0vteIyvdGlS5aRuv6HKoqi2YjZjTf+94tSk7YBrtTkTV4BZ3Use+tkwWBhnGYpOFrX3+fYLMDy8QjpO+p1VMgdoUp5qvQ+blRjikLk9Xrbv02gyocEV6wONZTSA5t/Yl+rheMM2mlYlLCNiuzNtx022eQ7APW2GXbFgTj5IZMWFkObqW9TNqdkXFS18eA0bJrexxYcY6UJs74kOZ8eEg2F7ErjaCJ+2LIlNMJy7m4ywy6saBBRhGVHGX0A1GlctcOneb23iFZ/sdOQkNOrGBCaSY4vXH0+wcBlYyk"));
    System.out.println(ZlibCompressUtils.uncompress("eJyrVnIsKHje2fFszpqn/duVrJSM9IyUdJSeTe59snfOi3X7ni5pBwpmBmTk56UqmAJlnm/e/Xz3fGQdlnrGeqZKtQBAJyLe"));
    System.out.println(ZlibCompressUtils.uncompress("eNrFUMtqwzAQ/BWhUwKpLcmW5aSnNocWUreB9HUQBFleU2ETGce+pPTfu4oJ+YSAtEiandXM/NKHrtNjCpDoUSa1oita+JNrW0MXEyZTVulRLWuG2CsMR2s6uIBKpLkeMyUsgjJiZOa2pron6+0HedsRzvZ8z0nrGiCFseHpe06Q2cIXlBs36DhjIhIRT8ls8/xevCym5iewjZ+TT+iPzh90zBnOLnzpWsBL+sgZIztTm95NEzgKQh2ZqINgkZ0rnpUU+dr7xgFdDf0IoSsvAQ3lhgVDp587eziTVS05VqhssFXiiIwxfqVJUFNGgRZcTiz8ADtFoi4pqpRl1xR1fONUXIdiBM8jgYsvcef07x/YdZCb"));
*/

        String str="eNrFUMtqwzAQ/BWhUwKpLcmW5aSnNocWUreB9HUQBFleU2ETGce+pPTfu4oJ+YSAtEiandXM/NKHrtNjCpDoUSa1oita+JNrW0MXEyZTVulRLWuG2CsMR2s6uIBKpLkeMyUsgjJiZOa2pron6+0HedsRzvZ8z0nrGiCFseHpe06Q2cIXlBs36DhjIhIRT8ls8/xevCym5iewjZ+TT+iPzh90zBnOLnzpWsBL+sgZIztTm95NEzgKQh2ZqINgkZ0rnpUU+dr7xgFdDf0IoSsvAQ3lhgVDp587eziTVS05VqhssFXiiIwxfqVJUFNGgRZcTiz8ADtFoi4pqpRl1xR1fONUXIdiBM8jgYsvcef07x/YdZCb";
        //String str="eJyrVnIsKHje2fFszpqn/duVrJSM9IyUdJSeTe59snfOi3X7ni5pBwpmBmTk56UqmAJlnm/e/Xz3fGQdlnrGeqZKtQBAJyLe";
          /*   str=ZlibCompressUtils.uncompress(str);
        System.out.println(str);

        System.out.println(FuncStr.ascii2native(str));*/

        Map map=cast(str);
        for(Object key:map.keySet()){
            System.out.println("key===="+key+"values=========="+map.get(key));
        }


        System.out.println(ZlibCompressUtils.uncompress("H4sIAAAAAAAAAJ1YwWrbQBD9F51T2F3NjHZz7ckQeukx5JBgHwJVXOMYAqHQQ+vklOTSQC+F0kJ9CSmEgqGQr4kV9S8q2c6SalWjt7ex5NHM7r6Z92Z3T5N82J+8GfT6yXaibbK1/v1qPx9UTxbzeXk7XZx/L65v/LveUX9wUv+9enJ4PMjHO4fj42R79zQZLT8zNEaYuHo76o1fDvO3w8lR/fxF7TB6PTlY/3/v3dY/LpziLhZ2yQzsYnAXxl0ow6PgLpnGXQR3wRNzeGIWxpho3MUo3MXBLim8Y5LC5yIpnpjuUmJLp+edJGt2knL2vry9ebyalj++Pcwvyq+z8v4+aClmU0vJ4OTZRoAKR7uDEcKOcBccISqiQOAdEw33bdHw8kXD50I4YMjCR0kWbvVk4eUTjjFy8LmQxflU4UyncPDjSOYUr8oUTwxvwkx4YoQnRni35AjNhkfJIkosAsl47Tt8LQqXrAqXRhoHjMLXgvdk1hFr6ZJYU04Y3ZQTxaeff84uFx/vHu9+Byoi3aQiIngElwQUITxwGmVcmnKKRyG8wwuOJZx5pVP1BVhSTSytxtviYlap0wBL9B8s5axbhs9G9HWaNXxWVuYt6y33ZFVlHrRXHEYUMa1FTNE4VeBNnB2OPBuhkiO0eISwxmUiPhITPhKzwjGmIigcpwqFIxmfXthELB+vF4ODHwcM4+M9mwjJiyfW6RKhEQVHMkVscoTkxYkK10mC3zkKztOCjyKCb7JQlxILrpBck6fLXx+K68+L82nx5Srgad7A02HGbTxNyzuZlcXeEm9l3rLPeboOENZQWwA2T59lI97KvGW95ZoBwk7YGkB8APEBxAcQH0CCACFAWwM4/wnnZcuSDFaW9pZJ6hPd+wuSWUNPYBgAAA=="));
    }
}
