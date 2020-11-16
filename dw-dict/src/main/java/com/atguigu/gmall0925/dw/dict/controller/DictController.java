package com.atguigu.gmall0925.dw.dict.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@RestController
public class DictController {

    //elasticsearch-6.6.0/plugins/ik/config/IKAnalyzer.cfg.xml 自己配置的外部地址
    @GetMapping("dict")
    public String dict(HttpServletResponse response){
        StringBuilder dict = new StringBuilder();

        //此处应该查询数据库  获得分词列表
        dict.append("蓝瘦香菇\n");
        dict.append("双卡双待\n");
        response.addHeader("Last-Modified",new Date().toString());

        return dict.toString();
    }
}
