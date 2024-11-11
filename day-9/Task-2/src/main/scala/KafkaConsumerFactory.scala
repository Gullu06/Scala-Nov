// KafkaConsumerFactory.scala
import org.apache.kafka.clients.consumer.ConsumerConfig
import java.util.Properties

object KafkaConsumerFactory {
  def createConsumerProps(groupId: String): Properties = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest") // Start reading from the beginning if no offset exists
    props
  }
}
