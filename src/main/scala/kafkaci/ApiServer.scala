package kafkaci

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import kafkaci.Topics._
import kafkaci.producers.Producers
import kafkaci.util.StoreHelpers._
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes

import scala.io.StdIn

object ApiServer  extends App {
  def start(streams: KafkaStreams): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val songCountStore = waitUntilStoreIsQueryable(GITHUB_WEBHOOKS_COUNT, QueryableStoreTypes.keyValueStore[String,Long],streams)

    val route =
      path("count") {
        get {
          val count =  songCountStore.get("suryagaddipati/meow")
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1> Count: ${count}</h1>"))
        }
      } ~
        ignoreTrailingSlash{
          path("create-job") {
            post {
              (formField('repoName)) { (repoName) =>
                complete(Producers.sendJobCreateRequest(repoName, system))
              }
            }
          }
        }




    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000) // get this from config

    println(s"Server online at http://localhost:9000/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => {system.terminate(); streams.close()}) // and shutdown when done
  }


}
