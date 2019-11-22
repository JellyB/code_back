package com.huatu.ztk.user.service;


import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.bean.*;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.dao.FeedbackDao;
import com.huatu.ztk.user.dao.UserDao;
import com.huatu.ztk.user.dao.UserMessageDao;
import com.huatu.ztk.user.utils.ZlibCompressUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 意见反馈服务层
 * Created by shaojieyue
 * Created time 2016-06-06 18:14
 */

@Service
public class FeedbackService {
    private static final Logger logger = LoggerFactory.getLogger(FeedbackService.class);
    public static final int FEEDBACK_CONTENT_MAX_LENGTH = 200;

    @Autowired
    private FeedbackDao feedbackDao;

    @Autowired
    private UserDao userDao;
    @Autowired
    private UserMessageDao userMessageDao;

    public void insert(Feedback feedback, String cv, String system, String device, int catgory) throws BizException {

        String content = feedback.getContent();

        //反馈内容为空
        if (StringUtils.isBlank(content)) {
            throw new BizException(UserErrors.FEEDBACK_CONTENT_IS_NULL);
        }

        //反馈内容过长
        if (content.length() > FEEDBACK_CONTENT_MAX_LENGTH) {
            throw new BizException(UserErrors.FEEDBACK_CONTENT_TOO_LONG);
        }

        //未设置反馈类型
        if (feedback.getType() == 0) {
            throw new BizException(UserErrors.FEEDBACK_NO_TYPE);
        }

        //过滤掉表情符号
        content = removeFourChar(content);

        final UserDto userDto = userDao.findById(feedback.getUid());
//        feedback.setUname(userDto.getName());
        feedback.setContent(StringEscapeUtils.escapeHtml4(StringUtils.trimToEmpty(content)));
        feedback.setContacts(StringEscapeUtils.escapeHtml4(StringUtils.trimToEmpty(feedback.getContacts())));


        //设置创建时间
        feedback.setCreateTime(System.currentTimeMillis());

        Map environmentMap = new LinkedHashMap();
        environmentMap.put("App版本号", cv);
        environmentMap.put("操作设备", device);
        environmentMap.put("系统版本号", system);

        //为了与网站的反馈数据一致，转换成json，再压缩
        String enviromentStr = ZlibCompressUtils.compress(JsonUtil.toJson(environmentMap));

        feedbackDao.insert(feedback, enviromentStr, catgory);
    }

    /**
     * 获取反馈
     *
     * @param type
     * @param size
     * @param page
     * @return
     * @throws BizException
     */
    public FeedbackDto getFeedback(Integer type, Integer processed, Integer size, Integer page, Integer isSolve, String content, Long id, long start, long end) throws BizException {
        if (size < 1 || page < 1) {
            throw new BizException(UserErrors.INDEX_ERROR);
        }

        FeedbackDto feedbackDto = new FeedbackDto();
        List<Feedback> feedbacksByPage = new ArrayList<>();

        feedbacksByPage = feedbackDao.getFeedbacksByTypeAndPage(type, processed, size, page, isSolve, content, id, start, end);

        for (Feedback fb : feedbacksByPage) {
            if (fb.getEnvironmentMap() == null || fb.getEnvironmentMap().length() == 0) {
                continue;
            }
            try {
                String uncompress = ZlibCompressUtils.uncompress(fb.getEnvironmentMap());
                System.out.println(uncompress);
                Map map = JsonUtil.toMap(uncompress);
                fb.setSysVersion(map.get("系统版本号").toString());
                fb.setAppVersion(map.get("App版本号").toString());
                fb.setFacility(map.get("操作设备").toString());
            } catch (Exception e) {
                continue;
            }

        }
        feedbackDto.setFeedbacks(feedbacksByPage);
        Long total = feedbackDao.getFeedbackCount(type, processed, isSolve, content, id, start, end);
        feedbackDto.setTotal(total);
        Long temp = (total / size);
        if (temp * size < total /*&& feedbacksByPage.size() != 0*/) {
            temp++;
        }
        feedbackDto.setTotalPage(temp);
        if (page * size < total) {
            feedbackDto.setNext(1L);
        } else {
            feedbackDto.setNext(0L);
        }
        return feedbackDto;
    }

    /**
     * 过滤掉表情符号
     *
     * @param content
     * @return
     */
    public static String removeFourChar(String content) {
        byte[] conbyte = content.getBytes();
        for (int i = 0; i < conbyte.length; i++) {
            if ((conbyte[i] & 0xF8) == 0xF0) {
                for (int j = 0; j < 4; j++) {
                    conbyte[i + j] = 0x30;
                }
                i += 3;
            }
        }
        content = new String(conbyte);
        return content.replaceAll("0000", "");
    }

    public void delFeedback(Integer id, String modifier) {
        feedbackDao.delFeedback(id, modifier);
    }

    /**
     * 回复学员
     *
     * @param id
     * @param content
     */
    public int reply(int id, String content, String title, String modifier) throws BizException {

        //查询用户意见反馈
        Feedback feedback = feedbackDao.findById(id);
        if (null == feedback) {
            logger.warn("反馈id错误,ID:{}", id);
            throw new BizException(UserErrors.FEEDBACK_ID_NOT_EXIST);
        }

        //获取意见反馈的用户id
        long uid = feedback.getUid();
        //msg表插入数据
        logger.info(content + ":内容:::::::::::::::");
        int msgId = userMessageDao.insertMsg(content, title);
        //msg_person_rel插入数据
        if (0 == msgId) {
            logger.warn("意见反馈回复(msg)保存失败，用户id：{}，回复内容：{}", id, content);
            throw new BizException(UserErrors.REPLY_SAVE_ERROR);
        }

        int insertMsgRel = userMessageDao.insertMsgRel(msgId, uid);
        if (1 != insertMsgRel) {
            logger.warn("意见反馈回复(msg_person_rel)保存失败，用户id：{}，回复内容：{}", id, content);
            throw new BizException(UserErrors.REPLY_SAVE_ERROR);
        }

        //更新feedback表数据
        String messageIds = feedback.getMsgIds();
        if (StringUtils.isEmpty(messageIds)) {
            messageIds = "" + msgId;
        } else {
            messageIds = messageIds + "," + msgId;
        }
        return feedbackDao.updateFeedbackByReply(id, messageIds, modifier);

    }


    /**
     * 查询意见反馈回复
     *
     * @param id
     * @return
     */
    public List<UserMessage> getReply(int id) throws BizException {
        List<UserMessage> list = new LinkedList<UserMessage>();


        //根据feedbackId查询到关联的msgId
        Feedback feedback = feedbackDao.findById(id);
        if (null == feedback) {
            logger.warn("反馈id错误,ID:{}", id);
            throw new BizException(UserErrors.FEEDBACK_ID_NOT_EXIST);
        }
        //根据messageId查询回复内容
        String msgIds = feedback.getMsgIds();
        if (StringUtils.isNotEmpty(msgIds)) {
            String[] split = msgIds.split(",");
            for (String msgId : split) {
                //根据msgId查询消息内容
                UserMessage message = userMessageDao.findUserFeedBackMsgById(Integer.parseInt(msgId));
                logger.info("回复内容:{}", message.getContent());
                logger.info("解压内容:{}", ZlibCompressUtils.uncompress(message.getContent()));
                list.add(message);
            }
        }

        return list;
    }

    public void setSolve(Integer id, Integer solve, String modifier) {

        feedbackDao.setSolve(id, solve, modifier);
    }


    public List<FeedBackContentDto> getBatchUserReply(List<Integer> ids) throws BizException {
        if (CollectionUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        List<FeedBackContentDto> feedBackContentDtoList = new ArrayList<>();
        List<Feedback> feedbackList = feedbackDao.findByIds(ids);

        if (CollectionUtils.isEmpty(feedbackList)) {
            return Lists.newArrayList();
        }

        List<String> msgIds = feedbackList.stream().filter(feedback -> StringUtils.isNotEmpty(feedback.getMsgIds()))
                .map(Feedback::getMsgIds)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(msgIds)) {
            return Lists.newArrayList();
        }

        //需要查询的messageId
        List<Integer> idList = new ArrayList<>();
        msgIds.stream().forEach(msgIdStr -> {

            List<Integer> collect = Arrays.stream(msgIdStr.split(",")).map(id -> Integer.valueOf(id)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                idList.addAll(collect);
            }
        });
        //logger.info("所有的messageId:{}", JsonUtil.toJson(idList));
        List<UserMessage> userFeedBackMsgByIds = userMessageDao.findUserFeedBackMsgByIds(idList);
        // key:msgId,value:msgInfo
        Map<Long, UserMessage> collect = userFeedBackMsgByIds.stream().collect(Collectors.toMap(u -> u.getId(), u -> u));
        //logger.info("collect内容是:{}", JsonUtil.toJson(collect));

        feedbackList.stream().forEach(feedback -> {
            FeedBackContentDto feedBackContentDto = new FeedBackContentDto();
            List<String> replyContentList = new ArrayList<>();
            String msgIdList = feedback.getMsgIds();
            if (StringUtils.isNotEmpty(msgIdList)) {
                List<String> newMsgIdList = Arrays.stream(msgIdList.split(",")).collect(Collectors.toList());
                for (String msgId : newMsgIdList) {
                    UserMessage userMessage = collect.get(Long.valueOf(msgId));

                    if (null != userMessage) {
                        replyContentList.add(userMessage.getContent());
                    }
                }
            }
            feedBackContentDto.setFeedBackId(feedback.getId());
            feedBackContentDto.setReplyContent(replyContentList);
            feedBackContentDtoList.add(feedBackContentDto);
        });
        return feedBackContentDtoList;
    }


}
