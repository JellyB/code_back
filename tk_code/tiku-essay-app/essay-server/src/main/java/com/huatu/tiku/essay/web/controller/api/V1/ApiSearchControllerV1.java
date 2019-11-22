package com.huatu.tiku.essay.web.controller.api.V1;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.service.EssaySearchService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author jbzm
 * @date Create on 2018/2/2 21:02
 */
@RestController
@RequestMapping(value = "api/v1/search", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ApiSearchControllerV1 {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private EssaySearchService essaySearchService;
    @Autowired
    private EssayPaperService essayPaperService;


    /**
     * 有需要再添加,用户查询缓存.
     */
    @LogPrint
    @GetMapping("content")
    public Object keyWordCache(@Token UserSession userSession) {
        return null;
    }


    /**
     * 根据关键词搜索单题组
     * @param userSession
     * @param content
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping("question")
    public Object searchQuestion(@Token(check = false) UserSession userSession, String content,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int pageSize,
                                 @RequestHeader String cv,
                                 @RequestHeader int terminal) {

        //只要前十个字符后面多余的失效
        if (content.length() > 10) {
            content = content.substring(0, 10);
        }
        return essaySearchService.searchQuestion(userSession, content, page,pageSize,cv,terminal);
    }


    /**
     * 根据关键词搜索套题
     * @param userSession
     * @param content
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping("paper")
    public Object searchPaper(@Token UserSession userSession,
                              @RequestParam(defaultValue = "")String content,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int pageSize) {

        return essaySearchService.searchPaper(content,page, pageSize);
    }


    /**
     * 将已上线试卷数据导入es
     * @return
     */
    @LogPrint
    @GetMapping("import/paper/{paperId}")
    public Object importPaper2Search(@PathVariable long paperId) {
        essaySearchService.importPaper2Search(paperId);
        return null;
    }




    /**
     * 将已上线试卷数据导入es
     * @return
     */
    @LogPrint
    @GetMapping("import/question")
    public Object importQuestion2Search() {
        essaySearchService.importQuestion2Search();
        return null;
    }

}
