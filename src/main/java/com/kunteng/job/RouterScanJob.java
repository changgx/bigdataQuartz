package com.kunteng.job;/**
 * Created by Administrator on 2016/10/14.
 */

import com.kunteng.service.RouterService;
import com.kunteng.util.DateUtil;
import com.kunteng.util.JedisSentinelTemplate;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Administrator 2016/10/14
 */
public class RouterScanJob {
    private static Logger log = Logger.getLogger(RouterScanJob.class);
    @Autowired
    private RouterService routerService;
    @Autowired
    private JedisSentinelTemplate redisSentinelTemplate;

    public RouterScanJob() {

    }


    public void routerScan() {
        log.info("===============" + new Date() + " job start====================");
        List<String> list = routerService.findActiveRouterList();
        log.info("===================list======================");
        log.info(list);
        log.info("===================list======================");
        String time = DateUtil.getYesterdayTime();
        for (int i = 0; i < list.size(); i++) {
            log.info("===============list.get(i)======================");
            log.info(list.get(i));
            log.info("===============list.get(i)======================");

            String channelId = redisSentinelTemplate.get("mac:" + list.get(i));
            log.info("===============channelId======================");
            log.info(channelId);
            log.info("===============channelId======================");

            Map<String, Object> map = routerService.getValueThread(list.get(i), time);
            log.info("===============map======================");
            log.info(map);
            log.info("===============map======================");
            Map<String, Integer> mapMacNew = new HashMap();
            Map<String, Integer> mapMacOld = new HashMap();
            routerService.dealInfo(JSONObject.fromObject(map).getString("data"), mapMacNew, mapMacOld, channelId);
            log.info("===============mapMacNew======================");
            log.info(mapMacNew);
            log.info("===============mapMacNew======================");
            log.info("===============mapMacOld======================");
            log.info(mapMacOld);
            log.info("===============mapMacOld======================");
            //插入新客户
            routerService.insertMap(mapMacNew, list.get(i), time, 1);
            //插入老客户
            routerService.insertMap(mapMacOld, list.get(i), time, 0);
        }

    }
}
