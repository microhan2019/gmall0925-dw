package com.atguigu.gmall0925.dw.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import bean.StartUpLog
import com.alibaba.fastjson.JSON
import com.atguigu.gmall.dw.constant.GmallConstants
import com.atguigu.gmall0925.dw.util.MyEsUtil
import com.atguigu.gmall0925.dw.utils.MyKafkaUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import redis.clients.jedis.Jedis

import scala.collection.mutable.ListBuffer

object RealtimeStartupLog {

  def main(args: Array[String]): Unit = {
    //创建StreamingContext
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("RealtimeStartupLog")

    //SparkContext对象
    val sc: SparkContext = new SparkContext(sparkConf)

    //StreamingContext实时处理对象
    val ssc: StreamingContext = new StreamingContext(sc, Seconds(10))

    //利用工具类从Kafka中读取实时 启动日志
    val startupLogDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstants.KAFKA_TOPIC_STARTUP, ssc)


    /*
    测试一下看是否可以正常从Kafka中读取数据
    startupLogDStream.map(_.value()).foreachRDD(rdd=>{
      println(rdd.collect().mkString("\n"))
    })
    */

    //统计：当日活跃用户及分时趋势图（需要把启动日志进行去重，同一天同一个用户多次启动算一次启动）

    val startupString: DStream[String] = startupLogDStream.map(_.value())
    //把启动日志变成样例类对象
    val startUpDStream: DStream[StartUpLog] = startupString.map(log => {
      val startUpLog: StartUpLog = JSON.parseObject(log, classOf[StartUpLog])
      startUpLog
    })

    //=============== 利用Redis去重 ============================
    
    //(1). 先做过滤, 把redis中的数据与当前批次的数据进行对比，过滤掉已有的数据
    val filterDStream: DStream[StartUpLog] = startUpDStream.transform(rdd => {
      println("过滤前：" + rdd.count())
      val jedis = new Jedis("hadoop102", 6379)

      val dauSet: util.Set[String] = jedis.smembers("dau:" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
      jedis.close()
      //把已经存在redis中的数据通过广播变量发送出去
      val dauSetBC: Broadcast[util.Set[String]] = sc.broadcast(dauSet)

      val filterRDD: RDD[StartUpLog] = rdd.filter(startupLog => {
        !dauSetBC.value.contains(startupLog.mid)
      })
      println("过滤后：" + filterRDD.count())

      filterRDD

    })


    //(2). 把新活跃用户的数据保存到redis
    filterDStream.foreachRDD(rdd => {
      rdd.foreachPartition(startupItr => {
        //获取Redis数据库连接对象
        val jedis = new Jedis("hadoop102", 6379)

        //用于匹配保存到es数据的集合
        val listBuffer = new ListBuffer[StartUpLog]


        startupItr.foreach(startupLog => {
          val key = "dau:" + new SimpleDateFormat("yyyy-MM-dd" +
            "").format(new Date(startupLog.ts))
          //数据存放到redis中
          jedis.sadd(key, startupLog.mid)

          //补充一些时间字段，用于es中的时间分析
         startupLog.logDate= new SimpleDateFormat("yyyy-MM-dd").format(new Date(startupLog.ts))
         startupLog.logHourMinute = new SimpleDateFormat("HH:mm").format(new Date(startupLog.ts))
         startupLog.logHour = startupLog.logHourMinute.split(":")(0)
          listBuffer += startupLog
        })

        jedis.close()

        println(listBuffer.mkString("\n"))
        //（3） 把数据也保存到ES一份
        MyEsUtil.executeIndexBulk(GmallConstants.ES_INDEX_DAU,listBuffer.toList,null)



      })
    })



    //(3). 把数据保存到 es数据库中


    //启动流式处理
    ssc.start()
    ssc.awaitTermination()

  }
}
