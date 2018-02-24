package kafkaci.producers

import java.util.Properties
import java.util.concurrent.Future

import com.ovoenergy.kafka.serialization.circe._
import kafkaci.Topics._
import kafkaci.models.github.GithubWebhook
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
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


  def projectCreateRequest(repo: String): Future[RecordMetadata] ={
    val projectCreateRequestProducer = new KafkaProducer[String, String](props, new StringSerializer, new StringSerializer)
    projectCreateRequestProducer.send(new ProducerRecord[String, String](PROJECT_CREATE_REQUESTS, repo,repo))
  }
}
