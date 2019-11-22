package interview.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: xuhuiqiang
 * Time: 2018-09-27  10:47 .
 */
@RestController
@RequestMapping(value = "/_monitor", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ServerCheckController {
    /**
     * 空接口，检测服务器状态
     */
    @RequestMapping(value = "health")
    public void check() {

    }
}
