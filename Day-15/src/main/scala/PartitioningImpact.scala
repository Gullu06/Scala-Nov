/***
 * Exercise 5: Partitioning Impact on Performance
 * Objective: Understand the impact of partitioning on performance and data shuffling.
 * Task:

 * Load a large dataset (e.g., a CSV or JSON file) into an RDD.
 * Partition the RDD into 2, 4, and 8 partitions separately and perform the following tasks:
 * Count the number of rows in the RDD.
 * Sort the data using a wide transformation.
 * Write the output back to disk.
 * Compare execution times for different partition sizes.
 * Expected Analysis:
 *
 * Observe how partition sizes affect shuffle size and task distribution in the Spark UI.
 * Understand the trade-off between too many and too few partitions.
 *
***/

import org.apache.spark.{SparkContext, SparkConf}

object PartitioningImpact {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("Analyzing Tasks and Executors")
      .setMaster("local[*]") // Local mode with all available cores
    val sc = new SparkContext(conf)

    // Step 2: Load a large dataset
    println("Loading a large dataset...")
    // Simulating a large dataset with 1 million rows of random integers
    val largeRDD = sc.parallelize(Seq.fill(1000000)(scala.util.Random.nextInt(100000)), numSlices = 4)

    // Step 3: Experiment with different partition sizes
    val partitionSizes = Seq(2, 4, 8)

    partitionSizes.foreach { numPartitions =>
      println(s"\nProcessing with $numPartitions partitions:")

      // Repartition the RDD
      val repartitionedRDD = largeRDD.repartition(numPartitions)

      // Task 1: Count the number of rows
      val rowCount = repartitionedRDD.count()
      println(s"Row count: $rowCount")

      // Task 2: Sort the data (wide transformation)
      val sortedRDD = repartitionedRDD.sortBy(identity)

      // Task 3: Write the output to disk
      val outputPath = s"output/partitioned_$numPartitions"
      sortedRDD.saveAsTextFile(outputPath)
      println(s"Sorted data saved to $outputPath")
    }

    // Step 4: Analyze in Spark UI
    println("\nObserve the Spark UI at http://localhost:4040 to analyze tasks, stages, and shuffle size.")

    // Keep the application running to analyze Spark UI
    println("Press Enter to exit...")
    scala.io.StdIn.readLine()

    // Stop SparkContext

    scala.io.StdIn.readLine()
    sc.stop()
  }
}
