import kafkaci.web._
import org.scalatra._
import javax.servlet.ServletContext

import kafkaci.Config.streamsConfiguration
import kafkaci.KafkaCITopology
import org.apache.kafka.streams.KafkaStreams

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    val streams = new KafkaStreams(KafkaCITopology.get, streamsConfiguration)
    streams.cleanUp()
    streams.start()

    context.mount(new WebApp(streams), "/*")
  }
}
