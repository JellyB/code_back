package com.huatu.tiku.banckend.dao.provider;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/10/29.
 */
public class OldQuestionSearchProvider {

    public String findQuestionMaterial(@Param("questionIds") List<Integer> questionIds){
        List<String> collect = questionIds.stream().map(String::valueOf).collect(Collectors.toList());
        String join = String.join(",", collect);

        StringBuilder sb = new StringBuilder("SELECT");
        sb.append(" o.pukey AS id, ");
        sb.append(" m.pukey AS parent, ");
        sb.append(" m.stem AS material ");
        sb.append(" FROM ");
        sb.append(" v_multi_question m ");
        sb.append(" INNER JOIN v_obj_question o ON m.pukey = o.multi_id ");
        sb.append(" WHERE ");
        sb.append(" o.pukey in ");
        sb.append("(" ).append(join).append(")");
        System.out.println("query="+sb.toString());
        return sb.toString();
    }

    public String findQuestionByMultiIds(@Param("ids") Set<Long> oldMultiIds){
        List<String> collect = oldMultiIds.stream().map(String::valueOf).collect(Collectors.toList());
        String join = String.join(",", collect);
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(" PUKEY as id , ");
        sb.append(" multi_id as parent ");
        sb.append(" FROM ");
        sb.append(" v_obj_question  ");
        sb.append(" WHERE ");
        sb.append(" bb102 > 0 ");
        sb.append(" AND multi_id IN ");
        sb.append("(" ).append(join).append(")");
        System.out.println("query="+sb.toString());
        return sb.toString();
    }
}
