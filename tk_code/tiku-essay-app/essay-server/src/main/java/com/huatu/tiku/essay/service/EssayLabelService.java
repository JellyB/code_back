package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.resp.EssayLabelCheckVO;
import com.huatu.tiku.essay.vo.resp.EssayLabelInfoVO;
import com.huatu.tiku.essay.vo.resp.QuestionAnswerLabelListVO;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by x6 on 2018/7/5.
 */
public interface EssayLabelService {

    PageUtil<List<QuestionAnswerLabelListVO>> findByConditions(long areaId, String year, double examScoreMin, int wordNumMin, double subScoreRatioMin,
                                                         double examScoreMax, int wordNumMax, double subScoreRatioMax,
                                                         int labelStatus, long questionId, String stem, long answerId, int page, int pageSize, String admin);

    Map startLabel(long answerId, String admin, int isFinal);

    EssayLabelCheckVO checkFlag(long totalId);

    EssayLabelTotal saveTotalLabel(EssayLabelTotal total,String admin);

    long saveDetailLabel(EssayLabelDetail detail);

    QuestionAnswerLabelListVO getNext(String admin,long areaId, String year, double examScoreMin, int wordNumMin, double subScoreRatioMin,
                                      double examScoreMax, int wordNumMax, double subScoreRatioMax,
                                      int labelStatus, long questionId, String stem,long answerId);

    void closeUnfinishedLabel();

    long findNext(String admin,long areaId, String year, double examScoreMin, int wordNumMin, double subScoreRatioMin,
                  double examScoreMax, int wordNumMax, double subScoreRatioMax,
                  int labelStatus, long questionId, String stem);

    int saveCopyRatioToMysql(long answerId, double copyRatio);

    EssayLabelInfoVO getInfo(long totalId);

    Long copy(long totalId, long finalId,String admin);

    EssayLabelDetail getDetailInfo(long detailId);

    int delLabel(long totalId);

    int delDetailLabel(long detailId);

    List<EssayLabelDetail> getThesisList(long totalId);

    int restart(long finalId,String admin);

    void addFinalLabelTeacher(String teacher);

    Object getFinalLabelTeacher();

    ModelAndView getFinalExcel(long questionId,long startDate,long endDate);

    ModelAndView getTeacherExcel(long startDate,long endDate);

    int giveUpLabel(long totalId,String teacher,Integer type);

    ModelAndView getFinalXmlExcel(Long labelId);

    void fixTitle();

    void addVIPlLabelTeacher(String teacher);

    Object getVIPLabelTeacher();

    Object getFinalChart(long questionId, long start, long end);

    Boolean checkEdit(long totalId);

    long delVIPlLabelTeacher(String teacher);

    Function<EssayQuestionAnswer, Double> getCopyRatio();
}
