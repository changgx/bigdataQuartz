package com.kunteng.service;/**
 * Created by Administrator on 2016/10/14.
 */

import com.kunteng.bean.RouterScan;
import com.kunteng.dao.HbaseDao;
import com.kunteng.dao.RouterDao;
import com.kunteng.dao.XmemcachedDao;
import com.kunteng.util.DateUtil;
import com.kunteng.util.JedisSentinelTemplate;
import com.kunteng.util.SpringContextUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Administrator 2016/10/14
 */
@Service("routerService")
public class RouterService {
    private static Logger log = Logger.getLogger(RouterService.class);
    @Autowired
    private HbaseDao hbaseDao;
    @Autowired
    private XmemcachedDao mcDao;
    @Autowired
    private JedisSentinelTemplate redisTemplate;
    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<String> findActiveRouterList() {
        String sql=" SELECT mac FROM router where router.status <>-1 limit 0,10";
        return jdbcTemplate.queryForList(sql,String.class);
//        return routerDao.findActiveRouterList();
    }

    public void insertAvgTimeDay(Map map) {
        String sql="replace into router_avg_time_day (router,date,new_flag,avgtime,visitor_num,tenmin,thirtymin,onehour,twohour,other) " +
                "   VALUES (?,?,?,?,?,?,?,?,?,?)";

        String[] params=new String[10];
        params[0]=map.get("router").toString();
        params[1]=map.get("date").toString();
        params[2]=map.get("newFlag").toString();
        params[3]=map.get("avgtime").toString();
        params[4]=map.get("visitorNum").toString();
        params[5]=map.get("tenmin").toString();
        params[6]=map.get("thirtymin").toString();
        params[7]=map.get("onehour").toString();
        params[8]=map.get("twohour").toString();
        params[9]=map.get("other").toString();
        jdbcTemplate.update(sql,params);
        //routerDao.insertAvgTimeDay(map);
    }
    public Map<String, Object> getValueThread(final String mac, String time) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (time.contains("-") && (time.length() == 13 || time.length() == 10)
                && mac.length() == 12) {
            // 2015-10-20-10-00
            long before = 0L;
            long after = 0L;
            if (time.length() == 10) {
                before = DateUtil.getLongTime(time + "-00-00", "minu");
                after = DateUtil.getLongTime(time + "-23-59", "minu");
            }

            ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
            final List<RouterScan> list = new ArrayList<RouterScan>();
            final List<Future<String>> resultList = new ArrayList<Future<String>>();
            for (int i = 0; i < 24; i++) {
                final long startTime = before + i * 3600 * 1000;
                final long endTime = before + (i + 1) * 3600 * 1000;
                final Future<String> future = cachedThreadPool.submit(new Callable<String>() {
                    public String call() throws Exception {
                        for (long i = startTime; i < endTime; ) {
                            String scanTime = DateUtil.parseLongTime(i, "minu");
                            // 转换成2015/10/19 07:03这种格式
                            String formatScanTime = DateUtil.formatWay(scanTime);
                            String key = "scan-" + scanTime + "-" + mac;
                            String routerScan = (String) mcDao.getStringValue(key);
                            RouterScan rc = new RouterScan();
                            // memcached中没有查询到数据
                            if (routerScan == null || "".equals(routerScan)) {
                                String value = hbaseDao.routerScanHbase(key);
                                // hbase中也没有查询到数据
                                if ("".equals(value) || value == null) {
                                    rc.setScanMac("");
                                    rc.setScanTime(formatScanTime);
                                } else {
                                    // hbase中查询出数据放入memcache中
                                    mcDao.setValue(key, value);
                                    rc.setScanMac(value);
                                    rc.setScanTime(formatScanTime);
                                }
                            } else {
                                rc.setScanMac(routerScan);
                                rc.setScanTime(formatScanTime);
                            }
                            list.add(rc);
                            // 时间点以每分钟记录
                            i = i + 60 * 1000;
                        }
                        return "";
                    }
                });
                resultList.add(future);
            }
            //遍历任务的结果
            for (Future<String> fs : resultList) {
                try {
                    while (!fs.isDone()) ;//Future返回如果没有完成，则一直循环等待，直到Future返回完成
                    System.out.println(fs.get());     //打印各个线程（任务）执行的结果
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    //启动一次顺序关闭，执行以前提交的任务，但不接受新任务
                    cachedThreadPool.shutdown();
                }
            }
            System.out.println("主线程" + list.size());
            map.put("status", 0);
            map.put("data", list);

        } else {
            map.put("status", -1);
            map.put("error_code", 20002);
            map.put("error_msg", "查询参数错误");
        }
        return map;
    }
    public void dealInfo(String data, Map<String, Integer> mapNew, Map<String, Integer> mapOld, String channelId) {
        Map<String, Integer> map = new HashMap();
        JSONArray jsonArray = JSONArray.fromObject(data);
        log.info("=============jsonArray.size()============");
        log.info(jsonArray.size());
        log.info("==============jsonArray.size()===========");
        String scanMac = "";
        for (int i = 0; i < jsonArray.size(); i++) {
            log.info("============jsonArray.get(i)=============");
            log.info(jsonArray.get(i));
            log.info("=============jsonArray.get(i)============");
            scanMac = (String) jsonArray.getJSONObject(i).get("scanMac");
            log.info("============scanMac=============");
            log.info(scanMac);
            log.info("============scanMac=============");
            String[] a = scanMac.split("\\|");
            if (!"".equals(a[0])) {
                for (int j = 0; j < a.length; j++) {
                    if (map.get(a[j]) != null) {
                        map.put(a[j], map.get(a[j]) + 1);
                    } else {
                        map.put(a[j], 1);
                    }

                }
            }
        }


        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();
            if (value > 8) {
                String macId = key + "-" + channelId;
                String results = redisTemplate.hget("old_customer", macId);
                if ("1".equals(results)) {
                    //老顾客
                    mapOld.put(key, value);
                } else {
                    //新顾客
                    mapNew.put(key, value);
                }
            }
        }
        log.info("===============new Map=========================");
        log.info(mapNew);
        log.info("===============old Map=========================");
        log.info(mapOld);

    }


    //插入 驻留时间人数统计
    public void insertMap(Map<String, Integer> map, String mac, String time, Integer newflag) {
        int luren = 0;
        int tenmin = 0;
        int thirtymin = 0;
        int onehour = 0;
        int twohour = 0;
        int other = 0;
        int totalTime = 0;
        int visitorNum = 0;

        for (String key : map.keySet()) {
            int value = map.get(key);
            if (value > 0 && value < 8) {
                luren++;
            } else if (value >= 8 && value < 10) {
                tenmin++;
                totalTime += value;
            } else if (value >= 10 && value < 30) {
                thirtymin++;
                totalTime += value;
            } else if (value >= 30 && value < 60) {
                onehour++;
                totalTime += value;
            } else if (value >= 60 && value < 120) {
                twohour++;
                totalTime += value;
            } else if (value >= 120) {
                other++;
                totalTime += value;
            } else {

            }
        }
        float avgtime = 0;
        try {
            if (luren < map.size()) {
                visitorNum = map.size() - luren;
                DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                String p = decimalFormat.format((float) totalTime / (map.size() - luren));//format 返回的是字符串
                avgtime = Float.parseFloat(p);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //插入router每天统计的平均驻留时间
        Map mapinsert = new HashMap();
        mapinsert.put("router", mac);
        mapinsert.put("date", time);
        mapinsert.put("newFlag", newflag);
        mapinsert.put("avgtime", avgtime);
        mapinsert.put("visitorNum", visitorNum);
        mapinsert.put("tenmin", tenmin);
        mapinsert.put("thirtymin", thirtymin);
        mapinsert.put("onehour", onehour);
        mapinsert.put("twohour", twohour);
        mapinsert.put("other", other);
        try {
            this.insertAvgTimeDay(mapinsert);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
