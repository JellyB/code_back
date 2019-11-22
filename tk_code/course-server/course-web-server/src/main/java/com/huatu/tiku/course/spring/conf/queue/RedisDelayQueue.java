package com.huatu.tiku.course.spring.conf.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 描述：基于redis 实现的 delay queue
 *
 * @author biguodong
 * Create time 2019-02-26 下午2:46
 **/

@Slf4j
public class RedisDelayQueue implements DelayQueue {

    private transient final ReentrantLock lock = new ReentrantLock();

    private final Condition available = lock.newCondition();

    private RedisTemplate redisTemplate;

    /**
     * 最大超时时间不能超过 10 天
     */
    private long maxTimeOut = TimeUnit.DAYS.toMillis(10);

    private ObjectMapper objectMapper;

    private int unAckTime;

    private String messageStoreKey;

    private String realQueueName;

    private String unAckQueueName;

    private String queueName;

    private DelayQueueProcessListener delayQueueProcessListener;

    private AtomicBoolean nextAvailable = new AtomicBoolean(false);

    @Builder
    public RedisDelayQueue(RedisTemplate redisTemplate, String queueName, int unAckTime,
                           ObjectMapper objectMapper,
                           DelayQueueProcessListener delayQueueProcessListener) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.queueName = queueName;
        this.messageStoreKey =  queueName + MESSAGE;
        this.unAckQueueName = queueName + UN_ACK;
        this.realQueueName = queueName + QUEUE;
        this.unAckTime = unAckTime;
        this.delayQueueProcessListener = delayQueueProcessListener;
    }

    @Override
    public boolean push(Message message) {
        if (message.getTimeout() > maxTimeOut) {
            throw new RuntimeException("Maximum delay time should not be exceed in 10 days");
        }
        message.setCreateTime(System.currentTimeMillis());
        try {
            String json = objectMapper.writeValueAsString(message);
            HashOperations hashOperations = redisTemplate.opsForHash();
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            hashOperations.put(messageStoreKey, message.getId(), json);
            double priority = message.getPriority() / 100;
            double score = Long.valueOf(System.currentTimeMillis() + message.getTimeout()).doubleValue() + priority;
            zSetOperations.add(realQueueName, message.getId(), score);
            delayQueueProcessListener.pushCallback(message);
            nextAvailable.set(true);
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    @Scheduled(fixedRate = 1000)
    public void listen() {
        String id = peekId();
        //log.debug("current message id:{}", id);
        if (id == null) {
            return;
        }
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String json = hashOperations.get(messageStoreKey, id);
        try {
            Message message = objectMapper.readValue(json, Message.class);
            if (message == null) {
                return;
            }
            long delay = message.getCreateTime() + message.getTimeout() - System.currentTimeMillis();
            log.debug("current message delay time:{}", delay);
            if (delay <= 0) {
                delayQueueProcessListener.peekCallback(message);
            } else {
                LockSupport.parkNanos(this, TimeUnit.NANOSECONDS.convert(delay, TimeUnit.MILLISECONDS));
                delayQueueProcessListener.peekCallback(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean ack(String messageId) {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        HashOperations hashOperations = redisTemplate.opsForHash();
        Long number = zSetOperations.remove(unAckQueueName, messageId);
        Long removed = zSetOperations.remove(realQueueName, messageId);
        Long msgRemoved = hashOperations.delete(messageStoreKey, messageId);
        if (removed > 0 && msgRemoved > 0) {
            log.debug("remove a message form unAckQueueName, number id:{}, realQueueName, number id:{}, messageStoreKey number id:{}",
                    number, removed, msgRemoved);
            return true;
        }
        return false;
    }

    @Override
    public boolean setUnAckTimeout(String messageId, long timeout) {
        double unAckScore = Long.valueOf(System.currentTimeMillis() + timeout).doubleValue();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Double score = zSetOperations.score(unAckQueueName, messageId);
        if (score != null) {
            zSetOperations.add(unAckQueueName, messageId, unAckScore);
            return true;
        }
        return false;
    }

    @Override
    public boolean setTimeout(String messageId, long timeout) {
        try {
            HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            String json = hashOperations.get(messageStoreKey, messageId);
            if (json == null) {
                return false;
            }
            Message message = objectMapper.readValue(json, Message.class);
            message.setTimeout(timeout);
            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            Double score = zSetOperations.score(realQueueName, messageId);
            if (score != null) {
                double priority = message.getPriority() / 100;
                double newScore = Long.valueOf(System.currentTimeMillis() + timeout).doubleValue() + priority;
                boolean flag = zSetOperations.add(realQueueName,messageId, newScore);
                if (flag) {
                    json = objectMapper.writeValueAsString(message);
                    hashOperations.put(messageStoreKey, message.getId(), json);
                    return true;
                }
                return false;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Message get(String messageId) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String json = hashOperations.get(messageStoreKey, messageId);
        if (json == null) {
            return null;
        }
        Message msg;
        try {
            msg = objectMapper.readValue(json, Message.class);
            return msg;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public long size() {
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.zCard(realQueueName);
    }

    @Override
    public void clear() {
        redisTemplate.delete(realQueueName);
        redisTemplate.delete(unAckQueueName);
        redisTemplate.delete(messageStoreKey);

    }

    private String peekId() {
        try {
            if (nextAvailable.get()) {
                lock.lockInterruptibly();
                double max = Long.valueOf(System.currentTimeMillis() + this.maxTimeOut).doubleValue();
                ZSetOperations zSetOperations = redisTemplate.opsForZSet();
                Set<String> scanned = zSetOperations.rangeByScore(realQueueName, 0, max, 0 ,1);
                if (scanned.size() > 0) {
                    String messageId = scanned.toArray()[0].toString();
                    zSetOperations.remove(realQueueName, messageId);
                    setUnAckTimeout(messageId, unAckTime);
                    if (size() == 0) {
                        nextAvailable.set(false);
                    }
                    available.signal();
                    lock.unlock();
                    return messageId;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            available.signal();
            lock.unlock();
        }
        return null;
    }
}
