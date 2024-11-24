//5. Create an RDD from a list of tuples `(id, score)` and write a Spark program to calculate the average score for all records.

import org.apache.spark.{SparkConf, SparkContext}

object Question5 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("AverageScore")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Step 2: Define a list of tuples (id, score)
    val records = List(
      (1, 85),
      (2, 90),
      (3, 78),
      (4, 92),
      (5, 88),
      (6, 76)
    )

    // Step 3: Create an RDD from the list
    val rdd = sparkContext.parallelize(records)

    // Step 4: Calculate the total score and the number of records
    val (totalScore, count) = rdd
      .map { case (_, score) => (score, 1) } // Map to (score, 1) for each record
      .reduce { case ((sum1, count1), (sum2, count2)) =>
        (sum1 + sum2, count1 + count2) // Aggregate total score and count
      }

    // Step 5: Calculate the average score
    val averageScore = totalScore.toDouble / count

    // Step 6: Print the average score
    println(s"Average Score: $averageScore")

    // Stop the Spark context
    sparkContext.stop()
  }
}
