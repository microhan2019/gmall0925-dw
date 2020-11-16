package com.atguigu.gmall0925.dw.app

import bean.OrderInfo
import com.alibaba.fastjson.JSON
import com.atguigu.gmall.dw.constant.GmallConstants
import com.atguigu.gmall0925.dw.util.MyEsUtil
import com.atguigu.gmall0925.dw.utils.MyKafkaUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

//消费实时订单数据
object RealtimeOrderLog {
  def main(args: Array[String]): Unit = {

    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("gmall_order")

    val ssc = new StreamingContext(sparkConf, Seconds(5))

    //利用工具从kafka中读取数据
    val recordDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstants.KAFKA_TOPIC_ORDER, ssc)

    val orderInfoDStream: DStream[OrderInfo] = recordDStream.map(_.value()).map(jsonString => {
      //把json字符串变为json对象
      val orderInfo: OrderInfo = JSON.parseObject(jsonString, classOf[OrderInfo])

      //createTime: 2020-11-14 08:39:05
      val dateArray: Array[String] = orderInfo.createTime.split(" ")
      val dateString = dateArray(0)

      val timeArray = dateArray(1).split(":")
      val hour = timeArray(0)
      val minute = timeArray(1)


      orderInfo.createDate = dateString
      orderInfo.createHour = hour
      orderInfo.createHourMinute = hour + ":" + minute

      orderInfo
    })


//    //把新增订单保存到ES中
    orderInfoDStream.foreachRDD(orderInfoRDD=>{
      orderInfoRDD.foreachPartition(orderIter=>{
        val orderInfoList: List[OrderInfo] = orderIter.toList

        MyEsUtil.executeIndexBulk(GmallConstants.ES_INDEX_ORDER,orderInfoList,null)

      })
    })

    ssc.start()
    ssc.awaitTermination()




  }
}
