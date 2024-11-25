/*** Exercise 1: Understanding RDD and Partitioning
 Objective: Create and manipulate an RDD while understanding its partitions.
 Task:

 Load a large text file (or create one programmatically with millions of random numbers).
 Perform the following:
 Check the number of partitions for the RDD.
 Repartition the RDD into 4 partitions and analyze how the data is distributed.
 Coalesce the RDD back into 2 partitions.
 Print the first 5 elements from each partition.
 Expected Analysis:

 View the effect of repartition and coalesce in the Spark UI (stages, tasks).
***/

import org.apache.spark.{SparkConf, SparkContext}

object RddPartitioning {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize SparkConf and SparkContext
    val conf = new SparkConf()
      .setAppName("RDD Partitioning")
      .setMaster("local[*]") // Local mode with all available cores
    val sc = new SparkContext(conf)

    // Step 2: Generate a large RDD with random numbers
    val largeRDD = sc.parallelize(Seq.fill(1000000)(scala.util.Random.nextInt(100)), numSlices = 8)

    // Step 3: Check the number of partitions
    println(s"Initial number of partitions: ${largeRDD.getNumPartitions}")

    println("First 5 elements from each partition (before repartitioning):")
    largeRDD
      .mapPartitionsWithIndex((index, iterator) => iterator.take(5).map((index, _)))
      .collect()
      .groupBy(_._1)
      .foreach { case (partitionIndex, elements) =>
        println(s"Partition $partitionIndex: ${elements.map(_._2).mkString(", ")}")
      }

    // Step 4: Repartition the RDD into 4 partitions
    val repartitionedRDD = largeRDD.repartition(4)
    println(s"Number of partitions after repartitioning: ${repartitionedRDD.getNumPartitions}")

    // Step 5: Analyze how data is distributed in the repartitioned RDD
    println("First 5 elements from each partition (after repartitioning):")
    repartitionedRDD
      .mapPartitionsWithIndex((index, iterator) => iterator.take(5).map((index, _)))
      .collect()
      .groupBy(_._1)
      .foreach { case (partitionIndex, elements) =>
        println(s"Partition $partitionIndex: ${elements.map(_._2).mkString(", ")}")
      }

    // Step 6: Coalesce the RDD back into 2 partitions
    val coalescedRDD = repartitionedRDD.coalesce(2)
    println(s"Number of partitions after coalescing: ${coalescedRDD.getNumPartitions}")

    // Step 7: Analyze how data is distributed in the coalesced RDD
    println("First 5 elements from each partition (after coalescing):")
    coalescedRDD
      .mapPartitionsWithIndex((index, iterator) => iterator.take(5).map((index, _)))
      .collect()
      .groupBy(_._1)
      .foreach { case (partitionIndex, elements) =>
        println(s"Partition $partitionIndex: ${elements.map(_._2).mkString(", ")}")
      }

    // Stop SparkContext

    scala.io.StdIn.readLine()
    sc.stop()
  }
}
