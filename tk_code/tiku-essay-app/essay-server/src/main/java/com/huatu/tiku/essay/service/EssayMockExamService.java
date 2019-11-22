package com.huatu.tiku.essay.service;

import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.entity.EssayMaterial;
import com.huatu.tiku.essay.entity.EssayMockExam;
import com.huatu.tiku.essay.entity.vo.report.EssayAnswerCardVO;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.admin.EssayMockExamVO;
import com.huatu.tiku.essay.entity.vo.report.MockScoreReportVO;
import com.huatu.tiku.essay.vo.resp.*;
import com.huatu.tiku.essay.util.PageUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by x6 on 2017/12/15.
 * 模考相关service
 */
public interface EssayMockExamService {

    //材料列表
    List<EssayMaterial> materialList(int userId, long paperId, int terminal);

    //创建答题卡
    EssayMockExamAnswerVO createMockAnswerCard(CreateAnswerCardVO createAnswerCardVO);

    //试题列表
    EssayPaperQuestionVO questionList(int userId, long paperId, int terminal);

    //交卷
    int paperCommit(UserSession userSession, PaperCommitVO paperCommitVO, int terminal);

    //保存模考答题卡
    void saveMockPaperAnswer(String key);

    Map getHistory(int id,int tag);

    //查询成绩报告
    MockScoreReportVO getReport(int id, long paperId, int terminal, EssayAnswerCardEnum.ModeTypeEnum normal);

    //完成批改
    void correctFinish(String finish, EssayAnswerCardEnum.ModeTypeEnum normal);

    EssayMockExam getMock(long id);


    //保存模考信息
    Object saveMockPaper(EssayMockExamVO mockExamVO);

    //修改模考状态
    Object updateMockStatus(int status, long practiceId, long id);

    EssayMockExamVO queryMockPaper(long id);

    ResponseVO getLeftTime(int id, long paperId);

    Boolean calculateButton(long mockId);

    PageUtil<EssayPaperVO> getHistoryPaperList(int userId, int page, int pageSize, int tag, EssayAnswerCardEnum.ModeTypeEnum normal);

    Object handCommit(int userId, long paperId);

    EssayAnswerCardVO dealAnswerCardScore(EssayAnswerCardVO essayAnswerCardVO);

    EssayMockExam getMockDetail(long paperId);
}
