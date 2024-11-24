//7. Write a Spark program to perform a union operation on two RDDs of integers and remove duplicate elements from the resulting RDD.

import org.apache.spark.{SparkConf, SparkContext}

object Question7 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("UnionRDDs")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Step 2: Create two RDDs of integers
    val rdd1 = sparkContext.parallelize(List(1, 2, 3, 4, 5))
    val rdd2 = sparkContext.parallelize(List(4, 5, 6, 7, 8))

    // Step 3: Perform the union operation
    val unionRDD = rdd1.union(rdd2)

    // Step 4: Remove duplicates using the `distinct` transformation
    val distinctRDD = unionRDD.distinct()

    // Step 5: Collect and print the results
    println("Resulting RDD after union and removing duplicates:")
    distinctRDD.collect().foreach(println)

    // Stop the Spark context
    sparkContext.stop()
  }
}
