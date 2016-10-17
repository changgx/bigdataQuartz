package com.kunteng.dao;


import java.util.List;

/**
 * 
 * 类说明：Hbase查询接口
 * 
 * @author cyp
 * @date 2015年9月9日 下午12:13:45
 */
public interface HbaseDao {
	/**
	 * @Title routerStatusHbase
	 * @Description hbase中查询路由状态
	 * @author cyp
	 * @date 2015年9月9日 下午12:14:52
	 * @param mac
	 * @return String
	 */
	public String routerStatusHbase(String mac);

	/**
	 * @Title routerScanHbase
	 * @Description hbase中查询路由扫描到的mac
	 * @author cyp
	 * @date 2015年10月15日 下午6:15:10
	 * @param mac
	 * @return String
	 */
	public String routerScanHbase(String mac);


	/**
	 * 查询 给定天数以内的扫描到的所有的MAC
	 * @param days
	 * @return
	 */
	public List<String> getLatelyScanMac(final String days);

	public void updateRedisData(long statTime, long endTime, final boolean flag);
}
