package com.atguigu.gmall0925.dw.logger.controller;


import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggerController {

    private static final  org.slf4j.Logger logger = LoggerFactory.getLogger(LoggerController.class) ;

    @PostMapping("log")
    public void shipLog(@RequestParam("log") String log){
        logger.info(log); //落盘格式参考log4j.properties文件
    }
}
