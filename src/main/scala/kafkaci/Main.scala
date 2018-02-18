package kafkaci

import java.util.Properties

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.lightbend.kafka.scala.streams.{KStreamS, KTableS, StreamsBuilderS}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import scala.io.StdIn

object Main {

  val appServerPort = 9000
  val GITHUB_WEBHOOKS = "Github-Webhooks"
  val GITHUB_WEBHOOKS_COUNT = "Github-Webhooks-Count"


  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    implicit val stringSerde = Serdes.String()
    implicit val longSerde: Serde[Long] = Serdes.Long().asInstanceOf[Serde[Long]]


    val builder = new StreamsBuilderS()
    val githubWebhooks = builder.stream[String, String](GITHUB_WEBHOOKS)

    val webhookCounts: KTableS[String, Long] = githubWebhooks.groupBy((k,v) =>  v ).count(GITHUB_WEBHOOKS_COUNT)

//    webhookCounts.toStream.to(GITHUB_WEBHOOKS_COUNT,Produced.`with`(stringSerde,longSerde))

    val streams = new KafkaStreams(builder.build, streamsConfiguration)
    streams.start()
    Thread.sleep(8000)
    val songCountStore = streams.store(GITHUB_WEBHOOKS_COUNT, QueryableStoreTypes.keyValueStore[String,Long])

    val route =
      path("hello") {
        get {
          val count = songCountStore.get("suryagaddipati/meow")
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1> Count: ${count}</h1>"))
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", appServerPort)

    println(s"Server online at http://localhost:9000/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }


  def streamsConfiguration(): Properties ={
    val streamsConfiguration = new Properties()
    streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "KafkaCI")

    streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, "localhost:" + appServerPort)
    streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafkaCIState")
    streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "100")

    streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    streamsConfiguration
  }
}