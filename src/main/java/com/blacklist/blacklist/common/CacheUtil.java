package com.blacklist.blacklist.common;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Component
public class CacheUtil implements InitializingBean{

    private static CacheUtil instance;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public static CacheUtil getInstance() {
        return instance;
    }

    /**
     *
     * @param ip ip地址
     * @param url 当前访问地址
     * @param ttl 过期时间
     * @return
     */
    public boolean put(String ip, String url, long ttl) {
        if(!StringUtils.isEmpty(ip) ) {
            String key = ip + "|" + url;
            //先判断ip是否有访问过
            Integer times  = (Integer) this.redisTemplate.opsForValue().get(key);
            if (times != null) {
                //如果存在ip，则获取剩余的过期时间
                Long expire = this.redisTemplate.getExpire(ip);
                if (expire > 0) {
                    //把ip插入剩余的过期时间
                    this.redisTemplate.opsForValue().set(key, times + 1, ttl - expire, TimeUnit.SECONDS);
                    return true;
                }
            }

            //ip没有访问过接口
            this.redisTemplate.opsForValue().set(key, 1, ttl, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    public Integer get(String ip, String url) {
        if(!StringUtils.isEmpty(ip)) {
            String key = ip + "|" + url;
            return (Integer) this.redisTemplate.opsForValue().get(key);
        }
        return null;
    }
}
