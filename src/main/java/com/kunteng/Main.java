package com.kunteng;

import com.kunteng.util.SpringContextUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Administrator on 2016/7/22.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        ApplicationContext act = new ClassPathXmlApplicationContext("classpath:/context/application-context.xml");
    }
}
