/*** Exercise 3: Analyzing Tasks and Executors
 Objective: Understand how tasks are distributed across executors in local mode.
 Task:

 Create an RDD of strings with at least 1 million lines (e.g., lorem ipsum or repetitive text).
 Perform a transformation pipeline:
 Split each string into words.
 Map each word to (word, 1).
 Reduce by key to count word occurrences.
 Set spark.executor.instances to 2 and observe task distribution in the Spark UI.
 Expected Analysis:

 Compare task execution times across partitions and stages in the UI.
 Understand executor and task allocation for a local mode Spark job.
 ***/

import org.apache.spark.{SparkConf, SparkContext}

object AnalyzingTaskAndExecutor {

  def main(args: Array[String]): Unit = {

    // Step 1: Initialize SparkContext
    val conf = new SparkConf()
      .setAppName("Analyzing Tasks and Executors")
      .setMaster("local[*]") // Local mode with all available cores
    val sc = new SparkContext(conf)

    // Step 2: Create an RDD with 1 million lines of repetitive text
    println("Creating a large RDD...")
    val largeRDD = sc.parallelize(Seq.fill(1000000)("lorem ipsum dolor sit amet"))

    // Step 3: Transformation Pipeline
    println("Performing transformations...")

    // Split each line into words
    val wordsRDD = largeRDD.flatMap(line => line.split("\\s+"))

    // Map each word to (word, 1)
    val pairsRDD = wordsRDD.map(word => (word, 1))

    // Reduce by key to count word occurrences
    val wordCountsRDD = pairsRDD.reduceByKey(_ + _)

    // Step 4: Collect and Print Results
    println("Collecting and displaying results...")
    val wordCounts = wordCountsRDD.collect()

    println("Top 10 word counts:")
    wordCounts.take(10).foreach { case (word, count) =>
      println(s"$word: $count")
    }

    // Save the results to a text file (optional)
    wordCountsRDD.saveAsTextFile("output/wordCounts")

    // Step 5: Display Partition Information
    println(s"Number of partitions in the original RDD: ${largeRDD.getNumPartitions}")
    println(s"Number of partitions after reduceByKey: ${wordCountsRDD.getNumPartitions}")

    // Keep the application running to allow Spark UI observation
    println("Spark application completed. Visit http://localhost:4040 for Spark UI.")
    println("Press Enter to exit...")
    scala.io.StdIn.readLine()

    // Stop SparkContext

    scala.io.StdIn.readLine()
    sc.stop()
  }
}
