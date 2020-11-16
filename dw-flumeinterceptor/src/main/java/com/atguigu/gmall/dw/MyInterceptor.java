package com.atguigu.gmall.dw;

import com.google.gson.Gson;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyInterceptor implements Interceptor {
    Gson gson = null;

    @Override
    public void initialize() {
        gson = new Gson();
    }

    //根据日志中的type值来给header设选择器标签
    @Override
    public Event intercept(Event event) {
        String logString = new String(event.getBody());

        if (logString != null && logString.length() > 0) {
            HashMap<String, String> hashMap = gson.fromJson(logString, HashMap.class);
            //获取日志的类型是启动日志还是事件日志
            String type = hashMap.get("type");

            //启动日志
            if (type.equals("startup")) {
                Map<String, String> headers = event.getHeaders();
                //channel选择器根据自定义的header头的值 logType进行过滤
                headers.put("logType", "startup");

                //事件日志
            } else if (type.equals("event")) {
                Map<String, String> headers = event.getHeaders();
                headers.put("logType", "event");
            }
        }

        return event;
    }

    @Override
    public List<Event> intercept(List<Event> list) {

        for (Event event : list) {
            intercept(event);

        }
        return list;
    }

    @Override
    public void close() {

    }


    /**
     * 通过该静态内部类来创建自定义对象供flume使用，
     * 实现Interceptor.Builder接口，并实现其抽象方法
     */
    public static class Builder implements Interceptor.Builder {

        /**
         * 该方法主要用来返回创建的自定义类拦截器对象
         *
         * @return
         */
        @Override
        public Interceptor build() {
            return new MyInterceptor();
        }

        @Override
        public void configure(Context context) {
            //可以通过context得到 flume.conf中设置的参数 ，传递给Interceptor

        }
    }


   /* 测试下
   public static void main(String[] args) {
        Gson gson = new Gson();
        String logString = "{\"area\":\"shanghai\",\"uid\":\"user_995\",\"os\":\"andriod\",\"appid\":\"app007\",\"mid\":\"88a147a9-ecc4-40be-885a-ae67bb9b7b2b\",\"type\":\"startup\",\"vs\":\"1.1.1\",\"ts\":1546780968866}";
        HashMap<String, String> hashMap = gson.fromJson(logString, HashMap.class);

        System.out.println(hashMap.get("type"));

    }
*/

}
