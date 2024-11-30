import org.apache.spark.sql.{DataFrame, SparkSession}

object case_study_2 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val spark = SparkSession.builder()
      .appName("Caching Example")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Create a sample DataFrame (Sales Data)
    val salesData = Seq.tabulate(1000000)(i => (i, s"Product-${i % 100}", i % 50, i * 10.0))
      .toDF("sale_id", "product_name", "region_id", "sale_amount")

    println("Sample Sales Data:")
    salesData.show(5)

    // Function to measure execution time
    def measureExecutionTime[T](block: => T, description: String): Unit = {
      val start = System.nanoTime()
      block
      val end = System.nanoTime()
      println(f"$description completed in ${(end - start) / 1e9}%1.3f seconds")
    }

    // Without Caching
    println("\n--- Execution Without Caching ---")
    measureExecutionTime(
      salesData.groupBy("product_name").count().show(),
      "Aggregation 1 (no caching)"
    )
    measureExecutionTime(
      salesData.filter($"sale_amount" > 1000).count(),
      "Filter Operation (no caching)"
    )
    measureExecutionTime(
      salesData.groupBy("region_id").sum("sale_amount").show(),
      "Aggregation 2 (no caching)"
    )

    // With Caching
    println("\n--- Execution With Caching ---")
    salesData.cache() // Cache the DataFrame in memory
    measureExecutionTime(
      salesData.groupBy("product_name").count().show(),
      "Aggregation 1 (with caching)"
    )
    measureExecutionTime(
      salesData.filter($"sale_amount" > 1000).count(),
      "Filter Operation (with caching)"
    )
    measureExecutionTime(
      salesData.groupBy("region_id").sum("sale_amount").show(),
      "Aggregation 2 (with caching)"
    )

    // Unpersist the DataFrame after use
    salesData.unpersist()

    // Stop SparkSession
    spark.stop()
  }
}
