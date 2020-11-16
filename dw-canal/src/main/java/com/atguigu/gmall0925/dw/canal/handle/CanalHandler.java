package com.atguigu.gmall0925.dw.canal.handle;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.google.common.base.CaseFormat;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.atguigu.gmall.dw.constant.GmallConstants;
import com.atguigu.gmall0925.dw.canal.util.MyKafkaSender;

import java.util.List;

public class CanalHandler {

    public static void handle(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDatasList) {
        //根据表名和事件类型 绝对处理规则
        if ("order_info".equals(tableName) && eventType == CanalEntry.EventType.INSERT && rowDatasList != null && rowDatasList.size() > 0) {
            //循环每一行
            for (CanalEntry.RowData rowData : rowDatasList) {
                //得到修改后的列集合
                List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();

                JSONObject jsonObject = new JSONObject(); //用于把mysql变化的数据封装为json字符串

                //循环每一列
                for (CanalEntry.Column column : afterColumnsList) {
                    System.out.println(column.getName() + ":" + column.getValue());

                    //把数据送入到kafka中
                    //利用工具把mysql中的字段名变为驼峰规则
                    String propertyName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, column.getName());

                    jsonObject.put(propertyName,column.getValue());

                }

                //发送到kafka的 GMALL_ORDER主题
                MyKafkaSender.send(GmallConstants.KAFKA_TOPIC_ORDER,jsonObject.toJSONString());




            }

        }

    }

}
