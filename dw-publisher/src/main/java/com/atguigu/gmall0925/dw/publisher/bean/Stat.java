package com.atguigu.gmall0925.dw.publisher.bean;

import com.atguigu.gmall0925.dw.publisher.Option;

import java.util.List;

public class Stat {

    String title;
    List<Option> option;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Option> getOption() {
        return option;
    }

    public void setOption(List<Option> option) {
        this.option = option;
    }
}
