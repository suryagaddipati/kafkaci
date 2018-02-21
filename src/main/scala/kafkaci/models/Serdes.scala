package kafkaci.models

import com.ovoenergy.kafka.serialization.circe.{circeJsonDeserializer, circeJsonSerializer}
import io.circe.generic.auto._
import com.ovoenergy.kafka.serialization.core._
import com.ovoenergy.kafka.serialization.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import kafkaci.models.github.GithubWebhook

object Serdes {
  def githubWebhookSerde = new WrapperSerde[GithubWebhook](circeJsonSerializer[GithubWebhook], circeJsonDeserializer[GithubWebhook])
  def buildSerde = new WrapperSerde[Build](circeJsonSerializer[Build], circeJsonDeserializer[Build])
  def projectSerde = new WrapperSerde[Project](circeJsonSerializer[Project], circeJsonDeserializer[Project])
//  def serdes[T] = new WrapperSerde[T](circeJsonSerializer[T], circeJsonDeserializer[T])
}
