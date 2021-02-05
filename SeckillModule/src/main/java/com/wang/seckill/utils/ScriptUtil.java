package com.wang.seckill.utils;

import java.io.IOException;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

//读取lua脚本的工具类
public class ScriptUtil {
    public static DefaultRedisScript<List> getRedisScript(String filename) throws IOException {
    	DefaultRedisScript<List> redisScript;
    	redisScript = new DefaultRedisScript<List>();
    	redisScript.setResultType(List.class);
    	redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(filename)));
        return redisScript;
    }
}
