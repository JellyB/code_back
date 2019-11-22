package com.huatu.ztk.knowledge.api;



import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;

import java.util.List;
import java.util.Set;

/**
 * 知识点dubbo 服务
 * Created by shaojieyue
 * Created time 2016-05-08 14:16
 */
public interface QuestionPointDubboService {

    /**
     * 汇总答题的知识点
     * @param questions 试题列表
     * @param corrects 答题是否正确
     * @param times 答题时间
     * @return
     */
    public List<QuestionPointTree> questionPointSummary(List<Integer> questions, int[] corrects, int[] times);

    /**
     * 汇总答题的知识点 -- 此方法计算正确率时候使用的基数为所有题目
     * @param questions 试题列表
     * @param corrects 答题是否正确
     * @param times 答题时间
     * @return
     *
     * add by lijun 2018-03-27
     */
    List<QuestionPointTree> questionPointSummaryWithTotalNumber(List<Integer> questions, int[] corrects, int[] times);

    /**
     * 通过id查询知识点
     * @param pointId 知识点id
     * @return
     */
    public QuestionPoint findById(final int pointId);

    /**
     * 查询一个知识点的子节点
     * @param pointId
     * @return
     */
    public List<QuestionPoint> findChildren(final int pointId);

    /**
     * 查询父类节点
     * @param pointId 父类节点信息
     * @return
     */
    public List<QuestionPoint> findParent(final int pointId);


    /**
     * 组装知识点树
     * @param pointId 知识点
     * @param recursive 是否递归查询知识点
     * @return
     */
    public List<QuestionPointTree> findPointTree(final int pointId,boolean recursive);

    /**
     * 随机获取知识点
     * @return 随机知识点
     */
    public QuestionPoint randomPoint();

    /**
     * 从一个知识点下边,随机获取指定个数的3级知识点
     * @param point 知识点
     * @param count 获取随机知识点个数
     * @return
     */
    public List<QuestionPoint> randomPoint(int point,int count);

    /**
     * 批量查询知识点
     * @param children
     * @return
     */
    public List<QuestionPoint> findBath(List<Integer> children);

    /**
     * 查询用户已经做过的知识点
     * @param uid 用户id
     * @param subject 科目
     * @return 返回用户已经做过知识点集合
     */
    public Set<Integer> findUserPoints(long uid, int subject);

    /**
     * 查询用户知识点数量
     * 里面包含了所有级别的节点
     * @param subject 科目
     * @return
     */
    public int findPointsCount(int subject);

    /**
     * 查找每日特训的知识点
     *
     * @param uid
     * @param subject
     * @param size
     * @return
     */
    List<QuestionPoint> findDayTrainPoints(long uid, int subject, int size);
}
