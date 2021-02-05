package com.wang.seckill.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 漏桶算法
 */
@Slf4j
public class LeakyBucket {

    /**
     * 漏桶输出的速率
     */
    private final double rate;

    /**
     * 桶的容量
     */
    private final double capacity;

    /**
     * 桶中现在有的水量
     */
    private int storage;

    /**
     * 上一次刷新时间的时间戳
     */
    private long refreshTime;

    public LeakyBucket(double rate, double capacity) {
        this.rate = rate;
        this.capacity = capacity;
        this.storage = 0;
        this.refreshTime = System.currentTimeMillis();
    }

    /**
     * 每次请求都会刷新桶中水的存储量
     */
    private void refreshStorage() {
        // 获取当前的时间戳
        long nowTime = System.currentTimeMillis();
        // 想象成水以一定的速率流出 但是如果加水的速度过快（高并发），水就满出来了
        storage = (int) Math.max(0, storage - (nowTime - refreshTime) * rate);
        refreshTime = nowTime;
    }

    /**
     * 请求是否进入桶（该请求是否有效）
     *
     * @return true代表请求有效 反之无效
     */
    public synchronized boolean enterBucket() {
        refreshStorage();
        log.info("桶的存储量 = {}", storage);
        // 桶内水的存储量小于桶的容量则成功请求并将桶内水的容量加一
        if (storage < capacity) {
            storage++;
            return true;
        } else {
            return false;
        }
    }
}