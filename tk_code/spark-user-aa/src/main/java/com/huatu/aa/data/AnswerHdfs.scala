package com.huatu.aa.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.mongodb.spark.config.ReadConfig
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Row, SaveMode, SparkSession}

import scala.collection.mutable.{ArrayBuffer, Map}

case class AnswerCard3(
                        userid: Long,
                        correct: Array[Int],
                        question: Array[Int],
                        point: Array[Int],
                        answertime: Array[Int],
                        createtime: String,
                        subject: Int
                      )

object AnswerHdfs {

  def main(args: Array[String]): Unit = {

    val hdfs = "hdfs://ns1/huatu-data/ztk_answer_card/flume/"
    val sdf = new SimpleDateFormat("yyyy/MM/dd")

    val one = sdf.format(new Date(System.currentTimeMillis()))
    val two = sdf.format(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L))
    //    val one = "2019/07/02"


    val inputUrl = "mongodb://huatu_ztk:wEXqgk2Q6LW8UzSjvZrs@192.168.100.153:27017,192.168.100.153:27017,192.168.100.155:27017/huatu_ztk"
    val db = "tmp_zac"

    val warehouseLocation = new File("spark-warehouse").getAbsolutePath
    System.setProperty("HADOOP_USER_NAME", "root")

    val conf = new SparkConf()
      .setAppName("hdfs-ztk_answer_card")
      .setMaster("local[5]")
      .set("hive.exec.dynamic.partition", "true")
      .set("hive.exec.dynamic.partition.mode", "nonstrict")
      .set("hive.exec.max.dynamic.partitions", "1800")
      .set("mapreduce.map.memory.mb", "10240")
      .set("mapreduce.reduce.memory.mb", "10240")
      .set("mspark.sql.parquet.writeLegacyFormat", "true")
      .set("spark.mongodb.input.readPreference.name", "secondaryPreferred")
      .set("spark.mongodb.input.partitioner", "MongoPaginateBySizePartitioner")
      .set("spark.mongodb.input.partitionerOptions.partitionKey", "_id")
      .set("spark.mongodb.input.partitionerOptions.partitionSizeMB", "32")
      .set("spark.mongodb.input.samplesPerPartition", "5000000")
      .set("spark.debug.maxToStringFields", "100")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "100")
      .registerKryoClasses(Array(classOf[scala.collection.mutable.WrappedArray.ofRef[_]]))

    import com.mongodb.spark.sql._
    val sparkSession = SparkSession.builder().config("spark.sql.warehouse.dir", warehouseLocation).config(conf).enableHiveSupport().getOrCreate()

    import sparkSession.implicits._
    val sc = sparkSession.sparkContext


    val ztk_question = sparkSession.loadFromMongoDB(
      ReadConfig(
        Map(
          "uri" -> inputUrl.concat(".ztk_question_new"),
          "readPreference.name" -> "secondaryPreferred",
          "partitioner" -> "MongoPaginateBySizePartitioner",
          "partitionerOptions.partitionSizeMB" -> "32",
          "maxBatchSize" -> "1000000",
          "keep_alive_ms" -> "500")
      )).toDF() // Uses the ReadConfig
    ztk_question.createOrReplaceTempView("ztk_question")
    //    val q2p = sc.broadcast(map.collectAsMap())
    /**
      * mongo 214024
      * spark 205846
      * the mapping of the knowledge to points
      */
    // spark context
    val q2p = sc.broadcast(sparkSession.sql("select _id,points from ztk_question").rdd.filter { r =>
      !r.isNullAt(0) && !r.isNullAt(1) && r.getSeq(1).nonEmpty
    }.map {
      r =>
        val _id: Int = r.get(0).asInstanceOf[Number].intValue()
        val pid = r.getSeq(1).head.toString.toDouble.intValue()
        (_id, pid)
    }.collectAsMap())

    println(q2p.value.size)
    if (q2p.value.isEmpty) {
      sparkSession.stop()
    }

    val o = sparkSession.read.json(hdfs + one)
    val t = sparkSession.read.json(hdfs + two)

    val asdf = new SimpleDateFormat("yyyyMMdd")

    val yesterday = asdf.format(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L))
    val today = asdf.format(new Date(System.currentTimeMillis()))
    val yesterday_time = asdf.parse(yesterday).getTime
    val today_time = asdf.parse(today).getTime

    val b_yesterday = sc.broadcast(yesterday)
    val b_today = sc.broadcast(today)
    val b_yesterday_time = sc.broadcast(yesterday_time)
    val b_today_time = sc.broadcast(today_time + (24 * 60 * 60 * 1000L - 1L))
    val b_asdf = sc.broadcast(asdf)

    val r = o.rdd.++(t.rdd)

    val result = r
      .mapPartitions {

        ite: Iterator[Row] =>

          val q2pMap = q2p.value
          val format = new SimpleDateFormat("yyyyMMdd")
          val arr = new ArrayBuffer[AnswerCard3]()

          val v_yesterday = b_yesterday.value
          val v_b_today = b_today.value
          val v_yesterday_time = b_yesterday_time.value
          val v_today_time = b_today_time.value

          val v_asdf = b_asdf.value

          while (ite.hasNext) {

            val r = ite.next()

            try {

              val userId = r.getAs[Long]("userId").longValue()
              val corrects = r.getAs[Seq[Long]]("corrects").map(_.asInstanceOf[Int].intValue()).toArray
              val questions = r.getAs[Seq[Long]]("questions").map(_.asInstanceOf[Int].intValue()).toArray
              val times = r.getAs[Seq[Long]]("times").map(_.asInstanceOf[Int].intValue()).toArray
              val createTime = r.getAs[Long]("createTime").longValue()
              val subject = r.getAs[Long]("subject").asInstanceOf[Int].intValue()

              val points = new ArrayBuffer[Int]()
              questions.foreach { qid =>

                val pid: Int = q2pMap.getOrElse(qid, 0)
                points += pid
              }

              val d = v_asdf.format(new Date(createTime))
              if (createTime > v_yesterday_time && createTime <= v_today_time) {
                val i = d.compareTo(v_yesterday)
                if (i >= 0) {

                  arr += AnswerCard3(userId, corrects, questions, points.toArray, times, d, subject)
                }

              }
            } catch {
              case ex: NumberFormatException => {
                println(r)
              }
              case ex2: NullPointerException => {
                println(r)
              }
            }
          }
          arr.iterator
      }.toDF()

    result.repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .partitionBy("createtime")
      .saveAsTable("tmp_zac3")
  }
}
