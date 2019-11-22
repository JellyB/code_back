package interview.dao.hownet;

import interview.bean.HownetPrimitive;
import interview.bean.HownetWord;
import interview.bean.HownetWordRelation;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-09-27  14:48 .
 */
@Repository
public interface HownetDao extends Mapper<HownetPrimitive> {
    /**
     * 查找义原
     * @return
     */
    List<HownetPrimitive> findPrimitive();
    /**
     * 查找hownetWord
     * @return
     */
    List<HownetWord> findHownetWord();
    /**
     * 查找hownetWord关系
     * @return
     */
    List<HownetWordRelation> findHownetWordRelation();
}
