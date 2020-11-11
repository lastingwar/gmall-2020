package com.lastingwar.gmall.publisher.service.impl;

import com.lastingwar.gmall.publisher.mapper.DauMapper;
import com.lastingwar.gmall.publisher.mapper.OrderMapper;
import com.lastingwar.gmall.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yhm
 * @create 2020-11-06 19:25
 */
@Service
public class PublisherServiceImpl implements PublisherService {

    @Autowired
    private DauMapper dauMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public int getDauTotal(String date) {

        Integer integer = dauMapper.selectDauTotal(date);

        return integer;
    }

    @Override
    public Map getDauHours(String date) {
        // 创建结果集合
        HashMap<String, Object> result = new HashMap<>();
        // 获取数据
        List<Map> maps = dauMapper.selectDauTotalHourMap(date);
        for (Map map : maps) {
            result.put(map.get("LH").toString(),map.get("CT"));
        }
        return result;
    }

    @Override
    public Double getOrderAmount(String date) {
        return orderMapper.selectOrderAmountTotal(date);
    }

    @Override
    public Map getOrderAmountHour(String date) {
        List<Map> mapList = orderMapper.selectOrderAmountHourMap(date);
        Map orderAmountHourMap=new HashMap();
        for (Map map : mapList) {
            orderAmountHourMap.put(map.get("CREATE_HOUR"), map.get("SUM_AMOUNT"));
        }
        return orderAmountHourMap;
    }

}
