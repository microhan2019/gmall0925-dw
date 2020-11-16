package com.atguigu.gmall0925.dw.ads.app

import com.atguigu.gmall0925.dw.ads.udaf.ProvinceRemark
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object RegionTop3sku {
  def main(args: Array[String]): Unit = {

    var doDate:String = ""
    if (args.length > 0) {
      doDate = args(0)
    }

    //创建SparkConf对象
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("RegionTop3sku")

    //创建SparkSession对象
    val spark: SparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate


    //创建自定义UDAF函数
    val remark = new ProvinceRemark
    //注册
    spark.udf.register("province_remark",remark)

    println(s"doDate = ${doDate}")

    //切换对应的数据库
    spark.sql("use gmall0925")

    //子查询临时表  province_remark是自定义的UDAF函数
    spark.sql(s"select '"+ doDate +"' dt,  region_name,  sku_name,  sum" +
      s"(order_amount) order_amount ,  province_remark(province_name, " +
      s"order_amount ) remark  from  dws_province_sku where   dt ='${doDate}'" +
      s"  group by  region_name,sku_name  order by " +
      "region_name,sku_name").createOrReplaceTempView("tmp_region_amount")



    //查询
    spark.sql("insert into table ads_region_top3_sku select  dt,region_name, " +
      "sku_name,order_amount,remark from " +
      "(select dt,region_name, sku_name," +
      "order_amount ,remark, rank() over(partition by region_name order by " +
      "order_amount desc) rk from tmp_region_amount)region_amount_rk where " +
      "rk<=3").show(100,false)


    spark.stop()




  }
}
