//2. Create two RDDs containing numbers and write a Spark program to compute their Cartesian product using RDD transformations.

import org.apache.spark.{SparkConf, SparkContext}

object Question2 {
  def main(args: Array[String]): Unit = {
    // Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("Question2")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Define two RDDs with numbers
    val numbersRDD1 = sparkContext.parallelize(Seq(1, 2, 3))
    val numbersRDD2 = sparkContext.parallelize(Seq(4, 5, 6))

    // Compute the Cartesian product of the two RDDs
    val cartesianProductRDD = numbersRDD1.cartesian(numbersRDD2)

    // Collect and print the result
    println("Cartesian Product of the two RDDs:")
    cartesianProductRDD.collect().foreach { case (x, y) =>
      println(s"($x, $y)")
    }

    // Gracefully stop the Spark context
    sparkContext.stop()
  }
}
