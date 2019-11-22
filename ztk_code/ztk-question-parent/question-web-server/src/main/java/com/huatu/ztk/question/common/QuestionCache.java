package com.huatu.ztk.question.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huatu.ztk.question.bean.*;
import com.huatu.ztk.question.protobuf.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Question 缓存,采用rocksdb存储
 * Created by shaojieyue
 * Created time 2016-04-24 21:52
 */
public class QuestionCache {
    private static final Logger logger = LoggerFactory.getLogger(QuestionCache.class);
    private static final byte QUESTION_TYPE_SINGLE = 1;//普通题
    private static final byte QUESTION_TYPE_MULTIPLE = 2;//复合题
    private static final byte QUESTION_TYPE_SINGLE_SUBJECTIVE = 3;//单一主观题
    private static final byte QUESTION_TYPE_MULTIPLE_SUBJECTIVE = 4;//复合主观题
    static RocksDB db = null;
    public static final String PATH_TO_DB = "/data/rocksdb/question.db";
//    public static final String PATH_TO_DB = "E:\\data\\rocksdb\\question.db";

    static {
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        Options options = new Options()
                .setCreateIfMissing(true)
                .optimizeForPointLookup(2048);

        try {
            // a factory method that returns a RocksDB instance
            db = RocksDB.open(options, PATH_TO_DB);
            // do something
        } catch (RocksDBException e) {
            throw new RuntimeException("open rocksdb fail.", e);
        }
    }

    /**
     * 将试题缓存从rocksdb中移除
     *
     * @param questionId 试题id
     */
    public static final void remove(int questionId) {
        try {
            db.remove(Ints.toByteArray(questionId));
            logger.info("remove question from rocksdb success. questionId={}", questionId);
        } catch (RocksDBException e) {
            logger.error("remove question from rocksdb fail. questionId={}", questionId);
        }

    }

    /**
     * 批量获取试题
     *
     * @param ids
     * @return
     */
    public static final Map<Integer, Question> multiGet(List<Integer> ids) {
        Map<Integer, Question> questionMap;
        List<byte[]> list = ids.stream().map(id -> Ints.toByteArray(id)).collect(Collectors.toList());
        Map<byte[], byte[]> dataMap = null;
        try {
            dataMap = db.multiGet(list);//批量获取id
        } catch (RocksDBException e) {
            logger.error("multiGet data from rocksdb fail.", e);
        }
        if (dataMap == null) {
            return Maps.newHashMap();
        }

        questionMap = Maps.newHashMap();
        for (Map.Entry<byte[], byte[]> entry : dataMap.entrySet()) {
            questionMap.put(Ints.fromByteArray(entry.getKey()), convert(entry.getValue()));
        }
        return questionMap;
    }

    /**
     * 获取单个试题
     *
     * @param id
     * @return
     */
    public static final Question get(int id) {
        byte[] bytes = null;
        try {
            bytes = db.get(Ints.toByteArray(id));
        } catch (RocksDBException e) {
            logger.error("get data from rocksdb fail. qid={}", id, e);
        }
        if (bytes == null) {
            return null;
        }
        return convert(bytes);
    }

    /**
     * 把对象存入rocksdb
     *
     * @param question
     */
    public static final void put(Question question) {
        final byte[] idBytes = Ints.toByteArray(question.getId());
        byte[] data = null;
        byte type = -1;
        try {
            if (question instanceof GenericQuestion) {//普通试题
                data = toProtobufGenericQuestion((GenericQuestion) question).toByteArray();
                type = QUESTION_TYPE_SINGLE;
            } else if (question instanceof CompositeQuestion) {//复合题
                CompositeQuestionProtos.CompositeQuestion compositeQuestion = serializeCompositeQuestion((CompositeQuestion) question);
                if (compositeQuestion == null) {
                    return;
                }
                data = compositeQuestion.toByteArray();
                type = QUESTION_TYPE_MULTIPLE;
            } else if (question instanceof GenericSubjectiveQuestion) {//单一主观题
                data = toProtobufGenericSubjectiveQuestion((GenericSubjectiveQuestion) question).toByteArray();
                type = QUESTION_TYPE_SINGLE_SUBJECTIVE;
            } else if (question instanceof CompositeSubjectiveQuestion) {//复合主观题
                CompositeSubjectiveQuestionProtos.CompositeSubjectiveQuestion compositeSubjectiveQuestion =
                        serializeCompositeSubjectiveQuestion((CompositeSubjectiveQuestion) question);
                if (compositeSubjectiveQuestion == null) {
                    return;
                }
                data = compositeSubjectiveQuestion.toByteArray();
                type = QUESTION_TYPE_MULTIPLE_SUBJECTIVE;
            } else {
                throw new RuntimeException("unknown object");
            }
        } catch (Exception e) {
            /**
             * 部分试题转换字节流失败(捕捉错误，不在rocksdb上保存，直接跳出）---原因之前有些字段被限定为必输字段，但是业务上可能不用这些字段
             * protobuf必输字段改为非必输字段，会导致原有的字节流转化报错，所以放弃对无必输字段的试题的rocksdB存储
             */
            logger.error("rocksDB put parse error，id={}",question.getId());
            if(question instanceof GenericSubjectiveQuestion){
                logger.error("question is GenericSubjectiveQuestion,字段解析失败");
            }else if(question instanceof CompositeSubjectiveQuestion){
                logger.error("question is CompositeSubjectiveQuestion，字段解析失败");
            }else{
                e.printStackTrace();
            }
            return;
        }
        //合并数据 data=对象字节+对象类型
        data = Bytes.concat(data, new byte[]{type});
        try {
            db.put(idBytes, data);
        } catch (RocksDBException e) {
            logger.error("put GenericQuestion to rocksdb fail. qid={}", question.getId(), e);
        }
    }

    /**
     * 将复合题对象转化为protobuf对象
     *
     * @param question
     * @return
     */
    private static CompositeQuestionProtos.CompositeQuestion serializeCompositeQuestion(CompositeQuestion question) {

        List<Question> cids = question.getChildrens();

        if (CollectionUtils.isEmpty(cids)) {
            logger.warn("empty children obj,id={}", question.getId());
            return null;
        }


        List<GenericQuestionProtos.GenericQuestion> childObjList = cids.stream()
                .map(i -> toProtobufGenericQuestion((GenericQuestion) i))
                .collect(Collectors.toList());

        final CompositeQuestionProtos.CompositeQuestion compositeQuestion = CompositeQuestionProtos.CompositeQuestion.newBuilder()
                .setId(question.getId())
                .setArea(question.getArea())
                .setType(question.getType())
                .setFrom(StringUtils.trimToEmpty(question.getFrom()))
                .setYear(question.getYear())
                .setMaterial(question.getMaterial())
                .setStatus(question.getStatus())
                .setSubject(question.getSubject())
                .setMode(question.getMode())
                .setCreateBy(question.getCreateBy())
                .setCreateTime(question.getCreateTime())
                .addAllQuestions(question.getQuestions())
                .setTeachType(StringUtils.trimToEmpty(question.getTeachType()))
                .setDifficult(question.getDifficult())
                .addAllChildrens(childObjList)
                .setScore(question.getScore())
                .build();
        return compositeQuestion;
    }


    /**
     * 将复合主观题对象转化为protobuf对象
     *
     * @param question
     * @return
     */
    private static CompositeSubjectiveQuestionProtos.CompositeSubjectiveQuestion serializeCompositeSubjectiveQuestion(CompositeSubjectiveQuestion question) {
        List<Question> cids = question.getChildrens();
        if (CollectionUtils.isEmpty(cids)) {
            logger.warn("empty children obj,id={}", question.getId());
            return null;
        }

        List<CommonQuestionProtos.CommonQuestion> childObjList = cids.stream()
                .map(i -> toProtobufCommonQuestion(i))
                .collect(Collectors.toList());

        final CompositeSubjectiveQuestionProtos.CompositeSubjectiveQuestion compositeQuestion = CompositeSubjectiveQuestionProtos.CompositeSubjectiveQuestion.newBuilder()
                .setId(question.getId())
                .setArea(question.getArea())
                .setType(question.getType())
                .setFrom(StringUtils.trimToEmpty(question.getFrom()))
                .setYear(question.getYear())
                .addAllMaterials(CollectionUtils.isNotEmpty(question.getMaterials()) ? Lists.newArrayList(question.getMaterials()) : Lists.newArrayList())
                .setStatus(question.getStatus())
                .setSubject(question.getSubject())
                .setMode(question.getMode())
                .setCreateBy(question.getCreateBy())
                .setCreateTime(question.getCreateTime())
                .addAllQuestions(question.getQuestions())
                .setRequire(question.getRequire())
                .setTeachType(StringUtils.trimToEmpty(question.getTeachType()))
                .setDifficult(question.getDifficult())
                .addAllChildrens(childObjList)
                .setScore(question.getScore())
                .build();
        return compositeQuestion;
    }


    /**
     * 将试题对象转化为protobuf对象
     *
     * @param question
     * @return
     */
    private static GenericQuestionProtos.GenericQuestion toProtobufGenericQuestion(GenericQuestion question) {
        final GenericQuestionProtos.GenericQuestion.Builder builder = GenericQuestionProtos.GenericQuestion.newBuilder();
        try {
            builder.setAnswer(question.getAnswer())
                    .setFrom(StringUtils.trimToEmpty(question.getFrom()))
                    .setStatus(question.getStatus())
                    .setType(question.getType())
                    .setStem(question.getStem())
                    .setAnalysis(StringUtils.trimToEmpty(question.getAnalysis()))
                    .addAllChoices(question.getChoices())
                    .setDifficult(question.getDifficult())
                    .setArea(question.getArea())
                    .setId(question.getId())
                    .setYear(question.getYear())
                    .addAllPoints(question.getPoints())
                    .setSubject(question.getSubject())
                    .setMode(question.getMode())
                    .addAllPointsName(question.getPointsName())
                    .setParent(question.getParent())
                    .setCreateTime(question.getCreateTime())
                    .setCreateBy(question.getCreateBy())
                    .setMaterial(StringUtils.trimToEmpty(question.getMaterial()))
                    .addAllMaterials(CollectionUtils.isNotEmpty(question.getMaterials()) ? Lists.newArrayList(question.getMaterials()) : Lists.newArrayList())
                    .setTeachType(StringUtils.trimToEmpty(question.getTeachType()))
                    .setScore(question.getScore())
                    .setRecommendedTime(question.getRecommendedTime())
                    .setExtend(StringUtils.trimToEmpty(question.getExtend()));
            List<KnowledgeInfo> pointList = question.getPointList();
            if (CollectionUtils.isNotEmpty(pointList)) {
                List<KnowledgeProtos.KnowledgeProto> knowledgeProtos = pointList.stream().map(i -> toProtobufKnowledge(i)).collect(Collectors.toList());
                builder.addAllPointList(knowledgeProtos);
            }
        } catch (Exception e) {
            logger.error("试题结构有问题：{}", question.getId());
            logger.error("question：{}", question);
            throw e;
        }
        return builder.build();
    }


    /**
     * 单一主观题
     *
     * @param question
     * @return
     */
    private static GenericSubjectiveQuestionProtos.GenericSubjectiveQuestion toProtobufGenericSubjectiveQuestion(GenericSubjectiveQuestion question) {
        final GenericSubjectiveQuestionProtos.GenericSubjectiveQuestion.Builder builder = GenericSubjectiveQuestionProtos.GenericSubjectiveQuestion.newBuilder();
        builder.setRequire(question.getRequire())
                .setScoreExplain(question.getScoreExplain())
                .setReferAnalysis(question.getReferAnalysis())
                .setAnswerRequire(question.getAnswerRequire())
                .setExamPoint(question.getExamPoint())
                .setSolvingIdea(question.getSolvingIdea())
                .addAllMaterials(CollectionUtils.isNotEmpty(question.getMaterials()) ? Lists.newArrayList(question.getMaterials()) : Lists.newArrayList())
                .setFrom(StringUtils.trimToEmpty(question.getFrom()))
                .setStatus(question.getStatus())
                .setType(question.getType())
                .setStem(question.getStem())
                .setDifficult(question.getDifficult())
                .setArea(question.getArea())
                .setId(question.getId())
                .setYear(question.getYear())
                .setSubject(question.getSubject())
                .setMode(question.getMode())
                .setParent(question.getParent())
                .setCreateTime(question.getCreateTime())
                .setCreateBy(question.getCreateBy())
                .setTeachType(StringUtils.trimToEmpty(question.getTeachType()))
                .setMaxWordCount(question.getMaxWordCount())
                .setMinWordCount(question.getMinWordCount())
                .setScore(question.getScore())
                .setExtend(question.getExtend());
        List<KnowledgeInfo> pointList = question.getPointList();
        if (CollectionUtils.isNotEmpty(pointList)) {
            List<KnowledgeProtos.KnowledgeProto> knowledgeProtos = pointList.stream().map(i -> toProtobufKnowledge(i)).collect(Collectors.toList());
            builder.addAllPointList(knowledgeProtos);
        }
        return builder.build();
    }


    /**
     * 将试题对象转化为protobuf对象
     *
     * @param obj
     * @return
     */
    private static CommonQuestionProtos.CommonQuestion toProtobufCommonQuestion(Question obj) {

        if (obj instanceof GenericQuestion) {
            GenericQuestion question = (GenericQuestion) obj;
            final CommonQuestionProtos.CommonQuestion.Builder builder = CommonQuestionProtos.CommonQuestion.newBuilder();
            builder.setAnswer(question.getAnswer())
                    .setFrom(StringUtils.trimToEmpty(question.getFrom()))
                    .setStatus(question.getStatus())
                    .setType(question.getType())
                    .setStem(question.getStem())
                    .setAnalysis(question.getAnalysis())
                    .addAllChoices(question.getChoices())
                    .setDifficult(question.getDifficult())
                    .setArea(question.getArea())
                    .setId(question.getId())
                    .setYear(question.getYear())
                    .addAllPoints(question.getPoints())
                    .setSubject(question.getSubject())
                    .setMode(question.getMode())
                    .addAllPointsName(question.getPointsName())
                    .setParent(question.getParent())
                    .setCreateTime(question.getCreateTime())
                    .setCreateBy(question.getCreateBy())
                    .setMaterial(StringUtils.trimToEmpty(question.getMaterial()))
                    .addAllMaterials(CollectionUtils.isNotEmpty(question.getMaterials()) ? Lists.newArrayList(question.getMaterials()) : Lists.newArrayList())
                    .setTeachType(StringUtils.trimToEmpty(question.getTeachType()))
                    .setScore(question.getScore())
                    .setRecommendedTime(question.getRecommendedTime());
            List<KnowledgeInfo> pointList = question.getPointList();
            if (CollectionUtils.isNotEmpty(pointList)) {
                List<KnowledgeProtos.KnowledgeProto> collect = pointList.stream().map(i -> toProtobufKnowledge(i)).collect(Collectors.toList());
                builder.addAllPointList(collect);
            }
            return builder.build();
        } else {
            GenericSubjectiveQuestion question = (GenericSubjectiveQuestion) obj;
            final CommonQuestionProtos.CommonQuestion.Builder builder = CommonQuestionProtos.CommonQuestion.newBuilder();
            builder.setRequire(question.getRequire())
                    .setScoreExplain(question.getScoreExplain())
                    .setReferAnalysis(question.getReferAnalysis())
                    .setAnswerRequire(question.getAnswerRequire())
                    .setExamPoint(question.getExamPoint())
                    .setSolvingIdea(question.getSolvingIdea())
                    .addAllMaterials(CollectionUtils.isNotEmpty(question.getMaterials()) ? Lists.newArrayList(question.getMaterials()) : Lists.newArrayList())
                    .setFrom(StringUtils.trimToEmpty(question.getFrom()))
                    .setStatus(question.getStatus())
                    .setType(question.getType())
                    .setStem(question.getStem())
                    .setDifficult(question.getDifficult())
                    .setArea(question.getArea())
                    .setId(question.getId())
                    .setYear(question.getYear())
                    .setSubject(question.getSubject())
                    .setMode(question.getMode())
                    .setParent(question.getParent())
                    .setCreateTime(question.getCreateTime())
                    .setCreateBy(question.getCreateBy())
                    .setTeachType(StringUtils.trimToEmpty(question.getTeachType()))
                    .setMaxWordCount(question.getMaxWordCount())
                    .setMinWordCount(question.getMinWordCount())
                    .setScore(question.getScore());
            List<KnowledgeInfo> pointList = question.getPointList();
            if (CollectionUtils.isNotEmpty(pointList)) {
                List<KnowledgeProtos.KnowledgeProto> collect = pointList.stream().map(i -> toProtobufKnowledge(i)).collect(Collectors.toList());
                builder.addAllPointList(collect);
            }
            return builder.build();
        }
    }

    /**
     * 将试题多级知识点对象转化为protobuf对象
     *
     * @param obj
     * @return
     */
    private static KnowledgeProtos.KnowledgeProto toProtobufKnowledge(KnowledgeInfo obj) {
        KnowledgeProtos.KnowledgeProto.Builder builder = KnowledgeProtos.KnowledgeProto.newBuilder()
                .addAllPoints(obj.getPoints())
                .addAllPointsName(obj.getPointsName());
        return builder.build();
    }

    /**
     * 将protobuf question对象转换为普通的GenericQuestion
     *
     * @param bytes
     * @return
     */
    private static final Question convert(byte[] bytes) {
        Question question = null;
        int type = bytes[bytes.length - 1];//存储的对象类型
        //对象的序列化数据
        final byte[] objBytes = Arrays.copyOf(bytes, bytes.length - 1);
        bytes = null;//提前释放对象,让jvm尽快回收
        if (type == QUESTION_TYPE_SINGLE) {//普通试题
            question = convertGenericQuestion(objBytes);
        } else if (type == QUESTION_TYPE_MULTIPLE) {//复合题
            question = convertCompositeQuestion(objBytes);
        } else if (type == QUESTION_TYPE_SINGLE_SUBJECTIVE) {
            question = convertGenericSubjectiveQuestion(objBytes);
        } else if (type == QUESTION_TYPE_MULTIPLE_SUBJECTIVE) {
            question = convertCompositeSubjectiveQuestion(objBytes);
        }
        return question;
    }

    /**
     * protobuf 对象转换为 普通bean
     *
     * @param objBytes
     * @return
     */
    private static Question convertGenericQuestion(byte[] objBytes) {
        GenericQuestionProtos.GenericQuestion genericQuestion = null;
        try {
            genericQuestion = GenericQuestionProtos.GenericQuestion.parseFrom(objBytes);
        } catch (InvalidProtocolBufferException e) {
            logger.error("serialize bytes to GenericQuestion fail.", e);
        }
        if (genericQuestion == null) {
            return null;
        }

        GenericQuestion question = getGenericQuestion(genericQuestion);
        return question;
    }

    private static GenericQuestion getGenericQuestion(GenericQuestionProtos.GenericQuestion genericQuestion) {
        GenericQuestion question = new GenericQuestion();
        question.setAnswer(genericQuestion.getAnswer());
        question.setFrom(genericQuestion.getFrom());
        question.setStatus(genericQuestion.getStatus());
        question.setType(genericQuestion.getType());
        question.setStem(genericQuestion.getStem());
        question.setAnalysis(genericQuestion.getAnalysis());
        //必须新建ArrayList,用于kryo的序列化
        question.setChoices(Lists.newArrayList(genericQuestion.getChoicesList()));
        question.setDifficult(genericQuestion.getDifficult());
        question.setArea(genericQuestion.getArea());
        question.setId(genericQuestion.getId());
        question.setPoints(genericQuestion.getPointsList());
        question.setYear(genericQuestion.getYear());
        question.setMode(genericQuestion.getMode());
        question.setSubject(genericQuestion.getSubject());
        question.setMaterial(StringUtils.trimToNull(genericQuestion.getMaterial()));
        question.setPointsName(Lists.newArrayList(genericQuestion.getPointsNameList()));
        question.setCreateBy(genericQuestion.getCreateBy());
        question.setCreateTime(genericQuestion.getCreateTime());
        question.setParent(genericQuestion.getParent());
        question.setMaterials(Lists.newArrayList(genericQuestion.getMaterialsList()));
        question.setTeachType(StringUtils.trimToEmpty(genericQuestion.getTeachType()));
        question.setScore(genericQuestion.getScore());
        question.setRecommendedTime(genericQuestion.getRecommendedTime());
        question.setExtend(StringUtils.trimToEmpty(genericQuestion.getExtend()));
        if (CollectionUtils.isNotEmpty(genericQuestion.getPointListList())) {
            question.setPointList(genericQuestion.getPointListList().stream().map(i -> getKnowledgeInfo(i)).collect(Collectors.toList()));
        }
        return question;
    }

    /**
     * 转换复合题对象
     *
     * @param objBytes
     * @return
     */
    private static Question convertCompositeQuestion(byte[] objBytes) {
        CompositeQuestionProtos.CompositeQuestion compositeQuestion = null;
        try {
            compositeQuestion = CompositeQuestionProtos.CompositeQuestion.parseFrom(objBytes);
        } catch (InvalidProtocolBufferException e) {
            logger.error("serialize bytes to GenericQuestion fail.", e);
        }

        if (compositeQuestion == null) {
            return null;
        }
        CompositeQuestion question = new CompositeQuestion();
        question.setId(compositeQuestion.getId());
        question.setType(compositeQuestion.getType());
        question.setArea(compositeQuestion.getArea());
        question.setMaterial(compositeQuestion.getMaterial());
        question.setQuestions(compositeQuestion.getQuestionsList());
        question.setFrom(compositeQuestion.getFrom());
        question.setYear(compositeQuestion.getYear());
        question.setStatus(compositeQuestion.getStatus());
        question.setSubject(compositeQuestion.getSubject());
        question.setCreateTime(compositeQuestion.getCreateTime());
        question.setCreateBy(compositeQuestion.getCreateBy());
        question.setMode(compositeQuestion.getMode());
        question.setTeachType(StringUtils.trimToEmpty(compositeQuestion.getTeachType()));
        question.setDifficult(compositeQuestion.getDifficult());
        question.setScore(compositeQuestion.getScore());


        List<Question> childrens = compositeQuestion.getChildrensList().stream()
                .map(child -> getGenericQuestion(child))
                .collect(Collectors.toList());
        question.setChildrens(childrens);

        return question;
    }


    /**
     * 单一主观题
     * protobuf 对象转换为 普通bean
     *
     * @param objBytes
     * @return
     */
    private static Question convertGenericSubjectiveQuestion(byte[] objBytes) {
        GenericSubjectiveQuestionProtos.GenericSubjectiveQuestion savedQuestion = null;
        try {
            savedQuestion = GenericSubjectiveQuestionProtos.GenericSubjectiveQuestion.parseFrom(objBytes);
        } catch (InvalidProtocolBufferException e) {
            logger.error("serialize bytes to GenericQuestion fail.", e);
        }
        if (savedQuestion == null) {
            return null;
        }

        GenericSubjectiveQuestion question = new GenericSubjectiveQuestion();

        question.setId(savedQuestion.getId());
        question.setFrom(savedQuestion.getFrom());
        question.setStatus(savedQuestion.getStatus());
        question.setType(savedQuestion.getType());
        question.setStem(savedQuestion.getStem());
        question.setDifficult(savedQuestion.getDifficult());
        question.setArea(savedQuestion.getArea());
        question.setYear(savedQuestion.getYear());
        question.setMode(savedQuestion.getMode());
        question.setSubject(savedQuestion.getSubject());
        question.setCreateBy(savedQuestion.getCreateBy());
        question.setCreateTime(savedQuestion.getCreateTime());
        question.setParent(savedQuestion.getParent());

        question.setRequire(savedQuestion.getRequire());
        question.setScoreExplain(savedQuestion.getScoreExplain());
        question.setReferAnalysis(savedQuestion.getReferAnalysis());
        question.setAnswerRequire(savedQuestion.getAnswerRequire());
        question.setExamPoint(savedQuestion.getExamPoint());
        question.setSolvingIdea(savedQuestion.getSolvingIdea());
        question.setMaterials(Lists.newArrayList(savedQuestion.getMaterialsList()));

        question.setTeachType(StringUtils.trimToEmpty(savedQuestion.getTeachType()));
        question.setMaxWordCount(savedQuestion.getMaxWordCount());
        question.setMinWordCount(savedQuestion.getMinWordCount());
        question.setScore(savedQuestion.getScore());
        question.setExtend(StringUtils.trimToNull(savedQuestion.getExtend()));
        if (CollectionUtils.isNotEmpty(savedQuestion.getPointListList())) {
            question.setPointList(savedQuestion.getPointListList().stream().map(i -> getKnowledgeInfo(i)).collect(Collectors.toList()));
        }
        return question;
    }


    /**
     * 转换复合主观题对象
     *
     * @param objBytes
     * @return
     */
    private static Question convertCompositeSubjectiveQuestion(byte[] objBytes) {
        CompositeSubjectiveQuestionProtos.CompositeSubjectiveQuestion savedQuestion = null;
        try {
            savedQuestion = CompositeSubjectiveQuestionProtos.CompositeSubjectiveQuestion.parseFrom(objBytes);
        } catch (InvalidProtocolBufferException e) {
            logger.error("serialize bytes to GenericQuestion fail.", e);
        }

        if (savedQuestion == null) {
            return null;
        }
        CompositeSubjectiveQuestion question = new CompositeSubjectiveQuestion();
        question.setId(savedQuestion.getId());
        question.setType(savedQuestion.getType());
        question.setArea(savedQuestion.getArea());
        question.setMaterials(Lists.newArrayList(savedQuestion.getMaterialsList()));
        question.setQuestions(savedQuestion.getQuestionsList());
        question.setFrom(savedQuestion.getFrom());
        question.setYear(savedQuestion.getYear());
        question.setStatus(savedQuestion.getStatus());
        question.setSubject(savedQuestion.getSubject());
        question.setCreateTime(savedQuestion.getCreateTime());
        question.setCreateBy(savedQuestion.getCreateBy());
        question.setMode(savedQuestion.getMode());
        question.setRequire(savedQuestion.getRequire());

        question.setTeachType(StringUtils.trimToEmpty(savedQuestion.getTeachType()));
        question.setDifficult(savedQuestion.getDifficult());
        question.setScore(savedQuestion.getScore());

        List<Question> childrens = savedQuestion.getChildrensList().stream()
                .map(child -> getCommonQuestion(child))
                .collect(Collectors.toList());
        question.setChildrens(childrens);

        return question;
    }


    /**
     * 缓存对象转换为多知识点对象
     *
     * @param knowledgeProto
     * @return
     */
    private static KnowledgeInfo getKnowledgeInfo(KnowledgeProtos.KnowledgeProto knowledgeProto) {
        if (knowledgeProto == null) {
            return null;
        }

        return KnowledgeInfo.builder().points(knowledgeProto.getPointsList())
                .pointsName(knowledgeProto.getPointsNameList())
                .build();

    }

    /**
     * 将缓存对象转换为Question
     *
     * @param commonQuestion
     * @return
     */
    private static Question getCommonQuestion(CommonQuestionProtos.CommonQuestion commonQuestion) {

        if (commonQuestion.getType() == QuestionType.SINGLE_SUBJECTIVE) {
            CommonQuestionProtos.CommonQuestion savedQuestion = commonQuestion;

            GenericSubjectiveQuestion question = new GenericSubjectiveQuestion();

            question.setId(savedQuestion.getId());
            question.setFrom(savedQuestion.getFrom());
            question.setStatus(savedQuestion.getStatus());
            question.setType(savedQuestion.getType());
            question.setStem(savedQuestion.getStem());
            question.setDifficult(savedQuestion.getDifficult());
            question.setArea(savedQuestion.getArea());
            question.setYear(savedQuestion.getYear());
            question.setMode(savedQuestion.getMode());
            question.setSubject(savedQuestion.getSubject());
            question.setCreateBy(savedQuestion.getCreateBy());
            question.setCreateTime(savedQuestion.getCreateTime());
            question.setParent(savedQuestion.getParent());

            question.setRequire(savedQuestion.getRequire());
            question.setScoreExplain(savedQuestion.getScoreExplain());
            question.setReferAnalysis(savedQuestion.getReferAnalysis());
            question.setAnswerRequire(savedQuestion.getAnswerRequire());
            question.setExamPoint(savedQuestion.getExamPoint());
            question.setSolvingIdea(savedQuestion.getSolvingIdea());
            question.setMaterials(Lists.newArrayList(savedQuestion.getMaterialsList()));

            question.setTeachType(StringUtils.trimToEmpty(savedQuestion.getTeachType()));
            question.setMaxWordCount(savedQuestion.getMaxWordCount());
            question.setMinWordCount(savedQuestion.getMinWordCount());
            question.setScore(savedQuestion.getScore());
            question.setExtend(StringUtils.trimToNull(savedQuestion.getExtend()));
            if (CollectionUtils.isNotEmpty(savedQuestion.getPointListList())) {
                question.setPointList(savedQuestion.getPointListList().stream().map(i -> getKnowledgeInfo(i)).collect(Collectors.toList()));
            }
            return question;

        } else {
            CommonQuestionProtos.CommonQuestion genericQuestion = commonQuestion;

            GenericQuestion question = new GenericQuestion();
            question.setAnswer(genericQuestion.getAnswer());
            question.setFrom(genericQuestion.getFrom());
            question.setStatus(genericQuestion.getStatus());
            question.setType(genericQuestion.getType());
            question.setStem(genericQuestion.getStem());
            question.setAnalysis(genericQuestion.getAnalysis());
            //必须新建ArrayList,用于kryo的序列化
            question.setChoices(Lists.newArrayList(genericQuestion.getChoicesList()));
            question.setDifficult(genericQuestion.getDifficult());
            question.setArea(genericQuestion.getArea());
            question.setId(genericQuestion.getId());
            question.setPoints(genericQuestion.getPointsList());
            question.setYear(genericQuestion.getYear());
            question.setMode(genericQuestion.getMode());
            question.setSubject(genericQuestion.getSubject());
            question.setMaterial(StringUtils.trimToNull(genericQuestion.getMaterial()));
            question.setPointsName(Lists.newArrayList(genericQuestion.getPointsNameList()));
            question.setCreateBy(genericQuestion.getCreateBy());
            question.setCreateTime(genericQuestion.getCreateTime());
            question.setParent(genericQuestion.getParent());
            question.setMaterials(Lists.newArrayList(genericQuestion.getMaterialsList()));
            question.setTeachType(StringUtils.trimToEmpty(genericQuestion.getTeachType()));
            question.setScore(genericQuestion.getScore());
            question.setRecommendedTime(genericQuestion.getRecommendedTime());
            question.setExtend(StringUtils.trimToNull(genericQuestion.getExtend()));
            if (CollectionUtils.isNotEmpty(genericQuestion.getPointListList())) {
                question.setPointList(genericQuestion.getPointListList().stream().map(i -> getKnowledgeInfo(i)).collect(Collectors.toList()));
            }
            return question;
        }
    }
}
