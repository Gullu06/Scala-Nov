//8. Create an RDD from a list of strings where each string represents a CSV row. Write a Spark program to parse the rows and filter out records where the age is less than 18.

import org.apache.spark.{SparkConf, SparkContext}

object Question8 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("FilterCSVByAge")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Step 2: Create an RDD from a list of strings
    val csvData = List(
      "1,John,25",
      "2,Jane,17",
      "3,Bob,30",
      "4,Alice,15",
      "5,Tom,20"
    )
    val csvRDD = sparkContext.parallelize(csvData)

    // Step 3: Parse the rows and filter out records where the age is less than 18
    val filteredRDD = csvRDD
      .map(row => {
        val columns = row.split(",") // Split the row into columns
        (columns(0), columns(1), columns(2).toInt) // Convert age to Int
      })
      .filter { case (_, _, age) => age >= 18 } // Filter based on age

    // Step 4: Collect and print the results
    println("Filtered records where age >= 18:")
    filteredRDD.collect().foreach {
      case (id, name, age) => println(s"ID: $id, Name: $name, Age: $age")
    }

    // Stop the Spark context
    sparkContext.stop()
  }
}





//csvData = List(
//  "1,Jai,25",
//  "2,Rohan,17",
//  "3,Billo,30",
//  "4,Alia,15",
//  "5,Temur,20"
//)

