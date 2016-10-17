package com.kunteng.dao;



import com.kunteng.annotation.MyBatisDao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/14.
 */
@MyBatisDao
@Component
public interface RouterDao {
    List<String> findActiveRouterList();
    void insertAvgTimeDay(Map map);
}
