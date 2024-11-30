package case_study_1

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.broadcast

object case_study_1 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val spark = SparkSession.builder()
      .appName("Optimized Join Example")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._

    // Create a small dataset: User Details
    val userData = Seq(
      (1, "Alice"),
      (2, "Bob"),
      (3, "Charlie"),
      (4, "David"),
      (5, "Eve"),
      (6, "Frank"),
      (7, "Grace"),
      (8, "Helen"),
      (9, "Ivy"),
      (10, "Jack")
    ).toDF("user_id", "user_name")

    // Create a large dataset: Transaction Logs
    val transactions = Seq.tabulate(1000000)(i =>
      (i % 10 + 1, f"2024-01-${(i % 31) + 1}%02d", i * 10.0)
    ).toDF("user_id", "transaction_date", "transaction_amount")

    // Display sample data for clarity
    println("Sample from Transactions Dataset:")
    transactions.show(5, truncate = false)

    println("User Details Dataset:")
    userData.show()

    // Measure time for standard join (no broadcasting)
    val startDefaultJoin = System.nanoTime()
    val standardJoinResult = performJoin(transactions, userData, useBroadcast = false)
    println(s"Count from default join: ${standardJoinResult.count()}") // Trigger action to compute the join
    val endDefaultJoin = System.nanoTime()

    println(f"Time taken for standard join: ${(endDefaultJoin - startDefaultJoin) / 1e9}%1.3f seconds")

    // Measure time for broadcast join
    val startBroadcastJoin = System.nanoTime()
    val broadcastJoinResult = performJoin(transactions, userData, useBroadcast = true)
    println(s"Count from broadcast join: ${broadcastJoinResult.count()}") // Trigger action to compute the join
    val endBroadcastJoin = System.nanoTime()

    println(f"Time taken for broadcast join: ${(endBroadcastJoin - startBroadcastJoin) / 1e9}%1.3f seconds")
    // Stop SparkSession
    spark.stop()
  }

  /**
   * Perform a join operation with or without broadcasting.
   *
   * @param largeDataset The larger dataset (Transaction Logs).
   * @param smallDataset The smaller dataset (User Details).
   * @param useBroadcast Flag to determine if broadcasting should be used.
   * @return The resulting DataFrame after the join.
   */
  def performJoin(largeDataset: DataFrame, smallDataset: DataFrame, useBroadcast: Boolean): DataFrame = {
    if (useBroadcast) {
      println("Performing join with broadcast...")
      largeDataset.join(broadcast(smallDataset), "user_id")
    } else {
      println("Performing join without broadcast...")
      largeDataset.join(smallDataset, "user_id")
    }
  }
}
