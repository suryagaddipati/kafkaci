import kafkaci.web._
import org.scalatra._
import javax.servlet.ServletContext

import kafkaci.Config.streamsConfiguration
import kafkaci.Topology
import org.apache.kafka.streams.KafkaStreams

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    val streams = new KafkaStreams(Topology.get, streamsConfiguration)
    streams.cleanUp()
    streams.start()
    context.mount(new KafkaCIScalatraServlet(streams), "/*")
  }
}
