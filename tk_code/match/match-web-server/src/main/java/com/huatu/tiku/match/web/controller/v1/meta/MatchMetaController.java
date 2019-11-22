package com.huatu.tiku.match.web.controller.v1.meta;

import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.match.service.v1.count.TerminalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by huangqingpeng on 2019/3/20.
 */
@RestController
@RequestMapping(value = "meta")
@ApiVersion(value = "v1")
@Slf4j
public class MatchMetaController {

    @Autowired
    TerminalService terminalService;

    @RequestMapping("terminal/{matchId}")
    public Object countByTerminal(@PathVariable int matchId){
        return terminalService.groupByTerminal(matchId);
    }
}
