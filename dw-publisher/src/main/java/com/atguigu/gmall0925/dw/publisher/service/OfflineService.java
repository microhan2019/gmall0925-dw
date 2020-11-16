package com.atguigu.gmall0925.dw.publisher.service;

import com.atguigu.gmall0925.dw.publisher.bean.RegionTop3Sku;

import java.util.List;

public interface OfflineService {

    //根据日期 查询 各地区热门商品全部集合
    public List<RegionTop3Sku> getRegionTop3SkuList(String date);
}
