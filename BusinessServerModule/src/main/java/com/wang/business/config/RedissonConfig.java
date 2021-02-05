package com.wang.business.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://39.99.247.99:6379").setPassword("zSbatech*1m3");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
