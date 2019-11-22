package com.huatu.ztk.paper.common;

import com.self.generator.DistributedIdGeneratorClient;
import com.self.generator.common.zookeeper.ZKClient;

/**
 * Created by shaojieyue
 * Created time 2016-05-05 20:28
 */

public class IdClient {
    private static DistributedIdGeneratorClient client = null;

    static {
        final String esAddress = ZookeeperAddress.getEsAddress();
        ZKClient zkClient = ZKClient.getClient(esAddress);
        client = new DistributedIdGeneratorClient(zkClient);
    }

    public static final DistributedIdGeneratorClient getClient(){
        if (client == null) {
            init();
        }

        return client;
    }

    /**
     * 初始化es客户端
     */
    private static synchronized void init(){
        if (client != null) {//已经初始化过,则不进行初始化
            return;
        }
        final String esAddress = ZookeeperAddress.getEsAddress();
        ZKClient zkClient = ZKClient.getClient(esAddress);
        client = new DistributedIdGeneratorClient(zkClient);
    }
}
