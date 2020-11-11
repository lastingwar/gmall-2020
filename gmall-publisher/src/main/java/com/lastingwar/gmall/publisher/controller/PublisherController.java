package com.lastingwar.gmall.publisher.controller;


import com.alibaba.fastjson.JSONObject;
import com.lastingwar.gmall.publisher.service.PublisherService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yhm
 * @create 2020-11-06 19:20
 */
@RestController
public class PublisherController {
    @Autowired
    private PublisherService publisherService;

    @RequestMapping("realtime-total")
    public String getDauTotal(@RequestParam("date") String date) {
        // 1 创建集合存放结果
        ArrayList<Map> result = new ArrayList<>();


        // 2 获取日活
        int dauTotal = publisherService.getDauTotal(date);

        // 3 封装数据
        HashMap<String, Object> daumap = new HashMap<>();
        daumap.put("id", "dau");
        daumap.put("name", "新增日活");
        daumap.put("value", dauTotal);
        result.add(daumap);

        HashMap<String, Object> midmap = new HashMap<>();
        midmap.put("id", "new_mid");
        midmap.put("name", "新增设备");
        midmap.put("value", 233);
        result.add(midmap);


        Map orderAmountMap = new HashMap();
        orderAmountMap.put("id", "order_amount");
        orderAmountMap.put("name", "新增交易额");
        Double orderAmount = publisherService.getOrderAmount(date);
        orderAmountMap.put("value", orderAmount);
        result.add(orderAmountMap);

        System.out.println(result);
        return JSONObject.toJSONString(result);
    }

    @RequestMapping("realtime-hours")
    public String getDauHours(@RequestParam("id") String id,
                              @RequestParam("date") String date) {
        // 创建一个map接收两天的数据
        HashMap<String, Map> result = new HashMap<>();

        String yesterday = LocalDate.parse(date).plusDays(-1).toString();

        Map todayMap = null;
        Map yesterdayMap = null;

        if ("dau".equals(id)) {
            // 获取今天数据
            todayMap = publisherService.getDauHours(date);

            // 获取昨天数据
            yesterdayMap = publisherService.getDauHours(yesterday);

        } else if ("order_amount".equals(id)) {

            // 获取今天交易额
            todayMap = publisherService.getOrderAmountHour(date);
            // 获取昨天交易额
            yesterdayMap = publisherService.getOrderAmountHour(yesterday);

        }
        result.put("today",todayMap);
        result.put("yesterday",yesterdayMap);

        return JSONObject.toJSONString(result);
    }
}
