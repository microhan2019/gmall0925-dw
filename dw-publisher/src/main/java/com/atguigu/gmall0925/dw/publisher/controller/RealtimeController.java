package com.atguigu.gmall0925.dw.publisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0925.dw.publisher.Option;
import com.atguigu.gmall0925.dw.publisher.bean.Stat;
import com.atguigu.gmall0925.dw.publisher.service.RealtimeService;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class RealtimeController {

    @Autowired
    RealtimeService realtimeService;

    @GetMapping("realtime-total")
    public String getRealtimeTotal(@RequestParam("date") String date) {
        List totalList = new ArrayList<Map>();

        //日活总数
        int dauTotal = realtimeService.getDauTotal(date);
        Map dauMap = new HashMap();
        dauMap.put("id", "dau");
        dauMap.put("name", "新增日活");
        dauMap.put("value", dauTotal);
        totalList.add(dauMap);

        // 新增用户,这个没有，可以增加的
        Map newMidMap = new HashMap<String, Object>();
        newMidMap.put("id", "new_mid");
        newMidMap.put("name", "新增用户");
        newMidMap.put("value", 3000);
        totalList.add(newMidMap);


        // 新增订单收入
        Map orderTotalAmount = new HashMap<String, Object>();
        orderTotalAmount.put("id", "order_amount");
        orderTotalAmount.put("name", "新增收入");
        orderTotalAmount.put("value", realtimeService.getOrderTotalAmount(date));
        totalList.add(orderTotalAmount);


        return JSON.toJSONString(totalList);
    }


    @GetMapping("realtime-hour")
    public String getRealtimeHour(@RequestParam("id") String id, @RequestParam("date") String dateSting) {
        //说明要查询的是日活
        if ("dau".equals(id)) {

            Map hoursMap = new HashMap();

            //求今天的
            Map dauHoursToday = realtimeService.getDauHours(dateSting);
            hoursMap.put("today", dauHoursToday);

            //求昨天的
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd").parse(dateSting);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date yesterday = DateUtils.addDays(date, -1); //工具类
            String yesterdayString = new SimpleDateFormat("yyyy-MM-dd").format(yesterday);

            Map dauHoursYesterday = realtimeService.getDauHours(yesterdayString);
            hoursMap.put("yesterday", dauHoursYesterday);


            return JSON.toJSONString(hoursMap);

            //说明要查询的是订单分时收入金额
        } else if ("order_amount".equals(id)) {
            Map hoursMap = new HashMap();

            //求今天的
            Map<String, Double> orderTotalAmountHoursTd = realtimeService.getOrderTotalAmountHours(dateSting);
            hoursMap.put("today", orderTotalAmountHoursTd);

            //求昨天的
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd").parse(dateSting);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date yesterday = DateUtils.addDays(date, -1); //工具类
            String yesterdayString = new SimpleDateFormat("yyyy-MM-dd").format(yesterday);
            Map<String, Double> orderTotalAmountHoursYd = realtimeService.getOrderTotalAmountHours(yesterdayString);
            hoursMap.put("yesterday", orderTotalAmountHoursYd);

            return JSON.toJSONString(hoursMap);

        } else {
            return null;
        }
    }


    //参数根据传入路径进行确定
    @GetMapping("sale_detail")
    public String getSaleDetail(@RequestParam("date") String dateStr, @RequestParam("keyword") String keyword,
                                @RequestParam("startpage") int startPage, @RequestParam("size") int pageSize) {

        //先求商品明细 和 男女比例
        Map saleDetail_Gender = realtimeService.getSaleDetail(dateStr, keyword, "user_gender", 2, startPage, pageSize);


        //计算男女比例
        HashMap aggsGender = (HashMap) saleDetail_Gender.get("aggs"); //聚合部分
        Integer total = (Integer) saleDetail_Gender.get("total"); //从聚合部分取出总人数
        System.out.println("total = " + total);

        Stat genderStat = new Stat();
        List<Option> genderList = new ArrayList<>();
        for (Object o : aggsGender.entrySet()) {
            Map.Entry entry = (Map.Entry) o;

            Option option = new Option(); //封装成对象

            if (entry.getKey().equals("F")) {
                option.setName("女");

            } else {
                option.setName("男");
            }
            option.setValue(Math.round((Long) entry.getValue() * 1000D / total) / 10D);

            genderList.add(option);

        }
        genderStat.setOption(genderList);
        genderStat.setTitle("用户性别占比");


        //计算各个年龄层占比
        Map saleDetail_Age = realtimeService.getSaleDetail(dateStr, keyword, "user_age", 100, startPage, pageSize);
        //取出聚合部分，每个年龄段
        int age_20 = 0;
        int age_20_30 = 0;
        int age_30 = 0;

        HashMap ageAggs = (HashMap) saleDetail_Age.get("aggs");
        for (Object o : ageAggs.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Integer age = Integer.parseInt((String) entry.getKey()); //每一个年龄
            Long count = (Long) entry.getValue(); //每一个年龄对应的人数

            if (age < 20) {
                age_20 += count;
            } else if (age >= 30) {
                age_30 += count;
            } else {
                age_20_30 += count;
            }
        }


        List<Option> ageOption = new ArrayList<>();
        Option age_20_option = new Option();
        age_20_option.setName("20岁以下");
        age_20_option.setValue((Math.round(age_20 * 1000D / total)) / 10d);

        Option age_20_30_option = new Option();
        age_20_30_option.setName("20岁到30岁");
        age_20_30_option.setValue((Math.round(age_20_30 * 1000D / total)) / 10d);

        Option age_30_option = new Option();
        age_30_option.setName("30岁以下");
        age_30_option.setValue((Math.round(age_30 * 1000D / total)) / 10d);

        ageOption.add(age_20_option);
        ageOption.add(age_20_30_option);
        ageOption.add(age_30_option);

        Stat ageStat = new Stat();
        ageStat.setTitle("用户年龄占比");
        ageStat.setOption(ageOption);

        //把两个聚合结果放到list中
        List<Stat> statList = new ArrayList<>();
        statList.add(genderStat);
        statList.add(ageStat);


        //都汇总到Map中
        Map saleMap = new HashMap();
        saleMap.put("total", total);
        saleMap.put("detail", saleDetail_Gender.get("detail"));
        saleMap.put("stat", statList);


        return JSON.toJSONString(saleMap);

    }

}
