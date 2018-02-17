package kafkaci

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{LongSerializer, StringSerializer}

object GithubWebhookProducer extends App{
  val props = new Properties
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  val songProducer = new KafkaProducer[String, String](props, new StringSerializer, new StringSerializer)
  while (true) {
    songProducer.send(new ProducerRecord[String, String]("Github-Webhooks", "uk", "suryagaddipati/meow"))
    Thread.sleep(1000L)
  }

}
