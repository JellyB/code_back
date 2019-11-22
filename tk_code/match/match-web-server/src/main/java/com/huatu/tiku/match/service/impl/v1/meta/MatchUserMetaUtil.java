package com.huatu.tiku.match.service.impl.v1.meta;

import com.google.common.collect.Maps;
import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/3/6.
 */
public class MatchUserMetaUtil {
    public static final String IS_OTHER = "1";      //缓存为空
    public static final String IS_NOT_OTHER = "0";  //缓存有值
    /**
     * match
     *
     * @param matchUserMeta
     * @return
     */
    public static Map<String, Object> convertEnrollObject2Map(MatchUserMeta matchUserMeta) {
        Map<String, Object> enrollMap = Maps.newHashMap();
        enrollMap.put(MetaAttrEnum.POSITION_ID.getKey(), matchUserMeta.getPositionId());
        enrollMap.put(MetaAttrEnum.POSITION_NAME.getKey(), matchUserMeta.getPositionName());
        enrollMap.put(MetaAttrEnum.SCHOOL_ID.getKey(), matchUserMeta.getSchoolId());
        enrollMap.put(MetaAttrEnum.SCHOOL_NAME.getKey(), matchUserMeta.getSchoolName());
        enrollMap.put(MetaAttrEnum.ENROLL_TIME.getKey(), matchUserMeta.getEnrollTime().getTime());
        enrollMap.put(MetaAttrEnum.PRACTICE_ID.getKey(), matchUserMeta.getPracticeId());
        enrollMap.put(MetaAttrEnum.IS_ANSWER.getKey(), matchUserMeta.getIsAnswer());
        enrollMap.put(MetaAttrEnum.SUBMIT_TYPE.getKey(), matchUserMeta.getSubmitType());
        enrollMap.put(MetaAttrEnum.SCORE.getKey(), matchUserMeta.getScore());
        if (null != matchUserMeta.getRank()) {
            enrollMap.put(MetaAttrEnum.RANK.getKey(), matchUserMeta.getRank());
        }
        if (null != matchUserMeta.getRankCount()) {
            enrollMap.put(MetaAttrEnum.RANK_COUNT.getKey(), matchUserMeta.getRankCount());
        }
        if (null != matchUserMeta.getRankForPosition()) {
            enrollMap.put(MetaAttrEnum.RANK_POSITION.getKey(), matchUserMeta.getRankForPosition());
        }
        if (null != matchUserMeta.getRankCountForPosition()) {
            enrollMap.put(MetaAttrEnum.RANK_POSITION_COUNT.getKey(), matchUserMeta.getRankCountForPosition());
        }
        if (null != matchUserMeta.getMaxScore()) {
            enrollMap.put(MetaAttrEnum.MAX_SCORE.getKey(), matchUserMeta.getMaxScore());
        }
        if (null != matchUserMeta.getAverage()) {
            enrollMap.put(MetaAttrEnum.AVERAGE.getKey(), matchUserMeta.getAverage());
        }
        if (null != matchUserMeta.getCardCreateTime()) {
            enrollMap.put(MetaAttrEnum.CARD_CREATE_TIME.getKey(), matchUserMeta.getCardCreateTime().getTime());
        }
        if (null != matchUserMeta.getSubmitTime()) {
            enrollMap.put(MetaAttrEnum.SUBMIT_TIME.getKey(), matchUserMeta.getSubmitTime().getTime());
        }
        return enrollMap;

    }

    public static MatchUserMeta convertEnrollMap2Object(Map<String, Object> enrollMap) {
        List<MetaAttrEnum> metaAttrEnums = enrollMap.keySet().stream().map(MetaAttrEnum::create).collect(Collectors.toList());
        MatchUserMeta matchUserMeta = new MatchUserMeta();
        for (MetaAttrEnum metaAttrEnum : metaAttrEnums) {
            switch (metaAttrEnum) {
                case POSITION_ID:
                    matchUserMeta.setPositionId(MapUtils.getInteger(enrollMap, metaAttrEnum.getKey()));
                    break;
                case POSITION_NAME:
                    matchUserMeta.setPositionName(MapUtils.getString(enrollMap, metaAttrEnum.getKey()));
                    break;
                case SCHOOL_ID:
                    matchUserMeta.setSchoolId(MapUtils.getInteger(enrollMap, metaAttrEnum.getKey(), -1));
                    break;
                case SCHOOL_NAME:
                    matchUserMeta.setSchoolName(MapUtils.getString(enrollMap, metaAttrEnum.getKey(), ""));
                    break;
                case ENROLL_TIME:
                    matchUserMeta.setEnrollTime(new Timestamp(MapUtils.getLong(enrollMap, metaAttrEnum.getKey())));
                    break;
                case PRACTICE_ID:
                    matchUserMeta.setPracticeId(MapUtils.getLong(enrollMap, metaAttrEnum.getKey(), -1L));
                    break;
                case IS_ANSWER:
                    matchUserMeta.setIsAnswer(MapUtils.getInteger(enrollMap, metaAttrEnum.getKey(), MatchInfoEnum.AnswerStatus.NO_JOIN.getKey()));
                    break;
                case SUBMIT_TYPE:
                    matchUserMeta.setSubmitType(MapUtils.getInteger(enrollMap, metaAttrEnum.getKey(), MatchInfoEnum.SubmitTypeEnum.NO_SUBMIT.getKey()));
                    break;
                case SCORE:
                    matchUserMeta.setScore(MapUtils.getDouble(enrollMap, metaAttrEnum.getKey(), 0D));
                    break;
                case CARD_CREATE_TIME:
                    matchUserMeta.setCardCreateTime(new Timestamp(MapUtils.getLong(enrollMap, metaAttrEnum.getKey())));
                    break;
                case SUBMIT_TIME:
                    matchUserMeta.setSubmitTime(new Timestamp(MapUtils.getLong(enrollMap, metaAttrEnum.getKey())));
                    break;
                case RANK:
                    matchUserMeta.setRank(MapUtils.getInteger(enrollMap, metaAttrEnum.getKey()));
                    break;
                case RANK_COUNT:
                    matchUserMeta.setRankCount(MapUtils.getInteger(enrollMap, metaAttrEnum.getKey()));
                    break;
                case AVERAGE:
                    matchUserMeta.setAverage(MapUtils.getDouble(enrollMap, metaAttrEnum.getKey()));
                    break;
                case MAX_SCORE:
                    matchUserMeta.setMaxScore(MapUtils.getDouble(enrollMap, metaAttrEnum.getKey()));
                    break;
                case RANK_POSITION:
                    matchUserMeta.setRankForPosition(MapUtils.getInteger(enrollMap, metaAttrEnum.getKey()));
                    break;
                case RANK_POSITION_COUNT:
                    matchUserMeta.setRankCountForPosition(MapUtils.getInteger(enrollMap, metaAttrEnum.getKey()));
                    break;
                default:    break;
            }
        }
        return matchUserMeta;
    }

    /**
     * HASH缓存中的fields
     */
    @Getter
    @AllArgsConstructor
    public enum MetaAttrEnum {
        POSITION_ID("positionId"),
        POSITION_NAME("positionName"),
        SCHOOL_ID("schoolId"),
        SCHOOL_NAME("schoolName"),
        ENROLL_TIME("enrollTime"),
        PRACTICE_ID("practiceId"),
        CARD_CREATE_TIME("cardCreateTime"),
        IS_ANSWER("isAnswer"),
        SUBMIT_TIME("submitTime"),
        SUBMIT_TYPE("submitType"),
        SCORE("score"),
        RANK("rank"),
        RANK_POSITION("rankForPosition"),
        MAX_SCORE("maxScore"),
        AVERAGE("average"),
        RANK_COUNT("rankCount"),
        RANK_POSITION_COUNT("rankCountForPosition"),
        OTHER("other");     //1标识其他字段为空，0标识其他缓存有值

        public String getKey() {
            return this.key;
        }

        private String key;

        public static MetaAttrEnum create(String id) {
            for (MetaAttrEnum metaAttrEnum : MetaAttrEnum.values()) {
                if (metaAttrEnum.getKey().equals(id)) {
                    return metaAttrEnum;
                }
            }
            return MetaAttrEnum.OTHER;
        }
    }


}
