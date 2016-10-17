package com.kunteng.impl;


import com.kunteng.dao.HbaseDao;
import com.kunteng.util.DateUtil;
import com.kunteng.util.JedisSentinelTemplate;
import com.kunteng.util.SpringContextUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * 
 * 类说明：Hbase查询
 * 
 * @author cyp
 * @date 2015年9月9日 下午12:11:31
 */
@Component("hbaseDao")
public class HbaseImpl implements HbaseDao {
	private static Logger log = Logger.getLogger(HbaseImpl.class);
	@Autowired
	private JedisSentinelTemplate redisTemplate;
	@Autowired
	private HbaseTemplate hbaseTemplate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kunteng.data.dao.HbaseDao#routerStatusHbase(java.lang.String)
	 */
	public String routerStatusHbase(String mac) {
		// TODO Auto-generated method stub
		String value = "";
		value = hbaseTemplate.get("macStatus", mac, "info", "value",
				new RowMapper<String>() {
					public String mapRow(Result result, int rowNum)
							throws Exception {
						// TODO Auto-generated method stub
						String v = "";
						for (Cell cell : result.rawCells()) {
							v = new String(CellUtil.cloneValue(cell));
							log.info("hbase查询到数据：" + v);
						}
						return v;
					}
				});
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.kunteng.data.dao.HbaseDao#routerAddressHbase(java.lang.String)
	 */
//	public List<RouterAddress> routerAddressHbase(String mac) {
//		// TODO Auto-generated method stub
//		return hbaseTemplate.getMaxVersion("usertracetest", mac, "info",
//				new RowMapper<List<RouterAddress>>() {
//			
//					public List<RouterAddress> mapRow(Result result, int rowNum)
//							throws Exception {
//						// TODO Auto-generated method stub
//						List<RouterAddress> hbaseFieldList = new ArrayList<RouterAddress>();
//						List<RouterAddress> list = new ArrayList<RouterAddress>();
//						for (Cell cell : result.rawCells()) {
//							RouterAddress ra = new RouterAddress();
//							if (new String(CellUtil.cloneQualifier(cell))
//									.equals("address")) {
//								ra.setAddress(new String(CellUtil
//										.cloneValue(cell)));
//							}
//							if (new String(CellUtil.cloneQualifier(cell))
//									.equals("longtime")) {
//								ra.setLongTime(new String(CellUtil
//										.cloneValue(cell)));
//							}
//							hbaseFieldList.add(ra);
//						}
//						int num = hbaseFieldList.size();
//						for (int i = 0; i < num / 2; i++) {
//							hbaseFieldList.get(i).setLongTime(
//									hbaseFieldList.get(i + num / 2)
//											.getLongTime());
//						}
//						for (int i = 0; i < num / 2; i++) {
//							list.add(hbaseFieldList.get(i));
//						}
//						return list;
//					}
//				});
//
//	}
	/*
	 * 
	 * (non-Javadoc)
	 * @see com.kunteng.data.dao.HbaseDao#routerScanHbase(java.lang.String)
	 */
	public String routerScanHbase(String mac) {
		// TODO Auto-generated method stu
		String value = "";
		value = hbaseTemplate.get("routerScan", mac, "info", "value",
				new RowMapper<String>() {
					public String mapRow(Result result, int rowNum)
							throws Exception {
						// TODO Auto-generated method stub
						String v = "";
						for (Cell cell : result.rawCells()) {
							v = new String(CellUtil.cloneValue(cell));
							log.info("hbase查询到数据：" + v);
						}
						return v;
					}
				});
		return value;
	}

	/**
	 * 查询 给定天数以内的扫描到的所有的MAC
	 * @param days
	 * @return
	 */
	public List<String> getLatelyScanMac(final String days){
		final List<String> resultList = new ArrayList<String>();
		hbaseTemplate.find("yunac_oldCustomer", new Scan(), new RowMapper<Object>() {
			public Object mapRow(Result result, int i) throws Exception {
				//log.info("ALL++++++++++++rowkey+++++++"+new String(result.getRow()));
				for (Cell cell : result.rawCells()) {
					log.debug("+++++++++++++++++++cell qualifier++++" + new String(CellUtil.cloneQualifier(cell)));
					if ((new String(CellUtil.cloneQualifier(cell)))
							.equals("time")) {

						long dbTime = DateUtil.getLongTime(
								new String(CellUtil.cloneValue(cell)), "hour");// 13位时间戳
						if(dbTime==0){// 日期数据异常
							return null;
						}
						log.debug("+++++++++++++++ time+++ in^_^"+new String(CellUtil.cloneValue(cell)));
						long now = System.currentTimeMillis();

						if (now - dbTime > Long.parseLong("180") * 24 * 60 * 60 * 1000) {
							// 超过半年时间，判断为新用户
							log.debug("+++++++++++++++in new user+++  ^_^");
						} else {
							// 不超过半年的，为老用户
							log.debug("++++++++++++rowkey+++++++" + new String(result.getRow()));
							//resultList.add(new String(result.getRow()));
							redisTemplate.hset("old_customer", new String(result.getRow()), "1");

						}
					}
				}

				return null;
			}
		});
		log.info("===================================================");
		log.info("=========old_customer  end"+new Date()+"===========");
		log.info("===================================================");
		return resultList;
	}

	/**
	 * 更新redis中老顧客的信息
	 * 1.從hbase中查詢出半年前的無效數據
	 * 2.從redis中刪除無效數據
	 * 3.從hbase中查詢去昨天的新增數據
	 * 4.向redis中新增3中的數據
	 */
	public void updateRedisData(long startTime, long endTime,  final boolean flag){
		log.info(startTime);
		log.info(endTime);
        Scan scan=new Scan();
        try {
			scan.setTimeRange(startTime,endTime);
			hbaseTemplate.find("yunac_oldCustomer", scan,new RowMapper<String>(){
				public String mapRow(Result result, int rowNum) throws Exception {
					log.info(new String(result.getRow()));
					if(flag){
						//新增
						redisTemplate.hset("old_customer",new String(result.getRow()),"1");
					}else {
						//刪除
						redisTemplate.hdel("old_customer",new String(result.getRow()));
					}
					return null;
				}
			});
		} catch (IOException e) {
            e.printStackTrace();
        }

    }






}
