//4. Write a Spark program to count the frequency of each character in a given collection of strings using RDD transformations.

import org.apache.spark.{SparkConf, SparkContext}

object Question4 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("CharacterFrequency")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Step 2: Define a collection of strings
    val stringCollection = Seq(
      "Apache Spark is amazing",
      "Big data processing with Spark",
      "Count character frequency"
    )

    // Step 3: Create an RDD from the collection
    val rdd = sparkContext.parallelize(stringCollection)

    // Step 4: Transform the RDD to count character frequency
    val characterFrequency = rdd
      .flatMap(line => line.replaceAll("\\s", "").toCharArray) // Remove spaces and split into characters
      .map(char => (char, 1)) // Map each character to a tuple (char, 1)
      .reduceByKey(_ + _)     // Aggregate counts by character

    // Step 5: Collect and print the result
    println("Character Frequency:")
    characterFrequency.collect().foreach { case (char, count) =>
      println(s"Character '$char': $count")
    }

    // Step 6: Stop the Spark context
    sparkContext.stop()
  }
}
