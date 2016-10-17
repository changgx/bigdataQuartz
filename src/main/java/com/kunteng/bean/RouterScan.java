package com.kunteng.bean;

/**
 * 
 * 类说明：路由器扫描到mac返回实体
 * 
 * @author cyp
 * @date 2015年9月24日 下午6:19:49
 */
public class RouterScan {
	private String scanMac;// 扫描到的设备mac
	private String scanTime;// 扫描到设备的时间
	//private String type;// 扫描到的设备类型
	//private String brand;// 扫描到的设备品牌

	public String getScanMac() {
		return scanMac;
	}

	public void setScanMac(String scanMac) {
		this.scanMac = scanMac;
	}

	public String getScanTime() {
		return scanTime;
	}

	public void setScanTime(String scanTime) {
		this.scanTime = scanTime;
	}
}
