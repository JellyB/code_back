package com.huatu.tiku.essay.vo.resp;

/**
 * 学员申论题目收藏关系表（试题，试卷）
 */

public class EssayUserCollectionVO{

    //类型(单题 0  套题1)
    private int type;

    //用户id
    private int userId;

    //题目id。试题id
    private long baseId;
    //题目id。试题id
    private long detailId;
    //收藏id
    private String name;






}
