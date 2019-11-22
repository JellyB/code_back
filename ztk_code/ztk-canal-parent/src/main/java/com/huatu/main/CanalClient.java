package com.huatu.main;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.huatu.beans.ChangedDataInfo;
import com.huatu.mq.JsonUtil;
import com.huatu.mq.MQUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import static com.huatu.constants.Constant.*;

/**
 * Created by ismyway on 16/5/10.
 */
public class CanalClient {

    private static final Logger logger = LoggerFactory.getLogger(CanalClient.class);
    //每次获取数量
    private static final int BATCH_SIZE = 1;

    //试题和试卷的表名
    private static final String TABLE_QUESTION = "v_obj_question";
    private static final String TABLE_MULTI_QUESTION = "v_multi_question";
    //真题
    private static final String TABLE_PASTPAPER_INFO = "v_pastpaper_info";
    //模拟题
    private static final String TABLE_TESTPAPER_INFO = "v_testpaper_info";

    private static final String TABLE_MODETESTPAPER_INFO = "v_modetest_paper_info";

    //客户端是否运行的开关
    private volatile boolean running = false;
    //监控binlod的主线程
    private Thread thread = null;
    //Canal连接器;
    private CanalConnector connector;

    //初始化Canal连接器
    private void init() {
        connector = CanalConnectors.newSingleConnector(new InetSocketAddress(SERVER_HOST, SERVER_PORT), CNANL_DESTINATON, JDBC_USER, JDBC_PASSWORD);
    }

    //启动方法
    public void start() {
        init();
        Assert.notNull(connector, "connector is null");
        thread = new Thread(new Runnable() {

            public void run() {
                //主要业务逻辑处理方法
                process();
            }
        });

        //开关打开
        running = true;
        //线程开始执行
        thread.start();
    }

    /**
     * 关闭处理的线程
     */
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }

        MDC.remove("destination");
    }

    /**
     * 主要处理过程
     */
    private void process() {

        while (running) {
            try {
                MDC.put("destination", CNANL_DESTINATON);
                connector.connect();
                connector.subscribe();
                while (running) {

                    try {
                        Message message = connector.getWithoutAck(BATCH_SIZE); // 获取指定数量的数据
                        long batchId = message.getId();
                        int size = message.getEntries().size();
                        if (batchId == -1 || size == 0) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                        } else {
                            //TODO 主要业务逻辑处理
                            //将监控到的数据写入到mq里面
                            for (Entry entry : message.getEntries()) {
                                //只处理rowdata
                                if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) {
                                    continue;
                                }

                                //如果是指定的表发生变动,则封装ChangedDataInfo类,将关键改动信息发送到mq中
                                String tableName = entry.getHeader().getTableName();

                                if (isNeedSend(tableName)) {
                                    ChangedDataInfo dataInfo = new ChangedDataInfo();
                                    //设置表名
                                    dataInfo.setTableName(tableName);
                                    RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
                                    //设置操作类型
                                    CanalEntry.EventType type = rowChange.getEventType();
                                    dataInfo.setOption(type.toString());
                                    List<CanalEntry.RowData> datas = rowChange.getRowDatasList();
                                    for (CanalEntry.RowData rowData : datas) {
                                        //只处理增删改
                                        if (type != CanalEntry.EventType.INSERT && type != CanalEntry.EventType.UPDATE && type != CanalEntry.EventType.DELETE) {
                                            continue;
                                        }

                                        if (type == CanalEntry.EventType.INSERT) {
                                            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                                                if (column.getName().equalsIgnoreCase("PUKEY")) {
                                                    dataInfo.setPuKey(Integer.parseInt(column.getValue()));
                                                    break;
                                                }
                                            }
                                        } else if (type == CanalEntry.EventType.DELETE) {
                                            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                                                if (column.getName().equalsIgnoreCase("PUKEY")) {
                                                    dataInfo.setPuKey(Integer.parseInt(column.getValue()));
                                                    break;
                                                }
                                            }
                                        } else {
                                            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                                                //只处理v_modetest_paper_info被修改时的情况，插入删除的情况与v_testpaper_info表是一致的
                                                if (tableName.equals(TABLE_MODETESTPAPER_INFO) && column.getName().equalsIgnoreCase("testpaper_id")) {
                                                    dataInfo.setPuKey(Integer.parseInt(column.getValue()));
                                                    break;
                                                }
                                                //如果是v_modetest_paper_info表，继续，为了查找到下一列testpaper_id，该表第一列是记录id
                                                if (tableName.equals(TABLE_MODETESTPAPER_INFO)) {
                                                    continue;
                                                }

                                                if (column.getName().equalsIgnoreCase("PUKEY")) {
                                                    dataInfo.setPuKey(Integer.parseInt(column.getValue()));
                                                    break;
                                                }
                                            }
                                        }
                                        //按表发送信息
                                        final String msg = JsonUtil.toJson(dataInfo);
                                        logger.info("send message={}",msg);

                                        //与v_testpaper_info共用一个队列
                                        if (tableName.equals(TABLE_MODETESTPAPER_INFO)) {
                                            tableName = TABLE_TESTPAPER_INFO;
                                        }

                                        MQUtil.sendMessage(tableName, msg);
                                    }

                                } else {
                                    continue;
                                }
                            }
                        }

                        connector.ack(batchId); // 提交确认
                        // connector.rollback(batchId); // 处理失败, 回滚数据
                    } catch (IOException | CanalClientException exception ){
                        break;//IO异常则跳出循环,重新连接
                    }catch (Exception e) {
                        logger.error("ex", e);
                    }

                }
            } catch (Exception e) {
                logger.error("process error!", e);

            } finally {
                connector.disconnect();
                MDC.remove("destination");
                try {
                    Thread.sleep(100000);//阻塞一段时间
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断是否是需要发送改变信息的表
     *
     * @param tableInfo 从canal读取到的表信息,一般为 库名+表名
     * @return
     */
    private boolean isNeedSend(String tableInfo) {
        return tableInfo.contains(TABLE_PASTPAPER_INFO) ||tableInfo.contains(TABLE_TESTPAPER_INFO)
                || tableInfo.contains(TABLE_QUESTION) || tableInfo.contains(TABLE_MULTI_QUESTION)
                || tableInfo.contains(TABLE_MODETESTPAPER_INFO);
    }

    public static void main(String[] args) {
        final CanalClient canalClient = new CanalClient();
        canalClient.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                try {
                    logger.info("## stop the canal client");
                    canalClient.stop();
                } catch (Throwable e) {
                    logger.warn("##something goes wrong when stopping canal:\n{}", ExceptionUtils.getFullStackTrace(e));
                } finally {
                    logger.info("## canal client is down.");
                }
            }

        });

    }


}
