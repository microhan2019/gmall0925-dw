package com.atguigu.gmall0925.dw.util

import java.util.Objects

import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.core.{Bulk, BulkResult, Index}
import org.apache.commons.beanutils.BeanUtils

object MyEsUtil {

  private val ES_HOST = "http://192.168.1.102"
  private val ES_HTTP_PORT = 9200

  private var factory: JestClientFactory = null

  /**
   * 获取客户端
   *
   * @return jestclient
   */
  def getClient: JestClient = {
    if (factory == null) {
      build()
    }
    factory.getObject
  }


  /**
   * 关闭客户端
   */
  def close(client: JestClient): Unit = {
    if (!Objects.isNull(client)) {
      try {
        client.shutdownClient() //client.close
      } catch {
        case e: Exception => e.printStackTrace()
      }
    }
  }

  /**
   * 建立连接
   */
  private def build(): Unit = {
    factory = new JestClientFactory
    factory.setHttpClientConfig(new HttpClientConfig.Builder(ES_HOST + ":" + ES_HTTP_PORT).multiThreaded(true)
      .maxTotalConnection(20) //连接总数
      .connTimeout(50000).readTimeout(50000).build)

  }


  /*def main(args: Array[String]): Unit = {
    val client = getClient

    val query: String = " "
    val search: Search = new Search.Builder(query).addIndex("xxx")
      .addType("xxx").build()

    val result: SearchResult = client.execute(search) //执行查询
    val list: util.List[SearchResult#Hit[util.HashMap[String, String], Void]]
    = result.getHits(classOf[util.HashMap[String, String]])
    import collection.JavaConversions._
    for(hit:SearchResult#Hit[util.HashMap[String, String], Void] <- list){

    }

    val index: Index = new Index.Builder().index("").`type`("").build()

  }*/


  //批量执行插入
  def executeIndexBulk(indexName: String, list: List[Any], idColumn: String): Unit = {
    val bulkBuilder: Bulk.Builder = new Bulk.Builder()
      .defaultIndex(indexName).defaultType("_doc")

    for (doc <- list) {
      val indexBuilder = new Index.Builder(doc)
      if (idColumn != null) {
        val id: String = BeanUtils.getProperty(doc, idColumn)
        indexBuilder.id(id)
      }
      val index: Index = indexBuilder.build()
      bulkBuilder.addAction(index)
    }
    val jestclient: JestClient = getClient


    val result: BulkResult = jestclient.execute(bulkBuilder.build())

    if (result.isSucceeded) {
      println("保存成功:" + result.getItems.size())
    }



  }

  def main(args: Array[String]): Unit = {
    println(getClient)


  }


}
