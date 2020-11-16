package com.atguigu.gmall0925.dw.canal.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.atguigu.gmall0925.dw.canal.handle.CanalHandler;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;

public class CanalClient {
    public static void main(String[] args) {
        //获取canal客户端连接，用户名和密码都没设
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop102", 11111),
                "example", "", "");

        while (true){
            canalConnector.connect();
            canalConnector.subscribe("gmall0925.order_info");

            //获取100个动作，相当于100个sql
            Message message = canalConnector.get(100);

            int size = message.getEntries().size();
            //没有数据休眠5秒
            if(size==0){
                System.out.println("没有数据");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //循环处理每个sql命令
            for (CanalEntry.Entry entry : message.getEntries()) {
                //只处理ROWDATA类型的命令
                if(entry.getEntryType().equals(CanalEntry.EntryType.ROWDATA)){
                    String tableName = entry.getHeader().getTableName(); //获取表名
                    ByteString storeValue = entry.getStoreValue(); //获取数据（序列化）

                    CanalEntry.RowChange rowChange = null;
                    try {
                        //反序列化该值，为一个结构化对象
                        rowChange = CanalEntry.RowChange.parseFrom(storeValue);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }


                    CanalEntry.EventType eventType = rowChange.getEventType();//事件类型
                    List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList(); //得到多行集合

                    //调用方法
                    CanalHandler.handle(tableName,eventType,rowDatasList);

                }

            }

        }


    }

}
