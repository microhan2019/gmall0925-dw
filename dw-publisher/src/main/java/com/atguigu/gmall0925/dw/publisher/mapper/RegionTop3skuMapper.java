package com.atguigu.gmall0925.dw.publisher.mapper;

import com.atguigu.gmall0925.dw.publisher.bean.RegionTop3Sku;
import tk.mybatis.mapper.common.Mapper;

//如果是mybatis，接口需要写各种方法，在resource中写xml文件（写sql）
// 但是通用mapper不用写，自动封装
public interface RegionTop3skuMapper extends Mapper<RegionTop3Sku> {

}
