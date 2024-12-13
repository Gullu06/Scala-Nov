package aggregation

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object AggregationJob {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Dashboard Aggregations with Incremental Updates")
      .master("local[*]")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Paths for enriched data and metrics
    val enrichedDataPath = "gs://movie_bucket_pc/enriched_ratings/"
    val metricsBasePath = "gs://movie_bucket_pc/aggregated_metrics/"

    // Load the enriched ratings dataset
    val enrichedDF = spark.read
      .option("header", "true")
      .parquet(enrichedDataPath)

    // Validate the data to ensure it is correct and contains expected columns
    enrichedDF.printSchema()
    enrichedDF.show(5)

    // Incrementally update Per Movie Metrics
    val movieMetricsPath = s"$metricsBasePath/per_movie_metrics"
    updateAggregatedMetrics(
      enrichedDF,
      Seq("movieId", "title", "genres"),
      movieMetricsPath,
      spark
    )

    // Incrementally update Per Genre Metrics
    val genreMetricsPath = s"$metricsBasePath/per_genre_metrics"
    val genreEnrichedDF = enrichedDF
      .withColumn("genre", explode(split(col("genres"), "\\|"))) // Split genres into rows
    updateAggregatedMetrics(
      genreEnrichedDF,
      Seq("genre"),
      genreMetricsPath,
      spark
    )

    // Incrementally update Per User Demographic Metrics
    val demographicMetricsPath = s"$metricsBasePath/per_demographic_metrics"
    updateAggregatedMetrics(
      enrichedDF,
      Seq("age", "gender", "location"),
      demographicMetricsPath,
      spark
    )

    println("Incremental updates to aggregated metrics are completed.")
  }

  /**
   * Function to update aggregated metrics incrementally.
   *
   * @param newData The new data to be aggregated.
   * @param groupColumns Columns to group by for aggregation.
   * @param outputPath The output path for the aggregated metrics.
   * @param spark The Spark session.
   */
  def updateAggregatedMetrics(
                               newData: DataFrame,
                               groupColumns: Seq[String],
                               outputPath: String,
                               spark: SparkSession
                             ): Unit = {
    import spark.implicits._

    // Read existing metrics if available
    val existingMetrics = try {
      spark.read.format("parquet").load(outputPath)
    } catch {
      case _: Exception =>
        println(s"No existing metrics found at $outputPath, starting fresh.")
        spark.emptyDataFrame
    }

    // Perform aggregation on new data
    val newMetrics = newData
      .groupBy(groupColumns.map(col): _*)
      .agg(
        avg("rating").as("average_rating"),
        count("rating").as("total_ratings")
      )

    // Merge new metrics with existing metrics
    val updatedMetrics = if (!existingMetrics.isEmpty) {
      existingMetrics
        .union(newMetrics)
        .groupBy(groupColumns.map(col): _*)
        .agg(
          avg("average_rating").as("average_rating"),
          sum("total_ratings").as("total_ratings")
        )
    } else {
      newMetrics
    }

    // Write the updated metrics back to the output path
    updatedMetrics.write
      .mode("overwrite")
      .parquet(outputPath)

    println(s"Metrics updated successfully at $outputPath")
  }
}
