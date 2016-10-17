package com.kunteng.dao;
/**
 * 
 * 类说明：memcache接口
 * 
 * @author cyp
 * @date 2015年9月9日 下午12:16:17
 */

public interface XmemcachedDao {
	/**
	 * @Title getRouterStatusValue
	 * @Description 在memcached中获取字符数组数据
	 * @author cyp
	 * @date 2015年9月9日 下午12:15:42
	 * @param key
	 * @return char[]
	 */
	public char[] getStatusValue(String key);
	/**
	 * @Title getStringValue
	 * @Description 在memcached中获取字符串数据
	 * @author cyp
	 * @date 2015年10月16日 上午11:14:51
	 * @param key
	 * @return String
	 */
	public String getStringValue(String key);

	/**
	 * @Title setRouterStatusValue
	 * @Description 向memcached中插入值
	 * @author cyp
	 * @date 2015年9月9日 下午12:15:54
	 * @param key
	 * @param value
	 * @return void
	 */
	public void setValue(String key, Object value);
	
	
}
