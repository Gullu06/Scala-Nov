//1. Given a collection of strings, write a Spark program to count the total number of words in the collection using RDD transformations and actions.

import org.apache.spark.{SparkConf, SparkContext}

object Question1 {
  def main(args: Array[String]): Unit = {
    // Initialize Spark configuration and context
    val sparkConfig = new SparkConf()
      .setAppName("TotalWordCounter")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConfig)

    // Input data: A sequence of text lines
    val textLines = Seq(
      "Given a collection of strings,",
      "write a Spark program to count the total number of words",
      "in the collection using RDD transformations and actions."
    )

    // Create an RDD from the input data
    val textRDD = sparkContext.parallelize(textLines)

    // Process the RDD to calculate the total word count
    val wordCount = textRDD
      .flatMap(_.split("\\s+"))  // Break each line into individual words
      .filter(_.nonEmpty)       // Exclude empty entries (if any)
      .map(_.trim)              // Remove unnecessary whitespaces
      .count()                  // Compute the word count

    // Display the result
    println(s"Total words in the dataset: $wordCount")

    // Gracefully stop the Spark context
    sparkContext.stop()
  }
}
