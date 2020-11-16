package com.atguigu.gmall0925.dw.hive2es.app

import com.atguigu.gmall.dw.constant.GmallConstants
import com.atguigu.gmall0925.dw.hive2es.bean.SaleDetailDayCount
import com.atguigu.gmall0925.dw.util.MyEsUtil
import org.apache.spark.rdd.RDD
import org.apache.spark.{SPARK_BRANCH, SparkConf}
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

object SaleDetailApp {

  def main(args: Array[String]): Unit = {

    var dt = ""
    if (args.nonEmpty && args(0) != null) {
      dt = args(0)
    } else {
      dt = "2020-10-25"
    }

    val sparkConf: SparkConf = new SparkConf().setAppName("SaleDetail").setMaster("local[*]")
    val spark: SparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()

    import spark.implicits._
    spark.sql("use gmall0925")

    val sql = "select user_id,sku_id,user_gender,cast(user_age as int) user_age,user_level," +
      "cast(sku_price as double),sku_name,sku_tm_id, sku_category3_id,sku_category2_id,sku_category1_id," +
      "sku_category3_name,sku_category2_name,sku_category1_name," +
      "spu_id,sku_num,cast(order_count as bigint) order_count," +
      "cast(order_amount as double) order_amount,dt" +
      " from dws_sale_detail_daycount where dt='" + dt + "'"

    //spark 从hive 中读取数据
    val saleDetailRDD: RDD[SaleDetailDayCount] = spark.sql(sql).as[SaleDetailDayCount].rdd

    //把读取到的数据保存到ES中
    saleDetailRDD.foreachPartition(saleDetailIter => {
      val listBuffer = new ListBuffer[SaleDetailDayCount]

      for (saleDetail <- saleDetailIter) {
        listBuffer += saleDetail

        //批量插入有问题！
//        if (listBuffer.size >= 10) {
          MyEsUtil.executeIndexBulk(GmallConstants.ES_INDEX_SALE_DETAIL, listBuffer.toList, null)
          listBuffer.clear()
//        }
      }
//      if (listBuffer.size > 0) {
//        MyEsUtil.executeIndexBulk(GmallConstants.ES_INDEX_SALE_DETAIL, listBuffer.toList, null)
//      }
    })


  }

}
