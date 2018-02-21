package kafkaci.producers

import java.util.Properties

import com.ovoenergy.kafka.serialization.circe._
import kafkaci.Topics._
import kafkaci.models.Job
import kafkaci.models.github.GithubWebhook
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
// Import the Circe generic support
import io.circe.generic.auto._



object GithubWebhookProducer extends App{
  val props = new Properties
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  githubWebhookProducer
//  jobCreateRequestProducer

  private def githubWebhookProducer = {
    val songProducer = new KafkaProducer[String, GithubWebhook](props, new StringSerializer, circeJsonSerializer[GithubWebhook])
    while (true) {
      songProducer.send(new ProducerRecord[String, GithubWebhook](GITHUB_WEBHOOKS, "suryagaddipati/meow", GithubWebhook("suryagaddipati/meow")))
      Thread.sleep(1000L)
    }
  }

  private def jobCreateRequestProducer = {
    val songProducer = new KafkaProducer[String, Job](props, new StringSerializer, circeJsonSerializer[Job])
    songProducer.send(new ProducerRecord[String, Job](JOB_CREATE_REQUESTS, "suryagaddipati/meow",Job("suryagaddipati/meow")))
  }

}
