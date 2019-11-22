package com.huatu.tiku.interview.service.impl;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.interview.constant.BaseInfo;
import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.*;
import com.huatu.tiku.interview.entity.template.MyTreeMap;
import com.huatu.tiku.interview.entity.template.TemplateMap;
import com.huatu.tiku.interview.entity.template.WechatTemplateMsg;
import com.huatu.tiku.interview.entity.vo.request.PaperAllInfoVo;
import com.huatu.tiku.interview.repository.*;
import com.huatu.tiku.interview.repository.impl.PaperInfoRepositoryImpl;
import com.huatu.tiku.interview.repository.impl.QuestionAnswerRepositoryImpl;
import com.huatu.tiku.interview.repository.impl.UserRepositoryImpl;
import com.huatu.tiku.interview.service.PaperInfoService;
import com.huatu.tiku.interview.service.WechatTemplateMsgService;
import com.huatu.tiku.interview.util.common.PageUtil;
import com.huatu.tiku.interview.util.json.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by junli on 2018/4/11.
 */
@Service
public class PaperInfoServiceImpl implements PaperInfoService {

    @Autowired
    private PaperInfoRepository paperInfoRepository;

    @Autowired
    private PaperInfoRepositoryImpl paperInfoRepositoryImpl;

    @Autowired
    private QuestionInfoRepository questionInfoRepository;

    @Autowired
    private ChoiceInfoRepository choiceInfoRepository;

    @Autowired
    private QuestionAnswerRepositoryImpl answerRepositoryImpl;

    @Autowired
    private WechatTemplateMsgService templateMsgService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private PaperPushLogRepository paperPushLogRepository;
    @Value("${classInteractionUrl}")
    private String classInteractionUrl;
    @Autowired
    private QuestionAnswerRepository answerRepository;
    @Override
    public PaperInfo findById(long id) {
        return paperInfoRepository.findOne(id);
    }

    @Override
    public void push(final long id) {
        //修改数据状态
        PaperInfo paperInfo = paperInfoRepository.findOne(id);
        if (null == paperInfo || paperInfo.getStatus() != WXStatusEnum.Status.NORMAL.getStatus()) {
            throw new BizException(ErrorResult.create(ResultEnum.ERROR.getCode(), "课堂互动信息不存在"), "课堂互动信息不存在");
        }
        paperInfo.setBizStatus(BaseInfo.PAPER_STATUS.PUSHED.getState());
        paperInfoRepository.save(paperInfo);
        //执行推送消息
        //1.获取当前课堂活动对应的所有用户信息
        List<Map<String, Object>> mapList = userRepository.listForLimit(1, 100, "", paperInfo.getClassId(), -1);
        for (Map user : mapList) {
            String openId = String.valueOf(user.get("openId"));
            WechatTemplateMsg templateMsg = WechatTemplateMsg.builder()
                    .touser(openId)
//                    .template_id("V8aaTIdIh8PPtmxFfWK3uP-4_lwkrX0qDvRhFYGHzAo")//(测试)
                    .template_id("6YHeT8fixF0Qw-n48Zs-7c8Fkk-R4YRmDeYepZ_enPM")//（线上）
                    .url(String.format(classInteractionUrl, id, openId))
                    .data(MyTreeMap.createMap(
                            new TemplateMap("first", WechatTemplateMsg.item("华图在线面试理论课正在进行中~\n", "#000000")),
                            new TemplateMap("keyword1", WechatTemplateMsg.item(paperInfo.getPaperName(), "#000000")),
                            new TemplateMap("keyword2", WechatTemplateMsg.item(user.get("uname") == null ? "无法显示名称" : user.get("uname").toString(), "#000000")),
                            new TemplateMap("remark", WechatTemplateMsg.item("\n请点击详情，回答老师发布的问题哦！", "#000000"))
                    )).build();
            //TODO 此处使用异步发送
            templateMsgService.sendTemplate(JsonUtil.toJson(templateMsg));
        }
    }

    @Transactional
    @Override
    public void save(PaperAllInfoVo paperAllInfoVo) {
        //是否为修改操作
        final boolean isUpdate = (paperAllInfoVo.getId() != 0);
        //辅助方法 把paperVo 转化成PaperInfo
        Function<PaperAllInfoVo, PaperInfo> getPaperEntity = (vo) -> {
            PaperInfo paperInfo = PaperInfo.builder()
                    .paperName(vo.getPaperName())
                    .classId(0)
                    .type(vo.getType())
                    .examType(1)//考试类型(1课堂互动 2全真模考)
                    .build();
            paperInfo.setId(paperAllInfoVo.getId());
            paperInfo.setCreator(paperAllInfoVo.getCreator());
            paperInfo.setModifier(paperAllInfoVo.getModifier());
            paperInfo.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
            paperInfo.setBizStatus(BaseInfo.PAPER_STATUS.UN_PUSHED.getState());
            return paperInfo;
        };

        PaperInfo paperInfo = getPaperEntity.apply(paperAllInfoVo);
        //1.存储paper
        paperInfoRepository.save(paperInfo);
        //2.存储Question
        //2.1 删除原本试题信息
        if (isUpdate) {
            deleteQuestionAndChoiceByPaperId(paperAllInfoVo.getId());
        }
        paperAllInfoVo.getQuestionInfoAllVoList().stream()
                .map(questionInfoAllVo -> {
                    QuestionInfo questionInfo = QuestionInfo.builder()
                            .paperId(paperInfo.getId())
                            .stem(questionInfoAllVo.getStem())
                            .questionType(paperInfo.getType())
                            .build();
                    if (isUpdate) {
                        questionInfo.setModifier(paperAllInfoVo.getModifier());
                    } else {
                        questionInfo.setCreator(paperAllInfoVo.getCreator());
                        //删除题目下面的选项信息
                        choiceInfoRepository.deleteChoiceByQuestionId(questionInfoAllVo.getId(), WXStatusEnum.Status.DELETE.getStatus());
                    }
                    questionInfo.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
                    QuestionInfo save = questionInfoRepository.save(questionInfo);
                    questionInfoAllVo.setId(save.getId());
                    return questionInfoAllVo;

                })
                //3.存储choice 信息
                .forEach(questionInfoAllVo ->
                        questionInfoAllVo.getChoiceInfoList()
                                .forEach(choiceInfo -> {
                                    choiceInfo.setQuestionId(questionInfoAllVo.getId());
                                    if (isUpdate) {
                                        choiceInfo.setModifier(paperAllInfoVo.getModifier());
                                    } else {
                                        choiceInfo.setCreator(paperAllInfoVo.getCreator());
                                    }
                                    choiceInfo.setStatus(WXStatusEnum.Status.NORMAL.getStatus());
                                    choiceInfoRepository.save(choiceInfo);
                                })
                );
    }

    @Transactional
    @Override
    public void delete(long paperId) {
        PaperInfo paperInfo = paperInfoRepository.findOne(paperId);
        if (null == paperInfo) {
            return;
        }
        if (paperInfo.getBizStatus() != BaseInfo.PAPER_STATUS.UN_PUSHED.getState()) {
            throw new BizException(ErrorResult.create(ResultEnum.ERROR.getCode(), "已推送课堂互动信息不能删除"), "已推送课堂互动信息不能删除");
        }
        deleteQuestionAndChoiceByPaperId(paperId);
        //3. 删除考题信息
        paperInfo.setStatus(WXStatusEnum.Status.DELETE.getStatus());
        paperInfoRepository.save(paperInfo);
    }

    @Override
    public PaperAllInfoVo detail(long id) {
        PaperInfo paperInfo = paperInfoRepository.findOne(id);
        if (null == paperInfo) {
            return null;
        }
        //vo 转换
        PaperAllInfoVo allInfoVo = new PaperAllInfoVo();
        allInfoVo.setId(paperInfo.getId());
        allInfoVo.setType(paperInfo.getType());
        allInfoVo.setPaperName(paperInfo.getPaperName());
        allInfoVo.setBizStatus(paperInfo.getBizStatus());

        List<QuestionInfo> questionInfoList = questionInfoRepository.findByPaperIdAndStatus(paperInfo.getId(), WXStatusEnum.Status.NORMAL.getStatus());
        List<PaperAllInfoVo.QuestionInfoAllVo> questionInfoAllVos = questionInfoList.stream()
                .map(question -> {
                    PaperAllInfoVo.QuestionInfoAllVo questionInfoAllVo = new PaperAllInfoVo.QuestionInfoAllVo();
                    questionInfoAllVo.setId(question.getId());
                    questionInfoAllVo.setStem(question.getStem());
                    List<ChoiceInfo> choiceInfoList = choiceInfoRepository.findByQuestionIdInAndStatus(new ArrayList<Long>() {{
                        add(question.getId());
                    }}, WXStatusEnum.Status.NORMAL.getStatus());
                    questionInfoAllVo.setChoiceInfoList(choiceInfoList);
                    return questionInfoAllVo;
                })
                .collect(Collectors.toList());
        allInfoVo.setQuestionInfoAllVoList(questionInfoAllVos);
        return allInfoVo;
    }

    /**
     * 根据 试卷id 删除 考题信息 与 选项信息
     *
     * @param paperId
     */
    private void deleteQuestionAndChoiceByPaperId(long paperId) {
        List<QuestionInfo> questionInfoList = questionInfoRepository.findByPaperIdAndStatus(paperId, WXStatusEnum.Status.NORMAL.getStatus());
        //1. 删除选项信息
        List<QuestionInfo> collect = questionInfoList.stream()
                .map(questionInfo -> {
                    choiceInfoRepository.deleteChoiceByQuestionId(questionInfo.getId(), WXStatusEnum.Status.DELETE.getStatus());
                    questionInfo.setStatus(WXStatusEnum.Status.DELETE.getStatus());
                    return questionInfo;
                })
                .collect(Collectors.toList());
        //2. 删除考题信息
        questionInfoRepository.save(collect);
    }

    @Override
    public PageUtil<PaperInfo> list(int page, int pageSize, int type, String paperName) {
        long count = paperInfoRepositoryImpl.count(type, paperName);
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (count != 0) {
            mapList.addAll(paperInfoRepositoryImpl.listForLimit(page, pageSize, type, paperName));
        }
        PageUtil result = PageUtil.builder()
                .result(mapList)
                .next(count > page * pageSize ? 1 : 0)
                .total(count)
                .totalPage((int) (Math.ceil((double) count / (double) pageSize)))
                .build();
        return result;

    }

    @Override
    public HashMap<String, Object> meta(long id) {
        PaperAllInfoVo detail = detail(id);
        if (null == detail) {
            throw new BizException(ErrorResult.create(ResultEnum.ERROR.getCode(), "课堂互动信息不存在"), "课堂互动信息不存在");
        }
        HashMap<String, Object> result = new HashMap<>();
        List<Long> collect = detail.getQuestionInfoAllVoList().stream()
                .map(PaperAllInfoVo.QuestionInfoAllVo::getId)
                .collect(Collectors.toList());
        if (null == collect || collect.size() == 0) {
            throw new BizException(ErrorResult.create(ResultEnum.ERROR.getCode(), "该课堂互动信息不存在考题信息"), "该课堂互动信息不存在考题信息");
        }
        List<Map<String, Object>> answerList = answerRepositoryImpl.findDataWithUserNameByQuestionId(collect);
        //考题数据转换成统计数据
        //1.paper基础信息
        HashMap<String, Object> paperInfo = new HashMap() {{
            put("id", detail.getId());
            put("paperName", detail.getPaperName());
            put("type", detail.getType());
        }};
        result.put("paperInfo", paperInfo);
        //2.试题与答案信息
        final boolean choiceMeta = BaseInfo.PAPER_TYPE.choiceMeta(detail.getType());
        List<HashMap<String, Object>> questionList = detail.getQuestionInfoAllVoList().stream()
                .map(questionInfoAllVo -> {
                    HashMap<String, Object> question = new HashMap<String, Object>() {{
                        put("id", questionInfoAllVo.getId());
                        put("stem", questionInfoAllVo.getStem());
                    }};
                    //处理选项数据
                    List<HashMap<String, Object>> choiceCollect = questionInfoAllVo.getChoiceInfoList().stream()
                            .map(choiceInfo ->
                                    new HashMap<String, Object>() {{
                                        put("id", choiceInfo.getId());
                                        put("content", choiceInfo.getContent());
                                    }}
                            )
                            .collect(Collectors.toList());
                    question.put("choiceInfoList", choiceCollect);
                    //处理用户的选项数据
                    List<HashMap<String, Object>> answerCollect = answerList.parallelStream()
                            .filter(map -> null != map.get("questionId")
                                    && Long.valueOf(map.get("questionId").toString()) == questionInfoAllVo.getId())
                            .map(answer ->
                                    new HashMap<String, Object>() {{
                                        put("userName", answer.get("userName"));
                                        put("content", answer.get("content"));
                                    }}
                            )
                            .filter(answer -> answer.get("userName") != null)
                            .collect(Collectors.toList());
                    question.put("answerList", answerCollect);
                    //处理统计的情况
                    if (choiceMeta) {//需要处理统计数据
                        Map<String, Long> metaList = answerCollect.stream()
                                .flatMap(answer ->
                                        Arrays.stream(answer.get("content").toString().split(","))
                                )
                                //过滤掉错误的选项结果(未作答情况)
                                .filter(StringUtils::isNotBlank)
                                .collect(Collectors.groupingBy(String::valueOf, Collectors.counting()));
                        question.put("metaList", metaList);
                    }
                    return question;
                })
                .collect(Collectors.toList());
        result.put("questionInfo", questionList);
        return result;
    }

    @Override
    public void pushV2(long paperId, long classId,long adminId) {
        //校验数据
        PaperInfo paperInfo = paperInfoRepository.findOne(paperId);
        if (null == paperInfo || paperInfo.getStatus() != WXStatusEnum.Status.NORMAL.getStatus()) {
            throw new BizException(ErrorResult.create(ResultEnum.ERROR.getCode(), "课堂互动信息不存在"), "课堂互动信息不存在");
        }
        //插入推送记录
        PaperPushLog paperPushLog = PaperPushLog.builder()
                .classId(classId)
                .paperId(paperId)
                .build();
        paperPushLog.setCreator(adminId+"");
        paperPushLog = paperPushLogRepository.save(paperPushLog);
        long pushId = paperPushLog.getId();

        //执行推送消息
        //1.获取当前课堂活动对应的所有用户信息,逐个推送
        List<Map<String, Object>> mapList = userRepository.listForLimit(1, 100, "", classId, -1);
        for (Map user : mapList) {
            String openId = String.valueOf(user.get("openId"));
            WechatTemplateMsg templateMsg = WechatTemplateMsg.builder()
                    .touser(openId)
//                    .template_id("V8aaTIdIh8PPtmxFfWK3uP-4_lwkrX0qDvRhFYGHzAo")//(测试)
                    .template_id("6YHeT8fixF0Qw-n48Zs-7c8Fkk-R4YRmDeYepZ_enPM")//（线上）
                    .url(String.format(classInteractionUrl, paperId, openId,pushId))
                    .data(MyTreeMap.createMap(
                            new TemplateMap("first", WechatTemplateMsg.item("华图在线面试理论课正在进行中~\n", "#000000")),
                            new TemplateMap("keyword1", WechatTemplateMsg.item(paperInfo.getPaperName(), "#000000")),
                            new TemplateMap("keyword2", WechatTemplateMsg.item(user.get("uname") == null ? "无法显示名称" : user.get("uname").toString(), "#000000")),
                            new TemplateMap("remark", WechatTemplateMsg.item("\n请点击详情，回答老师发布的问题哦！", "#000000"))
                    )).build();
            templateMsgService.sendTemplate(JsonUtil.toJson(templateMsg));
        }
    }

    @Override
    public Map<String, Object> metaV2(long paperId, long adminId,long classId,long pushId) {
        PaperAllInfoVo detail = detail(paperId);
        if (null == detail) {
            throw new BizException(ErrorResult.create(ResultEnum.ERROR.getCode(), "课堂互动信息不存在"), "课堂互动信息不存在");
        }
        if(pushId == 0){
            List<PaperPushLog> pushList = ListUtils.EMPTY_LIST;
            //判断班级是否为默认值
            if(-1 == classId){
                //根据试卷id和推送人id查询
                pushList = paperPushLogRepository.findByPaperIdAndCreatorOrderByIdDesc(paperId, adminId + "");
            }else{
                //根据试卷id和班级id查询
                pushList = paperPushLogRepository.findByPaperIdAndClassId(paperId, classId);
            }
            if(CollectionUtils.isNotEmpty(pushList)){
                pushId = pushList.get(0).getId();
            }
        }

        HashMap<String, Object> result = new HashMap<>();
        List<Long> collect = detail.getQuestionInfoAllVoList().stream()
                .map(PaperAllInfoVo.QuestionInfoAllVo::getId)
                .collect(Collectors.toList());
        if (null == collect || collect.size() == 0) {
            throw new BizException(ErrorResult.create(ResultEnum.ERROR.getCode(), "该课堂互动信息不存在考题信息"), "该课堂互动信息不存在考题信息");
        }
        List<Map<String, Object>> answerList = answerRepositoryImpl.findDataWithUserNameByPushId(pushId);
        //考题数据转换成统计数据
        //1.paper基础信息
        HashMap<String, Object> paperInfo = new HashMap() {{
            put("id", detail.getId());
            put("paperName", detail.getPaperName());
            put("type", detail.getType());
        }};
        result.put("paperInfo", paperInfo);
        //2.试题与答案信息
        final boolean choiceMeta = BaseInfo.PAPER_TYPE.choiceMeta(detail.getType());
        List<HashMap<String, Object>> questionList = detail.getQuestionInfoAllVoList().stream()
                .map(questionInfoAllVo -> {
                    HashMap<String, Object> question = new HashMap<String, Object>() {{
                        put("id", questionInfoAllVo.getId());
                        put("stem", questionInfoAllVo.getStem());
                    }};
                    //处理选项数据
                    List<HashMap<String, Object>> choiceCollect = questionInfoAllVo.getChoiceInfoList().stream()
                            .map(choiceInfo ->
                                    new HashMap<String, Object>() {{
                                        put("id", choiceInfo.getId());
                                        put("content", choiceInfo.getContent());
                                    }}
                            )
                            .collect(Collectors.toList());
                    question.put("choiceInfoList", choiceCollect);
                    //处理用户的选项数据
                    List<HashMap<String, Object>> answerCollect = answerList.parallelStream()
                            .filter(map -> null != map.get("questionId")
                                    && Long.valueOf(map.get("questionId").toString()) == questionInfoAllVo.getId())
                            .map(answer ->
                                    new HashMap<String, Object>() {{
                                        put("userName", answer.get("userName"));
                                        put("content", answer.get("content"));
                                    }}
                            )
                            .filter(answer -> answer.get("userName") != null)
                            .collect(Collectors.toList());
                    question.put("answerList", answerCollect);
                    //处理统计的情况
                    if (choiceMeta) {//需要处理统计数据
                        Map<String, Object> metaList = new HashMap<>();
                        List<ChoiceInfo> choiceInfoList = questionInfoAllVo.getChoiceInfoList();

                        for(ChoiceInfo choiceInfo:choiceInfoList){
                            HashMap<String, Object> choiceInfoMeta = new HashMap<>();
                            StringBuilder userList = new StringBuilder();
                            int num = 0;
                            //遍历answerList
                            for(Map userAnswer:answerCollect){
                                if(null != userAnswer.get("content") && userAnswer.get("content").toString().contains(choiceInfo.getId()+"")){
                                    userList.append(userAnswer.get("userName")).append(",");
                                    num ++;
                                }

                            }
                            choiceInfoMeta.put("num",num);
                            choiceInfoMeta.put("userList", userList.substring(0, ((userList.length()>= 1?userList.length() - 1:0))).toString());
                            metaList.put(choiceInfo.getId()+"",choiceInfoMeta);
                        }
                        question.put("metaList", metaList);
                    }
                    return question;
                })
                .collect(Collectors.toList());
        result.put("questionInfo", questionList);
        return result;
    }

    @Override
    public List<Map<String,Object>> findUserAnswer(String openId, long pushId) {
        List<QuestionAnswer> answerList = answerRepository.findByOpenIdAndPushIdAndStatus(openId, pushId, WXStatusEnum.Status.NORMAL.getStatus());
        List<Map<String, Object>> userAnswerList = new LinkedList<>();
        for(QuestionAnswer answer:answerList){
            long questionId = answer.getQuestionId();
            String content = answer.getContent();
            HashMap<String, Object> answerInfo = new HashMap<>();
            answerInfo.put("questionId",questionId);
            answerInfo.put("content",content);
            userAnswerList.add(answerInfo);
        }
        return userAnswerList;
    }
}
