package com.huatu.tiku.banckend.service;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.dto.request.IconDto;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-29 2:14 PM
 **/
public interface IconService {

    int add(int subject, List<IconDto> icons) throws BizException;

    int update(int subject, IconDto iconDto) throws BizException;

    List<IconDto> list(int subject) throws BizException;

    int turn(Long id, int bizStatus) throws BizException;
}
