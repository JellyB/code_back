package com.huatu.tiku.match.service.v1.count;

import java.util.Map;

/**
 * Created by huangqingpeng on 2019/3/20.
 */
public interface TerminalService {

    Map<Integer,Integer> groupByTerminal(int matchId);
}
