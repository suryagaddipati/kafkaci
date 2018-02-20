package kafkaci

import java.util
import java.util.{Collections, Properties}

import com.ovoenergy.kafka.serialization.circe._
import kafkaci.models.GithubWebhook
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
// Import the Circe generic support
import io.circe.generic.auto._
import io.circe.syntax._



object GithubWebhookProducer extends App{
  val props = new Properties
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val songProducer = new KafkaProducer[String, GithubWebhook](props, new StringSerializer,circeJsonSerializer[GithubWebhook])
  while (true) {
    songProducer.send(new ProducerRecord[String, GithubWebhook]("Github-Webhooks",GithubWebhook("suryagaddipati/meow")))
    Thread.sleep(1000L)
  }

}
