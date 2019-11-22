package com.huatu.ztk.backend;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.metas.controller.MatchInfoController;
import com.huatu.ztk.commons.exception.BizException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/11/19
 * @描述 统计模考大赛参加人员单元测试
 */
public class MatchInfoTest extends BaseTestW {


    @Autowired
    MatchInfoController matchInfoController;

    //路径配置备份备用
     /*   System.setProperty("webapp.dir", "\\Users\\lizhenjuan\\20181009-dataTool\\ztk-data-tool\\data-tool-server\\src\\main\\webapp");
        System.setProperty("server_resources", "\\Users\\lizhenjuan\\20181009-dataTool\\ztk-data-tool\\data-tool-server\\src\\main");
        System.setProperty("server_name", "ztk-data-tool");
        System.setProperty("server_ip", "localhost");
        System.setProperty("disconf.user_define_download_dir", "/Users/lizhenjuan/03workCode/disconf");
        System.setProperty("disconf.env", "online");*/

    /**
     * 统计某个模考大赛参见人员
     *
     * @throws BizException
     */
    @Test
    public void getMatchEnrollInfo() throws BizException {

        //4000959 19季度;4000964 20季度;4000976 21季度;22季度,4000990;4000996 二十三季度；4001012 24季度;4001019 24补考
        //4000959,4000964,4000976,4000990,4000996,4001012,4001019

        // 1-3季度（3526831,3526876,3526881）
        //4-到 10季度(3526885,3526890,3526894,3526895,3526898,3526901,3526909);
        //11-18 季度（3526911,3526922,3526924,3526930,3526934,3526936,4000938,4000956）
        //19-24季度（4000959,4000964,4000976,4000990,4000996,4001012,4001019）
        List<Integer> ids = Lists.newArrayList(3526911,3526922,3526924,3526930,3526934,3526936,4000938,4000956);
        ids.stream().forEach(id -> {
            try {
                matchInfoController.getMatchEnrollInfo(id);
            } catch (BizException e) {
                e.printStackTrace();
            }
        });

    }

}
