import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.storage.StorageLevel

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Sales Performance Analysis")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // Load data
    val trainDF = loadCSV(spark, "gs://walmart_recruiting_data_bucket/train.csv")
    val featuresDF = loadCSV(spark, "gs://walmart_recruiting_data_bucket/features.csv")
    val storesDF = loadCSV(spark, "gs://walmart_recruiting_data_bucket/stores.csv")

    // Step 2: Clean and Cache Features and Stores
    val cleanedFeaturesDF = cleanFeatures(featuresDF).cache()
    val broadcastStoresDF = broadcast(cleanStores(storesDF))

    // Step 3: Validate and Filter Sales Data
    val validatedSalesDF = validateSalesData(trainDF)
    val positiveSalesDF = validatedSalesDF.filter(col("Weekly_Sales") >= 0)

    // Step 4: Enrich Data
    val enrichedDF = enrichSalesData(positiveSalesDF, cleanedFeaturesDF, broadcastStoresDF).persist(StorageLevel.MEMORY_AND_DISK)
    // Step 5: Aggregations
    computeStoreLevelMetrics(enrichedDF, "gs://walmart_recruiting_data_bucket/store_metrics/")
    computeDepartmentMetrics(enrichedDF, "gs://walmart_recruiting_data_bucket/department_metrics/")
    computeHolidayMetrics(enrichedDF, "gs://walmart_recruiting_data_bucket/holiday_metrics/")

    // Step 6: Storage Optimization
    saveEnrichedData(enrichedDF, "gs://walmart_recruiting_data_bucket/enriched_data/")

    println("Data aggregation and metrics computation completed successfully")
    spark.stop()
  }

  // Load CSV
  def loadCSV(spark: SparkSession, path: String): DataFrame = {
    spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)
  }

  // Clean Features Data
  def cleanFeatures(featuresDF: DataFrame): DataFrame = {
    featuresDF.filter(
      col("Store").isNotNull &&
        col("Date").isNotNull &&
        col("Temperature").isNotNull &&
        col("Fuel_Price").isNotNull
    ).withColumnRenamed("IsHoliday", "Features_IsHoliday")
  }
  // Clean Stores Data
  def cleanStores(storesDF: DataFrame): DataFrame = {
    storesDF.filter(
      col("Store").isNotNull &&
        col("Type").isNotNull &&
        col("Size").isNotNull
    )
  }

  // Validate Sales Data
  def validateSalesData(trainDF: DataFrame): DataFrame = {
    trainDF.filter(
      col("Store").isNotNull &&
        col("Dept").isNotNull &&
        col("Date").isNotNull &&
        col("Weekly_Sales").isNotNull &&
        col("IsHoliday").isNotNull
    )
  }

  // Enrich Sales Data
  def enrichSalesData(salesDF: DataFrame, featuresDF: DataFrame, storesDF: DataFrame): DataFrame = {
    salesDF
      .join(featuresDF, Seq("Store", "Date"), "inner")
      .join(storesDF, Seq("Store"), "inner")
      .repartition(col("Store"), col("Date"))
  }// Compute Store-Level Metrics
  def computeStoreLevelMetrics(enrichedDF: DataFrame, outputPath: String): Unit = {
    val storeMetricsDF = enrichedDF.groupBy("Store")
      .agg(
        sum("Weekly_Sales").as("Total_Weekly_Sales"),
        avg("Weekly_Sales").as("Average_Weekly_Sales")
      )
      .cache()

    storeMetricsDF.write
      .mode("overwrite")
      .json(outputPath)

    storeMetricsDF.unpersist()
  }

  // Compute Department Metrics
  def computeDepartmentMetrics(enrichedDF: DataFrame, outputPath: String): Unit = {
    val departmentMetricsDF = enrichedDF.groupBy("Store", "Dept")
      .agg(
        sum("Weekly_Sales").as("Total_Dept_Sales"),
        avg("Weekly_Sales").as("Average_Dept_Sales"),
        collect_list("Weekly_Sales").as("Weekly_Sales_Trend")
      )
      .cache()

    departmentMetricsDF.write
      .mode("overwrite")
      .json(outputPath)

    departmentMetricsDF.unpersist()
  }

// Compute Holiday Metrics
def computeHolidayMetrics(enrichedDF: DataFrame, outputPath: String): Unit = {
  val holidayMetricsDF = enrichedDF.groupBy("Dept", "IsHoliday")
    .agg(
      sum("Weekly_Sales").as("Total_Weekly_Sales"),
      avg("Weekly_Sales").as("Average_Weekly_Sales")
    )
    .withColumn(
      "Holiday_Type",
      when(col("IsHoliday"), "Holiday").otherwise("Non-Holiday")
    )
    .drop("IsHoliday")
    .cache()

  holidayMetricsDF.write
    .mode("overwrite")
    .json(outputPath)

  holidayMetricsDF.unpersist()
}

  // Save Enriched Data
  def saveEnrichedData(enrichedDF: DataFrame, outputPath: String): Unit = {
    enrichedDF.write
      .mode("overwrite")
      .partitionBy("Store", "Date")
      .parquet(outputPath)
  }
}
