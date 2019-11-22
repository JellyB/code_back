package top.jbzm.index.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.jbzm.index.dao.IndexDao;

/**
 * @author jbzm
 * @date Create on 2018/3/21 16:59
 */
@RestController
@RequestMapping("index")
public class IndexController {
    @Autowired
    private IndexDao indexDao;

    @GetMapping("findCursor")
    public Object findCursor(@RequestParam int type) {
        return indexDao.findCursor(type);
    }
}
