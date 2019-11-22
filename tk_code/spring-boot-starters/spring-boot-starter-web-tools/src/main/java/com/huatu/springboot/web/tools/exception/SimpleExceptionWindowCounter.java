package com.huatu.springboot.web.tools.exception;

import com.huatu.common.utils.date.TimestampUtil;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 按秒统计
 * @author hanchao
 * @date 2018/1/15 18:04
 */
public class SimpleExceptionWindowCounter implements ExceptionCounter,Runnable {
    private static final int SAMPLE_SIZE = 100;
    private volatile int current;
    private int windowSize;
    private int windowSeconds;
    private AtomicInteger[] windowCounter;
    private CircularFifoBuffer exceptionContainer = new CircularFifoBuffer(SAMPLE_SIZE);
    private ReentrantLock lock = new ReentrantLock(false);
    private static Thread monitor;

    public SimpleExceptionWindowCounter(){
        //默认认为12个窗口，每个窗口记录5s
        this(12,5);
    }

    public SimpleExceptionWindowCounter(int windowSize,int windowSeconds){
        this.windowSize = windowSize;
        this.windowSeconds = windowSeconds;
        windowCounter = new AtomicInteger[windowSize];
        for (int i = 0; i < windowCounter.length; i++) {
            windowCounter[i] = new AtomicInteger(0);
        }
        monitor = new Thread(this);
        monitor.setDaemon(true);
        monitor.start();
    }

    private int getIndex(){
        //不加锁，允许出现被覆写
        int timeStamp = TimestampUtil.currentUnixTimeStamp();
        int index = (timeStamp / windowSeconds) % windowSize;
        if(index != current){
            windowCounter[index].set(0);
            current = index;
        }
        return index;
    }

    public void add(Exception e){
        int index = getIndex();
        windowCounter[index].incrementAndGet();
        lock.lock();
        try {
            exceptionContainer.add(e);
        } finally {
            lock.unlock();
        }
    }


    /**
     * 返回目前总共出现的异常数量
     * @return
     */
    public int count(){
        return Arrays.stream(windowCounter).mapToInt(AtomicInteger::get).sum();
    }

    @Override
    public int seconds() {
        return windowSize * windowSeconds;
    }

    @Override
    public Collection<Exception> samples() {
        return exceptionContainer;
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()){
                getIndex();
                Thread.sleep(1000);
            }
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}
