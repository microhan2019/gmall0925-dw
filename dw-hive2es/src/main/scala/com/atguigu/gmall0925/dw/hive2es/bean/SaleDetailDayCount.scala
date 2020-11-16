package com.atguigu.gmall0925.dw.hive2es.bean

//用户购买商品明细表的字段
case class SaleDetailDayCount(
                               user_id: String,
                               sku_id: String,
                               user_gender: String,
                               user_age: Int,
                               user_level: String,
                               sku_price: Double,
                               sku_name: String,
                               sku_tm_id: String,
                               sku_category1_id: String,
                               sku_category2_id: String,
                               sku_category3_id: String,
                               sku_category1_name: String,
                               sku_category2_name: String,
                               sku_category3_name: String,
                               spu_id: String,
                               sku_num: Long,
                               order_count: Long,
                               order_amount: Double,
                               var dt:String
                             ) {


}

