package com.huatu.ztk.backend.subject.bean;

import com.huatu.ztk.commons.exception.ErrorResult;

/**
 * Created by linkang on 3/3/17.
 */
public class SubjectErrors {

    public static final ErrorResult CHILDREN_EXISTS = ErrorResult.create(20001, "该科目存在子科目，无法删除");

    public static final ErrorResult SUBJECT_EXISTS = ErrorResult.create(20001, "已存在该科目");


    public static final ErrorResult NODE_EXISTS = ErrorResult.create(20002, "已存在该节点");


    public static final ErrorResult EMPTY_NAME = ErrorResult.create(20003, "节点名称为空");


    public static final ErrorResult CANNOT_DEL = ErrorResult.create(20004, "该节点无法删除");

}
