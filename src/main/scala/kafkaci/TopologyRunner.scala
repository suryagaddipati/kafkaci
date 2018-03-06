package kafkaci

import org.apache.kafka.streams.KafkaStreams

object TopologyRunner {
  def main(args: Array[String]) {
    println("Hello, world")
    val streams = new KafkaStreams(KafkaCITopology.get, Config.streamsConfiguration)
    streams.cleanUp()
    streams.start()

  }
}
