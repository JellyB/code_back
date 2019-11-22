package com.huatu.ztk.backend.arena.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.huatu.ztk.backend.arena.service.ArenaService;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 竞技场信息
 * Created by linkang on 11/18/16.
 */

@RestController
@RequestMapping(value = "/arena", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ArenaController {

    @Autowired
    private ArenaService arenaService;


    /**
     * 根据输入的日期查询竞技排名
     *
     * @param date
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "rank/day", method = RequestMethod.GET)
    public Object rankList(@RequestParam String date) throws BizException {
        if (StringUtils.isBlank(date)) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        return arenaService.findRankByDate(date);
    }


    /**
     * 根据输入的账号查询竞技排名
     *
     * @param account
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "rank/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object search(@RequestParam String account) throws BizException {
        return arenaService.findRankByAccount(account);
    }


}
