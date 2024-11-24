//9. Create an RDD of integers from 1 to 100 and write a Spark program to compute their sum using an RDD action.

import org.apache.spark.{SparkConf, SparkContext}

object Question9 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("ComputeSum")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Step 2: Create an RDD of integers from 1 to 100
    val numbersRDD = sparkContext.parallelize(1 to 100)

    // Step 3: Compute the sum using an RDD action
    val sum = numbersRDD.sum()

    // Step 4: Print the computed sum
    println(s"The sum of integers from 1 to 100 is: $sum")

    // Stop the Spark context
    sparkContext.stop()
  }
}
