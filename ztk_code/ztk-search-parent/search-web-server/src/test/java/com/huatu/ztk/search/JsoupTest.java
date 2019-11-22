package com.huatu.ztk.search;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shaojieyue
 * Created time 2016-07-02 10:55
 */
public class JsoupTest {
    private static final Logger logger = LoggerFactory.getLogger(JsoupTest.class);

    public static void main(String[] args) {
        final String text = Jsoup.parse("1234<p>asdf</p><img src=\"http://xxx\"></img> <table width=\"200\" border=\"1\"><tbody><tr class=\"firstRow\"><td> 2</td><td> 27</td><td> 5</td></tr><tr><td> 21</td><td> 512</td><td> 22</td></tr><tr><td> 9</td><td> 125</td><td> （）</td></tr></tbody></table>").body().text();
        System.out.println(text);
    }
}
