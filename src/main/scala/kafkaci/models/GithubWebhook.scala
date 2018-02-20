package kafkaci.models

import com.ovoenergy.kafka.serialization.circe.circeJsonSerializer
import com.ovoenergy.kafka.serialization.circe.circeJsonDeserializer
import org.apache.kafka.common.serialization.{Serdes, StringDeserializer, StringSerializer}
import io.circe.generic.auto._

case class GithubWebhook(repo: String)

case class GithubWebhookSerde() extends  WrapperSerde[GithubWebhook](circeJsonSerializer[GithubWebhook], circeJsonDeserializer[GithubWebhook])
