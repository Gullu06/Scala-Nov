/*** Exercise 2: Narrow vs Wide Transformations
  Objective: Differentiate between narrow and wide transformations in Spark.
Task:

  Create an RDD of numbers from 1 to 1000.
Apply narrow transformations: map, filter.
  Apply a wide transformation: groupByKey or reduceByKey (simulate by mapping numbers into key-value pairs, e.g., (number % 10, number)).
  Save the results to a text file.
  Expected Analysis:

  Identify how narrow transformations execute within a single partition, while wide transformations cause shuffles.
Observe the DAG in the Spark UI, focusing on stages and shuffle operations.
***/

import org.apache.spark.{SparkConf, SparkContext}

object Transformations {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize SparkConf and SparkContext
    val conf = new SparkConf()
      .setAppName("Narrow vs Wide Transformations")
      .setMaster("local[*]") // Local mode with all available cores
    val sc = new SparkContext(conf)

    // Step 2: Create an RDD of numbers from 1 to 1000
    val numbersRDD = sc.parallelize(1 to 1000)

    // Step 3: Apply narrow transformations
    val mappedRDD = numbersRDD.map(_ * 2)          // Multiply each number by 2
    val filteredRDD = mappedRDD.filter(_ % 3 == 0) // Keep numbers divisible by 3

    // Step 4: Apply a wide transformation
    // Map numbers into key-value pairs: (number % 10, number)
    val kvRDD = filteredRDD.map(number => (number % 10, number))

    // Perform a reduceByKey (wide transformation)
    val reducedRDD = kvRDD.reduceByKey(_ + _) // Sum the values for each key

    // Step 5: Save the results to a text file
    reducedRDD.saveAsTextFile("output_transformation")
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()

    // Stop the SparkContext
    sc.stop()
  }
}
