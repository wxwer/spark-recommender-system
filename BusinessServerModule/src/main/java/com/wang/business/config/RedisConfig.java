package com.wang.business.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {
	private String redisHost;
    private Integer port;
    private Integer database;
    private String password;
    
    public RedisConfig() {
        try {
            Properties properties = new Properties();
            Resource resource = new ClassPathResource("recommend.properties");
            properties.load(new FileInputStream(resource.getFile()));
            this.redisHost = properties.getProperty("redis.host");
            this.port = Integer.valueOf(properties.getProperty("redis.port"));
            this.database = Integer.valueOf(properties.getProperty("redis.database"));
            this.password = properties.getProperty("redis.password");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    @Bean(name = "jedis")
    public Jedis getRedisClient() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(100);
        config.setMaxIdle(50);
        config.setMinIdle(10);
        //设置连接时的最大等待毫秒数
        config.setMaxWaitMillis(10000);
        //设置在获取连接时，是否检查连接的有效性
        config.setTestOnBorrow(true);
        //设置释放连接到池中时是否检查有效性
        config.setTestOnReturn(true);

        //在连接空闲时，是否检查连接有效性
        config.setTestWhileIdle(true);

        //两次扫描之间的时间间隔毫秒数
        config.setTimeBetweenEvictionRunsMillis(30000);
        //每次扫描的最多的对象数
        config.setNumTestsPerEvictionRun(10);
        //逐出连接的最小空闲时间，默认是180000（30分钟）
        config.setMinEvictableIdleTimeMillis(60000);
        Jedis resource = new JedisPool(config, redisHost, port, 30000, password, database).getResource();
        return resource;
    }
}
