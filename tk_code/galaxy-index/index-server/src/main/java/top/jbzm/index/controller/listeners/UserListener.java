package top.jbzm.index.controller.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.jbzm.common.ErrorResult;
import top.jbzm.exception.MyException;
import top.jbzm.index.dao.IndexDao;
import top.jbzm.index.dto.DataStorm;

/**
 * @author jbzm
 * @date Create on 2018/3/21 13:55
 */
@RestController
@RequestMapping("user")
@Slf4j
public class UserListener {
    private final IndexDao indexDao;

    @Autowired
    public UserListener(IndexDao indexDao) {
        this.indexDao = indexDao;
    }

    /**
     * 统一制作索引
     *
     * @param dataStorm
     * @return
     */
    @PostMapping("data/storm")
    public Object dataStorm(@RequestBody DataStorm dataStorm) {
        if (dataStorm.getData() == null || dataStorm.getData().size() == 0) {
            throw new MyException(ErrorResult.create(204, "传输集合内无数据"));
        }
        log.info("接受到了" + dataStorm.getData().size());
        return indexDao.indexDataStorm(dataStorm);
    }
}
