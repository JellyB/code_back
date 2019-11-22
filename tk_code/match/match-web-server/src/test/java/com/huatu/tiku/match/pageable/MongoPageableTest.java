package com.huatu.tiku.match.pageable;

import com.huatu.common.test.BaseWebTest;
import com.huatu.tiku.match.dao.document.MatchDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-01-09 下午4:00
 **/
public class MongoPageableTest extends BaseWebTest {

    @Autowired
    private MatchDao matchDao;

    @Test
    public void pageableTest(){
        matchDao.findMatchBySubjectPageable(1, new PageRequest(1, 20));
    }
}
