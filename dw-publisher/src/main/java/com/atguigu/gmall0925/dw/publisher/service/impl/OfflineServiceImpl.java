package com.atguigu.gmall0925.dw.publisher.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0925.dw.publisher.bean.RegionTop3Sku;
import com.atguigu.gmall0925.dw.publisher.mapper.RegionTop3skuMapper;
import com.atguigu.gmall0925.dw.publisher.service.OfflineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class OfflineServiceImpl implements OfflineService {

    @Autowired
    RegionTop3skuMapper regionTop3skuMapper;//利用通用mapper

    @Override
    public List<RegionTop3Sku> getRegionTop3SkuList(String date) {
        //查询全部
        List<RegionTop3Sku> regionTop3SkuAllList = regionTop3skuMapper.selectAll();
        System.out.println("全部：" + JSON.toJSONString(regionTop3SkuAllList));

        //根据条件进行查询
        RegionTop3Sku regionTop3SkuQuery = new RegionTop3Sku();
        regionTop3SkuQuery.setDt(date); //设置条件
        List<RegionTop3Sku> regionTop3SkuQueryList = regionTop3skuMapper.select(regionTop3SkuQuery);
        // RegionTop3Sku regionTop3Sku = regionTop3SkuMapper.selectOne(regionTop3SkuQuery); //如果返回值不止一条会报错
        System.out.println("条件：" + JSON.toJSONString(regionTop3SkuQueryList));


        //较为复杂的查询，模糊查询
        Example example = new Example(RegionTop3Sku.class);
        example.createCriteria().andLike("skuName", "%小米%");
        List<RegionTop3Sku> regionTop3SkusExampleList = regionTop3skuMapper.selectByExample(example);
        System.out.println("小米：" + JSON.toJSONString(regionTop3SkusExampleList));


        return regionTop3SkuQueryList;
    }
}
