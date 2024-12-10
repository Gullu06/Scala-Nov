import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import scala.util.Random
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

object WeeklySalesProducer {
  def main(args: Array[String]): Unit = {
    val topicName = "weekly_sales"

    val producerProps = new Properties()
    producerProps.put("bootstrap.servers", "localhost:9092")
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val kafkaProducer = new KafkaProducer[String, String](producerProps)
    val randomGenerator = new Random()

    val storeIds = (1 to 50).map(i => f"store_$i%03d")
    val deptIds = (1 to 10).map(i => f"dept_$i%02d")

    try {
      while (true) {
        val randomStore = storeIds(randomGenerator.nextInt(storeIds.length))
        val randomDept = deptIds(randomGenerator.nextInt(deptIds.length))
        val randomWeeklySales = (randomGenerator.nextDouble() * 10000).round
        val randomDate = "2023-12-01"

        val salesJson = ("Store" -> randomStore) ~
          ("Dept" -> randomDept) ~
          ("Weekly_Sales" -> randomWeeklySales) ~
          ("Date" -> randomDate)

        val salesMessage = compact(render(salesJson))
        val kafkaRecord = new ProducerRecord[String, String](topicName, randomStore, salesMessage)
        kafkaProducer.send(kafkaRecord)

        println(s"Produced sales message: $salesMessage")
        Thread.sleep(1000)
      }
    } finally {
      kafkaProducer.close()
    }
  }
}
