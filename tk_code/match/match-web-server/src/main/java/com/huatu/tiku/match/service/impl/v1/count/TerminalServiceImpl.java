package com.huatu.tiku.match.service.impl.v1.count;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.dao.document.AnswerCardDao;
import com.huatu.tiku.match.service.v1.count.TerminalService;
import com.huatu.tiku.match.service.v1.meta.MatchUserMetaService;
import com.huatu.ztk.paper.bean.AnswerCard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/3/20.
 */
@Service
@Slf4j
public class TerminalServiceImpl implements TerminalService {

    @Autowired
    MatchUserMetaService matchUserMetaService;
    @Autowired
    AnswerCardDao answerCardDao;
    @Override
    public Map<Integer, Integer> groupByTerminal(int matchId) {
        Function<Integer,List<MatchUserMeta>> function = ((id)->{
            Example example = new Example(MatchUserMeta.class);
            example.and().andEqualTo("matchId",matchId).andGreaterThan("practiceId",-1);
            return matchUserMetaService.selectByExample(example);
        });
        int page = 1;
        Map<Integer, Integer> map = Maps.newHashMap();
        while (true){
            //分页查询统计信息
            PageInfo<MatchUserMeta> pageInfo = PageHelper.startPage(page, 100).doSelectPageInfo(() -> {
                function.apply(matchId);
            });
            List<MatchUserMeta> list = pageInfo.getList();
            if(CollectionUtils.isEmpty(list)){
                break;
            }
            //统计答题卡终端
            countTerminal(list,map);
            if(pageInfo.isHasNextPage()){
                page ++ ;
            }else{
                break;
            }
        }
        return map;
    }

    private void countTerminal(List<MatchUserMeta> list, Map<Integer, Integer> map) {
        List<AnswerCard> answerCards = answerCardDao.findById(list.stream().filter(i -> i.getPracticeId() > -1).map(MatchUserMeta::getPracticeId).collect(Collectors.toList()));
        Map<Integer, List<AnswerCard>> collect = answerCards.parallelStream().collect(Collectors.groupingBy(AnswerCard::getTerminal));
        for (Map.Entry<Integer, List<AnswerCard>> entry : collect.entrySet()) {
            Integer count = map.getOrDefault(entry.getKey(), 0);
            count += entry.getValue().size();
            map.put(entry.getKey(),count);
        }
    }
}
