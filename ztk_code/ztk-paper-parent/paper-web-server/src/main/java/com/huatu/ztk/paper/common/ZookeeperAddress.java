package com.huatu.ztk.paper.common;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfItem;

/**
 * zk配置地址
 * Created by shaojieyue
 * Created time 2016-05-30 20:25
 */

@DisconfFile(filename = "zookeeper.properties")
public class ZookeeperAddress {
    public static String esAddress;

    @DisconfFileItem(name = "zookeeper.address",associateField ="esAddress")
    public static String getEsAddress() {
        return esAddress;
    }

    public static void setEsAddress(String esAddress) {
        ZookeeperAddress.esAddress = esAddress;
    }
}
