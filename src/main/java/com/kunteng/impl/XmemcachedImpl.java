package com.kunteng.impl;


import com.kunteng.dao.XmemcachedDao;
import com.kunteng.util.SpringContextUtil;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeoutException;

/**
 * 
 * 类说明：memcache接口实现查询和插入数据
 * 
 * @author cyp
 * @date 2015年9月9日 下午12:17:11
 */

@Component("mcDao")
public class XmemcachedImpl implements XmemcachedDao {
	private static Logger log = Logger.getLogger(XmemcachedImpl.class);
	@Autowired
	private MemcachedClient memcachedClient;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kunteng.data.dao.XmemcachedDao#getRouterStatusValue(java.lang.String)
	 */
	public char[] getStatusValue(String key) {
		// TODO Auto-generated method stub
		char record[] = null;
		try {
			Object recordValue = memcachedClient.get(key);
			record = (char[]) recordValue;
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			log.error("memcache时间超时："+e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (MemcachedException e) {
			// TODO Auto-generated catch block
			log.error(e);
		}
		return record;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.kunteng.data.dao.XmemcachedDao#setRouterStatusValue(java.lang.String,
	 * char[])
	 */
	public void setValue(String key, Object value) {
		// TODO Auto-generated method stub
		try {
			memcachedClient.set(key, 0, value);
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			log.error("memcache时间超时："+e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (MemcachedException e) {
			// TODO Auto-generated catch block
			log.error(e);
		}
	}
	/*
	 * (non-Javadoc)
	 * @see com.kunteng.data.dao.XmemcachedDao#getStringValue(java.lang.String)
	 */
	public String getStringValue(String key) {
		// TODO Auto-generated method stub
		String value = null;
		try {
			Object recordValue = memcachedClient.get(key);
			value = (String) recordValue;
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			log.error("memcache时间超时："+e);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			log.error(e);
		} catch (MemcachedException e) {
			// TODO Auto-generated catch block
			log.error(e);
		}
		return value;
	}
}
