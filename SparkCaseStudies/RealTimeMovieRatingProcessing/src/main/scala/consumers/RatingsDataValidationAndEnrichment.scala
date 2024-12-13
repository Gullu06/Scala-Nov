package consumers

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

object RatingsDataValidationAndEnrichment {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Ratings Validation and Enrichment")
      .master("local[*]")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    // Define schemas
    val ratingsSchema = new StructType()
      .add("userId", StringType)
      .add("movieId", StringType)
      .add("rating", DoubleType)
      .add("timestamp", StringType)

    val moviesSchema = new StructType()
      .add("movieId", StringType)
      .add("title", StringType)
      .add("genres", StringType)

    val usersSchema = new StructType()
      .add("userId", StringType)
      .add("age", StringType)
      .add("gender", StringType)
      .add("location", StringType)

    // Load static movie metadata
    var moviesDF = spark.read
      .option("header", "true")
      .schema(moviesSchema)
      .csv("gs://movie_bucket_pc/movie.csv")
      .dropDuplicates("movieId")
      .cache()

    // Load static user demographics
    var usersDF = spark.read
      .option("header", "true")
      .schema(usersSchema)
      .csv("gs://movie_bucket_pc/user_data.csv")
      .dropDuplicates("userId")
      .cache()

    // Read real-time movie metadata updates
    val moviesUpdatesStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "movies-metadata")
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(value AS STRING) as jsonString")
      .select(from_json(col("jsonString"), moviesSchema).as("data"))
      .select("data.*")

    // Read real-time user demographics updates
    val usersUpdatesStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "users-demographics")
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(value AS STRING) as jsonString")
      .select(from_json(col("jsonString"), usersSchema).as("data"))
      .select("data.*")

    // Merge static datasets with updates
    moviesUpdatesStream.writeStream.foreachBatch { (batchDF: DataFrame, batchId: Long) =>
      moviesDF = moviesDF.union(batchDF).dropDuplicates("movieId")
    }.outputMode("update").start()

    usersUpdatesStream.writeStream.foreachBatch { (batchDF: DataFrame, batchId: Long) =>
      usersDF = usersDF.union(batchDF).dropDuplicates("userId")
    }.outputMode("update").start()

    // Read real-time ratings data
    val ratingsStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "movie-ratings")
      .option("startingOffsets", "earliest")
      .load()
      .selectExpr("CAST(value AS STRING) as jsonString")
      .select(from_json(col("jsonString"), ratingsSchema).as("data"))
      .select("data.*")

    // Validate and enrich ratings data
    val validatedRatings = ratingsStream.filter(col("rating").between(0.5, 5.0))
    val enrichedWithMovies = validatedRatings.join(broadcast(moviesDF), Seq("movieId"), "inner")
    val fullyEnrichedRatings = enrichedWithMovies.join(broadcast(usersDF), Seq("userId"), "inner")

    // Write enriched data to console
    val query = fullyEnrichedRatings.writeStream
      .outputMode("append")
      .format("console")
      .option("checkpointLocation", "gs://movie_bucket_pc/checkpoints/full-enrichment")
      .start()

    // Write enriched data to Parquet
    val query1 = fullyEnrichedRatings.writeStream
      .outputMode("append")
      .format("parquet")
      .option("path", "gs://movie_bucket_pc/enriched_ratings") // Correct the 'path' option here
      .option("checkpointLocation", "gs://movie_bucket_pc/enriched-ratings/") // Correct the checkpoint
      .start()

    query.awaitTermination()
    query1.awaitTermination()
  }
}

