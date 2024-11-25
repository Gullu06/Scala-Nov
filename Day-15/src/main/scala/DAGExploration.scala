/*** Exercise 4: Exploring DAG and Spark UI
 Objective: Analyze the DAG and understand the stages involved in a complex Spark job.
 Task:

 Create an RDD of integers from 1 to 10,000.
 Perform a series of transformations:
 filter: Keep only even numbers.
 map: Multiply each number by 10.
 flatMap: Generate tuples (x, x+1) for each number.
 reduceByKey: Reduce by summing keys.
 Perform an action: Collect the results.
 Expected Analysis:

 Analyze the DAG generated for the job and how Spark breaks it into stages.
 Compare execution times of stages and tasks in the Spark UI.
***/

import org.apache.spark.{SparkConf, SparkContext}

object DAGExploration {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setAppName("Analyzing Tasks and Executors")
      .setMaster("local[*]") // Local mode with all available cores
    val sc = new SparkContext(conf)


    // Step 2: Create an RDD of integers from 1 to 10,000
    println("Creating an RDD of integers...")
    val numbersRDD = sc.parallelize(1 to 10000)

    // Step 3: Perform a series of transformations
    println("Performing transformations...")

    // Filter: Keep only even numbers
    val evenNumbersRDD = numbersRDD.filter(_ % 2 == 0)

    // Map: Multiply each number by 10
    val multipliedRDD = evenNumbersRDD.map(_ * 10)

    // FlatMap: Generate tuples (x, x+1) for each number
    val tuplesRDD = multipliedRDD.flatMap(x => Seq((x, 1), (x + 1, 1)))

    // ReduceByKey: Reduce by summing keys
    val reducedRDD = tuplesRDD.reduceByKey(_ + _)

    // Step 4: Perform an action to trigger computation
    println("Performing action: Collecting results...")
    val results = reducedRDD.collect()

    // Print a subset of the results for verification
    println("Sample of the results:")
    results.take(10).foreach { case (key, value) =>
      println(s"$key -> $value")
    }

    // Step 5: Analyze partitions and stages in the Spark UI
    println("Analyzing DAG and execution stages in Spark UI.")
    println("Visit http://localhost:4040 to explore the Spark UI.")

    // Keep the application running to allow Spark UI observation
    println("Press Enter to exit...")
    scala.io.StdIn.readLine()

    // Stop SparkContext

    scala.io.StdIn.readLine()
    sc.stop()
  }
}
