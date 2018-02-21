package kafkaci

import java.util.Properties

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.lightbend.kafka.scala.streams.{KGroupedStreamS, KStreamS, KTableS, StreamsBuilderS}
import kafkaci.models.Build
import kafkaci.models.github.GithubWebhook
import kafkaci.models.Serdes._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{Serde, Serdes}
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.kafka.streams.{Consumed, KafkaStreams, StreamsConfig}

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



    val builder = new StreamsBuilderS()

    //repo-name/hook
    val githubWebhooks: KStreamS[String,GithubWebhook] = builder.stream[String, GithubWebhook](GITHUB_WEBHOOKS,Consumed.`with`(Serdes.String,githubWebhookSerde))


    //repo-name/count
    val githubWebhookCount: KTableS[String,Long] = githubWebhooks.map((k,v) => (k,v.repo)).groupBy((k,v) =>  k ).count(GITHUB_WEBHOOKS_COUNT)

    //repo-name/build
    val builds: KStreamS[String,Build] = githubWebhooks.leftJoin(githubWebhookCount,(hook:GithubWebhook,count: Long)  => Build(count+1,hook))

    //repo-name/builds
    val buildTable: KGroupedStreamS[String,Build] = builds.groupBy((k,v)=>k)



//      .to("build", Produced.`with`(Serdes.String,buildSerde))

    val streams = new KafkaStreams(builder.build, streamsConfiguration)
    streams.cleanUp()
    streams.start()
    val songCountStore = waitUntilStoreIsQueryable(GITHUB_WEBHOOKS_COUNT, QueryableStoreTypes.keyValueStore[String,Long],streams)


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
      .onComplete(_ => {system.terminate(); streams.close()}) // and shutdown when done
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

  import org.apache.kafka.streams.KafkaStreams
  import org.apache.kafka.streams.errors.InvalidStateStoreException
  import org.apache.kafka.streams.state.QueryableStoreType

  def waitUntilStoreIsQueryable[T](storeName: String, queryableStoreType: QueryableStoreType[T], streams: KafkaStreams): T =
    try
      streams.store(storeName, queryableStoreType)
    catch {
      case ignored: InvalidStateStoreException =>
        // store not yet ready for querying
        Thread.sleep(100)
        waitUntilStoreIsQueryable(storeName,queryableStoreType,streams)
    }
}