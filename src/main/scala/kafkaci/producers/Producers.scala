package kafkaci.producers

import java.util.Properties

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import com.ovoenergy.kafka.serialization.circe._
import kafkaci.Topics._
import kafkaci.models.github.GithubWebhook
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
// Import the Circe generic support
import io.circe.generic.auto._



object Producers extends App{
  val props = new Properties
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  githubWebhookProducer
//  jobCreateRequestProducer

  private def githubWebhookProducer = {
    val songProducer = new KafkaProducer[String, GithubWebhook](props, new StringSerializer, circeJsonSerializer[GithubWebhook])
    while (true) {
      songProducer.send(new ProducerRecord[String, GithubWebhook](GITHUB_WEBHOOKS, GithubWebhook("suryagaddipati/meow")))
      Thread.sleep(1000L)
    }
  }



      //new KafkaProducer[String, String](props)send(new ProducerRecord[String, String](JOB_CREATE_REQUESTS, reponame,reponame))
}
