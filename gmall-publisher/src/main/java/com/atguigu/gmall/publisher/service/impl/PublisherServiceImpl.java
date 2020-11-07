package com.atguigu.gmall.publisher.service.impl;

import com.atguigu.gmall.publisher.mapper.DauMapper;
import com.atguigu.gmall.publisher.service.PublisherService;
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
            result.put(map.get("lh").toString(),map.get("ct"));
        }
        return result;
    }
}
