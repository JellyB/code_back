package interview.controller;

import interview.util.SemanticUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Author: xuhuiqiang
 * Time: 2018-08-09  15:25 .
 */
@RestController
@RequestMapping(value = "/init", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class InitController {
    private static final Logger logger = LoggerFactory.getLogger(InitController.class);

    @Autowired
    private SemanticUtil semanticUtil;

    @RequestMapping(value = "/test", method = RequestMethod.PUT)
    public Object similarWord(@RequestParam List<String> str1,
                              @RequestParam List<String> str2) throws Exception {
        return semanticUtil.simWordsStr(str1,str2);
    }

}
