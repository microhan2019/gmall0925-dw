package com.atguigu.gmall0925.dw.ads.udaf

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, DoubleType, MapType, StringType, StructField, StructType}

import scala.collection.immutable.HashMap


//自定义UDAF函数，把同一地区、同一商品 的 “省市 金额” 进行转换处理
class ProvinceRemark extends UserDefinedAggregateFunction {

  //输入类型：省市名 String , 订单金额 double
  override def inputSchema: StructType = StructType(Array(StructField
  ("province_name", StringType), StructField("order_amount", DoubleType)))


  //存储类型：各个省市的金额Map(省市->金额),   总金额Double
  override def bufferSchema: StructType = StructType(Array(StructField
  ("province_amount", MapType(StringType, DoubleType)), StructField
  ("total_amount", DoubleType)))


  //输出类型：String
  override def dataType: DataType = StringType


  //校验    是否相同的输入会有相同的输出
  override def deterministic: Boolean = true


  //初始化。存储器
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = new HashMap[String, Double]()
    buffer(1) = 0d

  }

  //更新数据  executor更新每条数据
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    //输入的值
    val provinceName: String = input.getString(0)
    val orderAmount: Double = input.getDouble(1)

    //原来有的值
    var provinceAmountMap: Map[String, Double] = buffer.getAs[Map[String,
      Double]](0)
    var totalAmount: Double = buffer.getDouble(1)

    //合并 把 省市 和金额   保存到map中
    provinceAmountMap = provinceAmountMap + (provinceName -> (provinceAmountMap
      .getOrElse(provinceName, 0d) + orderAmount))
    totalAmount += orderAmount

    buffer(0) = provinceAmountMap
    buffer(1) = totalAmount

  }

  // 合并数据  把executor 中的数据两两合并
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    var provinceAmountMap1: Map[String, Double] = buffer1.getAs[Map[String, Double]](0)
    val totalAmount1: Double = buffer1.getDouble(1)

    var provinceAmountMap2: Map[String, Double] = buffer2.getAs[Map[String, Double]](0)
    var totalAmount2: Double = buffer2.getDouble(1)

    val provinceAmountNewMap: Map[String, Double] = provinceAmountMap1.foldLeft(provinceAmountMap2) {
      case (map2, (provinceName, orderAmount)) => {
        map2 + (provinceName -> (map2.getOrElse(provinceName, 0d) + orderAmount))
      }
    }

    buffer1(0) = provinceAmountNewMap
    buffer1(1) = totalAmount1 + totalAmount2

  }


  // 展示结果  把存储器中最后的合并结果 进行展示 变成输出格式 string
  override def evaluate(buffer: Row): Any = {
    //把Map中的值进行计算 ===》 百分比
    val provinceAmountMap: Map[String, Double] = buffer.getAs[Map[String,
      Double]](0)
    val totalAmount: Double = buffer.getDouble(1)

    //(省市->金额) ===》(省市，比率)
    val provinceInfoList: List[ProvinceInfo] = provinceAmountMap.map {
      case (provinceName, orderAmount) => {
        val ratio: Double = Math.round(orderAmount * 1000d / totalAmount) / 10d
        ProvinceInfo(provinceName, ratio)
      }
    }.toList

    //排序，取前2名
    var provinceInfoSortedList: List[ProvinceInfo] = provinceInfoList
      .sortWith(_.ratio > _.ratio).take(2)

    //计算其他
    if (provinceInfoList.size > 2) {
      var otherRatio = 100d
      provinceInfoSortedList.foreach {
        provinceInfo => otherRatio -= provinceInfo.ratio
      }
      otherRatio = Math.round(otherRatio * 10d) / 10d
      provinceInfoSortedList = provinceInfoSortedList :+ ProvinceInfo("其他", otherRatio)
    }

    //整理成字符串
    provinceInfoSortedList.mkString(",")

  }
}

//省市、比率 样例类
case class ProvinceInfo(name: String, ratio: Double) {
  override def toString: String = name + ":" + ratio + "%"
}
