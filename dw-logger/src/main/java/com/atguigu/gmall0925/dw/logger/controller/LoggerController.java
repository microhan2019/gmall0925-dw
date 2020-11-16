package com.atguigu.gmall0925.dw.logger.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.dw.constant.GmallConstants;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class LoggerController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LoggerController.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/log")
    public void shipLog(@RequestParam("log") String log) {

        /*
        实时计算：落盘之前把数据推送到Kafka
         */
        JSONObject jsonObject = JSON.parseObject(log);
        //给日期加上时间戳
        jsonObject.put("ts",
                System.currentTimeMillis() + new Random().nextInt(3600 * 1000 * 5));

        //启动日志，把日志推送到Kafka启动主题
        if("startup".equals(jsonObject.getString("type"))){
            kafkaTemplate.send(GmallConstants.KAFKA_TOPIC_STARTUP,
                    jsonObject.toJSONString());
        }else{
            kafkaTemplate.send(GmallConstants.KAFKA_TOPIC_EVENT,
                    jsonObject.toJSONString());
        }

        String logNew = jsonObject.toJSONString();

        logger.info(logNew); //落盘格式参考log4j.properties文件
    }
}
