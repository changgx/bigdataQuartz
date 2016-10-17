package com.kunteng.util;

import net.sf.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * 
 * 类说明：时间工具类
 * 
 * @author cyp
 * @date 2015年9月9日 下午12:19:05
 */
public class DateUtil {
	/**
	 * @Title getBeforeHour
	 * @Description 当前时间的前一小时
	 * @author cyp
	 * @date 2015年9月9日 下午12:19:16
	 * @return String
	 */
	public static String getBeforeHour() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
		String NowDate = sdf.format(new Date().getTime() - 60 * 60 * 1000);
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// String NowDate = sdf.format(new Date().getTime());
		return NowDate;
	}

	/**
	 * @Title getLongTime
	 * @Description 日期转时间戳
	 * @author cyp
	 * @date 2015年10月15日 下午6:00:51
	 * @param date
	 * @param style
	 * @return long
	 */
	public static long getLongTime(String date, String style) {
		if(null==date || date.length()<13){
			return 0L;
		}
		SimpleDateFormat sdf = null;
		if ("hour".equals(style)) {
			sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
		} else if ("minu".equals(style)) {
			sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		}

		long longTime = 0L;
		try {
			longTime = sdf.parse(date).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return longTime;
	}

	/**
	 * @Title parseLongTime
	 * @Description 时间戳转日期
	 * @author cyp
	 * @date 2015年9月9日 下午12:19:43
	 * @param longTime
	 * @return String
	 */
	public static String parseLongTime(long longTime, String style) {
		SimpleDateFormat sdf = null;
		if ("hour".equals(style)) {
			sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
		} else if ("minu".equals(style)) {
			sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
		}
		String NowDate = sdf.format(longTime);
		return NowDate;
	}

	public static String formatWay(String date) {
		// 2015-10-19-07-03 >> 2015/10/19 07:03
		String[] s = date.split("-");
		StringBuilder formatDate = new StringBuilder();
		formatDate.append(s[0]).append("/").append(s[1]).append("/")
				.append(s[2]);
		StringBuilder formatTime = new StringBuilder();
		formatTime.append(s[3]).append(":").append(s[4]);
		return formatDate.toString() + " " + formatTime.toString();
	}

	public static void main(String[] args) {
		HashMap<String,String> map=new HashMap();
		map.put("1","changgx01");
		map.put("2","changgx02");
		JSONArray jsonArray=JSONArray.fromObject(map);
		System.out.println(jsonArray);
	}

	/**
	 * 获取昨天日期
	 * @return
	 */
	public static String getYesterdayTime(){
		Date d=new Date(System.currentTimeMillis()-1000*60*60*24);
		SimpleDateFormat sp=new SimpleDateFormat("yyyy-MM-dd");
		 return sp.format(d);//获取昨天日期
	}
}
