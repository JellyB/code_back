import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.paper.bean.PaperQuestionBean;
import com.huatu.ztk.backend.paper.service.PaperService;
import com.huatu.ztk.paper.bean.Paper;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2018/5/23
 */
@Slf4j
public class MatchStaticHtmlTest extends BaseTestW {

    @Autowired
    private PaperService paperService;

    //文件路径
    private static final String path = "/Users/junli/Tool/java/source-huatu/ztk-backend-parent/backend-web-server/src/main/webapp/WEB-INF/pages/";
    //paperId
    //2018省考万人模考第二十三季
    private static final int paperId = 3526734;

    @Test
    public void createHtml() throws Exception {
        //路径信息
        String tempPath = path + "match.ftl";
        String resultPath = path + "result.html";
        //数据
        //1.通过paperId 查询当前的试卷信息
        Paper paper = paperService.findPaperById(paperId);

        HashMap<String, Object> data = new HashMap<>();
        //暂存 modules
        data.put("paperName", paper.getName());
        data.put("modules", paper.getModules());

        List<PaperQuestionBean> questionList = (List<PaperQuestionBean>) paperService.getQuestionByPaper(paper);
        //试题分组，分成 并按照 18 - 17 - 21 -20 -19 模块顺序排列
        Map<Integer, List<PaperQuestionBean>> questionData = questionList.parallelStream()
                .collect(Collectors.groupingBy(
                        paperBean -> paperBean.getQuestion().getSubject()));
        data.put("questionData", questionList);

        //选项前缀
        ArrayList<String> choiceStem = new ArrayList<>();
        choiceStem.add("A");
        choiceStem.add("B");
        choiceStem.add("C");
        choiceStem.add("B");
        data.put("choiceStem", choiceStem);


        buildHtml(tempPath, resultPath, data);
    }


    public static void buildHtml(String tempPath, String resultPath, Map<String, Object> data) {
        BufferedInputStream in = null;
        FileWriter out = null;
        try {
            //String path = this.getClass().getResource("/").getPath();
            //模板文件
            File file = new File(tempPath);
            //构造输入流
            in = new BufferedInputStream(new FileInputStream(file));
            int len;
            byte[] bytes = new byte[1024];
            //模板内容
            StringBuilder content = new StringBuilder();
            while ((len = in.read(bytes)) != -1) {
                content.append(new String(bytes, 0, len, "utf-8"));
            }

            //构造Configuration
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
            //构造StringTemplateLoader
            StringTemplateLoader loader = new StringTemplateLoader();
            //添加String模板
            loader.putTemplate("test", content.toString());
            //把StringTemplateLoader添加到Configuration中
            configuration.setTemplateLoader(loader);
            //获取模板
            Template template = configuration.getTemplate("test");
            //构造输出路
            out = new FileWriter(resultPath);
            //生成HTML
            template.process(data, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
