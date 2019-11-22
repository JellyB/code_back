package com.huatu.tiku.essay.service;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.dto.ImageSortDto;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.file.YoutuMQVO;
import com.huatu.tiku.essay.vo.file.YoutuVO;
import com.huatu.tiku.essay.vo.resp.*;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by x6 on 2017/11/23.
 */
public interface UserAnswerService {
    ResponseVO createAnswerCard(int userId, CreateAnswerCardVO createAnswerCardVO, int terminal, EssayAnswerCardEnum.ModeTypeEnum normal);

    /**
     * 创建答题卡v2
     *
     * @param userId
     * @param createAnswerCardVO
     * @param terminal
     * @param unFinishedCount
     * @param answerCardType
     * @return
     */
    ResponseVO createAnswerCardV2(int userId, CreateAnswerCardVO createAnswerCardVO, int terminal, BiFunction<Integer, CreateAnswerCardVO, Long> unFinishedCount, int answerCardType);

    List<EssayAnswerVO> correctList(int userId, Integer type, Pageable pageRequest, EssayAnswerCardEnum.ModeTypeEnum normal);

    long countCorrectList(int userId, int type, EssayAnswerCardEnum.ModeTypeEnum normal);

    Object paperCommit(UserSession userSession, PaperCommitVO paperCommitVO, int terminal, String cv, EssayAnswerCardEnum.ModeTypeEnum normal);


    List<EssayQuestionVO> answerDetail(int id, int type, long answerId, int terminal, String cv);

    ResponseVO correctCount(int userId, int type, long baseId, EssayAnswerCardEnum.ModeTypeEnum normal);

    ResponseVO free();

    Object correctPaper(UserSession userSession, PaperCommitVO paperCommitVO, int terminal, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum);


    YoutuVO photo(MultipartFile file, int type, int userId, int terminal) throws Exception;

    List<EssayQuestionVO> answerDetailV2(int id, int type, long answerId, int terminal, String cv);

    Map delAnswer(int type, long answerId);

    List<EssayAnswerVO> recycleList(int userId, Integer type, Pageable pageRequest);

    long countRecycleList(int userId, Integer type);

    List<EssayAnswerVO> questionCorrectList(int userId, Integer type, EssayAnswerCardEnum.ModeTypeEnum normal, Pageable pageRequest);

    long countQuestionCorrectList(int userId, Integer type);

    Object unfinishedCardCommit();

    void savePhotoAnswer(YoutuMQVO vo);

    EssayQuestionVO answerDetailV2(long answerId);

    /**
     * @param file
     * @param userId
     * @param answerId
     * @param sort
     * @return
     */
    Object photoDistinguish(MultipartFile file, int userId, long answerId, int sort);

    /**
     * 修改答题图片排序
     *
     * @param userId
     * @param dtoList
     * @return
     */
    Object updatePhotoSort(int userId, List<ImageSortDto> dtoList);

    /**
     * 逻辑删除图片long
     *
     * @param imageId
     * @param answerId
     * @return
     */
    Object deleteImageByLogic(long answerId, long imageId);

    /**
     * 获取图片对应内容
     *
     * @param uid
     * @param answerId
     * @return
     */
    Object getImageContent(int uid, Long answerId);

    /**
     * 提交答题卡
     *
     * @param userSession
     * @param paperCommitVO
     * @param terminal
     * @param cv
     * @return
     */
    Object paperCommitV2(UserSession userSession, PaperCommitVO paperCommitVO, int terminal, String cv);

    /**
     * 套卷的提交批改
     *
     * @param userSession
     * @param paperCommitVO
     * @param terminal
     * @return
     */
    Object correctPaperV2(UserSession userSession, PaperCommitVO paperCommitVO, int terminal);

    public BiFunction<Integer,CreateAnswerCardVO,Long> getUnFinishedCount(CreateAnswerCardVO createAnswerCardVO);

}
