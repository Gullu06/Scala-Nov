//3. Create an RDD from a list of integers and filter out all even numbers using a Spark program.

import org.apache.spark.{SparkConf, SparkContext}

object Question3 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("FilterOddNumbers")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Step 2: Create an RDD from a list of integers
    val numbersRDD = sparkContext.parallelize(Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

    // Step 3: Filter out even numbers
    val oddNumbersRDD = numbersRDD.filter(num => num % 2 != 0)

    // Step 4: Collect and print the result
    println("Filtered odd numbers:")
    oddNumbersRDD.collect().foreach(println)

    // Step 5: Stop the Spark context
    sparkContext.stop()
  }
}
