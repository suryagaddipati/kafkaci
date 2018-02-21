package kafkaci

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.stream.ActorMaterializer
import kafkaci.Main.appServerPort
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import  Topics._
import scala.io.StdIn

object ApiServer {
  def start(streams: KafkaStreams): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher
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
