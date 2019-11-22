package interview.controller;

import interview.service.CorrectServiceV1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: xuhuiqiang
 * Time: 2018-08-20  13:05 .
 */
@RestController
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CorrectControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(InitController.class);

    @Autowired
    private CorrectServiceV1 correctService;

    @RequestMapping(value = "/correct", method = RequestMethod.PUT)
    public Object similarWord(@RequestBody String answer) throws Exception {
        return correctService.correct(answer);
    }
}
