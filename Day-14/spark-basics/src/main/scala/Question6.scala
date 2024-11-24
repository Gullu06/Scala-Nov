//6. Create two RDDs containing key-value pairs `(id, name)` and `(id, score)`. Write a Spark program to join these RDDs on `id` and produce `(id, name, score)`.

import org.apache.spark.{SparkConf, SparkContext}

object Question6 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize Spark configuration and context
    val sparkConf = new SparkConf()
      .setAppName("JoinRDDs")
      .setMaster("local[*]")
    val sparkContext = new SparkContext(sparkConf)

    // Step 2: Create two RDDs
    val namesRDD = sparkContext.parallelize(List(
      (1, "Alice"),
      (2, "Bob"),
      (3, "Charlie"),
      (4, "David")
    ))

    val scoresRDD = sparkContext.parallelize(List(
      (1, 85),
      (2, 90),
      (3, 78),
      (5, 88)
    ))

    // Step 3: Perform an inner join on `id`
    val joinedRDD = namesRDD.join(scoresRDD)

    // Step 4: Transform the joined RDD to produce (id, name, score)
    val resultRDD = joinedRDD.map {
      case (id, (name, score)) => (id, name, score)
    }

    // Step 5: Collect and print the result
    resultRDD.collect().foreach {
      case (id, name, score) =>
        println(s"(ID: $id, Name: $name, Score: $score)")
    }

    // Stop the Spark context
    sparkContext.stop()
  }
}
