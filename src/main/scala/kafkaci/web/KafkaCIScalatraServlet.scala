package kafkaci.web

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.complete
import kafkaci.Topics.GITHUB_WEBHOOKS_COUNT_STORE
import kafkaci.util.StoreHelpers.waitUntilStoreIsQueryable
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.scalatra._

class KafkaCIScalatraServlet(streams : KafkaStreams) extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

  get("/webhook-count"){
    val repoName = params("repo")
    val webhookCountStore = waitUntilStoreIsQueryable(GITHUB_WEBHOOKS_COUNT_STORE, QueryableStoreTypes.keyValueStore[String, Long], streams)
    val count = webhookCountStore.get(repoName)
    complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1> Count: ${count}</h1>"))
  }
  post("/create-job"){
     val repoName = params("repo")
    views.html.hello()
  }


}
