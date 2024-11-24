//10. Write a Spark program to group an RDD of key-value pairs `(key, value)` by key and compute the sum of values for each key.

import org.apache.spark.{SparkConf, SparkContext}

object Question_10 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("GroupAndSumByKey")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Step 2: Create an RDD of key-value pairs
    val data = Seq(
      ("apple", 10),
      ("banana", 15),
      ("apple", 5),
      ("banana", 20),
      ("orange", 8),
      ("apple", 12)
    )
    val rdd = sparkContext.parallelize(data)

    // Step 3: Group by key and compute the sum of values for each key
    val sumByKeyRDD = rdd
      .reduceByKey((x, y) => x + y)

    // Step 4: Collect and print the results
    val results = sumByKeyRDD.collect()
    results.foreach { case (key, sum) =>
      println(s"Key: $key, Sum of values: $sum")
    }

    // Stop the Spark context
    sparkContext.stop()
  }
}
