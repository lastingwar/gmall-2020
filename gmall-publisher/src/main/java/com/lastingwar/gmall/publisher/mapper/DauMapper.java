package com.lastingwar.gmall.publisher.mapper;

import java.util.List;
import java.util.Map;

/**
 * @author yhm
 * @create 2020-11-06 19:26
 */
public interface DauMapper {


    public Integer selectDauTotal(String date);

    public List<Map> selectDauTotalHourMap(String date);

}
