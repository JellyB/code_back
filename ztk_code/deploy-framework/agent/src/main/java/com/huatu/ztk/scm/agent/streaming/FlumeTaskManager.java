package com.huatu.ztk.scm.agent.streaming;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.huatu.ztk.smc.flume.TailSource;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: shijinkui
 * Date: 13-8-29
 * Time: 下午3:08
 * To change this template use File | Settings | File Templates.
 */
public class FlumeTaskManager {
    private static final Logger logger = LoggerFactory.getLogger(FlumeTaskManager.class.getName());

    private static final int TAIL_SLEEP_TIME = 200;
    private static final int LINES_READ_START = 50;
    // flume tasks
    private final ConcurrentMap<String, FlumeTask> tasks = new ConcurrentHashMap<String, FlumeTask>();

    public void addContext(File file, ChannelHandlerContext ctx) {
    	Preconditions.checkNotNull(file);
    	String absolutePath = file.getAbsolutePath();
    	if(tasks.get(absolutePath)==null){//初始化FlumeTask
    		logger.info("==>init task "+absolutePath);
    		FlumeTask t = new FlumeTask(file);
    		t.start();
    		tasks.put(file.getAbsolutePath(), t);
    	}
    	//添加消费者
    	tasks.get(absolutePath).addConsumer(ctx);
    }

    public void deleteContext(ChannelHandlerContext ctx) {
    	String key = null;
    	FlumeTask task = null;
    	Iterator<String> iter = tasks.keySet().iterator();
    	boolean success ;
    	while(iter.hasNext()){
    		key = iter.next();
    		task = tasks.get(key);
    		success = task.removeConsumer(ctx);
    		if(success&&task.countConsumer()==0){//无消费者
    			logger.info("remove task "+key);
    			tasks.remove(key);
    			task.stopTask();
    		}
    	}
    }
    
    
    /**
     * 读log, 写streaming
     */
    private final class FlumeTask extends Thread {
        private final AtomicBoolean isRunning = new AtomicBoolean(true);
        private final String taskName;
        private final TailSource tail;
        private final ConcurrentMap<String, ChannelHandlerContext> contexts =
                new ConcurrentHashMap<String, ChannelHandlerContext>();
        private FlumeTask(File file) {
        	//参数检查
        	Preconditions.checkArgument(file.exists()&&file.isFile(),"file "+file.getAbsolutePath()+" is not exist or not a file");
        	//任务名称
            this.taskName=file.getAbsolutePath();
            this.tail = new TailSource(file, offset(file,LINES_READ_START), TAIL_SLEEP_TIME);
        }
        
        private long offset(File file,int line){
    		List<String> list = null;
    		try {
    			list = Files.readLines(file,Charset.forName("UTF-8"));
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
    		if(list.size()<=line){//文件的行数不大于要求输出的行数
    			return 0;
    		}
    		long len = 0;
    		for(int i=list.size()-line;i<list.size();i++){
    			len = len+list.get(i).length();
    		}
    		
    		return file.length()-len-2;
        }
        
        public void addConsumer(ChannelHandlerContext context){
        	contexts.put(context.toString(), context);
        	logger.info("'"+taskName+"' add consumer "+context);
        	logger.info("'"+taskName+"' The current number of consumers is "+countConsumer());
        }
        
        public boolean removeConsumer(ChannelHandlerContext context){
        	boolean success = !(contexts.remove(context.toString())==null);
        	if(success){
        		logger.info("'"+taskName+"' del consumer ");
        		logger.info("'"+taskName+"' The current number of consumers is "+countConsumer());
        	}
        	return success;
        }
        
        public int countConsumer(){
        	return contexts.size();
        }
        
        public void stopTask() {
        	logger.info("==>stop monitor tail source "+tail.getName());
            isRunning.compareAndSet(true, false);
            try {
				tail.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
              logger.error("IOException",e);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
              logger.error("InterruptedException",e);
			}
        }

        @Override
        public void run() {
            if (!isRunning.get()) {
                return;
            }

            try {
                tail.open();
            } catch (IOException e) {
                logger.error("can not open flume", e);
                return;
            }
            while (isRunning.get()) {
                try {
                    byte[] bytes = tail.next().getBody();
                    //分发消息
                    broadcast(new String(bytes));
                } catch (IOException e) {
                  logger.error("IOException",e);
                } catch (InterruptedException e) {
                  logger.error("InterruptedException",e);
                }
            }
        }
        
        /**
         * 广播分发消息
         * @param msg
         */
        public void broadcast(String msg){
        	Iterator<ChannelHandlerContext> iter = contexts.values().iterator();
        	ChannelHandlerContext context = null;
        	while(iter.hasNext()){
        		context = iter.next();
        		context.writeAndFlush(new TextWebSocketFrame(msg));
        	}
        }
    }
}
