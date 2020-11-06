package com.atguigu.gmall.constant;

/**
 * @author yhm
 * @create 2020-11-04 10:49
 */
public class GmallConstants {

    public static final String KAFKA_TOPIC_STARTUP="GMALL_STARTUP";
    public static final String KAFKA_TOPIC_EVENT="GMALL_EVENT";

    //启动日志主题
    public static final String GMALL_STARTUP = "TOPIC_START";

    //事件日志主题
    public static final String GMALL_EVENT = "TOPIC_EVENT";

    //订单表日志主题
    public static final String GMALL_ORDER_INFO = "TOPIC_ORDER_INFO";

    public static final String KAFKA_TOPIC_NEW_ORDER="GMALL_NEW_ORDER";
    public static final String KAFKA_TOPIC_ORDER_DETAIL="GMALL_ORDER_DETAIL";

    public static final String ES_INDEX_DAU="gmall2020_dau";
    public static final String ES_INDEX_NEW_MID="gmall2020_new_mid";
    public static final String ES_INDEX_NEW_ORDER="gmall2020_new_order";
    public static final String ES_INDEX_SALE_DETAIL="gmall2020_sale_detail";

}

