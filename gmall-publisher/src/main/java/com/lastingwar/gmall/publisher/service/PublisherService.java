package com.lastingwar.gmall.publisher.service;

import java.io.IOException;
import java.util.Map;

/**
 * @author yhm
 * @create 2020-11-06 19:24
 */
public interface PublisherService {

    public int getDauTotal(String date);

    public Map getDauHours(String date);

    public Double getOrderAmount(String date);

    public Map getOrderAmountHour(String date);


    public String getSaleDetail(String date, Integer startpage, Integer size, String keyword) throws IOException;
}
