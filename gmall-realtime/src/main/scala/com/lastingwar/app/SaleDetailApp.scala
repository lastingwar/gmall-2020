package com.lastingwar.app

import java.util

import com.alibaba.fastjson.JSON
import com.lastingwar.bean.{OrderDetail, OrderInfo, SaleDetail}
import com.lastingwar.gmall.constant.GmallConstants
import com.lastingwar.utils.{MyKafkaUtil, RedisUtil}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.json4s.DefaultFormats
import redis.clients.jedis.Jedis

import scala.collection.mutable.ListBuffer
import collection.JavaConverters._
import org.json4s.native.Serialization

/**
 * 将order_info和order_detail_info双流join,并根据user_info补全,存入redis
 *
 * @author yhm
 * @create 2020-11-10 18:55
 */
object SaleDetailApp {
  def main(args: Array[String]): Unit = {
    // 1. 创建SparkConf并设置App名称
    val conf: SparkConf = new SparkConf().setAppName("SparkCoreTest").setMaster("local[*]")

    // 2. 创建StreamingContext,该对象是提交SparkStreamingApp的入口,3s是批处理间隔
    val ssc = new StreamingContext(conf,Seconds(3))

    // 获取orderInfoKafkaDStream
    val orderInfoKafkaDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstants.GMALL_ORDER_INFO, ssc)
    // 获取orderDetailInfoKafkaDStream
    val orderDetailInfoKafkaDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstants.GMALL_ORDER_DETAIL, ssc)

    // 将orderInfoDStream 转化为样例类

    val orderInfoDStream: DStream[(String, OrderInfo)] = orderInfoKafkaDStream.map(record => {
      val orderInfo: OrderInfo = JSON.parseObject(record.value(), classOf[OrderInfo])
      // 日期 格式: yyyy-MM-dd HH-mm-ss
      val createTimeArr: Array[String] = orderInfo.create_time.split(" ")
      // create_date取yyyy-MM-dd
      orderInfo.create_date = createTimeArr(0)
      // create_hour取HH
      orderInfo.create_hour = createTimeArr(1).split(":")(0)
      //收件人 电话 脱敏  取原电话的后4位加**
      orderInfo.consignee_tel = "*******" + orderInfo.consignee_tel.splitAt(7)._2

      (orderInfo.id, orderInfo)
    })

    // 将orderDetailInfoDStream 转化为样例类
    val orderDetailInfoDStream: DStream[(String, OrderDetail)] = orderDetailInfoKafkaDStream.map(record => {
      val OrderDetail: OrderDetail = JSON.parseObject(record.value(), classOf[OrderDetail])

      (OrderDetail.order_id, OrderDetail)
    })

    // 双流join 全外连接,做逻辑判断
    val fullOutJoinDStream: DStream[(String, (Option[OrderInfo], Option[OrderDetail]))] = orderInfoDStream.fullOuterJoin(orderDetailInfoDStream)


    // 逻辑判断,返回SaleDerailDStream
    //mapPartitions 减少redis连接
    val noUserSaleDetailDStream: DStream[SaleDetail] = fullOutJoinDStream.mapPartitions(iter => {

      // 获取一个jedis连接
      val jedisClient: Jedis = RedisUtil.getJedisClient

      // 创建一个list存放结果
      val saleDetails = new ListBuffer[SaleDetail]()

      // 样例类转json
      implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

      iter.foreach {
        case (orderId, (orderInfoOption, orderDetailOption)) =>
          // 确定redis中的key
          val orderInfoKey = s"OrderInfo$orderId"
          val orderDetailKey = s"OrderDetail$orderId"



          // if orderInfo 不为空
          if (orderInfoOption.isDefined) {
            val orderInfo: OrderInfo = orderInfoOption.get

            // 1对N的关系,1必须加入到redis中
            val orderInfoStr: String = Serialization.write(orderInfo)
            jedisClient.set(orderInfoKey, orderInfoStr)
            jedisClient.expire(orderInfoKey, 100)


            // 同时orderDetail 不为空
            if (orderDetailOption.isDefined) {
              val saleDetail = new SaleDetail(orderInfo, orderDetailOption.get)
              // 加入到集合中
              saleDetails += saleDetail
            }
            // 查找缓存中的连接上的orderDetail
            if (jedisClient.exists(orderDetailKey)) {
              val orderDetailSet: util.Set[String] = jedisClient.smembers(orderDetailKey)

              //循环遍历集合,添加到list中
              orderDetailSet.asScala.foreach(orderDetailStr => {
                val orderDetail1: OrderDetail = JSON.parseObject(orderDetailStr, classOf[OrderDetail])
                val saleDetail = new SaleDetail(orderInfo, orderDetail1)
                saleDetails += saleDetail
              })
            }

          }
          // orderInfo为空,orderDetail不为空
          else {
            //获取orderDetail
            val orderDetail: OrderDetail = orderDetailOption.get
            //查缓存,存在则加入到list
            if (jedisClient.exists(orderInfoKey)) {
              val orderInfoRedis: OrderInfo = JSON.parseObject(jedisClient.get(orderInfoKey), classOf[OrderInfo])

              val detail = new SaleDetail(orderInfoRedis, orderDetail)
              saleDetails += detail
            }
            // 不存在添加到redis
            else {
              val orderDetailJSON: String = Serialization.write(orderDetail)
              jedisClient.sadd(orderDetailKey, orderDetailJSON)
              jedisClient.expire(orderDetailKey, 100)
            }
          }
      }

      // 归还连接
      jedisClient.close()
      // 返回list的iterator
      saleDetails.iterator
    })



    // 测试
//    noUserSaleDetailDStream.print(100)




    //启动采集器
    ssc.start()

    // 4. 默认情况下，上下文对象不能关闭
    //ssc.stop()


    //等待采集结束，终止上下文环境对象
    ssc.awaitTermination()
  }
}
