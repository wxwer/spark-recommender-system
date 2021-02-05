package com.wang.seckill.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.api.R;
import com.wang.seckill.service.KillService;

@RestController
@RequestMapping("/seckill/kill")
@Slf4j
public class SecKillController {

    @Autowired
    private KillService killService;
    
    private static int count = 0;
    /**
     * Alibaba Sentinel限流+redis预热库存+分布式锁+kafka异步生成订单
     *
     * @param userId  用户Id
     * @param goodsId 商品Id
     * @return 秒杀成功则返回订单编号，失败则返回null
     */
    @RequestMapping("/distrubuteLockSecSkill")
    public R distrubuteLockSecSkill(@RequestParam("sessId") Integer sessId, @RequestParam("gid") Integer goodsId,@RequestParam("code") String code) {
        try {
            count++;
            log.info("请求的次数 = {}", count);
            String killId = sessId+"-"+goodsId;
            Long t1 =System.currentTimeMillis();
            String oderNum = killService.kill(killId, code,1);
            Long t2 =System.currentTimeMillis();
            System.out.println("cost time: "+(t2-t1));
            if(!StringUtils.isEmpty(oderNum)) {
            	return R.ok("抢购成功，生成的订单号为："+oderNum);
            }
            else {
            	return R.failed("抢购失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.failed(e.getMessage());
        }
    }
}

