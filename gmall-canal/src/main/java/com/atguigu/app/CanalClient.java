package com.atguigu.app;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.atguigu.gmall.constant.GmallConstants;
import com.atguigu.utils.MyKafkaSender;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * canal处理数据
 * @author yhm
 * @create 2020-11-06 17:54
 */
public class CanalClient {
    public static void main(String[] args) {

        // 获取一个连接器
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop102", 11111)
                , "example", "", "");

        // 持续监控,并处理数据
        while (true){
            canalConnector.connect();
            //选择监控的表格
            canalConnector.subscribe("gmall2020.*");

            Message message = canalConnector.get(100);
            // 空数据判断休息
            if (message.getEntries().size() <= 0){
                System.out.println("没有数据,休息一会!");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 数据处理
            else {
                for (CanalEntry.Entry entry : message.getEntries()) {
                    // 如果entry类型是rowdata
                    if (CanalEntry.EntryType.ROWDATA.equals(entry.getEntryType())){


                        try {
                            // 获取表名
                            String tableName = entry.getHeader().getTableName();
                            // 获取序列化数据
                            ByteString storeValue = entry.getStoreValue();
                            // 使用反序列化工具反序列化
                            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(storeValue);
                            //获取事件类型
                            CanalEntry.EventType eventType = rowChange.getEventType();
                            // 获取数据集合
                            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                            
                            // 根据获取的表名,事件类型和数据进行处理
                            handle(tableName,eventType,rowDatasList);
                            
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }

    }

    /**
     * 根据获取的表名,事件类型和数据进行处理
     * @param tableName 表名
     * @param eventType 事件类型
     * @param rowDatasList 多行数据
     */
    private static void handle(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDatasList) {
        //对于订单表而言,只需要新增数据
        if ("order_info".equals(tableName) && CanalEntry.EventType.INSERT.equals(eventType)){

            for (CanalEntry.RowData rowData : rowDatasList) {
                //创建JSON对象,用于存放多个列的数据
                JSONObject jsonObject = new JSONObject();
                // 取出多列
                for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                    jsonObject.put(column.getName(),column.getValue());
                }
                System.out.println(jsonObject);
                //发送数据至Kafka,主题保存在common中
                MyKafkaSender.send(GmallConstants.GMALL_ORDER_INFO,jsonObject.toString());
            }
        }
    }
}
