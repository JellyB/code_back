package com.huatu.tiku.teacher.service.impl.match;

import com.huatu.tiku.match.bean.entity.MatchQuestionMeta;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.OldPaperService;
import com.huatu.tiku.teacher.service.match.MatchQuestionMetaService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Paper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/10/16.
 */
@Service
public class MatchQuestionMetaServiceImpl extends BaseServiceImpl<MatchQuestionMeta> implements MatchQuestionMetaService {

    public MatchQuestionMetaServiceImpl() {
        super(MatchQuestionMeta.class);
    }

    @Autowired
    private OldPaperService oldPaperService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public int persistenceByPaperId(int matchPaperId) {
        Paper paper = oldPaperService.findPaperById(matchPaperId);
        List<Integer> questions = paper.getQuestions();
        int size = 0;
        if (CollectionUtils.isEmpty(questions)) {
            return size;
        }
        for (Integer question : questions) {
            size += persistenceByQuestion(question, matchPaperId);
        }
        return size;
    }

    /**
     * 将试题的信息持久化到mysql中
     *
     * @param question
     * @param matchPaperId
     * @return
     */
    private int persistenceByQuestion(int question, int matchPaperId) {
        boolean flag = false;
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try {
            String questionMetaKey = getQuestionMetaKey(question);
            Map<byte[], byte[]> map = connection.hGetAll(questionMetaKey.getBytes());
            Map<String, String> collect = map.entrySet().stream().collect(Collectors.toMap(entry -> new String(entry.getKey()), entry -> new String(entry.getValue())));
            MatchQuestionMeta matchQuestionMeta = new MatchQuestionMeta();
            matchQuestionMeta.setMatchId(matchPaperId);
            matchQuestionMeta.setQuestionId(question);
            matchQuestionMeta.setDetail(JsonUtil.toJson(collect));
        } finally {
            connection.close();
        }
        return flag ? 1 : 0;
    }

    /**
     * 查询question meta2 redis key
     *
     * @param qid
     * @return
     */
    public static final String getQuestionMetaKey(int qid) {
        return new StringBuilder("qmeta_").append(qid).toString();
    }
}
