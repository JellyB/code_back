package com.huatu.tiku.springboot.redis.queue.core;

import com.huatu.tiku.springboot.redis.queue.support.RejectException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * TODO 阻塞or轮询
 *
 * @author hanchao
 * @date 2017/10/31 16:45
 */
@Slf4j
public class SimpleRedisQueueListenerContainer implements QueueListenerContainer, ApplicationContextAware, InitializingBean {
    private volatile boolean running = true;
    private final ThreadLocal<Jedis> JEDIS_HOLDER;
    private ApplicationContext applicationContext;
    private List<RedisQueueListener> listeners = new CopyOnWriteArrayList<>();
    private Map<RedisQueueListener, Thread> workers = new ConcurrentHashMap<>();

    private JedisPool jedisPool;

    public SimpleRedisQueueListenerContainer(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        JEDIS_HOLDER = ThreadLocal.withInitial(() -> jedisPool.getResource());
    }


    public void addListener(RedisQueueListener redisQueueListener) {
        synchronized (this) {
            listeners.add(redisQueueListener);
            Thread thread = new Thread(new ListenerThread(redisQueueListener));
            thread.setDaemon(true);
            thread.start();
            workers.put(redisQueueListener, thread);
        }
    }

    public void removeListener(RedisQueueListener redisQueueListener) {
        synchronized (this) {
            listeners.remove(redisQueueListener);
            Thread thread = workers.get(redisQueueListener);
            thread.interrupt();
        }
    }


    @PreDestroy
    public void destroy() {
        try {
            running = false;
            for (RedisQueueListener listener : listeners) {
                removeListener(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Collection<RedisQueueListener> listenerCollection = this.applicationContext.getBeansOfType(RedisQueueListener.class).values();
        if (CollectionUtils.isNotEmpty(listenerCollection)) {
            for (RedisQueueListener redisQueueListener : listenerCollection) {
                addListener(redisQueueListener);
            }
        }
    }


    /**
     *
     */
    class ListenerThread implements Runnable {
        private RedisQueueListener listener;

        public ListenerThread(RedisQueueListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            Jedis jedis = null;
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        jedis = JEDIS_HOLDER.get();
                        listen(jedis);
                    } catch (InterruptedException e) {
                        throw e;
                    } catch (Exception e) {
                        log.error("", e);
                        try {
                            Thread.sleep(1000);
                            JEDIS_HOLDER.set(jedisPool.getResource());
                            if (jedis != null) {
                                jedis.close();
                            }
                        } catch (Exception ie) {
                            log.error("return jedis error:", e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                log.warn("listener interrupted...");
            }
            if (jedis != null) {
                jedis.close();
            }
        }

        public void listen(Jedis jedis) throws InterruptedException {
            String message = jedis.rpop(listener.queue());
            if (message == null) {
                TimeUnit.SECONDS.sleep(1);
            } else {
                try {
                    listener.consume(message);
                } catch (RejectException e) {
                    //do nothing
                } catch (Exception e) {
                    //should drop the message,or put it to another queue
                    log.error("",e);
                }
            }
        }

    }


}
