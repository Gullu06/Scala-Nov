import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.types._

import scala.concurrent.ExecutionContextExecutor

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("MovieLensApiService")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val spark = SparkSession.builder()
      .appName("Movie Lens Api Service")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("google.cloud.auth.service.account.enable", "true")
      .master("local[*]")
      .getOrCreate()

    // Paths to aggregated metrics
    val aggregatedDataPath = s"gs://movie_bucket_pc/aggregated_metrics//"
    val perMovieMetricsPath = s"$aggregatedDataPath/per_movie_metrics/"
    val perGenreMetricsPath = s"$aggregatedDataPath/per_genre_metrics/"
    val perDemographicMetricsPath = s"$aggregatedDataPath/per_demographic_metrics/"

    // Define schema for movie metrics
    val movieMetricsSchema = StructType(Seq(
      StructField("movie_id", IntegerType, true),
      StructField("movie_name", StringType, true),
      StructField("genre", StringType, true),
      StructField("rating", DoubleType, true),
      StructField("views", LongType, true)
    ))

    // Safely read Parquet files
    def safeReadParquet(path: String): Option[DataFrame] = {
      if (fileExists(path)) {
        val df = spark.read.schema(movieMetricsSchema).parquet(path)
        if (!df.isEmpty) Some(df) else {
          println(s"WARNING: No data found in $path.")
          None
        }
      } else {
        println(s"ERROR: Path does not exist - $path.")
        None
      }
    }

    def fileExists(path: String): Boolean = {
      val hadoopConf = spark.sparkContext.hadoopConfiguration
      val fs = FileSystem.get(hadoopConf)
      fs.exists(new Path(path))
    }

    // Define routes
    val route =
      pathPrefix("api") {
        concat(
          path("movie-metrics") {
            get {
              safeReadParquet(perMovieMetricsPath) match {
                case Some(movieMetricsDF) =>
                  val movieMetrics = movieMetricsDF.collect().map(_.toString()).mkString("\n")
                  complete(movieMetrics)
                case None =>
                  complete(404, "Movie metrics data not found or empty.")
              }
            }
          },
          path("genre-metrics") {
            get {
              safeReadParquet(perGenreMetricsPath) match {
                case Some(genreMetricsDF) =>
                  val genreMetrics = genreMetricsDF.collect().map(_.toString()).mkString("\n")
                  complete(genreMetrics)
                case None =>
                  complete(404, "Genre metrics data not found or empty.")
              }
            }
          },
          path("demographics-metrics") {
            get {
              safeReadParquet(perDemographicMetricsPath) match {
                case Some(demographicMetricsDF) =>
                  val demographicMetrics = demographicMetricsDF.collect().map(_.toString()).mkString("\n")
                  complete(demographicMetrics)
                case None =>
                  complete(404, "Demographics metrics data not found or empty.")
              }
            }
          }
        )
      }

    // Start the server
    val bindingFuture = Http().newServerAt("localhost", 8080).bindFlow(route)
    println("Server online at http://localhost:8080/")
    println("Press RETURN to stop...")

    scala.io.StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
