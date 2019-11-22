package interview.dao.hownet;

import interview.bean.SynonymStop;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-09-27  16:23 .
 */
@Repository
public interface SynonymStopDao extends Mapper<SynonymStop> {
    /**
     * 查找近义词
     * @param groupId
     * @return
     */
    List<SynonymStop> findSynonym(@Param("groupId")int groupId);
}
